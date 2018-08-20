package com.umberto.medicinetracking;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.umberto.medicinetracking.database.AppDatabase;
import com.umberto.medicinetracking.database.Medicine;
import com.umberto.medicinetracking.database.Photo;
import com.umberto.medicinetracking.database.TaskDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DbPhotoTableTest {
    private TaskDao mTaskDao;
    private AppDatabase mDb;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        mTaskDao = mDb.taskDao();
    }

    @After
    public void closeDb() throws IOException {
        mDb.close();
    }

    @Test
    public void writeUserAndReadInList() throws Exception {
        Photo photo=new Photo();
        photo.setMedicineId(1);
        photo.setCreatedDate(new Date());
        photo.setFileName("Test");
        mTaskDao.insertPhoto(photo);

        List<Photo> list= mTaskDao.selectPhotoTest(1);
        assert(list.isEmpty());
    }
}
