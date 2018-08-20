package com.umberto.medicinetracking.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.os.Parcel;
import android.os.Parcelable;

import com.umberto.medicinetracking.utils.DateTypeConverter;

import java.util.Date;

@Entity(tableName = "medicine")
public class Medicine  implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "description")
    private String description;
    @TypeConverters({DateTypeConverter.class})
    @ColumnInfo(name = "expire_date")
    private Date expireData;
    @ColumnInfo(name = "quantity")
    private int quantity;
    @ColumnInfo(name = "show_alert")
    private boolean showAlert;
    //cover image
    @ColumnInfo(name = "file_name")
    private String fileName;

    public Medicine(){}

    protected Medicine(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        quantity = in.readInt();
        showAlert = in.readByte() != 0;
        fileName = in.readString();
    }

    public static final Creator<Medicine> CREATOR = new Creator<Medicine>() {
        @Override
        public Medicine createFromParcel(Parcel in) {
            return new Medicine(in);
        }

        @Override
        public Medicine[] newArray(int size) {
            return new Medicine[size];
        }
    };

    public int getId(){ return id; }
    public void setId(int id){ this.id=id; }

    public void setTitle(String title){
        this.title=title;
    }
    public String getTitle(){
        return this.title;
    }

    public void setDescription(String name){
        this.description=description;
    }
    public String getDescription(){
        return this.description;
    }

    public void setExpireData(Date expireData){
        this.expireData=expireData;
    }
    public Date getExpireData(){
        return this.expireData;
    }

    public void setQuantity(int quantity){
        this.quantity=quantity;
    }
    public int getQuantity(){
        return this.quantity;
    }

    public void setShowAlert(boolean showAlert){
        this.showAlert=showAlert;
    }
    public boolean getShowAlert(){
        return this.showAlert;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
    public String getFileName() {
        return fileName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(quantity);
        dest.writeByte((byte) (showAlert ? 1 : 0));
        dest.writeString(fileName);
    }
}
