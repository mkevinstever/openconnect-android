/*
 * Adapted from OpenVPN for Android
 * Copyright (c) 2012-2013, Arne Schwabe
 * Copyright (c) 2013, Kevin Cernekee
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * In addition, as a special exception, the copyright holders give
 * permission to link the code of portions of this program with the
 * OpenSSL library.
 */

package app.openconnect.core;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import app.openconnect.MainActivity;
import app.openconnect.R;
import app.openconnect.VpnProfile;
import app.openconnect.api.GrantPermissionsActivity;
import app.openconnect.core.VPNLog.LogArrayAdapter;
import app.openconnect.fragments.FeedbackFragment;
import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.Locale;

import org.infradead.libopenconnect.LibOpenConnect;
import org.infradead.libopenconnect.LibOpenConnect.VPNStats;

public class OpenVpnService extends VpnService {

	public static final String TAG = "OpenConnect";

	public static final String START_SERVICE = "app.openconnect.START_SERVICE";
	public static final String START_SERVICE_STICKY = "app.openconnect.START_SERVICE_STICKY";
	public static final String ACTION_VPN_STATUS = "app.openconnect.VPN_STATUS";
	public static final String EXTRA_CONNECTION_STATE = "app.openconnect.connectionState";
	public static final String EXTRA_UUID = "app.openconnect.UUID";

	// These are valid in the CONNECTED state
	public VpnProfile profile;
	public LibOpenConnect.IPInfo ipInfo;
	public String serverName;
	public Date startTime;

	private DeviceStateReceiver mDeviceStateReceiver;
	private SharedPreferences mPrefs;

	private KeepAlive mKeepAlive;
	private int mIdleTimeout;

	private final IBinder mBinder = new LocalBinder();

	private String mUUID;
	private int mStartId;

	private Thread mVPNThread;
	private OpenConnectManagementThread mVPN;

	private UserDialog mDialog;
	private Context mDialogContext;

	private int mActivityConnections;
	private boolean mNotificationActive;

	private int mConnectionState = OpenConnectManagementThread.STATE_DISCONNECTED;
	private String[] mConnectionStateNames;
	private VPNStats mStats = new VPNStats();

	private final VPNLog mVPNLog = new VPNLog();
	private final Handler mHandler = new Handler();

	public class LocalBinder extends Binder {
		public OpenVpnService getService() {
			// Return this instance of LocalService so clients can call public methods
			return OpenVpnService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		String action = intent.getAction();
		if( action !=null && action.equals(START_SERVICE))
			return mBinder;
		else
			return super.onBind(intent);
	}

	@Override
	public void onRevoke() {
		Log.i(TAG, "VPN access has been revoked");
		stopVPN();
	}

	@Override
	public void onCreate() {
		// Restore service state from disk if available
		// This gets overwritten if somebody calls startService()
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mUUID = mPrefs.getString("service_mUUID", "");

		mVPNLog.restoreFromFile(getCacheDir().getAbsolutePath() + "/logdata.ser");
		mConnectionStateNames = getResources().getStringArray(R.array.connection_states);
	}

	@Override
	public void onDestroy() {
		killVPNThread(true);
		if (mDeviceStateReceiver != null) {
			this.unregisterReceiver(mDeviceStateReceiver);
		}
		mVPNLog.saveToFile(getCacheDir().getAbsolutePath() + "/logdata.ser");
	}

	private synchronized boolean doStopVPN() {
		if (mVPN != null) {
			mVPN.stopVPN();
			return true;
		}
		return false;
	}

	private void killVPNThread(boolean joinThread) {
		if (doStopVPN() && joinThread) {
			try {
				mVPNThread.join(1000);
			} catch (InterruptedException e) {
				Log.e(TAG, "OpenConnect thread did not exit");
			}
		}
	}

