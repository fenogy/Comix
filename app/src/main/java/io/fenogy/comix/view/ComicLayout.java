package io.fenogy.comix.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;


public class ComicLayout extends LinearLayout {

    public ComicLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ComicLayout(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width *2;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
