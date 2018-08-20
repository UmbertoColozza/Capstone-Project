package com.umberto.medicinetracking.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.backup.DownloadTask;
import com.umberto.medicinetracking.backup.UploadTask;
import com.umberto.medicinetracking.backup.ExportToSD;
import com.umberto.medicinetracking.backup.ImportFromSD;
import com.umberto.medicinetracking.utils.PrefercenceUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity implements UploadTask.OnUploadProgress, DownloadTask.OnDownloadProgress {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.layout_progress)
    LinearLayout mLayoutProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        //getDatabasePath()
        setupActionBar();
    }

    //Export data if Button export is clicked
    protected void exportClick(View view) {
        mLayoutProgress.setVisibility(View.VISIBLE);
        if (PrefercenceUtils.getBackupRemote(this)) {
            UploadTask uploadTask = new UploadTask(this, this);
            uploadTask.execute();
        } else {
            ExportToSD export = new ExportToSD(this,this);
            export.execute();
        }
    }

    //Import data if Button import is clicked
    protected void importClick(View view) {
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

    @Override
    public void onFinishUpload() {
        mLayoutProgress.setVisibility(View.GONE);
    }

    @Override
    public void onFinishDownload() {
        mLayoutProgress.setVisibility(View.GONE);
    }
}
