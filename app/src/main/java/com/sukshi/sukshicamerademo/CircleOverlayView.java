package com.sukshi.sukshicamerademo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CircleOverlayView extends GraphicOverlay {
    private Bitmap bitmap;

    public CircleOverlayView(Context context) {
        super(context);
    }

    public CircleOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (bitmap == null) {
            createWindowFrame();
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    protected void createWindowFrame() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas osCanvas = new Canvas(bitmap);

        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());


        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.white));
        paint.setAlpha(255);
        osCanvas.drawRect(outerRectangle, paint);
        paint.setColor(Color.TRANSPARENT);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2.7f;
        float radius = Utils2.getScreenWidth(getContext()) * 0.35f;
        osCanvas.drawCircle(centerX, centerY, radius, paint);
    }

    public void updateLayout(){
        postInvalidate();
    }
    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint pStroke = new Paint();
        pStroke.setStyle(Paint.Style.STROKE);
        pStroke.setStrokeWidth(8);
        if(isEnabled()) {
            pStroke.setColor(Color.GREEN);
        }else{
            pStroke.setColor(Color.RED);
        }
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2.7f;
        float radius = Utils2.getScreenWidth(getContext()) * 0.35f;
        canvas.drawCircle(centerX,centerY,radius+2,pStroke);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        bitmap = null;
    }
}
