package tcslab.syndesiapp.views;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import tcslab.syndesiapp.R;

/**
 * Shows the preferences screen
 * Created by Blaise on 27.04.2015.
 */
public class SettingsFragment extends PreferenceFragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
