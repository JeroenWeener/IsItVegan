package com.jwindustries.isitvegan.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.jwindustries.isitvegan.R;

public class DragHandleView extends View {
    private final Path mPath = new Path();
    private int mColor;

    public DragHandleView(Context context) {
        super(context);
    }

    public DragHandleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DragHandleView, 0, 0);
        try {
            mColor = a.getColor(R.styleable.DragHandleView_color, Color.parseColor("#00000000"));
        } finally {
            a.recycle();
        }
    }

    public DragHandleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DragHandleView, 0, 0);
        try {
            mColor = a.getColor(R.styleable.DragHandleView_color, Color.parseColor("#00000000"));
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw rounded corners
        mPath.reset();
        mPath.addRoundRect(0, 0, getWidth(), getHeight(), 100, 100, Path.Direction.CW);
        mPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
        canvas.clipPath(mPath);

        // Draw over rounded corners on top
        mPath.reset();
        mPath.addRect(0, (int) (getHeight() / 2), getWidth(), getHeight(), Path.Direction.CW);
        mPath.setFillType(Path.FillType.EVEN_ODD);
        canvas.clipPath(mPath);

        canvas.drawColor(mColor);
    }
}