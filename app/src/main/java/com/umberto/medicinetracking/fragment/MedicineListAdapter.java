package com.umberto.medicinetracking.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.Medicine;
import com.umberto.medicinetracking.database.Repository;
import com.umberto.medicinetracking.utils.ImageUtils;
import com.umberto.medicinetracking.utils.MedicineUtils;
import com.umberto.medicinetracking.utils.PrefercenceUtils;

import java.util.List;

public class MedicineListAdapter extends RecyclerView.Adapter<MedicineListAdapter.MedicineAdapterViewHolder> {
    private final ListFragment.OnItemListClickListener itemClickListener;
    private final List<Medicine> mMedicineList;
    private final Context mContext;
    private Repository mRepository;

    public MedicineListAdapter(Context context, List<Medicine> medicineList, ListFragment.OnItemListClickListener itemClickListener){
        mMedicineList=medicineList;
        this.itemClickListener=itemClickListener;
        mContext=context;
    }
    @NonNull
    @Override
    public MedicineAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem;
        if(PrefercenceUtils.showGrid(mContext)) {
            layoutIdForListItem = R.layout.medicine_grid_item;
        } else {
            layoutIdForListItem = R.layout.medicine_list_item;
        }
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        mRepository=new Repository(mContext);
        return new MedicineAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineAdapterViewHolder holder, int position) {
        holder.tvMedicineName.setText(mMedicineList.get(position).getTitle());
        holder.tvQuantity.setText(MedicineUtils.intToString(mMedicineList.get(position).getQuantity()));
        if(holder.tvMedicineExpire!=null) {
            holder.tvMedicineExpire.setText(MedicineUtils.dateToString(mMedicineList.get(position).getExpireData()));
        }
        if(holder.ivCover!=null && !TextUtils.isEmpty((mMedicineList.get(position).getFileName()))) {
            holder.ivCover.setContentDescription(mMedicineList.get(position).getTitle());
            Picasso.with(mContext)
                    .load(ImageUtils.getFile(mContext, mMedicineList.get(position).getFileName()))
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.ivCover);
        }
    }

    @Override
    public int getItemCount() {
        return mMedicineList!=null ? mMedicineList.size() : 0;
    }

    class MedicineAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView tvMedicineName;
        final TextView tvMedicineExpire;
        final TextView tvQuantity;
        ImageView ivCover;
        final Button buttonAdd;
        final Button buttonSubtract;
        MedicineAdapterViewHolder(View view) {
            super(view);
            if(PrefercenceUtils.showGrid(mContext)){
                tvMedicineName = view.findViewById(R.id.tvGridMedicineName);
                tvQuantity = view.findViewById(R.id.tvGridQuantity);
                tvMedicineExpire=null;
                ivCover = view.findViewById(R.id.imageview_cover);
                buttonAdd = view.findViewById(R.id.gridButtonAdd);
                buttonSubtract = view.findViewById(R.id.gridButtonSubtract);
            } else {
                tvMedicineName = view.findViewById(R.id.tvListMedicineName);
                tvQuantity = view.findViewById(R.id.tvListQuantity);
                tvMedicineExpire=view.findViewById(R.id.tvListDateMedicineExpire);
                buttonAdd = view.findViewById(R.id.listButtonAdd);
                buttonSubtract = view.findViewById(R.id.listButtonSubtract);
            }
            view.setOnClickListener(this);

            buttonAdd.setOnClickListener(v -> {
                int position=getAdapterPosition();
                final Medicine medicine=mMedicineList.get(position);
                medicine.setQuantity(medicine.getQuantity()+1);
                tvQuantity.setText(MedicineUtils.intToString(medicine.getQuantity()));
                mRepository.insertUpdateMedicine(medicine, null);
            });
            buttonSubtract.setOnClickListener(v -> {
                int position=getAdapterPosition();
                final Medicine medicine=mMedicineList.get(position);
                medicine.setQuantity(medicine.getQuantity()-1);
                if(medicine.getQuantity()<0) {
                    medicine.setQuantity(0);
                }
                tvQuantity.setText(MedicineUtils.intToString(medicine.getQuantity()));
                mRepository.insertUpdateMedicine(medicine, null);
            });
        }
        @Override
        public void onClick(View v) {
            int position=getAdapterPosition();
            itemClickListener.onItemListSelected(mMedicineList.get(position));
        }
    }
}
