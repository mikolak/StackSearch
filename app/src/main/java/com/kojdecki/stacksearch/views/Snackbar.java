package com.kojdecki.stacksearch.views;

import android.content.Context;
import android.os.Parcelable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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

    private static Stack<SnackbarParams> sQueue = new Stack<SnackbarParams>();
    private static Context sContext = null;

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

    private Snackbar(Context context, SnackbarParams params) {
        super(context);
        mParams = params;
        mDetector = new GestureDetector(getContext(), new mListener());
        mContent = inflate(context, R.layout.snackbar, null);
        addView(mContent);

        ((TextView) findViewById(R.id.snackbar_text)).setText(mParams.mMessage);
        Button button = (Button) findViewById(R.id.snackbar_button);
        button.setText(mParams.mAction);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        button.setOnClickListener(mParams.mListener);
    }

    private void hide() {
        //TODO animation
        //TODO remove this view from parent
    }

    public static void make(Context context, String message, String action, int duration, OnClickListener listener) {
        if (!context.equals(sContext)) {
            sQueue.clear();
            sContext = context;
        }
        sQueue.push(new SnackbarParams(message, action, duration, listener));
    }

    public static void show() {
        SnackbarParams snackbarParams = sQueue.pop();
        Snackbar snackbar = new Snackbar(sContext, snackbarParams);
        //TODO show snackbar
    }

    private void dismissRight() {
        //TODO animation
    }

    private void dismissLeft() {
        //TODO animation
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
