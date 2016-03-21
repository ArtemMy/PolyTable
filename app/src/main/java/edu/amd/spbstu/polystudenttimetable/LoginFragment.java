package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;


import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.CountDownLatch;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "polytable_log";

    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_OPENER = 4;
    public GoogleApiClient mClient;
    private enum WHAT_TO_DO
    {
        CREATE_FILES,
        DOWNLOAD_FILES,
        UPLOAD_FILES,
        DELETE_FILES,
        FIND_FILES,
        DO_NOTHING
    };

    WHAT_TO_DO whatToDoOnConnected;

    View mView;
    private Bitmap mBitmapToSave;

    public static LoginFragment newInstance(String param1, int param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        Log.d(TAG, ((MainNavigationDrawer)getActivity()).toString());
        if (!((MainNavigationDrawer)getActivity()).isOnline()) ((MainNavigationDrawer)getActivity()).askForInternet();
        Log.d(TAG, "login fragment onCreate");

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(getActivity())
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE);

        mClient = builder.build();
        mClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnectionSuspended(int cause) {
            }

            @Override
            public void onConnected(Bundle arg0) {
                Log.i(TAG, "Connected!");
            }
        });
        mClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
                if (!result.hasResolution()) {
                    // show the localized error dialog.
                    GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), result.getErrorCode(), 0).show();
                    return;
                }
                try {
                    result.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLUTION);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Exception while starting resolution activity", e);
                }
            }
        });
        mClient.connect();
    }

    @Override
    public void onClick(View v) {
        if(!mClient.isConnected())
        {
            mClient.connect();
            return;
        }
        if(!((MainNavigationDrawer) getActivity()).isOnline()) {
            ((MainNavigationDrawer) getActivity()).askForInternet();
            return;
        }
        Log.d(TAG, "click");
        switch(v.getId()) {
            case R.id.login_choose_button:
                Log.d(TAG, "login");
                /*
                Query query = new Query.Builder()
                        .addFilter(Filters.and(
                                        Filters.eq(SearchableField.MIME_TYPE, "text/xml"),
                                        Filters.eq(SearchableField.TITLE, "group"),
                                        Filters.sharedWithMe())
                        ).build();
                Drive.DriveApi.query(mClient, query)
                        .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                            @Override
                            public void onResult(DriveApi.MetadataBufferResult result) {
                                if (!result.getStatus().isSuccess()) {
                                    return;
                                }
                                mResultsAdapter.clear();
                                mResultsAdapter.append(result.getMetadataBuffer());
                            }
                        });
            */
                break;

            case R.id.login_create_button:
                Log.d(TAG, "login");
                ((MainNavigationDrawer)getActivity()).switchContent(new SearchTabFragment());
                break;
            case R.id.logout_button:
                Log.d(TAG, "logout");
                if (mClient.isConnected()) {
                    mClient.clearDefaultAccountAndReconnect().setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    mClient.disconnect();
                                    loggedOut();
                                }
                            }
                    );
                    ((MainNavigationDrawer) getActivity()).setIsLoggedIn(false);
                }
                break;
        }
    }
    private void loggedOut()
    {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.connect_fail));
        Button btn = (Button)mView.findViewById(R.id.login_choose_button);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.login_create_button);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.logout_button);
        btn.setEnabled(false);
    }
    private void loggedIn()
    {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.connect_email));
        Button btn = (Button)mView.findViewById(R.id.login_choose_button);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.login_create_button);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.logout_button);
        btn.setEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        if(view == null)
        Log.d(TAG, "login fragment onCreateView");
        if(mView == null)
            mView = inflater.inflate(R.layout.fragment_login, container, false);

        Button btn = (Button)mView.findViewById(R.id.login_choose_button);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.logout_button);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.login_create_button);
        btn.setOnClickListener(this);

        if (((MainNavigationDrawer) getActivity()).isLoggedIn()) {
            loggedIn();
        } else {
            loggedOut();
        }
        return mView;
    }

    @Override
    public void onResume() {
        if (((MainNavigationDrawer) getActivity()).isLoggedIn()) {
            loggedIn();
        } else {
            loggedOut();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (((MainNavigationDrawer) getActivity()).isLoggedIn()) {
            loggedIn();
        } else {
            loggedOut();
        }
        if(mClient.isConnected() || mClient.isConnecting())
            mClient.disconnect();
        super.onPause();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            Log.i("google_drive_on_act_res", "hereherehere in fragment");
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if(resultCode == getActivity().RESULT_OK) {
                    Log.i(TAG, "REQUEST_CODE_RESOLUTION");
                    mClient.connect();
                }
                break;
            case REQUEST_CODE_OPENER:
                if (resultCode == getActivity().RESULT_OK) {
                    DriveId driveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                }
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}
