package com.sensoro.loratool.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sensoro.lora.setting.server.ILoRaSettingServer;
import com.sensoro.lora.setting.server.bean.StationListRsp;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.AdvanceSettingStationActivity;
import com.sensoro.loratool.activity.SearchStationActivity;
import com.sensoro.loratool.activity.SettingStationActivity;
import com.sensoro.loratool.adapter.StationInfoAdapter;
import com.sensoro.libbleserver.ble.SensoroStation;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.widget.SensoroEditText;
import com.sensoro.station.communication.bean.StationInfo;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import static android.view.View.GONE;

/**
 * Created by sensoro on 16/8/18.
 */

public class StationFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static final String INPUT = "INPUT";
    private static final int DOWN = 0;
    private static final int UP = 1;
    private Context mContext;
    private LoRaSettingApplication loRaSettingApplication;
    private PullToRefreshListView mPtrListView;
    private StationInfoAdapter mStationInfoAdapter = null;
    private SensoroStation targetSensoroStation = null;
    private StationInfo targetStationInfo = null;
    private ProgressDialog progressDialog;
    private PopupWindow mPopupWindow;
    private View mPopupView;
    private ImageView configImageView;
    private ImageView cloudImageView;
    private ImageView upgradeImageView;
    private ImageView signalImageView;
    private ImageView clearImageView;
    private LinearLayout configLayout;
    private LinearLayout cloudLayout;
    private LinearLayout upgradeLayout;
    private LinearLayout signalLayout;
    private LinearLayout clearLayout;
