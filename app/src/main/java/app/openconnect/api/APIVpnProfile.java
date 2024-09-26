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

import android.os.Parcel;
import android.os.Parcelable;

public class APIVpnProfile implements Parcelable {

	public final String mUUID;
	public final String mName;
	public final boolean mUserEditable;

	public APIVpnProfile(Parcel in) {
		mUUID = in.readString();
		mName = in.readString();
        mUserEditable = in.readInt() != 0;
	}

	public APIVpnProfile(String uuidString, String name, boolean userEditable) {
		mUUID=uuidString;
		mName = name;
		mUserEditable=userEditable;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mUUID);
		dest.writeString(mName);
		dest.writeInt(mUserEditable ? 1 : 0); // 修改这里
	}

	public static final Parcelable.Creator<APIVpnProfile> CREATOR
	= new Parcelable.Creator<APIVpnProfile>() {
		public APIVpnProfile createFromParcel(Parcel in) {
			return new APIVpnProfile(in);
		}

		public APIVpnProfile[] newArray(int size) {
			return new APIVpnProfile[size];
		}
	};
}