package com.umberto.medicinetracking.database;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class MedicineSearchListViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDatabase mDb;
    private final String search;

    public MedicineSearchListViewModelFactory(AppDatabase database,String search) {
        mDb = database;
        this.search = search;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MedicineSearchListViewModel(mDb, search);
    }
}
