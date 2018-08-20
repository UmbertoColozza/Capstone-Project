package com.umberto.medicinetracking.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.Medicine;
import com.umberto.medicinetracking.utils.MedicineUtils;
import com.umberto.medicinetracking.utils.PrefercenceUtils;
import java.util.List;

public class MedicineDataProvider implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private List<Medicine> listMedicine;
    private int appWidgetId;

    public MedicineDataProvider(Context context, Intent intent) {
        mContext = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        listMedicine = PrefercenceUtils.getWidgetMedicine(mContext);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return(listMedicine.size());
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);

        row.setTextViewText(R.id.tvWidgetMedicineTitle, listMedicine.get(position).getTitle());
        row.setTextViewText(R.id.tvWidgetMedicineExpire, TextUtils.concat(mContext.getString(R.string.expire_date), MedicineUtils.dateToString(listMedicine.get(position).getExpireData())));

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("widget_item_id", listMedicine.get(position).getId());
        row.setOnClickFillInIntent(R.id.item_widget, fillInIntent);

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
