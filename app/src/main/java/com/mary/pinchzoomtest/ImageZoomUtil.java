package com.mary.pinchzoomtest;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ImageZoomUtil {
    private static final String TAG = "MainActivity";

    private Bitmap image;

    private float height;
    private float width;
    private float X;
    private float Y;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    private PointF start = new PointF();
    private PointF mid = new PointF();

    private float[] lastEvent = null;

    private float newRot = 0f;
    private float d = 0f;

    //디바이스 크기
    int deviceWidth;
    int deviceHeight;

    //모드 구분
    private final int NONE = 0;
    private final int DRAG = 1;
    private final int ZOOM = 2;
    int mode = NONE;

    RectF rect = new RectF();
    RectF rect2 = new RectF(-1,0,deviceWidth,2048);

    //드래그 좌표 저장
    int posX1 = 0;
    int posX2 = 0;

    //최종 좌표 저장
    float x0 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    //이전값 저장
    float beforeDy;

    //스케일 저장
    float scale = 1f;
    float lastScale;

    //Zoom x1
    float finalX1 = 1080f;

    //DragDx 값 저장
    float dragDx;
    float dragDy;

    //ZoomDx 값 저장
    float zoomDx;
    float zoomDy;


    //핀치 시 두 좌표간 거리 저장
    float oldDist = 1f;
    float newDist = 1f;

    //리셋을 해야하는건지 확인함
    boolean isReset = false;

    //하는게 줄어든데서 또 줄이는건지, 아니면 줄이다보니 줄어든건지 확인하는 인자
    boolean isZoomIn = true;

    //Image가 화면 내에 있어야 핀치를 할 수 있도록 Image를 인자로 받는다.
    public ImageZoomUtil(Bitmap image) {
        Log.d(TAG, "ImageZoomUtil: ??");
        this.image = image;

    }

    public void settingImage(ImageView v, int deviceWidth, int deviceHeight) {


        ImageView view = v;

        this.deviceWidth = deviceWidth;
        this.deviceHeight = deviceHeight;

        float displayY = image.getHeight();

        matrix.set(savedMatrix);
        matrix.postTranslate(0, ((deviceHeight / 2) - (displayY) * 1.5f));
        x2 = image.getWidth();
        y1 = (deviceHeight / 2) - (displayY * 1.5f);
        view.setImageMatrix(matrix);

    }

    public void touchEvent(ImageView v, MotionEvent event) {

        ImageView view = v;

        int act = event.getAction();
        switch (act & MotionEvent.ACTION_MASK) {
            //손가락 하나 터치
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                Log.d(TAG, "touchEvent: 사진 내부에 들어옴");
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                posX1 = (int) event.getX();
                posX2 = (int) event.getY();


                Log.d(TAG, "onTouchEvent: mode : " + mode);


                break;

            //드래그
            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG) { //드래그 중일 시 XY값을 변화시키며 위치를 이동한다.
                    matrix.set(savedMatrix);

                    rect.setEmpty();
                    matrix.mapRect(rect);

                    Log.d(TAG, "touchEvent: rect.centerX() "+ rect.right);
                    Log.d(TAG, "touchEvent: "+rect.width());

                    float dragDx = event.getX() - start.x;
                    float dragDy = event.getY() - start.y;

                    float[] values = new float[9];
                    matrix.getValues(values);

                    float finalScale = values[Matrix.MSCALE_X];

                    if (finalScale == 1) {
                        mode = NONE;
                        break;
                    }

                    if (values[Matrix.MTRANS_X] + dragDx >= 0) {
                        Log.d(TAG, "touchEvent: x1 정지이벤트");
                        matrix.setScale(finalScale, finalScale);
                        matrix.postTranslate(0, (values[Matrix.MTRANS_Y]) + dragDy);

                        view.setImageMatrix(matrix);
                        return;
                    }


//                    Log.d(TAG, "touchEvent: values[Matrix.MTRANS_X]+ (finalScale*image.getWidth()) :" + (values[Matrix.MTRANS_X] + (finalScale * image.getWidth())));
//                    Log.d(TAG, "touchEvent: deviceWidth :" + image.getWidth());
//                    Log.d(TAG, "touchEvent: values[Matrix.MTRANS_X]) :" + values[Matrix.MTRANS_X]);
//                    Log.d(TAG, "touchEvent: (finalScale*image.getWidth()) :" + (finalScale * image.getWidth()));
//                    Log.d(TAG, "touchEvent: (finalScale) :" + (finalScale));
                    //Log.d(TAG, "touchEvent: dx :" +dx );

                    //시작점의 x좌표 + 원래 사진 길이 * 비율 == x2좌표?


                    Log.d(TAG, "touchEvent: values[Matrix.MTRANS_X]+dragDx : "+(values[Matrix.MTRANS_X]+finalScale*deviceWidth+dragDx));
                    if ((values[Matrix.MTRANS_X]+finalScale*deviceWidth+dragDx)<=deviceWidth) {
                        Log.d(TAG, "touchEvent: x2 정지이벤트");

                        matrix.setScale(finalScale, finalScale);
                        matrix.postTranslate((-(finalScale-1)*deviceWidth), (values[Matrix.MTRANS_Y]) + dragDy);

                        view.setImageMatrix(matrix);
                        return;
                    }


//                    if(y1 + dy <= 0){
//                        Log.d(TAG, "touchEvent: y1 정지이벤트");
//                        matrix.postTranslate(dx, dy);
//                        mode = NONE;
//                        lastEvent = null;
//                        matrix.postTranslate(0, -(y1 + dy));
//                        setDragFinalLocation();
//                        y1=0;
//                        view.setImageMatrix(matrix);
//                        return;
//                    }
//
//                    //줌인 - 아웃 됐을때만 적용하기?
//                    if(x2+dx>deviceWidth){
//                        Log.d(TAG, "touchEvent: x2 정지이벤트");
//                        matrix.postTranslate(dx, dy);
//                        mode = NONE;
//                        lastEvent = null;
//                        setDragFinalLocation();
//                        x2=deviceWidth;
//                        matrix.postTranslate(0, -(x2 + dx));
//                        view.setImageMatrix(matrix);
//                        return;
//                    }


                    mode = DRAG;
                    Log.d(TAG, "touchEvent: mode : drag");
                    matrix.postTranslate(dragDx, dragDy);

                    beforeDy = values[Matrix.MTRANS_Y] + dragDy;


                } else if (mode == ZOOM) {

                    rect.setEmpty();
                    matrix.mapRect(rect);

                    newDist = spacing(event);

                    float[] values = new float[9];
                    matrix.getValues(values);

                    width = values[Matrix.MSCALE_X] * image.getWidth();

                    matrix.set(savedMatrix);
                    scale = (newDist / oldDist);

//                    Log.d(TAG, "touchEvent: values[Matrix.MTRANS_X] " + (((finalX1 - mid.x) * (scale - 1)) + finalX1 +
//                            zoomDx));
//                    Log.d(TAG, "touchEvent: dx : " + (mid.x));
//                    Log.d(TAG, "touchEvent: dx : " + (start.x));
//                    Log.d(TAG, "touchEvent: dx : " + (values[Matrix.MTRANS_X]));
//                    Log.d(TAG, "touchEvent: dx : " + deviceWidth);

                        matrix.set(savedMatrix);
                        zoomDx = event.getX() - start.x;
                        zoomDy = event.getY() - start.y;

                        if (values[Matrix.MTRANS_X] + zoomDx >= 0) {
                            Log.d(TAG, "touchEvent: 하아아" + scale);
                            if (width * scale < image.getWidth()) {
                                matrix.preScale(1, 1);
                                view.setImageMatrix(matrix);
                                getFinalX1();
                                return;
                            }

                            matrix.preScale(scale, scale,0, zoomDy);
                            matrix.preTranslate(0, zoomDy);
                            view.setImageMatrix(matrix);
                            return;

                        } else if ((((finalX1 - mid.x) * (scale - 1)) + finalX1 + zoomDx) <=1080) {
                            Log.d(TAG, "touchEvent: 뭐야 동작 왜이래?");
//                            matrix.postTranslate(-1080*(scale-1) , zoomDy);
//                            matrix.preScale(scale, scale);
//                            view.setImageMatrix(matrix);
//                            finalX1=1080;
//                            return;
                        }

                    if (event.getX() - values[Matrix.MTRANS_X] * scale < event.getX()) {
                        Log.d(TAG, "touchEvent: 목표 맞나?");
                        //여백이 생길때의 리스너
                        //matrix.preTranslate(0, zoomDy);
                        //matrix.setTranslate(0, mid.y/2);
                        //matrix.postScale(scale, scale, 0, zoomDy);
                        //matrix.setScale(scale, scale);



                        matrix.preScale(scale, scale,0, zoomDy);
                        matrix.preTranslate(0, zoomDy);

                        view.setImageMatrix(matrix);
                        return;

                    }

                        matrix.postTranslate(zoomDx, zoomDy);

                    if (width * scale < image.getWidth()) {

                        Log.d(TAG, "touchEvent: ??");
                        isReset = true;
                        matrix.setScale(1, 1);
                        matrix.setTranslate(0, mid.y/2);
                        mode = NONE;
                        lastEvent = null;

                        view.setImageMatrix(matrix);
                        return;

                    } else if (width * scale >= image.getWidth() * 16) {
                        Log.d(TAG, "touchEvent: ??? : zoom in : " + isZoomIn);
                        if (isZoomIn) {
                            Log.d(TAG, "touchEvent: ???");
                            isZoomIn = false;
                            matrix.preScale(4, 4, mid.x, mid.y);

                            mode = NONE;
                            lastEvent = null;
                            view.setImageMatrix(matrix);
                            return;
                        }

                        if (scale > 1) {
                            Log.d(TAG, "touchEvent: scale : " + scale);
                            return;

                        } else {
                            //Log.d(TAG, "touchEvent: scale else : "+scale);
                            isZoomIn = true;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }


                    }

                    matrix.postScale(scale, scale, mid.x, mid.y);

                    getFinalX1();

                }


                break;

            case MotionEvent.ACTION_UP: // 첫 손가락을 뗄 경우
                Log.d(TAG, "touchEvent: ACTION_UP");
                break;

            case MotionEvent.ACTION_POINTER_UP: // 두 손가락을 뗄 경우

                mode = NONE;
                lastEvent = null;

                if (isReset) {
                    matrix.setTranslate(0, (deviceHeight / 2) - (image.getHeight() * 1.5f));
                    isReset = false;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // 한 손가락이 있는 상태에서 두 손가락이 닿은 경우 = 핀치
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;

            case MotionEvent.ACTION_CANCEL:

            default:
                break;

        }

        view.setImageMatrix(matrix);


// 줌은 되지만 드래그가 안 되는 코드 ㅜㅜ
//    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
//        @Override
//        public boolean onScale(ScaleGestureDetector detector) {
//            mScaleFactor *= scaleGestureDetector.getScaleFactor();
//
//            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor,10.0f));
//
//            imageView.setScaleX(mScaleFactor);
//            imageView.setScaleY(mScaleFactor);
//
//            return true;
//        }
//    }

    }


    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }


    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private void getFinalX1(){
        finalX1 = ((finalX1 - mid.x) * (scale -lastScale - 1)) + finalX1 + zoomDx + dragDx;
        Log.d(TAG, "touchEvent: " + finalX1);
        zoomDx = 0;
        dragDx = 0;
        lastScale = scale;
        scale=1;
    }

}
