package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataChangeSet;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by artem on 3/7/16.
 */
public class WriteFile extends AsyncTask<Void, Void, Boolean> {

    private GoogleApiClient mClient;
    private Lesson lesson = null;
    SharedPreferences mPref;
    private static final String TAG = "polytable_log";
    static final String PREF_FILE_ID = "POLYTABLE_FILE_ID";
    private Activity ctx;
    private ProgressDialog pb;

    public WriteFile(Activity context, Lesson lesson) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE);
        mClient = builder.build();
        this.lesson = lesson;
        ctx = context;
    }

    @Override
    protected final Boolean doInBackground(Void... v) {
        Log.d("TAG", "in background");
        final CountDownLatch latch = new CountDownLatch(1);
        mClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnectionSuspended(int cause) {
            }

            @Override
            public void onConnected(Bundle arg0) {
                latch.countDown();
            }
        });
        mClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult arg0) {
                latch.countDown();
            }
        });
        mClient.connect();
        try {
            latch.await();
        } catch (InterruptedException e) {
            return null;
        }
        if (!mClient.isConnected()) {
            return null;
        }
        try {
            return doInBackgroundConnected();
        } finally {
            mClient.disconnect();
        }
    }

    protected Boolean doInBackgroundConnected(Void... v) {
        if(lesson != null) {
            DriveId driveId = DriveId.decodeFromString(lesson.driveFileId);
            DriveFile file = driveId.asDriveFile();
            try {
                DriveApi.DriveContentsResult driveContentsResult = file.open(
                        getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    return false;
                }
                DriveContents driveContents = driveContentsResult.getDriveContents();
                OutputStream outputStream = driveContents.getOutputStream();
                //                outputStream.write(writeXML(group).getBytes());
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                oos.writeObject(lesson);
                oos.flush();
                oos.close();
                com.google.android.gms.common.api.Status status =
                        driveContents.commit(getGoogleApiClient(), null).await();
                if(!status.getStatus().isSuccess()) return false;
            } catch (IOException e) {
                Log.e(TAG, "IOException while appending to the output stream", e);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        pb.dismiss();
        if (!result) {
            // The creation failed somehow, so show a message.
            Log.d(TAG, "write failed");

            return;
        }
        else if(lesson != null) {
            ((MainNavigationDrawer) ctx).setIsLoggedIn(true);
            mPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        }
        Log.d(TAG, "write success");

        // The creation succeeded, show a message.
    }
    @Override
    protected void onPreExecute()
    {
        pb = new ProgressDialog(ctx);
        pb.setMessage(ctx.getResources().getString(R.string.placeholder_downloading));
        pb.show();
    }

    /**
     * Gets the GoogleApliClient owned by this async task.
     */
    protected GoogleApiClient getGoogleApiClient() {
        return mClient;
    }

}