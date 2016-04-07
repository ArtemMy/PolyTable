package edu.amd.spbstu.polystudenttimetable;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import com.google.android.gms.common.api.BatchResult;
import com.google.android.gms.common.api.PendingResults;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;

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
    static final String PREF_FILE_ID = "poly_table_file_id";

    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_OPENER = 4;
    private enum WHAT_TO_DO
    {
        CREATE_FILES,
        DOWNLOAD_FILES,
        UPLOAD_FILES,
        DELETE_FILES,
        FIND_FILES,
        DO_NOTHING
    };

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQ_ACCPICK = 1;
    private static final int REQ_CONNECT = 2;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    WHAT_TO_DO whatToDoOnConnected;
    private String emailStr;
    private String fileCode;

    View mView;

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

        /*
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(getActivity())
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER);

        mClient = builder.build();
        mClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnectionSuspended(int cause) {
                Log.i(TAG, "Suspended: " + String.valueOf(cause));
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
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        */
        if (UT.AM.getEmail() == null) {
            getActivity().startActivityForResult(AccountPicker.newChooseAccountIntent(null,
                            null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null),
                    REQ_ACCPICK);
        }

    }

    @Override
    public void onClick(View v) {
        if (UT.AM.getEmail() == null) {
            getActivity().startActivityForResult(AccountPicker.newChooseAccountIntent(null,
                            null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null),
                    REQ_ACCPICK);

            Snackbar snackbar = Snackbar
                    .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.try_again), Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }
        if(!((MainNavigationDrawer) getActivity()).isOnline()) {
            ((MainNavigationDrawer) getActivity()).askForInternet();
            return;
        }
        Log.d(TAG, "click");
        switch(v.getId()) {
            case R.id.login_choose_les:
                if(((MainNavigationDrawer)getActivity()).the_obj == null) {
                    Snackbar snackbar = Snackbar
                            .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.not_loaded), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }
                final ArrayList<String> listt = new ArrayList<>();
                final List <Lesson> lis = ((MainNavigationDrawer)getActivity()).isGroup() ?
                        ((Group)((MainNavigationDrawer)getActivity()).the_obj).m_listLessons :
                        ((Lecturer)((MainNavigationDrawer)getActivity()).the_obj).m_listLessons;

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.str_depth));
                builder.setIcon(R.drawable.logo_amd_mod);

                for(int i = 0; i < lis.size(); ++i) {
                    listt.add(lis.get(i).m_subject);
                }

//                        ListView modeList = new ListView(getActivity());
                final ArrayAdapter<String> modeAdapter3 = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1, android.R.id.text1,
                        listt);
                builder.setAdapter(modeAdapter3,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                Lesson les = lis.get(which);
                                final String fileCode = String.valueOf(les.hashCode());

                                new AsyncTask<Void, String, Boolean>() {
                                    @Override
                                    protected Boolean doInBackground(Void... params) {
                                        String s = REST.query(fileCode);
                                        if (s == null)
                                            return false;
                                        final List<String> l = ((MainNavigationDrawer) getActivity()).isGroup() ?
                                                ((Group) ((MainNavigationDrawer) getActivity()).the_obj).m_info.m_listLessonsId :
                                                ((Lecturer) ((MainNavigationDrawer) getActivity()).the_obj).m_info.m_listLessonsId;
                                        l.set(which, s);
                                        ((MainNavigationDrawer) getActivity()).update();
                                        return true;
                                    }
                                }.execute();

//                                refreshResults(false);
                            }
                        });
                final Dialog dialog3 = builder.create();
                dialog3.show();
                break;
            case R.id.login_share_les:
                if(((MainNavigationDrawer)getActivity()).the_obj == null) {
                    Snackbar snackbar = Snackbar
                            .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.not_loaded), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }

                final ArrayList<String> listt1 = new ArrayList<>();
                final List <Lesson> lis1 = ((MainNavigationDrawer)getActivity()).isGroup() ?
                        ((Group)((MainNavigationDrawer)getActivity()).the_obj).m_listLessons :
                        ((Lecturer)((MainNavigationDrawer)getActivity()).the_obj).m_listLessons;

                if(((MainNavigationDrawer)getActivity()).the_obj == null) {
                    Snackbar snackbar = Snackbar
                            .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.not_loaded), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }

                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.str_depth));
                builder.setIcon(R.drawable.logo_amd_mod);

                for(int i = 0; i < lis1.size(); ++i) {
                    listt1.add(lis1.get(i).m_subject);
                }

