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

package app.openconnect;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple content provider that serves arbitrary asset files from our .apk.
 */
public class FileProvider extends ContentProvider {

	private static final String TAG = "OpenVPNFileProvider";
	private static final int BUFFER_SIZE = 8192;

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		try {
			File dumpfile = getFileFromURI(uri);
			MatrixCursor cursor = new MatrixCursor(projection);
			Object[] row = new Object[projection.length];

			for (int i = 0; i < projection.length; i++) {
				String column = projection[i];
				if (OpenableColumns.SIZE.equals(column)) {
					row[i] = dumpfile.length();
				} else if (OpenableColumns.DISPLAY_NAME.equals(column)) {
					row[i] = dumpfile.getName();
				} else {
					row[i] = null; // Ensure all columns are populated
				}
			}
			cursor.addRow(row);
			return cursor;
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found", e);
			return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Insertion not supported.
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Deletion not supported.
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// Update not supported.
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// Assume all files are .apks.
		return "application/octet-stream";
	}

	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
		File dumpfile = getFileFromURI(uri);

		try {
			InputStream is = new FileInputStream(dumpfile);
			ParcelFileDescriptor pfd = ParcelFileDescriptor.open(dumpfile, ParcelFileDescriptor.MODE_READ_ONLY);
			return new AssetFileDescriptor(pfd, 0, dumpfile.length());
		} catch (IOException e) {
			throw new FileNotFoundException("Unable to open file: " + uri);
		}
	}

	private File getFileFromURI(Uri uri) throws FileNotFoundException {
		String path = uri.getPath();
		if (path.startsWith("/")) {
			path = path.substring(1); // Remove leading slash
		}

		// Validate file path format
		if (!path.matches("^[0-9a-z-.]*(dmp|dmp.log)$")) {
			throw new FileNotFoundException("URL not in expected format: " + uri);
		}

		File cachedir = getContext().getCacheDir();
		return new File(cachedir, path);
	}

	public void writeDataToOutputStream(ParcelFileDescriptor output, InputStream inputStream) {
		try (FileOutputStream fout = new FileOutputStream(output.getFileDescriptor())) {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) >= 0) {
				fout.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			Log.i(TAG, "Failed transferring data", e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				Log.e(TAG, "Failed to close input stream", e);
			}
		}
	}
}