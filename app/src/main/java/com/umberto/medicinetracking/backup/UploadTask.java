package com.umberto.medicinetracking.backup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.AppDatabase;
import com.umberto.medicinetracking.utils.ImageUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

//Export all file to Google Drive
public class UploadTask extends AsyncTask<Void, Integer,Void>{
    private final Context mContext;
    private DriveResourceClient mResourceClient;
    private File[] mFiles;
    private int mFilePosition;
    private MetadataBuffer mMetadata;
    private int mFileDelIndex;

    private final OnUploadProgress onProgress;
    //private DriveClient mDriveClient;
    public UploadTask(Context contex, OnUploadProgress onProgress){
        mContext=contex;
        getResourceClient();
        this.onProgress=onProgress;
    }

    private void getResourceClient(){
        GoogleSignInAccount mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(mContext);
        if(mGoogleSignInAccount!=null){
            mResourceClient = Drive.getDriveResourceClient(mContext, mGoogleSignInAccount);
        }
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(mContext, signInOptions);
    }

    private void uploadFile(){
        if(mFilePosition>=mFiles.length){
            uploadDb();
            return;
        }
        AppDatabase.closeDb(mContext);
        boolean found=false;
        for(Metadata data : mMetadata){
            if(data.getTitle().equals(mFiles[mFilePosition].getName())){
                found=true;
                break;
            }
        }
        if(!found) {

            final Task<DriveFolder> appFolderTask = mResourceClient.getAppFolder();
            final Task<DriveContents> createContentsTask = mResourceClient.createContents();
            Tasks.whenAll(appFolderTask, createContentsTask)
                    .continueWithTask(task -> {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        String mimeType = getMimeType(mFiles[mFilePosition].getName());
                        if (mimeType.equals("image/jpeg")) {
                            //------ THIS IS AN EXAMPLE FOR PICTURE ------
                            ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                            Bitmap image = BitmapFactory.decodeFile(mFiles[mFilePosition].getAbsolutePath());
                            image.compress(Bitmap.CompressFormat.JPEG, 100, bitmapStream);
                            try {
                                outputStream.write(bitmapStream.toByteArray());
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            try {
                                FileInputStream fileInputStream = new FileInputStream(mFiles[mFilePosition]);
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(mFiles[mFilePosition].getName())
                                .setMimeType(mimeType)
                                .setStarred(true)
                                .build();

                        return mResourceClient.createFile(parent, changeSet, contents);
                    }).addOnSuccessListener(driveFile -> {
                        mFilePosition++;
                        uploadFile();
                    })
                    .addOnFailureListener(e -> {
                        mFilePosition++;
                        uploadFile();
                    }); //Wait to finish thread
        } else {
            mFilePosition++;
            uploadFile();
        }
    }

    private void uploadDb(){
        File fileDb=mContext.getDatabasePath(AppDatabase.DATABASE_NAME);
        if(fileDb==null){
            finishUpload(false,mContext.getString(R.string.error_copy_db),mContext.getString(R.string.error_copy_db));
            return;
        }
        final Task<DriveFolder> appFolderTask = mResourceClient.getAppFolder();
        final Task<DriveContents> createContentsTask = mResourceClient.createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(task -> {
                    DriveFolder parent = appFolderTask.getResult();
                    DriveContents contents = createContentsTask.getResult();
                    OutputStream outputStream = contents.getOutputStream();
                    String mimeType="application/x-sqlite3";
                    try {
                        FileInputStream fileInputStream = new FileInputStream(fileDb);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(fileDb.getName())
                            .setMimeType(mimeType)
                            .setStarred(true)
                            .build();

                    return mResourceClient.createFile(parent, changeSet, contents);
                }).addOnSuccessListener(driveFile -> finishUpload(true,"",""))
                .addOnFailureListener(e -> finishUpload(false,mContext.getString(R.string.error_copy_db),e.getMessage())); //Wait to finish thread
    }

    private void deleteFile(){
        if(mFileDelIndex>=mMetadata.getCount()){
            uploadFile();
            return;
        }
        boolean found=false;
        for(File file : mFiles){
            if(mMetadata.get(mFileDelIndex).getTitle().equals(AppDatabase.DATABASE_NAME)){
                break;
            } else if(mMetadata.get(mFileDelIndex).getTitle().equals(file.getName())){
                found = true;
                break;
            }
        }
        if(!found){
            mResourceClient.delete(mMetadata.get(mFileDelIndex).getDriveId().asDriveFile())
                    .addOnSuccessListener(aVoid -> {
                        mFileDelIndex++;
                        deleteFile();
                    }).addOnFailureListener(e -> {
                        mFileDelIndex++;
                        deleteFile();
                    });
        } else {
            mFileDelIndex++;
            deleteFile();
        }
    }
    private void finishUpload(boolean success,String userMessage, String errorMessage){
        if(onProgress!=null){
            onProgress.onFinishUpload(success,userMessage,errorMessage);
        }
    }
    @Override
    protected Void doInBackground(Void... voids) {
        boolean success=true;
        mFiles = ImageUtils.getListImage(mContext);

        if(mFiles==null || mFiles.length==0){
            finishUpload(true,"","");
            return null;
        }

        if(mResourceClient==null){
            finishUpload(false,mContext.getString(R.string.error_signin_failed),mContext.getString(R.string.error_signin_failed));
            return null;
        }
        Query query = new Query.Builder()
                .addFilter(Filters.or(Filters.eq(SearchableField.MIME_TYPE, "image/jpeg"),Filters.eq(SearchableField.MIME_TYPE, "application/x-sqlite3")))
                .build();
        // [END drive_android_query_title]
        mResourceClient
                        .query(query)
                        .addOnSuccessListener(metadata -> {
                            mMetadata=metadata;
                            mFilePosition=0;
                            mFileDelIndex=0;
                            //Remove file old
                            deleteFile();
                        })
                        .addOnFailureListener(e -> {
                            e.printStackTrace();
                            finishUpload(false,mContext.getString(R.string.error_query_drive), e.toString());
                        });
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    // url = file path or whatever suitable URL you want.
    private static String getMimeType(String filePath) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
