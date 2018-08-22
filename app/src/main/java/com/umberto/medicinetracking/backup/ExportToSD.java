package com.umberto.medicinetracking.backup;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.AppDatabase;
import com.umberto.medicinetracking.utils.ImageUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

//Export all file to SD card if is installed
public class ExportToSD  extends AsyncTask<Void, Integer,String[]> {
    private final Context mContext;
    private final OnUploadProgress onProgress;
    public ExportToSD(Context context,OnUploadProgress onProgress){
        mContext=context;
        this.onProgress=onProgress;
    }

    @Override
    protected String[] doInBackground(Void... voids) {
        //If not have permission finish export.
        if(ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            return new String[]{mContext.getString(R.string.error_permission),mContext.getString(R.string.error_permission)};
        }
        if(!SdIsPresent()){
            return new String[]{mContext.getString(R.string.error_sd_not_exist),mContext.getString(R.string.error_sd_not_exist)};
        }
        AppDatabase.closeDb(mContext);
        File[] files = ImageUtils.getListImage(mContext);
        File[] filesSd = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/MedicineTracking");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
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
                        return new String[]{mContext.getString(R.string.error_file_copy),mContext.getString(R.string.error_file_copy)};
                    }
                }
            }
            try {
                File db = mContext.getDatabasePath(AppDatabase.DATABASE_NAME);
                exportFile(db);
            } catch (IOException e) {
                e.printStackTrace();
                return new String[]{mContext.getString(R.string.error_copy_db),mContext.getString(R.string.error_copy_db)};
            }
        return null;
    }

    @Override
    protected void onPostExecute(String[] messages) {
        super.onPostExecute(messages);
        if(onProgress!=null) {
            if (messages != null) {
                onProgress.onFinishUpload(false, messages[0], messages[1]);
            } else {
                onProgress.onFinishUpload(true, "", "");
            }
        }
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

    private static boolean SdIsPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
}
