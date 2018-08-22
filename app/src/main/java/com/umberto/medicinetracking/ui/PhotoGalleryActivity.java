package com.umberto.medicinetracking.ui;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.fragment.PhotoGalleryFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoGalleryActivity extends AppCompatActivity {
    public static final String KEY_PHOTO_MEDICINE_ID="key_medicine_id";
    public static final String KEY_PHOTO_POSITION="key_position";
    public static final String KEY_TITLE="key_title";
    private int mMedicineId;
    private int mPosition;
    private String mTitle;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);
        ButterKnife.bind(this);
        if(savedInstanceState!=null){
            mTitle=savedInstanceState.getString(KEY_TITLE);
        }else {
            Bundle extras=getIntent().getExtras();
            if (extras != null) {
                if (extras.containsKey(KEY_PHOTO_MEDICINE_ID)) {
                    mMedicineId = extras.getInt(KEY_PHOTO_MEDICINE_ID, -1);
                }
                if (extras.containsKey(KEY_PHOTO_POSITION)) {
                    mPosition = extras.getInt(KEY_PHOTO_POSITION, 0);
                }
                if (extras.containsKey(KEY_PHOTO_POSITION)) {
                    mTitle = extras.getString(KEY_PHOTO_POSITION);
                }
                setupPhotoGalleryFragment();
            }
        }
        if(mTitle==null){
            mTitle=getString(R.string.title_activity_gallery);
        }
        setTitle(mTitle);
        setupActionBar();

    }

    /**
     * Set up the android.app.ActionBar, if the API is available.
     */
    private void setupActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Set up PhotoGalleryFragment
     */
    private void setupPhotoGalleryFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        PhotoGalleryFragment photoFragment=new PhotoGalleryFragment();
        photoFragment.setData(mMedicineId);
        transaction.add(R.id.photo_gallery_frame_layout, photoFragment);
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TITLE, mTitle);
    }
}
