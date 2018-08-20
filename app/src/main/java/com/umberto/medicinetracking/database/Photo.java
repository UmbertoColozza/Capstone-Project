package com.umberto.medicinetracking.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.umberto.medicinetracking.utils.DateTypeConverter;

import java.util.Date;

@Entity(tableName = "photo")
public class Photo {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "medicine_id")
    private int medicineId;
    @ColumnInfo(name = "file_name")
    private String fileName;
    @TypeConverters({DateTypeConverter.class})
    @ColumnInfo(name = "created_date")
    private Date createdDate;

    public int getId(){ return id; }
    public void setId(int id){ this.id=id; }

    public void setMedicineId(int medicineId){
        this.medicineId=medicineId;
    }
    public int getMedicineId(){
        return medicineId;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
    public String getFileName() {
        return fileName;
    }

    public void setCreatedDate(Date createdDate){
        this.createdDate=createdDate;
    }
    public Date getCreatedDate(){
        return createdDate;
    }
}
