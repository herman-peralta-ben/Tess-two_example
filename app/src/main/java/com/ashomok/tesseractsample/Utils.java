package com.ashomok.tesseractsample;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by herman on 01/11/16.
 */

public class Utils {

    /**
     * Prepare directory on external storage
     *
     * @param path
     * @throws Exception
     */
    public static boolean prepareDirectory(String path) {
        boolean done = true;
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e("Utils", "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
                    done = false;
                }
            } else {
                Log.i("Utils", "Created directory " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            done = false;
        }

        return done;
    }

    /**
     * Copy tessdata files (located on assets/tessdata) to destination directory
     *
     * @param assetsFolder - name of directory with .traineddata files
     */
    public static void copyAssetsFolder(Context context, String assetsFolder, String destPath) {
        try {
            String fileList[] = context.getAssets().list(assetsFolder);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = destPath + assetsFolder + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = context.getAssets().open(assetsFolder + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d("Utils", "Copied " + fileName + "to " + pathToDataFile);
                }
            }
        } catch (IOException e) {
            Log.e("Utils", "Unable to copy files to tessdata " + e.toString());
            e.printStackTrace();
        }
    }
}
