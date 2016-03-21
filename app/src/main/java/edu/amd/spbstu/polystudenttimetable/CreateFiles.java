package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * Created by artem on 3/7/16.
 */
public class CreateFiles extends AsyncTask<Void, Void, Boolean> {

    private GoogleApiClient mClient;
    private Object obj = null;
    SharedPreferences mPref;
    private static final String TAG = "polytable_log";
    static final String PREF_FILE_ID = "poly_table_file_id";
    static final String PREF_GROUP= "poly_table_is_group";
    private Activity ctx;
    private ProgressDialog pb;

    public CreateFiles(Activity context, Object obj) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE);
        mClient = builder.build();
        this.obj = obj;
        ctx = context;
        mPref = ctx.getSharedPreferences(
                "edu.amd.spbstu.polystudenttimetable", Context.MODE_PRIVATE);
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

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("PolyStudent_TimeTable_Data").build();
        DriveFolder.DriveFolderResult folderResult = Drive.DriveApi.getRootFolder(getGoogleApiClient()).createFolder(
                getGoogleApiClient(), changeSet).await();
        ArrayList<Lesson> _listLessons;
        ArrayList<String> _listLessonsId;
        if (obj.getClass().equals(Group.class)) {
            _listLessons = ((Group)obj).m_listLessons;
            _listLessonsId = ((Group)obj).m_info.m_listLessonsId;
        }
        else {
            _listLessons = ((Lecturer)obj).m_listLessons;
            _listLessonsId = ((Lecturer)obj).m_info.m_listLessonsId;
        }
        try {
            for (int i = 0; i < _listLessons.size(); ++i) {
                Log.d(TAG, "started creating №" + String.valueOf(i));
                DriveApi.DriveContentsResult driveContentsResult =
                        Drive.DriveApi.newDriveContents(mClient).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.d(TAG, "!driveContentsResult.getStatus().isSuccess()");
                    return false;
                }
                DriveContents originalContents = driveContentsResult.getDriveContents();
                OutputStream os = originalContents.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(_listLessons.get(i));
                oos.flush();
                oos.close();

                MetadataChangeSet originalMetadata = new MetadataChangeSet.Builder()
                        .setTitle(String.valueOf(_listLessons.get(i).hashCode()) + ".data").build();

                DriveFolder.DriveFileResult fileResult = folderResult.getDriveFolder().createFile(
                        getGoogleApiClient(), originalMetadata, originalContents).await();
                if (!fileResult.getStatus().isSuccess()) {
                    Log.d(TAG, "!fileResult.getStatus().isSuccess()");
                    return false;
                }

                // Finally, fetch the metadata for the newly created file, again
                // calling await to block until the request finishes.
                DriveResource.MetadataResult metadataResult = fileResult.getDriveFile()
                        .getMetadata(getGoogleApiClient())
                        .await();
                if (!metadataResult.getStatus().isSuccess()) {
                    Log.d(TAG, "!metadataResult.getStatus().isSuccess()");
                    return false;
                }
                _listLessons.get(i).driveFileId = metadataResult.getMetadata().getDriveId().encodeToString();
                _listLessonsId.add(metadataResult.getMetadata().getDriveId().encodeToString());
                Log.d(TAG, "created №" + String.valueOf(i));
            }

            Log.d(TAG, "creating general");
            // now the general file
            DriveApi.DriveContentsResult driveContentsResult =
                    Drive.DriveApi.newDriveContents(mClient).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return false;
            }
            DriveContents originalContents = driveContentsResult.getDriveContents();
            OutputStream os = originalContents.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            DriveResource.MetadataResult metadataResult;
            if(obj.getClass().equals(Group.class))
                oos.writeObject(((Group) obj).m_info);
            else
                oos.writeObject(((Lecturer)obj).m_info);
            oos.flush();
            oos.close();
            int id;
            if(obj.getClass().equals(Group.class))
                id = ((Group)obj).m_info.m_id;
            else
                id = ((Lecturer)obj).m_info.m_id;
            MetadataChangeSet originalMetadata = new MetadataChangeSet.Builder()
                    .setTitle(String.valueOf(id) + ".data").build();
            DriveFolder.DriveFileResult fileResult = folderResult.getDriveFolder().createFile(
                    getGoogleApiClient(), originalMetadata, originalContents).await();
            if (!fileResult.getStatus().isSuccess())
                return false;
            metadataResult = fileResult.getDriveFile()
                    .getMetadata(getGoogleApiClient())
                    .await();
            if (!metadataResult.getStatus().isSuccess()) {
                // We failed, stop the task and return.
                return false;
            }

            metadataResult.getMetadata().getDriveId().getResourceId();
            Log.d(TAG, "created general");
            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(PREF_FILE_ID, metadataResult.getMetadata().getDriveId().encodeToString());
            editor.apply();
            Log.d(TAG, "saved");

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if(isCancelled())
            return;
        pb.dismiss();
        if (!result) {
            Log.d(TAG, "failed");
            // The creation failed somehow, so show a message.
            return;
        }
        else if(obj != null) {
            ((MainNavigationDrawer) ctx).setIsLoggedIn(true);
            ((MainNavigationDrawer) ctx).the_obj = obj;
        }
        ((MainNavigationDrawer) ctx).switchContent(new MyTimeTableFragment());

        // The creation succeeded, show a message.
    }
    @Override
    protected void onPreExecute()
    {
        pb = new ProgressDialog(ctx);
        pb.setMessage(ctx.getResources().getString(R.string.placeholder_downloading));

        pb.setCancelable(true);
        pb.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // actually could set running = false; right here, but I'll
                // stick to contract.
                cancel(true);
            }
        });
        pb.show();
    }

    /**
     * Gets the GoogleApliClient owned by this async task.
     */
    protected GoogleApiClient getGoogleApiClient() {
        return mClient;
    }

}