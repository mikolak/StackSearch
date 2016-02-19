package com.kojdecki.stacksearch.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by calot on 2/19/16.
 */
public class CircleImageView extends ImageView {
//    private Paint mAlphaPaint = null;

    public CircleImageView(Context context) {
        super(context);
//        init();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        init();
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        init();
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
//        init();
    }

    /*private void init() {
        mAlphaPaint = new Paint();
        mAlphaPaint.setARGB(0, 0, 0, 0);
        mAlphaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }*/

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
            Bitmap result = bitmapDrawable.getBitmap().copy(bitmapDrawable.getBitmap().getConfig(), true);

            int diameter = Math.min(result.getWidth(), result.getHeight());
            int x_c = result.getWidth()/2;
            int y_c = result.getHeight()/2;

            result.setHasAlpha(true);
            for (int x = 0; x < diameter; x++)
                for (int y = 0; y < diameter; y++) {
                    if ((x - x_c) * (x - x_c) + (y - y_c) * (y - y_c) > (diameter/2) * (diameter/2))
                        result.setPixel(x, y, Color.parseColor("#00000000"));
                }

            drawable = new BitmapDrawable(getResources(), result);
        }

        super.setImageDrawable(drawable);
    }
}
