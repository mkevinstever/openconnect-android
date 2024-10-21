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

import app.openconnect.core.ProfileManager;
import app.openconnect.fragments.ConnectionEditorFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ConnectionEditorActivity extends Activity {

	private String mName = "";
	private String mUUID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(android.R.layout.activity_list_item); // set a default layout if necessary

		mUUID = getIntent().getStringExtra(getPackageName() + ".profileUUID");

		// Check if UUID is valid
		if (mUUID == null) {
			// Handle error case, maybe show a Toast or Log error
			finish();
			return;
		}

		ConnectionEditorFragment frag = new ConnectionEditorFragment();
		Bundle args = new Bundle();
		args.putString("profileUUID", mUUID);
		frag.setArguments(args);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, frag)
				.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.vpnpreferences_menu, menu);
		return true; // Indicate that you have created the menu
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.remove_vpn) {
			askProfileRemoval();
			return true; // Indicate that the menu item was handled
		}
		return super.onOptionsItemSelected(item);
	}

	public void setProfileName(String name) {
		mName = name;
		setTitle(getString(R.string.edit_profile_title, mName));
	}

	private void askProfileRemoval() {
		new AlertDialog.Builder(this)
				.setTitle("Confirm deletion")
				.setMessage(getString(R.string.remove_vpn_query, mName))
				.setPositiveButton(android.R.string.yes, (dialog, which) -> {
					ProfileManager.delete(mUUID);
					finish();
				})
				.setNegativeButton(android.R.string.no, null)
				.create()
				.show();
	}
}