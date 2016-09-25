package com.xiaosu.gridlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;


/**
 * 作者：疏博文 创建于 2016-03-14 12:22
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class GridLayout extends ViewGroup {

    private float mItemMaxHeight;
    private int mColumnNum;
    private float mHorizontalSpace;
    private float mVerticalSpace;
    private float mOrgItemHeight;
    private float mItemMaxWidth;

    public GridLayout(Context context) {
        this(context, null);
    }

    public GridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridLayout);
        //每一行的item的个数
        mColumnNum = a.getInt(R.styleable.GridLayout_android_columnCount, 1);
        //item之间的横向的距离
        mHorizontalSpace = a.getDimension(R.styleable.GridLayout_horizontalSpace, 0);
        //item之间的纵向的距离
        mVerticalSpace = a.getDimension(R.styleable.GridLayout_verticalSpace, 0);
        //item的高度
        mOrgItemHeight = mItemMaxHeight = a.getDimension(R.styleable.GridLayout_itemHeight, -1);

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //requestLayout之后,这个值要复原
        mItemMaxHeight = mOrgItemHeight;

        int childCount = getChildCount();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            throw new RuntimeException("layout_width不能指定为wrap_content");
        }

        if (childCount != 0) {
            //行数
            int rowNum = childCount / mColumnNum;
            //剩余的个数
            int extraNum = childCount % mColumnNum;

            //先确定item的宽
            int mWidth = MeasureSpec.getSize(widthMeasureSpec);

            mItemMaxWidth = (mWidth - getPaddingLeft() - getPaddingRight() - mHorizontalSpace * (mColumnNum - 1)) / mColumnNum;

            //如果没有指定item的高,则将高设定为item的宽
            mItemMaxHeight = mItemMaxHeight == -1 ? mItemMaxWidth : mItemMaxHeight;

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int) mItemMaxWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) mItemMaxHeight, MeasureSpec.EXACTLY);

            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                measureChild(child, childWidthMeasureSpec, childHeightMeasureSpec);
            }

            //如果高度是wrap_content则需要确定高度
            if (heightMode == MeasureSpec.AT_MOST) {
                //再确定高
                float mHeight;
                if (extraNum == 0) {
                    mHeight = rowNum * mItemMaxHeight + mVerticalSpace * (rowNum - 1);
                    mHeight += getPaddingBottom() + getPaddingTop();
                } else {
                    mHeight = (rowNum + 1) * mItemMaxHeight + mVerticalSpace * rowNum;
                    mHeight += getPaddingBottom() + getPaddingTop();
                }

                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) mHeight, MeasureSpec.EXACTLY);
            }
        } else {
            //没有child设定为不占位置
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int left = l + getPaddingLeft();
        int top = t + getPaddingTop();

        for (int i = 0; i < count; i++) {
            //左上角的y坐标
            //左上角x坐标
            int right = (int) (left + mItemMaxWidth);
            int bottom = (int) (top + mItemMaxHeight);

            View child = getChildAt(i);
            int childMeasuredWidth = child.getMeasuredWidth();
            int childMeasuredHeight = child.getMeasuredHeight();

            // TODO: 16/7/8 添加Gravity支持,目前只支持center模式
            int LROffset = (int) ((mItemMaxWidth - childMeasuredWidth) / 2);
            int TBOffset = (int) ((mItemMaxHeight - childMeasuredHeight) / 2);

            child.layout(left + LROffset, top + TBOffset, right - LROffset, bottom - TBOffset);

            if (((i + 1) % mColumnNum) == 0) {
                //换行
                top += (bottom + mVerticalSpace);
                left = l + getPaddingLeft();
            } else {
                left = (int) (right + mHorizontalSpace);
            }
        }
    }
}
