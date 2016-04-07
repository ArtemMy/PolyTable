package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Xml;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by artem on 3/7/16.
 */
public class UpdateFiles extends AsyncTask<Void, Void, Boolean> {

    private GoogleApiClient mClient;
    private Object obj = null;
    SharedPreferences mPref;
    private static final String TAG = "polytable_log";
    static final String PREF_FILE_ID = "poly_table_file_id";
    private Activity ctx;
    private String filename;

    public UpdateFiles(Activity context, Object obj) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER);


        mClient = builder.build();
        this.obj = obj;
        ctx = context;
        filename = null;
        mPref = ctx.getSharedPreferences(
                "edu.amd.spbstu.polystudenttimetable", Context.MODE_PRIVATE);
    }

    public UpdateFiles(Activity context, Object obj, String fileCode) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE);

        mClient = builder.build();
        this.obj = obj;
        ctx = context;
        filename = null;
        mPref = ctx.getSharedPreferences(
                "edu.amd.spbstu.polystudenttimetable", Context.MODE_PRIVATE);
        this.filename = fileCode;
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
        Log.d(TAG, "aloha");

        try {
            DriveId driveId;
//            DriveApi.DriveIdResult resultId = Drive.DriveApi.fetchDriveId(getGoogleApiClient(), FileId).await();
            if(filename == null) {
                String FileId = mPref.getString(PREF_FILE_ID, "");
                if (FileId.equalsIgnoreCase("")) {
                    Log.d(TAG, "FileId == null");
                    return false;
                }
                driveId = DriveId.decodeFromString(FileId);
                if(driveId.getResourceId() != null)
                    driveId = Drive.DriveApi.fetchDriveId(mClient, driveId.getResourceId()).await().getDriveId();
            } else {
                /*
                Log.d(TAG, filename);
                Query query = new Query.Builder()
                        .addFilter(Filters.and(
                                        Filters.eq(SearchableField.TITLE, filename),
                                        Filters.sharedWithMe())
                        ).build();
                Query query = new Query.Builder()
                        .addFilter(Filters.sharedWithMe()
                        ).build();
                DriveApi.MetadataBufferResult result = Drive.DriveApi.query(mClient, query).await();
                if (!result.getStatus().isSuccess()) {
                    return false;
                }

                Log.d(TAG, String.valueOf(result.getMetadataBuffer().getCount()));
                if (result.getMetadataBuffer().getCount() == 0) {
                    Snackbar snackbar = Snackbar
                            .make(ctx.findViewById(R.id.main_coord_layout), ctx.getResources().getString(R.string.wrong_data), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return false;
                }
                driveId = result.getMetadataBuffer().get(0).getDriveId();
                */
                driveId = Drive.DriveApi.fetchDriveId(mClient, filename).await().getDriveId();
                SharedPreferences.Editor editor = ctx.getSharedPreferences(
                        "edu.amd.spbstu.polystudenttimetable", Context.MODE_PRIVATE).edit();
                editor.putString(PREF_FILE_ID, driveId.encodeToString());
                editor.apply();
                Log.d(TAG, "saved");
            }
            DriveFile genFile = driveId.asDriveFile();

            DriveApi.DriveContentsResult genDriveContentsResult = genFile.open(
                    getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            if (!genDriveContentsResult.getStatus().isSuccess()) {
                Log.d(TAG, "!genDriveContentsResult.getStatus().isSuccess()");
                return false;
            }
            if(driveId.getResourceId() != null)
                Log.d(TAG, "gen resource id" + driveId.getResourceId());
            else
                Log.d(TAG, "gen resource id null");

            DriveContents genDriveContents = genDriveContentsResult.getDriveContents();
            InputStream genInputStream = genDriveContents.getInputStream();
            //                outputStream.write(writeXML(group).getBytes());
            ObjectInputStream genIs = new ObjectInputStream(genInputStream);
            ArrayList<Lesson> _listLessons;
            ArrayList<String> _listLessonsId;
            Object in = genIs.readObject();
            if(in.getClass().equals(GroupInfo.class)) {
                obj = new Group();
                ((Group)obj).m_info = (GroupInfo)in;
                _listLessons =  ((Group)obj).m_listLessons;
                _listLessonsId =  ((Group)obj).m_info.m_listLessonsId;
            } else {
                obj = new Lecturer();
                ((Lecturer)obj).m_info = (LecturerInfo)in;
                _listLessons =  ((Lecturer)obj).m_listLessons;
                _listLessonsId =  ((Lecturer)obj).m_info.m_listLessonsId;
            }
            genIs.close();
            genDriveContents.discard(getGoogleApiClient());

            for(int i = 0; i < _listLessonsId.size(); ++i) {
//            for(String lessonId : _listLessonsId) {
                DriveId fileId = DriveId.decodeFromString(_listLessonsId.get(i));
                if(fileId.getResourceId() != null)
                    fileId = Drive.DriveApi.fetchDriveId(mClient, fileId.getResourceId()).await().getDriveId();
                DriveFile file = fileId.asDriveFile();

                DriveApi.DriveContentsResult driveContentsResult = file.open(
                        getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.d(TAG, "!driveContentsResult.getStatus().isSuccess()");
                    return false;
                }
                DriveContents driveContents = driveContentsResult.getDriveContents();
                InputStream inputStream = driveContents.getInputStream();
                //                outputStream.write(writeXML(group).getBytes());
                ObjectInputStream is = new ObjectInputStream(inputStream);
                Lesson l = (Lesson) is.readObject();
                DriveId did = driveContents.getDriveId();
                Log.d(TAG, "resource id: " + did.getResourceId());
                l.driveFileId = driveContents.getDriveId().encodeToString();
                _listLessonsId.set(i, driveContents.getDriveId().encodeToString());
                _listLessons.add(l);
                is.close();
                driveContents.discard(getGoogleApiClient());
            }
        }
        catch (IOException e) {
            Log.e(TAG, "IOException while appending to the output stream", e);
            return false;
        }
        catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not found", e);
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if(result == null || !result) {
            Log.d(TAG, "fail");
            new AlertDialog.Builder(ctx)
                    .setTitle(ctx.getResources().getString(R.string.error))
                    .setMessage(ctx.getResources().getString(R.string.relogin))
                    .setPositiveButton(android.R.string.ok, null) // dismisses by default
                    .create()
                    .show();
            return;
        }
        else if(obj != null) {
            ((MainNavigationDrawer) ctx).the_obj = obj;
            ((MainNavigationDrawer) ctx).setIsLoggedIn(true);
        }
        Snackbar snackbar = Snackbar
                .make(ctx.findViewById(R.id.main_coord_layout), ctx.getResources().getString(R.string.load_success), Snackbar.LENGTH_LONG);
        snackbar.show();
        Log.d(TAG, "updated");
        // The creation succeeded, show a message.
    }
    /**
     * Gets the GoogleApliClient owned by this async task.
     */
    protected GoogleApiClient getGoogleApiClient() {
        return mClient;
    }

}