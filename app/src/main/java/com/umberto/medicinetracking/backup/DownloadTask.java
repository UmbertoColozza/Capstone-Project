package com.umberto.medicinetracking.backup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.AppDatabase;
import com.umberto.medicinetracking.utils.ImageUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

//Download all file from Google Drive to Local
public class DownloadTask extends AsyncTask<Void, Integer,Void>{
    private final Context mContext;
    private DriveResourceClient mResourceClient;
    private File[] mFiles;
    private int mFilePosition;
    private MetadataBuffer mMetadata;
    private final OnDownloadProgress onDowload;

    public DownloadTask(Context contex,OnDownloadProgress onDowload){
        mContext=contex;
        this.onDowload=onDowload;
        getResourceClient();
    }

    //Get Google resource client
    private void getResourceClient(){
        GoogleSignInAccount mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(mContext);
        if(mGoogleSignInAccount!=null){
            mResourceClient = Drive.getDriveResourceClient(mContext, mGoogleSignInAccount);
        }
    }

    private void downloadFile(){
        //if the index is greater than the metadata number, the download ends and jump to deleteLocalFile()
        if(mFilePosition>=mMetadata.getCount()){
            deleteLocalFile();
            return;
        }
        Metadata metadata=mMetadata.get(mFilePosition);
        DriveFile file=mMetadata.get(mFilePosition).getDriveId().asDriveFile();
        if(mMetadata.get(mFilePosition).getTitle().equals(AppDatabase.DATABASE_NAME)){
            createDb(file);
            return;
        }
        // [START drive_android_open_file]
        Task<DriveContents> openFileTask =
                mResourceClient.openFile(file, DriveFile.MODE_READ_ONLY);
        // [END drive_android_open_file]

        // Check to see if the file already exists, if not exist download file else jump to next file
        boolean found=false;
        for(File localFile : mFiles){
            if(metadata.getTitle().equals(mContext.getDatabasePath(AppDatabase.DATABASE_NAME).getName())){
                break;
            } else if(metadata.getTitle().equals(localFile.getName())){
                found=true;
                break;
            }
        }
        if(!found) {
            openFileTask
                    .continueWithTask(task -> {
                        DriveContents contents = task.getResult();
                        InputStream inputStream = contents.getInputStream();
                        // [START save bitmap from stream]
                        Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                        ImageUtils.saveImage(mContext, bmp, metadata.getTitle());
                        inputStream.close();
                        // [END save bitmap from stream]
                        // [START drive_android_discard_contents]
                        return mResourceClient.discardContents(contents).addOnSuccessListener(aVoid -> {
                            mFilePosition++;
                            downloadFile();
                        });
                        // [END drive_android_discard_contents]
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        // Move to the next file
                        mFilePosition++;
                        downloadFile();
                    });
        } else {
            mFilePosition++;
            downloadFile();
        }
    }

    //Delete all files that are not online
    private void deleteLocalFile(){
        for(File file : mFiles){
            boolean found=false;
            for(Metadata metadata : mMetadata){
                if(metadata.getTitle().equals(file.getName())){
                    found=true;
                    break;
                }
            }
            if(!found){
                file.delete();
            }
        }
        //Finish download
        finishDownload(true,"","");
    }

    private void createDb(DriveFile file){
        // [START drive_android_open_file]
        Task<DriveContents> openFileTask =
                mResourceClient.openFile(file, DriveFile.MODE_READ_ONLY);
        // [END drive_android_open_file]
        // [START drive_android_read_contents]
        openFileTask
                .continueWithTask(task -> {
                    DriveContents contents = task.getResult();
                    InputStream inputStream=contents.getInputStream();

                    // Process contents...
                    // [START_EXCLUDE]
                    // [START save db from stream]
                    String outFileName = mContext.getDatabasePath(AppDatabase.DATABASE_NAME).getAbsolutePath();
                    OutputStream mOutput = new FileOutputStream(outFileName);

                    byte[] mBuffer = new byte[1024];
                    int mLength;
                    while ((mLength = inputStream.read(mBuffer))>0)
                    {
                        mOutput.write(mBuffer, 0, mLength);
                    }
                    mOutput.flush();
                    mOutput.close();
                    inputStream.close();
                    // [END save db from stream]
                    // [END_EXCLUDE]
                    // [START drive_android_discard_contents]
                    return mResourceClient.discardContents(contents).addOnSuccessListener(aVoid -> {
                        mFilePosition++;
                        downloadFile();
                    });
                    // [END drive_android_discard_contents]
                })
                .addOnFailureListener(e -> {
                    mFilePosition++;
                    downloadFile();
                });


    }
    @Override
    protected Void doInBackground(Void... voids) {
        mFiles = ImageUtils.getListImage(mContext);
        //Close database
        AppDatabase.closeDb(mContext);

        //Get list of online file with mime type "image/jpeg" or "application/x-sqlite3"
        Query query = new Query.Builder()
                .addFilter(Filters.or(Filters.eq(SearchableField.MIME_TYPE, "image/jpeg"),Filters.eq(SearchableField.MIME_TYPE, "application/x-sqlite3")))
                .build();
                mResourceClient
                        .query(query)
                        .addOnSuccessListener(metadata -> {
                            //If file online exist start download else finish download
                            if(metadata.getCount()>0){
                                mMetadata=metadata;
                                mFilePosition=0;
                                downloadFile();
                            } else {
                                finishDownload(true,"","");
                            }
                        })
                        .addOnFailureListener(e -> {
                            //Finish download
                            finishDownload(false,mContext.getString(R.string.error_query_drive),e.getMessage());
                        });
        return null;
    }

    private void finishDownload(boolean success,String userMessage,String errorMessage){
        if(onDowload!=null){
            onDowload.onFinishDownload(success,userMessage,errorMessage);
        }
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
