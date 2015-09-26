package com.mingwei.simple;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * google play的旋转progressBar 效果
 * 
 * @author mingwei
 * 
 */
public class SimpleProgressDrawable extends Drawable implements Animatable {

	public static final String TAG = "SimpleProgressDrawable";

	private RectF mRectF = null;
	private Paint mPaint;
	private int mColor = Color.RED;
	private int mStrokeWidth = 10;

	private ValueAnimator mRotationAnimator;
	private ValueAnimator mSweepAppearAnimator;
	private ValueAnimator mSweepDisAppearAnimator;
	private ValueAnimator mEndAnimator;

	private int mRotationDuration = 2000;
	private int mSweepDuration = 600;
	private int mEndDuration = 200;
	private float mSweepSpeed = 1f;
	private float mRotationSpeed = 1f;
	private float mSweepAngleMax = 300;
	private float mSweepAngleMin = 20;
	private float mRotationAngleOffset = 0;
	private float mCurrentEndRotation = 1f;

	private Interpolator mRotationInterpolator = new LinearInterpolator();
	private Interpolator mSweepAppearingInterpolator = new DecelerateInterpolator();
	private Interpolator mEndInterpolator = new LinearInterpolator();

	private float mCurrentRotationAngle;
	private float mCurrentSweepAngle;
	private boolean mAppearingMode = true;
	private boolean isFirstSweep = false;
	private boolean isRunning;

	public SimpleProgressDrawable(int color, float sweepSpeed, float rotationSpeed, int sweepMin, int sweepMax) {
		mColor = color;
		mSweepSpeed = sweepSpeed;
		mRotationSpeed = rotationSpeed;
		mSweepAngleMin = sweepMin;
		mSweepAngleMax = sweepMax;
		//
		mPaint = new Paint();
		mPaint.setColor(mColor);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(mStrokeWidth);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeCap(Cap.ROUND);
		starAnimator();
	}

