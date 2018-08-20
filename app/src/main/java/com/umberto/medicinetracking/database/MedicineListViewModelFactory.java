package com.umberto.medicinetracking.database;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class MedicineListViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final Repository mRepository;

    public MedicineListViewModelFactory(Repository repository) {
        mRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MedicineListViewModel(mRepository);
    }
}
