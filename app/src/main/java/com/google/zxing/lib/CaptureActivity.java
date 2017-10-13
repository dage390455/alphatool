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
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.lib.camera.CameraManager;
import com.google.zxing.lib.clipboard.ClipboardInterface;
import com.google.zxing.lib.history.HistoryItem;
import com.google.zxing.lib.history.HistoryManager;
import com.google.zxing.lib.result.ResultHandler;
import com.google.zxing.lib.result.ResultHandlerFactory;
import com.google.zxing.lib.share.ShareActivity;

import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.utils.Utils;
import com.sensoro.station.communication.bean.StationInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener {

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
    private ArrayList<StationInfo> mStationList = new ArrayList<>();
    public Handler getHandler() {
        return handler;
    }

    CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.zxing_capture);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        flashImageView = (ImageView) findViewById(R.id.zxing_capture_iv_flash);
        flashImageView.setOnClickListener(this);

        requestCode = getIntent().getIntExtra(ZXING_REQUEST_CODE, -1);
        mStationList = getIntent().getParcelableArrayListExtra("stationList");
//        requirePermission();

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
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_share:
                intent.setClassName(this, ShareActivity.class.getName());
                startActivity(intent);
                break;
            case R.id.menu_from_photo:
                selectFromPhoto();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void selectFromPhoto() {
        Intent innerIntent = new Intent(); // "android.intent.action.GET_CONTENT"
        if (Build.VERSION.SDK_INT < 19) {
            innerIntent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }

        innerIntent.setType("image/*");

        Intent wrapperIntent = Intent.createChooser(innerIntent, getString(R.string.pick_qrpic));

        CaptureActivity.this
                .startActivityForResult(wrapperIntent, PHOTO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == HISTORY_REQUEST_CODE) {
                int itemNumber = intent.getIntExtra(Intents.History.ITEM_NUMBER, -1);
                if (itemNumber >= 0) {
                    HistoryItem historyItem = historyManager.buildHistoryItem(itemNumber);
                    decodeOrStoreSavedBitmap(historyItem.getResult());
                }
            } else {
                if (requestCode == PHOTO_REQUEST_CODE) {
                    //选择图片
                    Uri uri = intent.getData();
                    ContentResolver contentResolver = this.getContentResolver();
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    new DecodeImageAsyncTask().execute(bitmap);
                }
            }
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


    class DecodeImageAsyncTask extends AsyncTask<Bitmap, Void, String> {
        ProgressDialog decodeDialog = new ProgressDialog(CaptureActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            decodeDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度
            decodeDialog.setMessage(getString(R.string.is_decoding));
            decodeDialog.show();
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];

            Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
            Collection<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
            decodeFormats.addAll(EnumSet.of(BarcodeFormat.QR_CODE));
            hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

            Result result = null;
            try {
                result = new MultiFormatReader().decode(loadImage(bitmap, CaptureActivity.this), hints);
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (result == null) {
                return null;
            } else {
                return result.getText();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            decodeDialog.dismiss();
            if (result != null) {
                beepManager.playBeepSoundAndVibrate();

                processResultCustome(result);
            } else {
                Toast.makeText(CaptureActivity.this, getString(R.string.decode_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private BinaryBitmap loadImage(Bitmap bitmap, Context context) throws IOException {
        int lWidth = bitmap.getWidth();
        int lHeight = bitmap.getHeight();
        int[] lPixels = new int[lWidth * lHeight];
        bitmap.getPixels(lPixels, 0, lWidth, 0, 0, lWidth, lHeight);
        return new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(lWidth, lHeight, lPixels)));
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
        Toast.makeText(CaptureActivity.this, rawResult.getText(), Toast.LENGTH_SHORT).show();
        processResultCustome(rawResult.getText());
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
                boolean isFound = false;
                StationInfo stationInfo = null;
                synchronized (mStationList) {
                    for (StationInfo searchStationInfo : mStationList) {
                        if (searchStationInfo.getSys().getSn().equals(result)) {
                            isFound = true;
                            stationInfo = searchStationInfo;
                        }
                    }
                }
                if (isFound) {
                    // 进入 stationinfo 详情
//                    Intent intent = new Intent(this, StationDetailActivity.class);
//                    intent.putExtra("stationInfo", stationInfo);
//                    this.startActivity(intent);
//                    startActivity(intent);
                } else {
                    // 提示该beacon未被扫到
                    new AlertDialog.Builder(CaptureActivity.this).setMessage(getString(R.string.station_not_found)).setTitle(getString(R.string.alert_title)).setNegativeButton(getString(R.string.confirm), null).create().show();
                }
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
            String type = null;
            data = result.split("\\|");
            // if length is 2, it is fault-tolerant hardware.
            type = data[0];
            if (type.length() == 2) {
                serialNumber = data[1];
            } else {
                serialNumber = data[0].substring(data[0].length() - 12);
            }
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


    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
     *
     * @param barcode     A bitmap of the captured image.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param rawResult   The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_points));
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
            } else if (points.length == 4 &&
                    (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                            rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and metadata
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
                drawLine(canvas, paint, points[2], points[3], scaleFactor);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    if (point != null) {
                        canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
                    }
                }
            }
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
        if (a != null && b != null) {
            canvas.drawLine(scaleFactor * a.getX(),
                    scaleFactor * a.getY(),
                    scaleFactor * b.getX(),
                    scaleFactor * b.getY(),
                    paint);
        }
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