	private void starAnimator() {
		// 整体在不断的旋转
		mRotationAnimator = ValueAnimator.ofFloat(0, 360);
		mRotationAnimator.setDuration((long) (mRotationDuration / mSweepSpeed));
		mRotationAnimator.setInterpolator(mRotationInterpolator);
		mRotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
		mRotationAnimator.setRepeatMode(ValueAnimator.RESTART);
		mRotationAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float fraction = getAnimationFraction(animation);
				setRotationAngle(fraction * 360);
			}
		});
		// 不断增长的动画
		mSweepAppearAnimator = ValueAnimator.ofFloat(mSweepAngleMin, mSweepAngleMax);
		mSweepAppearAnimator.setDuration((long) (mSweepDuration / mSweepSpeed));
		mSweepAppearAnimator.setInterpolator(mSweepAppearingInterpolator);
		mSweepAppearAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float fraction = getAnimationFraction(animation);
				float sweepangle;
				if (isFirstSweep) {
					sweepangle = fraction * mSweepAngleMax;
				} else {
					sweepangle = mSweepAngleMin + fraction * (mSweepAngleMax - mSweepAngleMin);
				}
				setSweepAngle(sweepangle);
			}
		});
		mSweepAppearAnimator.addListener(new AnimatorListener() {

			private boolean cancel;

			@Override
			public void onAnimationStart(Animator animation) {
				cancel = false;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (!cancel) {
					isFirstSweep = false;
					setDisAppearing();
					mSweepDisAppearAnimator.start();
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				cancel = true;
			}
		});
		// 不断变短的动画
		mSweepDisAppearAnimator = ValueAnimator.ofFloat(mSweepAngleMax, mSweepAngleMin);
		mSweepDisAppearAnimator.setDuration((long) (mSweepDuration / mSweepSpeed));
		mSweepDisAppearAnimator.setInterpolator(mSweepAppearingInterpolator);
		mSweepDisAppearAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float fraction = getAnimationFraction(animation);
				setSweepAngle(mSweepAngleMax - fraction * (mSweepAngleMax - mSweepAngleMin));

			}
		});
		mSweepDisAppearAnimator.addListener(new AnimatorListener() {

			private boolean cancel;

			@Override
			public void onAnimationStart(Animator animation) {
				cancel = false;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (!cancel) {
					setAppearing();
					mSweepAppearAnimator.start();
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				cancel = false;
			}
		});
		//
		mEndAnimator = ValueAnimator.ofFloat(1f, 0);
		mEndAnimator.setDuration(mEndDuration);
		mEndAnimator.setInterpolator(mEndInterpolator);
		mEndAnimator.addUpdateListener(new AnimatorUpdateListener() {

			public void onAnimationUpdate(ValueAnimator animation) {
				float fraction = getAnimationFraction(animation);
				setEndRotation(1.0f - fraction);
			}
		});
		mEndAnimator.addListener(new AnimatorListener() {
			boolean cancel = false;

			@Override
			public void onAnimationStart(Animator animation) {
				cancel = false;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				setEndRotation(0f);
				if (!cancel) {
					stop();
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				cancel = true;
			}
		});

	}

	@Override
	public void draw(Canvas canvas) {
		float startAngle = mCurrentRotationAngle - mRotationAngleOffset;
		float sweepAngle = mCurrentSweepAngle;
		if (!mAppearingMode) {
			startAngle = startAngle + (360 - sweepAngle);
		}
		startAngle %= 360;
		canvas.drawArc(mRectF, startAngle, sweepAngle, false, mPaint);

	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void start() {
		if (isRunning()) {
			return;
		}
		isRunning = true;
		reinitValues();
		mRotationAnimator.start();
		mSweepAppearAnimator.start();
	}

	@Override
	public void stop() {
		if (!isRunning()) {
			return;
		}
		isRunning = false;
		stopAnimator();
	}

	private void stopAnimator() {
		mRotationAnimator.cancel();
		mSweepAppearAnimator.cancel();
		mSweepDisAppearAnimator.cancel();

	}

	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		super.setBounds(left, top, right, bottom);
		mRectF = new RectF(left + mStrokeWidth / 2 + 0.5f, top + mStrokeWidth / 2 + 0.5f, right - mStrokeWidth / 2 - 0.5f,
				bottom - mStrokeWidth / 2 - 0.5f);
	}

	private void reinitValues() {
		isFirstSweep = true;
	}

	public void setRotationAngle(float rotationangle) {
		mCurrentRotationAngle = rotationangle;
		invalidateSelf();
	}

	public void setSweepAngle(float sweepangle) {
		mCurrentSweepAngle = sweepangle;
		invalidateSelf();
	}

	private void setEndRotation(float end) {
		mCurrentEndRotation = end;
	}

	public void setAppearing() {
		mAppearingMode = true;
		mRotationAngleOffset = mRotationAngleOffset + mSweepAngleMin;
	}

	public void setDisAppearing() {
		mAppearingMode = false;
		mRotationAngleOffset = mRotationAngleOffset + (360 - mSweepAngleMax);
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	public float getAnimationFraction(ValueAnimator animator) {
		float fraction = animator.getDuration() > 0 ? ((float) animator.getCurrentPlayTime()) / animator.getDuration() : 0f;
		fraction = Math.min(fraction, 1f);
		fraction = animator.getInterpolator().getInterpolation(fraction);
		return fraction;
	}

	static class Build {
		private int mColor;
		private int mStroke;
		private int mColors[];
		private float mSweepSpeed;
		private float mRotationSpeed;
		private int mSweepMin;
		private int mSweepMax;

		public SimpleProgressDrawable builder() {
			return new SimpleProgressDrawable(mColor, mSweepSpeed, mRotationSpeed, mSweepMin, mSweepMax);
		}

		public Build setColor(int color) {
			mColor = color;
			return this;
		}

		public Build setColors(int colors[]) {
			mColors = colors;
			return this;
		}

		public Build setStroke(int stroke) {
			mStroke = stroke;
			return this;
		}

		public Build setSweepSpeed(float sweep) {
			mSweepSpeed = sweep;
			return this;
		}

		public Build setRotationSpeed(float rotation) {
			mRotationSpeed = rotation;
			return this;
		}

		public Build setSweepMin(int min) {
			mSweepMin = min;
			return this;
		}

		public Build setSweepMax(int max) {
			mSweepMax = max;
			return this;
		}

	}
}
