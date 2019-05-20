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
import android.widget.ImageView;
import android.widget.TextView;
import com.example.asuspc.baidumapapp.R;
import com.example.asuspc.entity.Dining;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends BaseAdapter implements Filterable  {

    private List<Dining> mData;
    private LayoutInflater mInflater;
    private ViewHolder mHolder;
    private ArrayList<Dining> mUnfilteredData;
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
        TextView dining_name;
        TextView dining_price;
        ImageView picture;
        Button update;
        Button delete;
    }

    public MyAdapter(Context context, ArrayList<Dining> data,OnSlideClickListener listener){
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
            convertView = mInflater.inflate(R.layout.dining_layout, null);
            mHolder = new ViewHolder();
            mHolder.dining_name = (TextView) convertView.findViewById(R.id.a_dining_name);
            mHolder.dining_price = (TextView) convertView.findViewById(R.id.a_dining_price);
            mHolder.update = (Button) convertView.findViewById(R.id.a_dining_xiugai);
            mHolder.delete = (Button) convertView.findViewById(R.id.a_dining_delete);
            mHolder.picture = (ImageView) convertView.findViewById(R.id.a_dining_picture);
            convertView.setTag(mHolder);
        }else{
            mHolder = (ViewHolder) convertView.getTag();
        }
        final int pos = position;
        final View item = convertView;
        mHolder.delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onDeleteClick(pos, item);
            }
        });
        mHolder.update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onUpdateClick(pos, item);
            }
        });
        mHolder.dining_name.setText(getItem(position));
        mHolder.dining_price.setText(mData.get(position).getDining_price()+"");
        byte[] pictureData=mData.get(position).getDining_picture();
        if(pictureData!=null)
        mHolder.picture.setImageBitmap(BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length));
        return convertView;
    }

    public interface OnSlideClickListener{
        public void onUpdateClick(int position, View item);
        public void onDeleteClick(int position, View item);
    }

    private class ArrayFilter extends Filter {

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
