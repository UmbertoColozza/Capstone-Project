package com.umberto.medicinetracking.fragment;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.Photo;
import com.umberto.medicinetracking.database.PhotoViewModel;
import com.umberto.medicinetracking.database.PhotoViewModelFactory;
import com.umberto.medicinetracking.database.Repository;
import com.umberto.medicinetracking.utils.ImageUtils;
import java.io.File;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PhotoGalleryFragment extends Fragment {
    @BindView(R.id.vpphoto) ViewPager mVPPhoto;
    ImagePagerAdapter imagePagerAdapter;
    private int mMedicineId;
    private int mPosition;
    List<Photo> mListPhoto;
    private Repository mRepository;

    // Empty constructor
    public PhotoGalleryFragment(){
    }

    // Inflates the photo pager
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        ButterKnife.bind(this, rootView);

        mRepository = new Repository(getContext());
        setupListPhoto();
        return rootView;
    }

    @OnClick(R.id.image_button_photo_share)
    public void shareImage(){
        int currentPage=mVPPhoto.getCurrentItem();
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpg");
        final File photoFile = ImageUtils.getFile(getContext(), mListPhoto.get(currentPage).getFileName());

        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getContext().getApplicationContext(), ImageUtils.FILE_PROVIDER_AUTHORITY, photoFile));
        startActivity(Intent.createChooser(shareIntent, getString(R.string.title_share_intent)));
    }

    @OnClick(R.id.image_button_photo_delete)
    public void deleteImage(){
        final int currentPage=mVPPhoto.getCurrentItem();
        final Photo photo=mListPhoto.get(currentPage);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(getString(R.string.alert_confirm_title));
        builder.setMessage(getString(R.string.alert_confirm_message));

        builder.setPositiveButton(getString(R.string.alert_confirm_positive), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mRepository.deletePhoto(photo);
                File deleteFile=ImageUtils.getFile(getContext(), photo.getFileName());
                ImageUtils.deleteImageFile(getContext(), deleteFile.getAbsolutePath());
                //if is first photo update cover photo in medicine
                if(currentPage==0){
                    String nextFileName="";
                    if(mListPhoto.size()>1){
                        nextFileName= mListPhoto.get(currentPage+1).getFileName();
                        mRepository.updatePhotoMedicine(photo.getMedicineId(), nextFileName);
                    } else {
                        mRepository.updatePhotoMedicine(photo.getMedicineId(), "");
                    }
                    mRepository.updatePhotoMedicine(photo.getMedicineId(), nextFileName);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getString(R.string.alert_confirm_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    //Setup view model photo
    private void setupListPhoto(){
        if(mMedicineId!=-1) {
            PhotoViewModelFactory viewModelFactory = new PhotoViewModelFactory(mRepository, mMedicineId);
            final PhotoViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(PhotoViewModel.class);
            viewModel.getPhotoList().observe(this, new Observer<List<Photo>>() {
                @Override
                public void onChanged(@Nullable List<Photo> photos) {
                    mListPhoto=photos;
                    imagePagerAdapter = new ImagePagerAdapter(getActivity(),photos);
                    mVPPhoto.setAdapter(imagePagerAdapter);
                }
            });
        }
    }

    //Set medicine id and photo position clicked
    public void setData(int medicineId, int position){
        mMedicineId=medicineId;
        mPosition=position;
    }
}
