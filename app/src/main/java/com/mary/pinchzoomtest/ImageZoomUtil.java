package com.mary.pinchzoomtest;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ImageZoomUtil {
    private static final String TAG = "MainActivity";

    private Bitmap image;

    private float width;

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

    private final int PINCHDRAG = 3;
    private final int PINCHZOOM = 4;

    int mode = NONE;
    int pinchMode = NONE;

    //초기에 Matrix 화면의 크기를 제어할 때, 받아오게 되는 Y location
    float setY = 0;

    //드래그 좌표 저장
    int posX1 = 0;
    int posX2 = 0;

    //스케일 저장
    float scale = 1f;

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
        setY = (deviceHeight / 2) - (displayY * 1.5f);
        view.setImageMatrix(matrix);

    }

    public void touchEvent(ImageView v, MotionEvent event) {

        ImageView view = v;

        int act = event.getAction();
        switch (act & MotionEvent.ACTION_MASK) {
            //손가락 하나 터치
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                posX1 = (int) event.getX();
                posX2 = (int) event.getY();
                break;

            //드래그
            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG) { //드래그 중일 시 XY값을 변화시키며 위치를 이동한다.

                    Log.d(TAG, "touchEvent: mode : 드래그");

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
                    matrix.postTranslate(dragDx, dragDy);

                } else if (mode == ZOOM) {

                    newDist = spacing(event);

                    matrix.set(savedMatrix);
                    scale = (newDist / oldDist);

                    matrix.set(savedMatrix);
                    zoomDx = event.getX() - start.x;
                    zoomDy = event.getY() - start.y;

                    float[] values = new float[9];
                    matrix.getValues(values);

                    width = values[Matrix.MSCALE_X] * image.getWidth();

//                    핀치 드래그
//                    if (scale > 0.95 && scale < 1.05) {
//
//                        pinchMode = PINCHDRAG;
//
//                        Log.d(TAG, "touchEvent: 핀치 이동 모션");
//
//                        Log.d(TAG, "touchEvent: : "+values[Matrix.MTRANS_X]);
//                        Log.d(TAG, "touchEvent: : "+zoomDx);
//
//                        if (values[Matrix.MTRANS_X] + zoomDx >= 0) {
//                            Log.d(TAG, "touchEvent: 핀치 x1 제어");
//                            matrix.postTranslate(0, zoomDy);
//                            break;
//
//                        } else if ((values[Matrix.MTRANS_X] + deviceWidth + zoomDx) <= deviceWidth) {
//                            Log.d(TAG, "touchEvent: 핀치 x2 제어");
//                            matrix.postTranslate(0, zoomDy);
//                            break;
//                        }
//
//                        matrix.postTranslate(zoomDx, zoomDy);
//                        break;
//
//                    } else {

                    Log.d(TAG, "touchEvent: 핀치 줌 모션");

                    if (width * scale < image.getWidth()) {

                        //줌인을 시작할 때, 터치 실수로 인해 줌이 줄어 아래의 translate 이동 코드가 동작되지 않도록 제어하는 코드
                        if(values[Matrix.MSCALE_X]<=1){
                            break;
                        }

                        Log.d(TAG, "touchEvent: ?? : zoom out 제한 코드");
                        isReset = true;
                        matrix.setScale(1, 1);
                        matrix.postTranslate(0, mid.y - (image.getWidth() / image.getHeight() * deviceWidth) / 2);
                        mode = NONE;
                        lastEvent = null;

                        view.setImageMatrix(matrix);
                        break;


                    } else if (width * scale >= image.getWidth() * 16) {
                        Log.d(TAG, "touchEvent: zoom in 제한 코드");
                        if (isZoomIn) {
                            isZoomIn = false;
                            matrix.preScale(4, 4, mid.x, mid.y);

                            mode = NONE;
                            lastEvent = null;
                            view.setImageMatrix(matrix);
                            return;
                        }

                        if (scale > 1) {
                            return;

                        } else {
                            isZoomIn = true;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }


                    }


//                        if (pinchMode == PINCHDRAG) {
//                            matrix.postTranslate(zoomDx, zoomDy);
//                        } else {

//                        }


                    matrix.postScale(scale, scale, mid.x, mid.y);
                    pinchMode = PINCHZOOM;

                }


                break;
//                }

            case MotionEvent.ACTION_UP: // 첫 손가락을 뗄 경우
                Log.d(TAG, "touchEvent: ACTION_UP");
                break;

            case MotionEvent.ACTION_POINTER_UP: // 두 손가락을 뗄 경우

                mode = NONE;
                lastEvent = null;

                float[] values = new float[9];
                matrix.getValues(values);

                if (values[Matrix.MTRANS_X] > 0) {
                    Log.d(TAG, "touchEvent: 줌 아웃 시 x1 왼쪽이 빔");
                    matrix.setScale(values[Matrix.MSCALE_X], values[Matrix.MSCALE_Y]);
                    matrix.postTranslate(0, values[Matrix.MTRANS_Y]);
                } else if (values[Matrix.MTRANS_X] + deviceWidth * values[Matrix.MSCALE_X] < deviceWidth) {
                    Log.d(TAG, "touchEvent: 줌 아웃 시 x2 오른쪽이 빔");
                    matrix.setScale(values[Matrix.MSCALE_X], values[Matrix.MSCALE_Y]);
                    matrix.postTranslate(-((values[Matrix.MSCALE_X] * deviceWidth) - deviceWidth), values[Matrix.MTRANS_Y]);
                }

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
