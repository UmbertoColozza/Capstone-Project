package com.umberto.medicinetracking.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.Medicine;
import java.lang.reflect.Type;
import java.util.List;

public class PrefercenceUtils {
    public static final String WIDGET_MEDICINE_KEY="widget_medicine_key";
    public static final String MEDICINE_CHENGED_KEY="medicine_changed";

    //Get if Remote Backup is enabled or disabled
    public static boolean getBackupRemote(Context context){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.pref_backup_remote_key), false);
    }

    //Set Remote Backup
    public static void setBackupRemote(Context context, boolean value){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.pref_backup_remote_key), value);
        editor.apply();
    }

    //Get if Backup is enabled or disabled
    public static boolean getBackup(Context context){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.pref_backup_sync_key), false);
    }

    //Set backup
    public static void setBackup(Context context, boolean value){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.pref_backup_sync_key), value);
        editor.apply();
    }

    //Get if the backup is to be started at each change
    public static boolean getWhenBackup(Context context){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.pref_backup_start_key), false);
    }

    //Set when backup
    public static void setWhenBackup(Context context, boolean value){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.pref_backup_start_key), value);
        editor.apply();
    }

    //Get the account display name
    public static boolean getBackupAccount(Context context){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.pref_backup_account_key), false);
    }

    //Set account display name
    public static void setBackupAccount(Context context, boolean value){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.pref_backup_account_key), value);
        editor.apply();
    }

    //Set if layout list of main activity is a grid
    public static boolean showGrid(Context context){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.pref_show_list_grid_key), false);
    }

    //Set if layout is a grid of main activity
    public static void saveArrayList(Context context, List<Medicine> list){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(WIDGET_MEDICINE_KEY, json);
        editor.apply();
    }

    //Set list of medicine showed in widget
    public static void setWidgetMedicine(Context context,List<Medicine> list){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(list!=null) {
            Gson gson = new Gson();
            String json = gson.toJson(list);
            editor.putString(WIDGET_MEDICINE_KEY, json);
        } else {
            editor.putString(WIDGET_MEDICINE_KEY, "");
        }
        editor.apply();
    }

    //Get list of medicine showed in widget
    public static List<Medicine> getWidgetMedicine(Context context){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(WIDGET_MEDICINE_KEY, null);
        Type type = new TypeToken<List<Medicine>>() {}.getType();
        return gson.fromJson(json, type);
    }

    //Set if there has been some change
    public static void setMedicineChanged(Context context,boolean value){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(MEDICINE_CHENGED_KEY, value);
        editor.apply();
    }

    //Get if there has been some change
    public static boolean getMedicineChanged(Context context){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(MEDICINE_CHENGED_KEY, false);
    }
}
