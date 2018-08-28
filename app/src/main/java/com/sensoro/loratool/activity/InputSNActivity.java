package com.sensoro.loratool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sensoro.loratool.R;
import com.sensoro.loratool.base.BaseActivity;
import com.sensoro.loratool.imainview.IInputSNActivityView;
import com.sensoro.loratool.presenter.InputSNActivityPresenter;
import com.sensoro.loratool.utils.ProgressUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InputSNActivity extends BaseActivity<IInputSNActivityView, InputSNActivityPresenter>
        implements IInputSNActivityView, TextWatcher {
    @BindView(R.id.ac_input_sn_close)
    ImageView acInputSnClose;
    @BindView(R.id.sensor_deploy_title_layout)
    RelativeLayout sensorDeployTitleLayout;
    @BindView(R.id.ac_input_sn_et)
    EditText acInputSnEt;
    @BindView(R.id.ac_input_sn_iv)
    ImageView acInputSnIv;
    @BindView(R.id.ac_input_sn_btn)
    Button acInputSnBtn;
    private ProgressUtils mProgressUtils;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_input_sn);
        ButterKnife.bind(mActivity);
        mPresenter.initData(mActivity);
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());
        acInputSnEt.addTextChangedListener(this);
    }

    @Override
    protected InputSNActivityPresenter createPresenter() {
        return new InputSNActivityPresenter();
    }


    @OnClick({R.id.ac_input_sn_close, R.id.ac_input_sn_iv, R.id.ac_input_sn_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ac_input_sn_close:
                finish();
                break;
            case R.id.ac_input_sn_iv:
                acInputSnEt.setText("");
                break;
            case R.id.ac_input_sn_btn:
                String s = acInputSnEt.getText().toString();
                mPresenter.clickBtn(s);
                break;
        }
    }

    @Override
    public void startAc(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 0) {
            acInputSnIv.setVisibility(View.GONE);
            acInputSnBtn.setBackground(getResources().getDrawable(R.drawable.shape_button_normal));
        } else {
            acInputSnIv.setVisibility(View.VISIBLE);
            acInputSnBtn.setBackground(getResources().getDrawable(R.drawable.shape_button));
        }
    }

    @Override
    protected void onDestroy() {
        mProgressUtils.destroyProgress();
        super.onDestroy();
    }


    @Override
    public void toastShort(String msg) {
        Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {

    }

    @Override
    public void showProgressDialog() {
        mProgressUtils.showProgress();
    }

    @Override
    public void dismissProgressDialog() {
        mProgressUtils.dismissProgress();
    }
}
