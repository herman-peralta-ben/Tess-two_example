package com.ashomok.tesseractsample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity
    implements OCRManager.OCRListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PHOTO_REQUEST_CODE = 1;

    private TextView textView;
    private Uri outputFileUri;

    private OCRManager ocrManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button captureImg = (Button) findViewById(R.id.action_btn);
        if (captureImg != null) {
            captureImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startCameraActivity();
                }
            });
        }
        textView = (TextView) findViewById(R.id.textResult);

        ocrManager = new OCRManager(this);
    }

    /**
     * to get high resolution image from camera
     */
    private void startCameraActivity() {
        try {
            String IMGS_PATH = OCRManager.PATH_WORKING_DIR + "/imgs";
            Utils.prepareDirectory(IMGS_PATH);

            String img_path = IMGS_PATH + "/ocr.jpg";

            outputFileUri = Uri.fromFile(new File(img_path));

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, PHOTO_REQUEST_CODE);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //making photo
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //Todo pruebas, descomentar el putExtra
            Bundle extras = data.getExtras();
            Bitmap photoBitmap = (Bitmap) extras.get("data");

            //ocrManager.doFileOCR(outputFileUri, this);
            ocrManager.doBitmapOCR(photoBitmap, this);
        } else {
            Toast.makeText(this, "ERROR: Image was not obtained.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOCRStart() {
        Toast.makeText(this, "Iniciando OCR", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOCREnd(boolean done, String text) {
        Toast.makeText(this, "FIN OCR RESULT: " + done, Toast.LENGTH_SHORT).show();
        textView.setText(text);
    }
}


