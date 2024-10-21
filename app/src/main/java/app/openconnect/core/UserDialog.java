
package app.openconnect.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;

/**
 * Abstract class for handling user dialogs and managing user preferences.
 */
public abstract class UserDialog {
	public static final String TAG = "OpenConnect";

	private Object mResult;
	private boolean mDialogUp;
	protected SharedPreferences mPrefs;

	// Use ConcurrentHashMap for thread-safe operations if multiple threads access deferred prefs
	private static final HashMap<String, DeferredPref> mDeferredPrefs = new HashMap<>();

	public UserDialog(SharedPreferences prefs) {
		mPrefs = prefs;
	}

	// Wait for user response
	public Object waitForResponse() {
		synchronized (this) {
			while (mResult == null) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt(); // Restore interrupted status
				}
			}
		}
		return mResult;
	}

	// Finish the dialog and notify waiting threads
	protected void finish(Object result) {
		synchronized (this) {
			if (mDialogUp) {
				mResult = result;
				this.notifyAll();
			}
		}
	}

	// Abstract class for deferred preferences
	private abstract class DeferredPref {
		protected SharedPreferences mPrefs;
		protected String mKey;

		public DeferredPref(SharedPreferences prefs, String name) {
			mPrefs = prefs;
			mKey = name;
		}
		public abstract void commit();
	}

	// Deferred string preference implementation
	private class DeferredStringPref extends DeferredPref {
		public String value;

		public DeferredStringPref(SharedPreferences prefs, String name, String newValue) {
			super(prefs, name);
			value = newValue;
		}

		public void commit() {
			mPrefs.edit().putString(mKey, value).apply(); // Use apply() for async commit
		}
	}

	// Deferred boolean preference implementation
	private class DeferredBooleanPref extends DeferredPref {
		public boolean value;

		public DeferredBooleanPref(SharedPreferences prefs, String name, boolean newValue) {
			super(prefs, name);
			value = newValue;
		}

		public void commit() {
			mPrefs.edit().putBoolean(mKey, value).apply(); // Use apply() for async commit
		}
	}

	// Clear all deferred preferences
	public static void clearDeferredPrefs() {
		mDeferredPrefs.clear();
	}

	// Write all deferred preferences to SharedPreferences
	public static void writeDeferredPrefs() {
		for (DeferredPref p : mDeferredPrefs.values()) {
			p.commit();
		}
		mDeferredPrefs.clear();
	}

	// Set a deferred string preference
	protected void setStringPref(String key, String value) {
		mDeferredPrefs.put(key, new DeferredStringPref(mPrefs, key, value));
	}

	// Get a string preference, with error handling
	protected String getStringPref(String key) {
		DeferredStringPref p = (DeferredStringPref) mDeferredPrefs.get(key);
		return (p != null) ? p.value : mPrefs.getString(key, "");
	}

	// Set a deferred boolean preference
	protected void setBooleanPref(String key, boolean value) {
		mDeferredPrefs.put(key, new DeferredBooleanPref(mPrefs, key, value));
	}

	// Get a boolean preference, with error handling
	protected boolean getBooleanPref(String key) {
		DeferredBooleanPref p = (DeferredBooleanPref) mDeferredPrefs.get(key);
		return (p != null) ? p.value : mPrefs.getBoolean(key, false);
	}

	// Start rendering the dialog
	public void onStart(Context context) {
		mDialogUp = true;
		Log.d(TAG, "Rendering user dialog");
	}

	// Stop rendering the dialog
	public void onStop(Context context) {
		mDialogUp = false;
		Log.d(TAG, "Tearing down user dialog");
	}

	// Determine if the dialog can be skipped based on preferences
	public Object earlyReturn() {
		Log.d(TAG, (mResult == null ? "Not skipping" : "Skipping") + " user dialog");
		return mResult;
	}
}