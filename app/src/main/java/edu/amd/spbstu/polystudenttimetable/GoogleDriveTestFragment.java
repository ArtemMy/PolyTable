/*
package edu.amd.spbstu.polystudenttimetable;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.plus.Plus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GoogleDriveTestFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GoogleDriveTestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
/*
public class GoogleDriveTestFragment extends Fragment
implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "drive-quickstart";

    public static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    public static final int REQUEST_CODE_CREATOR = 2;
    public static final int REQUEST_CODE_RESOLUTION = 3;
    public static final int REQUEST_CODE_OPENER = 4;

    View mView;
    private Bitmap mBitmapToSave;
    private boolean isLoggedIn = false;

    public static GoogleDriveTestFragment newInstance(String param1, int param2) {
        GoogleDriveTestFragment fragment = new GoogleDriveTestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public GoogleDriveTestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        Log.d(TAG, "login fragment onCreate");
    }
    @Override
    public void onClick(View v) {
        Log.d(TAG, "click");
        switch(v.getId()) {
            case R.id.login_button:
                Log.d(TAG, "login");
                if (((MainNavigationDrawer) getActivity()).mGoogleApiClient != null) {
                    ((MainNavigationDrawer) getActivity()).mGoogleApiClient.connect();
                }
                else {
                    ((MainNavigationDrawer) getActivity()).mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                            .addApi(Drive.API)
                            .addApi(Plus.API)
                            .addScope(Drive.SCOPE_FILE)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
                    ((MainNavigationDrawer) getActivity()).mGoogleApiClient.connect();
                }
            break;
            case R.id.logout_button:
                Log.d(TAG, "logout");
                if (((MainNavigationDrawer) getActivity()).mGoogleApiClient != null) {
                    Log.d(TAG, "clearDefaultAccountAndReconnect");
                    ((MainNavigationDrawer) getActivity()).mGoogleApiClient.clearDefaultAccountAndReconnect().setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    ((MainNavigationDrawer) getActivity()).mGoogleApiClient.disconnect();
                                    logOut();
                                }
                            }
                    );
                }
                break;
            case R.id.photo_button:
                if (mBitmapToSave == null) {
                    // This activity has no UI of its own. Just start the camera.
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                            REQUEST_CODE_CAPTURE_IMAGE);
                    return;
                }
                break;
            case R.id.file_button:
                IntentSender intentSender = Drive.DriveApi
                        .newOpenFileActivityBuilder()
                        .setMimeType(new String[] { "text/plain", "text/html" })
                        .build(((MainNavigationDrawer) getActivity()).mGoogleApiClient);
                try {
                    getActivity().startIntentSenderForResult(
                            intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    Log.w(TAG, "Unable to send intent", e);
                }
                break;
            case R.id.create_button:
                Log.i(TAG, "Creating new contents.");
                final ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
                        ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override
                            public void onResult(DriveFolder.DriveFileResult result) {
                                if (!result.getStatus().isSuccess()) {
                                    Log.d(TAG, "Error while trying to create the file");
                                    return;
                                }
                                Log.d(TAG, "Created a file with content: " + result.getDriveFile().getDriveId());
                            }
                        };
                Drive.DriveApi.newDriveContents(((MainNavigationDrawer) getActivity()).mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            // Handle error
                            return;
                        }

                        final DriveContents driveContents = result.getDriveContents();

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("New folder").build();
                        Drive.DriveApi.getRootFolder(((MainNavigationDrawer) getActivity()).mGoogleApiClient).createFolder(
                                ((MainNavigationDrawer) getActivity()).mGoogleApiClient, changeSet).setResultCallback(null);
                        new Thread() {
                            @Override
                            public void run() {
                                // write content to DriveContents
                                OutputStream outputStream = driveContents.getOutputStream();
                                Writer writer = new OutputStreamWriter(outputStream);
                                try {
                                    writer.write("Hello World!");
                                    writer.close();
                                } catch (IOException e) {
                                    Log.e(TAG, e.getMessage());
                                }

                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                        .setTitle("New file")
                                        .setMimeType("text/plain")
                                        .setStarred(true).build();

                                // create a file on root folder
                                Drive.DriveApi.getRootFolder(((MainNavigationDrawer) getActivity()).mGoogleApiClient)
                                        .createFile(((MainNavigationDrawer) getActivity()).mGoogleApiClient, changeSet, driveContents)
                                        .setResultCallback(fileCallback);
                            }
                        }.start();
                    }
                });
                break;
        }
    }
    private void logOut()
    {
        TextView txt = (TextView) mView.findViewById(R.id.login_text);
        txt.setText("please log in");
        isLoggedIn = false;
        Button btn = (Button)mView.findViewById(R.id.login_button);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.logout_button);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.photo_button);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.file_button);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.create_button);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.delete_button);
        btn.setEnabled(false);
    }
    private void logIn()
    {
        TextView txt = (TextView) mView.findViewById(R.id.login_text);
        txt.setText("logged in");
        if(((MainNavigationDrawer) getActivity()).mGoogleApiClient != null){
            txt.setText("logged in as " + Plus.AccountApi.getAccountName(((MainNavigationDrawer) getActivity()).mGoogleApiClient));
        }
        isLoggedIn = true;
        Button btn = (Button)mView.findViewById(R.id.login_button);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.logout_button);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.photo_button);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.file_button);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.create_button);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.delete_button);
        btn.setEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the help for this fragment
//        if(view == null)
        Log.d(TAG, "login fragment onCreateView");
        if(mView == null)
            mView = inflater.inflate(R.help.fragment_google_drive_test, container, false);
        Button btn = (Button)mView.findViewById(R.id.login_button);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.logout_button);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.photo_button);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.file_button);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.create_button);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.delete_button);
        btn.setOnClickListener(this);

        if (isLoggedIn) {
            logIn();
        } else {
            logOut();
        }
        return mView;
    }

    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        final Bitmap image = mBitmapToSave;
        mBitmapToSave = null;
        Drive.DriveApi.newDriveContents(((MainNavigationDrawer) getActivity()).mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        // If the operation was not successful, we cannot do anything
                        // and must
                        // fail.
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New contents created.");
                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents().getOutputStream();
                        // Write the bitmap data from it.
                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                        try {
                            outputStream.write(bitmapStream.toByteArray());
                        } catch (IOException e1) {
                            Log.i(TAG, "Unable to write file contents.");
                        }
                        // Create the initial metadata - MIME type and title.
                        // Note that the user will be able to change the title later.
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("image/jpeg").setTitle("Android Photo.png").build();
                        // Create an intent for the file chooser, and start it.
                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(result.getDriveContents())
                                .build(((MainNavigationDrawer) getActivity()).mGoogleApiClient);
                        try {
                            getActivity().startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "Failed to launch file chooser.");
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult((MainNavigationDrawer) getActivity(), REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
        if (((MainNavigationDrawer) getActivity()).mGoogleApiClient != null) {
            Log.d(TAG, Plus.AccountApi.getAccountName(((MainNavigationDrawer) getActivity()).mGoogleApiClient));
            logIn();
            if (mBitmapToSave != null) {
                saveFileToDrive();
            }
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            Log.i("google_drive_on_act_res", "hereherehere in fragment");
        switch (requestCode) {
            case REQUEST_CODE_CAPTURE_IMAGE:
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    // Store the image data as a bitmap for writing later.
                    Log.i(TAG, "REQUEST_CODE_CAPTURE_IMAGE");
                    mBitmapToSave = (Bitmap) data.getExtras().get("data");
                    saveFileToDrive();
                }
                break;
            case REQUEST_CODE_CREATOR:
                // Called after a file is saved to Drive.
                Log.i(TAG, "REQUEST_CODE_CREATOR");
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                    mBitmapToSave = null;
                }
                break;
            case REQUEST_CODE_RESOLUTION:
                if(resultCode == getActivity().RESULT_OK) {
                    Log.i(TAG, "REQUEST_CODE_CREATOR");
                    if (((MainNavigationDrawer) getActivity()).mGoogleApiClient != null) {
                        if (((MainNavigationDrawer) getActivity()).mGoogleApiClient.isConnected()) {
                            Log.d(TAG, Plus.AccountApi.getAccountName(((MainNavigationDrawer) getActivity()).mGoogleApiClient));
                            logIn();
                            if (mBitmapToSave != null) {
                                saveFileToDrive();
                            }
                        }
                        else
                            ((MainNavigationDrawer) getActivity()).mGoogleApiClient.connect();
                    }

                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}

*/