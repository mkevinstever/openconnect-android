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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.infradead.libopenconnect.LibOpenConnect;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import app.openconnect.core.UserDialog;

public class AuthFormHandler extends UserDialog
		implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

	public static final String TAG = "OpenConnect";

	private final LibOpenConnect.AuthForm mForm;
	private Context mContext;
	private boolean isOK;
	private AlertDialog mAlert;

	private CheckBox savePassword = null;
	private boolean noSave = false;
	private final String formPfx;

	private int batchMode = BATCH_MODE_DISABLED;
	private final boolean mAuthgroupSet;
	private boolean mAllFilled = true;

	private TextView mFirstEmptyText;
	private TextView mFirstText;

	private static final int BATCH_MODE_DISABLED = 0;
	private static final int BATCH_MODE_EMPTY_ONLY = 1;
	private static final int BATCH_MODE_ENABLED = 2;
	private static final int BATCH_MODE_ABORTED = 3;

	public AuthFormHandler(SharedPreferences prefs, LibOpenConnect.AuthForm form, boolean authgroupSet,
			String lastFormDigest) {
		super(prefs);

		mForm = form;
		mAuthgroupSet = authgroupSet;
		formPfx = getFormPrefix(mForm);
		noSave = getBooleanPref("disable_username_caching");

		String s = getStringPref("batch_mode");
		if (s.equals("empty_only")) {
			batchMode = BATCH_MODE_EMPTY_ONLY;
		} else if (s.equals("enabled")) {
			batchMode = BATCH_MODE_ENABLED;
		}

		// If the server is sending us the same form twice in a row, that probably
		// means there's a problem with the data we are sending back.  Either prompt
		// the user, or abort.
		if (formPfx.equals(lastFormDigest)) {
			if (batchMode == BATCH_MODE_EMPTY_ONLY) {
				batchMode = BATCH_MODE_DISABLED;
			} else if (batchMode == BATCH_MODE_ENABLED) {
				batchMode = BATCH_MODE_ABORTED;
			}
		}
	}

	public String getFormDigest() {
		return formPfx;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// catches OK, Cancel, and Back button presses
		if (isOK) {
			saveAndStore();
		}
		finish(isOK ? LibOpenConnect.OC_FORM_RESULT_OK : LibOpenConnect.OC_FORM_RESULT_CANCELLED);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			isOK = true;
		}
	}

	private String digest(String s) {
		String out = "";
		if (s == null) {
			s = "";
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			StringBuilder sb = new StringBuilder();
			byte[] d = digest.digest(s.getBytes(StandardCharsets.UTF_8));
			for (byte dd : d) {
				sb.append(String.format("%02x", dd));
			}
			out = sb.toString();
		} catch (Exception e) {
			Log.e(TAG, "MessageDigest failed", e);
		}
		return out;
	}

	private String getOptDigest(LibOpenConnect.FormOpt opt) {
		StringBuilder in = new StringBuilder();

		switch (opt.type) {
		case LibOpenConnect.OC_FORM_OPT_SELECT:
			for (LibOpenConnect.FormChoice ch : opt.choices) {
				in.append(digest(ch.name));
				in.append(digest(ch.label));
			}
			/* falls through */
		case LibOpenConnect.OC_FORM_OPT_TEXT:
		case LibOpenConnect.OC_FORM_OPT_PASSWORD:
			in.append(":").append(opt.type).append(":");
			in.append(digest(opt.name));
			in.append(digest(opt.label));
		}
		return digest(in.toString());
	}

	private String getFormPrefix(LibOpenConnect.AuthForm form) {
		if (form == null || form.opts == null) {
			Log.e(TAG, "getFormPrefix: AuthForm or opts is null");
			return "";
		}

		StringBuilder in = new StringBuilder();

		for (LibOpenConnect.FormOpt opt : form.opts) {
			if (opt == null) {
				Log.e(TAG, "getFormPrefix: FormOpt is null");
				continue;
			}
			in.append(getOptDigest(opt));
		}
		return "FORMDATA-" + digest(in.toString()) + "-";
	}

	private final LinearLayout.LayoutParams fillWidth =
			new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

	private LinearLayout newHorizLayout(String label) {
		LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.setLayoutParams(fillWidth);

		TextView tv = new TextView(mContext);
		tv.setText(label);
		ll.addView(tv);

		return ll;
	}

	private LinearLayout newTextBlank(LibOpenConnect.FormOpt opt, String defval) {
		if (opt == null) {
			Log.e(TAG, "newTextBlank: FormOpt is null");
			return null;
		}

		LinearLayout ll = newHorizLayout(opt.label);

		TextView tv = new EditText(mContext);
		tv.setLayoutParams(fillWidth);

		if (defval == null) {
			defval = opt.value != null ? opt.value : "";
		}
		tv.setText(defval);

		if (mFirstEmptyText == null && defval.equals("")) {
			mFirstEmptyText = tv;
		}
		if (mFirstText == null) {
			mFirstText = tv;
		}

		int baseType = (opt.flags & LibOpenConnect.OC_FORM_OPT_NUMERIC) != 0 ?
				InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT;
		if (opt.type == LibOpenConnect.OC_FORM_OPT_PASSWORD) {
			tv.setInputType(baseType | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			tv.setTransformationMethod(PasswordTransformationMethod.getInstance());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				tv.setAutofillHints(View.AUTOFILL_HINT_PASSWORD);
			}
		} else {
			tv.setInputType(baseType | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				tv.setAutofillHints(View.AUTOFILL_HINT_USERNAME);
			}
		}
		tv.setOnEditorActionListener((textView, actionId, keyEvent) -> {
			if (actionId == EditorInfo.IME_ACTION_DONE ||
					(keyEvent != null &&
							keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
							keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {
				isOK = true;
				mAlert.dismiss();
				return true;
			} else {
				return false;
			}
		});

		opt.userData = tv;
		ll.addView(tv);
		return ll;
	}

	private void spinnerSelect(LibOpenConnect.FormOpt opt, int index) {
		LibOpenConnect.FormChoice fc = opt.choices.get(index);
		String s = fc.name != null ? fc.name : "";

		if (opt.userData == null) {
			// first run
			opt.userData = s;
		} else if (!s.equals(opt.userData)) {
			opt.value = s;
			mAlert.dismiss();
			finish(LibOpenConnect.OC_FORM_RESULT_NEWGROUP);
		}
	}

	private LinearLayout newDropdown(final LibOpenConnect.FormOpt opt, int selection) {
		List<String> choiceList = new ArrayList<String>();

	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
	    		android.R.layout.simple_spinner_item, choiceList);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

	    for (LibOpenConnect.FormChoice fc : opt.choices) {
	    	choiceList.add(fc.label);
	    }

		Spinner sp = new Spinner(mContext);
		sp.setAdapter(adapter);
		sp.setLayoutParams(fillWidth);

		sp.setSelection(selection);
		spinnerSelect(opt, selection);

		sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				spinnerSelect(opt, (int)id);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		LinearLayout ll = newHorizLayout(opt.label);
		ll.addView(sp);

		return ll;
	}

	private CheckBox newSavePasswordView(boolean isChecked) {
		CheckBox cb = new CheckBox(mContext);
		cb.setText(R.string.save_password);
		cb.setChecked(isChecked);
		return cb;
	}

	private void saveAndStore() {
		if (mForm == null || mForm.opts == null) {
			Log.w("saveAndStore", "mForm or mForm.opts is null, aborting save operation.");
			return;
		}

		for (LibOpenConnect.FormOpt opt : mForm.opts) {
			if ((opt.flags & LibOpenConnect.OC_FORM_OPT_IGNORE) != 0) {
				continue;
			}

			switch (opt.type) {
				case LibOpenConnect.OC_FORM_OPT_TEXT:
				case LibOpenConnect.OC_FORM_OPT_PASSWORD: {
					saveTextOption(opt);
					break;
				}
				case LibOpenConnect.OC_FORM_OPT_SELECT: {
					saveSelectOption(opt);
					break;
				}
				default:
					Log.w("saveAndStore", "Unknown option type: " + opt.type);
					break;
			}
		}
	}

	private void saveTextOption(LibOpenConnect.FormOpt opt) {
		if (opt.userData instanceof TextView) {
			TextView tv = (TextView) opt.userData;
			String s = tv.getText().toString();
			if (opt.type == LibOpenConnect.OC_FORM_OPT_PASSWORD) {
				if (savePassword != null) {
					boolean checked = savePassword.isChecked();
					setStringPref(formPfx + getOptDigest(opt), checked ? s : "");
					setStringPref(formPfx + "savePass", checked ? "true" : "false");
				}
			} else if (!noSave) {
				setStringPref(formPfx + getOptDigest(opt), s);
			}
			opt.value = s;
		} else {
			Log.w("saveTextOption", "userData is not a TextView for option: " + opt.name);
		}
	}

	private void saveSelectOption(LibOpenConnect.FormOpt opt) {
		if (opt.userData instanceof String) {
			String s = (String) opt.userData;
			if (!noSave) {
				setStringPref(formPfx + getOptDigest(opt), s);
				if ("group_list".equals(opt.name)) {
					setStringPref("authgroup", s);
				}
			}
			opt.value = s;
		} else {
			Log.w("saveSelectOption", "userData is not a String for option: " + opt.name);
		}
	}


	// If the user had saved a preferred authgroup, submit a NEWGROUP request before rendering the form
	public boolean setAuthgroup() {
		LibOpenConnect.FormOpt opt = mForm.authgroupOpt;
		if (opt == null) {
			Log.e(TAG, "setAuthgroup: authgroupOpt is null");
			return false;
		}

		String authgroup = getStringPref("authgroup");
		if (authgroup == null || authgroup.equals("")) {
			Log.e(TAG, "setAuthgroup: authgroup is null or empty");
			return false;
		}

		LibOpenConnect.FormChoice selected = opt.choices != null && opt.choices.size() > 0 ? opt.choices.get(mForm.authgroupSelection) : null;
		if (selected != null && (mAuthgroupSet || authgroup.equals(selected.name))) {
			// already good to go
			opt.value = authgroup;
			return false;
		}
		if (opt.choices != null) {
			for (LibOpenConnect.FormChoice ch : opt.choices) {
				if (authgroup.equals(ch.name)) {
					opt.value = authgroup;
					return true;
				}
			}
		}
		Log.w(TAG, "saved authgroup '" + authgroup + "' not present in " + opt.name + " dropdown");
		return false;
	}

	public Object earlyReturn() {
		if (setAuthgroup()) {
			return LibOpenConnect.OC_FORM_RESULT_NEWGROUP;
		}
		if (batchMode != BATCH_MODE_EMPTY_ONLY && batchMode != BATCH_MODE_ENABLED) {
			return null;
		}

		// do a quick pass through all prompts to see if we can fill in the
		// answers without bugging the user
		for (LibOpenConnect.FormOpt opt : mForm.opts) {
			if (opt == null || (opt.flags & LibOpenConnect.OC_FORM_OPT_IGNORE) != 0) {
				continue;
			}
			switch (opt.type) {
				case LibOpenConnect.OC_FORM_OPT_PASSWORD:
				case LibOpenConnect.OC_FORM_OPT_TEXT:
					String defval = noSave ? "" : getStringPref(formPfx + getOptDigest(opt));
					if (defval.equals("")) {
						return null;
					}
					opt.value = defval;
					break;
				case LibOpenConnect.OC_FORM_OPT_SELECT:
					if (opt.value == null) {
						return null;
					}
					break;
			}
		}
		return LibOpenConnect.OC_FORM_RESULT_OK;
	}

	public void onStart(Context context) {
		final AuthFormHandler h = this;

		super.onStart(context);
		mContext = context;
		isOK = false;

		float scale = mContext.getResources().getDisplayMetrics().density;
		LinearLayout v = new LinearLayout(mContext);
		v.setOrientation(LinearLayout.VERTICAL);
		v.setPadding((int)(14 * scale), (int)(2 * scale), (int)(10 * scale), (int)(2 * scale));

		boolean hasPassword = false, hasUserOptions = false;
		String defval;

		mFirstText = mFirstEmptyText = null;


		if (mForm != null && mForm.opts != null) {
			for (LibOpenConnect.FormOpt opt : mForm.opts) {
				if ((opt.flags & LibOpenConnect.OC_FORM_OPT_IGNORE) != 0) {
					continue;
				}
				switch (opt.type) {
					case LibOpenConnect.OC_FORM_OPT_PASSWORD:
						hasPassword = true;

					case LibOpenConnect.OC_FORM_OPT_TEXT:
						defval = noSave ? "" : getStringPref(formPfx + getOptDigest(opt));

						if (defval == null || defval.equals("")) {
							defval = (opt.value != null && !opt.value.equals("")) ? opt.value : "";
							mAllFilled = false;
						}
						v.addView(newTextBlank(opt, defval));
						hasUserOptions = true;
						break;
					case LibOpenConnect.OC_FORM_OPT_SELECT:
						if (opt.choices.size() == 0) {
							break;
						}

						int selection = 0;
						if (opt == mForm.authgroupOpt) {
							selection = mForm.authgroupSelection;
						} else {
							defval = noSave ? "" : getStringPref(formPfx + getOptDigest(opt));
							for (int i = 0; i < opt.choices.size(); i++) {
								if (opt.choices.get(i).name.equals(defval)) {
									selection = i;
									break;
								}
							}
						}
						v.addView(newDropdown(opt, selection));
						hasUserOptions = true;
						break;
					default:
						Log.w("AuthFormHandler", "Unknown option type: " + opt.type);
						break;
				}
			}
		}

		if (hasPassword && !noSave) {
			boolean savePass = !getStringPref(formPfx + "savePass").equals("false");
			savePassword = newSavePasswordView(savePass);
			v.addView(savePassword);
		}

		if (batchMode == BATCH_MODE_ABORTED) {
			finish(LibOpenConnect.OC_FORM_RESULT_CANCELLED);
			return;
		}

		if ((batchMode == BATCH_MODE_EMPTY_ONLY && mAllFilled) ||
				batchMode == BATCH_MODE_ENABLED || !hasUserOptions) {
			saveAndStore();
			finish(LibOpenConnect.OC_FORM_RESULT_OK);
			return;
		}

		mAlert = new AlertDialog.Builder(mContext)
				.setView(v)
				.setTitle(mContext.getString(R.string.login_title, getStringPref("profile_name")))
				.setPositiveButton(R.string.ok, h)
				.setNegativeButton(R.string.cancel, h)
				.create();
		mAlert.setOnDismissListener(h);


		if (mForm.message != null) {
			String s = mForm.message.trim();

			if (s.length() > 128) {
				s = s.substring(0, 128);
			}
			if (s.length() > 0) {
				mAlert.setMessage(s);
			}
		}


		mAlert.show();

		TextView focus = mFirstEmptyText != null ? mFirstEmptyText : mFirstText;
		if (focus != null) {
			focus.append("");
			focus.requestFocus();
		}
	}

	public void onStop(Context context) {
		super.onStop(context);
		if (mAlert != null) {
			saveAndStore();
			if (mAlert.isShowing()) {
				mAlert.dismiss();
			}
			mAlert = null;
		}
	}
}