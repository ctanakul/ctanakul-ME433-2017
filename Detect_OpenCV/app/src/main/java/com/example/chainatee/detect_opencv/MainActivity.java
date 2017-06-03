package com.example.chainatee.detect_opencv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.IOException;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static android.graphics.Color.rgb;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;

public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {
    private Camera mCamera;
    private TextureView mTextureView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
//    private Bitmap bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
    private Bitmap bmp = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888);
    private Canvas canvas = new Canvas(bmp);
    private Paint paint1 = new Paint();
    private TextView mTextView;
    private static final String TAG = "MainActivity";

    private Mat tmp = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC3);

    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    static long prevtime = 0; // for FPS calculation

    SeekBar sensitivityControl;
    SeekBar sensitivityControl2;
    int threshgb = 40;
    int val1 = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keeps the screen from turning off

        mTextView = (TextView) findViewById(R.id.cameraStatus);

        // see if the app has permission to use the camera
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
            mSurfaceHolder = mSurfaceView.getHolder();

            mTextureView = (TextureView) findViewById(R.id.textureview);
            mTextureView.setSurfaceTextureListener(this);

            // set the paintbrush for writing text on the image
            paint1.setColor(0xffff0000); // red
            paint1.setTextSize(24);
            sensitivityControl = (SeekBar) findViewById(R.id.seek1);
            sensitivityControl2 = (SeekBar) findViewById(R.id.seek2);
            mTextView.setText("started camera");
        } else {
            mTextView.setText("no camera permissions");
        }
        setMyControlListener();
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(640, 480);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY); // no autofocusing
        parameters.setAutoExposureLock(true); // keep the white balance constant
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90); // rotate to portrait mode

        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    // the important function
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // every time there is a new Camera preview frame
        mTextureView.getBitmap(bmp);

        final Canvas c = mSurfaceHolder.lockCanvas();
        if (c != null) {
            // ################################## Homework 13 ######################################
            // modify myself
//            int threshgr = 0; // comparison value between green and red
////            int threshgb = 40; // comparison value between green and blue
//            int[] pixels = new int[bmp.getWidth()]; // pixels[] is the RGBA data
//            int trackedY = 0;
//            for (int j = 0; j < bmp.getHeight(); j++) {
//                trackedY = j;
//                bmp.getPixels(pixels, 0, bmp.getWidth(), 0, trackedY, bmp.getWidth(), 1);
//                // in the row, see if there is more green than red
//                for (int i = 0; i < bmp.getWidth(); i++) {
//                    if (((green(pixels[i]) - red(pixels[i])) > threshgr) && ((green(pixels[i]) - blue(pixels[i])) > threshgb)) {
//                        pixels[i] = rgb(0, 255, 0); // over write the pixel with pure green
//                    }
//                }
//                // update the row
//                bmp.setPixels(pixels, 0, bmp.getWidth(), 0, j, bmp.getWidth(), 1);
//            }
            // ################################ (END) Homework 13 ####################################
            // Modify for OpenCV

            // Image processing
//            Mat tmp = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC3);
            Utils.bitmapToMat(bmp, tmp);
//            Core.inRange(tmp, new Scalar(0, 20, 100, 0), new Scalar(255, 255, 255, 255), tmp);
            Core.inRange(tmp, new Scalar(val1, 20, 100, 0), new Scalar(255, 255, 255, 255), tmp);
            Utils.matToBitmap(tmp, bmp);
//            Core.inRange(src, new Scalar(20, 100, 100), new Scalar(30, 255, 255), dst);
        }

        // draw a circle at some position
        int pos = 50;
        canvas.drawCircle(pos, 240, 5, paint1); // x position, y position, diameter, color

        // write the pos as text
//        canvas.drawText("pos = " + pos, 10, 200, paint1);
        canvas.drawText("threshGB = " + threshgb, 10, 200, paint1);
        c.drawBitmap(bmp, 0, 0, null);
        mSurfaceHolder.unlockCanvasAndPost(c);

        // calculate the FPS to see how fast the code is running
        long nowtime = System.currentTimeMillis();
        long diff = nowtime - prevtime;
        mTextView.setText("FPS " + 1000 / diff);
        prevtime = nowtime;
    }

    private void setMyControlListener() {
        sensitivityControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                progressChanged = threshgb;
//                myTextView.setText("The value is: "+progress);
                threshgb = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        sensitivityControl2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                progressChanged = threshgb;
//                myTextView.setText("The value is: "+progress);
                val1 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

}
