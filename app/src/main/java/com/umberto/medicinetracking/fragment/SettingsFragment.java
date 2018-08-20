package com.umberto.medicinetracking.fragment;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.utils.NetworkUtils;
import com.umberto.medicinetracking.utils.PrefercenceUtils;


public class SettingsFragment extends PreferenceFragmentCompat implements
        OnSharedPreferenceChangeListener {
    private static final int REQUEST_CODE_SIGN_IN = 0;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Add visualizer preferences, defined in the XML file in res->xml->pref_visualizer
        addPreferencesFromResource(R.xml.pref_medicine);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();

        //Set current account name in preference summary
        Preference p=prefScreen.findPreference(getString(R.string.pref_backup_account_key));
        if(p.getKey()==getString(R.string.pref_backup_account_key)){
            if (NetworkUtils.isSignedIn(getContext())) {
                p.setSummary(NetworkUtils.accountDisplayName(getContext()));
            } else {
                p.setSummary("");
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Figure out which preference was changed
        Preference preference = findPreference(key);
        if(preference instanceof SwitchPreference){
            setChecked(preference, sharedPreferences.getBoolean(preference.getKey(), false));
        }

        if(key==getString(R.string.pref_backup_remote_key)){
            boolean value = sharedPreferences.getBoolean(preference.getKey(), false);
            if(value){
                GoogleSignInClient googleSignInClient = buildGoogleSignInClient();
                startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
            } else {
                signOut();
                Preference accountPreferenc=findPreference(getString(R.string.pref_backup_account_key));
                accountPreferenc.setSummary("");
            }
        }
    }

    private void setChecked(Preference preference, boolean checked){
        SwitchPreference sp=(SwitchPreference) preference;
        if(checked!=sp.isChecked()){
            sp.setChecked(checked);
        }
    }


    /**
     * Updates the summary for the preference
     *
     * @param key The preference key to be updated
     * @param value      The value that the preference was updated to
     */
    private void setPreferenceSummary(String key, String value) {
        Preference preference = findPreference(key);
        if (preference != null) {
            preference.setSummary(value);
        }
    }

    /**
     * Updates preference checked
     *
     * @param key The preference key to be updated
     * @param value      The value that the preference was updated to
     */
    private void setPreferenceChecked(String key, boolean value) {
        Preference preference = findPreference(key);
        if (preference != null) {
            SwitchPreference sw=(SwitchPreference)preference;
            sw.setChecked(value);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER,Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(getContext(), signInOptions);
    }

    //Result of signin account
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_SIGN_IN){
            if(resultCode == Activity.RESULT_OK){
                PrefercenceUtils.setBackupAccount(getContext(),true);
                setPreferenceSummary(getString(R.string.pref_backup_account_key), NetworkUtils.accountDisplayName(getContext()));
            } else {
                //Signed in wrond return preference checked false
                PrefercenceUtils.setBackupRemote(getContext(), false);
                //android.support.v14.preference.SwitchPreference sp=(android.support.v14.preference.SwitchPreference) getPreferenceScreen().findPreference(getString(R.string.pref_backup_remote_key));
                //sp.setChecked(false);
                PrefercenceUtils.setBackupRemote(getContext(), false);
            }

        }
    }

    //Signout of Google account
    private void signOut(){
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build();
        GoogleSignInClient sClient=buildGoogleSignInClient();

        if(sClient!=null) {
            sClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            setPreferenceSummary(getString(R.string.pref_backup_account_key), "");
                            PrefercenceUtils.setBackupAccount(getContext(), false);
                            PrefercenceUtils.setBackupRemote(getContext(), false);
                        }
                    });
        }
    }
}