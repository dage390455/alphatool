package com.sensoro.loratool.base;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.sensoro.loratool.R;
import com.sensoro.loratool.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * @author DDONG
 * @date 2018/2/4 0004
 */

public abstract class BaseActivity<V, P extends BasePresenter<V>> extends AppCompatActivity {
//    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
//    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
    /**
     * 代理者
     */
    protected P mPrestener;
    /**
     * 主AC
     */
    protected BaseActivity mActivity;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.MyTheme);
        super.onCreate(savedInstanceState);
        mPrestener = createPresenter();
        mPrestener.attachView((V) this);
        V view = mPrestener.getView();
        if (view instanceof BaseActivity) {
            mActivity = (BaseActivity) view;
        } else {
            LogUtils.loge(this, "当前View转换异常！");
            mActivity = this;
        }
        //取消bar
        ActionBar supportActionBar = mActivity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
//        CustomDensityUtils.SetCustomDensity(this, SensoroCityApplication.getInstance());
        //控制顶部状态栏显示
//        StatusBarCompat.setStatusBarColor(this);
        onCreateInit(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MobclickAgent.onPageStart(mActivity.getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        MobclickAgent.onPageEnd(mActivity.getClass().getSimpleName());
    }

    /**
     * 抽象方法初始化View
     */
    protected abstract void onCreateInit(Bundle savedInstanceState);


    @Override
    protected void onDestroy() {
        mPrestener.onDestroy();
        mPrestener.detachView();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
//        final NotificationManagerCompat manager = NotificationManagerCompat.from(mActivity);
//        boolean isOpened = manager.areNotificationsEnabled();
//        if (!isNotificationEnabled(mActivity) && !isOpened) {
//            showRationaleDialog();
//        }
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    /**
     * 创建代理者
     *
     * @return
     */
    protected abstract P createPresenter();

    /**
     * 判断底部是否有导航栏
     * TODO 最好抽取工具类
     *
     * @return
     */
    public boolean checkDeviceHasNavigationBar() {
        final WindowManager windowManager = mActivity.getWindowManager();
        final Display d = windowManager.getDefaultDisplay();

        final DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics);
        }

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }
    //    /**
//     * 检查通知权限
//     *
//     * @param context
//     * @return
//     */
//    @SuppressLint("NewApi")
//    private boolean isNotificationEnabled(Context context) {
//
//        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
//        ApplicationInfo appInfo = context.getApplicationInfo();
//        String pkg = context.getApplicationContext().getPackageName();
//        int uid = appInfo.uid;
//
//        Class appOpsClass = null;
//        /* Context.APP_OPS_MANAGER */
//        try {
//            appOpsClass = Class.forName(AppOpsManager.class.getName());
//            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
//                    String.class);
//            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
//
//            int value = (Integer) opPostNotificationValue.get(Integer.class);
//            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
//
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }


    //    /**
//     * 弹出声明的 Dialog
//     */
//    private void showRationaleDialog() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//        builder.setTitle("提示")
//                .setMessage("通知中包含了重要报警信息，请前往设置，打开的通知选项。")
//                .setPositiveButton("前往设置",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                //去设置界面
//                                Intent intent = new Intent();
//                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                Uri uri = Uri.fromParts("package", SensoroCityApplication.getInstance()
//                                        .getPackageName(), null);
//                                intent.setData(uri);
//                                dialog.dismiss();
//                                startActivity(intent);
//                            }
//                        })
//                .setNegativeButton("取消",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                .setCancelable(false)
//                .show();
//    }
}
