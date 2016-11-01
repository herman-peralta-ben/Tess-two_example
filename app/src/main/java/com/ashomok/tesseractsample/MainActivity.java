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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity
    implements OCRManager.OCRListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PHOTO_REQUEST_CODE = 1;

    private TextView textView;
    private Uri outputFileUri;
    private EditText edOpcion;

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
        edOpcion = (EditText) findViewById(R.id.option);

        findViewById(R.id.btnTestFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edOpcion.setText("2");
                File testImage = new File(OCRManager.PATH_WORKING_DIR + "prueba.jpg");
                outputFileUri = Uri.fromFile(testImage);

                Log.i("PRUEBA", outputFileUri.getPath());
                Toast.makeText(MainActivity.this, "Test image : " + testImage.exists() + ", " + testImage.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                prueba(null);
            }
        });

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

            prueba(photoBitmap);
        } else {
            Toast.makeText(this, "ERROR: Image was not obtained.", Toast.LENGTH_SHORT).show();
        }
    }

    private void prueba(Bitmap photoBitmap) {
        String msg = "";
        switch (edOpcion.getText().toString()) {
            case "0": //pruebas bitmap
                msg = "1 bitmap";
                ocrManager.doBitmapOCR(photoBitmap, this);
                break;

            case "1": //pruebas array bitmap
                msg = "array bitmap";

                Bitmap[] arrBitmap = new Bitmap[10];
                for(int i=0; i<arrBitmap.length ; i++) {
                    arrBitmap[i] = photoBitmap;
                }
                ocrManager.doBitmapArrayOCR(arrBitmap, this);
                break;

            case "2": //pruebas uri
                msg = "1 uri";

                ocrManager.doFileOCR(outputFileUri, this);
                break;

            case "3": //pruebas array uri
                msg = "array uri";

                Uri[] imgUris = new Uri[10];
                for(int i=0; i<imgUris.length ; i++) {
                    imgUris[i] = outputFileUri;
                }
                ocrManager.doFileArrayOCR(imgUris, this);
                break;
        }

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onOCRStart() {
        Toast.makeText(this, "Iniciando OCR", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOCREnd(boolean done, ArrayList<String> texts) {
        String text = "FIN OCR RESULT: " + done + " size " + texts.size() + "\n";

        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

        for(String t : texts) {
            text += t + "====================\n";
        }

        textView.setText(text);
    }
}


