package com.umberto.medicinetracking.ui;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.service.NotificationScheduler;
import com.umberto.medicinetracking.database.MedicineExpiringViewModel;
import com.umberto.medicinetracking.database.MedicineViewModel;
import com.umberto.medicinetracking.database.MedicineViewModelFactory;
import com.umberto.medicinetracking.database.Repository;
import com.umberto.medicinetracking.service.UploadService;
import com.umberto.medicinetracking.fragment.ListFragment;
import com.umberto.medicinetracking.database.Medicine;
import com.umberto.medicinetracking.utils.ImageUtils;
import com.umberto.medicinetracking.utils.MedicineUtils;
import com.umberto.medicinetracking.utils.PrefercenceUtils;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ListFragment.OnItemListClickListener {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab_add) FloatingActionButton mFabAdd;
    private Repository mRepository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupActionBar();

        mFabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                startActivity(intent);
            }
        });

        mRepository=new Repository(this);

        if(savedInstanceState==null) {
            //Setup Fragmentlist
            setupListFragment();

            //Check if activity has been called from widgwt or notification
            Intent intent = getIntent();
            int notifyId=intent.getIntExtra(NotificationScheduler.NOTIFICATION_MEDICINE_ID, -100);
            int widgetId=intent.getIntExtra("widget_item_id", -1);
            if ( notifyId != -100) {
                NotificationScheduler.cancelReminder(MainActivity.this);
                showAlert(notifyId);
            } else if(widgetId!=-1){
                openDescriptionActivity(widgetId);
            }
        }
        getExpiringMedicine();
    }

    //Setup Fragmentlist
    private void setupListFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ListFragment listFragment=new ListFragment();
        transaction.replace(R.id.fragment_list, listFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        setSupportActionBar(mToolbar);
    }

    //Get list of the top 10 medicines in order are expiring. First medicine is set to alarm
    private void getExpiringMedicine(){
        final MedicineExpiringViewModel viewModel = ViewModelProviders.of(this).get(MedicineExpiringViewModel.class);
        viewModel.getMedicine().observe(this, new Observer<List<Medicine>>() {
            @Override
            public void onChanged(@Nullable List<Medicine> list) {
                if(list!=null && list.size()>0) {
                    //Save list of expiring medicine in preference that will be used by the widget
                    PrefercenceUtils.setWidgetMedicine(MainActivity.this,list);
                    //Set notification for first expiring medicine
                    Medicine medicine=list.get(0);
                    CharSequence content = TextUtils.concat(getString(R.string.expire_date),MedicineUtils.dateToString(medicine.getExpireData()));
                    NotificationScheduler.setReminder(MainActivity.this,medicine.getExpireData(),medicine.getTitle(), content.toString(),medicine.getId());
                }
                else {
                    PrefercenceUtils.setWidgetMedicine(MainActivity.this,null);
                }
            }
        });
    }

    //Show alert if exist
    private void showAlert(int medicineId){
            MedicineViewModelFactory viewModelFactory = new MedicineViewModelFactory(mRepository, medicineId);
            final MedicineViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(MedicineViewModel.class);
            viewModel.getMedicine().observe(this, new Observer<Medicine>() {
                @Override
                public void onChanged(@Nullable Medicine medicine) {
                    if(medicine!=null) {
                        showAlert(medicine);
                    }
                    viewModel.getMedicine().removeObservers(MainActivity.this);
                }
            });
    }

    private void showAlert(Medicine medicine){
        AlertDialog.Builder alertadd = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.alert_layout, null);
        TextView tvTitle=view.findViewById(R.id.textview_medicine_title_alert);
        TextView tvExpireData=view.findViewById(R.id.textview_expire_date_alert);
        ImageView dialogImage=view.findViewById(R.id.dialog_imageview);
        tvTitle.setText(medicine.getTitle());
        tvExpireData.setText(TextUtils.concat(getString(R.string.expire_date), MedicineUtils.dateToString(medicine.getExpireData())));
        if(!TextUtils.isEmpty(medicine.getFileName())){
            Picasso.with(this)
                    .load(ImageUtils.getFile( this, medicine.getFileName()))
                    .placeholder(R.drawable.ic_notify)
                    .error(R.drawable.ic_notify)
                    .into(dialogImage);
        }
        alertadd.setView(view);
        alertadd.setPositiveButton(R.string.btn_close_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {

            }
        });
        alertadd.show();
    }

    //Open Description Activity
    private void openDescriptionActivity(int id){
        Intent intent = new Intent(this, DescriptionActivity.class);
        intent.putExtra(DescriptionActivity.MEDICINE_KEY_ID, id);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    @Override
    public void onItemListSelected(Medicine item) {
        openDescriptionActivity(item.getId());
    }

    //Start service backup if preference is enabled.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if preference backup is true start service backup
        if(PrefercenceUtils.getBackup(this)) {
            if(PrefercenceUtils.getWhenBackup(this) && !PrefercenceUtils.getMedicineChanged(this)){
                return;
            }
            PrefercenceUtils.setMedicineChanged(this, false);

            //Start uploadservice with action:
            // uploaddbdrive Upload only db if preference backup only change data on drive
            // uploaddbsd Upload only db if preference backup only change data on SD card
            // drive: Backup on google drive
            // sdcard: Backup on SD card
            Intent intent=new Intent(this,UploadService.class);
            if(PrefercenceUtils.getWhenBackup(this)){
                if(PrefercenceUtils.getBackupRemote(this)) {
                    intent.setAction("uploaddbdrive");
                } else  {
                    intent.setAction("uploaddbsd");
                }
            } else {
                if (PrefercenceUtils.getBackupRemote(this)) {
                    intent.setAction("drive");
                } else {
                    intent.setAction("sdcard");
                }
            }
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mRepository.isOpen(this)){
            getExpiringMedicine();
        }
    }
}
