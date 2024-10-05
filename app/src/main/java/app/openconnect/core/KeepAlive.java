/*
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

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;

public class KeepAlive extends BroadcastReceiver {

	public static final String TAG = "OpenConnect";

	public static final String ACTION_KEEPALIVE_ALARM = "app.openconnect.KEEPALIVE_ALARM";

	private boolean mConnectionActive;

	private PendingIntent mPendingIntent;

	private PowerManager.WakeLock mWakeLock;

	private Handler mWorkerHandler;
	private Handler mMainHandler;

	private String mDNSServer;
	private int mBaseDelayMs;
	private DeviceStateReceiver mDeviceStateReceiver;

	private String mDNSHost = "www.google.com";

	public KeepAlive(int seconds, String DNSServer, DeviceStateReceiver deviceStateReceiver) {
		mBaseDelayMs = seconds * 1000;
		mDNSServer = DNSServer;
		mDeviceStateReceiver = deviceStateReceiver;
	}

	// Bypass the resolver so we know we're never getting a cached result, contacting
	// the wrong server, using an incorrect timeout, etc.
	private byte[] buildDNSQuery(byte transID[], String hostname) {
		byte prefix[] = {
				0x01, 0x00,				// Flags: standard query, recursion desired
				0x00, 0x01,				// Questions: 1
				0x00, 0x00,				// Answer RRs: 0
				0x00, 0x00,				// Authority RRs: 0
				0x00, 0x00,				// Additional RRs: 0
		};
		byte suffix[] = {
				0x00, 0x01,				// Type: A
				0x00, 0x01,				// Class: IN
		};

		byte name[] = new byte[hostname.length() + 2];
		int i = 0;
		for (String s : hostname.split("\\.")) {
			name[i++] = (byte)s.length();
			for (int j = 0; j < s.length(); j++) {
				name[i++] = (byte)s.charAt(j);
			}
		}

		byte q[] = new byte[transID.length + prefix.length + name.length + suffix.length];

		System.arraycopy(transID, 0, q, 0, transID.length);
		System.arraycopy(prefix, 0, q, transID.length, prefix.length);
		System.arraycopy(name, 0, q, transID.length + prefix.length, name.length);
		System.arraycopy(suffix, 0, q, transID.length + prefix.length + name.length, suffix.length);

		return q;
	}

	private DatagramSocket openSocket() {
		DatagramSocket sock;
		try {
			sock = new DatagramSocket();
			sock.connect(InetAddress.getByName(mDNSServer), 53);
		} catch (Exception e) {
			Log.e(TAG, "KeepAlive: unexpected socket exception", e);
			return null;
		}
		return sock;
	}

	private byte[] receiveDNSResponse(DatagramSocket sock, int timeoutMs) {
		byte data[] = new byte[1024];
		DatagramPacket p = new DatagramPacket(data, 1024);
		try {
			sock.setSoTimeout(timeoutMs);
			sock.receive(p);
		} catch (IOException e) {
			return null;
		}
		return data;
	}

	private boolean sendDNSQuery(DatagramSocket sock) {
		byte transID[] = new byte[2];
		Random r = new Random();
		transID[0] = (byte)r.nextInt(256);
		transID[1] = (byte)r.nextInt(256);

		byte q[] = buildDNSQuery(transID, mDNSHost);
		try {
			sock.send(new DatagramPacket(q, q.length));
		} catch (IOException e) {
			Log.w(TAG, "KeepAlive: error sending DNS request", e);
			return false;
		}

		int timeoutMs = 10000;
		boolean gotResponse = false;

		while (true) {
			byte data[] = receiveDNSResponse(sock, timeoutMs);
			if (data == null) {
				break;
			}
			gotResponse = true;
			if (data[0] == transID[0] && data[1] == transID[1]) {
				Log.d(TAG, "KeepAlive: good reply from server");
				return true;
			}
			// lower timeout and see if there are any more packets waiting
			timeoutMs = 100;
		}

		if (gotResponse) {
			Log.w(TAG, "KeepAlive: got reply with bad transaction ID");
		} else {
			Log.i(TAG, "KeepAlive: no reply was received");
		}
		return false;
	}

	private void handleKeepAlive(final Context context) {
		mPendingIntent = null;
		if (!mConnectionActive) {
			return;
		}

		mWakeLock.acquire();
		mDeviceStateReceiver.setKeepalive(true);
		mWorkerHandler.post(new Runnable() {
			@Override
			public void run() {
				// DNS query runs on worker thread
				DatagramSocket sock = openSocket();
				final boolean result;
				if (sock != null) {
					result = sendDNSQuery(sock);
					sock.close();
				} else {
					result = false;
				}

				mMainHandler.post(new Runnable() {
					@Override
					public void run() {
						// this runs back on the main thread
						scheduleNext(context, result ? mBaseDelayMs : (mBaseDelayMs / 2));
						mDeviceStateReceiver.setKeepalive(false);
						mWakeLock.release();
					}
				});
			}
		});
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals(ACTION_KEEPALIVE_ALARM)) {
			handleKeepAlive(context);
		}
	}

	@SuppressLint("UnspecifiedImmutableFlag")
	private void scheduleNext(Context context, int delayMs) {
		Intent intent = new Intent("app.openconnect.KEEPALIVE_ALARM");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		} else {
			mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMs, mPendingIntent);
	}

	@SuppressLint("InvalidWakeLockTag")
    public void start(Context context) {
		if (mBaseDelayMs == 0) {
			return;
		}

		HandlerThread t = new HandlerThread("KeepAlive");
		t.start();
		mWorkerHandler = new Handler(t.getLooper());
		mMainHandler = new Handler();

		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "KeepAlive");

		mConnectionActive = true;
		scheduleNext(context, mBaseDelayMs);
	}

	public void stop(Context context) {
		mConnectionActive = false;
		if (mPendingIntent != null) {
			AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			am.cancel(mPendingIntent);
			mPendingIntent = null;
		}
		if (mWorkerHandler != null) {
			mWorkerHandler.post(new Runnable() {
				@Override
				public void run() {
					Looper.myLooper().quit();
				}
			});
		}
	}
}