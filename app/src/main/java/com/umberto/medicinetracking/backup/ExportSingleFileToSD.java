package com.umberto.medicinetracking.backup;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.umberto.medicinetracking.database.AppDatabase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

//Export single file to SD card if installed. This is used when preference Change only is selected
public class ExportSingleFileToSD extends AsyncTask<Void, Integer,Void> {
    private Context mContext;
    private UploadTask.OnUploadProgress onProgress;
    private File file;
    private boolean overwrite;

    public ExportSingleFileToSD(Context context,File file,boolean overwrite, UploadTask.OnUploadProgress onProgress){
        mContext=context;
        this.onProgress=onProgress;
        this.file=file;
        this.overwrite=overwrite;
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
        if(file.getName().equals(mContext.getDatabasePath(AppDatabase.DATABASE_NAME))) {
            AppDatabase.closeDb(mContext);
        }
        File[] filesSd = null;
        boolean existDir = true;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/MedicineTracking");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if(success){
            File filedst=new File(storageDir.getAbsolutePath(),AppDatabase.DATABASE_NAME);
            if(filedst.exists() && !overwrite){
                return null;
            }
            try {
                File db = mContext.getDatabasePath(AppDatabase.DATABASE_NAME);
                exportFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
