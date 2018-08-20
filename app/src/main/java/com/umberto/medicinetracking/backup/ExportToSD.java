package com.umberto.medicinetracking.backup;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.umberto.medicinetracking.database.AppDatabase;
import com.umberto.medicinetracking.utils.ImageUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

//Export all file to SD card if is installed
public class ExportToSD  extends AsyncTask<Void, Integer,Void> {
    private Context mContext;
    private UploadTask.OnUploadProgress onProgress;
    public ExportToSD(Context context,UploadTask.OnUploadProgress onProgress){
        mContext=context;
        this.onProgress=onProgress;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        //If not have permission finish export.
        if(ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }
        if(!SdIsPresent()){
            return null;
        }
        AppDatabase.closeDb(mContext);
        File[] files = ImageUtils.getListImage(mContext);
        File[] filesSd = null;
        boolean existDir = true;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/MedicineTracking");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
            existDir = false;
        } else {
            filesSd = storageDir.listFiles();
        }

        //Sync file, chek files deleted
        if (filesSd != null && filesSd.length > 0) {
            for (File fileSd : filesSd) {
                boolean found = false;
                for (File file : files) {
                    if (file.getName().equals(fileSd.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    fileSd.delete();
                }
            }
        }

            for (File file : files) {
                boolean found = false;
                if (filesSd != null && filesSd.length > 0) {
                    for (File fileSd : filesSd) {
                        if (file.getName().equals(fileSd.getName())) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    try {
                        exportFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                File db = mContext.getDatabasePath(AppDatabase.DATABASE_NAME);
                exportFile(db);
            } catch (IOException e) {
                e.printStackTrace();
            }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        onProgress.onFinishUpload();
    }

    private void exportFile(File src) throws IOException {
        File dst = new File(Environment.getExternalStorageDirectory() + "/MedicineTracking", src.getName());
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    public static boolean SdIsPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
}
