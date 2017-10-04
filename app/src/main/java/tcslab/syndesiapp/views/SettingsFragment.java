package tcslab.syndesiapp.views;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import tcslab.syndesiapp.R;

/**
 * Shows the preferences screen.
 *
 * Created by Blaise on 27.04.2015.
 */
public class SettingsFragment extends PreferenceFragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Demo remove localization rate and precision
        PreferenceScreen screen = getPreferenceScreen();
        Preference pref = getPreferenceManager().findPreference("PREF_LOC_RATE");
        screen.removePreference(pref);
        pref = getPreferenceManager().findPreference("PREF_LOC_PRECISION");
        screen.removePreference(pref);
        pref = getPreferenceManager().findPreference("PREF_AUTO_LOC_PERM");
        screen.removePreference(pref);
    }
}
