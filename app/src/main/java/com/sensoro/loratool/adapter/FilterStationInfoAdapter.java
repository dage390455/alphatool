package com.sensoro.loratool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.sensoro.loratool.R;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.model.FilterData;

import java.util.List;
import java.util.Map;

/**
 * Created by fangping on 2016/7/7.
 */

public class FilterStationInfoAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private Map<String, List<FilterData>> dataSet;
    private String[] parentArray;

    public FilterStationInfoAdapter(Context context, Map<String, List<FilterData>> dataSet, String[] parentArray) {
        this.mContext = context;
        this.dataSet = dataSet;
        this.parentArray = parentArray;
    }

    @Override
    public int getGroupCount() {
        return dataSet.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return dataSet.get(parentArray[groupPosition]).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return dataSet.get(parentArray[groupPosition]);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return dataSet.get(parentArray[groupPosition]).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_filter_child, null);
        }
        view.setTag(R.layout.item_filter_parent, groupPosition);
        view.setTag(R.layout.item_filter_child, childPosition);
        TextView nameTextView = (TextView) view.findViewById(R.id.item_filter_name);
        final TextView statusTextView = (TextView) view.findViewById(R.id.item_filter_status);
        nameTextView.setText(dataSet.get(parentArray[groupPosition]).get(childPosition).getName());
        FilterData filterData = (FilterData) getChild(groupPosition, childPosition);
        if (filterData.isSelected()) {
            statusTextView.setText("✔️️");
        } else {
            statusTextView.setText("");

        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (groupPosition) {
                    case Constants.FILTER_STATION_SWITCH:
                    case Constants.FILTER_STATION_NEARBY:
                    case Constants.FILTER_STATION_SIGNAL:
                        List<FilterData> tempList = (List)getGroup(groupPosition);
                        for (int i = 0; i < tempList.size(); i++) {
                            FilterData filterData = tempList.get(i);
                            if (i == childPosition) {
                                filterData.setSelected(true);
                            } else {
                                filterData.setSelected(false);
                            }
                        }
                        notifyDataSetChanged();;
                        break;
                    default:
                        FilterData tempData = (FilterData) getChild(groupPosition, childPosition);
                        if (tempData.isSelected()) {
                            tempData.setSelected(false);
                        } else {
                            tempData.setSelected(true);
                        }
                        notifyDataSetChanged();
                        break;
                }

            }
        });
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public View getGroupView(int parentPos, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_filter_parent, null);
        }
        view.setTag(R.layout.item_filter_parent, parentPos);
        view.setTag(R.layout.item_filter_child, -1);
        TextView text = (TextView) view.findViewById(R.id.filter_menu_name);
        text.setText(parentArray[parentPos]);
        return view;
    }


}