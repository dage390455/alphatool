package com.sensoro.loratool.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sensoro.loratool.R;

/**
 * Created by fangping on 2016/7/7.
 */

public class MenuInfoAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private String mMenuInfoArray[] = null;
    private int selectedIndex;

    public MenuInfoAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mMenuInfoArray = context.getResources().getStringArray(R.array.drawer_title_array);
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
    }
    @Override
    public int getCount() {
        return mMenuInfoArray.length;
    }

    @Override
    public Object getItem(int i) {
        return mMenuInfoArray[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        MenuInfoViewHolder holder = null;
        if (convertView == null) {
            holder = new MenuInfoViewHolder();
            convertView = mInflater.inflate(R.layout.item_menu, null);
            holder.item_name = (TextView) convertView.findViewById(R.id.item_menu_name);
            holder.item_value = (TextView) convertView.findViewById(R.id.item_menu_value);

            convertView.setTag(holder);
        } else {
            holder = (MenuInfoViewHolder) convertView.getTag();
        }
        holder.item_name.setText(mMenuInfoArray[position]);

//        holder.item_value.setVisibility(View.GONE);
        if (position == selectedIndex) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.slide_menu_item_bg));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }


    class MenuInfoViewHolder {

        TextView item_name;
        TextView item_value;

        public MenuInfoViewHolder() {

        }
    }
}