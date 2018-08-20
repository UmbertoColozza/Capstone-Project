package com.umberto.medicinetracking.database;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class PhotoViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final Repository mRepository;
    private final int medicineId;

    public PhotoViewModelFactory(Repository repository, int medicineId) {
        mRepository = repository;
        this.medicineId = medicineId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new PhotoViewModel(mRepository, medicineId);
    }
}
