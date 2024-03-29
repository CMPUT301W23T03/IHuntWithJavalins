package com.example.ihuntwithjavalins.Camera;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ihuntwithjavalins.QRCode.QRCode;
import com.example.ihuntwithjavalins.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;


// https://medium.com/analytics-vidhya/creating-a-barcode-scanner-using-android-studio-71cff11800a2
// https://alitalhacoban.medium.com/barcode-scanner-app-android-studio-60f87b5a10cd

/**
 * Activity that uses the device's camera to scan barcodes and QR codes.
 * Design Patterns
 * builder pattern - new BarcodeDetector.builder(this)
 * observer pattern - Surfaceholder.callback() with surefaceView
 */
public class CameraScanActivity extends AppCompatActivity {

    private SurfaceView surfaceView; // box of live camera overlay
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private TextView barcodeText;
    private String barcodeData;
    public static int cameraFlag;
    private String TAG = "Sample"; // used as string tag for debug-log messaging

    /**
     * Sets up the UI and initializes the barcode detector and camera source.
     *
     * @param savedInstanceState the saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_barcode);
        surfaceView = findViewById(R.id.surface_view);
        barcodeText = findViewById(R.id.barcode_text);

        cameraFlag = 0;

        initialiseDetectorsAndSources();
    }

    /**
     * Initializes the barcode detector and camera source.
     */
    private void initialiseDetectorsAndSources() {
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(CameraScanActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(CameraScanActivity.this, new
                                String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                //do nothing
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    barcodeText.post(new Runnable() {
                        @Override
                        public void run() {
                            barcodeData = "";
                            barcodeData = barcodes.valueAt(0).displayValue;
                            barcodeText.setText(barcodeData);
                            Log.d(TAG, "barcodeText = " + barcodeText.getText().toString());
                            if (!Arrays.asList(" ", "").contains(barcodeData)) {
                                QRCode thisCode = new QRCode(barcodeText.getText().toString());
                                Intent intent = new Intent(CameraScanActivity.this, CameraCaughtNewActivity.class);
                                intent.putExtra("cameraSavedCodeObject", (Serializable) thisCode);
                                startActivity(intent);
                            }
                        }
                    });

                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        getSupportActionBar().hide();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().hide();
        initialiseDetectorsAndSources();
    }

}