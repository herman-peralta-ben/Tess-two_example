package com.ashomok.tesseractsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by herman on 01/11/16.
 */

public class OCRManager {
    private static final String TAG = OCRManager.class.getSimpleName();

    public static final String PATH_WORKING_DIR = Environment.getExternalStorageDirectory().toString() + "/HermanPruebas/";
    private static final String PATH_ASSETS_TESSDATA = "tessdata";
    private static final String lang = "eng";

    private Context context;
    private TessBaseAPI tessBaseApi;

    private String result = "empty";

    public OCRManager(Context context) {
        this.context = context;
    }

    public void doFileOCR(Uri imgUri, OCRListener ocrListener) {
        prepareTesseract(context);

        (new OCRAsyncTask(imgUri, ocrListener)).execute();
    }

    public void doBitmapOCR(Bitmap bitmap, OCRListener ocrListener) {
        prepareTesseract(context);

        (new OCRAsyncTask(bitmap, ocrListener)).execute();
    }

    private void prepareTesseract(Context context) {
        Utils.prepareDirectory(PATH_WORKING_DIR + PATH_ASSETS_TESSDATA);

        Utils.copyAssetsFolder(context, PATH_ASSETS_TESSDATA, PATH_WORKING_DIR);
    }

    /**
     * don't run this code in main thread - it stops UI thread. Create AsyncTask instead.
     * http://developer.android.com/intl/ru/reference/android/os/AsyncTask.html
     *
     * @param imgUri
     */
    private boolean startFileOCR(Uri imgUri) {
        boolean result;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            Bitmap bitmap = BitmapFactory.decodeFile(imgUri.getPath(), options);

            result = startBitmapOCR(bitmap);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();

            result = false;
        }

        return result;
    }

    /**
     * don't run this code in main thread - it stops UI thread. Create AsyncTask instead.
     * http://developer.android.com/intl/ru/reference/android/os/AsyncTask.html
     *
     * @param bitmap
     * @return
     */
    private boolean startBitmapOCR(Bitmap bitmap) {
        try {
            result = extractText(bitmap);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();

            return false;
        }

        return true;
    }

    private String extractText(Bitmap bitmap) {
        try {
            tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            if (tessBaseApi == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }

        tessBaseApi.init(PATH_WORKING_DIR, lang);

//       //EXTRA SETTINGS
//        //For example if we only want to detect numbers
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
//
//        //blackList Example
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
//                "YTRWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");

        Log.d(TAG, "Training file loaded");
        tessBaseApi.setImage(bitmap);
        String extractedText = "empty result";
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error in recognizing text.");
        }
        tessBaseApi.end();

        return extractedText;
    }

    private class OCRAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private Uri imgUri;
        private Bitmap bitmap;
        private OCRListener ocrListener;
        boolean doBitmap = false;

        public OCRAsyncTask(Uri imgUri, OCRListener ocrListener) {
            this.imgUri = imgUri;
            this.ocrListener = ocrListener;
        }

        public OCRAsyncTask(Bitmap bitmap, OCRListener ocrListener) {
            this.bitmap = bitmap;
            this.ocrListener = ocrListener;

            doBitmap = true;
        }

        @Override
        protected void onPreExecute() {
            ocrListener.onOCRStart();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean res;

            if(doBitmap) {
                res = startBitmapOCR(bitmap);
            } else {
                res = startFileOCR(imgUri);
            }

            return res;
        }

        @Override
        protected void onPostExecute(Boolean done) {
            ocrListener.onOCREnd(done, result);
        }
    }

    public interface OCRListener {
        public void onOCRStart();
        public void onOCREnd(boolean done, String text);
    }
}
