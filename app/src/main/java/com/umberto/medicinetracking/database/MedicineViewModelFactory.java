package com.umberto.medicinetracking.database;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class MedicineViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final Repository mRepository;
    private final int medicineId;

    public MedicineViewModelFactory(Repository repository, int medicineId) {
        mRepository = repository;
        this.medicineId = medicineId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MedicineViewModel(mRepository, medicineId);
    }
}
