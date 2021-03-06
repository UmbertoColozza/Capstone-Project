package com.umberto.medicinetracking.fragment;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.utils.NetworkUtils;
import com.umberto.medicinetracking.utils.PrefercenceUtils;

public class SettingsFragment extends PreferenceFragmentCompat implements
        OnSharedPreferenceChangeListener {
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_STORAGE_PERMISSION = 1;


    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Add visualizer preferences, defined in the XML file in res->xml->pref_visualizer
        addPreferencesFromResource(R.xml.pref_medicine);

        PreferenceScreen prefScreen = getPreferenceScreen();

        //Set current account name in preference summary
        Preference p=prefScreen.findPreference(getString(R.string.pref_backup_account_key));
        if(p.getKey().equals(getString(R.string.pref_backup_account_key))){
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

        if(key.equals(getString(R.string.pref_backup_sync_key))){
            boolean value = sharedPreferences.getBoolean(preference.getKey(), false);
            if(value && !PrefercenceUtils.getBackupRemote(getContext())){
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // If you do not have permission, request it
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                }
            }
        }
        if(key.equals(getString(R.string.pref_backup_remote_key))){
            boolean value = sharedPreferences.getBoolean(preference.getKey(), false);
            if(value){
                GoogleSignInClient googleSignInClient = buildGoogleSignInClient();
                startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
            } else {
                signOut();
                Preference accountPreference=findPreference(getString(R.string.pref_backup_account_key));
                accountPreference.setSummary("");
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
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestScopes(Drive.SCOPE_APPFOLDER,Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(getContext(), signInOptions);
    }

    //Result of signin account
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_SIGN_IN){
            if(resultCode == Activity.RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    PrefercenceUtils.setBackupAccount(getContext(), true);
                    setPreferenceSummary(getString(R.string.pref_backup_account_key), NetworkUtils.accountDisplayName(getContext()));
                    Toast.makeText(getContext(), R.string.signin_success, Toast.LENGTH_LONG).show();
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    e.printStackTrace();
                    Toast.makeText(getContext(), R.string.error_signin_failed, Toast.LENGTH_LONG).show();
                    PrefercenceUtils.setBackupAccount(getContext(),true);
                    setPreferenceSummary(getString(R.string.pref_backup_account_key), NetworkUtils.accountDisplayName(getContext()));
                }
                return;
            }
            PrefercenceUtils.setBackupAccount(getContext(),true);
            setPreferenceSummary(getString(R.string.pref_backup_account_key), NetworkUtils.accountDisplayName(getContext()));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Preference preference = findPreference(getString(R.string.pref_backup_sync_key));
                    setChecked(preference, false);
                }
                break;
            }
        }
    }

    //Signout of Google account and Firebase
    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        GoogleSignInClient sClient=buildGoogleSignInClient();

        if(sClient!=null) {
            sClient.signOut().addOnCompleteListener(task -> {
                setPreferenceSummary(getString(R.string.pref_backup_account_key), "");
                PrefercenceUtils.setBackupAccount(getContext(), false);
                PrefercenceUtils.setBackupRemote(getContext(), false);
            });
        }
    }
}