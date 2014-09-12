package com.ilovelixin.szjt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class MainSettingsActivity extends Activity 
{
    public final static String PRF_DEFAULT_PAGE_KEY = "default_page_list";
    public final static String PRF_MOBILE_DATA_KEY = "mobile_data";
    public final static String PRF_DEFAULT_PAGE_VALUE = "1";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		setupPreferencesScreen();
	}

    private void setupPreferencesScreen() 
	{
        getFragmentManager().beginTransaction().replace(android.R.id.content,  new PrefsFragment()).commit();
	}
    
    public class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreate(Bundle savedInstanceState) 
        {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainSettingsActivity.this);
            Preference connectionPref = findPreference(PRF_DEFAULT_PAGE_KEY);
            int index = Integer.parseInt(sharedPreferences.getString(PRF_DEFAULT_PAGE_KEY, PRF_DEFAULT_PAGE_VALUE));
            int[] id = {R.string.title_section1, R.string.title_section2, R.string.title_section3};
            connectionPref.setSummary(getString(id[index]));
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
        {
            // TODO Auto-generated method stub
            // Set summary to be the user-description for the selected value
            if (key.equals(PRF_DEFAULT_PAGE_KEY))
            {
                Preference connectionPref = findPreference(key);
                int index = Integer.parseInt(sharedPreferences.getString(key, PRF_DEFAULT_PAGE_VALUE));
                int[] id = {R.string.title_section1, R.string.title_section2, R.string.title_section3};
                connectionPref.setSummary(getString(id[index]));
            }
        }
        
        @Override
        public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen, Preference preference) 
        {
            if (preference.getKey().equals(PRF_MOBILE_DATA_KEY))
            {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
                startActivity(intent);
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onResume() 
        {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() 
        {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }
}
