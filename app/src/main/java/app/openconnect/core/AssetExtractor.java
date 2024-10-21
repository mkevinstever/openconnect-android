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

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AssetExtractor {

	public static final String TAG = "OpenConnect";

	public static final int FL_FORCE = 0x01;
	public static final int FL_NOEXEC = 0x02;

	private static final int BUFLEN = 65536;

	private static long crc32(File f) throws IOException {
		try (FileInputStream in = new FileInputStream(f)) {
			CRC32 crcMaker = new CRC32();
			byte[] buffer = new byte[BUFLEN];

			int len;
			while ((len = in.read(buffer)) != -1) {
				crcMaker.update(buffer, 0, len);
			}
			return crcMaker.getValue();
		}
	}

	private static void writeStream(InputStream in, File file) throws IOException {
		try (FileOutputStream out = new FileOutputStream(file)) {
			byte[] buffer = new byte[BUFLEN];

			int len;
			while ((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
		}
	}

	private static String getArch() {
		String prop = System.getProperty("os.arch");
		if (prop.contains("aarch64")) {
			return "arm64-v8a";
		} else if (prop.contains("x86_64")) {
			return "x86_64";
		} else if (prop.contains("x86") || prop.contains("i686") || prop.contains("i386")) {
			return "x86";
		} else {
			return "armeabi";
		}
	}

	public static boolean extractAll(Context ctx, int flags, String path) {
		String patterns[] = { "assets/raw/noarch", "assets/raw/" + getArch() };

		if (path == null) {
			path = ctx.getFilesDir().getAbsolutePath();
		} else if (!path.endsWith(File.separator)) {
			path += File.separator;
		}

		File dir = new File(path);
		if (!dir.exists() && !dir.mkdirs()) {
			Log.e(TAG, "AssetExtractor: failed to create directory " + path);
			return false;
		}

		try (ZipFile zf = new ZipFile(ctx.getPackageCodePath())) {
			for (Enumeration<?> e = zf.entries(); e.hasMoreElements(); ) {
				ZipEntry ze = (ZipEntry) e.nextElement();
				if (ze.isDirectory()) {
					continue;
				}

				String fname = ze.getName();
				for (String prefix : patterns) {
					if (!fname.startsWith(prefix)) {
						continue;
					}
					fname = path + fname.substring(prefix.length());
					File file = new File(fname);
					if ((flags & FL_FORCE) == 0 && file.exists() && crc32(file) == ze.getCrc()) {
						Log.d(TAG, "AssetExtractor: skipping " + fname);
						continue;
					}
					Log.i(TAG, "AssetExtractor: writing " + fname);
					try (InputStream inputStream = zf.getInputStream(ze)) {
						writeStream(inputStream, file);
					}
					if ((flags & FL_NOEXEC) == 0) {
						file.setExecutable(true);
					}
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "AssetExtractor: caught exception", e);
			return false;
		}
		return true;
	}

	public static boolean extractAll(Context ctx) {
		return extractAll(ctx, 0, null);
	}

	private static String readAndClose(Reader reader) throws IOException {
		StringWriter sw = new StringWriter();
		char[] buffer = new char[BUFLEN];

		int len;
		while ((len = reader.read(buffer)) != -1) {
			sw.write(buffer, 0, len);
		}
		return sw.toString();
	}

	public static String readString(Context ctx, String name) {
		try (InputStream in = ctx.getAssets().open(name);
			 Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
			return readAndClose(reader);
		} catch (IOException e) {
			Log.e(TAG, "AssetExtractor: readString exception", e);
		}
		return null;
	}

	public static String readStringFromFile(String filename) {
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			return readAndClose(reader);
		} catch (IOException e) {
			Log.e(TAG, "AssetExtractor: readString exception", e);
		}
		return null;
	}
}