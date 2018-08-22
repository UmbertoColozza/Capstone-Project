package com.umberto.medicinetracking.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

public class MedicineViewModel extends ViewModel {
    private final int mMedicineId;
    private final Repository mRepository;
    private final LiveData<Medicine> medicine;

    public MedicineViewModel(Repository repository, int medicineId) {
        mRepository=repository;
        this.mMedicineId=medicineId;
        medicine = mRepository.getMedicine(medicineId);
    }

    public LiveData<Medicine> getMedicine() {
        return medicine;
    }
}
