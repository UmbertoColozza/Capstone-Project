package com.umberto.medicinetracking.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.backup.DownloadTask;
import com.umberto.medicinetracking.backup.OnUploadProgress;
import com.umberto.medicinetracking.backup.UploadTask;
import com.umberto.medicinetracking.backup.ExportToSD;
import com.umberto.medicinetracking.backup.ImportFromSD;
import com.umberto.medicinetracking.utils.PrefercenceUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity implements OnUploadProgress, DownloadTask.OnDownloadProgress {
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.layout_progress)
    LinearLayout mLayoutProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setupActionBar();
        setTitle(getString(R.string.title_activity_settings));
    }

    //Export data if Button export is clicked
    public void exportClick(View view) {
        if(PrefercenceUtils.getBackupRemote(this)){
            mLayoutProgress.setVisibility(View.VISIBLE);
            UploadTask uploadTask = new UploadTask(this, this);
            uploadTask.execute();
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // If you do not have permission, request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                exportToSD();
            }
        }
    }

    private void exportToSD() {
        ExportToSD export = new ExportToSD(this, this);
        export.execute();
    }
    //Import data if Button import is clicked
    public void importClick(View view) {
        mLayoutProgress.setVisibility(View.VISIBLE);
        if (PrefercenceUtils.getBackupRemote(this)) {
            DownloadTask downloadTask = new DownloadTask(this, this);
            downloadTask.execute();
        } else {
            ImportFromSD importFromSD = new ImportFromSD(this,this);
            importFromSD.execute();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showMessage(String message){
        Toast.makeText(getApplicationContext(), message , Toast.LENGTH_LONG).show();
        mLayoutProgress.setVisibility(View.GONE);
    }
    @Override
    public void onFinishUpload(boolean success,String userMessage,String errorMessage) {
        //Can't toast on a thread that has not called Looper.prepare()
        if(success){
            showMessage(getString(R.string.backup_succesfully));
        }
        else {
            showMessage(userMessage);
        }
    }

    @Override
    public void onFinishDownload(boolean success,String userMessage,String errorMessage) {if(success){
        Toast.makeText(this,R.string.download_succesfully,Toast.LENGTH_LONG).show();
    }
    else {
        Toast.makeText(this,userMessage,Toast.LENGTH_LONG).show();
    }
        mLayoutProgress.setVisibility(View.GONE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportToSD();
                }
                break;
            }
        }
    }
}
