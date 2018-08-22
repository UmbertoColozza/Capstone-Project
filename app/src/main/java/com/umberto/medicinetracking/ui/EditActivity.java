package com.umberto.medicinetracking.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.fragment.EditFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditActivity extends AppCompatActivity implements EditFragment.OnDeleteListener {
    public static final String KEY_MEDICINE_EDIT_ID="key_medicine_id";
    public static final String KEY_MEDICINE_EDIT_TITLE="key_medicine_title";
    @BindView(R.id.toolbar) Toolbar mToolbar;

    private String mMedicineTitle;
    private int mMedicineId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ButterKnife.bind(this);

        if(savedInstanceState!=null){
            mMedicineId=savedInstanceState.getInt(KEY_MEDICINE_EDIT_ID);
            mMedicineTitle=savedInstanceState.getString(KEY_MEDICINE_EDIT_TITLE);
        }else {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                if (extras.containsKey(KEY_MEDICINE_EDIT_ID)) {
                    mMedicineId = extras.getInt(KEY_MEDICINE_EDIT_ID, -1);
                }
                if (extras.containsKey(KEY_MEDICINE_EDIT_TITLE)) {
                    mMedicineTitle = extras.getString(KEY_MEDICINE_EDIT_TITLE);
                }
                setupEditFragment();
            }
        }

        setupActionBar();

        if(mMedicineId==-1){
            setTitle(getString(R.string.title_activity_edit));
        } else {
            setTitle(mMedicineTitle);
        }
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
     * Set up Editfragment .
     */
    private void setupEditFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        EditFragment editFragment=new EditFragment();
        editFragment.setData(mMedicineId);
        transaction.add(R.id.edit_frame_container, editFragment);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Go back activity if medicine is deleted
    @Override
    public void onDeleted() {
        NavUtils.navigateUpFromSameTask(this);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_MEDICINE_EDIT_TITLE, mMedicineTitle);
        outState.putInt(KEY_MEDICINE_EDIT_ID, mMedicineId);
    }
}
