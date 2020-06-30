package safetyapp.srrr.com.fearless;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import safetyapp.srrr.com.fearless.R;

import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
//        Fragment currentFragment = getFragmentManager().findFragmentById("");
        Preference darkModeToggle = findPreference(getString(R.string.darkMode));
        darkModeToggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(),"Dark Mode Toggled",Toast.LENGTH_LONG).show();
                getActivity().recreate();

                return true;
            }
        });

    }
}
