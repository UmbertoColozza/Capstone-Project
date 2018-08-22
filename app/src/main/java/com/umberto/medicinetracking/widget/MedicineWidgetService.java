package com.umberto.medicinetracking.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class MedicineWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MedicineDataProvider(getApplicationContext(),intent);
    }
}
