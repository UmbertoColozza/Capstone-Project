package com.umberto.medicinetracking.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;
import com.umberto.medicinetracking.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {
    public static final String FILE_PROVIDER_AUTHORITY="com.umberto.medicinetracking.fileprovider";
    //Get file from name
    public static File getFile(Context context, String fileName){
        Uri pathUri =Uri.parse(context.getFilesDir().getAbsolutePath()).buildUpon()
                .appendPath(fileName)
                .build();
        return new File(pathUri.toString());
    }

    /**
     * Resamples the captured photo to fit the screen for better memory usage.
     *
     * @param context   The application context.
     * @param imagePath The path of the photo to be resampled.
     * @return The resampled bitmap
     */
    public static Bitmap resamplePic(Context context, String imagePath) {

        // Get device screen size information
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;

        // Get the dimensions of the original bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imagePath);
    }

    /**
     * Creates the temporary image file in the cache directory.
     *
     * @return The temporary image file.
     * @throws IOException Thrown if there is an error creating the file
     */
    public static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getFilesDir();

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    /**
     * Deletes image file for a given file name.
     *
     * @param context   The application context.
     * @param fileName The name of the file to be deleted.
     */
    public static boolean deleteImageFromFileName(Context context,String fileName){
        return deleteImageFile(context, context.getFilesDir()+"/"+fileName);
    }
    /**
     * Deletes image file for a given path.
     *
     * @param context   The application context.
     * @param imagePath The path of the photo to be deleted.
     */
    public static boolean deleteImageFile(Context context, String imagePath) {
        // Get the file
        File imageFile = new File(imagePath);
        if(imageFile==null || !imageFile.exists()){
            return false;
        }

        // Delete the image
        boolean deleted = imageFile.delete();

        // If there is an error deleting the file, show a Toast
        if (!deleted) {
            String errorMessage = context.getString(R.string.error);
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
        }

        return deleted;
    }

    //Delete all photo with medicine id "JPEG_medicineId_"
    public static void deleteAllPhotoList(Context context, int medicineId){
        File dir=new File(context.getFilesDir().getAbsolutePath());
        FilenameFilter beginswith = (directory, filename) -> filename.startsWith("JPEG_"+medicineId+"_");
        for(String file : dir.list(beginswith)){
            File imageFile=new File(context.getFilesDir(),file);
            imageFile.delete();
        }
    }
    /**
     * Helper method for saving the image.
     *
     * @param context The application context.
     * @param image   The image to be saved.
     * @return The path of the saved image.
     */
    public static String saveImage(Context context, Bitmap image, int medicineId) {
        // Create the new file in the external storage
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_"+medicineId+"_" + timeStamp + ".jpg";
        return saveImage(context, image, imageFileName);
    }
    public static String saveImage(Context context, Bitmap image,String imageFileName) {
        File storageDir = new File(context.getFilesDir().getAbsolutePath());

        // Save the new Bitmap
            File imageFile = new File(storageDir, imageFileName);
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Show a Toast with the save location
            Toast.makeText(context, context.getString(R.string.saved_message), Toast.LENGTH_SHORT).show();
            return imageFileName;
    }

    public static File[] getListImage(Context context){
        File directory = new File(context.getFilesDir().getAbsolutePath());
        return directory.listFiles();
    }
}
