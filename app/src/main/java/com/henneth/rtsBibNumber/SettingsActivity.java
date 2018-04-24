package com.henneth.rtsBibNumber;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.henneth.rtsBibNumber.R;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        String pref_server_string = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_server", "(empty)");
        Preference pref_server = (Preference) findPreference("pref_server");
        pref_server.setSummary("Current server: " + pref_server_string);

        String pref_team_mode_string = PreferenceManager.getDefaultSharedPreferences(this).getString("team_mode", "Solo / Team / Mix");
        Preference pref_team_mode = (Preference) findPreference("team_mode");
        pref_team_mode.setSummary("Current mode: " + pref_team_mode_string);

        String pref_port_string = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_port", "(empty)");
        Preference pref_port = (Preference) findPreference("pref_port");
        pref_port.setSummary("Current port: " + pref_port_string);

        String pref_ip_string = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_ip", "(empty)");
        Preference pref_ip = (Preference) findPreference("pref_ip");
        pref_ip.setSummary("Current IP address: " + pref_ip_string);

        if (!pref_server_string.equals("Live Trail")) {
            getPreferenceScreen().findPreference("pref_port").setEnabled(false);
        }
        if (!pref_server_string.equals("WiFi")) {
            getPreferenceScreen().findPreference("pref_ip").setEnabled(false);
        }

        setPreferenceChangeListener();
    }

    public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen,
                                          Preference preference) {
        return true;
    }
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("changed", "changed");
//        Preference pref = findPreference(key);

    }
    public void setPreferenceChangeListener(){
        final ListPreference server_pref = (ListPreference) findPreference("pref_server");
        // Loads the title for the first time
        // Listens for change in value, and then changes the title if required.
        server_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("newValue", newValue.toString());
                server_pref.setSummary("Current server: " + newValue.toString());

                if (newValue.toString().equals("Live Trail")) {
                    getPreferenceScreen().findPreference("pref_port").setEnabled(true);
                } else {
                    getPreferenceScreen().findPreference("pref_port").setEnabled(false);
                }

                if (newValue.toString().equals("WiFi")) {
                    getPreferenceScreen().findPreference("pref_ip").setEnabled(true);
                } else {
                    getPreferenceScreen().findPreference("pref_ip").setEnabled(false);
                }

                return true;
            }
        });

        final ListPreference team_mode_pref = (ListPreference) findPreference("team_mode");
        team_mode_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("newValue", newValue.toString());
                team_mode_pref.setSummary("Current mode: " + newValue.toString());
                return true;
            }
        });

        final EditTextPreference pref_ip = (EditTextPreference) findPreference("pref_ip");
        pref_ip.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("newValue", newValue.toString());
                pref_ip.setSummary("Current IP address: " + newValue.toString());
                return true;
            }
        });

    }

}
