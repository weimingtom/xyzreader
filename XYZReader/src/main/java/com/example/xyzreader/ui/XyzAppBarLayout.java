package com.example.xyzreader.ui;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;

/**
 * Created by rajsettipalli on 19/01/2016.
 */
public class XyzAppBarLayout extends AppBarLayout {

    private float           mAspectRatio = 1.0f;

    public XyzAppBarLayout(Context context) {
        super(context);
    }

    public XyzAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAspectRatio(float aspectRatio) {

        mAspectRatio = aspectRatio;
        requestLayout();

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = Math.round(MeasureSpec.getSize(widthMeasureSpec) / mAspectRatio);
        int heightForAspect = MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY);


        super.onMeasure(widthMeasureSpec, heightForAspect);


    }

}
