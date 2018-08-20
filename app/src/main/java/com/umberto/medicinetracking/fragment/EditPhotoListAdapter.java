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
import butterknife.OnClick;

public class EditPhotoListAdapter extends RecyclerView.Adapter<EditPhotoListAdapter.EditPhotoListAdapterViewHolder> {
    private OnDeleteImageListener deleteClickListener;
    private List<Photo> mPhotoList;
    private Context mContext;

    // OnDeleteImageListener interface, calls a method in the host fragment
    public interface OnDeleteImageListener {
        void onDeleteSelected(Photo item, int index);
    }

    public EditPhotoListAdapter(Context context, OnDeleteImageListener deleteClickListener){
        this.deleteClickListener=deleteClickListener;
        mContext=context;
    }
    @NonNull
    @Override
    public EditPhotoListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.edit_photo_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new EditPhotoListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditPhotoListAdapterViewHolder holder, int position) {
        if(!TextUtils.isEmpty(mPhotoList.get(position).getFileName())) {
            holder.mPhoto.setContentDescription(TextUtils.concat(mContext.getString(R.string.image_content_description),Integer.toString(position)));
            Picasso.with(mContext)
                    .load(ImageUtils.getFile( mContext, mPhotoList.get(position).getFileName()))
                    .placeholder(R.drawable.ic_notify)
                    .error(R.drawable.ic_notify)
                    .into(holder.mPhoto);
        }
    }

    @Override
    public int getItemCount() {
        return mPhotoList == null ? 0 : mPhotoList.size();
    }

    public void setPhoto(final List<Photo> photoList){
        mPhotoList=photoList;
        notifyDataSetChanged();
    }

    class EditPhotoListAdapterViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.ivPhoto) ImageView mPhoto;

        EditPhotoListAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.button_delete_photo)
        public void deletePhoto(){
            int adapterPosition = getAdapterPosition();
            Photo item=mPhotoList.get(adapterPosition);
            deleteClickListener.onDeleteSelected(item,adapterPosition);
        }
    }
}
