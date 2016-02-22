package com.kojdecki.stacksearch.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kojdecki.stacksearch.R;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * * Snackbar implementation not using android.design. Requires calling activity to have Relative
 * Layout as the root view.
 *
 * Created by Mikołaj Kojdecki, 21/02/2016
 */
//should only be instantiated by calling Snackbar.make()
@SuppressLint("ViewConstructor")
public class Snackbar extends FrameLayout {
    public final static int LENGTH_SHORT = 2000;
    public final static int LENGTH_LONG = 4000;
    public final static int LENGTH_INDEFINITE = -1;

    private static LinkedList<Snackbar> sQueue = new LinkedList<>();
    private static Context sContext = null;
    private static boolean sShowing = false;
    private static Snackbar sActive = null;

    static class SnackbarParams {
        String mMessage;
        String mAction;
        int mDuration;
        OnClickListener mListener = null;

        public SnackbarParams(String mMessage, String mAction, int mDuration, OnClickListener listener) {
            this.mMessage = mMessage;
            this.mAction = mAction;
            this.mDuration = mDuration;
            this.mListener = listener;
        }
    }

    private GestureDetector mDetector = null;
    private SnackbarParams mParams = null;
    private Activity mActivity = null;
    private Handler mHandler = null;
    private Runnable mAction = null;

    private Snackbar(Activity context, SnackbarParams params) {
        super(context);
        mParams = params;
        mActivity = context;

        mDetector = new GestureDetector(getContext(), new MyOnGestureListener());
        mHandler = new Handler();

        RelativeLayout.LayoutParams layoutParams
                = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        setLayoutParams(layoutParams);

        setViewContent();
    }

    private void setTimer() {
        if (mParams.mDuration != LENGTH_INDEFINITE) {
            mAction = new Runnable() {
                @Override
                public void run() {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hide();
                        }
                    });
                }
            };

            mHandler.postDelayed(mAction, mParams.mDuration);
        }
    }

    private void cancelTimer() {
        if (mHandler != null && mAction != null) {
            mHandler.removeCallbacks(mAction);
        }
    }

    private void setViewContent() {
        View mContent = inflate(mActivity, R.layout.snackbar, null);
        addView(mContent);

        ((TextView) findViewById(R.id.snackbar_text)).setText(mParams.mMessage);
        TextView button = (TextView) findViewById(R.id.snackbar_button);
        button.setText(mParams.mAction.toUpperCase());
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        if (mParams.mListener != null) {
            button.setOnClickListener(mParams.mListener);
        }
    }

    /**
     * Should only be called when Activity is deactivated (e.g. in onPause()).
     * Removes Snackbar and clears queue.
     */
    public static void cancel() {
        if (sActive != null) {
            synchronized (Snackbar.class) {
                sQueue.clear();
                sActive.remove();
            }
        }
    }

    private void hide() {
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.snackbar_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //not needed
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        remove();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        startAnimation(animation);
    }

    public static Snackbar make(Activity context, String message, String action, int duration, OnClickListener listener) {
        if (!context.equals(sContext)) {
            sQueue.clear();
            sContext = context;
        }

        return new Snackbar(context, new SnackbarParams(message, action, duration, listener));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mDetector.onTouchEvent(ev);
        return false;
    }

    public void show() {
        synchronized (Snackbar.class) {
            if (sShowing) {
                sQueue.add(this);
            } else {
                sShowing = true;
                sActive = this;

                ViewGroup viewGroup = (ViewGroup) ((ViewGroup) mActivity
                        .findViewById(android.R.id.content)).getChildAt(0);

                viewGroup.addView(this, getLayoutParams());
                Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.snackbar_in);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        setTimer();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                startAnimation(animation);
            }
        }
    }

    private void remove() {
        synchronized (Snackbar.class) {
            sShowing = false;
            sActive = null;

            cancelTimer();

            ViewGroup viewGroup = (ViewGroup) ((ViewGroup) mActivity
                    .findViewById(android.R.id.content)).getChildAt(0);
            viewGroup.removeView(this);
        }

        if (sQueue.size() != 0) {
            sQueue.pop().show();
        }
    }

    private void dismissRight() {
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.snackbar_out_right);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //not needed
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        remove();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        startAnimation(animation);
    }

    private void dismissLeft() {
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.snackbar_out_left);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //not needed
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        remove();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        startAnimation(animation);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
        //return super.onTouchEvent(event);
    }

    class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                if (velocityX > 0)
                    dismissRight();
                else
                    dismissLeft();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        sContext = null;
        sQueue.clear();
        return super.onSaveInstanceState();
    }
}