//                        ListView modeList = new ListView(getActivity());
                final ArrayAdapter<String> modeAdapter2 = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1, android.R.id.text1,
                        listt1);
                builder.setAdapter(modeAdapter2,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Lesson les = lis1.get(which);
                                final String fileCode = les.driveFileId;
                                Log.d(TAG, fileCode);
                                new AsyncTask<Void, String, Boolean>() {
                                    @Override
                                    protected Boolean doInBackground(Void... params) {

                                        JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
                                            @Override
                                            public void onFailure(GoogleJsonError e,
                                                                  HttpHeaders responseHeaders)
                                                    throws IOException {
                                                // Handle error
                                                Snackbar snackbar = Snackbar
                                                        .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.error), Snackbar.LENGTH_LONG);
                                                snackbar.show();
                                                Log.d(TAG, e.getMessage());
                                            }

                                            @Override
                                            public void onSuccess(Permission permission,
                                                                  HttpHeaders responseHeaders)
                                                    throws IOException {
                                                Snackbar snackbar = Snackbar
                                                        .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.share_success), Snackbar.LENGTH_LONG);
                                                snackbar.show();
                                                Log.d(TAG, "Permission ID: " + permission.getRole());
                                            }
                                        };
                                        if(!REST.share(fileCode, emailStr, callback))
                                            return false;
                                        return true;
                                    }
                                }.execute();

//                                refreshResults(false);
                            }
                        });
                final Dialog dialog2 = builder.create();
                dialog2.show();
                break;
            case R.id.login_choose_gr:
                Log.d(TAG, "login choose gr");

                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.str_depth));
                builder.setIcon(R.drawable.logo_amd_mod);
//                        ListView modeList = new ListView(getActivity());
                final ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1, android.R.id.text1,
                        StaticStorage.m_primatGroupsName);
                builder.setAdapter(modeAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GroupInfo tmpGroup = StaticStorage.m_primatGroups.get(which);
                                final String fileCode = String.valueOf(tmpGroup.m_id);

                                new AsyncTask<Void, String, Boolean>() {
                                    @Override
                                    protected Boolean doInBackground(Void... params) {

                                        String res = REST.query(fileCode);
                                        if (res == null) {
                                            Snackbar snackbar = Snackbar
                                                    .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.error), Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                            return false;
                                        } else {
                                            Snackbar snackbar = Snackbar
                                                    .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.load_success), Snackbar.LENGTH_LONG);
                                            snackbar.show();

                                            Log.d(TAG, "created general");
                                            SharedPreferences.Editor editor = getActivity().getSharedPreferences(
                                                    getActivity().getPackageName(), Context.MODE_PRIVATE).edit();
                                            editor.putString(PREF_FILE_ID, res);
                                            editor.commit();
                                            Log.d(TAG, "saved");
                                            ((MainNavigationDrawer) getActivity()).setIsLoggedIn(true);

                                            ((MainNavigationDrawer)getActivity()).update();

                                            return true;
                                        }
                                    }
                                }.execute();

//                                refreshResults(false);
                            }
                        });
                final Dialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.login_share_gr:

                Log.d(TAG, "login share gr");
                if(((MainNavigationDrawer)getActivity()).the_obj == null) {
                    Snackbar snackbar = Snackbar
                            .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.not_loaded), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }


                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                final EditText edittext = new EditText(getActivity());

                edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                alert.setTitle(R.string.email);

                alert.setView(edittext);
                alert.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        emailStr = edittext.getText().toString();
                        new AsyncTask<Void, String, Boolean>() {
                            @Override
                            protected Boolean doInBackground(Void... params) {

                                JsonBatchCallback<Permission> callback1 = new JsonBatchCallback<Permission>() {
                                    @Override
                                    public void onFailure(GoogleJsonError e,
                                                          HttpHeaders responseHeaders)
                                            throws IOException {
                                        // Handle error
                                        Snackbar snackbar = Snackbar
                                                .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.error), Snackbar.LENGTH_LONG);
                                        snackbar.show();
                                        Log.d(TAG, e.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(Permission permission,
                                                          HttpHeaders responseHeaders)
                                            throws IOException {
                                        Log.d(TAG, "Permission ID: " + permission.getRole());
                                    }
                                };

                                JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
                                    @Override
                                    public void onFailure(GoogleJsonError e,
                                                          HttpHeaders responseHeaders)
                                            throws IOException {
                                        // Handle error
                                        Snackbar snackbar = Snackbar
                                                .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.error), Snackbar.LENGTH_LONG);
                                        snackbar.show();
                                        Log.d(TAG, e.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(Permission permission,
                                                          HttpHeaders responseHeaders)
                                            throws IOException {
                                        Snackbar snackbar = Snackbar
                                                .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.share_success), Snackbar.LENGTH_LONG);
                                        snackbar.show();
                                        Log.d(TAG, "Permission ID: " + permission.getRole());
                                    }
                                };
                                if(((MainNavigationDrawer)getActivity()).the_obj == null) {
                                    Snackbar snackbar = Snackbar
                                            .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.not_loaded), Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                    return false;
                                }
                                SharedPreferences pref = getActivity().getSharedPreferences(
                                        "edu.amd.spbstu.polystudenttimetable", Context.MODE_PRIVATE);
                                final String FileId = pref.getString(PREF_FILE_ID, "");
                                if(!REST.share(FileId, emailStr, callback))
                                    return false;
                                if(((MainNavigationDrawer)getActivity()).isGroup()) {
                                    for(String FId : ((Group)((MainNavigationDrawer)getActivity()).the_obj).m_info.m_listLessonsId) {
                                        if(!REST.share(FId, emailStr, callback1))
                                            return false;
                                    }
                                } else {
                                    for(String FId : ((Lecturer)((MainNavigationDrawer)getActivity()).the_obj).m_info.m_listLessonsId) {
                                        if(!REST.share(FId, emailStr, callback1))
                                            return  false;
                                    }
                                }
                                return true;
                            }
                        }.execute();

