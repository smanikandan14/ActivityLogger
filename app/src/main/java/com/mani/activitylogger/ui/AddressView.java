package com.mani.activitylogger.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.mani.activitylogger.R;
import com.mani.activitylogger.util.FontProvider;

/**
 * Created by maniselvaraj on 5/10/14.
 */
public class AddressView extends View {

    Paint mTextPaint;
    Context mContext;
    String startAddress;
    String endAddress;
    int textSize;
    int mViewWidth;
    int mViewHeight;
    int mArrowMargin;
    Bitmap arrowBitmap;
    int posX, posY;
    RectF arrowRect;
    int mArrowWidth;
    int mArrowHeight;

    public AddressView(Context context) {
        super(context);
        init(context);
    }

    public AddressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AddressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        textSize = mContext.getResources().getDimensionPixelSize(R.dimen.trip_address_text_size);
        mArrowMargin = mContext.getResources().getDimensionPixelSize(R.dimen.trip_arrow_margin);

        mTextPaint = new Paint();
        mTextPaint.setTypeface(FontProvider.getBold());
        mTextPaint.setTextSize(textSize);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mContext.getResources().getColor(R.color.font_dark));

        arrowRect = new RectF();
        arrowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow);

        int bitmapWidth = (arrowBitmap.getWidth() > textSize) ? textSize : arrowBitmap.getWidth();
        int bitmapHeight = (arrowBitmap.getHeight() > textSize) ? textSize : arrowBitmap.getHeight();

        //Arrow height is set as 70% of line height.
        mArrowHeight = (textSize * 70 ) / 100;

        //Maintain the aspect ratio of bitmap when determining width
        mArrowWidth = (bitmapWidth * mArrowHeight )/bitmapHeight;

    }

    public void setStartAddressText(String text) {
        startAddress = text;
    }

    public void setEndAddressText(String text) {
        endAddress = text;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        mViewWidth = widthSize;

        if( mViewHeight == 0) {
            // Determine the total number of lines required to draw
            // startAddress, endAddress along with arrow.
            mViewHeight = measureTotalHeight();
        }

        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    private int measureTotalHeight() {
        String totalAddress = startAddress + endAddress;
        int requiredHeight = 1;
        int size = (int) Math.ceil(mTextPaint.measureText(totalAddress));
        while ( size > mViewWidth) {
            int textWidth = mTextPaint.breakText(totalAddress, true, mViewWidth, null);
            totalAddress = totalAddress.substring(0, textWidth);
            size -= mViewWidth;
            requiredHeight++;
        }
        return requiredHeight * textSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        posX = 0;
        posY = textSize;

        //Draw start trip address text
        drawText(startAddress, canvas);

        //Draw a arrow image with margins on left & right
        drawArrow(canvas);

        //Draw end trip address text
        drawText(endAddress, canvas);
    }

    private void drawText(String address, Canvas canvas) {
        while (address.length() > 0) {
            // Measure how much space is required to draw the address with text paint.
            int size = (int) Math.ceil(mTextPaint.measureText(address));
            String textToDraw;

            if ( (posX + size) > mViewWidth) {
                // Find the position of text which can be drawn with textPaint on width
                // of mViewWidth
                int textWidth = mTextPaint.breakText(address, true, (mViewWidth-posX), null);
                textToDraw = address.substring(0, textWidth);
                address = address.substring(textWidth, address.length());
                canvas.drawText(textToDraw, posX, posY - mTextPaint.descent(), mTextPaint);
                // Move posY to next line.
                posY += textSize;
                // Set posX to starting of line.
                posX = 0;
            } else {
                textToDraw = address;
                canvas.drawText(textToDraw, posX, posY - mTextPaint.descent(), mTextPaint);
                address = "";
                // Move posX just next to the end of text in the line.
                posX = size;
            }
        }
    }

    private void drawArrow(Canvas canvas) {
        //If posX + arrow width > space left on the line, Move to next line to draw arrow.
        if ((posX + mArrowMargin + mArrowWidth) > mViewWidth) {
            posX = 0;
            posY += textSize;
        }

        posX += mArrowMargin; // Left margin for arrow
        arrowRect.top = posY - (mArrowHeight + mTextPaint.descent()) ;
        arrowRect.bottom = arrowRect.top + mArrowHeight; //Height of arrow
        arrowRect.left = posX;
        arrowRect.right = posX + mArrowWidth; //Width of arrow
        canvas.drawBitmap(arrowBitmap, null, arrowRect, null);
        posX += (mArrowWidth + mArrowMargin); // Add right margin for arrow
    }
}
