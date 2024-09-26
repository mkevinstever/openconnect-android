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

import app.openconnect.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

public class CertWarningDialog extends UserDialog
		implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

	public static final int RESULT_NO = 0;
	public static final int RESULT_ONCE = 1;
	public static final int RESULT_ALWAYS = 2;

	private String mHostname;
	private String mCertSHA1;
	private String mReason;

	private int mAccept = RESULT_NO;
	private AlertDialog mAlertDialog;

	public CertWarningDialog(SharedPreferences prefs, String hostname, String certSHA1, String reason) {
		super(prefs);
		this.mHostname = hostname;
		this.mCertSHA1 = certSHA1;
		this.mReason = reason;
	}

	@Override
	public Object earlyReturn() {
		return null;
	}

	@Override
	public void onStart(Context context) {
		super.onStart(context);

		if (context != null) {
			mAlertDialog = new AlertDialog.Builder(context)
					.setTitle(R.string.cert_warning_title)
					.setMessage(context.getString(R.string.cert_warning_message, mHostname, mReason, mCertSHA1))
					.setPositiveButton(R.string.cert_warning_always_connect, this)
					.setNeutralButton(R.string.cert_warning_just_once, this)
					.setNegativeButton(R.string.no, this)
					.create();
			mAlertDialog.setOnDismissListener(this);
			mAlertDialog.show();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		finish(mAccept);
		mAlertDialog = null;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				mAccept = RESULT_ALWAYS;
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				mAccept = RESULT_ONCE;
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				mAccept = RESULT_NO;
				break;
			default:
				break;
		}
	}

	@Override
	public void onStop(Context context) {
		super.onStop(context);
		if (mAlertDialog != null) {
			mAlertDialog.dismiss();
		}

		finish(mAccept != RESULT_NO ? mAccept : null);
	}
}