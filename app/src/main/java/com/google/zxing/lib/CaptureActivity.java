/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.lib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.lib.camera.CameraManager;
import com.google.zxing.lib.clipboard.ClipboardInterface;
import com.google.zxing.lib.history.HistoryManager;
import com.google.zxing.lib.result.ResultHandler;
import com.google.zxing.lib.result.ResultHandlerFactory;
import com.sensoro.lora.setting.server.ILoRaSettingServer;
import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.lora.setting.server.bean.DeviceInfoListRsp;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.ble.SensoroDevice;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements Constants, SurfaceHolder.Callback, View.OnClickListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    public static final String ZXING_RESULT = "ZXING_RESULT";

    public static final String ZXING_REQUEST_CODE = "ZXING_REQUEST_CODE";
    public static final int ZXING_REQUEST_CODE_SCAN_BEACON = 100;
    public static final int ZXING_REQUEST_CODE_ENTER_PWD = 101;
    public static final int ZXING_REQUEST_CODE_RESULT = 102;

    private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

    public static final int HISTORY_REQUEST_CODE = 0x0000bacc;
    public static final int PHOTO_REQUEST_CODE = 0x000000aa;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private Result savedResultToShow;
    private TextView statusView;
    private boolean hasSurface;
    private boolean copyToClipboard;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    private HistoryManager historyManager;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private AmbientLightManager ambientLightManager;

    private ImageView flashImageView;
    private boolean isFlashOn;
    private LoRaSettingApplication appliction;
    private int requestCode;
    private boolean isMulti = false;
    private Map<String,SensoroDevice> deviceMap = new HashMap<>();
    public Handler getHandler() {
        return handler;
    }

    @BindView(R.id.capture_bottom_edit)
    EditText manualEditText;
    @BindView(R.id.capture_bottom_manual_sc)
    SwitchCompat manualSwitchCompat;
    CameraManager getCameraManager() {
        return cameraManager;
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_capture);
        ButterKnife.bind(this);
        appliction = (LoRaSettingApplication) getApplication();
        flashImageView = (ImageView) findViewById(R.id.zxing_capture_iv_flash);
        flashImageView.setOnClickListener(this);

        requestCode = getIntent().getIntExtra(ZXING_REQUEST_CODE, -1);
        ArrayList<SensoroDevice> dataList = getIntent().getParcelableArrayListExtra(EXTRA_NAME_DEVICE_LIST);
        for (int i = 0; i < dataList.size(); i ++) {
            deviceMap.put(dataList.get(i).getSn(), dataList.get(i));
        }
        manualSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isMulti = b;
            }
        });
        manualEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (textView.getText().length() != 16) {
                    processResult(textView.getText().toString());
                } else {
                    Toast.makeText(appliction, "SN 格式不正确", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
        initCamera();
    }


    private void initCamera() {
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        ambientLightManager = new AmbientLightManager(this);

        PreferenceManager.setDefaultValues(this, R.xml.zxing_preferences, false);


        ImageView mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
        TranslateAnimation mAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);
        mAnimation.setDuration(1500);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        mQrLineView.setAnimation(mAnimation);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // historyManager must be initialized here to update the history preference
        historyManager = new HistoryManager(this);
        historyManager.trimHistory();

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());

        statusView = (TextView) findViewById(R.id.status_view);

        handler = null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        /** 设置横屏 **/
