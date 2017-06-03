package com.example.chainatee.detect_opencv;

/**
 * Created by chainatee on 6/3/17.
 */
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.TimingLogger;

class Sample1View extends SampleViewBase {
    public static final int  VIEW_MODE_RGBA   = 0;
    public static final int  VIEW_MODE_BLUE  = 1;
    public static final int  VIEW_MODE_YELLOW = 2;
    private Mat mYuv;
    private Mat mRgba;
    private Mat mGraySubmat;
    private Mat mIntermediateMat;
    private Bitmap mBitmap;
    private int mViewMode;
    private Mat mColor;
    private Mat mResult;
    private Mat mHsv;
    TimingLogger timings;

    public Sample1View(Context context) {
        super(context);
        mViewMode = VIEW_MODE_RGBA;
    }

    @Override
    protected void onPreviewStarted(int previewWidth, int previewHeight) {
        synchronized (this) {
            mYuv = new Mat(getFrameHeight() + getFrameHeight() / 2, getFrameWidth(), CvType.CV_8UC1);
            mGraySubmat = mYuv.submat(0, getFrameHeight(), 0, getFrameWidth());
            mRgba = new Mat();
            mIntermediateMat = new Mat();
            mBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
            mHsv = new Mat();
            mColor = new Mat();
            mResult = new Mat();
        }
    }

    @Override
    protected void onPreviewStopped() {
        if(mBitmap != null) {
            mBitmap.recycle();
        }

        synchronized (this) {
            // Explicitly deallocate Mats
            if (mYuv != null)
                mYuv.release();
            if (mRgba != null)
                mRgba.release();
            if (mGraySubmat != null)
                mGraySubmat.release();
            if (mIntermediateMat != null)
                mIntermediateMat.release();
            mYuv = null;
            mRgba = null;
            mGraySubmat = null;
            mIntermediateMat = null;
            if (mResult != null)
                mResult.release();
            if (mHsv != null)
                mHsv.release();
            if (mColor != null)
                mColor.release();
            mColor = null;
            mResult = null;
            mHsv = null;
        }
    }

    @Override
    protected Bitmap processFrame(byte[] data) {
        mYuv.put(0, 0, data);
        final int viewMode = mViewMode;

        switch (viewMode) {
            case VIEW_MODE_RGBA:
                Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
                break;
            case VIEW_MODE_YELLOW:
                ColorDetection.cvt_YUVtoRGBtoHSV(mYuv,mHsv);
                ColorDetection.getYellowMat(mHsv,mColor);

                ColorDetection.detectSingleBlob(mYuv, mColor, "Y", mResult);
                Imgproc.cvtColor(mResult, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
                break;
            case VIEW_MODE_BLUE:
                ColorDetection.cvt_YUVtoRGBtoHSV(mYuv,mHsv);
                ColorDetection.getBlueMat(mHsv,mColor);

                ColorDetection.detectSingleBlob(mYuv, mColor, "B", mResult);
                Imgproc.cvtColor(mResult, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
                break;
        }

        Bitmap bmp = mBitmap;

        try {
            Utils.matToBitmap(mRgba, bmp);
        } catch(Exception e) {
            Log.e("org.opencv.samples.tutorial1", "Utils.matToBitmap() throws an exception: " + e.getMessage());
            bmp.recycle();
            bmp = null;
        }
        return bmp;
    }

    public void setViewMode(int viewMode) {
        mViewMode = viewMode;
    }
}
//public class Sample1View {
//}
