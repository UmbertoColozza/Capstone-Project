package com.umberto.medicinetracking.fragment;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.AppExecutors;
import com.umberto.medicinetracking.database.Medicine;
import com.umberto.medicinetracking.database.MedicineSearchListViewModel;
import com.umberto.medicinetracking.database.Repository;
import com.umberto.medicinetracking.utils.ImageUtils;
import com.umberto.medicinetracking.utils.PrefercenceUtils;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String SEARCH_KEY="search_key";
    @BindView(R.id.rvMedicine) RecyclerView mRecyclerViewMedicine;
    private List<Medicine> mMedicineList;
    MedicineListAdapter medicineListAdapter;
    GridLayoutManager gridLayoutManager;
    ItemTouchHelper itemTouchhelper;
    private String mSearch;
    private MedicineSearchListViewModel viewModel;
    private Repository repository;
    private SearchView searchView;
    private Repository mRepository;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_show_list_grid_key))) {
            setListFragment();
        }
    }

    // OnItemClickListener interface, calls a method in the host activity named onItemSelected
    public interface OnItemListClickListener {
        void onItemListSelected(Medicine item);
    }

    // Empty constructor
    public ListFragment(){
    }
    // Inflates the list medicine
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);
        repository=new Repository(getContext());
        mSearch="";
        if(savedInstanceState!=null){
            mSearch = savedInstanceState.getString(SEARCH_KEY);
        }
        mRepository = new Repository(getContext());
        // Register the listener
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        setupViewModel();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupViewModel();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item= menu.findItem(R.id.action_search);
        searchView = (SearchView) item.getActionView();

        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText!=mSearch) {
                    mSearch = newText;
                    setupViewModel();
                }
                return true;
            }
        });
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setQuery(mSearch, false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mSearch="";
                setupViewModel();
                return true;
            }
        });
    }

    private void setupViewModel(){
        repository.getMedicineSearchList(mSearch)
                .observe(this, new Observer<List<Medicine>>() {
                    @Override
                    public void onChanged(@Nullable List<Medicine> medicines) {
                        if (medicines == null) {
                            return;
                        }
                        mMedicineList=medicines;
                        setListFragment();

                    }
                });
    }

    private void updateColumns(){
        int numberOfColumns=1;
        //if attr isTable equal false set one column else set three column
        if(PrefercenceUtils.showGrid(getContext())) {
            if(getResources().getBoolean(R.bool.isTablet)){
                numberOfColumns=6;
            } else {
                numberOfColumns=3;
            }
        }
        gridLayoutManager = new GridLayoutManager(this.getContext(), numberOfColumns);
        mRecyclerViewMedicine.setLayoutManager(gridLayoutManager);
    }

    private void setListFragment(){
        int numberOfColumns=1;
        //if attr isTable equal false set one column else set three column
        if(PrefercenceUtils.showGrid(getContext())) {
            if(getResources().getBoolean(R.bool.isTablet)){
                numberOfColumns=6;
            } else {
                numberOfColumns=3;
            }
        }
        gridLayoutManager = new GridLayoutManager(this.getContext(), numberOfColumns);
        mRecyclerViewMedicine.setLayoutManager(gridLayoutManager);
        medicineListAdapter=new MedicineListAdapter(getContext(),mMedicineList, (OnItemListClickListener) getContext());
        mRecyclerViewMedicine.setAdapter(medicineListAdapter);
        setUpItemTouchHelper();
        if(!PrefercenceUtils.showGrid(getContext())) {
            //setSwipe();
            itemTouchhelper.attachToRecyclerView(mRecyclerViewMedicine);
        } else {
            if(itemTouchhelper!=null){
                //Detach the ItemTouchHelper from the RecyclerView
                itemTouchhelper.attachToRecyclerView(mRecyclerViewMedicine);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_KEY, mSearch);
    }

    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.alert_confirm_title));
                builder.setMessage(getString(R.string.alert_confirm_message));
                builder.setPositiveButton(getString(R.string.alert_confirm_positive), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                deleteMedicine(swipedPosition);
                            }
                        });
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(getString(R.string.alert_confirm_negative), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        medicineListAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();
            }

        };
        itemTouchhelper = new ItemTouchHelper(simpleItemTouchCallback);
    }

    private void deleteMedicine(final int index){
        if(mMedicineList.get(index)!=null){
            ImageUtils.deleteAllPhotoList(getContext(), mMedicineList.get(index).getId());
            mRepository.deletePhotoByMedicineId(mMedicineList.get(index).getId());
            mRepository.deleteMedicine(mMedicineList.get(index));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister ListFragment as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
