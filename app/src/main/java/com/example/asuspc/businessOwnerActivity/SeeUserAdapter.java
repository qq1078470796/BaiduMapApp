package com.example.asuspc.businessOwnerActivity;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.example.asuspc.baidumapapp.R;
import com.example.asuspc.entity.BusinessList;

import java.util.ArrayList;
import java.util.List;

public class SeeUserAdapter extends BaseAdapter implements Filterable  {

    private List<BusinessList> mData;
    private LayoutInflater mInflater;
    private ViewHolder mHolder;
    private ArrayList<BusinessList> mUnfilteredData;
    private ArrayFilter mFilter;
    private OnSlideClickListener mListener;
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    class ViewHolder{
        TextView bm_see_username;
        Button bm_see_delete;
    }

    public SeeUserAdapter(Context context, ArrayList<BusinessList> data, OnSlideClickListener listener){
        this.mData = data;
        this.mListener = listener;
        this.mInflater = LayoutInflater.from(context);
        mUnfilteredData = new ArrayList<BusinessList>(mData);
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData == null ? null : mData.get(position).getUserName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.user_list, null);
            mHolder = new ViewHolder();
            mHolder.bm_see_username = (TextView) convertView.findViewById(R.id.bm_see_username);
            mHolder.bm_see_delete = (Button) convertView.findViewById(R.id.bm_see_delete);
            convertView.setTag(mHolder);
        }else{
            mHolder = (ViewHolder) convertView.getTag();
        }
        final int pos = position;
        final View item = convertView;
        mHolder.bm_see_delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onDeleteClick(pos, item);
            }
        });

        mHolder.bm_see_username.setText(getItem(position));
        return convertView;
    }

    public interface OnSlideClickListener{
        public void onDeleteClick(int position, View item);
    }

    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<BusinessList>(mData);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<BusinessList> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();
                ArrayList<BusinessList> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();
                ArrayList<String> newValues = new ArrayList<String>(count);

                for (int i = 0; i < count; i++) {
                    String pc= unfilteredValues.get(i).getUserName();
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
            mData = (List<BusinessList>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
