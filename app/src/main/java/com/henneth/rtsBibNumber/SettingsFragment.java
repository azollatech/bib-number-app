package com.henneth.rtsBibNumber;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.henneth.rtsBibNumber.R;

/**
 * Created by hennethcheng on 29/8/2017.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
