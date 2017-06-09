package com.example.chainatee.littleuber;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ScrollView;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.WindowManager;

import org.opencv.android.OpenCVLoader;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static android.graphics.Color.rgb;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{
//    public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{
    SeekBar myControl;
    SeekBar myControl2;
    SeekBar myControl3;
    SeekBar myControl4;
    SeekBar myControl5;
    SeekBar myControlMidpoint;
    SeekBar myControlNickR;
    SeekBar myControlNickT;
    SeekBar myControlEmergencyVelTurnRight;
    SeekBar myControlEmergencyVelTurnLeft;
    SeekBar myControlEmergencyThresholdRight;
    SeekBar myControlEmergencyThresholdLeft;
    TextView myTextView;
    TextView myTextView4;
    TextView NickColorTextView;
//    Button button;
//    TextView myTextView2;
//    ScrollView myScrollView;
    TextView myTextView3;

    private UsbManager manager;
    private UsbSerialPort sPort;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;

    /////////////////HW13
    private Camera mCamera;
    private TextureView mTextureView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
//    private Bitmap bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
    private Bitmap bmp = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888);
    private Canvas canvas = new Canvas(bmp);
    private Paint paint1 = new Paint();
    private TextView mTextView;

    static long prevtime = 0; // for FPS calculation
    int threshgb = 40;
    int oldmidpointx = 160;
    boolean LeftFlag = false;
    boolean RightFlag = false;
    /////////////////HW13 (END)

    ////////////////////////////OpenCV
    private static final String TAG = "MainActivity";
    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        };
    }
    private Mat tmp = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC4);
    private Mat tmp_bitwise_mask = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);

    int valRmin = 128;
    int valRmax = 255;
    int valGmin = 85;
    int valGmax = 255;
    int valBmin = 0;
    int valBmax = 137;
    private TextView valRGBMinMaxTextView;

    ////////////////////////////OpenCV (END)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myControl = (SeekBar) findViewById(R.id.seekvelMaxf);
        myControl2 = (SeekBar) findViewById(R.id.seek2);
        myControl3 = (SeekBar) findViewById(R.id.seek3);
        myControl4 = (SeekBar) findViewById(R.id.seek4);
        myControl5 = (SeekBar) findViewById(R.id.seek5);
        myControlNickR = (SeekBar) findViewById(R.id.seekNickR);
        myControlNickT = (SeekBar) findViewById(R.id.seekNickT);
        myControlMidpoint = (SeekBar) findViewById(R.id.seekMidpoint);
        myControlEmergencyVelTurnRight = (SeekBar) findViewById(R.id.seekEmergencyVelTurnRight);
        myControlEmergencyVelTurnLeft = (SeekBar) findViewById(R.id.seekEmergencyVelTurnLeft);
        myControlEmergencyThresholdRight = (SeekBar) findViewById(R.id.seekEmergencyThresholdRight);
        myControlEmergencyThresholdLeft = (SeekBar) findViewById(R.id.seekEmergencyThresholdLeft);

        myTextView = (TextView) findViewById(R.id.textView01);
        myTextView4 = (TextView) findViewById(R.id.textView04);
        NickColorTextView = (TextView) findViewById(R.id.textViewNick);
        myTextView.setText("Enter whatever you Like!");
        setMyControlListener();
        valRGBMinMaxTextView = (TextView) findViewById(R.id.valRGBMinMaxStatus);
//        myTextView2 = (TextView) findViewById(R.id.textView02);
//        myScrollView = (ScrollView) findViewById(R.id.ScrollView01);
        myTextView3 = (TextView) findViewById(R.id.textView03);
//        button = (Button) findViewById(R.id.button1);

        /////////////////HW13
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keeps the screen from turning off
        mTextView = (TextView) findViewById(R.id.cameraStatus);
        // see if the app has permission to use the camera
