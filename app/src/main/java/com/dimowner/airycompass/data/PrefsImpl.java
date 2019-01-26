/*
 * Copyright 2019 Dmitriy Ponomarenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dimowner.airycompass.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * App preferences implementation
 */
public class PrefsImpl implements Prefs {

	private static final String PREF_NAME = "com.dimowner.audiorecorder.data.PrefsImpl";

	private static final String PREF_KEY_IS_FIRST_RUN = "is_first_run";
	private static final String PREF_KEY_THEME_COLORMAP_POSITION = "theme_color";
	private static final String PREF_KEY_KEEP_SCREEN_ON = "keep_screen_on";
	private static final String PREF_KEY_ENERGY_SAVING_MODE = "is_energy_saving_mode";

	private SharedPreferences sharedPreferences;

	private volatile static PrefsImpl instance;

	private PrefsImpl(Context context) {
		sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}

	public static PrefsImpl getInstance(Context context) {
		if (instance == null) {
			synchronized (PrefsImpl.class) {
				if (instance == null) {
					instance = new PrefsImpl(context);
				}
			}
		}
		return instance;
	}

	@Override
	public boolean isFirstRun() {
		return !sharedPreferences.contains(PREF_KEY_IS_FIRST_RUN) || sharedPreferences.getBoolean(PREF_KEY_IS_FIRST_RUN, false);
	}

	@Override
	public void firstRunExecuted() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(PREF_KEY_IS_FIRST_RUN, false);
		editor.apply();
	}

	@Override
	public void setAppThemeColor(int colorMapPosition) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_KEY_THEME_COLORMAP_POSITION, colorMapPosition);
		editor.apply();
	}

	@Override
	public int getThemeColor() {
		return sharedPreferences.getInt(PREF_KEY_THEME_COLORMAP_POSITION, 0);
	}

	@Override
	public void setKeepScreenOn(boolean on) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(PREF_KEY_KEEP_SCREEN_ON, on);
		editor.apply();
	}

	@Override
	public boolean isKeepScreenOn() {
		return sharedPreferences.getBoolean(PREF_KEY_KEEP_SCREEN_ON, false);
	}

	@Override
	public void setEnergySavingMode(boolean on) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(PREF_KEY_ENERGY_SAVING_MODE, on);
		editor.apply();
	}

	@Override
	public boolean isEnergySavingMode() {
		return sharedPreferences.getBoolean(PREF_KEY_ENERGY_SAVING_MODE, false);
	}
}
