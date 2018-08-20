package com.umberto.medicinetracking.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import com.umberto.medicinetracking.database.AppDatabase;
import com.umberto.medicinetracking.backup.UploadSingleFileTask;
import com.umberto.medicinetracking.backup.UploadTask;
import com.umberto.medicinetracking.backup.ExportToSD;

public class UploadService extends IntentService implements UploadTask.OnUploadProgress{
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UploadService(String name) {
        super(name);
    }

    public UploadService(){
        super("UploadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context=getApplicationContext();
        switch (intent.getAction()){
            case "drive":
                UploadTask uploadTask = new UploadTask(context, this);
                uploadTask.execute();
                break;
            case "sdcard":
                ExportToSD exportToSD=new ExportToSD(context,this);
                exportToSD.execute();
                break;
            case "uploaddb":
                UploadSingleFileTask uploadSingleFileTask=new UploadSingleFileTask(context,context.getDatabasePath(AppDatabase.DATABASE_NAME),true,this);
                uploadSingleFileTask.execute();
                break;
                default:
                    this.stopSelf();
                    break;
        }
    }

    @Override
    public void onFinishUpload() {

        this.stopSelf();
    }
}
