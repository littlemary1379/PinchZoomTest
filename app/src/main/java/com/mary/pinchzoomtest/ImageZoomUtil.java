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
    private float locationWidth;
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


    //드래그 좌표 저장
    int posX1 = 0;
    int posX2 = 0;

    //최종 좌표 저장
    float x0 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

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

                    dragDx = event.getX() - start.x;
                    dragDy = event.getY() - start.y;

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

                        if ((float) image.getHeight() / image.getWidth() * deviceWidth * finalScale > deviceHeight) {
                            Log.d(TAG, "touchEvent: 이게 얼만디" + ((deviceHeight - (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale)));

                            if (values[Matrix.MTRANS_Y] <= 0 && values[Matrix.MTRANS_Y] + dragDy > 0) {
                                Log.d(TAG, "touchEvent: x1 + y축이 커질때 y1 정지이벤트");
                                matrix.setScale(finalScale, finalScale);
                                matrix.postTranslate(0, 0);
                                break;
                            } else if (values[Matrix.MTRANS_Y] + (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale >= deviceHeight
                                    && values[Matrix.MTRANS_Y] + (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale + dragDy <= deviceHeight) {
                                Log.d(TAG, "touchEvent: x1 + y축이 커질때 y2 정지이벤트");
                                matrix.setScale(finalScale, finalScale);
                                matrix.postTranslate(0, deviceHeight - (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale);
                                break;
                            }

                        } else {
                            if (values[Matrix.MTRANS_Y] + dragDy <= 0) {
                                Log.d(TAG, "touchEvent: x1 + y1 정지이벤트");
                                matrix.setScale(finalScale, finalScale);
                                matrix.postTranslate(0, 0);

                            } else if (values[Matrix.MTRANS_Y] + (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale + dragDy > deviceHeight) {
                                Log.d(TAG, "touchEvent: x1 + y2 정지이벤트");
                                matrix.setScale(finalScale, finalScale);
                                matrix.postTranslate(0, deviceHeight - (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale);

                            }
                        }
                        break;
                    }

                    //시작점의 x좌표 + 원래 좌표길이 * 비율 == x2좌표?
                    if ((values[Matrix.MTRANS_X] + finalScale * deviceWidth + dragDx) <= deviceWidth) {
                        Log.d(TAG, "touchEvent: x2 정지이벤트");

                        matrix.setScale(finalScale, finalScale);
                        matrix.postTranslate((-(finalScale - 1) * deviceWidth), (values[Matrix.MTRANS_Y]) + dragDy);

                        if ((float) image.getHeight() / image.getWidth() * deviceWidth * finalScale > deviceHeight) {
                            Log.d(TAG, "touchEvent: 이게 얼만디" + ((deviceHeight - (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale)));

                            if (values[Matrix.MTRANS_Y] <= 0 && values[Matrix.MTRANS_Y] + dragDy > 0) {
                                Log.d(TAG, "touchEvent: x2 + y축이 커질때 y1 정지이벤트");
                                matrix.setScale(finalScale, finalScale);
                                matrix.postTranslate((-(finalScale - 1) * deviceWidth), 0);
                                break;
                            } else if (values[Matrix.MTRANS_Y] + (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale >= deviceHeight
                                    && values[Matrix.MTRANS_Y] + (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale + dragDy <= deviceHeight) {
                                Log.d(TAG, "touchEvent: x2 + y축이 커질때 y2 정지이벤트");
                                matrix.setScale(finalScale, finalScale);
                                matrix.postTranslate((-(finalScale - 1) * deviceWidth), deviceHeight - (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale);
                                break;
                            }

                        } else {
                            if (values[Matrix.MTRANS_Y] + dragDy <= 0) {
                                Log.d(TAG, "touchEvent: x2 + y1 정지이벤트");
                                matrix.setScale(finalScale, finalScale);
                                matrix.postTranslate((-(finalScale - 1) * deviceWidth), 0);

                            } else if (values[Matrix.MTRANS_Y] + (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale + dragDy > deviceHeight) {
                                Log.d(TAG, "touchEvent: x2 + y2 정지이벤트");
                                matrix.setScale(finalScale, finalScale);
                                matrix.postTranslate((-(finalScale - 1) * deviceWidth), deviceHeight - (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale);

                            }
                        }


                        break;
                    }

                    //드래그 시 화면을 넘지 못하도록 y조작 필요

                    if ((float) image.getHeight() / image.getWidth() * deviceWidth * finalScale > deviceHeight) {
                        Log.d(TAG, "touchEvent: 이게 얼만디" + ((deviceHeight - (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale)));

                        if (values[Matrix.MTRANS_Y] <= 0 && values[Matrix.MTRANS_Y] + dragDy > 0) {
                            Log.d(TAG, "touchEvent: y축이 커질때 y1 정지이벤트");
                            matrix.setScale(finalScale, finalScale);
                            matrix.postTranslate(values[Matrix.MTRANS_X] + dragDx, 0);
                            break;
                        } else if (values[Matrix.MTRANS_Y] + (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale >= deviceHeight
                                && values[Matrix.MTRANS_Y] + (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale + dragDy <= deviceHeight) {
                            Log.d(TAG, "touchEvent: y축이 커질때 y2 정지이벤트");
                            matrix.setScale(finalScale, finalScale);
                            matrix.postTranslate(values[Matrix.MTRANS_X] + dragDx, deviceHeight - (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale);
                            break;
                        }


                    } else {

                        if (values[Matrix.MTRANS_Y] + dragDy < 0) {
                            Log.d(TAG, "touchEvent: y1 정지이벤트");
                            matrix.setScale(finalScale, finalScale);
                            matrix.postTranslate(values[Matrix.MTRANS_X] + dragDx, 0);

                            view.setImageMatrix(matrix);
                            return;
                        } else if (values[Matrix.MTRANS_Y] + (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale + dragDy > deviceHeight) {
                            Log.d(TAG, "touchEvent: y2 정지이벤트");
                            matrix.setScale(finalScale, finalScale);
                            matrix.postTranslate(values[Matrix.MTRANS_X] + dragDx, deviceHeight - (float) image.getHeight() / image.getWidth() * deviceWidth * finalScale);

                            view.setImageMatrix(matrix);
                            return;
                        }
                    }


                    mode = DRAG;
                    Log.d(TAG, "touchEvent: mode : drag");
                    matrix.postTranslate(dragDx, dragDy);

                } else if (mode == ZOOM) {


                    newDist = spacing(event);

                    float[] values = new float[9];
                    matrix.getValues(values);

                    width = values[Matrix.MSCALE_X] * image.getWidth();
                    locationWidth = values[Matrix.MSCALE_X] * deviceWidth;

                    matrix.set(savedMatrix);
                    scale = (newDist / oldDist);

//                    Log.d(TAG, "touchEvent: values[Matrix.MTRANS_X] " + (((finalX1 - mid.x) * (scale - 1)) + finalX1 +
//                            zoomDx));
//                    Log.d(TAG, "touchEvent: dx : " + (mid.x));
//                    Log.d(TAG, "touchEvent: dx : " + (start.x));
//                    Log.d(TAG, "touchEvent: dx : " + (values[Matrix.MTRANS_X]));
//                    Log.d(TAG, "touchEvent: dx : " + deviceWidth);

                    matrix.set(savedMatrix);
                    zoomDx = event.getX(1) - start.x;
                    zoomDy = event.getY() - start.y;

                    if (scale > 0.95 && scale < 1.05) {

                        if (values[Matrix.MTRANS_X] + zoomDx >= 0) {

                            if (width * scale < image.getWidth()) {
                                Log.d(TAG, "touchEvent: 핀치 이동 x1 정지 이벤트");
                                matrix.preTranslate(0, zoomDy);
                                matrix.preScale(1, 1);
                                view.setImageMatrix(matrix);
                                return;
                            }

                            matrix.preScale(scale, scale);
                            matrix.postTranslate(0, zoomDy);
                            view.setImageMatrix(matrix);
                            return;

                        } else if ((values[Matrix.MTRANS_X] - (mid.x - values[Matrix.MTRANS_X]) * (scale - 1) + locationWidth * scale + zoomDx) <= deviceWidth) {

                            matrix.postScale(scale, scale, mid.x, mid.y);
                            matrix.postTranslate(0, zoomDy);

                            view.setImageMatrix(matrix);
                            return;
                        }

                        matrix.postTranslate(zoomDx, zoomDy);
                    }


//                    if (event.getX() - values[Matrix.MTRANS_X] * scale < event.getX()) {
//                        Log.d(TAG, "touchEvent: 목표 맞나?");
//                        //여백이 생길때의 리스너
//                        //matrix.preTranslate(0, zoomDy);
//                        //matrix.setTranslate(0, mid.y/2);
//                        //matrix.postScale(scale, scale, 0, zoomDy);
//                        //matrix.setScale(scale, scale);
//
//
//                        matrix.preScale(scale, scale, 0, zoomDy);
//                        matrix.preTranslate(0, zoomDy);
//
//                        view.setImageMatrix(matrix);
//                        return;
//
//                    }


                    if (width * scale < image.getWidth()) {

                        Log.d(TAG, "touchEvent: ??");
                        Log.d(TAG, "touchEvent: ?? : zoom out 제한 코드");
                        isReset = true;
                        matrix.setScale(1, 1);
                        matrix.setTranslate(0, mid.y / 2);
                        mode = NONE;
                        lastEvent = null;

                        view.setImageMatrix(matrix);
                        return;

                    } else if (width * scale >= image.getWidth() * 16) {
                        Log.d(TAG, "touchEvent: ??? : zoom in : " + isZoomIn);
                        Log.d(TAG, "touchEvent: ??? : zoom in 제한 코드");
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
                savedMatrix.set(matrix);
                midPoint(mid, event);
                mode = ZOOM;
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


}
