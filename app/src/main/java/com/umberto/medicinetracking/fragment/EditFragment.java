package com.umberto.medicinetracking.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.common.util.CollectionUtils;
import com.squareup.picasso.Picasso;
import com.umberto.medicinetracking.R;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.squareup.picasso.Target;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.umberto.medicinetracking.backup.ExportSingleFileToSD;
import com.umberto.medicinetracking.database.Medicine;
import com.umberto.medicinetracking.database.MedicineViewModel;
import com.umberto.medicinetracking.database.MedicineViewModelFactory;
import com.umberto.medicinetracking.database.Photo;
import com.umberto.medicinetracking.database.PhotoViewModel;
import com.umberto.medicinetracking.database.PhotoViewModelFactory;
import com.umberto.medicinetracking.database.Repository;
import com.umberto.medicinetracking.backup.UploadSingleFileTask;
import com.umberto.medicinetracking.utils.ImageUtils;
import com.umberto.medicinetracking.utils.MedicineUtils;
import com.umberto.medicinetracking.utils.PrefercenceUtils;

//Edit medicine and photo gallery. Photo gallery is visible after medicine inserted.
public class EditFragment extends Fragment implements EditPhotoListAdapter.OnDeleteImageListener, Repository.InsertCallback {
    private static final String MEDICINE_ID="medicine_id";
    private static final String EDIT_TITLE="title";
    private static final String EDIT_DESCRIPTION="description";
    private static final String EDIT_EXPIRE_DATA="expire_data";
    private static final String EDIT_WEB="edit_web";
    private static final String EDIT_QUANTITY="edit_quantity";
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private static final int REQUEST_CAMERA_PERMISSION = 3;
    private static final String FILE_PROVIDER_AUTHORITY = "com.umberto.medicinetracking.fileprovider";
    private static final int REQUEST_IMAGE_GET = 4;
    private String mTempPhotoPath;
    private Target targetBitmap;
    private OnDeleteListener deleteClickListener;
    private DatePickerDialog mDatePickerDialog;
    private SimpleDateFormat dateFormatter;

    private Medicine mMedicine;
    private int mMedicineId;
    private List<Photo> photoList;

    @BindView(R.id.edit_text_title) EditText mEditTitle;
    @BindView(R.id.edit_text_description) EditText mEditDescription;
    @BindView(R.id.edit_text_expire_data) EditText mEditExpireData;
    @BindView(R.id.layout_gallery) LinearLayout mLayoutGallery;
    @BindView(R.id.edit_text_web) EditText mEditWeb;
    @BindView(R.id.edit_text_quantity) EditText mEditQuantity;
    @BindView(R.id.layout_edit_url) RelativeLayout mLayoutEditUrl;
    @BindView(R.id.rvEditGallery) RecyclerView mRvEditGallery;

    private EditPhotoListAdapter adapter;

    private Repository mRepository;

    // OnDeleteListener interface, calls a method in the host activity
    public interface OnDeleteListener {
        void onDeleted();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_edit, container, false);
        ButterKnife.bind(this, rootView);

        setDateTimeField();

        this.deleteClickListener=(OnDeleteListener)getContext();
        mRepository=new Repository(getContext());

        if(savedInstanceState != null) {
            mMedicineId = savedInstanceState.getInt(MEDICINE_ID,-1);
            mEditTitle.setText(savedInstanceState.getString(EDIT_TITLE));
            mEditDescription.setText(savedInstanceState.getString(EDIT_DESCRIPTION));
            mEditExpireData.setText(savedInstanceState.getString(EDIT_EXPIRE_DATA));
            mEditWeb.setText(savedInstanceState.getString(EDIT_WEB));
            mEditQuantity.setText(savedInstanceState.getString(EDIT_QUANTITY));
        }
        //If medicine id is not null setup view model
        if(mMedicineId!=-1){
            setupViewModelMedicine();
            setupViewModelPhoto();
        }

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        mRvEditGallery.setLayoutManager(linearLayoutManager);
        adapter=new EditPhotoListAdapter(getContext(),this);
        mRvEditGallery.setAdapter(adapter);