	@SuppressLint("UnspecifiedImmutableFlag")
	private PendingIntent getMainActivityIntent() {
		// Touching "Configure" on the system VPN dialog will restore the app
		// (same as clicking the launcher icon)
		Intent intent = new Intent(getBaseContext(), MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		PendingIntent startLW;
		startLW = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
		return startLW;
	}

	private void registerDeviceStateReceiver(OpenVPNManagement management) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(DeviceStateReceiver.PREF_CHANGED);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		mDeviceStateReceiver = new DeviceStateReceiver(management, mPrefs);

		// Use ContextCompat.registerReceiver to register the receiver and set a export flag
		ContextCompat.registerReceiver(this, mDeviceStateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
	}

	private void registerKeepAlive() {
		String DNSServer = "8.8.8.8";
		try {
			DNSServer = ipInfo.DNS.get(0);
		} catch (IndexOutOfBoundsException e) {
			// null dns list.
		} catch (Exception e) {
			Log.i(TAG, "server DNS IP is bogus, falling back to " + DNSServer + " for KeepAlive", e);
		}

		int idle = this.mIdleTimeout;
		if (idle < 60 || idle > 7200) idle = 1800;
		idle = idle * 4 / 10;
		Log.d(TAG, "calculated KeepAlive interval: " + idle + " seconds");

		IntentFilter filter = new IntentFilter(KeepAlive.ACTION_KEEPALIVE_ALARM);
		mKeepAlive = new KeepAlive(idle, DNSServer, mDeviceStateReceiver);

		// Use ContextCompat to register the receiver and set a export flag
		ContextCompat.registerReceiver(this, mKeepAlive, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
		mKeepAlive.start(this);
	}

	private void unregisterReceivers() {
		try {
			if (mDeviceStateReceiver != null) {
				unregisterReceiver(mDeviceStateReceiver);
			}
			mDeviceStateReceiver = null;
		} catch (IllegalArgumentException iae) {
			// catch "Receiver not registered" error
			Log.w(TAG, "can't unregister DeviceStateReceiver", iae);
		}

		try {
			if (mKeepAlive != null) {
				mKeepAlive.stop(this);
				unregisterReceiver(mKeepAlive);
			}
			mKeepAlive = null;
		} catch (IllegalArgumentException iae) {
			Log.w(TAG, "can't unregister KeepAlive", iae);
		}
	}

	@SuppressLint("SuspiciousIndentation")
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent == null) {
			Log.e(TAG, "OpenVpnService started with null intent");
			stopSelf();
			return START_NOT_STICKY;
		}

		String action = intent.getAction();
		if (START_SERVICE.equals(action)) {
			return START_NOT_STICKY;
		} else if (START_SERVICE_STICKY.equals(action)) {
			return START_REDELIVER_INTENT;
		}

		// Extract information from the intent.
		mUUID = intent.getStringExtra(EXTRA_UUID);
		if (mUUID == null) {
			return START_NOT_STICKY;
		}
		mPrefs.edit().putString("service_mUUID", mUUID).apply();

		profile = ProfileManager.get(mUUID);
		if (profile == null) {
			return START_NOT_STICKY;
		}

		killVPNThread(true);

		// stopSelfResult(most_recent_startId) will kill the service
		// stopSelfResult(previous_startId) will not
		mStartId = startId;

        mVPN = new OpenConnectManagementThread(getApplicationContext(), profile, this);
        mVPNThread = new Thread(mVPN, "OpenVPNManagementThread");
        mVPNThread.start();

		unregisterReceivers();
		registerDeviceStateReceiver(mVPN);

		ProfileManager.setConnectedVpnProfile(profile);

        return START_NOT_STICKY;
    }

	public Builder getVpnServiceBuilder() {
		VpnService.Builder b = new VpnService.Builder();
		b.setSession(profile.mName);
		b.setConfigureIntent(getMainActivityIntent());
		return b;
	}

	// From: http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	public static String humanReadableByteCount(long bytes, boolean mbit) {
		if(mbit)
			bytes = bytes *8;
		int unit = mbit ? 1000 : 1024;
		if (bytes < unit)
			return bytes + (mbit ? " bit" : " B");

		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = String.valueOf((mbit ? "kMGTPE" : "KMGTPE").charAt(exp-1));
		if(mbit)
			return String.format(Locale.getDefault(),"%.1f %sbit", bytes / Math.pow(unit, exp), pre);
		else 
			return String.format(Locale.getDefault(),"%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	@SuppressLint("DefaultLocale")
	public static String formatElapsedTime(long startTime) {
		StringBuilder sb = new StringBuilder();
		startTime = (new Date().getTime() - startTime) / 1000;
		if (startTime >= 60 * 60 * 24) {
			// days
			sb.append(String.format("%1$d:", startTime / (60 * 60 * 24)));
		}
		if (startTime >= 60 * 60) {
			// hours
			startTime %= 60 * 60 * 24;
			sb.append(String.format("%1$02d:", startTime / (60 * 60)));
			startTime %= 60 * 60;
		}
		// minutes:seconds
		sb.append(String.format("%1$02d:%2$02d", startTime / 60, startTime % 60));
		return sb.toString();
	}

	/* called from the activity on broadcast receipt, or startup */
	public synchronized void startActiveDialog(Context context) {
		if (mDialog != null && mDialogContext == null) {
			mDialogContext = context;
			mDialog.onStart(context);
		}
	}

	/* called when the activity shuts down (mDialog will be re-rendered when the activity starts again) */
	public synchronized void stopActiveDialog(Context context) {
		if (mDialogContext != context) {
			return;
		}
		if (mDialog != null) {
			mDialog.onStop(mDialogContext);
		}
		mDialogContext = null;
	}

	private synchronized void setDialog(UserDialog dialog) {
		mDialog = dialog;
	}

	@SuppressWarnings("deprecation")
	private void updateNotification() {
		int NOTIFICATION_ID = 1;
		if (mDialog != null && mActivityConnections == 0 && !mNotificationActive) {
			mNotificationActive = true;

			Notification.Builder builder = new Notification.Builder(this)
		            .setSmallIcon(R.drawable.ic_stat_vpn)
		            .setContentTitle(getString(R.string.notification_input_needed))
		            .setContentText(getString(R.string.notification_touch_here))
		            .setContentIntent(getMainActivityIntent());

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, builder.getNotification());
            mNotificationActive = true;
		} else if ((mDialog == null || mActivityConnections > 0) && mNotificationActive) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(NOTIFICATION_ID);
            mNotificationActive = false;
		}
	}

