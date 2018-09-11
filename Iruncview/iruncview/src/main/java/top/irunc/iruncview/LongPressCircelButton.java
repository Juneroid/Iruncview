package top.irunc.iruncview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.Timer;
import java.util.TimerTask;

public class LongPressCircelButton extends View {
    private float progress = 0;
    // 定义画笔
    private Paint mPaint;
    private int centerX,centerY,width,height;
    /**
     * 上一次点击的的坐标
     */
    private float lastX;
    private float lastY;
    /**
     * 长按坐标
     */
    private float longPressX;
    private float longPressY;

    /**
     * 是否移动
     */
    private boolean isMove;
    /**
     * 滑动的阈值
     */
    private static final int TOUCH_SLOP = 40;
    private Runnable runnable;
    private OnMyLongClickListener onMyLongClickListener;
    private Handler myHandler;
    private Timer timer;

    private int buttoncolor = Color.parseColor("#b3e5fc");
    private int buttoncolor2 = Color.parseColor("#0277bd");
    private int border = 20;
    private int bordercolor = Color.parseColor("#1976d2");
    private String text = "一 键\n报 警";
    private int textsize = 50;
    private int longpressTime = 2000;
    private boolean textbold = false;

    public LongPressCircelButton(Context context) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }
    public LongPressCircelButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.longpressbt);
        buttoncolor = a.getInteger(R.styleable.longpressbt_btcolorstart, buttoncolor);
        buttoncolor2 =  a.getInteger(R.styleable.longpressbt_btcolorend, buttoncolor2);
        border =  a.getInteger(R.styleable.longpressbt_border,border);
        bordercolor =  a.getInteger(R.styleable.longpressbt_bordercolor, bordercolor);
        textsize =  a.getInteger(R.styleable.longpressbt_textsize, textsize);
        text = a.getString(R.styleable.longpressbt_text);
        longpressTime = a.getInteger(R.styleable.longpressbt_longpresstime, longpressTime);
        textbold = a.getBoolean(R.styleable.longpressbt_textbold, textbold);
        a.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        invalidate();
                        if (progress >= 100)
                        {
                            timer.cancel();
                            timer = null;
                            progress = 0;
                            invalidate();
                            onMyLongClickListener.onMyLongPress();
                        }
                        break;
                    case 1:
                        invalidate();
                        break;
                }
            }
        };

        runnable = new Runnable() {
            @Override
            public void run() {
                if (timer == null) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            progress++;
                            myHandler.sendEmptyMessage(0);
                        }
                    }, 0, longpressTime / 100);
                }
            }
        };
    }

    public void setOnMyLongClickListener(OnMyLongClickListener onMyLongClickListener) {
        this.onMyLongClickListener = onMyLongClickListener;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.translate(centerX,centerY);
        mPaint.setColor(buttoncolor2);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(border);
        mPaint.setAntiAlias(true);
        if (progress==0) {
            Shader shader = new LinearGradient(-height, -height, height, height, buttoncolor,
                    buttoncolor2, Shader.TileMode.CLAMP);
            mPaint.setShader(shader);
        }
        else
        {
            Shader shader = new LinearGradient(-height, -height, height, height, buttoncolor2,
                    buttoncolor, Shader.TileMode.CLAMP);
            mPaint.setShader(shader);
        }

        //金属
//        {
//            int[] colors = {0xff484848, 0xffc0c0c0, 0xff333333, 0xffdcdcdc, 0xff484848, 0xffdcdcdc, 0xff727272, 0xffdcdcdc ,0xff484848};
//            SweepGradient sweepGradient = new SweepGradient(0, 0, colors, null);
//            mPaint.setShader(sweepGradient);
//            Paint cPaint = new Paint();
//            cPaint.setStrokeWidth(2);
//            cPaint.setStyle(Paint.Style.STROKE);
//            cPaint.setColor(0xff333333);
//            for(int i =10;i<=height/2-border*2;i+=10) {
//                cPaint.setStrokeWidth(40/i);
//                if (i==height/2-border*2)
//                {
//                    cPaint.setColor(0xff000000);
//                    cPaint.setStrokeWidth(5);
//                }
//                canvas.drawCircle(0, 0, i, cPaint);
//            }
//        }

        // 绘制一个圆
        canvas.drawARGB(0,0, 0, 0);
        canvas.drawCircle(0, 0, height/2-border*2, mPaint);


        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textsize);
        textPaint.setFakeBoldText(textbold);
        textPaint.setStyle(Paint.Style.FILL);
        //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
        textPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom

        int baseLineY = (int) (0 - top/2 - bottom/2);//基线中间点的y轴计算公式
        if (!text.contains("\n")) {
            canvas.drawText(text, 0, baseLineY, textPaint);
        }
        else
        {
            String [] a = text.split("\n");
            if (a.length ==2)
            {
                int baseLineY1 = (int) (0 - top);//基线中间点的y轴计算公式
                int baseLineY2 = (int) (0 - bottom);//基线中间点的y轴计算公式
                canvas.drawText(a[0], 0, baseLineY2, textPaint);
                canvas.drawText(a[1], 0, baseLineY1, textPaint);
            }
            else
            {
                canvas.drawText(text, 0, baseLineY, textPaint);
            }
        }

//        mPaint.setShader(null);
        mPaint.setStrokeWidth(border);
        mPaint.setColor(bordercolor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(-height/2+border,-height/2+border,height/2-border,height/2-border,-90,progress * 3.6f,false,mPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w/2;
        centerY = h/2;
        width = w;
        height = h;
        //初始化点
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                isMove = false;
                lastX = x;
                lastY = y;
                postDelayed(runnable, ViewConfiguration.getLongPressTimeout());
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMove){
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    if(progress != 0)
                    {
                        progress = 0;
                        myHandler.sendEmptyMessage(1);
                    }
                    break;
                }
                if (Math.abs(lastX-x) > TOUCH_SLOP || Math.abs(lastY-y) > TOUCH_SLOP){
                    //移动超过了阈值，表示移动了
                    isMove = true;
                    //移除runnable
                    removeCallbacks(runnable);
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    if(progress != 0)
                    {
                        progress = 0;
                        myHandler.sendEmptyMessage(1);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                removeCallbacks(runnable);
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                if(progress != 0)
                {
                    progress = 0;
                    myHandler.sendEmptyMessage(1);
                }
                break;
        }
        return true;
    }

    public interface OnMyLongClickListener{
        void onMyLongPress();
        void onMyLongError();
    }
}
