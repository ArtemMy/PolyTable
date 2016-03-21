package edu.amd.spbstu.polystudenttimetable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 *
 * Writes/reads an object to/from a private local file
 *
 *
 */
public class LocalPersistence {

    private static final String TAG = "polytable_log";

    /**
     *
     * @param context
     * @param object
     * @param filename
     */
    public static void writeObjectToFile(Context context, Object object, String filename) {

        Log.d(TAG, "writeObjectToFile");
        ObjectOutputStream objectOut = null;
        try {

            FileOutputStream fileOut = context.openFileOutput(filename, Activity.MODE_PRIVATE);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(object);
            fileOut.getFD().sync();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
            }
        }
        Log.d(TAG,"wrote.");
    }


    /**
     *
     * @param context
     * @param filename
     * @return
     */
    public static Object readObjectFromFile(Context context, String filename) {

        Log.d(TAG, "readObjectFrpmFile");
        ObjectInputStream objectIn = null;
        Object object = null;
        try {

            FileInputStream fileIn = context.getApplicationContext().openFileInput(filename);
            objectIn = new ObjectInputStream(fileIn);
            object = objectIn.readObject();

        } catch (FileNotFoundException e) {
            // Do nothing
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        } finally {
            if (objectIn != null) {
                try {
                    objectIn.close();
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                    // do nowt
                }
            }
        }
        Log.d(TAG,"read.");

        return object;
    }

}