//        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
            mSurfaceHolder = mSurfaceView.getHolder();

            mTextureView = (TextureView) findViewById(R.id.textureview);
            mTextureView.setSurfaceTextureListener(this);

            // set the paintbrush for writing text on the image
            paint1.setColor(0xffff0000); // red
            paint1.setTextSize(24);

            mTextView.setText("started camera");
        } else {
            mTextView.setText("no camera permissions");
        }
        /////////////////HW13 (END)

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                myTextView2.setText("value on click is "+myControl.getProgress() + " &" +myControl2.getProgress());
//                String sendString = String.valueOf(myControl.getProgress()) + ' ' +String.valueOf(myControl2.getProgress()) + '\n';
////                String sendString = String.valueOf(myControl.getProgress()) + '\n';
//                try {
//                    sPort.write(sendString.getBytes(), 10); // 10 is the timeout
//                } catch (IOException e) { }
//            }
//        });

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);




        RangeSeekBar rangeSeekbarValR = (RangeSeekBar) findViewById(R.id.rangeSeekbarValR);
        RangeSeekBar rangeSeekbarValG = (RangeSeekBar) findViewById(R.id.rangeSeekbarValG);
        RangeSeekBar rangeSeekbarValB = (RangeSeekBar) findViewById(R.id.rangeSeekbarValB);
        rangeSeekbarValR.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
//                Toast.makeText(getApplicationContext(), "Min Value- " + minValue + " & " + "Max Value- " + maxValue, Toast.LENGTH_LONG).show();
                valRmin = (int) minValue;
                valRmax = (int) maxValue;
            }
        });
        rangeSeekbarValG.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
//                Toast.makeText(getApplicationContext(), "Min Value- " + minValue + " & " + "Max Value- " + maxValue, Toast.LENGTH_LONG).show();
                valGmin = (int) minValue;
                valGmax = (int) maxValue;
            }
        });
        rangeSeekbarValB.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
//                Toast.makeText(getApplicationContext(), "Min Value- " + minValue + " & " + "Max Value- " + maxValue, Toast.LENGTH_LONG).show();
                valBmin = (int) minValue;
                valBmax = (int) maxValue;
            }
        });
        rangeSeekbarValR.setNotifyWhileDragging(true);
        rangeSeekbarValG.setNotifyWhileDragging(true);
        rangeSeekbarValB.setNotifyWhileDragging(true);

    }

    ///////////////////////HW13
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
//        parameters.setPreviewSize(640, 480);
        parameters.setPreviewSize(320, 240);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY); // no autofocusing
        parameters.setAutoExposureLock(false); // keep the white balance constant
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
        float sumMR = 0;
        float sumM = 0;
