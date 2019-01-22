package com.sensoro.loratool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.MatunFireAdapter;
import com.sensoro.loratool.base.BaseActivity;
import com.sensoro.loratool.imainview.IChannelEditorActivityView;
import com.sensoro.loratool.model.SettingDeviceModel;
import com.sensoro.loratool.presenter.ChannelEditorPresenter;
import com.sensoro.loratool.widget.AlphaToast;
import com.sensoro.loratool.widget.SettingEnterDialogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChannelEditorActivity extends BaseActivity<IChannelEditorActivityView, ChannelEditorPresenter>
        implements IChannelEditorActivityView {
    @BindView(R.id.include_top_title_imv_finish)
    ImageView includeTopTitleImvFinish;
    @BindView(R.id.include_top_title_tv_title)
    TextView includeTopTitleTvTitle;
    @BindView(R.id.include_top_title_tv_subtitle)
    TextView includeTopTitleTvSubtitle;
    @BindView(R.id.ac_channel_editor_rc)
    RecyclerView acChannelEditorRc;
    private MatunFireAdapter matunFireAdapter;
    private SettingEnterDialogUtils mEnterDialogUtils;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_channel_editor);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(this);
    }

    private void initView() {
        includeTopTitleTvTitle.setText(getString(R.string.setting_text_et_channel));

        initRC();
        initDialog();
    }

    private void initDialog() {
        mEnterDialogUtils = new SettingEnterDialogUtils(this);
    }

    private void initRC() {
        matunFireAdapter = new MatunFireAdapter(this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        acChannelEditorRc.setLayoutManager(manager);
        acChannelEditorRc.setAdapter(matunFireAdapter);
        matunFireAdapter.setOnItemClickListener(mPresenter);
    }

    @Override
    protected ChannelEditorPresenter createPresenter() {
        return new ChannelEditorPresenter();
    }


    @OnClick({R.id.include_top_title_imv_finish,R.id.include_top_title_tv_subtitle})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.include_top_title_imv_finish:
                finish();
                break;
            case R.id.include_top_title_tv_subtitle:
                mPresenter.doSave();
                break;

        }
    }

    @Override
    public void updateData(ArrayList<SettingDeviceModel> datas) {
        matunFireAdapter.updateData(datas);
    }

    @Override
    public void showDialog(SettingDeviceModel model) {
        mEnterDialogUtils.show(model.content,model.hint,mPresenter);
    }

    @Override
    public void dismissDialog() {
        mEnterDialogUtils.dismiss();
    }

    @Override
    public void notifyData() {
        matunFireAdapter.notifyDataSetChanged();
    }

    @Override
    public List<SettingDeviceModel> getData() {
        return matunFireAdapter.getData();
    }

    @Override
    public void setIntentResult(int resultCode, Intent intent) {
        setResult(resultCode,intent);
        finish();
    }

    @Override
    public SettingDeviceModel getItem(int position) {
        if(position < 0){
            return null;
        }

        return matunFireAdapter.getData().get(position);
    }

    @Override
    public void toastShort(String msg) {
        AlphaToast.INSTANCE.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {

    }
}
