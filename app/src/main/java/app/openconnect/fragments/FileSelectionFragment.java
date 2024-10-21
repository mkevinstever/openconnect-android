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

package app.openconnect.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import app.openconnect.FileSelect;
import app.openconnect.R;

public class FileSelectionFragment extends ListFragment {

	private static final String ITEM_KEY = "key";
	private static final String ITEM_IMAGE = "image";
	private static final String ROOT = "/";


	private List<String> path = null;
	private TextView myPath;
	private ArrayList<HashMap<String, Object>> mList;

	private Button selectButton;


	private String parentPath;
	private String currentPath = ROOT;


	private String[] formatFilter = null;

	private File selectedFile;
	private HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();
	private String mStartPath;
	private CheckBox mInlineImport;
	private Button mClearButton;
	private boolean mHideImport = false;
	private boolean mForceImport = false;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.file_dialog_main, container,false);

		myPath = (TextView) v.findViewById(R.id.path);

		mInlineImport = (CheckBox) v.findViewById(R.id.doinline);

		if (mHideImport) {
			mInlineImport.setVisibility(View.GONE);
			mInlineImport.setChecked(false);
		} else if (mForceImport) {
			mInlineImport.setEnabled(false);
			mInlineImport.setChecked(true);
		}

		selectButton = (Button) v.findViewById(R.id.fdButtonSelect);
		selectButton.setEnabled(false);
		selectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (selectedFile != null) {
					if(mInlineImport.isChecked())

						((FileSelect) getActivity()).importFile(selectedFile.getPath());
					else 
						((FileSelect) getActivity()).setFile(selectedFile.getPath());
				}
			}
		});

		mClearButton = (Button) v.findViewById(R.id.fdClear);
		mClearButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((FileSelect) getActivity()).clearData();
			}
		});
		if(!((FileSelect) getActivity()).showClear()) {
			mClearButton.setVisibility(View.GONE);
			mClearButton.setEnabled(false);
		}

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mStartPath = ((FileSelect) getActivity()).getSelectPath();
		getDir(mStartPath);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
	}

	private void getDir(String dirPath) {

		boolean useAutoSelection = dirPath.length() < currentPath.length();

		Integer position = lastPositions.get(parentPath);

		getDirImpl(dirPath);

		if (position != null && useAutoSelection) {
			getListView().setSelection(position);
		}

	}

	/**
	 * Monta a estrutura de arquivos e diretorios filhos do diretorio fornecido.
	 * 
	 * @param dirPath
	 *            Diretorio pai.
	 */
	private void getDirImpl(final String dirPath) {

		currentPath = dirPath;

		final List<String> item = new ArrayList<String>();
		path = new ArrayList<String>();
		mList = new ArrayList<HashMap<String, Object>>();

		File f = new File(currentPath);
		File[] files = f.listFiles();
		if (files == null) {
			currentPath = ROOT;
			f = new File(currentPath);
			files = f.listFiles();
		}

		myPath.setText(getText(R.string.location) + ": " + currentPath);

		if (!currentPath.equals(ROOT)) {

			item.add(ROOT);
			addItem(ROOT, R.drawable.folder);
			path.add(ROOT);

			item.add("../");
			addItem("../", R.drawable.folder);
			path.add(f.getParent());
			parentPath = f.getParent();

		}

		TreeMap<String, String> dirsMap = new TreeMap<String, String>();
		TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
		TreeMap<String, String> filesMap = new TreeMap<String, String>();
		TreeMap<String, String> filesPathMap = new TreeMap<String, String>();
		for (File file : files) {
			if (file.isDirectory()) {
				String dirName = file.getName();
				dirsMap.put(dirName, dirName);
				dirsPathMap.put(dirName, file.getPath());
			} else {
				final String fileName = file.getName();
				final String fileNameLwr = fileName.toLowerCase(Locale.getDefault());
				// se ha um filtro de formatos, utiliza-o
				if (formatFilter != null) {
					boolean contains = false;
					for (int i = 0; i < formatFilter.length; i++) {
						final String formatLwr = formatFilter[i].toLowerCase(Locale.getDefault());
						if (fileNameLwr.endsWith(formatLwr)) {
							contains = true;
							break;
						}
					}
					if (contains) {
						filesMap.put(fileName, fileName);
						filesPathMap.put(fileName, file.getPath());
					}
					// senao, adiciona todos os arquivos
				} else {
					filesMap.put(fileName, fileName);
					filesPathMap.put(fileName, file.getPath());
				}
			}
		}
		item.addAll(dirsMap.tailMap("").values());
		item.addAll(filesMap.tailMap("").values());
		path.addAll(dirsPathMap.tailMap("").values());
		path.addAll(filesPathMap.tailMap("").values());

		SimpleAdapter fileList = new SimpleAdapter(getActivity(), mList, R.layout.file_dialog_row, new String[] {
			ITEM_KEY, ITEM_IMAGE }, new int[] { R.id.fdrowtext, R.id.fdrowimage });

		for (String dir : dirsMap.tailMap("").values()) {
			addItem(dir, R.drawable.folder);
		}

		for (String file : filesMap.tailMap("").values()) {
			addItem(file, R.drawable.file);
		}

		fileList.notifyDataSetChanged();

		setListAdapter(fileList);

	}

	private void addItem(String fileName, int imageId) {
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(ITEM_KEY, fileName);
		item.put(ITEM_IMAGE, imageId);
		mList.add(item);
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		File file = new File(path.get(position));

		if (file.isDirectory()) {
			selectButton.setEnabled(false);

			if (file.canRead()) {
				lastPositions.put(currentPath, position);
				getDir(path.get(position));
			} else {
				new AlertDialog.Builder(getActivity())
				.setTitle("[" + file.getName() + "] " + getText(R.string.cant_read_folder))
				.setPositiveButton("OK", null).show();
			}
		} else {
			selectedFile = file;
			v.setSelected(true);
			selectButton.setEnabled(true);
		}
	}

	public void setNoInLine() {
		mHideImport = true;
	}

	public void setForceInLine() {
		mForceImport = true;
	}

}