//        if (prefs.getBoolean(PreferencesActivity.KEY_DISABLE_AUTO_ORIENTATION, true)) {
//            setRequestedOrientation(getCurrentOrientation());
//        } else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
//        }

        resetStatusView();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }

        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);

        inactivityTimer.onResume();

        Intent intent = getIntent();

        copyToClipboard = prefs.getBoolean(PreferencesActivity.KEY_COPY_TO_CLIPBOARD, true)
                && (intent == null || intent.getBooleanExtra(Intents.Scan.SAVE_HISTORY, true));

        decodeFormats = null;
        characterSet = null;

        if (intent != null) {

            String action = intent.getAction();

            if (Intents.Scan.ACTION.equals(action)) {

                // Scan the formats the intent requested, and return the result to the calling activity.
                decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
                decodeHints = DecodeHintManager.parseDecodeHints(intent);

                if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
                    int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
                    int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
                    if (width > 0 && height > 0) {
                        cameraManager.setManualFramingRect(width, height);
                    }
                }

                if (intent.hasExtra(Intents.Scan.CAMERA_ID)) {
                    int cameraId = intent.getIntExtra(Intents.Scan.CAMERA_ID, -1);
                    if (cameraId >= 0) {
                        cameraManager.setManualCameraId(cameraId);
                    }
                }

                String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
                if (customPromptMessage != null) {
                    statusView.setText(customPromptMessage);
                }

            }

            characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

            flashImageView.setBackgroundResource(R.drawable.zxing_flash_off);
            isFlashOn = false;
        }
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        ambientLightManager.stop();
        beepManager.close();
        cameraManager.closeDriver();
        historyManager = null;
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        deviceMap.clear();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.zxing_capture, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zxing_capture_iv_flash:
                if (!isFlashOn) {
                    isFlashOn = true;
                    flashImageView.setBackgroundResource(R.drawable.zxing_flash_on);
                    cameraManager.setTorch(true);
                } else {
                    isFlashOn = false;
                    flashImageView.setBackgroundResource(R.drawable.zxing_flash_off);
                    cameraManager.setTorch(false);
                }
                break;
            default:
                break;
        }
    }


    private void decodeOrStoreSavedBitmap( Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult   The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode     A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();
        ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            historyManager.addHistoryItem(rawResult, resultHandler);
            // Then not from history, so beep/vibrate and we have an image to draw on
            beepManager.playBeepSoundAndVibrate();
            /** 将扫描图片关键信息高亮处理，如果不需要图像相关功能，可以禁用 **/
//            drawResultPoints(barcode, scaleFactor, rawResult);
        }
        /** 复制到剪切板 **/
        if (copyToClipboard && !resultHandler.areContentsSecure()) {
            CharSequence displayContents = resultHandler.getDisplayContents();
            ClipboardInterface.setText(displayContents, this);
        }
        /** 连续扫描 **/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (fromLiveScan && prefs.getBoolean(PreferencesActivity.KEY_BULK_MODE, false)) {
            // Wait a moment or else it will scan the same barcode continuously about 3 times
            restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
        }
        /** 自动打开网页 **/
        if (resultHandler.getDefaultButtonID() != null && prefs.getBoolean(PreferencesActivity.KEY_AUTO_OPEN_WEB, false)) {
            resultHandler.handleButtonPress(resultHandler.getDefaultButtonID());
            return;
        }

        processResultCustome(rawResult.getText());
    }

    @OnClick(R.id.capture_close)
    public void close() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NAME_DEVICE_LIST, new ArrayList<>(deviceMap.values()));
        setResult(ZXING_REQUEST_CODE_RESULT, intent);
        finish();
    }

    private void requestDeviceWithSearch(final String sn) {
        ILoRaSettingServer loRaSettingServer = appliction.loRaSettingServer;
        loRaSettingServer.deviceList(sn, "sn", new Response.Listener<DeviceInfoListRsp>() {
            @Override
            public void onResponse(final DeviceInfoListRsp response) {
                ArrayList<DeviceInfo> searchList = (ArrayList) response.getData().getItems();
                if (searchList.size() > 0) {
                    DeviceInfo deviceInfo = searchList.get(0);
                    if (appliction.getSensoroDeviceMap().containsKey(sn)) {
                        SensoroDevice sensoroDevice = appliction.getSensoroDeviceMap().get(sn);
                        sensoroDevice.setPassword(deviceInfo.getPassword());
                        sensoroDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
                        sensoroDevice.setHardwareVersion(deviceInfo.getDeviceType());
                        sensoroDevice.setBand(deviceInfo.getBand());
                        if (isSupportDevice(sensoroDevice)) {
                            if (!deviceMap.containsKey(sn)) {
                                deviceMap.put(sn, sensoroDevice);
                                Toast.makeText(appliction, "设备添加成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(appliction, "设备已存在升级队列中", Toast.LENGTH_SHORT).show();
                            }
                            if (!isMulti) {
                                Intent intent = new Intent();
                                intent.putExtra(EXTRA_NAME_DEVICE_LIST, new ArrayList<>(deviceMap.values()));
                                setResult(ZXING_REQUEST_CODE_RESULT, intent);
                                finish();
                            }
                        }
                    } else {
                        Toast.makeText(appliction, "未找到设备在附近", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(appliction, R.string.beacon_not_found, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(appliction, R.string.beacon_not_found, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean isSupportDevice(SensoroDevice device) {
        boolean isSame = false;
        for (String key : deviceMap.keySet()) {
            SensoroDevice sensoroDevice = deviceMap.get(key);
            System.out.println("sensorodevice.hw=>" + sensoroDevice.getHardwareVersion());
            System.out.println("device.hw=>" + device.getHardwareVersion());
            System.out.println("sensorodevice.fw=>" + sensoroDevice.getHardwareVersion());
            System.out.println("device.fw=>" + device.getHardwareVersion());
            if (sensoroDevice.getHardwareVersion().equalsIgnoreCase(device.getHardwareVersion())) {
                if (sensoroDevice.getFirmwareVersion().equalsIgnoreCase(device.getFirmwareVersion())) {
                    if (sensoroDevice.getBand().equalsIgnoreCase(device.getBand())) {
                        isSame = true;
                    } else {
                        Toast.makeText(this, R.string.tips_same_band, Toast.LENGTH_SHORT).show();
                        isSame = false;
                    }
                } else {
                    Toast.makeText(this, R.string.tips_same_firmware, Toast.LENGTH_SHORT).show();
                    isSame = false;
                }
            } else {
                Toast.makeText(this, R.string.tips_same_hardware, Toast.LENGTH_SHORT).show();
                isSame = false;
            }
        }
        return isSame;
    }

    private void processResult(String sn) {
        if (appliction.getSensoroDeviceMap().containsKey(sn)) {
            List<DeviceInfo> deviceInfoList = appliction.getDeviceInfoList();
            boolean isFind = false;
            for (int i = 0 ; i <deviceInfoList.size(); i++) {
                DeviceInfo deviceInfo = deviceInfoList.get(i);
                if (deviceInfo.getSn().equalsIgnoreCase(sn)) {
                    isFind = true;
                    SensoroDevice sensoroDevice = appliction.getSensoroDeviceMap().get(sn);
                    sensoroDevice.setPassword(deviceInfo.getPassword());
                    sensoroDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
                    sensoroDevice.setHardwareVersion(deviceInfo.getDeviceType());
                    sensoroDevice.setBand(deviceInfo.getBand());
                    if (isSupportDevice(sensoroDevice)) {
                        if (!deviceMap.containsKey(sn)) {
                            Toast.makeText(appliction, "设备添加成功", Toast.LENGTH_SHORT).show();
                            deviceMap.put(sn, sensoroDevice);
                        } else {
                            Toast.makeText(appliction, "设备已存在升级队列中", Toast.LENGTH_SHORT).show();
                        }
                        if (!isMulti) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_NAME_DEVICE_LIST, new ArrayList<>(deviceMap.values()));
                            setResult(ZXING_REQUEST_CODE_RESULT, intent);
                            finish();
                        }
                    }

                    break;
                }
            }
            if (!isFind) {
                requestDeviceWithSearch(sn);
            }
        } else {
            Toast.makeText(appliction, "未找到设备在附近", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * process result
     */
    private void processResultCustome(String result) {
        if (TextUtils.isEmpty(result)) {
            Toast.makeText(CaptureActivity.this, R.string.scan_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == ZXING_REQUEST_CODE_SCAN_BEACON) {
            String scanSerialNumber = parseResultMac(result);
            if (scanSerialNumber == null) {
                // QRCode is not Yunzi
                Toast.makeText(appliction, R.string.qr_not_lora_station, Toast.LENGTH_SHORT).show();
            } else {
                processResult(scanSerialNumber);
            }
        } else if (requestCode == ZXING_REQUEST_CODE_ENTER_PWD) {
            byte[] password = parseResultResult(result);
            Intent intent = new Intent();
            intent.putExtra(ZXING_RESULT, password);
            setResult(ZXING_REQUEST_CODE_ENTER_PWD, intent);
            finish();
        } else if (requestCode == ZXING_REQUEST_CODE_RESULT) {
            Intent intent = new Intent();
            intent.putExtra(ZXING_RESULT, result);
            setResult(ZXING_REQUEST_CODE_RESULT, intent);
            finish();
        }
    }

    private String parseResultMac(String result) {
        String serialNumber = null;
        if (result != null) {
            String[] data = null;
            data = result.split("\\|");
            // if length is 2, it is fault-tolerant hardware.
            serialNumber = data[0];

        }
        return serialNumber;
    }

    private byte[] parseResultResult(String result) {
        String token = null;
        String mac = null;
        String[] data = null;
        data = result.split("\\|");
        // if length is 2, it is fault-tolerant hardware.
        String type = data[0];
        if (type.length() == 2) {
            mac = data[1];
            token = data[4];
            if (type.equalsIgnoreCase(Constants.TYPE_DEV)) {
                token = Constants.DEFAULT_PASSWORD;
            }
            if (type.equalsIgnoreCase(Constants.TYPE_BUS)) {
            }
        } else {
            mac = data[0];
            token = data[3];
            String macType = mac.substring(6, 7);
            if (Integer.valueOf(macType) % 2 != 0) {
                token = Constants.DEFAULT_PASSWORD;
            } else if (Integer.valueOf(macType) % 2 == 0) {
            }
        }

        byte[] pwd = null;
        if (!token.equals(Constants.DEFAULT_PASSWORD)) {
            pwd = Utils.getSignature(token.getBytes(), Constants.BASE_KEY);
//			byte[] temp2 = pwd = Utils.getSignature(pwd, Constants.BASE_KEY2);
//			Log.v("zwz", temp2.toString());
        }
        return pwd;
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap(null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    private void resetStatusView() {
        statusView.setText(R.string.msg_default_status);
        statusView.setVisibility(View.VISIBLE);
    }
}