//        int des_point = 0;
//        int trackedY = 50;
//        int midpoint =240*3/4;
        int midpoint = (int) (1.0* myControlMidpoint.getProgress()/100 * 240);
        int midpointx = 0;
        int futurepoint = 240*3/4;
        int futurepointx = 0;
        final Canvas c = mSurfaceHolder.lockCanvas();
        //Velocity variables
        int velMax=0;

        double velMaxf = 1.0 * myControl.getProgress() / myControl.getMax();
        double turnVelf = 1.0 * myControl2.getProgress() / myControl2.getMax();
        int velL = 0;
        int velR = 0;
        if (c != null) {
//            int thresh = 0; // comparison value
//            int[] pixels = new int[bmp.getWidth()]; // pixels[] is the RGBA data
//            int startY = 200; // which row in the bitmap to analyze to read
//            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, startY, bmp.getWidth(), 1);
//
//            // in the row, see if there is more green than red
//            for (int i = 0; i < bmp.getWidth(); i++) {
//                if ((green(pixels[i]) - red(pixels[i])) > thresh) {
//                    pixels[i] = rgb(0, 255, 0); // over write the pixel with pure green
//                }
//            }
//
//            // update the row
//            bmp.setPixels(pixels, 0, bmp.getWidth(), 0, startY, bmp.getWidth(), 1);


            // modify myself
//            int threshgr = 0; // comparison value between green and red
////            int threshgb = 40; // comparison value between green and blue
//            int[] pixels = new int[bmp.getWidth()]; // pixels[] is the RGBA data
//            int trackedY = 0;
////            for (int j = 0; j < bmp.getHeight(); j++) {
//                trackedY = 50;
//                bmp.getPixels(pixels, 0, bmp.getWidth(), 0, trackedY, bmp.getWidth(), 1);
//                // in the row, see if there is more green than red
//                for (int i = 0; i < bmp.getWidth(); i++) {
//                    if (((green(pixels[i]) - red(pixels[i])) > threshgr) && ((green(pixels[i]) - blue(pixels[i])) > threshgb)) {
//                        pixels[i] = rgb(0, 255, 0); // over write the pixel with pure green
//                    }
//                }
//                // update the row
////                bmp.setPixels(pixels, 0, bmp.getWidth(), 0, j, bmp.getWidth(), 1);
//                bmp.setPixels(pixels, 0, bmp.getWidth(), 0, trackedY, bmp.getWidth(), 1);
////            }

            // Nick's red dot
            int COM = 0;
            int R = myControlNickR.getProgress();
            int T = myControlNickT.getProgress();
            int[] pixels = new int[bmp.getWidth()]; // pixels[] is the RGBA data
            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, midpoint, bmp.getWidth(), 1);
            for (int i = 0; i < bmp.getWidth(); i++) {
                if (((green(pixels[i]) - red(pixels[i])) > -R)&&((green(pixels[i]) - red(pixels[i])) < R)&&(green(pixels[i])  > T)) {
                    pixels[i] = rgb(1, 1, 1); // set the pixel to almost 100% black

                    sumM = sumM + green(pixels[i])+red(pixels[i])+blue(pixels[i]);
                    sumMR = sumMR + (green(pixels[i])+red(pixels[i])+blue(pixels[i]))*i;
                }
            }
            // only use the data if there were a few pixels identified, otherwise you might get a divide by 0 error
            if(sumM > 5){
                midpointx = (int) (sumMR / sumM);
            }
            else{
                midpointx = 0;
            }
            canvas.drawCircle(midpointx, midpoint, 3, paint1); // x position, y position, diameter, color

            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, futurepoint, bmp.getWidth(), 1);
            for (int i = 0; i < bmp.getWidth(); i++) {
                if (((green(pixels[i]) - red(pixels[i])) > -R)&&((green(pixels[i]) - red(pixels[i])) < R)&&(green(pixels[i])  > T)) {
                    pixels[i] = rgb(1, 1, 1); // set the pixel to almost 100% black

                    sumM = sumM + green(pixels[i])+red(pixels[i])+blue(pixels[i]);
                    sumMR = sumMR + (green(pixels[i])+red(pixels[i])+blue(pixels[i]))*i;
                }
            }
            // only use the data if there were a few pixels identified, otherwise you might get a divide by 0 error
            if(sumM > 5){
                futurepointx = (int) (sumMR / sumM);
            }
            else{
                futurepointx = 0;
            }
            canvas.drawCircle(futurepointx, futurepoint, 3, paint1); // x position, y position, diameter, color


            NickColorTextView.setText("1.R: "+ R + " 2.T: " + T);
            // Nick's red dot (END)


            //OpenCV
//            Utils.bitmapToMat(bmp, tmp);
//            Core.inRange(tmp, new Scalar(valRmin, valGmin, valBmin, 0), new Scalar(valRmax, valGmax, valBmax, 255), tmp_bitwise_mask);
//            for (int i = 0; i < bmp.getWidth() - 1; i++) {
//                if ((int) tmp_bitwise_mask.get(midpoint, i)[0] > 0) {
//                    sumMR = sumMR + i;
//                    sumM = sumM + 1;
//                }
//            }
//            midpointx = (int) (sumMR / sumM);
//            sumMR = 0;
//            sumM = 0;
//            for (int i = 0; i < bmp.getWidth() - 1; i++) {
//                if ((int) tmp_bitwise_mask.get(futurepoint, i)[0] > 0) {
//                    sumMR = sumMR + i;
//                    sumM = sumM + 1;
//                }
//            }
//            futurepointx = (int) (sumMR / sumM);
//
//            Utils.matToBitmap(tmp_bitwise_mask, bmp);
//            canvas.drawCircle(midpointx, midpoint, 3, paint1); // x position, y position, diameter, color
//            canvas.drawCircle(futurepointx, futurepoint, 3, paint1); // x position, y position, diameter, color
            //OpenCV (END)
//            threshgb = des_point;

        }

        int pos = 50;
//        canvas.drawCircle(pos, 240, 5, paint1); // x position, y position, diameter, color

        // write the pos as text
//        canvas.drawText("pos = " + pos, 10, 200, paint1);
        canvas.drawText("futurepointx = " + midpointx, 10, 190, paint1);
        canvas.drawText("midpointx = " + midpointx, 10, 210, paint1);



        //////////Method1
