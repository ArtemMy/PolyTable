package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.ExecutionOptions;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by artem on 3/7/16.
 */
public class CreateFilesRestApi extends AsyncTask<Void, Void, List<String>> {

    private static final String TAG = "polytable_log";
    static final String PREF_FILE_ID = "poly_table_file_id";

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = DriveScopes.all().toArray(new String[DriveScopes.all().size()]);
    static GoogleAccountCredential mCredential;
    static MainNavigationDrawer act;
    private com.google.api.services.drive.Drive mService = null;
    private Exception mLastError = null;
    Object obj;

    public static void createFiles(Activity context, Object obj) {
        act = (MainNavigationDrawer)context;
        if (!((MainNavigationDrawer)context).isOnline()) ((MainNavigationDrawer)context).askForInternet();

        SharedPreferences settings = act.getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                act.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new CreateFilesRestApi(obj).execute();
            } else {
                Snackbar snackbar = Snackbar
                        .make(act.findViewById(R.id.main_coord_layout), "No network connection available.", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }

    }

    /**
     * Background task to call Drive API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected List<String> doInBackground(Void... params) {
        try {
//                return getDataFromApi();

            JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
                @Override
                public void onFailure(GoogleJsonError e,
                                      HttpHeaders responseHeaders)
                        throws IOException {
                    // Handle error
                    Log.d(TAG, e.getMessage());
                }

                @Override
                public void onSuccess(Permission permission,
                                      HttpHeaders responseHeaders)
                        throws IOException {
                    Log.d(TAG, "Permission ID: " + permission.getRole());
                }
            };

            BatchRequest batch = mService.batch();
            Permission userPermission = new Permission()
                    .setType("user")
                    .setRole("writer")
                    .setEmailAddress("");

            SharedPreferences pref = act.getSharedPreferences(
                    "edu.amd.spbstu.polystudenttimetable", Context.MODE_PRIVATE);
            String FileId = pref.getString(PREF_FILE_ID, "");

            DriveId driveId = DriveId.decodeFromString(FileId);
            Log.d(TAG, "shared" + driveId.getResourceId());
            mService.permissions().insert(driveId.getResourceId(), userPermission)
                    .setFields("id")
                    .queue(batch, callback);
            batch.execute();

            return null;
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
    }
    @Override
    protected void onPostExecute(List<String> output) {
        if (output == null || output.size() == 0) {
            Snackbar snackbar = Snackbar
                    .make(act.findViewById(R.id.main_coord_layout), act.getResources().getString(R.string.share_success), Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            Snackbar snackbar = Snackbar
                    .make(act.findViewById(R.id.main_coord_layout), "Data retrieved using the Drive API:", Snackbar.LENGTH_LONG);
            snackbar.show();
            Log.d(TAG, "Data retrieved using the Drive API:");
            Log.d(TAG, TextUtils.join("\n", output));
        }
    }

    @Override
    protected void onCancelled() {
        if (mLastError != null) {
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) mLastError)
                                .getConnectionStatusCode());
            } else {
                if (mLastError instanceof UserRecoverableAuthIOException) {
                    act.startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {

                    Snackbar snackbar = Snackbar
                            .make(act.findViewById(R.id.main_coord_layout), "The following error occurred:\n"
                                    + mLastError.getMessage(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    Log.d(TAG, "The following error occurred:\n"
                            + mLastError.getMessage());
                }
            }
        } else {
            Log.d(TAG, "Request cancelled");
        }
    }

    public CreateFilesRestApi(Object obj) {
        this.obj = obj;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Drive API Android Quickstart")
                .build();
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    static private void chooseAccount() {
        act.startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    static private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    static private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(act);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    static void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                act,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
}