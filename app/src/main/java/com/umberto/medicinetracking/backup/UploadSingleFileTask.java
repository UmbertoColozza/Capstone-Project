package com.umberto.medicinetracking.backup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

//Export single file to Google Drive. This is used when preference Change only is selected
public class UploadSingleFileTask extends AsyncTask<Void, Integer,Void>{
    private Context mContext;
    private DriveResourceClient mResourceClient;
    private GoogleSignInAccount mGoogleSignInAccount;
    private File mFile;
    private String mimeType;
    private MetadataBuffer mMetadata;
    private UploadTask.OnUploadProgress onUploadProgress;
    private boolean overWrite;

    public UploadSingleFileTask(Context contex, File file, boolean overWrite,UploadTask.OnUploadProgress onUploadProgress){
        mContext=contex;
        mFile=file;
        mimeType=getMimeType(file.getName());
        getResourceClient();
        this.onUploadProgress=onUploadProgress;
        this.overWrite=overWrite;
    }

    private void getResourceClient(){
        mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(mContext);
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

    public void createFile(){
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
                }).addOnSuccessListener(new OnSuccessListener<DriveFile>() {
                    @Override
                    public void onSuccess(DriveFile driveFile) {
                        onLoadFinish();
                    }
                })
                .addOnFailureListener(e -> {
                    onLoadFinish();
                });
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
                        .addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadata) {
                                mMetadata=metadata;
                                    if(metadata.getCount()>0){
                                            if(overWrite) {
                                                mResourceClient.delete(metadata.get(0).getDriveId().asDriveFile()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        createFile();
                                                    }
                                                });
                                            } else {
                                                onLoadFinish();
                                            }
                                    }
                                    else {
                                        createFile();
                                    }
                                }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                onLoadFinish();
                            }
                        });
        return null;
    }

    private void onLoadFinish(){
        if(onUploadProgress!=null) {
            onUploadProgress.onFinishUpload();
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
    public static String getMimeType(String filePath) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
