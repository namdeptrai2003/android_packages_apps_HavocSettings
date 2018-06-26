/*
 * Copyright (C) 2018 Havoc-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.havoc.settings.fragments;

import android.app.Activity;
import android.content.Context;
import android.app.AlertDialog; 
import android.app.Dialog; 
import android.content.Intent;
import android.content.pm.PackageManager; 
import android.content.pm.ResolveInfo; 
import android.content.res.Resources; 
import android.graphics.drawable.Drawable; 
import android.content.DialogInterface; 
import android.content.DialogInterface.OnClickListener; 
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.content.SharedPreferences; 
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.content.ContentResolver; 
import android.content.res.Resources; 
import android.os.UserHandle; 
import android.provider.Settings;
import android.view.LayoutInflater; 
import android.view.View; 
import android.view.ViewGroup; 
import android.widget.BaseAdapter; 
import android.widget.AdapterView; 
import android.widget.AdapterView.OnItemClickListener; 
import android.widget.ImageView; 
import android.widget.RadioButton; 
import android.widget.TextView; 
import android.widget.Toast; 
import android.widget.ListView; 
import android.provider.Settings.SettingNotFoundException;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.development.DevelopmentSettings;
import com.android.settings.SettingsPreferenceFragment;
import org.lineageos.internal.util.PackageManagerUtils;
import com.havoc.settings.preferences.Utils;
import com.havoc.settings.R;

import java.util.ArrayList; 
import java.util.Collections; 
import java.util.Comparator; 
import java.util.HashMap; 
import java.util.Map; 
import java.util.List; 

import com.android.internal.util.omni.OmniSwitchConstants;
import com.havoc.settings.preferences.MasterSwitchPreference;

public class Recents extends SettingsPreferenceFragment implements 
Preference.OnPreferenceChangeListener, DialogInterface.OnDismissListener  {

    public static final String TAG = "Recents";
    private static final String SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents"; 
    private static final String RECENTS_ICON_PACK = "recents_icon_pack"; 
    private static final String RECENTS_MEMBAR = "systemui_recents_mem_display"; 
    private static final String RECENTS_CLEAR_ALL = "show_clear_all_recents"; 
    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location"; 
    private static final String RECENTS_DISMISS_ICON = "recents_dismiss_icon"; 
    private static final String RECENTS_LOCK_ICON = "recents_lock_icon"; 
    private static final String IMMERSIVE_RECENTS = "immersive_recents"; 
    private static final String RECENTS_DATE = "recents_full_screen_date"; 
    private static final String RECENTS_CLOCK = "recents_full_screen_clock"; 
    private static final String RECENTS_TYPE = "recents_layout_style"; 
    
    // private static final String USE_SLIM_RECENTS = "use_slim_recents";
    // private static final String PREF_STOCK_RECENTS_CATEGORY = "stock_recents_category";
    // private static final String PREF_ALTERNATIVE_RECENTS_CATEGORY = "alternative_recents_category";

    private static final String RECENTS_USE_OMNISWITCH = "recents_use_omniswitch";
    private static final String RECENTS_USE_SLIM= "use_slim_recents";
    private static final String OMNISWITCH_START_SETTINGS = "omniswitch_start_settings";

    // Package name of the omnniswitch app
    public static final String OMNISWITCH_PACKAGE_NAME = "org.omnirom.omniswitch";
    // Intent for launching the omniswitch settings actvity
    public static Intent INTENT_OMNISWITCH_SETTINGS = new Intent(Intent.ACTION_MAIN)
            .setClassName(OMNISWITCH_PACKAGE_NAME, OMNISWITCH_PACKAGE_NAME + ".SettingsActivity");
    private static final String CATEGORY_STOCK_RECENTS = "stock_recents";
    private static final String CATEGORY_OMNI_RECENTS = "omni";
    private static final String CATEGORY_SLIM_RECENTS = "slim_recents";

    private PreferenceCategory mStockRecents;
    private PreferenceCategory mOmniRecents;
    private PreferenceCategory mSlimRecents;
    private SwitchPreference mRecentsUseOmniSwitch;
    private Preference mOmniSwitchSettings;
    private boolean mOmniSwitchInitCalled;
    private SwitchPreference mSlimToggle;

    private Preference mRecentsIconPack; 
    private Preference mRecentsMembar; 
    private Preference mRecentsClearAll; 
    private ListPreference mRecentsClearAllLocation; 
    private Preference mRecentsDismissIcon; 
    private Preference mRecentsLockIcon; 
    private ListPreference mImmersiveRecents; 
    private SwitchPreference mClock; 
    private SwitchPreference mDate; 
    private ListPreference mRecentsType; 

    // private Preference mSlimRecents;
    // private PreferenceCategory mStockRecentsCategory;
    // private PreferenceCategory mAlternativeRecentsCategory;
    // private MasterSwitchPreference mOmniSwitchPreference;

    private final static String[] sSupportedActions = new String[] { 
        "org.adw.launcher.THEMES", 
        "com.gau.go.launcherex.theme" 
    }; 
 
    private static final String[] sSupportedCategories = new String[] { 
        "com.fede.launcher.THEME_ICONPACK", 
        "com.anddoes.launcher.THEME", 
        "com.teslacoilsw.launcher.THEME" 
    }; 
 
    private AlertDialog mDialog; 
    private ListView mListView; 

    private SharedPreferences mPreferences; 
    private Context mContext; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.havoc_settings_recents);

        mContext = getActivity().getApplicationContext(); 
        final ContentResolver resolver = getContentResolver(); 
        final PreferenceScreen prefSet = getPreferenceScreen(); 
        final Resources res = getResources(); 

        // mStockRecentsCategory = (PreferenceCategory) findPreference(PREF_STOCK_RECENTS_CATEGORY);
        // mAlternativeRecentsCategory =
        //         (PreferenceCategory) findPreference(PREF_ALTERNATIVE_RECENTS_CATEGORY);

        // mOmniSwitchPreference = (MasterSwitchPreference)
        //         findPreference(Settings.System.RECENTS_OMNI_SWITCH_ENABLED);

        String currentIconPack =  Settings.System.getStringForUser(resolver, 
        Settings.System.RECENTS_ICON_PACK, UserHandle.USER_CURRENT); 

        mRecentsIconPack = (Preference) findPreference(RECENTS_ICON_PACK); 
        if (currentIconPack != null && !currentIconPack.isEmpty()) { 
            mRecentsIconPack.setSummary(currentIconPack); 
        } else { 
            mRecentsIconPack.setSummary(R.string.recents_icon_pack_summary); 
        } 

        mImmersiveRecents = (ListPreference) findPreference(IMMERSIVE_RECENTS); 
        int mode = Settings.System.getInt(getContentResolver(), 
        Settings.System.IMMERSIVE_RECENTS, 0); 
            mImmersiveRecents.setValue(String.valueOf(mode)); 
        mImmersiveRecents.setSummary(mImmersiveRecents.getEntry()); 
        mImmersiveRecents.setOnPreferenceChangeListener(this); 

        // recents type 
        mRecentsType = (ListPreference) findPreference(RECENTS_TYPE); 
        int style = Settings.System.getIntForUser(getContentResolver(), 
                Settings.System.RECENTS_LAYOUT_STYLE, 0, UserHandle.USER_CURRENT); 
        mRecentsType.setValue(String.valueOf(style)); 
        mRecentsType.setSummary(mRecentsType.getEntry()); 
        mRecentsType.setOnPreferenceChangeListener(this); 

        mRecentsMembar = (Preference) findPreference(RECENTS_MEMBAR); 
 
        mRecentsClearAll = (Preference) findPreference(RECENTS_CLEAR_ALL); 
 
        mRecentsClearAllLocation = (ListPreference) findPreference(RECENTS_CLEAR_ALL_LOCATION); 
        int location = Settings.System.getIntForUser(resolver, 
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT); 
        mRecentsClearAllLocation.setValue(String.valueOf(location)); 
        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry()); 
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this); 
 
        mRecentsDismissIcon = (Preference) findPreference(RECENTS_DISMISS_ICON); 
 
        mRecentsLockIcon = (Preference) findPreference("recents_icon_pack");

        mClock = (SwitchPreference) findPreference(RECENTS_CLOCK); 
        mDate = (SwitchPreference) findPreference(RECENTS_DATE); 
        updateDisablestate(mode); 

        // mSlimRecents = (Preference) findPreference(USE_SLIM_RECENTS);
        // mSlimRecents.setOnPreferenceChangeListener(this);

        // boolean mUseSlimRecents = Settings.System.getIntForUser(
        //         resolver, Settings.System.USE_SLIM_RECENTS, 0,
        //         UserHandle.USER_CURRENT) == 1;
        // toggleAOSPrecents(!mUseSlimRecents);

        
        // Alternative recents en-/disabling
        // Preference.OnPreferenceChangeListener alternativeRecentsChangeListener =
        //         new Preference.OnPreferenceChangeListener() {
        //     @Override
        //     public boolean onPreferenceChange(Preference preference, Object newValue) {
        //         updateDependencies((Boolean) newValue ? preference : null);
        //         return true;
        //     }
        // };
        // for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
        //     Preference preference = mAlternativeRecentsCategory.getPreference(i);
        //     if (preference instanceof MasterSwitchPreference) {
        //         preference.setOnPreferenceChangeListener(alternativeRecentsChangeListener);
        //     }
        // }

        mStockRecents = (PreferenceCategory) findPreference(CATEGORY_STOCK_RECENTS);
        mOmniRecents = (PreferenceCategory) findPreference(CATEGORY_OMNI_RECENTS);
        mSlimRecents = (PreferenceCategory) findPreference(CATEGORY_SLIM_RECENTS);

        mSlimToggle = (SwitchPreference) findPreference("use_slim_recents");
        boolean enabled = Settings.System.getIntForUser(
                getActivity().getContentResolver(), Settings.System.USE_SLIM_RECENTS, 0,
                UserHandle.USER_CURRENT) == 1;
        mSlimToggle.setChecked(enabled);
        mRecentsIconPack.setEnabled(!enabled);
        mSlimToggle.setOnPreferenceChangeListener(this);

        mRecentsUseOmniSwitch = (SwitchPreference)
                prefSet.findPreference(RECENTS_USE_OMNISWITCH);

        try {
            mRecentsUseOmniSwitch.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.RECENTS_OMNI_SWITCH_ENABLED) == 1);
            mOmniSwitchInitCalled = true;
        } catch(SettingNotFoundException e){
            // if the settings value is unset
        }
        mRecentsUseOmniSwitch.setOnPreferenceChangeListener(this);

        mOmniSwitchSettings = (Preference)
                prefSet.findPreference(OMNISWITCH_START_SETTINGS);
        mOmniSwitchSettings.setEnabled(mRecentsUseOmniSwitch.isChecked());

        updateRecents();
    }

    public void updateDisablestate(int mode) { 
        if (mode == 0 || mode == 2) { 
           mClock.setEnabled(false); 
           mDate.setEnabled(false); 
        } else { 
           mClock.setEnabled(true); 
           mDate.setEnabled(true); 
        } 
    } 

    // @Override
    // public void onResume() {
    //     super.onResume();

    //     if (isOmniSwitchInstalled()) {
    //         mOmniSwitchPreference.setEnabled(true);
    //     } else {
    //         Settings.System.putInt(getContentResolver(),
    //                 Settings.System.RECENTS_OMNI_SWITCH_ENABLED, 0);
    //         mOmniSwitchPreference.setEnabled(false);
    //     }

    //     for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
    //         Preference preference = mAlternativeRecentsCategory.getPreference(i);
    //         if (preference instanceof MasterSwitchPreference) {
    //             ((MasterSwitchPreference) preference).reloadValue();
    //         }
    //     }
    //     updateDependencies(null);
    // }

    // private void updateDependencies(Preference enabledAlternativeRecentsPreference) {
    //     boolean alternativeRecentsEnabled = false;
    //     for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
    //         Preference preference = mAlternativeRecentsCategory.getPreference(i);
    //         if (enabledAlternativeRecentsPreference != null
    //                 && enabledAlternativeRecentsPreference != preference
    //                 && preference instanceof MasterSwitchPreference
    //                 && ((MasterSwitchPreference) preference).isChecked()) {
    //             // Only one alternative recents at the time!
    //             ((MasterSwitchPreference) preference).setCheckedPersisting(false);
    //         } else if (preference instanceof MasterSwitchPreference
    //                 && ((MasterSwitchPreference) preference).isChecked()) {
    //             alternativeRecentsEnabled = true;
    //         }
    //     }
    //     mStockRecentsCategory.setEnabled(!alternativeRecentsEnabled);
    // }

    // private boolean isOmniSwitchInstalled() {
    //     return Utils.isPackageEnabled(OmniSwitchConstants.APP_PACKAGE_NAME,
    //             getActivity().getPackageManager());
    // }

    @Override   
    public boolean onPreferenceChange(Preference preference, Object newValue){ 
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mImmersiveRecents) {
            int mode = Integer.valueOf((String) newValue); 
            Settings.System.putIntForUser(resolver, Settings.System.IMMERSIVE_RECENTS,
                    Integer.parseInt((String) newValue), UserHandle.USER_CURRENT);
            mImmersiveRecents.setValue((String) newValue);
            mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
            updateDisablestate(mode);
            mPreferences = mContext.getSharedPreferences("recent_settings", Activity.MODE_PRIVATE);
            if (!mPreferences.getBoolean("first_info_shown", false) && newValue != null) {
                getActivity().getSharedPreferences("recent_settings", Activity.MODE_PRIVATE)
                        .edit()
                        .putBoolean("first_info_shown", true)
                        .commit();
                openAOSPFirstTimeWarning();
            }
            return true;
         } else if (preference == mRecentsClearAllLocation) {
            int value = Integer.parseInt((String) newValue);
            int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, value, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
            return true;
        } else if (preference == mRecentsType) { 
            int style = Integer.valueOf((String) newValue); 
            int index = mRecentsType.findIndexOfValue((String) newValue); 
            Settings.System.putIntForUser(getActivity().getContentResolver(), 
                    Settings.System.RECENTS_LAYOUT_STYLE, style, UserHandle.USER_CURRENT); 
            mRecentsType.setSummary(mRecentsType.getEntries()[index]); 
            Utils.restartSystemUi(getContext()); 
        return true; 
        } else if (preference == mSlimToggle) {
        boolean value = (Boolean) newValue;
        Settings.System.putIntForUser(getContentResolver(),
                Settings.System.USE_SLIM_RECENTS, value ? 1 : 0,
                UserHandle.USER_CURRENT);
        mSlimToggle.setChecked(value);
        mRecentsIconPack.setEnabled(!value);
        updateRecents();
        return true;
    } else if (preference == mRecentsUseOmniSwitch) {
        boolean value = (Boolean) newValue;
        // if value has never been set before
        if (value && !mOmniSwitchInitCalled){
            openOmniSwitchFirstTimeWarning();
            mOmniSwitchInitCalled = true;
        }
        Settings.System.putInt(getContentResolver(),
                Settings.System.RECENTS_OMNI_SWITCH_ENABLED, value ? 1 : 0);
        mOmniSwitchSettings.setEnabled(value);
        updateRecents();
        return true;
    }
        return false;
    } 

    // private void toggleAOSPrecents(boolean enabled) {
    //     mRecentsIconPack.setEnabled(enabled);
    //     mImmersiveRecents.setEnabled(enabled);
    //     mRecentsMembar.setEnabled(enabled);
    //     mRecentsClearAll.setEnabled(enabled);
    //     mRecentsClearAllLocation.setEnabled(enabled);
    //     mRecentsDismissIcon.setEnabled(enabled);
    //     mRecentsLockIcon.setEnabled(enabled);
    // }


    @Override 
    public boolean onPreferenceTreeClick(Preference preference) { 
        if (preference == mRecentsIconPack) { 
            pickIconPack(getContext()); 
            return true; 
        } else if (preference == mOmniSwitchSettings){
            startActivity(INTENT_OMNISWITCH_SETTINGS);
            return true;
        }
        return super.onPreferenceTreeClick(preference); 
    } 
 
    /** Recents Icon Pack Dialog **/ 
    private void pickIconPack(final Context context) { 
        if (mDialog != null) { 
            return; 
        } 
        Map<String, IconPackInfo> supportedPackages = getSupportedPackages(context); 
        if (supportedPackages.isEmpty()) { 
            Toast.makeText(context, R.string.no_iconpacks_summary, Toast.LENGTH_SHORT).show(); 
            return; 
        } 
        AlertDialog.Builder builder = new AlertDialog.Builder(context) 
        .setTitle(R.string.dialog_pick_iconpack_title) 
        .setOnDismissListener(this) 
        .setNegativeButton(R.string.cancel, null) 
        .setView(createDialogView(context, supportedPackages)); 
        mDialog = builder.show(); 
    } 

    
    private void openOmniSwitchFirstTimeWarning() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.omniswitch_first_time_title))
                .setMessage(getResources().getString(R.string.omniswitch_first_time_message))
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                }).show();
    }

    private void updateRecents() {
        boolean omniRecents = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.RECENTS_OMNI_SWITCH_ENABLED, 0) == 1;
        boolean isOmniInstalled = PackageManagerUtils.isAppInstalled(getActivity(), OMNISWITCH_PACKAGE_NAME);
        boolean slimRecents = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.USE_SLIM_RECENTS, 0) == 1;

        mStockRecents.setEnabled(!omniRecents && !slimRecents);
        // Slim recents overwrites omni recents
        mOmniRecents.setEnabled(omniRecents || !slimRecents);
        // Don't allow OmniSwitch if we're already using slim recents
        mSlimRecents.setEnabled(slimRecents || !omniRecents);
    }

    private void openAOSPFirstTimeWarning() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.aosp_first_time_title))
                .setMessage(getResources().getString(R.string.aosp_first_time_message))
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                }).show();
    }
 
    private View createDialogView(final Context context, Map<String, IconPackInfo> supportedPackages) { 
        final LayoutInflater inflater = (LayoutInflater) context 
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        final View view = inflater.inflate(R.layout.dialog_iconpack, null); 
        final IconAdapter adapter = new IconAdapter(context, supportedPackages); 
 
        mListView = (ListView) view.findViewById(R.id.iconpack_list); 
        mListView.setAdapter(adapter); 
        mListView.setOnItemClickListener(new OnItemClickListener() { 
            @Override 
            public void onItemClick(AdapterView<?> parent, View view, 
                        int position, long id) { 
                if (adapter.isCurrentIconPack(position)) { 
                    return; 
                } 
                String selectedPackage = adapter.getItem(position); 
                Settings.System.putStringForUser(getContext().getContentResolver(), 
                Settings.System.RECENTS_ICON_PACK, selectedPackage, UserHandle.USER_CURRENT);  
                mDialog.dismiss(); 
            } 
        }); 
 
        return view; 
    } 
 
    @Override 
    public void onDismiss(DialogInterface dialog) { 
        if (mDialog != null) { 
            mDialog = null; 
        } 
        String currentIconPack =  Settings.System.getStringForUser( 
            getContext().getContentResolver(), 
            Settings.System.RECENTS_ICON_PACK, UserHandle.USER_CURRENT); 
        if (mRecentsIconPack != null && currentIconPack != null && !currentIconPack.isEmpty())  {
            mRecentsIconPack.setSummary(currentIconPack); 
        } else { 
            mRecentsIconPack.setSummary(R.string.recents_icon_pack_summary); 
        } 
    } 
 
    private static class IconAdapter extends BaseAdapter { 
        ArrayList<IconPackInfo> mSupportedPackages; 
        LayoutInflater mLayoutInflater; 
        String mCurrentIconPack; 
        int mCurrentIconPackPosition = -1; 
 
        IconAdapter(Context ctx, Map<String, IconPackInfo> supportedPackages) { 
            mLayoutInflater = LayoutInflater.from(ctx); 
            mSupportedPackages = new ArrayList<IconPackInfo>(supportedPackages.values()); 
            Collections.sort(mSupportedPackages, new Comparator<IconPackInfo>() { 
                @Override 
                public int compare(IconPackInfo lhs, IconPackInfo rhs) { 
                    return lhs.label.toString().compareToIgnoreCase(rhs.label.toString()); 
                } 
            }); 
 
            Resources res = ctx.getResources(); 
            String defaultLabel = res.getString(R.string.default_iconpack_title); 
            Drawable icon = res.getDrawable(android.R.drawable.sym_def_app_icon); 
            mSupportedPackages.add(0, new IconPackInfo(defaultLabel, icon, "")); 
            mCurrentIconPack = Settings.System.getStringForUser(ctx.getContentResolver(), 
                Settings.System.RECENTS_ICON_PACK, UserHandle.USER_CURRENT); 
        } 
 
        @Override 
        public int getCount() { 
            return mSupportedPackages.size(); 
        } 
 
        @Override 
        public String getItem(int position) { 
            return (String) mSupportedPackages.get(position).packageName; 
        } 
 
        @Override 
        public long getItemId(int position) { 
            return 0; 
        } 
 
        public boolean isCurrentIconPack(int position) { 
            return mCurrentIconPackPosition == position; 
        } 
 
        @Override 
        public View getView(int position, View convertView, ViewGroup parent) { 
            if (convertView == null) { 
                convertView = mLayoutInflater.inflate(R.layout.iconpack_view_radio, null); 
            } 
            IconPackInfo info = mSupportedPackages.get(position); 
            TextView txtView = (TextView) convertView.findViewById(R.id.title); 
            txtView.setText(info.label); 
            ImageView imgView = (ImageView) convertView.findViewById(R.id.icon); 
            imgView.setImageDrawable(info.icon); 
            RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.radio); 
            boolean isCurrentIconPack = info.packageName.equals(mCurrentIconPack); 
            radioButton.setChecked(isCurrentIconPack); 
            if (isCurrentIconPack) { 
                mCurrentIconPackPosition = position; 
            } 
            return convertView; 
        } 
    } 
 
    private Map<String, IconPackInfo> getSupportedPackages(Context context) { 
        Intent i = new Intent(); 
        Map<String, IconPackInfo> packages = new HashMap<String, IconPackInfo>(); 
        PackageManager packageManager = context.getPackageManager(); 
        for (String action : sSupportedActions) { 
            i.setAction(action); 
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) { 
                IconPackInfo info = new IconPackInfo(r, packageManager); 
                packages.put(r.activityInfo.packageName, info); 
            } 
        } 
        i = new Intent(Intent.ACTION_MAIN); 
        for (String category : sSupportedCategories) { 
            i.addCategory(category); 
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) { 
                IconPackInfo info = new IconPackInfo(r, packageManager); 
                packages.put(r.activityInfo.packageName, info); 
            } 
            i.removeCategory(category); 
        } 
        return packages; 
    } 
 
    static class IconPackInfo { 
        String packageName; 
        CharSequence label; 
        Drawable icon; 
 
        IconPackInfo(ResolveInfo r, PackageManager packageManager) { 
            packageName = r.activityInfo.packageName; 
            icon = r.loadIcon(packageManager); 
            label = r.loadLabel(packageManager); 
        } 
 
        IconPackInfo(){ 
        } 
 
        public IconPackInfo(String label, Drawable icon, String packageName) { 
            this.label = label; 
            this.icon = icon; 
            this.packageName = packageName; 
        } 
    } 


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HAVOC_SETTINGS;
    }
}
