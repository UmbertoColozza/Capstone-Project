package com.umberto.medicinetracking.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import com.squareup.picasso.Picasso;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.MedicineViewModel;
import com.umberto.medicinetracking.database.MedicineViewModelFactory;
import com.umberto.medicinetracking.database.Repository;
import com.umberto.medicinetracking.fragment.DescriptionFragment;
import com.umberto.medicinetracking.database.Medicine;
import com.umberto.medicinetracking.utils.ImageUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DescriptionActivity extends AppCompatActivity implements DescriptionFragment.OnItemDescriptionClickListener {
    public static final String MEDICINE_KEY_ID="medicine_item";
    private Medicine mMedicine;
    private int mMedicineId;
    private Repository mRepository;

    @BindView(R.id.photo) ImageView mPhoto;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        ButterKnife.bind(this);

        int mMutedColor = getResources().getColor(R.color.colorPrimary);
        setupActionBar();
        mCollapsingToolbar.setContentScrimColor(mMutedColor);
        mCollapsingToolbar.setStatusBarScrimColor(mMutedColor);
        mRepository=new Repository(this);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mMedicineId=savedInstanceState.getInt(MEDICINE_KEY_ID);
        } else {
            Bundle bundle = getIntent().getExtras();
            if(bundle!=null) {
                mMedicineId = bundle.getInt(MEDICINE_KEY_ID, 0);
            }
            setupDescriptionFragment();
        }

        setupViewModel();
    }

    private void setContentToolbar(){
        if(mMedicine!=null) {
            mToolbar.setTitle(mMedicine.getTitle());
            mCollapsingToolbar.setTitle(mMedicine.getTitle());
            mPhoto.setContentDescription(mMedicine.getTitle());
            if (!TextUtils.isEmpty(mMedicine.getFileName())) {
                Picasso.with(this)
                        .load(ImageUtils.getFile(this, mMedicine.getFileName()))
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(mPhoto);
            } else {
                mPhoto.setImageResource(R.drawable.placeholder);
            }

            mPhoto.setContentDescription(mMedicine.getTitle());
        }
    }

    private void setupViewModel(){
        MedicineViewModelFactory viewModelFactory = new MedicineViewModelFactory(mRepository, mMedicineId);
        final MedicineViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(MedicineViewModel.class);
        viewModel.getMedicine().observe(this, medicine -> {
            if(medicine==null){
                NavUtils.navigateUpFromSameTask(DescriptionActivity.this);
                return;
            }
            mMedicine = medicine;

            setContentToolbar();
        });
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

    /**
     * Set up the android.app.ActionBar, if the API is available.
     */
    private void setupDescriptionFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        DescriptionFragment descriptionFragment=new DescriptionFragment();
        descriptionFragment.setData(mMedicineId);
        transaction.add(R.id.frame_description_layout, descriptionFragment);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent intent=new Intent(this,EditActivity.class);
            intent.putExtra(EditActivity.KEY_MEDICINE_EDIT_ID,mMedicine.getId());
            intent.putExtra(EditActivity.KEY_MEDICINE_EDIT_TITLE,mMedicine.getTitle());
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.getInt(MEDICINE_KEY_ID, mMedicineId);
    }

    //On photo click
    @Override
    public void onItemDescriptionSelected(int position) {
        Intent intent=new Intent(this,PhotoGalleryActivity.class);
        intent.putExtra(PhotoGalleryActivity.KEY_PHOTO_MEDICINE_ID,mMedicine.getId());
        intent.putExtra(PhotoGalleryActivity.KEY_PHOTO_POSITION,position);
        intent.putExtra(PhotoGalleryActivity.KEY_TITLE,mMedicine.getTitle());
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
