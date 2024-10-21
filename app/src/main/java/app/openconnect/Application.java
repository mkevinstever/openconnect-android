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

package app.openconnect;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.HttpSender;
import org.acra.sender.ReportSenderException;

import app.openconnect.core.FragCache;
import app.openconnect.core.ProfileManager;
import app.openconnect.core.VPNLog;

@SuppressLint("NonConstantResourceId")
@ReportsCrashes(
		mode = ReportingInteractionMode.DIALOG,
		resDialogText = R.string.crash_dialog_text,
		resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
		formUri = "https://kpc.cloudant.com/acra-openconnect/_design/acra-storage/_update/report",
		formUriBasicAuthLogin="ineintlynnoveristimedesc",
		formUriBasicAuthPassword="mUmkrQIOKd3HalLf5AQuyxpA",
		formKey = ""
)
public class Application extends android.app.Application {

	private boolean isPackageInstalled(String name) {
		PackageManager pm = getPackageManager();
		try {
			pm.getPackageInfo(name, 0);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	private void setupACRA() {
		String[] hax0rPackages = {
				"com.koushikdutta.superuser",
				"com.noshufou.android.su",
				"com.noshufou.android.su.elite",
				"com.miui.uac",
				"eu.chainfire.supersu",
				"eu.chainfire.supersu.pro",
				"de.robv.android.xposed.installer",
				"biz.bokhorst.xprivacy",
				"biz.bokhorst.xprivacy.pro"
		};

		ACRA.init(this);
		ErrorReporter er = ACRA.getErrorReporter();

		// Set up custom HTTP sender with error reporting
		er.setReportSender(new HttpSender(HttpSender.Method.PUT,
				HttpSender.Type.JSON,
				null) {
			@Override
			public void send(CrashReportData report) throws ReportSenderException {
				report.put(ReportField.APPLICATION_LOG, VPNLog.dumpLast());
				super.send(report);
			}
		});

		// Check for installed hacker packages and report their statuses
		for (String packageName : hax0rPackages) {
			// Use a safe string replacement to avoid issues
			String safeKey = "pkg-" + packageName.replace(".", "-");
			er.putCustomData(safeKey, isPackageInstalled(packageName) ? "true" : "false");
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setupACRA();
		System.loadLibrary("openconnect");
		System.loadLibrary("stoken");
		ProfileManager.init(getApplicationContext());
		FragCache.init();
	}
}