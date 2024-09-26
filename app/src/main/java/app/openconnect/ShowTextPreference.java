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

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * A custom preference that displays text without invoking a dialog.
 */
public class ShowTextPreference extends DialogPreference {

    // Constructors
    public ShowTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ShowTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShowTextPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onClick() {
        // Override to prevent dialog from popping up when clicked
    }

    public void setText(String text) {
        // Ensure text is not null
        if (text == null) {
            text = ""; // Use empty string if null
        }

        // Persist the string value
        persistString(text);

        // Notify dependents if this preference's value has changed
        notifyDependencyChange(shouldDisableDependents());
    }
}