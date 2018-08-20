package com.umberto.medicinetracking.utils;

import android.content.Context;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveResourceClient;

public class NetworkUtils {
    //Get if user is signed
    public static boolean isSignedIn(Context context) {
        return GoogleSignIn.getLastSignedInAccount(context) != null;
    }
    //Get display name of user account
    public static String accountDisplayName(Context context) {
        GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(context);
        if(gsa!=null){
            return gsa.getDisplayName();
        }
        return "";
    }

    //Get Google account
    public static GoogleSignInAccount googleSigninAccount(Context context) {
        GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(context);
        if(gsa!=null){
            return gsa;
        }
        return null;
    }

    //Get Goole signin for Google Drive APPFOLDER authorization
    public static GoogleSignInClient googleSignInClient(Context context) {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(context, signInOptions);
    }

    //Get Google Resource Client
    public static DriveResourceClient getResourceClient(Context context,GoogleSignInAccount gSignIn){
        return Drive.getDriveResourceClient(context, gSignIn);
    }
}
