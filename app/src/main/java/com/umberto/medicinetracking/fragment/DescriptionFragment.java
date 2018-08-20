package com.umberto.medicinetracking.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.Medicine;
import com.umberto.medicinetracking.database.MedicineViewModel;
import com.umberto.medicinetracking.database.MedicineViewModelFactory;
import com.umberto.medicinetracking.database.Photo;
import com.umberto.medicinetracking.database.PhotoViewModel;
import com.umberto.medicinetracking.database.PhotoViewModelFactory;
import com.umberto.medicinetracking.database.Repository;
import com.umberto.medicinetracking.utils.ImageUtils;
import com.umberto.medicinetracking.utils.MedicineUtils;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DescriptionFragment extends Fragment {
    private static final String MEDICINE="medicine";
    private Medicine mMedicine;
    private int mMedicineId;
    @BindView(R.id.tv_description_expire_date) TextView mTVExpireData;
    @BindView(R.id.tv_description_quantity) TextView mTVQuantity;
    @BindView(R.id.tv_description_text) TextView mTVDescription;
    @BindView(R.id.rvDescriptionImage) RecyclerView mRVDescriptionImage;
    DescriptionPhotoListAdapter descriptionPhotoListAdapter;
    private Repository mRepository;

    // OnItemClickListener interface, calls a method in the host activity named onItemSelected (open photo gallery)
    public interface OnItemDescriptionClickListener {
        void onItemDescriptionSelected(int position);
    }

    // Empty constructor
    public DescriptionFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_description, container, false);
        ButterKnife.bind(this, rootView);

        if(savedInstanceState != null) {
            mMedicineId = savedInstanceState.getInt(MEDICINE);
        }


        GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),getResources().getBoolean(R.bool.isTablet) ? 6 : 3);
        mRVDescriptionImage.setLayoutManager(gridLayoutManager);
        descriptionPhotoListAdapter=new DescriptionPhotoListAdapter(getContext(),(OnItemDescriptionClickListener) getContext());
        mRVDescriptionImage.setAdapter(descriptionPhotoListAdapter);
        //Show all item
        mRVDescriptionImage.setNestedScrollingEnabled(false);

        mRepository=new Repository(getContext());

        if(mMedicineId!=-1) {
            setupViewModelMedicine();
            setupViewModelPhoto();
        }
        return rootView;
    }
    private void setContent(){
        if(mMedicine!=null) {
            mTVDescription.setText(mMedicine.getDescription());
            mTVQuantity.setText(TextUtils.concat(getString(R.string.quantity),MedicineUtils.intToString(mMedicine.getQuantity())));
            mTVExpireData.setText(TextUtils.concat(getString(R.string.expire_date),MedicineUtils.dateToString(mMedicine.getExpireData())));
        }
    }

    //Setup MedicineViewModel, select medicine by id
    private void setupViewModelMedicine(){
        MedicineViewModelFactory viewModelFactory = new MedicineViewModelFactory(mRepository, mMedicineId);
        final MedicineViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(MedicineViewModel.class);
        viewModel.getMedicine().observe(this, new Observer<Medicine>() {
            @Override
            public void onChanged(@Nullable Medicine medicine) {
                mMedicine = medicine;
                setContent();
            }
        });
    }
    //Setup PhotoViewModel, select all photo by id of medicine
    private void setupViewModelPhoto(){
            PhotoViewModelFactory photoViewModelFactory=new PhotoViewModelFactory(mRepository,mMedicineId);
            final PhotoViewModel photoViewModel = ViewModelProviders.of(this, photoViewModelFactory).get(PhotoViewModel.class);
            photoViewModel.getPhotoList().observe(this, new Observer<List<Photo>>() {
                @Override
                public void onChanged(@Nullable List<Photo> photo) {
                    if(photo!=null) {
                        descriptionPhotoListAdapter.setPhoto(photo);
                    }
                }
            });
    }

    //Set medicine id from parent Activity
    public void setData(int medicineId){
        mMedicineId=medicineId;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MEDICINE, mMedicineId);
    }
}
