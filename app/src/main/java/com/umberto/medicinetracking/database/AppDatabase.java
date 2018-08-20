package com.umberto.medicinetracking.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {Medicine.class, Photo.class}, version = 1, exportSchema = false)
@TypeConverters({})
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    public static final String DATABASE_NAME = "medicinetracking";
    private static AppDatabase sInstance;
    private static Context mContext;

    //Create instance of database if is null
    public static AppDatabase getInstance(Context context) {
        mContext = context;
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }

    //Close database
    public static void closeDb(Context context){
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        if(appDatabase.isOpen()) {
            appDatabase.close();
            sInstance=null;
        }
    }
    public abstract TaskDao taskDao();

}