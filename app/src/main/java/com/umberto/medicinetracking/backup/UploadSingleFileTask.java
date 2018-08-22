package com.umberto.medicinetracking.backup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.webkit.MimeTypeMap;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.umberto.medicinetracking.R;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

//Export single file to Google Drive. This is used when preference Change only is selected
public class UploadSingleFileTask extends AsyncTask<Void, Integer,Void>{
    private final Context mContext;
    private DriveResourceClient mResourceClient;
    private final File mFile;
    private final String mimeType;
    private MetadataBuffer mMetadata;
    private final OnUploadProgress onUploadProgress;
    private final boolean overWrite;

    public UploadSingleFileTask(Context contex, File file, boolean overWrite,OnUploadProgress onUploadProgress){
        mContext=contex;
        mFile=file;
        mimeType=getMimeType(file.getName());
        getResourceClient();
        this.onUploadProgress=onUploadProgress;
        this.overWrite=overWrite;
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

    private void createFile(){
        final Task<DriveFolder> appFolderTask = mResourceClient.getAppFolder();
        final Task<DriveContents> createContentsTask = mResourceClient.createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(task -> {
                    DriveFolder parent = appFolderTask.getResult();
                    DriveContents contents = createContentsTask.getResult();
                    OutputStream outputStream = contents.getOutputStream();

                    if(mimeType.equals("image/jpeg")) {
                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        Bitmap image = BitmapFactory.decodeFile(mFile.getAbsolutePath());
                        image.compress(Bitmap.CompressFormat.JPEG, 100, bitmapStream);
                        try {
                            outputStream.write(bitmapStream.toByteArray());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        try {
                            FileInputStream fileInputStream = new FileInputStream(mFile);
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
                            .setTitle(mFile.getName())
                            .setMimeType(mimeType)
                            .setStarred(true)
                            .build();

                    return mResourceClient.createFile(parent, changeSet, contents);
                }).addOnSuccessListener(driveFile -> onLoadFinish(true,"",""))
                .addOnFailureListener(e -> onLoadFinish(false,mContext.getString(R.string.error_file_copy),e.getMessage()));
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if(mFile==null){
            return null;
        }
        boolean success=true;
        //Check if exist online file. If exist and overwrite is false finish upload
        Query query = new Query.Builder()
                .addFilter(Filters.or(Filters.eq(SearchableField.TITLE, mFile.getName())))
                .build();
        // [END drive_android_query_title]
        Task<MetadataBuffer> queryTask =
                mResourceClient
                        .query(query)
                        .addOnSuccessListener(metadata -> {
                            mMetadata=metadata;
                                if(metadata.getCount()>0){
                                        if(overWrite) {
                                            mResourceClient.delete(metadata.get(0).getDriveId().asDriveFile()).addOnSuccessListener(aVoid -> createFile());
                                        } else {
                                            onLoadFinish(false,mContext.getString(R.string.error_file_exist),mContext.getString(R.string.error_file_exist));
                                        }
                                }
                                else {
                                    createFile();
                                }
                            })
                        .addOnFailureListener(e -> onLoadFinish(false,mContext.getString(R.string.error_query_drive),mContext.getString(R.string.error_query_drive)));
        return null;
    }

    private void onLoadFinish(boolean success,String userMessage,String errorMessage){
        if(onUploadProgress!=null) {
            onUploadProgress.onFinishUpload(success,userMessage,errorMessage);
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
