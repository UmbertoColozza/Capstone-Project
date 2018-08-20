package com.umberto.medicinetracking.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;
import com.umberto.medicinetracking.database.Medicine;
import java.util.List;

public class MedicineWidgetService extends RemoteViewsService {
    List<Medicine> list;
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MedicineDataProvider(getApplicationContext(),intent);
    }
}
