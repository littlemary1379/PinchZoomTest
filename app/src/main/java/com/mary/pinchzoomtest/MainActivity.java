package com.mary.pinchzoomtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowMetrics;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView imageView;
    private ConstraintLayout constraintLayout;

    private ImageZoomUtil imageZoomUtil;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //scaleGestureDetector=new ScaleGestureDetector(this, new ScaleListener());
        imageView = findViewById(R.id.imageViewTest);
        constraintLayout = findViewById(R.id.constraintLayoutInside);
        linearLayout = findViewById(R.id.linearLayout);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        setSelectedImage();
    }

    public Bitmap convertDrawableToBitmap(@DrawableRes int drawableId, int width, int height) {
        Drawable drawable = getDrawable(drawableId);

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        GradientDrawable gradientDrawable = (GradientDrawable) drawable;
        int w = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : width;
        int h = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : height;

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        gradientDrawable.setBounds(0, 0, w, h);
        gradientDrawable.setStroke(1, Color.BLACK);
        gradientDrawable.setFilterBitmap(true);
        gradientDrawable.draw(canvas);
        return bitmap;

    }

    public void setSelectedImage() {
        Bitmap image = convertDrawableToBitmap(R.drawable.main_flower, 50, 50);

        int nh = (int) (image.getHeight() * 410 / image.getWidth());

        Log.d(TAG, "setSelectedImage: nh :"+nh);

        Bitmap scaled = Bitmap.createScaledBitmap(image, 410, nh, true);
        imageView.setImageBitmap(scaled);

        int deviceWidth = linearLayout.getWidth();
        int deviceHeight = linearLayout.getHeight();

        imageView.setImageMatrix(new Matrix());
        imageZoomUtil = new ImageZoomUtil(scaled);
        imageZoomUtil.settingImage(imageView, deviceWidth, deviceHeight);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        imageZoomUtil.touchEvent(imageView, event);
        return true;
    }

}