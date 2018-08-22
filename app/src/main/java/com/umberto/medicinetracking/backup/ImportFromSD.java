package com.umberto.medicinetracking.backup;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.AppDatabase;
import com.umberto.medicinetracking.utils.ImageUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

//Import all file from SD card
public class ImportFromSD extends AsyncTask<Void, Integer,String[]> {
    private final Context mContext;
    private final DownloadTask.OnDownloadProgress downloadProgress;
    public ImportFromSD(Context context, DownloadTask.OnDownloadProgress onDowload){
        mContext=context;
        downloadProgress=onDowload;
    }

    @Override
    protected String[] doInBackground(Void... voids) {
        //If SD not installed finish import
        if(!SdIsPresent()){
            String[] messages = {mContext.getString(R.string.error_sd_not_exist), mContext.getString(R.string.error_sd_not_exist)};
            return messages;
        }
        File[] files= ImageUtils.getListImage(mContext);
        File[] filesSd;
        boolean existDir=true;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/MedicineTracking");
        boolean success = true;

        //If external storage directory not exist finish import
        if (!storageDir.exists()) {
            return null;
        }
        //Close database
        AppDatabase.closeDb(mContext);

        filesSd = storageDir.listFiles();
        //Delete all local file not in sd card
        if(filesSd!=null && filesSd.length>0) {
            for (File file : files) {
                boolean found = false;
                for (File fileSd : filesSd) {
                    if (file.getName().equals(fileSd.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    file.delete();
                }
            }

            //If not exist file import from SD Card
            for (File fileSd : filesSd) {
                if (fileSd.getName().equals(AppDatabase.DATABASE_NAME) || fileSd.getName().equals(AppDatabase.DATABASE_NAME + "-shm") || fileSd.getName().equals(AppDatabase.DATABASE_NAME + "-wal")) {
                    try {
                        importFile(fileSd, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        String[] messages = {mContext.getString(R.string.error_file_copy), fileSd.getAbsolutePath()+"\n"+e.getMessage()};
                        return messages;
                    }
                    break;
                } else {
                    try {
                        importFile(fileSd, false);
                    } catch (IOException e) {
                        e.printStackTrace();
                        String[] messages = {mContext.getString(R.string.error_file_copy), fileSd.getAbsolutePath()+"\n"+e.getMessage()};
                        return messages;
                    }
                }
            }
        }
        return null;
    }

    private void importFile(File src, boolean isDb) throws IOException {
        File dst;
        if(isDb) {
            dst = new File(mContext.getDatabasePath(AppDatabase.DATABASE_NAME).getParent(), src.getName());
        } else {
            dst = new File(mContext.getFilesDir().getAbsolutePath(), src.getName());
        }
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

    @Override
    protected void onPostExecute(String[] messages) {
        super.onPostExecute(messages);
        if(messages==null) {
            downloadProgress.onFinishDownload(true,"","");
        } else {
            downloadProgress.onFinishDownload(false,messages[0],messages[1]);
        }
    }
}
