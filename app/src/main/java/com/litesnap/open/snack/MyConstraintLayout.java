package com.litesnap.open.snack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class MyConstraintLayout extends FrameLayout {
    public static final String TAG = "MyConstraintLayout";
    private int mLastX;
    private boolean mIsDrag;
    private View mDecorView;
    private View mChildView;

    private int mShadowWidth;

    private GradientDrawable mShadowDrawable;
    public static final int DEFAULT_SHADOW_START_COLOR = Color.parseColor("#00000000");
    public static final int DEFAULT_SHADOW_END_COLOR = Color.parseColor("#50000000");

    private int mShadowStartColor = DEFAULT_SHADOW_START_COLOR;
    private int mShadowEndColor = DEFAULT_SHADOW_END_COLOR;

    private Activity mActivity;
    private final List<ObjectAnimator> mAnimationList;

    public MyConstraintLayout(Context context) {
        this(context, null);
    }

    public MyConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mShadowWidth = (int) AndroidSystem.dip2px(context, 15);
        mActivity = (Activity) context;
        mAnimationList = new ArrayList<>();
        mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{this.mShadowStartColor, this.mShadowEndColor});
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() > 1){
            throw new RuntimeException("child count must 1");
        }
        mDecorView = mActivity.getWindow().getDecorView();
        mChildView = getChildAt(0);
        if (mChildView.getBackground() == null){
            mChildView.setBackgroundColor(Color.WHITE);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.save();
        int left = mChildView.getLeft() - mShadowWidth;
        int top = 0;
        int right = left + mShadowWidth;
        int bottom = this.getHeight();
        mShadowDrawable.setBounds(left, top, right, bottom);
        mShadowDrawable.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) ev.getX();
                mIsDrag = false;
                if (mLastX < getMeasuredWidth() * 0.05){
                    mIsDrag = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = (int) (mLastX - ev.getX());
                if (mIsDrag){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mShadowDrawable.mutate();
                        mShadowDrawable.setColors(new int[] {mShadowStartColor , mShadowEndColor});
                        invalidate();
                    }
                    mChildView.offsetLeftAndRight(-dy);
                }
                mLastX = (int) ev.getX();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mChildView.getLeft() >= getMeasuredWidth() * 0.3f){
                    startRightAnimation();
                }else {
                    startLeftAnimation();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsDrag){
            return mIsDrag;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void startRightAnimation(){
        for (ObjectAnimator animator : mAnimationList) {
            animator.cancel();
            animator.end();
        }

        mAnimationList.clear();
        Rect rect = new Rect();
        mChildView.getLocalVisibleRect(rect);

        int end = rect.width();
        int duration = (int) (450.0f * ((rect.width() * 1.0f) / (mChildView.getWidth() * 1.0f)));
        ObjectAnimator animator = ObjectAnimator.ofInt(new Spac(), "RightSpac", 0, end);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        mAnimationList.add(animator);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsDrag = false;
                if (mChildView.getLeft() >= getRight()){
                    mActivity.onBackPressed();
                }
            }
        });
        animator.start();
    }


    private void startLeftAnimation(){
        for (ObjectAnimator animator : mAnimationList) {
            animator.cancel();
            animator.end();
        }

        mAnimationList.clear();
        int end = mChildView.getLeft();
        ObjectAnimator animator = ObjectAnimator.ofInt(new Spac(), "LeftSpac", 0, - end);
        animator.setDuration(250);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimationList.add(animator);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsDrag = false;
            }
        });
        animator.start();
    }

    private class Spac{
        private int num;

        private void setRightSpac(int x){
            int offset = x - num;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mShadowDrawable.mutate();
                mShadowDrawable.setColors(new int[] {mShadowStartColor , mShadowEndColor});
                invalidate();
            }
            mChildView.offsetLeftAndRight(offset);
            num = x;
        }

        private void setLeftSpac(int x){
            int offset = x - num;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mShadowDrawable.mutate();
                mShadowDrawable.setColors(new int[] {mShadowStartColor , mShadowEndColor});
                invalidate();
            }
            mChildView.offsetLeftAndRight(offset);
            num = x;
        }
    }

}
