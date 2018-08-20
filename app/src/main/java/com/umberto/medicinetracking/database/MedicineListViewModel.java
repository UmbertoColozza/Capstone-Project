package com.umberto.medicinetracking.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

public class MedicineListViewModel extends ViewModel {

    private LiveData<List<Medicine>> medicineList;

    public MedicineListViewModel(Repository repository) {
        medicineList = repository.getAllMedicine();
    }

    public LiveData<List<Medicine>> getMedicineList() {
        return medicineList;
    }
}