//        velMax = velMaxLim;
//        //set maximum velocity based on future point
//        if(Math.abs(futurepointx-160) > 0.3*160){
//            velMax = (int)(0.7*velMaxLim);
//        }
////        else {
////            velMax = (int) ((velMaxLim - (int)(velMaxf*(Math.abs(futurepointx-160) * velMaxLim / 160))));
////        }
////        velMax = (int) ((100 - (int)(velMaxf*(Math.abs(futurepointx-160) * 100 / 160))));
////        velMax = (int) (velMaxf*(100 - (int)(Math.abs(futurepointx-160) * 100 / 160)));
//
////        velL = velMax;
////        velR = velMax;
//
//        //set vel left and right based on midpointx
//        if (midpointx < 160) {
//            velL = velMax - (int)(turnVelf * Math.abs(midpointx-160) * velMax / 160);
//            velR = velMax;
//        } else if (midpointx > 160) {
//            velR = velMax - (int)(turnVelf * Math.abs(midpointx-160) * velMax / 160);
//            velL = velMax;
//        } else {
//            velL = velMax;
//            velR = velMax;
//        }
        /////////Method1(END)


        ////////Method2 (Stop moving one wheel if the point is oveer some threshold)
//        velMax= myControl.getProgress(); //40 is great
//        double slowVelturnf = 1.0*myControl3.getProgress()/myControl3.getMax();
//        double addVelf = 1.0*myControl4.getProgress()/myControl4.getMax();
//        double addVelfQuad = 1.0*myControl2.getProgress()/myControl2.getMax();
//        double slowWheelVelTurnQuadf = 1.0*myControl5.getProgress()/myControl5.getMax();
//        int velMaxTurnWheelAdd = 100 - velMax;
//        int offsetAbs = Math.abs(midpointx-160);
//        // if the point is to the right more than 30% 0f width, stop turning the right wheel
//        if(midpointx-160 > 20) {
//
//            if(midpointx-160 > 80) {
//                velL = velMax + (int)(addVelfQuad * velMaxTurnWheelAdd * Math.pow((1.0*offsetAbs / 160),2));
//                velR = velMax - (int)(slowWheelVelTurnQuadf*velMax);
//            } else {
//                velL = velMax + (int)(addVelf * velMaxTurnWheelAdd * offsetAbs / 160); // 4 is great
//                velR = velMax - (int)(slowVelturnf*velMax);
//            }
//        } else if (midpointx-160 < -20) {
//
//            if(midpointx-160 < -80) {
//                velR = velMax + (int)(addVelfQuad * velMaxTurnWheelAdd * Math.pow((1.0 * offsetAbs / 160),2));
//                velL = velMax - (int)(slowWheelVelTurnQuadf*velMax);
//            } else {
//                velR = velMax + (int)(addVelf * velMaxTurnWheelAdd * offsetAbs / 160); // 4 is great
//                velL = velMax - (int)(slowVelturnf*velMax);
//            }
//        } else {
//            velL = velMax;
//            velR = velMax;
//        }
//        valRGBMinMaxTextView.setText(" 1)velMax = " + (velMax) + " 2.)addVelfQuad = " + addVelfQuad + "\r\n" +
//                "3.)slowturnVelf = " + slowVelturnf + " 4.)addVelf = " + addVelf + "\r\n" +
//                "5.) slowWheelVelTurnQuadf = " + slowWheelVelTurnQuadf + "6.)Midpoint = " + midpoint + "\r\n" +
//                "R: " + valRmin + "-" + valRmax + " G: " + valGmin + "-" + valGmax + " B: " + valBmin + "-" + valBmax + "\r\n" +
//                "velL = " + velL + " velR = " + velR + " midpointx = " + midpointx + "\r\n" +
//                "offsetAbs = " + offsetAbs );
        ////////Method2 (END)

        ////////METHOD3
        velMax = myControl.getProgress(); //40 is great2
        double VelHighf = 1.0*myControl2.getProgress()/myControl2.getMax() ;
        double VelLowf = 1.0*myControl3.getProgress()/myControl3.getMax() ;
        int velMaxLim = myControl4.getProgress();
        int offset = midpointx-160;
        int velMaxTurnEmergencyRight = myControlEmergencyVelTurnRight.getProgress();
        int velMaxTurnEmergencyLeft = myControlEmergencyVelTurnLeft.getProgress();
        int EmergencyThresholdRight = (int)(myControlEmergencyThresholdRight.getProgress() / 100.0 * 160);
        int EmergencyThresholdLeft = (int)(myControlEmergencyThresholdLeft.getProgress() / 100.0 * 160);

