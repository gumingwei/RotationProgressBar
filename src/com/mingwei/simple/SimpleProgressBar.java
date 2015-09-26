package com.mingwei.simple;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

public class SimpleProgressBar extends ProgressBar {

	public SimpleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initDrawable(context, attrs, defStyleAttr);
	}

	public SimpleProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SimpleProgressBar(Context context) {
		this(context, null);
	}

	private void initDrawable(Context context, AttributeSet attrs, int defStyleAttr) {
		if (isInEditMode()) {
			setIndeterminateDrawable(new SimpleProgressDrawable.Build().builder());
		}
		Resources resources = getResources();
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RotationProgressBar, defStyleAttr, 0);
		int color = array.getColor(R.styleable.RotationProgressBar_rpb_color, Color.RED);
		setIndeterminateDrawable(new SimpleProgressDrawable.Build().setColor(color).setSweepSpeed(0.5f).setRotationSpeed(0.5f)
				.setSweepMin(20).setStroke(4).setSweepMax(300).builder());
	}
}
