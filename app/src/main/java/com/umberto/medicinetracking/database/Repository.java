package com.umberto.medicinetracking.database;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.umberto.medicinetracking.utils.MedicineUtils;

import java.util.Date;
import java.util.List;

public class Repository {
    public interface InsertCallback {
        void insertCallback(long id);
    }
    private InsertCallback callback;
    private final Context mContext;
    private static AppDatabase appDatabase;

    public Repository(Context context){
        mContext=context;
    }

    public synchronized static AppDatabase geAppDatabase(Context context) {
        if (appDatabase == null) {
            appDatabase = AppDatabase.getInstance(context);
        }
        return appDatabase;
    }

    public TaskDao getTaskDAO(){
        return geAppDatabase(mContext).taskDao();
    }

    public boolean isOpen(){
        geAppDatabase(mContext);
        return appDatabase.isOpen();
    }

    //MEDICINE
    public LiveData<List<Medicine>> getMedicineSearchList(String query){
        return getTaskDAO().selectMedicineSearch(query);
    }

    public LiveData<List<Medicine>> getAllMedicine(){
        return getTaskDAO().selectAllMedicine();
    }

    public LiveData<Medicine> getMedicine(int id){
        return getTaskDAO().selectMedicineById(id);
    }

    public LiveData<List<Medicine>> getExpiringMedicine(){
        return getTaskDAO().selectExpiringMedicine(MedicineUtils.getDateWithoutTime(new Date()).getTime());
    }

    public void insertUpdateMedicine(Medicine medicine, InsertCallback callback){
        this.callback=callback;
        AppExecutors.getInstance().diskIO().execute(() -> {
            if(medicine.getId()==0) {
                long id=getTaskDAO().insertMedicine(medicine);
                if(callback!=null){
                    callback.insertCallback(id);
                }
            } else {
                getTaskDAO().updateMedicine(medicine);
            }
        });
    }
    public void updateMedicineShowAlert(int medicineId){
        this.callback=callback;
        AppExecutors.getInstance().diskIO().execute(() -> {
                getTaskDAO().updateMedicineShowAlert(medicineId);
        });
    }

    //PHOTO
    public void updatePhotoMedicine(int medicineId, String fileName){
        AppExecutors.getInstance().diskIO().execute(() -> getTaskDAO().updateMedicineFileName(fileName, medicineId));
    }
    public void deleteMedicine(Medicine medicine){
        AppExecutors.getInstance().diskIO().execute(() -> getTaskDAO().deleteMedicine(medicine));
    }

    public void insertUpdatePhoto(Photo photo){
        AppExecutors.getInstance().diskIO().execute(() -> {
            if(photo.getId()==0) {
                getTaskDAO().insertPhoto(photo);
            } else {
                getTaskDAO().updatePhoto(photo);
            }
        });
    }

    public LiveData<List<Photo>> getAllPhotoByMedicine(int medicineId){
        return getTaskDAO().selectAllPhotoByMedicine(medicineId);
    }

    public void deletePhoto(Photo photo){
        AppExecutors.getInstance().diskIO().execute(() -> getTaskDAO().deletePhoto(photo));
    }

    public void deletePhotoByMedicineId(int medicineId){
        AppExecutors.getInstance().diskIO().execute(() -> getTaskDAO().deletePhotoByMedicineId(medicineId));
    }
}