//                        refreshResults(true);
/*                        Log.d(TAG, emailStr);
                        JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
                            @Override
                            public void onFailure(GoogleJsonError e,
                                                  HttpHeaders responseHeaders)
                                    throws IOException {
                                // Handle error
                                System.err.println(e.getMessage());
                            }

                            @Override
                            public void onSuccess(Permission permission,
                                                  HttpHeaders responseHeaders)
                                    throws IOException {
                                System.out.println("Permission ID: " + permission.getRole());
                            }
                        };

                        final com.google.api.services.drive.Drive driveService;
                        driveService = new com.google.api.services.drive.Drive.Builder(getActivity(), ).build();
                        BatchRequest batch = driveService.batch();
                        Permission userPermission = new Permission()
                                .setType("user")
                                .setRole("writer")
                                .setEmailAddress(emailStr);
                        driveService.permissions().create(fileId, userPermission)
                                .setFields("id")
                                .queue(batch, callback);
                        batch.execute();
                        */
                    }
                });

                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
                break;

            case R.id.login_create_button:
                Log.d(TAG, "login");
                ((MainNavigationDrawer)getActivity()).switchContent(new SearchTabFragment());
                break;
            case R.id.logout_button:
                Log.d(TAG, "logout");
                ((MainNavigationDrawer) getActivity()).setIsLoggedIn(false);
                REST.disconnect();
                UT.AM.setEmail(null);
                break;
        }
    }
    private void loggedOut()
    {
        Button btn = (Button)mView.findViewById(R.id.login_choose_gr);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.login_choose_les);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.login_create_button);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.logout_button);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.login_share_les);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.login_share_gr);
        btn.setEnabled(false);
    }
    private void loggedIn()
    {
        Button btn = (Button)mView.findViewById(R.id.login_choose_gr);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.login_choose_les);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.login_create_button);
        btn.setEnabled(false);
        btn = (Button)mView.findViewById(R.id.logout_button);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.login_share_les);
        btn.setEnabled(true);
        btn = (Button)mView.findViewById(R.id.login_share_gr);
        btn.setEnabled(true);
        TextView t= ((TextView)mView.findViewById(R.id.loggedas));
        if (((MainNavigationDrawer)getActivity()).the_obj != null) {
            if (((MainNavigationDrawer) getActivity()).isGroup())
                t.setText(getResources().getString(R.string.connect_email) + " " + ((Group) ((MainNavigationDrawer) getActivity()).the_obj).m_info.m_name);
            else
                t.setText(getResources().getString(R.string.connect_email) + " " + ((Lecturer) ((MainNavigationDrawer) getActivity()).the_obj).m_info.m_fio);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the help for this fragment
//        if(view == null)
        Log.d(TAG, "login fragment onCreateView");
        if(mView == null)
            mView = inflater.inflate(R.layout.fragment_login, container, false);

        Button btn = (Button)mView.findViewById(R.id.login_choose_gr);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.logout_button);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.login_create_button);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.login_choose_les);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.login_share_gr);
        btn.setOnClickListener(this);
        btn = (Button)mView.findViewById(R.id.login_share_les);
        btn.setOnClickListener(this);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.login));
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
        super.onPause();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            Log.i("google_drive_on_act_res", "hereherehere in fragment" + String.valueOf(requestCode));
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                Log.i(TAG, "REQUEST_CODE_RESOLUTION");

                if(resultCode == getActivity().RESULT_OK) {
                    Log.i(TAG, "REQUEST_CODE_RESOLUTION_OK");
                }
                break;
            case REQUEST_CODE_OPENER:
                if (resultCode == getActivity().RESULT_OK) {
                    Log.i(TAG, "REQUEST_CODE_OPENER");
                    DriveId driveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                }
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != getActivity().RESULT_OK) {
                    Log.i(TAG, "REQUEST_CODE_GOOGLE_PLAY");
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                Log.d(TAG, "1");
                if (resultCode == getActivity().RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    Log.d(TAG, "2");
                    if (accountName != null) {
                        Log.d(TAG, "3");
//                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        Log.d(TAG, accountName);
                    }
                } else if (resultCode == getActivity().RESULT_CANCELED) {
                    Snackbar snackbar = Snackbar
                            .make(getActivity().findViewById(R.id.main_coord_layout), "Account unspecified.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != getActivity().RESULT_OK) {
                    chooseAccount();
                }
                break;
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
//        getActivity().startActivityForResult(
//                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
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
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                getActivity(),
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;
        private boolean isShare;

        public MakeRequestTask(GoogleAccountCredential credential, boolean isShare) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Drive API Android Quickstart")
                    .build();
            this.isShare = isShare;
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
//                return getDataFromApi();
                if(isShare)
                    shareDataApi();
                else
                    queryDataApi();
                return null;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> fileInfo = new ArrayList<String>();
            FileList result = mService.files().list()
                    .setFields("nextPageToken, items(id, name)")
                    .execute();
            List<File> files = result.getItems();
            if (files != null) {
                for (File file : files) {
                    fileInfo.add(String.format("%s (%s)\n",
                            file.getTitle(), file.getId()));
                }
            }
            return fileInfo;
        }

        private void queryDataApi() throws IOException {
            FileList result = mService.files().list()
//                    .setSpaces("drive")
                    .setQ("name = '" + fileCode + ".data'")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(null)
                    .execute();
            Log.d(TAG, "query = name = '" + fileCode + "' and sharedWithMe = true");
            Log.d(TAG, "saved");

            List<File> files = result.getItems();
            Log.d(TAG, "queried " + files.size());
            Log.d(TAG, "0 " + files.get(0).getId());

            ((MainNavigationDrawer)getActivity()).setIsLoggedIn(true);
            File queriedFile = files.get(0);
            fileCode = files.get(0).getId();
            File fileMetadata = new File();
            fileMetadata.setTitle("Shared PolyTable data");
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            File file = mService.files().insert(fileMetadata)
                    .setFields("id")
                    .execute();

            ParentReference pr = new ParentReference();
            pr.setId(file.getId());
            queriedFile.getParents().add(pr);
//            File updatedFile = mService.parents().(fileCode, queriedFile).execute();
        }

        private void shareDataApi() throws IOException {
            Log.d(TAG, emailStr);

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
                    .setEmailAddress(emailStr);

            SharedPreferences pref = getActivity().getSharedPreferences(
                    "edu.amd.spbstu.polystudenttimetable", Context.MODE_PRIVATE);
            String FileId = pref.getString(PREF_FILE_ID, "");

            DriveId driveId = DriveId.decodeFromString(FileId);
            Log.d(TAG, "shared" + driveId.getResourceId());
            mService.permissions().insert(driveId.getResourceId(), userPermission)
                    .setFields("id")
                    .queue(batch, callback);
            batch.execute();
//            genDriveContents.discard(mClient);
        }
        @Override
        protected void onPostExecute(List<String> output) {
            if (output == null || output.size() == 0) {
                Snackbar snackbar = Snackbar
                        .make(getActivity().findViewById(R.id.main_coord_layout), getActivity().getResources().getString(R.string.share_success), Snackbar.LENGTH_LONG);
                snackbar.show();
//                if(!isShare)
//                    new UpdateFiles(getActivity(), ((MainNavigationDrawer)getActivity()).the_obj, fileCode).execute();
            } else {
                Snackbar snackbar = Snackbar
                        .make(getActivity().findViewById(R.id.main_coord_layout), "Data retrieved using the Drive API:", Snackbar.LENGTH_LONG);
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
                        getActivity().startActivityForResult(
                                ((UserRecoverableAuthIOException) mLastError).getIntent(),
                                REQUEST_AUTHORIZATION);
                    } else {

                        Snackbar snackbar = Snackbar
                                .make(getActivity().findViewById(R.id.main_coord_layout), "The following error occurred:\n"
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
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}
