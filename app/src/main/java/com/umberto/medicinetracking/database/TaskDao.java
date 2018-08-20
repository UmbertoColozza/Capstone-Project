package com.umberto.medicinetracking.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import java.util.List;

@Dao
public interface TaskDao {
    //Queries to Table "Medicine"
    //Select list of medicine ordered by name
    @Query("SELECT * FROM medicine ORDER BY title")
    LiveData<List<Medicine>> selectAllMedicine();

    //Select list of medicine title contains string
    @Query("SELECT * FROM medicine WHERE title LIKE '%' || :title || '%' ORDER BY title")
    LiveData<List<Medicine>> selectMedicineSearch(String title);

    //Select list 10 expiring medicines order by date
    @Query("SELECT * FROM medicine WHERE expire_date >= :date ORDER BY expire_date, title LIMIT 10")
    LiveData<List<Medicine>> selectExpiringMedicine(long date);


    //Insert medicine, if exists replace it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMedicine(Medicine medicineInsert);

    //Update medicine.
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMedicine(Medicine medicineUpdate);

    //Update cover photo of medicine.
    @Query("UPDATE medicine SET file_name= :fileName WHERE id = :medicineId")
    public abstract int updateMedicineFileName(String fileName, int medicineId);

    //Delete medicine
    @Delete
    void deleteMedicine(Medicine medicineDelete);

    //Select medicine by id
    @Query("SELECT * FROM medicine WHERE id = :id")
    LiveData<Medicine> selectMedicineById(int id);

    //Select medicine for test only
    @Query("SELECT * FROM medicine ORDER BY title")
    List<Medicine> selectAllMedicineTest();

    //Queries to Table "Photo Gallery"
    //Selecct list of photo ordered by name
    @Query("SELECT * FROM photo WHERE medicine_id=:idMedicine ORDER BY created_date")
    LiveData<List<Photo>> selectAllPhotoByMedicine(int idMedicine);

    //Insert medicine, if exists replace it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPhoto(Photo photoInsert);

    //Update medicine.
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePhoto(Photo photoUpdate);

    //Delete photo
    @Delete
    void deletePhoto(Photo photoDelete);

    //Delete photo by medicine id
    @Query("Delete FROM photo WHERE medicine_id = :medicineId")
    void deletePhotoByMedicineId(int medicineId);

    //Select medicine by id
    @Query("SELECT * FROM photo WHERE id = :id")
    LiveData<Photo> selectPhotoById(int id);

    //Select photo for test only
    @Query("SELECT * FROM photo WHERE medicine_id = :medicineId")
    List<Photo> selectPhotoTest(int medicineId);
}