        //Represents an arbitrary listener for image loading.
        //Download image from web, when bitmap is loaded save to local
        targetBitmap =new Target(){
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                String fileName = ImageUtils.saveImage(getContext(), bitmap,mMedicineId);
                if(!TextUtils.isEmpty(fileName)){
                    addPhotoRow(fileName);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return rootView;
    }

    //Set date picker dialog
    private void setDateTimeField() {
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        Calendar newCalendar = Calendar.getInstance();
        mDatePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            mEditExpireData.setText(dateFormatter.format(newDate.getTime()));
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @OnFocusChange(R.id.edit_text_expire_data)
    public void eventExpireData(){
        //When edit text expire data has focus show date picker dialog
        if(mEditExpireData.hasFocus()) {
            mDatePickerDialog.show();
        } else {
            //Validate inpu date
            String expireData = mEditExpireData.getText().toString();
            if (!TextUtils.isEmpty(expireData)) {
                Date date = MedicineUtils.dateFromString(expireData);
                if (!MedicineUtils.dateToString(date).equals(expireData)) {
                    mEditExpireData.setText(MedicineUtils.dateToString(date));
                }
            }
        }
    }

    //Launch camera
    @OnClick(R.id.button_camera)
    public void startCamera(){
        mLayoutEditUrl.setVisibility(View.GONE);
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_STORAGE_PERMISSION);
            return;
        }
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Launch the camera if the permission exists
            launchCamera();
        }
    }

    //Get image from web
    //Show edit text for inserting the image URL
    @OnClick(R.id.button_web)
    public void downloadFromWeb(){
        mLayoutEditUrl.setAlpha(0);
        mLayoutEditUrl.setVisibility(View.VISIBLE);
        mLayoutEditUrl.animate().alpha(1);
    }

    //Get image from gallery
    @OnClick(R.id.button_gallery)
    public void selectPhoto(){
        mLayoutEditUrl.setVisibility(View.GONE);
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    //Download image if url is not null
    @OnClick(R.id.button_edit_url)
    public void editUrl(){
        mLayoutEditUrl.setVisibility(View.GONE);
        if(!TextUtils.isEmpty(mEditWeb.getText())) {
            Picasso.with(getContext()).load(mEditWeb.getText().toString()).into(targetBitmap);
        }
    }

    //Save medicine
    @OnClick(R.id.button_save)
    public void save(){
        //id title is empty return
        if(mEditTitle.getText().toString().isEmpty()){
            showAlertTitle();
            return;
        }
        if(mMedicine==null){
            mMedicine=new Medicine();
        }
        if(mMedicineId!=-1){
            mMedicine.setId(mMedicineId);
        }
        mMedicine.setTitle(mEditTitle.getText().toString());
        mMedicine.setDescription(mEditDescription.getText().toString());
        mMedicine.setExpireData(MedicineUtils.dateFromString(mEditExpireData.getText().toString()));
        mMedicine.setQuantity(MedicineUtils.stringToInt(mEditQuantity.getText().toString()));
        mMedicine.setShowAlert(false);
        mRepository.insertUpdateMedicine(mMedicine, this);
        PrefercenceUtils.setMedicineChanged(getContext(), true);
        Toast.makeText(getContext(), R.string.medicine_saved_message, Toast.LENGTH_LONG).show();
    }

    //Return id of medicine saved
    @Override
    public void insertCallback(long id) {
        mMedicineId=(int)id;
        setupViewModelMedicine();
    }

    //Title of medicine is required
    //Date if empty insert date now
    //Show alert if title is empty
    private void showAlertTitle(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(getString(R.string.alert_error_title));
        builder.setMessage(R.string.alert_error_message_title);

        builder.setPositiveButton(getString(R.string.alert_ok_button), (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
    }

    //Delete medicine if user confirm
    @OnClick(R.id.button_delete)
    public void delete(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.alert_confirm_title));
        builder.setMessage(getString(R.string.alert_confirm_message));
        builder.setPositiveButton(getString(R.string.alert_confirm_positive), (dialog, which) -> {
            deleteAll();
            dialog.dismiss();
            // OnDeleteListener interface, calls a method in the host activity
            deleteClickListener.onDeleted();
        });
        builder.setNegativeButton(getString(R.string.alert_confirm_negative), (dialog, which) -> {
            // Do nothing
            dialog.dismiss();
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    //Delete medicine and photo
    private void deleteAll(){
        ImageUtils.deleteAllPhotoList(getContext(), mMedicine.getId());
        mRepository.deleteMedicine(mMedicine);
        mRepository.deletePhotoByMedicineId(mMedicine.getId());
    }

    //Add photo in database and save locally
    private void addPhotoRow(String image){

        final Photo photo=new Photo();
        photo.setCreatedDate(new Date());
        photo.setFileName(image);
        photo.setMedicineId(mMedicineId);
        mRepository.insertUpdatePhoto(photo);
        if(photoList==null || photoList.size()==0){
            mMedicine.setFileName(photo.getFileName());
            mRepository.insertUpdateMedicine(mMedicine, null);
        }
        if(CollectionUtils.isEmpty(photoList)){
            setupViewModelPhoto();
        }
        PrefercenceUtils.setMedicineChanged(getContext(), true);

        if(PrefercenceUtils.getBackup(getContext())){
            if(PrefercenceUtils.getWhenBackup(getContext())){
                if(PrefercenceUtils.getBackupRemote(getContext())) {
                    UploadSingleFileTask uploadSingleFileTask = new UploadSingleFileTask(getContext(), ImageUtils.getFile(getContext(), photo.getFileName()), false, null);
                    uploadSingleFileTask.execute();
                } else {
                    ExportSingleFileToSD exportSingleFileToSD = new ExportSingleFileToSD(getContext(),ImageUtils.getFile(getContext(), photo.getFileName()), false, null);
                    exportSingleFileToSD.execute();
                }
            }
        }
    }

    @Override
    public void onDeleteSelected(Photo item, int position) {
        //If is first photo change filename in medicine
        if(position==0) {
            if (photoList.size() > 1) {
                mMedicine.setFileName(photoList.get(1).getFileName());
            } else {
                mMedicine.setFileName("");
            }
            mRepository.insertUpdateMedicine(mMedicine, null);
        }
        //Delete row from table "photo"
        mRepository.deletePhoto(item);
        //Delete image file
        ImageUtils.deleteImageFromFileName(getContext(), item.getFileName());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    launchCamera();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(getContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    launchCamera();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(getContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    /**
     * Creates a temporary image file and captures a picture to store in it.
     */
    private void launchCamera() {

        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = ImageUtils.createTempImageFile(getContext());
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_IMAGE_CAPTURE:
                // If the image capture activity was called and was successful
                if (resultCode == Activity.RESULT_OK) {
                    // Process the image and set it to the TextView
                    Bitmap mResultsBitmap = ImageUtils.resamplePic(getContext(), mTempPhotoPath);
                    String fileName = ImageUtils.saveImage(getContext(), mResultsBitmap,mMedicineId);
                    ImageUtils.deleteImageFile(getContext(), mTempPhotoPath);
                    if(fileName!=null) {
                        addPhotoRow(fileName);
                    }
                } else {

                    // Otherwise, delete the temporary image file
                    ImageUtils.deleteImageFile(getContext(), mTempPhotoPath);
                }
                break;
            case REQUEST_IMAGE_GET:
                if (resultCode == Activity.RESULT_OK) {
                    Uri fullPhotoUri = data.getData();
                    try {
                        Bitmap mResultsBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), fullPhotoUri);
                        String fileName = ImageUtils.saveImage(getContext(), mResultsBitmap,mMedicineId);
                        if(fileName!=null) {
                            addPhotoRow(fileName);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }

    }

    public void setData(int medicineId){
        mMedicineId=medicineId;
    }

    private void setupViewModelMedicine() {
        MedicineViewModelFactory viewModelFactory = new MedicineViewModelFactory(mRepository, mMedicineId);
        MedicineViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(MedicineViewModel.class);
        viewModel.getMedicine().observe(this, medicine -> {
            if(medicine==null){
                return;
            }
                mMedicine = medicine;
                setText();
            mLayoutGallery.setVisibility(View.VISIBLE);
        });
    }

    private void setupViewModelPhoto(){
        if(mMedicineId!=-1) {
            PhotoViewModelFactory factory=new PhotoViewModelFactory(mRepository,mMedicineId);
            PhotoViewModel photoViewModel = ViewModelProviders.of(this, factory).get(PhotoViewModel.class);
            photoViewModel.getPhotoList().observe(this, photo -> {
                photoList = photo;
                adapter.setPhoto(photoList);
            });
        }
    }
    private void setText(){
        if(mMedicine!=null && mEditTitle.getText().toString().equals("")){
            mEditTitle.setText(mMedicine.getTitle());
            mEditDescription.setText(mMedicine.getDescription());
            mEditExpireData.setText(MedicineUtils.dateToString(mMedicine.getExpireData()));
            mEditQuantity.setText(MedicineUtils.intToString(mMedicine.getQuantity()));
        }
    }
    @Override
    public void onDestroy() {
        Picasso.with(getContext()).cancelRequest(targetBitmap);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MEDICINE_ID, mMedicineId);
        outState.putString(EDIT_TITLE, mEditTitle.getText().toString());
        outState.putString(EDIT_DESCRIPTION, mEditDescription.getText().toString());
        outState.putString(EDIT_EXPIRE_DATA, mEditExpireData.getText().toString());
        outState.putString(EDIT_WEB, mEditWeb.getText().toString());
        outState.putString(EDIT_QUANTITY, mEditQuantity.getText().toString());
    }
}
