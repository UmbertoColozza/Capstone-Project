package com.umberto.medicinetracking.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

public class PhotoViewModel extends ViewModel {
    private LiveData<List<Photo>> photoList;

    public PhotoViewModel(Repository repository, int medicineId) {
        photoList = repository.getAllPhotoByMedicine(medicineId);
    }

    public LiveData<List<Photo>> getPhotoList() {
        return photoList;
    }
}