//        if (Math.abs(midpointx - oldmidpointx) < 20){
//            oldmidpointx = midpointx;
//            if (midpointx > 160){
//                velL = (int)((1.0 * VelHighf * Math.pow(1.0 * Math.abs(offset) / 160 ,2) * (velMaxLim - velMax)) + velMax);
//                velR = (int)(velMax - (VelLowf * Math.pow(1.0 * Math.abs(offset) / 160, 2) * velMax));
//                if (Math.abs(offset) > EmergencyThresholdRight ){
////                velL = (int)((1.0 * VelHighf * Math.pow(1.0 * 288 / 160 ,2) * (velMaxLim - velMax)) + velMax);
//                    velL = velMaxTurnEmergencyRight;
//                    velR = 0;
//                }
//            } else if (midpointx < 160){
//                velR = (int)((1.0 * VelHighf * Math.pow(1.0 * Math.abs(offset) / 160 ,2) * (velMaxLim - velMax)) + velMax);
//                velL = (int)(velMax - (VelLowf * Math.pow(1.0 * Math.abs(offset) / 160, 2) * velMax));
//                if ((Math.abs(offset) > EmergencyThresholdLeft)){
////                velL = (int)((1.0 * VelHighf * Math.pow(1.0 * 288 / 160 ,2) * (velMaxLim - velMax)) + velMax);
//                    velR = velMaxTurnEmergencyLeft;
//                    velL = 0;
//                }
//            } else if (midpointx == 0){
//                velL = velMaxTurnEmergencyRight;
//                velR = 0;
//            } else {
//                velL = velMax;
//                velR = velMax;
//            }
//        } else {
//            velL = velMaxTurnEmergencyRight;
//            velR = 0;
//        }

        if ((futurepointx > midpointx)) {
            RightFlag = true;
            LeftFlag = false;
        } else if ((futurepointx < midpointx) && (futurepointx > 0)) {
            LeftFlag = true;
            RightFlag = false;
        }



        if (midpointx > 160 && midpointx < 319){
            velL = (int)((1.0 * VelHighf * Math.pow(1.0 * Math.abs(offset) / 160 ,2) * (velMaxLim - velMax)) + velMax);
            velR = (int)(velMax - (VelLowf * Math.pow(1.0 * Math.abs(offset) / 160, 2) * velMax));
            if (Math.abs(offset) > EmergencyThresholdRight ){
//                velL = (int)((1.0 * VelHighf * Math.pow(1.0 * 288 / 160 ,2) * (velMaxLim - velMax)) + velMax);
                velL = velMaxTurnEmergencyRight;
                velR = 0;
            }
        } else if (midpointx < 160 && midpointx > 0){
            velR = (int)((1.0 * VelHighf * Math.pow(1.0 * Math.abs(offset) / 160 ,2) * (velMaxLim - velMax)) + velMax);
            velL = (int)(velMax - (VelLowf * Math.pow(1.0 * Math.abs(offset) / 160, 2) * velMax));
            if ((Math.abs(offset) > EmergencyThresholdLeft)){
//                velL = (int)((1.0 * VelHighf * Math.pow(1.0 * 288 / 160 ,2) * (velMaxLim - velMax)) + velMax);
                velR = velMaxTurnEmergencyLeft;
                velL = 0;
            }
        } else if (midpointx == 0 || midpointx == 319) {
            if (RightFlag) {
                velL = velMaxTurnEmergencyRight;
                velR = 0;
            }
            if (LeftFlag) {
                velR = velMaxTurnEmergencyRight;
                velL = 0;
            }


        } else {
            velL = velMax;
            velR = velMax;
        }


        valRGBMinMaxTextView.setText(" 1)velMax = " + (velMax) + " 2.)VelHighf = " + VelHighf + "\r\n" +
                "3.)VelLowf = " + VelLowf + "4.)velMaxLim = " + velMaxLim + "2.1.)Midpoint = " + midpoint + "\r\n" +
                "R: " + valRmin + "-" + valRmax + " G: " + valGmin + "-" + valGmax + " B: " + valBmin + "-" + valBmax + "\r\n" +
                "velL = " + velL + " velR = " + velR + " midpointx = " + midpointx + "\r\n" +
                "offset = " + offset + " 2.2)velMaxTurnEmergencyRight = " + velMaxTurnEmergencyRight + " 2.3)velMaxTurnEmergencyLeft = " + velMaxTurnEmergencyLeft + "\r\n" +
                " 2.4)EmergencyThresholdRight = " + EmergencyThresholdRight + "\r\n" +
                " 2.5)EmergencyThresholdLeft = " + EmergencyThresholdLeft);

        ////////METHOD3 (END)


