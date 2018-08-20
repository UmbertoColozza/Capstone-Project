package com.umberto.medicinetracking.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import java.util.List;

public class MedicineViewModel extends ViewModel {
    private int mMedicineId;
    private Repository mRepository;
    private LiveData<Medicine> medicine;

    public MedicineViewModel(Repository repository, int medicineId) {
        mRepository=repository;
        this.mMedicineId=medicineId;
        medicine = mRepository.getMedicine(medicineId);
    }

    public LiveData<Medicine> getMedicine() {
        return medicine;
    }
}
