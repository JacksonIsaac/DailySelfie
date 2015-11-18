package com.jacksonisaac.dailyselfie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by JacksonIsaac on 19/11/15.
 */
public class ListViewAdapter extends BaseAdapter {

    private ArrayList<String> mList = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ViewHolder holder;
    private String mCurrentPhotoPath;

    public ListViewAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View newView = convertView;

        if (null == convertView) {
            holder = new ViewHolder();
            newView = mLayoutInflater.inflate(R.layout.activity_image_list, null);
            holder.thumbnail = (ImageView) newView.findViewById(R.id.thumbnail);
            holder.date = (TextView) newView.findViewById(R.id.date);
            newView.setTag(holder);
        } else {
            holder = (ViewHolder) newView.getTag();
        }

        mCurrentPhotoPath = mList.get(position);
        mCurrentPhotoPath = mCurrentPhotoPath.replaceFirst("file:", "");
        holder.date.setText("Date: " + formatDateFromFile(mCurrentPhotoPath));

        // Get the dimensions of the View
        int targetW = holder.thumbnail.getMaxWidth();
        int targetH = holder.thumbnail.getMaxHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        holder.thumbnail.setImageBitmap(bitmap);

        return newView;
    }

    private String formatDateFromFile(String filename)
    {
        int startIndex = filename.indexOf('_')+1;
        int endIndex = filename.indexOf('_', startIndex);
        String date = filename.substring(startIndex, endIndex);
        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6, 8);
        return year + "-" + month + "-" + day;
    }


    private class ViewHolder {
        ImageView thumbnail;
        TextView date;
    }

    public void add(String file) {
        mList.add(file);
        notifyDataSetChanged();
    }

    public ArrayList<String> getList() {
        return mList;
    }

    public void removeAllViews(){
        mList.clear();
        this.notifyDataSetChanged();
    }
}
