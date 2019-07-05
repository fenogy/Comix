package io.fenogy.comix.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.support.v7.widget.AppCompatImageView;

public class CoverImageView extends AppCompatImageView {

    public CoverImageView(Context context) {
        super(context);
    }

    public CoverImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width * 4 / 3);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        scale();
    }

    private void scale() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            double ratio = (double)height/(double)width;
            if (1.2 <= ratio && ratio <= 1.6) {
                setScaleType(ScaleType.CENTER_CROP);
            }
            else {
                setScaleType(ScaleType.FIT_CENTER);
            }
        }
    }
}