//        canvas.drawText("velMax = " + (velMax), 10, 30, paint1);
//        canvas.drawText("velL = " + velL, 10, 60, paint1);
//        canvas.drawText("velR = " + velR, 10, 90, paint1);
//        canvas.drawText("slowturnVelf = " + slowVelf, 10, 120, paint1);

        c.drawBitmap(bmp, 0, 0, null);
        mSurfaceHolder.unlockCanvasAndPost(c);

        // calculate the FPS to see how fast the code is running
        long nowtime = System.currentTimeMillis();
        long diff = nowtime - prevtime;
        mTextView.setText("FPS " + 1000 / diff);
        prevtime = nowtime;



//        valRGBMinMaxTextView.setText("R: " + valRmin + "-" + valRmax + " G: " + valGmin + "-" + valGmax + " B: " + valBmin + "-" + valBmax + "\r\n" +
//                                    "slowturnVelf = " + slowVelturnf + " velMax = " + (velMax) + "\r\n" +
//                                    " velL = " + velL + " velR = " + velR + " addVelf = " + addVelf + " \r\n" +
//                                    " addVelfQuad = " + addVelfQuad + "midpointx = " + midpointx + "\r\n" +
//                                    "offsetAbs = " + offsetAbs + "slowWheelVelTurnQuadf = " + slowWheelVelTurnQuadf);
//        String sendString = String.valueOf(myControl.getProgress()) + ' ' +String.valueOf(myControl2.getProgress()) + '\n';


//        mSurfaceHolder.unlockCanvasAndPost(c);
        String sendString = String.valueOf(velR) + ' ' + String.valueOf(velL) + '\n' ;

//                String sendString = String.valueOf(myControl.getProgress()) + '\n';

        // tRY SENDING STREAM OF data
        try {
            sPort.write(sendString.getBytes(), 10); // 10 is the timeout
        } catch (IOException e) { }
        // tRY SENDING STREAM OF data (END)

    }

    ///////////////////////HW13(END)


    private void setMyControlListener() {
        myControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
                myTextView.setText("The value is: "+ progress + " and " + myControl2.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        myControl2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
                myTextView4.setText("The value is: "+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {
                @Override
                public void onRunError(Exception e) {

                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };

    @Override
    protected void onPause(){
        super.onPause();
        stopIoManager();
        if(sPort != null){
            try{
                sPort.close();
            } catch (IOException e){ }
            sPort = null;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x04D8,0x000A, CdcAcmSerialDriver.class);
        UsbSerialProber prober = new UsbSerialProber(customTable);

        final List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);

        if(availableDrivers.isEmpty()) {
            //check
            return;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        sPort = driver.getPorts().get(0);

        if (sPort == null){
            //check
        }else{
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());
            if (connection == null){
                //check
                PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
                usbManager.requestPermission(driver.getDevice(), pi);
                return;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            }catch (IOException e) {
                //check
                try{
                    sPort.close();
                } catch (IOException e1) { }
                sPort = null;
                return;
            }
        }
        onDeviceStateChange();
    }

    private void stopIoManager(){
        if(mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if(sPort != null){
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange(){
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {
        //do something with received data

        //for displaying:
        String rxString = null;
        try {
            rxString = new String(data, "UTF-8"); // put the data you got into a string
            myTextView3.setText(rxString);
//            myScrollView.fullScroll(View.FOCUS_DOWN);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}