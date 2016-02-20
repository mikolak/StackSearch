package com.kojdecki.stacksearch.views;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kojdecki.stacksearch.R;

import java.util.Stack;

/**
 * Created by calot on 2/18/16.
 */
public class Snackbar extends FrameLayout {
    public static int LENGTH_SHORT = 2000;
    public static int LENGTH_LONG = 4000;

    private static Stack<Snackbar> sQueue = new Stack<Snackbar>();
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
    private View mContent = null;
    private Activity mActivity = null;
    //TODO timer

    private Snackbar(Activity context, SnackbarParams params) {
        super(context);
        mParams = params;
        mActivity = context;
        mDetector = new GestureDetector(getContext(), new MyOnGestureListener());
        mContent = inflate(context, R.layout.snackbar, null);
        addView(mContent);
//        RelativeLayout.LayoutParams layoutParams
//                = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
//                RelativeLayout.LayoutParams.WRAP_CONTENT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//
//        setLayoutParams(layoutParams);
        setViewContent();
    }

    private void setViewContent() {
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
            sActive.remove();
            sQueue.clear();
        }
    }

    private void hide() {
        //TODO animation
        remove();
    }

    public static Snackbar make(Activity context, String message, String action, int duration, OnClickListener listener) {
        if (!context.equals(sContext)) {
            sQueue.clear();
            sContext = context;
        }

        Snackbar snackbar = new Snackbar(context, new SnackbarParams(message, action, duration, listener));

        return snackbar;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //TODO implementation
        mDetector.onTouchEvent(ev);
        return false;
        //return super.onInterceptTouchEvent(ev);
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

                RelativeLayout.LayoutParams layoutParams
                        = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                setLayoutParams(layoutParams);
                viewGroup.addView(this, layoutParams);

                /*WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                Point size = new Point();
                wm.getDefaultDisplay().getSize(size);
                params.y = size.y - getHeight();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.format = PixelFormat.OPAQUE;
                params.type = WindowManager.LayoutParams.TYPE_TOAST;
                params.setTitle("Snackbar");
                params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                wm.addView(this, params);
*/
                //TODO display animation
            }
        }
    }

    private void remove() {
        synchronized (Snackbar.class) {
            sShowing = false;
            sActive = null;

            ViewGroup viewGroup = (ViewGroup) ((ViewGroup) mActivity
                    .findViewById(android.R.id.content)).getChildAt(0);
            viewGroup.removeView(this);
            /*WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(this);*/
        }

        if (sQueue.size() != 0) {
            sQueue.pop().show();
        }
    }

    private void dismissRight() {
        //TODO animation
        remove();
    }

    private void dismissLeft() {
        //TODO animation
        remove();
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
