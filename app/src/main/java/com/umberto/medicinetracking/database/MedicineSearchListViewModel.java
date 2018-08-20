package com.umberto.medicinetracking.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

public class MedicineSearchListViewModel extends ViewModel {

    private LiveData<List<Medicine>> medicineList;

    public MedicineSearchListViewModel(AppDatabase mDb,String search) {
        medicineList = mDb.taskDao().selectMedicineSearch(search);
    }

    public LiveData<List<Medicine>> getMedicineList() {
        return medicineList;
    }
}
