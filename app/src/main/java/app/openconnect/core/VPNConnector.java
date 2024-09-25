package app.openconnect.core;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.infradead.libopenconnect.LibOpenConnect.VPNStats;

import app.openconnect.R;
import app.openconnect.core.OpenVpnService.LocalBinder;

public abstract class VPNConnector {

	public static final String TAG = "OpenConnect";

	public OpenVpnService service;
	public VPNStats oldStats = new VPNStats();
	public VPNStats newStats = new VPNStats();
	public VPNStats deltaStats = new VPNStats();
	public boolean statsValid = false;

	private Context mContext;
	private boolean mIsActivity;
	private BroadcastReceiver mReceiver;
	private String mOwnerName;

	private Handler mStatsHandler;
	private Runnable mStatsRunnable;
	private int mStatsCount = 0;
	private boolean isReceiverRegistered = false; // trace status

	public abstract void onUpdate(OpenVpnService service);

	public VPNConnector(Context ctx, boolean isActivity) {
		mContext = ctx;
		mIsActivity = isActivity;

		Intent intent = new Intent(mContext, OpenVpnService.class);
		intent.setAction(OpenVpnService.START_SERVICE);
		mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (service != null) {
					onUpdate(service);
				}
			}
		};

		mOwnerName = mContext.getClass().getSimpleName();

		mStatsHandler = new Handler();
		mStatsRunnable = new Runnable() {
			@Override
			public void run() {
				if (service != null) {
					oldStats = newStats;
					newStats = service.getStats();

					deltaStats.rxBytes = newStats.rxBytes - oldStats.rxBytes;
					deltaStats.rxPkts = newStats.rxPkts - oldStats.rxPkts;
					deltaStats.txBytes = newStats.txBytes - oldStats.txBytes;
					deltaStats.txPkts = newStats.txPkts - oldStats.txPkts;

					service.requestStats();

					// wait until we've received at least two samples, so the delta is correct
					if (++mStatsCount >= 2) {
						statsValid = true;
					}
				}
				mStatsHandler.postDelayed(mStatsRunnable, 1000);
			}
		};
		mStatsRunnable.run();
	}

	public void stopActiveDialog() {
		stop();
		if (service != null) {
			service.stopActiveDialog(mContext);
		}
	}

	public void stop() {
		if (mReceiver != null && isReceiverRegistered) {
			mContext.unregisterReceiver(mReceiver);
			mReceiver = null;
			isReceiverRegistered = false; // Update status
		}

		if (mStatsHandler != null) {
			mStatsHandler.removeCallbacks(mStatsRunnable);
			mStatsHandler = null;
		}
	}

	public void unbind() {
		stop();
		if (service != null) {
			service.updateActivityRefcount(mIsActivity ? -1 : 0);
		}
		mContext.unbindService(mConnection);
	}

	public String getByteCountSummary() {
		if (!statsValid) {
			return "";
		}
		return mContext.getString(R.string.statusline_bytecount,
				OpenVpnService.humanReadableByteCount(newStats.rxBytes, false),
				OpenVpnService.humanReadableByteCount(deltaStats.rxBytes, true),
				OpenVpnService.humanReadableByteCount(newStats.txBytes, false),
				OpenVpnService.humanReadableByteCount(deltaStats.txBytes, true));
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
			LocalBinder binder = (LocalBinder) serviceBinder;
			service = binder.getService();
			service.updateActivityRefcount(mIsActivity ? 1 : 0);
			onUpdate(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			service = null;
			Log.w(TAG, mOwnerName + " was forcibly unbound from OpenVpnService");
		}
	};
}