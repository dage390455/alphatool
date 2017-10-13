package com.sensoro.loratool.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.fragment.DeviceFragment;

/**
 * Created by sensoro on 17/6/5.
 */

public class SensoroPopupView extends LinearLayout {
    private SensoroPopupViewListener mListener;
    private RelativeLayout singleLayout;
    private RelativeLayout multiLayout;
    private Context mContext;

    public SensoroPopupView(Context context) {
        super(context);
        this.mContext = context;
    }

    public SensoroPopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        View mTopPopupView = LayoutInflater.from(context).inflate(R.layout.menu_top_view, this);
        singleLayout = (RelativeLayout) mTopPopupView.findViewById(R.id.menu_rl_single);
        multiLayout = (RelativeLayout) mTopPopupView.findViewById(R.id.menu_rl_multi);
        final ImageView ivSingle = (ImageView) mTopPopupView.findViewById(R.id.menu_iv_single);
        final ImageView ivMulti = (ImageView) mTopPopupView.findViewById(R.id.menu_iv_multi);
        singleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCallBack(DeviceFragment.MODEL_SINGLE);
                ivSingle.setImageResource(R.mipmap.ic_selected);
                ivMulti.setImageResource(R.mipmap.ic_unselect);
                dismiss();
            }
        });

        multiLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCallBack(DeviceFragment.MODEL_MULTI);
                ivMulti.setImageResource(R.mipmap.ic_selected);
                ivSingle.setImageResource(R.mipmap.ic_unselect);
                dismiss();
            }
        });
    }

    public SensoroPopupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    public void registerListener(SensoroPopupViewListener listener) {
        this.mListener = listener;
    }

    public void show() {
        setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.push_menu_fadein);   //得到一个LayoutAnimationController对象；
        LayoutAnimationController controller = new LayoutAnimationController(animation);   //设置控件显示的顺序；
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);   //设置控件显示间隔时间；
        controller.setDelay(0.3f);   //为ListView设置LayoutAnimationController属性；
        this.setLayoutAnimation(controller);
        this.startLayoutAnimation();
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void dismiss() {
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.push_menu_fadeout);   //得到一个LayoutAnimationController对象；
        LayoutAnimationController controller = new LayoutAnimationController(animation);   //设置控件显示的顺序；
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);   //设置控件显示间隔时间；
        this.setLayoutAnimation(controller);
        this.startLayoutAnimation();
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mListener.onDismissAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mListener.onDismissAnimationEnd();
                setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public interface SensoroPopupViewListener {
        void onCallBack(int index);
        void onDismissAnimationStart();
        void onDismissAnimationEnd();
    }
}
