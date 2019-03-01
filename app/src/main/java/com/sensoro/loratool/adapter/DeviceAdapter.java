package com.sensoro.loratool.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sensoro.loratool.R;
import com.sensoro.loratool.model.SettingDeviceModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private final List<SettingDeviceModel> mList = new ArrayList<>();
    private RecyclerItemClickListener listener;

    public DeviceAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 1:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_general_type, parent, false);
                return new MatunFireGeneralHolder(view);
            case 2:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_title_type, parent, false);
                return new MatunFireTitleHolder(view);
            default:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_general_type, parent, false);
                return new MatunFireGeneralHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        SettingDeviceModel model = mList.get(position);
        switch (model.viewType){
            case 1:
                if (holder instanceof MatunFireGeneralHolder) {
                    loadGeneralType((MatunFireGeneralHolder) holder,model,position);
                }
                break;
            case 2:
                if (holder instanceof MatunFireTitleHolder) {
                    loadTitleType((MatunFireTitleHolder) holder,model);
                }

                break;
        }
    }

    private void loadTitleType(MatunFireTitleHolder holder, SettingDeviceModel model) {
        holder.itemTitleTypeTvTitle.setText(model.title);
    }

    private void loadGeneralType(MatunFireGeneralHolder holder, SettingDeviceModel model, int position) {
        holder.itemGeneralTypeTvName.setText(model.name);
        holder.itemGeneralTypeTvContent.setText(model.content);
        holder.itemGeneralTypeImvArrow.setVisibility(model.isArrow ? View.VISIBLE : View.GONE);
        holder.itemGeneralTypeLlRoot.setEnabled(model.isArrow);
        holder.itemGeneralTypeViewDivider.setVisibility(model.isDivider && mList.size() - 1 != position ? View.VISIBLE : View.GONE);

        holder.itemGeneralTypeLlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && model.canClick) {
                    listener.onItemClick(model,position);
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).viewType;
    }

    public void updateData(List<SettingDeviceModel> data) {
        mList.clear();
        mList.addAll(data);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(RecyclerItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public List<SettingDeviceModel> getData() {
        return mList;
    }


    class MatunFireGeneralHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_general_type_tv_name)
        TextView itemGeneralTypeTvName;
        @BindView(R.id.item_general_type_tv_content)
        TextView itemGeneralTypeTvContent;
        @BindView(R.id.item_general_type_imv_arrow)
        ImageView itemGeneralTypeImvArrow;
        @BindView(R.id.item_general_type_view_divider)
        View itemGeneralTypeViewDivider;
        @BindView(R.id.item_general_type_ll_root)
        LinearLayout itemGeneralTypeLlRoot;

        MatunFireGeneralHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class MatunFireTitleHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.item_title_type_tv_title)
        TextView itemTitleTypeTvTitle;
        public MatunFireTitleHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