	private void wakeUpActivity() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Intent vpnstatus = new Intent(ACTION_VPN_STATUS);
				vpnstatus.putExtra(EXTRA_CONNECTION_STATE, mConnectionState);
				vpnstatus.putExtra(EXTRA_UUID, mUUID);
				sendBroadcast(vpnstatus, permission.ACCESS_NETWORK_STATE);

				updateNotification();

				if (mConnectionState == OpenConnectManagementThread.STATE_CONNECTED &&
						mKeepAlive == null) {
					registerKeepAlive();
					FeedbackFragment.recordUse(getApplicationContext(), true);
				}
			}
		});
	}

	public void updateActivityRefcount(int num) {
		mActivityConnections += num;
		updateNotification();
	}

	/* called from the VPN thread; blocks until user responds */
	public Object promptUser(UserDialog dialog) {
		if (dialog == null) {
			throw new IllegalArgumentException("UserDialog cannot be null.");
		}
		Object ret = dialog.earlyReturn();
		if (ret != null) {
			return ret;
		}
		setDialog(dialog);
		wakeUpActivity();
		try {
			ret = mDialog.waitForResponse();
		} catch (Exception e) {
			Log.e("promptUser", "Error waiting for user response", e);

			ret = getDefaultResponse();
		} finally {
			setDialog(null);
		}
		return ret;
	}

	private Object getDefaultResponse() {
		return null;
	}


	public synchronized void threadDone() {
		final int startId = mStartId;
		Log.i(TAG, "VPN thread has terminated");
		mVPN = null;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (!stopSelfResult(startId)) {
					Log.w(TAG, "Not stopping service due to startId mismatch");
				} else {
					unregisterReceivers();
					Log.i(TAG, "Receivers unregistered successfully");
				}
			}
		});
	}


	public synchronized void setConnectionState(int state) {
		if (state == OpenConnectManagementThread.STATE_CONNECTED &&
				mConnectionState != OpenConnectManagementThread.STATE_CONNECTED) {
			startTime = new Date();
			Log.i(TAG, "Connection started at: " + startTime);
		}
		mConnectionState = state;
		wakeUpActivity();
		Log.d(TAG, "Connection state changed to: " + state);
	}


	public synchronized int getConnectionState() {
		return mConnectionState;
	}

	public String getConnectionStateName() {
		return mConnectionStateNames[getConnectionState()];
	}

	public void requestStats() {
		if (mVPN != null) {
			mVPN.requestStats();
		}
	}

	public synchronized void setStats(VPNStats stats) {
		if (stats != null) {
			mStats = stats;
		}
		wakeUpActivity();
	}

	public synchronized VPNStats getStats() {
		return mStats;
	}

	public synchronized void setIPInfo(LibOpenConnect.IPInfo ipInfo, String serverName, int idleTimeout) {
		this.ipInfo = ipInfo;
		this.serverName = serverName;
		this.mIdleTimeout = idleTimeout;
	}

	public LogArrayAdapter getArrayAdapter(Context context) {
		return mVPNLog.getArrayAdapter(context);
	}

	public void putArrayAdapter(LogArrayAdapter adapter) {
		if (adapter != null) {
			mVPNLog.putArrayAdapter(adapter);
		}
	}

	public void log(final int level, final String msg) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mVPNLog.add(level, msg);
			}
		});
	}

	public void clearLog() {
		mVPNLog.clear();
	}

	/*public String dumpLog() {
		return mVPNLog.dump();
	}*/

	public String getReconnectName() {
		VpnProfile p = ProfileManager.get(mUUID);
		return p == null ? null : p.getName();
	}

	public void startReconnectActivity(Context context) {
		Intent intent = new Intent(context, GrantPermissionsActivity.class);
		intent.putExtra(getPackageName() + GrantPermissionsActivity.EXTRA_UUID, mUUID);
		context.startActivity(intent);
	}

	public void stopVPN() {
		killVPNThread(false);
		ProfileManager.setConnectedVpnProfileDisconnected();
	}
}