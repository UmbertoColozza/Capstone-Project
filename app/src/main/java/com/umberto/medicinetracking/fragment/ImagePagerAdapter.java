package com.umberto.medicinetracking.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.database.Photo;
import com.umberto.medicinetracking.utils.ImageUtils;

import java.util.List;

//Photo Gallery Adapter
public class ImagePagerAdapter extends PagerAdapter {
    private final List<Photo> mImageList;
    private final Activity mActivity;

    public ImagePagerAdapter(Activity activity, List<Photo> imageList){
        mImageList = imageList;
        mActivity = activity;
    }

    @Override
    public int getCount() {
        return (mImageList != null) ? mImageList.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        //Get LayoutInflater
        LayoutInflater inflater=(LayoutInflater)container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.image_page, container,false);
        ImageView iv = view.findViewById(R.id.imageview_page);
        DisplayMetrics dm=new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width=dm.widthPixels;
        iv.setMinimumHeight(height);
        iv.setMinimumWidth(width);
        iv.setContentDescription(TextUtils.concat(mActivity.getString(R.string.image_content_description),Integer.toString(position)));
        Picasso.with(mActivity)
                .load(ImageUtils.getFile(mActivity, mImageList.get(position).getFileName()))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(iv, new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError() {
            }
        });
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
