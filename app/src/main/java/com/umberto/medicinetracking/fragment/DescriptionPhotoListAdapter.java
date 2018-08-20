package com.umberto.medicinetracking.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.Photo;
import com.umberto.medicinetracking.utils.ImageUtils;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

//Setup photo list adapter for recyclerview in fragment description
public class DescriptionPhotoListAdapter extends RecyclerView.Adapter<DescriptionPhotoListAdapter.DescriptionPhotoListAdapterViewHolder> {
    private List<Photo> mPhotoList;
    private Context mContext;
    private DescriptionFragment.OnItemDescriptionClickListener itemClickListener;


    public DescriptionPhotoListAdapter(Context context, DescriptionFragment.OnItemDescriptionClickListener itemClickListener){
        this.itemClickListener=itemClickListener;
        mContext=context;
    }
    @NonNull
    @Override
    public DescriptionPhotoListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.description_photo_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new DescriptionPhotoListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DescriptionPhotoListAdapterViewHolder holder, int position) {
        if(!TextUtils.isEmpty(mPhotoList.get(position).getFileName())) {
            holder.mPhoto.setContentDescription(TextUtils.concat(mContext.getString(R.string.image_content_description),Integer.toString(position)));
            Picasso.with(mContext)
                    .load(ImageUtils.getFile(mContext, mPhotoList.get(position).getFileName()))
                    .placeholder(R.drawable.ic_notify)
                    .error(R.drawable.ic_notify)
                    .into(holder.mPhoto);
        }
    }

    @Override
    public int getItemCount() {
        return mPhotoList == null ? 0 : mPhotoList.size();
    }

    public void setPhoto(List<Photo> photoList){
        mPhotoList=photoList;
        notifyDataSetChanged();
    }

    class DescriptionPhotoListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.ivPhoto) ImageView mPhoto;

        DescriptionPhotoListAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position=getAdapterPosition();
            itemClickListener.onItemDescriptionSelected(position);
        }
    }
}
