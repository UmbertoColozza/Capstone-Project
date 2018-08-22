package com.umberto.medicinetracking.utils;

import android.content.Context;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

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
}
