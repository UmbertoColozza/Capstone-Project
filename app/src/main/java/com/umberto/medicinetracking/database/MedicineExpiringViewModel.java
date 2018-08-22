package com.umberto.medicinetracking.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class MedicineExpiringViewModel extends AndroidViewModel {
    private final Repository mRepository;
    private final LiveData<List<Medicine>> medicine;

    public MedicineExpiringViewModel(Application application) {
        super(application);
        mRepository=new Repository(application);
        medicine = mRepository.getExpiringMedicine();
    }

    public LiveData<List<Medicine>> getMedicine() {
        return medicine;
    }
}