//    public ArrayList<StationInfo> mStationInfoList = new ArrayList<>();

    public static StationFragment newInstance(String input) {
        StationFragment stationFragment = new StationFragment();
        Bundle args = new Bundle();
        args.putString(INPUT, input);
        stationFragment.setArguments(args);
        return stationFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onPageStart("基站列表");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station, container, false);
        init(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this.getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this.getContext());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void init(View view) {
        loRaSettingApplication = (LoRaSettingApplication) this.getActivity().getApplication();
        progressDialog = new ProgressDialog(mContext);
        ViewGroup searchLayout = (ViewGroup) LayoutInflater.from(this.getActivity()).inflate(R.layout.layout_search, null);
        SensoroEditText mSearchEditText = (SensoroEditText) searchLayout.findViewById(R.id.et_head_search);
        mSearchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent testIntent = new Intent(getActivity(), SearchStationActivity.class);
//                testIntent.putParcelableArrayListExtra(Constants.EXTRA_NAME_STATION_LIST, mStationInfoList);//trasaction too large
                startActivity(testIntent);
            }
        });
        mPtrListView = (PullToRefreshListView) view.findViewById(R.id.station_ptr_list);
        mPtrListView.getRefreshableView().addHeaderView(searchLayout);
        mPtrListView.getRefreshableView().setHeaderDividersEnabled(false);
        mStationInfoAdapter = new StationInfoAdapter(mContext);
        mPtrListView.setAdapter(mStationInfoAdapter);
        mPtrListView.setOnItemClickListener(this);
        mPtrListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestStationList(DOWN);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestStationList(UP);
            }
        });
        initPopupWindow();
        requestStationList(DOWN);
    }

    private void initPopupWindow() {

        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        mPopupView = inflater.inflate(R.layout.menu_bottom_view, null);
        configImageView = (ImageView) mPopupView.findViewById(R.id.menu_iv_config);
        cloudImageView = (ImageView) mPopupView.findViewById(R.id.menu_iv_cloud);
        upgradeImageView = (ImageView) mPopupView.findViewById(R.id.menu_iv_upgrade);
        signalImageView = (ImageView) mPopupView.findViewById(R.id.menu_iv_signal);
        clearImageView = (ImageView) mPopupView.findViewById(R.id.menu_iv_clear);
        configLayout = (LinearLayout) mPopupView.findViewById(R.id.menu_ll_config);
        cloudLayout = (LinearLayout) mPopupView.findViewById(R.id.menu_ll_cloud);
        upgradeLayout = (LinearLayout) mPopupView.findViewById(R.id.menu_ll_upgrade);
        signalLayout = (LinearLayout) mPopupView.findViewById(R.id.menu_ll_signal);
        clearLayout = (LinearLayout) mPopupView.findViewById(R.id.menu_ll_clear);
        configImageView.setOnClickListener(this);
        cloudImageView.setOnClickListener(this);
        mPopupView.setFocusableInTouchMode(true);
        upgradeImageView.setVisibility(GONE);
        signalImageView.setVisibility(GONE);
        clearImageView.setVisibility(GONE);

        upgradeLayout.setVisibility(GONE);
        clearLayout.setVisibility(GONE);
        signalLayout.setVisibility(GONE);
        if (!Constants.permission[0]) {
            configLayout.setVisibility(GONE);
        } else {
            configLayout.setVisibility(View.VISIBLE);
        }
        if (!Constants.permission[2]) {
            cloudLayout.setVisibility(GONE);
        } else {
            cloudLayout.setVisibility(View.VISIBLE);
        }
        mPopupWindow = new PopupWindow(mPopupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setAnimationStyle(R.style.menuAnimationFade);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        ImageView closeIV = (ImageView) mPopupView.findViewById(R.id.menu_iv_close);
        closeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
        mPopupView.setAlpha(0.8f);

    }

    private void changePopupWindowState() {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        } else {
            mPopupWindow.showAtLocation(mPtrListView, Gravity.BOTTOM, 0, 0);
        }
    }

    public void request() {
        loRaSettingApplication.loRaSettingServer.stopAllRequest();
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(getResources().getString(R.string.tips_loading_station_data));
        progressDialog.show();
        requestStationList(DOWN);
    }

    private void requestStationList(int direction) {
        switch (direction) {
            case DOWN:
                ILoRaSettingServer loRaSettingServer = loRaSettingApplication.loRaSettingServer;
                loRaSettingServer.stationList(new Response.Listener<StationListRsp>() {
                    @Override
                    public void onResponse(final StationListRsp response) {
                        progressDialog.dismiss();
                        mPtrListView.onRefreshComplete();
                        mStationInfoAdapter.clearCache();
                        loRaSettingApplication.getStationInfoList().clear();
                        ArrayList stationInfoList = (ArrayList) response.getResult();
                        if (stationInfoList.size() == 0) {
                            Toast.makeText(mContext, R.string.tips_no_station, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        loRaSettingApplication.getStationInfoList().addAll(stationInfoList);
                        mStationInfoAdapter.appendData(stationInfoList);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        error.printStackTrace();
                        Toast.makeText(mContext, R.string.tips_network_error, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case UP:
                break;
        }
    }


    public void refreshNew( SensoroStation sensoroStation) {
        if (mStationInfoAdapter != null) {
            mStationInfoAdapter.refreshNew( sensoroStation, false);
        }
    }

    public void refreshGone(SensoroStation sensoroStation) {
        if (mStationInfoAdapter != null) {
            mStationInfoAdapter.refreshGone(sensoroStation, false);
        }
    }

    private void loadFilterData() {
        mStationInfoAdapter.filter();
    }

    public void filterStation() {
        loadFilterData();
    }

    public void cloud() {
        Intent intent = new Intent(mContext, AdvanceSettingStationActivity.class);
        intent.putExtra(Constants.EXTRA_NAME_STATION_TYPE, targetStationInfo.getDeviceType());
        intent.putExtra(Constants.EXTRA_NAME_STATION, targetSensoroStation);
        startActivity(intent);
    }

    public void config() {
        Intent intent = new Intent(mContext, SettingStationActivity.class);
        intent.putExtra(Constants.EXTRA_NAME_STATION, targetSensoroStation);
        intent.putExtra(Constants.EXTRA_NAME_STATION_TYPE, targetStationInfo.getDeviceType());
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_iv_cloud:
                cloud();
                mPopupWindow.dismiss();
                break;
            case R.id.menu_iv_config:
                config();
                mPopupWindow.dismiss();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int index = position - 2;
        if (index >= 0) {
            targetStationInfo = mStationInfoAdapter.getItem(index);

            if (targetStationInfo != null && mStationInfoAdapter.getNearByStationMap().containsKey(targetStationInfo.getSys().getSn())) {
                targetSensoroStation = mStationInfoAdapter.getNearByStationMap().get(targetStationInfo.getSys().getSn());
                targetSensoroStation.setPwd(targetStationInfo.getStation_pwd());
                changePopupWindowState();
            } else {
                Toast.makeText(mContext, R.string.tips_closeto_station, Toast.LENGTH_SHORT).show();
            }
        }


    }
}
