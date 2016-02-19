package com.kojdecki.stacksearch.views;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Parcelable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
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
    //TODO timer

    private Snackbar(Context context, SnackbarParams params) {
        super(context);
        mParams = params;
        // FIXME: 2/20/16 (gestures not working)
        mDetector = new GestureDetector(getContext(), new mListener());
        mContent = inflate(context, R.layout.snackbar, null);
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

    private void hide() {
        //TODO animation
        remove();
    }

    public static Snackbar make(Context context, String message, String action, int duration, OnClickListener listener) {
        if (!context.equals(sContext)) {
            sQueue.clear();
            sContext = context;
        }

        Snackbar snackbar = new Snackbar(context, new SnackbarParams(message, action, duration, listener));

        return snackbar;
    }

    public void show() {
        synchronized (Snackbar.class) {
            if (sShowing) {
                sQueue.add(this);
            } else {
                sShowing = true;
                //TODO display

                WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                Point size = new Point();
                wm.getDefaultDisplay().getSize(size);
                params.y = size.y - getHeight();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.format = PixelFormat.OPAQUE;
                params.type = WindowManager.LayoutParams.TYPE_TOAST;
                params.setTitle("Snackbar");
                params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                        //| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                wm.addView(this, params);
            }
        }
    }

    private void remove() {
        synchronized (Snackbar.class) {
            sShowing = false;
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(this);
        }

        if (sQueue.size() != 0) {
            sQueue.pop().show();
        }
        //TODO remove view
    }

    private void dismissRight() {
        //TODO animation
        remove();
    }

    private void dismissLeft() {
        //TODO animation
        remove();
    }

    class mListener extends GestureDetector.SimpleOnGestureListener {
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
