package com.sukshi.sukshicamerademo;

/*
 * Vishwam Corp CONFIDENTIAL

 * Vishwam Corp 2018
 * All Rights Reserved.

 * NOTICE:  All information contained herein is, and remains
 * the property of Vishwam Corp. The intellectual and technical concepts contained
 * herein are proprietary to Vishwam Corp
 * and are protected by trade secret or copyright law of U.S.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Vishwam Corp
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.sukshi.sukshicamerademo.FaceDect.previewFaceDetector;


public class CameraActivity extends AppCompatActivity implements FaceDect.OnMultipleFacesDetectedListener, FaceDect.OnCaptureListener, FaceDect.OnFaceUpdateListener {

    private static final String TAG = "Custom Camera";
    public static final String IMAGE_BITMAP = "bitmap";
    private Context context;
    public CameraSource mCameraSource;

    // CAMERA VERSION ONE DECLARATIONS
    FaceDect faceDect;

    // COMMON TO BOTH CAMERAS
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private boolean wasActivityResumed = false;

    String username = "vishwam";
    ImageView camera;
    CircleOverlayView overlayView;
    ImageView previewImages;
    AppCompatTextView tvError;
    public static boolean takePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        context = getApplicationContext();
        takePicture = false;
        tvError = findViewById(R.id.tvError);
        overlayView = findViewById(R.id.overlay);
        camera = findViewById(R.id.camera);

        previewImages = findViewById(R.id.preview);
        RelativeLayout relativeLayout = findViewById(R.id.camRLayout);

        mPreview = findViewById(R.id.previewAuth);
        mGraphicOverlay = findViewById(R.id.faceOverlayAuth);
        createCameraSourceFront();
        startCameraSource();

        findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.this.finish();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture = true;
            }
        });

    }

    @Override
    public void onMultipleFacesDetected(int n) {
    }

    @Override
    public void onCapture(byte[] data, int angle) {
        stopCameraSource();
        Bitmap OriginalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotatedbitmap = Bitmap.createBitmap(OriginalBitmap, 0, 0, OriginalBitmap.getWidth(), OriginalBitmap.getHeight(), matrix, true);
        saveFile(rotatedbitmap);
    }


    public void saveFile(final Bitmap bitmap) {

        File file = getOutputMediaFile();
        String path = file.getPath();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();

                    startActivity(new Intent(CameraActivity.this, MainActivity.class));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createCameraSourceFront() {
        faceDect = new FaceDect(this, mGraphicOverlay);
        faceDect.initialisefaceDetec();
        mCameraSource = new CameraSource.Builder(context, previewFaceDetector )
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        startCameraSource();
    }

    private void startCameraSource() {

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                // Log.e(TAG, "Unable to start caera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void stopCameraSource() {
        mPreview.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasActivityResumed) {
            createCameraSourceFront();
        }
        startCameraSource();
    }

    @Override
    protected void onPause() {
        wasActivityResumed = true;
        stopCameraSource();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopCameraSource();
        super.onDestroy();
    }

    public File getOutputMediaFile() {

        final String TAG = "CameraPreview";

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Camera");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        long time = System.currentTimeMillis();
        File file = new File(mediaStorageDir.getPath() + File.separator +  time + ".jpg");

        return file;
    }

    private String getMessage(Facing face){
        switch (face){
            case STRAIGHT:
                return "Face is straight";
            case TURNED_LEFT:
                return "Face turned to left";
            case TURNED_RIGHT:
                return  "Face turned to right";
            case TILTED_LEFT:
                return "Face tilted to left";
            case TILTED_RIGHT:
                return "Face tilted to right";
        }
        return null;
    }

    private Facing getFacing(Face face){
        if(face.getEulerZ() > 5){//tilted right
            return Facing.TILTED_RIGHT;
        }else if(face.getEulerZ() < -5){ //tilted left
            return Facing.TILTED_LEFT;
        }else if(face.getEulerY() > 5){
            return Facing.TURNED_LEFT;
        }else if(face.getEulerY() < -5){
            return  Facing.TURNED_RIGHT;
        }else{
            return Facing.STRAIGHT;
        }
    }
    @Override
    public void onFaceUpdate(Detector.Detections<Face> detectionResults,final Face face) {
       final Facing faceSide = getFacing(face);
        final String message = getMessage(faceSide);
        if(camera != null){
            camera.post(new Runnable() {
                @Override
                public void run() {
                    tvError.setVisibility(View.VISIBLE);
                   if(camera != null){
                        if(faceSide == Facing.STRAIGHT){
                            camera.setEnabled(true);
                            camera.setAlpha(1f);
                            tvError.setVisibility(View.GONE);
                        }else{
                            camera.setEnabled(false);
                            camera.setAlpha(0.7f);
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText(message);
                        }
                    }
                }
            });
          }
        if(overlayView  != null){
            overlayView.post(new Runnable() {
                @Override
                public void run() {
                    overlayView.setEnabled(faceSide == Facing.STRAIGHT);
                    overlayView.updateLayout();
                }
            });
        }
    }

}
