package com.example.xyzreader.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class DynamicHeightNetworkImageView extends ImageView {
    private float           mAspectRatio = 1.5f;


    public DynamicHeightNetworkImageView(Context context) {
        super(context);
    }

    public DynamicHeightNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicHeightNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
        requestLayout();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = Math.round(MeasureSpec.getSize(widthMeasureSpec) / mAspectRatio);
        int heightForAspect = MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY);


        super.onMeasure(widthMeasureSpec,heightForAspect);


    }


}
