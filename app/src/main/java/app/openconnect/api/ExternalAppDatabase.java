/*
 * Adapted from OpenVPN for Android
 * Copyright (c) 2012-2013, Arne Schwabe
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

package app.openconnect.api;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class ExternalAppDatabase {

	private WeakReference<Context> mContextRef;

	private final String PREFERENCES_KEY = "PREFERENCES_KEY";

	public ExternalAppDatabase(Context c) {
		mContextRef = new WeakReference<>(c);
	}

	public boolean isAllowed(String packagename) {
		Set<String> allowedapps = getExtAppList();
		return allowedapps.contains(packagename);
	}

	public Set<String> getExtAppList() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContextRef.get());
		return new HashSet<>(prefs.getStringSet(PREFERENCES_KEY, new HashSet<>()));
	}

	public void addApp(String packagename) {
		Set<String> allowedapps = getExtAppList();
		if (!allowedapps.contains(packagename)) {
			allowedapps.add(packagename);
			saveExtAppList(allowedapps);
		}
	}

	private void saveExtAppList(Set<String> allowedapps) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContextRef.get());
		Editor prefedit = prefs.edit();
		prefedit.putStringSet(PREFERENCES_KEY, allowedapps);
		prefedit.apply();
	}

	public void clearAllApiApps() {
		saveExtAppList(new HashSet<>());
	}

	public void removeApp(String packagename) {
		Set<String> allowedapps = getExtAppList();
		if (allowedapps.remove(packagename)) {
			saveExtAppList(allowedapps);
		}
	}
}