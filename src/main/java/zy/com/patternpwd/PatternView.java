package zy.com.patternpwd;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zy on 16-6-18.
 */
public class PatternView extends SurfaceView implements SurfaceHolder.Callback{

    private static final int DEFAULT_COUNT = 3;
    private static final int POINT_R = 50;
    private static final int DRAW_POINT_R = 20;

    private int width = 0;
    private int height = 0;

    private int originalColor;
    private int chooseColor;
    private int backgroundColor;
    private int count;

    private int pointR = POINT_R;
    private int drawPointR = DRAW_POINT_R;

    private boolean transparent = false;

    private List<Point> originalPoints;
    private List<Point> drawPoints;
    private List<Integer> pattern;
    private Point tmpPoint;
    private DrawThread thread;
    private SurfaceHolder holder;

    private PwdListener listener;

    public PatternView(Context context) {
        this(context, null);
    }

    public PatternView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PatternView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        originalColor = Color.BLUE;
        chooseColor = Color.GRAY;
        backgroundColor = Color.WHITE;
        count = DEFAULT_COUNT;

        pattern = new ArrayList<>();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PatternView, defStyleAttr, 0);
        int n = typedArray.getIndexCount();

        for (int i = 0; i < n; i ++){
            int attr = typedArray.getIndex(i);
            switch (attr){
                case R.styleable.PatternView_originalColor:
                    originalColor = typedArray.getColor(attr, Color.BLUE);
                    break;
                case R.styleable.PatternView_chooseColor:
                    chooseColor = typedArray.getColor(attr, Color.GRAY);
                    break;
                case R.styleable.PatternView_backgroundColor:
                    backgroundColor = typedArray.getColor(attr, Color.WHITE);
                    break;
                case R.styleable.PatternView_count:
                    count = typedArray.getInt(attr, DEFAULT_COUNT);
                    break;
            }
        }
        typedArray.recycle();

        if (backgroundColor == Color.TRANSPARENT){
            this.setZOrderOnTop(true);
            this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            transparent = true;
        }

        holder = this.getHolder();
        holder.addCallback(this);
        thread = new DrawThread(holder);
    }

    private void initPoints() {
        originalPoints = new LinkedList<>();
        drawPoints = new LinkedList<>();
        tmpPoint = new Point(0, 0);

        pointR = Math.min(width, height) / count / 4;

        int x = width / count / 4 + pointR;
        int y = height / count / 4 + pointR;

        drawPointR = pointR - 20;

        int wGap = width / count / 2;
        int hGap = height/ count / 2;

        for (int i = 0; i < count; i ++){
            for (int j = 0; j < count; j ++){
                originalPoints.add(new Point(x + j * 2 * wGap, y + i * 2 * hGap, i * count + j));
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        width = this.getWidth();
        height = this.getHeight();
        initPoints();
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Point p = null;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (listener != null){
                    listener.patternStart();
                }
                p = getPoint(x, y);
                if (p != null){
                    drawPoints.add(p);
                    if (listener != null){
                        listener.patternAdd(p.getNum());
                        pattern.add(p.getNum());
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                p = getPoint(x, y);
                if (p != null && (drawPoints.isEmpty() || drawPoints.get(drawPoints.size() - 1) != p)){
                    if (drawPoints.size() > 0 && drawPoints.get(drawPoints.size() - 1) == tmpPoint){
                        drawPoints.remove(drawPoints.size() - 1);
                    }
                    if (listener != null){
                        listener.patternAdd(p.getNum());
                        pattern.add(p.getNum());
                    }
                    drawPoints.add(p);
                }else if (p == null){
                    tmpPoint.x = (int) x;
                    tmpPoint.y = (int) y;
                    if (drawPoints.size() > 0 && drawPoints.get(drawPoints.size() - 1) != tmpPoint){
                        drawPoints.add(tmpPoint);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (drawPoints.size() > 0 && drawPoints.get(drawPoints.size() - 1) == tmpPoint){
                    drawPoints.remove(drawPoints.size() - 1);
                }
                if (listener != null){
                    listener.patternEnd(pattern);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private Point getPoint(float x, float y) {
        for (Point p : originalPoints){
            if (Math.abs(p.x - x) < pointR && Math.abs(p.y - y) < pointR){
                return p;
            }
        }
        return null;
    }

    public void setListener(PwdListener listener) {
        this.listener = listener;
    }

    public void clearAll() {
        pattern.clear();
        drawPoints.clear();
    }

    public class DrawThread extends Thread {
        private SurfaceHolder holder;
        private boolean running;

        public DrawThread(SurfaceHolder holder) {
            this.holder = holder;
            running = true;
        }

        @Override
        public void run() {
            while (running){
                Canvas canvas = null;
                try{
                    synchronized (holder){
                        canvas = holder.lockCanvas();
                        myDraw(canvas);
                    }

                }finally {
                    if (canvas != null){
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        public void myDraw(Canvas canvas) {
            if (canvas == null){
                return ;
            }
            if (!transparent){
                canvas.drawColor(backgroundColor);
            }else {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            }
            drawOriginal(canvas);

            if (drawPoints.isEmpty()){
                return ;
            }

            drawNow(canvas);
        }

        public void drawOriginal(Canvas canvas) {
            Paint p = new Paint();
            p.setColor(originalColor);
            for (Point point : originalPoints){
                canvas.drawCircle(point.x, point.y, pointR, p);
            }
        }

        public void drawNow(Canvas canvas) {

            Paint pD = new Paint();
            pD.setColor(chooseColor);
            Paint pL = new Paint();
            pL.setColor(chooseColor);
            pL.setStrokeWidth(20);
            Point lastP = drawPoints.get(0);
            canvas.drawCircle(lastP.x, lastP.y, drawPointR, pD);
            for (int i = 1; i < drawPoints.size(); i ++){
                Point tmpP = drawPoints.get(i);
                canvas.drawLine(lastP.x, lastP.y, tmpP.x, tmpP.y, pL);
                if (tmpP != tmpPoint){
                    canvas.drawCircle(tmpP.x, tmpP.y, drawPointR, pD);
                }
                lastP = tmpP;
            }
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public boolean getRunning() {
            return running;
        }
    }



}

