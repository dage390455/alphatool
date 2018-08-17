package com.sensoro.loratool.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.text.TextUtils;

import com.sensoro.loratool.R;
import com.sensoro.loratool.base.BasePresenter;
import com.sensoro.loratool.imainview.IPointDeployFragmentView;
import com.sensoro.loratool.utils.LogUtils;

import java.io.IOException;


import static android.content.Context.VIBRATOR_SERVICE;

public class PointDeployFragmentPresenter extends BasePresenter<IPointDeployFragmentView> implements
        MediaPlayer.OnErrorListener {
    private Activity mContext;
    private static final float BEEP_VOLUME = 0.10f;
    private MediaPlayer mediaPlayer;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        mediaPlayer = buildMediaPlayer(mContext);
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void openSNTextAc() {
//        Intent intent = new Intent(mContext, DeployManualActivity.class);
//        intent.putExtra(EXTRA_IS_STATION_DEPLOY, false);
//        getView().startAC(intent);
    }

    private void scanFinish(final String scanSerialNumber) {
        getView().showProgressDialog();
//        RetrofitServiceHelper.INSTANCE.getDeviceDetailInfoList(scanSerialNumber.toUpperCase(), null, 1).subscribeOn
//                (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceInfoListRsp>() {
//            @Override
//            public void onCompleted() {
//                getView().dismissProgressDialog();
//            }
//
//            @Override
//            public void onNext(DeviceInfoListRsp deviceInfoListRsp) {
//                refresh(scanSerialNumber, deviceInfoListRsp);
//            }
//
//            @Override
//            public void onErrorMsg(int errorCode, String errorMsg) {
//                getView().dismissProgressDialog();
//                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
//                    getView().toastShort(errorMsg);
//                    getView().startScan();
//                } else if (errorCode == 4013101 || errorCode == 4000013) {
//                    freshError(scanSerialNumber, null);
//                } else {
//                    freshError(scanSerialNumber, errorMsg);
//                }
//            }
//        });
    }

    private void freshError(String scanSN, String errorInfo) {
        //
//        Intent intent = new Intent();
//        intent.setClass(mContext, DeployResultActivity.class);
//        intent.putExtra(EXTRA_SENSOR_RESULT, -1);
//        intent.putExtra(EXTRA_SENSOR_SN_RESULT, scanSN);
//        intent.putExtra(EXTRA_IS_STATION_DEPLOY, false);
//        if (!TextUtils.isEmpty(errorInfo)) {
//            intent.putExtra(EXTRA_SENSOR_RESULT_ERROR, errorInfo);
//        }

//        getView().startAC(intent);
    }

    private void refresh(String scanSN/*, DeviceInfoListRsp response*/) {
        try {
//            Intent intent = new Intent();
//            if (response.getData().size() > 0) {
//                intent.setClass(mContext, DeployActivity.class);
//                intent.putExtra(EXTRA_DEVICE_INFO, response.getData().get(0));
//                intent.putExtra(EXTRA_IS_STATION_DEPLOY, false);
//                intent.putExtra("uid", mContext.getIntent().getStringExtra("uid"));
//                getView().startAC(intent);
//            } else {
//                freshError(scanSN, null);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) mContext.getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(200);
        }
    }

    private void playVoice() {
        vibrate();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void processResult(String result) {
        playVoice();
//        int currentIndex = ((MainActivity) mContext).getSensoroPager().getCurrentItem();
//        System.out.println("currentIndex==>" + currentIndex);
//        if (currentIndex != 3) {
//            return;
//        }
        if (TextUtils.isEmpty(result)) {
//            getView().toastShort(mContext.getResources().getString(R.string.scan_failed));
            getView().startScan();
            return;
        }
        String scanSerialNumber = parseResultMac(result);
        if (scanSerialNumber == null) {
//            getView().toastShort(mContext.getResources().getString(R.string.invalid_qr_code));
            getView().startScan();
        } else {
            if (scanSerialNumber.length() == 16) {
                scanFinish(scanSerialNumber);
            } else {
//                getView().toastShort(mContext.getResources().getString(R.string.invalid_qr_code));
                getView().startScan();
            }
        }
    }

    private String parseResultMac(String result) {

        String serialNumber = null;
        if (result != null) {
            String[] data;
            String type;
            data = result.split("\\|");
            // if length is 2, it is fault-tolerant hardware.
            type = data[0];
//            if (type.length() == 2) {
//                serialNumber = data[1];
//            } else {
//                serialNumber = data[0].substring(data[0].length() - 12);
//            }
            serialNumber = type;
        }
        return serialNumber;
    }

    private MediaPlayer buildMediaPlayer(Context activity) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try (AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.beep)) {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(false);
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
            return mediaPlayer;
        } catch (IOException ioe) {
            LogUtils.loge(this, ioe.getMessage());
            mediaPlayer.release();
            return null;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
}
