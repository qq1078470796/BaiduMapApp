package com.example.asuspc.nomalUserActivity;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asuspc.baidumapapp.R;
import com.example.asuspc.entity.Dining;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus   pc on 2017/12/13.
 */

public class SeeAdapter extends BaseAdapter implements Filterable {
    private List<Dining> mData;
    private LayoutInflater mInflater;
    private SeeAdapter.SeeViewHolder mHolder;
    private ArrayList<Dining> mUnfilteredData;
    private SeeAdapter.SeeArrayFilter mFilter;
    private SeeAdapter.OnSeeSlideClickListener mListener;
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SeeAdapter.SeeArrayFilter();
        }
        return mFilter;
    }

    class SeeViewHolder{
        TextView dining_name;
        TextView dining_price;
        ImageView picture;
        Button add;

    }

    public SeeAdapter(Context context, ArrayList<Dining> data, SeeAdapter.OnSeeSlideClickListener listener){
        this.mData = data;
        this.mListener = listener;
        this.mInflater = LayoutInflater.from(context);
        mUnfilteredData = new ArrayList<Dining>(mData);
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData == null ? null : mData.get(position).getDinging_name();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.see_dining, null);
            mHolder = new SeeAdapter.SeeViewHolder();
            mHolder.dining_name = (TextView) convertView.findViewById(R.id.see_dining_name);
            mHolder.dining_price = (TextView) convertView.findViewById(R.id.see_dining_price);
            mHolder.add = (Button) convertView.findViewById(R.id.see_dining_add);
            mHolder.picture = (ImageView) convertView.findViewById(R.id.see_dining_picture);
            convertView.setTag(mHolder);
        }else{
            mHolder = (SeeAdapter.SeeViewHolder) convertView.getTag();
        }
        final int pos = position;
        final View item = convertView;
        mHolder.add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onAddClick(pos, item);
            }
        });
        mHolder.dining_name.setText(getItem(position));
        mHolder.dining_price.setText(mData.get(position).getDining_price()+"");
        byte[] pictureData=mData.get(position).getDining_picture();
        if(pictureData!=null)
            mHolder.picture.setImageBitmap(BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length));
        return convertView;
    }

    public interface OnSeeSlideClickListener{
        public void onAddClick(int position, View item);
    }

    private class SeeArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<Dining>(mData);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<Dining> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();
                ArrayList<Dining> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<String> newValues = new ArrayList<String>(count);

                for (int i = 0; i < count; i++) {
                    String pc= unfilteredValues.get(i).getDinging_name();
                    if (pc != null) {
                        if(pc.startsWith(prefixString)){
                            newValues.add(pc);
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            mData = (List<Dining>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
