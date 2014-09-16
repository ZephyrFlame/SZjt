package com.ilovelixin.szjt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainSettingsActivity extends Activity 
{
    public final static String PRF_DEFAULT_PAGE_KEY = "default_page_list";
    public final static String PRF_MOBILE_DATA_KEY = "mobile_data";
    public final static String PRF_WIFI_SETTING_KEY = "wifi_setting";
    public final static String PRF_WIFI_SWITCH_KEY = "wifi_switch";
    public final static String PRF_DATA_SWITCH_KEY = "data_switch";
    public final static String PRF_DEFAULT_PAGE_VALUE = "1";
    
    private final static String TAG = "MainSettingsActivity";
    
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
            
            CheckBoxPreference wifiPref = (CheckBoxPreference)findPreference(PRF_WIFI_SWITCH_KEY);
            boolean wstate = getWIFIStatus(MainSettingsActivity.this);
            wifiPref.setChecked(wstate);
            
            CheckBoxPreference dataPref = (CheckBoxPreference)findPreference(PRF_DATA_SWITCH_KEY);
            boolean mstate = getMobileDataStatus(MainSettingsActivity.this);
            dataPref.setChecked(mstate);
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
            else if (key.equals(PRF_WIFI_SWITCH_KEY))
            {
                toggleWIFI(MainSettingsActivity.this, sharedPreferences.getBoolean(PRF_WIFI_SWITCH_KEY, true));
            }
            else if (key.equals(PRF_DATA_SWITCH_KEY))
            {
                toggleMobileData(MainSettingsActivity.this, sharedPreferences.getBoolean(PRF_DATA_SWITCH_KEY, true));
            }
        }
        
        @Override
        public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen, Preference preference) 
        {
            String key = preference.getKey();
            if (key.equals(PRF_MOBILE_DATA_KEY))
            {
                //Intent intent = new Intent(Intent.ACTION_MAIN);
                //intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
                Intent intent =  new Intent(Settings.ACTION_WIRELESS_SETTINGS);  
                startActivity(intent);
            }
            else if (key.equals(PRF_WIFI_SETTING_KEY))
            {
                Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);
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
        
        private void toggleMobileData(Context context, boolean enabled) 
        {
            ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            Class<?> conMgrClass = null;                    // ConnectivityManager类
            Field iConMgrField = null;                      // ConnectivityManager类中的字段
            Object iConMgr = null;                          // IConnectivityManager类的引用
            Class<?> iConMgrClass = null;                   // IConnectivityManager类
            Method setMobileDataEnabledMethod = null;       // setMobileDataEnabled方法

            try 
            {
                // 取得ConnectivityManager类
                conMgrClass = Class.forName(conMgr.getClass().getName());
                Log.i(TAG, "conMgr:" + conMgr.getClass().getName());
                // 取得ConnectivityManager类中的对象mService
                iConMgrField = conMgrClass.getDeclaredField("mService");
                // 设置mService可访问
                iConMgrField.setAccessible(true);
                // 取得mService的实例化类IConnectivityManager
                iConMgr = iConMgrField.get(conMgr);
                // 取得IConnectivityManager类
                iConMgrClass = Class.forName(iConMgr.getClass().getName());
                Log.i(TAG, "iConMgr:" + iConMgr.getClass().getName());
                int count = 0;
                Method[] methods = iConMgrClass.getMethods();
                Log.i(TAG, "Method:" + methods.length);
                for (Method m : methods)
                {
                    if (m.getName().contains("setMobileDataEnabled"))
                    {
                        Class<?>[] parameters = m.getParameterTypes(); 
                        count = parameters.length; 
                        String pstr = ""; 
                        Log.i(TAG, "setMobileDataEnabled->parameter count:" + count); 
                        if (count > 0) 
                        { 
                            for (Class<?> p : parameters) 
                            { 
                                pstr += p.getName() + ","; 
                            } 
                            pstr = pstr.substring(0, pstr.length() - 1); 
                            Log.i(TAG, "pstr:" + pstr); 
                        }
                        break;
                    }
                }
                // 取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
                if (count < 2)
                {
                    setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", boolean.class);
                }
                else
                {
                    Class[] cArg = new Class[2];
                    cArg[0] = String.class;
                    cArg[1] = Boolean.TYPE;
                    setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", cArg);
                }
                Log.i(TAG, "setMobileDataEnabledMethod:" + setMobileDataEnabledMethod.getName());
                // 设置setMobileDataEnabled方法可访问
                setMobileDataEnabledMethod.setAccessible(true);
                // 调用setMobileDataEnabled方法
                if (count < 2)
                {
                    setMobileDataEnabledMethod.invoke(iConMgr, enabled);
                }
                else
                {
                    Object[] pArg = new Object[2];
                    pArg[0] = context.getPackageName();
                    pArg[1] = enabled;
                    setMobileDataEnabledMethod.invoke(iConMgr, pArg);
                }
           } 
           catch (ClassNotFoundException e) 
           {
               e.printStackTrace();
               Log.i(TAG, "ClassNotFoundException:" + e.getStackTrace());
           } 
           catch (NoSuchFieldException e) 
           {
               e.printStackTrace();
               Log.i(TAG, "NoSuchFieldException:" + e.getStackTrace());
           } 
           catch (SecurityException e) 
           {
               e.printStackTrace();
               Log.i(TAG, "SecurityException:" + e.getStackTrace());
           } 
           catch (NoSuchMethodException e) 
           {
               e.printStackTrace();
               Log.i(TAG, "NoSuchMethodException:" + e.getMessage());
           } 
           catch (IllegalArgumentException e) 
           {
               e.printStackTrace();
               Log.i(TAG, "IllegalArgumentException:" + e.getStackTrace());
           } 
           catch (IllegalAccessException e) 
           {
               e.printStackTrace();
               Log.i(TAG, "IllegalAccessException:" + e.getStackTrace());
           } 
           catch (InvocationTargetException e) 
           {
               e.printStackTrace();
               Log.i(TAG, "InvocationTargetException:" + e.getStackTrace());
               
               Throwable ori = e.getTargetException();
               ori.printStackTrace();
               Log.i(TAG, "InvocationTargetException:" + ori.getMessage());
           }
           catch (Exception e) 
           {
               e.printStackTrace();
               Log.i(TAG, "IllegalAccessException:" + e.getStackTrace());
           }
        } 
        
        private boolean getMobileDataStatus(Context context) 
        {
            ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            Class<?> conMgrClass = null;                    // ConnectivityManager类
            Field iConMgrField = null;                      // ConnectivityManager类中的字段
            Object iConMgr = null;                          // IConnectivityManager类的引用
            Class<?> iConMgrClass = null;                   // IConnectivityManager类
            Method getMobileDataEnabledMethod = null;       // setMobileDataEnabled方法
       
            try 
            {
                // 取得ConnectivityManager类
                conMgrClass = Class.forName(conMgr.getClass().getName());
                Log.i(TAG, "conMgr:" + conMgr.getClass().getName());
                // 取得ConnectivityManager类中的对象mService
                iConMgrField = conMgrClass.getDeclaredField("mService");
                // 设置mService可访问
                iConMgrField.setAccessible(true);
                // 取得mService的实例化类IConnectivityManager
                iConMgr = iConMgrField.get(conMgr);
                // 取得IConnectivityManager类
                iConMgrClass = Class.forName(iConMgr.getClass().getName());
                Log.i(TAG, "conMgr:" + conMgr.getClass().getName());
                // 取得IConnectivityManager类中的getMobileDataEnabled(boolean)方法
                getMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("getMobileDataEnabled");
                Log.i(TAG, "getMobileDataEnabledMethod:" + getMobileDataEnabledMethod.getName());
                // 设置getMobileDataEnabled方法可访问
                getMobileDataEnabledMethod.setAccessible(true);
                // 调用getMobileDataEnabled方法
                return (Boolean) getMobileDataEnabledMethod.invoke(iConMgr);
            } 
            catch (ClassNotFoundException e) 
            {
                e.printStackTrace();
                Log.i(TAG, "ClassNotFoundException:" + e.getStackTrace());
            } 
            catch (NoSuchFieldException e) 
            {
                e.printStackTrace();
                Log.i(TAG, "NoSuchFieldException:" + e.getStackTrace());
            } 
            catch (SecurityException e) 
            {
                e.printStackTrace();
                Log.i(TAG, "SecurityException:" + e.getStackTrace());
            } 
            catch (NoSuchMethodException e) 
            {
                e.printStackTrace();
                Log.i(TAG, "NoSuchMethodException:" + e.getMessage());
            } 
            catch (IllegalArgumentException e) 
            {
                e.printStackTrace();
                Log.i(TAG, "IllegalArgumentException:" + e.getStackTrace());
            } 
            catch (IllegalAccessException e) 
            {
                e.printStackTrace();
                Log.i(TAG, "IllegalAccessException:" + e.getStackTrace());
            } 
            catch (InvocationTargetException e) 
            {
                e.printStackTrace();
                Log.i(TAG, "InvocationTargetException:" + e.getStackTrace());
            }
            catch (Exception e) 
            {
                e.printStackTrace();
                Log.i(TAG, "IllegalAccessException:" + e.getStackTrace());
            }
            
            return false;
        }
        
        private void toggleWIFI(Context context, boolean enabled) 
        {
            WifiManager wifiMgr =  (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            if (enabled)
            {
                if (!wifiMgr.isWifiEnabled() )
                {
                    wifiMgr.setWifiEnabled(true);
                }
            }
            else
            {
                if (wifiMgr.isWifiEnabled() )
                {
                    wifiMgr.setWifiEnabled(false);
                }
            }
        }
        
        private boolean getWIFIStatus(Context context) 
        {
            WifiManager wifiMgr =  (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            if (wifiMgr.getWifiState()== WifiManager.WIFI_STATE_ENABLED)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
