package com.bytedance.clockapplication.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.LogRecord;

public class Clock extends View {

    private final static String TAG = Clock.class.getSimpleName();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.010f;

    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;
    private static final int NEED_INVALIDATE=0x2333;

    private int mWidth, mCenterX, mCenterY, mRadius;

    /**
     * properties
     */

    private int centerInnerColor;
    private int centerOuterColor;

    private  Calendar calendar;

    private int secondsNeedleColor;
    private int hoursNeedleColor;
    private int minutesNeedleColor;

    private int degreesColor;

    private int hoursValuesColor;

    private int numbersColor;


    private boolean mShowAnalog = true;




    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case NEED_INVALIDATE :
                    invalidate();
                    mHandler.sendEmptyMessageDelayed(NEED_INVALIDATE,1000);
                    break;
                 default:
                        break;
            }
        }

    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.getLooper().quit();
    }



    public Clock(Context context) {
        super(context);
        init(context, null);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    private void init(Context context, AttributeSet attrs) {

        this.centerInnerColor = Color.LTGRAY;
        this.centerOuterColor = DEFAULT_PRIMARY_COLOR;

        this.secondsNeedleColor = DEFAULT_SECONDARY_COLOR;
        this.hoursNeedleColor = DEFAULT_PRIMARY_COLOR;
        this.minutesNeedleColor = DEFAULT_PRIMARY_COLOR;

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        this.hoursValuesColor = DEFAULT_PRIMARY_COLOR;

        numbersColor = Color.WHITE;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int halfWidth = mWidth / 2;
        mCenterX = halfWidth;
        mCenterY = halfWidth;
        mRadius = halfWidth;

        if (mShowAnalog) {
            drawDegrees(canvas);
            drawHoursValues(canvas);
            drawNeedles(canvas);
            drawCenter(canvas);
        } else {
            drawNumbers(canvas);
        }

    }

    private void drawDegrees(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = mCenterX - (int) (mWidth * 0.01f);
        int rEnd = mCenterX - (int) (mWidth * 0.05f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)
                paint.setAlpha(CUSTOM_ALPHA);
            else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startY = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)));

            int stopX = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int stopY = (int) (mCenterX - rEnd * Math.sin(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);

        }
    }

    /**
     @param canvas
     */
    private void drawNumbers(Canvas canvas) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth * 0.2f);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);

        String time = String.format("%s:%s:%s%s",
                String.format(Locale.getDefault(), "%02d", hour),
                String.format(Locale.getDefault(), "%02d", minute),
                String.format(Locale.getDefault(), "%02d", second),
                amPm == AM ? "AM" : "PM");

        SpannableStringBuilder spannableString = new SpannableStringBuilder(time);
        spannableString.setSpan(new RelativeSizeSpan(0.3f), spannableString.toString().length() - 2, spannableString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        StaticLayout layout = new StaticLayout(spannableString, textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
        canvas.translate(mCenterX - layout.getWidth() / 2f, mCenterY - layout.getHeight() / 2f);
        layout.draw(canvas);
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */


    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        // - hoursValuesColor
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(hoursValuesColor);
        paint.setTextSize(100);
        paint.setTextAlign(Paint.Align.CENTER);


        int rPadded = mCenterX - (int) (mWidth * 0.01f)-100;

        String degree;

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {
            int cul = 360 + 90 - i;
            if(cul > 360)
                cul -= 360;
            cul = cul/30;

            if(cul == 0){
                degree = "12";
            }
            else{
                degree = String.valueOf(cul);
            }

            paint.setAlpha(FULL_ALPHA);

            int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startY = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)));

            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float top = fontMetrics.top;
            float bottom = fontMetrics.bottom;
            startY = (int) (startY - top/2 - bottom/2);

            if(i % 30 == 0){
                canvas.drawText(degree,startX,startY,paint);
            }
        }


    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     * final
     */
    private void drawNeedles( Canvas canvas) {
        // Default Color:
        // - secondsNeedleColor
        // - hoursNeedleColor
        // - minutesNeedleColor

        Paint paintHour = new Paint(); //画时针
        Paint paintMinute = new Paint(); //画分针
        Paint paintSecond = new Paint(); //画秒针

        paintHour.setStyle(Paint.Style.FILL_AND_STROKE);
        paintMinute.setStyle(Paint.Style.FILL_AND_STROKE);
        paintSecond.setStyle(Paint.Style.FILL_AND_STROKE);

        paintHour.setColor(hoursNeedleColor);
        paintMinute.setColor(minutesNeedleColor);
        paintSecond.setColor(secondsNeedleColor);

        paintHour.setStrokeWidth(15);
        paintMinute.setStrokeWidth(10);
        paintSecond.setStrokeWidth(5);


        SimpleDateFormat format = new SimpleDateFormat("HH-mm-ss");
        String time = format.format(new Date(System.currentTimeMillis()));
        String[] split = time.split("-");
        int hour = Integer.parseInt(split[0]);
        int minute = Integer.parseInt(split[1]);
        int second = Integer.parseInt(split[2]);

        int hourdregrees=hour*30+minute/2;
        int minudegrees=minute*6+second/10;
        int seconddedree=second*6;

        canvas.rotate(hourdregrees,mCenterX,mCenterY);
        canvas.drawLine(mCenterX,mCenterY,mCenterX,mCenterY-mCenterX+250,paintHour);
        canvas.save();
        canvas.restore();

        canvas.rotate(-hourdregrees,mCenterX,mCenterY);

        canvas.rotate(minudegrees,mCenterX,mCenterY);
        canvas.drawLine(mCenterX,mCenterY,mCenterX,mCenterY-mCenterX+130,paintMinute);
        canvas.save();
        canvas.restore();

        canvas.rotate(-minudegrees,mCenterX,mCenterY);

        canvas.rotate(seconddedree,mCenterX,mCenterY);
        canvas.drawLine(mCenterX,mCenterY,mCenterX,mCenterY-mCenterX+30,paintSecond);


    }

    /**
     * Draw Center Dot
     *
     * @param canvas
     */

    private void drawCenter(Canvas canvas) {
        // Default Color:
        // - centerInnerColor
        // - centerOuterColor
        Paint paintCenterOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCenterOuter.setStyle(Paint.Style.FILL_AND_STROKE);
        paintCenterOuter.setStrokeCap(Paint.Cap.ROUND);
        paintCenterOuter.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paintCenterOuter.setColor(centerOuterColor);

        Paint paintCenterInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCenterInner.setStyle(Paint.Style.FILL_AND_STROKE);
        paintCenterInner.setStrokeCap(Paint.Cap.ROUND);
        paintCenterInner.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paintCenterInner.setColor(centerInnerColor);

        canvas.drawCircle(mCenterX,mCenterY,10,paintCenterOuter);
        canvas.drawCircle(mCenterX,mCenterY,5,paintCenterInner);



    }

    public void setShowAnalog(boolean showAnalog) {
        mShowAnalog = showAnalog;
        invalidate();
    }

    public boolean isShowAnalog() {
        return mShowAnalog;
    }

}