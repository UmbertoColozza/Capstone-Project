package com.umberto.medicinetracking;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.umberto.medicinetracking.database.AppDatabase;
import com.umberto.medicinetracking.database.Medicine;
import com.umberto.medicinetracking.database.TaskDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DbMedicineTableTest {
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
        Medicine medicine=new Medicine();
        medicine.setTitle("Test");
        medicine.setFileName("Test");
        medicine.setDescription("Test");
        medicine.setExpireData(new Date());
        medicine.setQuantity(0);
        mTaskDao.insertMedicine(medicine);

        List<Medicine> list= mTaskDao.selectAllMedicineTest();
        assert(list.isEmpty());
    }
}
