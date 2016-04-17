package edu.amd.spbstu.polystudenttimetable;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class MainNavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DetailedClassFragment.OnFragmentInteractionListener,
        LoginFragment.OnFragmentInteractionListener,
        MyDetailedClassFragment.OnFragmentInteractionListener,
        MyDetailedObjFragment.OnFragmentInteractionListener,
        MyHomeworkFragment.OnFragmentInteractionListener,
        MyTimeTableFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        TimeTableFragment.OnFragmentInteractionListener,
        REST.ConnectCBs,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{


//    DBHelper dbHelper;
    private static final String TAG = "polytable_log";
    private static final String LOCAL_FILE = "localtt.data";
    private static final String PREFERENCE_FIRST_RUN = "polytable_first_run";

    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_OPENER = 4;
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 5;
    private static final int REQ_ACCPICK = 1;
    private static final int REQ_CONNECT = 2;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final String PREF_IS_LOGGED_IN= "poly_table_logged_in";
    static final String PREF_GROUP= "poly_table_is_group";
    static final String PREF_FILE_ID = "poly_table_file_id";
    private boolean isLoggedIn;
    public Object the_obj = null;
    public ContactInfo the_contact = null;
    public String the_name = null;

    public void onFragmentInteraction(Uri uri)
    {
        return;
    }
    static private  boolean justStarted = true;
    GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_navigation_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


/*
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addApi(Plus.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
         */
        UT.init(this);
        if (!REST.init(this));
        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if( statusCode != ConnectionResult.SUCCESS)
        {
            Log.e("statuscode",statusCode+"");
            if(GooglePlayServicesUtil.isUserRecoverableError(statusCode))
            {
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                        statusCode,
                        MainNavigationDrawer.this,
                        REQUEST_CODE_RECOVER_PLAY_SERVICES);

                // If Google Play services can provide an error dialog
                if (errorDialog != null) {
                    errorDialog.show();
                }
            }
            else
            {
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.main_coord_layout), getResources().getString(R.string.services_err), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
        else {
            if (UT.AM.getEmail() == null) {
                startActivityForResult(AccountPicker.newChooseAccountIntent(null,
                                null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null),
                        REQ_ACCPICK);
            }
        }

        isLoggedIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_IS_LOGGED_IN, false);


        Map<Integer, Group> tmp_groups = (Map<Integer, Group>)(LocalPersistence.readObjectFromFile(getApplicationContext(), "recent_groups.data"));
        for(Group gr : tmp_groups.values()) {
            for(Lesson lesson : gr.m_listLessons) {
                for (List<RegLessonInstance> lreg : lesson.m_reg.values()) {
                    for (RegLessonInstance reg : lreg) {
                        reg.parent = lesson;
                        for (RegLessonInstance.Homework hw : reg.m_homework.values()) {
                            hw.m_lesson = reg;
                        }
                    }
                }
            }
        }
        if(tmp_groups != null)
            StaticStorage.m_recentGroups = tmp_groups;
        Map<Integer, Lecturer> tmp_lects = (Map<Integer, Lecturer>)(LocalPersistence.readObjectFromFile(getApplicationContext(), "recent_lects.data"));
        for(Lecturer lect : tmp_lects.values()) {
            for(Lesson lesson : lect.m_listLessons) {
                for (List<RegLessonInstance> lreg : lesson.m_reg.values()) {
                    for (RegLessonInstance reg : lreg) {
                        reg.parent = lesson;
                        for (RegLessonInstance.Homework hw : reg.m_homework.values()) {
                            hw.m_lesson = reg;
                        }
                    }
                }
            }
        }
        if(tmp_lects != null)
            StaticStorage.m_recentLecturers = tmp_lects;
        if(isLoggedIn) {
            the_obj = LocalPersistence.readObjectFromFile(getApplicationContext(), LOCAL_FILE);
            if (the_obj instanceof Group) {
                the_name = ((Group) the_obj).m_info.m_name;
                the_contact = ((Group) the_obj).m_info.m_contact;
                for(Lesson lesson : ((Group)the_obj).m_listLessons) {
                    for (List<RegLessonInstance> lreg : lesson.m_reg.values()) {
                        for (RegLessonInstance reg : lreg) {
                            reg.parent = lesson;
                            for (RegLessonInstance.Homework hw : reg.m_homework.values()) {
                                hw.m_lesson = reg;
                            }
                        }
                    }
                }
            } else if (the_obj instanceof Lecturer){
                the_name = ((Lecturer) the_obj).m_info.m_fio;
                the_contact = ((Lecturer) the_obj).m_info.m_contact;
                for(Lesson lesson : ((Lecturer)the_obj).m_listLessons) {
                    for (List<RegLessonInstance> lreg : lesson.m_reg.values()) {
                        for (RegLessonInstance reg : lreg) {
                            reg.parent = lesson;
                            for (RegLessonInstance.Homework hw : reg.m_homework.values()) {
                                hw.m_lesson = reg;
                            }
                        }
                    }
                }
            }
        }
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        if(Locale.getDefault().getDisplayLanguage().equals("English")) {
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
            boolean firstRun = p.getBoolean(PREFERENCE_FIRST_RUN, true);
            if(firstRun) {
                    new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.eng_lang))
                            .setMessage(getResources().getString(R.string.rus_titles))
                            .setPositiveButton(android.R.string.ok, null) // dismisses by default
                            .create()
                            .show();
                }
                p.edit().putBoolean(PREFERENCE_FIRST_RUN, false).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

//        dbHelper = new DBHelper(this);

       StaticStorage.clear();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if(isOnline())
            new ServerGetPrimatLecturers(this, false).execute();
        // number in header
        View nav_header = LayoutInflater.from(this).inflate(R.layout.nav_header_main_navigation_drawer, null);
        Log.d("init", nav_header.toString());
        TextView tv = (TextView) nav_header.findViewById(R.id.header_days1);
        Log.d("init", tv.toString());
        tv.setText(String.valueOf(daysToExams()));
        tv = (TextView) nav_header.findViewById(R.id.header_days2);
        Log.d("init", tv.toString());
        tv.setText(String.valueOf(daysToExams() + 7));

        TextView subtv = (TextView) nav_header.findViewById(R.id.header_days_sub1);
        Log.d("init", tv.toString());
        switch(daysToExams() % 10) {
            case 1:
                subtv.setText(getResources().getString(R.string.header_1text1));
                break;
            case 2:
            case 3:
            case 4:
                subtv.setText(getResources().getString(R.string.header_1text234));
                break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 0:
                subtv.setText(getResources().getString(R.string.header_1text56789));
                break;
        }
        subtv = (TextView) nav_header.findViewById(R.id.header_days_sub2);
        Log.d("init", tv.toString());
        switch((daysToExams() + 7) % 10) {
            case 1:
                subtv.setText(getResources().getString(R.string.header_2text1));
                break;
            case 2:
            case 3:
            case 4:
                subtv.setText(getResources().getString(R.string.header_2text234));
                break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 0:
                subtv.setText(getResources().getString(R.string.header_2text56789));
                break;
        }

        navigationView.addHeaderView(nav_header);

        navigationView.setNavigationItemSelectedListener(this);

        Log.d("init", "begin transaction");
        android.support.v4.app.Fragment frg = new SearchTabFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, frg, "search")
                .addToBackStack(null)
                .commit();
        navigationView.getMenu().getItem(3).setChecked(true);
    }

    @Override
    protected void onStart() {
        Log.d("init", "onStart");
        super.onStart();
//        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    boolean isLoggedIn() {
        return isLoggedIn;
    }

    void setIsLoggedIn(boolean b) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(PREF_IS_LOGGED_IN, b);
        isLoggedIn = b;
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1 ) {
                getSupportFragmentManager().popBackStack();
            } else {
                Intent i = new Intent(this, ActivityMain.class);
                finish();
                startActivity(i);
//                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main_navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch(id) {
            case R.id.nav_search:
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;

                switchContent(new SearchTabFragment());

/*
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                if (currentapiVersion >= 17) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            switchContent(new SearchTabFragment());
                            android.support.v4.app.Fragment frg = new SearchTabFragment();
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, frg, "search")
                                    .addToBackStack(null).commit();
                        }
                    }, 275);
                } else {
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
                */
                break;
            case R.id.nav_about:
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                Intent i = new Intent(this, ActivityMain.class);
                finish();
                startActivity(i);
                break;
            case R.id.nav_help:
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                switchContent(new MyHelpFragment(this));
                break;

            case R.id.nav_login:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, new LoginFragment(), "login")
                                .addToBackStack(null)
                                .commit();
                    }
                }, 275);
/*
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, new LoginFragment(), "login");
                ft.addToBackStack(null);
                ft.commit();
*/
                break;
            case R.id.nav_my_tt:
                if(isLoggedIn()) {
                    if (the_obj != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                switchContent(new MyTimeTableFragment());
                            }
                        }, 200);
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle(getResources().getString(R.string.error))
                                .setMessage(getResources().getString(R.string.not_loaded))
                                .setPositiveButton(android.R.string.ok, null) // dismisses by default
                                .create()
                                .show();
                    }
                } else {
                    navigationView = (NavigationView) findViewById(R.id.nav_view);
                    navigationView.getMenu().getItem(0).setChecked(true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            switchContent(new LoginFragment());
                        }
                    }, 200);
                }
                break;
            case R.id.nav_my_hw:
                if(isLoggedIn()) {
                    if (the_obj != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                switchContent(new MyHomeworkFragment());
                            }
                        }, 200);
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle(getResources().getString(R.string.error))
                                .setMessage(getResources().getString(R.string.not_loaded))
                                .setPositiveButton(android.R.string.ok, null) // dismisses by default
                                .create()
                                .show();
                    }
                } else {
                    navigationView = (NavigationView) findViewById(R.id.nav_view);
                    navigationView.getMenu().getItem(0).setChecked(true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            switchContent(new LoginFragment());
                        }
                    }, 200);
                }
                break;
        }
        return true;
    }

    private static int daysToExams () {
        LocalDate exam1 = new LocalDate(LocalDate.now().getYear(), 12, 1);
        exam1 = exam1.dayOfMonth().withMaximumValue();
        while ( exam1.getDayOfWeek() != DateTimeConstants.MONDAY ) {
            exam1 = exam1.minusDays(1);
        }

        LocalDate exam2 = new LocalDate(LocalDate.now().getYear(), 6, 1);
        while ( exam2.getDayOfWeek() != DateTimeConstants.MONDAY ) {
            exam2 = exam2.minusDays(1);
        }
        if(Days.daysBetween(LocalDate.now(), exam2).getDays() > 0)
            return Days.daysBetween(LocalDate.now(), exam2).getDays();
        return Days.daysBetween(LocalDate.now(), exam1).getDays();
    }

    public void switchContent(Fragment fragment) {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, fragment.toString())
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int result, Intent data) {
        Log.i("google_drive_on_act_res", "hereherehere in activity");
        switch (requestCode) {
            case REQ_ACCPICK:
                Log.d(TAG, "REQ_ACCPICK");
                if (data != null && data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) != null) {
                    Log.d(TAG, "data != null");
                    UT.AM.setEmail(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    REST.connect();
                }
                if (!REST.init(this)) {                                                    UT.lg("act result - NO ACCOUNT");
                    suicide(R.string.try_again); //---------------------------------->>>
                }
                return;
            case REQ_CONNECT:
                if (result == RESULT_OK)
                    REST.connect();
                else {                                                                       UT.lg("act result - NO AUTH");
                    suicide(R.string.wrong_data);  //---------------------------------->>>
                }
                return;
            case REQUEST_CODE_OPENER:
                if (result == RESULT_OK) {
                    final DriveId driveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    final String rid = driveId.getResourceId();
//                    Uri uri = Uri.parse("intent://drive.google.com/drive/u/0/mobile/folders/" + driveId.getResourceId());

                    Log.d(TAG, "Selected file's ID: " + driveId);

                    new AsyncTask<Void, String, File>() {
                        @Override
                        protected File doInBackground(Void... params) {
                            String by = REST.read(rid);
                            try {
                                Metadata meta = driveId.asDriveFile().getMetadata(mGoogleApiClient).await().getMetadata();
                                File tmpFile = File.createTempFile(meta.getTitle(), meta.getFileExtension(), UT.acx.getExternalCacheDir());

                                FileOutputStream fileOut = openFileOutput(tmpFile.getAbsolutePath(), Activity.MODE_PRIVATE);
                                OutputStreamWriter objectOut = new OutputStreamWriter(fileOut);
                                objectOut.write(by);
                                objectOut.flush();
                                objectOut.close();
                                return tmpFile;
                            }
                            catch (IOException e) {
                                Log.d(TAG, e.getMessage());
                                return null;
                            }
                        }
                        @Override
                        protected void onPostExecute(File nada) {
                            super.onPostExecute(nada);
                            Log.d(TAG, "DONE");

                            MimeTypeMap myMime = MimeTypeMap.getSingleton();
                            String mimeType = myMime.getMimeTypeFromExtension(fileExt(nada.getName()).substring(1));

                            Intent newIntent = new Intent(Intent.ACTION_VIEW);
                            newIntent.setDataAndType(Uri.fromFile(nada), mimeType);
                            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            try {
                                startActivity(newIntent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(MainNavigationDrawer.this, "No handler for this type of file.", Toast.LENGTH_LONG).show();
                            }
                        }

                    }.execute();
                }
                return;
        }
        if(((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).isChecked()) {
                LoginFragment fragment = (LoginFragment) getSupportFragmentManager()
                        .findFragmentByTag("login");
                LoginFragment lf = new LoginFragment();
                lf.toString();
                fragment.onActivityResult(requestCode, result, data);
            }
        else {
            super.onActivityResult(requestCode, result, data);
        }
    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    private void suicide(int rid) {
        UT.AM.setEmail(null);
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.main_coord_layout), getResources().getString(rid), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void suicide(String msg) {
        UT.AM.setEmail(null);
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.main_coord_layout), msg, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onResume() {  super.onResume();
        REST.connect();
    }
    @Override
    protected void onPause() {  super.onPause();
        REST.disconnect();
    }
    @Override
    public void onConnOK() {
        Log.d(TAG, "\n\nCONNECTED TO: " + UT.AM.getEmail());
        if(isLoggedIn()) {
            update();
        }
    }

    @Override
    public void onConnFail(Exception ex) {
        Log.d(TAG, "CONNECTION FAIL!");
        if(!isOnline()) {
            update();
            return;
        }

        if (ex == null) {                                                         UT.lg("connFail - UNSPECD 1");
            Log.d(TAG, "here");
            suicide(R.string.try_again);  return;  //---------------------------------->>>
        }
        if (ex instanceof UserRecoverableAuthIOException) {                        UT.lg("connFail - has res");
            startActivityForResult((((UserRecoverableAuthIOException) ex).getIntent()), REQ_CONNECT);
        } else if (ex instanceof GoogleAuthIOException) {                          UT.lg("connFail - SHA1?");
            if (ex.getMessage() != null) suicide(ex.getMessage());  //--------------------->>>
            else  suicide(R.string.try_again);  //---------------------------------->>>
        } else {                                                                  UT.lg("connFail - UNSPECD 2");
            Log.d(TAG, ex.getMessage());
            suicide(R.string.try_again);  //---------------------------------->>>
        }
    }

    public void createTree(final Object obj) {
        new AsyncTask<Void, String, Boolean>() {
            ProgressDialog pb;

            private String findOrCreateFolder(String prnt, String titl){
                ArrayList<ContentValues> cvs = REST.search(prnt, titl, UT.MIME_FLDR);
                String id, txt;
                if (cvs.size() > 0) {
                    txt = "found ";
                    id =  cvs.get(0).getAsString(UT.GDID);
                } else {
                    id = REST.createFolder(prnt, titl);
                    txt = "created ";
                }
                if (id != null)
                    txt += titl;
                else
                    txt = "failed " + titl;
                publishProgress(txt);
                return id;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                String titl;
                Object gen;
                List<Lesson> lessons;
                List<String> ids;

                if (obj instanceof Group) {
                    titl = ((Group) obj).m_info.m_name.split("/")[0];
                    the_name = ((Group)obj).m_info.m_name;
                    the_contact = ((Group)obj).m_info.m_contact;
                    gen = ((Group) obj).m_info;
                    lessons = ((Group) obj).m_listLessons;
                    ids = ((Group)obj).m_info.m_listLessonsId;
                } else if (obj instanceof Lecturer){
                    titl = ((Lecturer) obj).m_info.m_fio.split(" ")[0];
                    the_name = ((Lecturer)obj).m_info.m_fio;
                    the_contact = ((Lecturer)obj).m_info.m_contact;
                    gen = ((Lecturer)obj).m_info;
                    lessons = ((Lecturer) obj).m_listLessons;
                    ids = ((Lecturer)obj).m_info.m_listLessonsId;
                } else {
                    return null;
                }
                String rsid = findOrCreateFolder("root", UT.MYROOT);
                if (rsid != null) {
                    rsid = findOrCreateFolder(rsid, UT.titl2Month(titl));
                    if (rsid != null) {
                        // others
                        ids.clear();
                        for(int i = 0; i < lessons.size(); ++i) {
//                            File fll = UT.byte2File(serializableToByte(lessons.get(i)), "tmp");
                            Gson gson = new GsonBuilder()
                                    .setPrettyPrinting()
                                    .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                                    .create();
                            File fll = UT.str2File(gson.toJson(lessons.get(i)), "tmp");
                            String id = null;
                            if (fll != null) {
                                id = REST.createFile(rsid, String.valueOf(lessons.get(i).hashCode()), UT.MIME_TEXT, fll);
                                fll.delete();
                            }
                            Log.d(TAG, "created lesson" + String.valueOf(i));
                            lessons.get(i).driveFileId = id;
                            ids.add(i, id);
                        }
                        // main file
//                        File fl = UT.byte2File(serializableToByte(gen), "tmp");
                        Gson gson = new GsonBuilder()
                                .setPrettyPrinting()
                                .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                                .create();
                        File fl = UT.str2File(gson.toJson(gen), "tmp");
                        if(fl == null) {
                            Log.d(TAG, "fl null");
                        } else {
                            Log.d(TAG, "fl " + String.valueOf(fl));
                        }

                        if(titl == null) {
                            Log.d(TAG, "titl null");
                        } else {
                            Log.d(TAG, "fl " + String.valueOf(fl));
                        }

                        if(titl == null) {
                            Log.d(TAG, "rsid null");
                        } else {
                            Log.d(TAG, "rsid " + String.valueOf(rsid));
                        }

                        String gid = null;
                        if (fl != null) {
                            gid = REST.createFile(rsid, titl, UT.MIME_TEXT, fl);
                            fl.delete();
                        }

                        Log.d(TAG, "created general " + String.valueOf(gid));
                        SharedPreferences.Editor editor = getSharedPreferences(
                                getPackageName(), Context.MODE_PRIVATE).edit();
                        editor.putString(PREF_FILE_ID, gid);
                        Log.d(TAG, "saved " + String.valueOf(editor.commit()));

                        the_obj = obj;
                        setIsLoggedIn(true);
                        switchContent(new MyTimeTableFragment());
                    }
                }
                return true;
            }
            @Override
            protected void onProgressUpdate(String... strings) { super.onProgressUpdate(strings);
                Log.d(TAG, strings[0]);
            }
            @Override
            protected void onPostExecute(Boolean nada) {
                super.onPostExecute(nada);
                Log.d(TAG, "DONE");
                pb.dismiss();
                if(nada == null) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.main_coord_layout), getResources().getString(R.string.error), Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.main_coord_layout), getResources().getString(R.string.load_success), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d(TAG, "DO");
                pb = new ProgressDialog(MainNavigationDrawer.this);
                pb.setMessage(MainNavigationDrawer.this.getResources().getString(R.string.placeholder_downloading));

                pb.setCancelable(true);
                pb.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(true);
                    }
                });
                pb.show();
            }
        }.execute();
    }

    public void write(final Lesson lesson) {
        new AsyncTask<Void, String, String>() {
            ProgressDialog pb;
            @Override
            protected String doInBackground(Void... params) {

 //               File fl = UT.byte2File(serializableToByte(lesson), "tmp");
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                        .create();

                File fl = UT.str2File(
                        gson.toJson(lesson), "tmp");
                return REST.update(lesson.driveFileId, String.valueOf(lesson.hashCode()), UT.MIME_TEXT, null, fl);
            }

            @Override
            protected void onProgressUpdate(String... strings) { super.onProgressUpdate(strings);
                Log.d(TAG, strings[0]);
            }

            @Override
            protected void onPostExecute(String nada) {
                super.onPostExecute(nada);
                Log.d(TAG, "DONE");
                pb.dismiss();
                if(nada == null) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.main_coord_layout), getResources().getString(R.string.error), Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    LocalPersistence.writeObjectToFile(getApplicationContext(), the_obj, LOCAL_FILE);
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.main_coord_layout), getResources().getString(R.string.load_success), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d(TAG, "DO");
                pb = new ProgressDialog(MainNavigationDrawer.this);
                pb.setMessage(MainNavigationDrawer.this.getResources().getString(R.string.placeholder_saving));
                pb.show();
            }

        }.execute();
    }

    public void write() {
        new AsyncTask<Void, String, String>() {
            ProgressDialog pb;
            @Override
            protected String doInBackground(Void... params) {
                String gid = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(PREF_FILE_ID, null);
                if(gid == null)
                    Log.d(TAG, "gid == null");
                if (gid == null)  { return null; }

                Object gen = null;
                String titl = null;
                if (the_obj instanceof Group) {
                    titl = String.valueOf(((Group) the_obj).m_info.m_id);
                    gen = ((Group) the_obj).m_info;
                } else if (the_obj instanceof Lecturer) {
                    titl = String.valueOf(((Lecturer) the_obj).m_info.m_id);
                    gen = ((Lecturer) the_obj).m_info;
                }

//                File fl = UT.byte2File(serializableToByte(gen), "tmp");
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                        .create();

                File fl = UT.str2File(gson.toJson(gen), "tmp");
                return REST.update(gid, titl, UT.MIME_TEXT, null, fl);
            }
            @Override
            protected void onProgressUpdate(String... strings) { super.onProgressUpdate(strings);
                Log.d(TAG, strings[0]);
            }
            @Override
            protected void onPostExecute(String nada) {
                super.onPostExecute(nada);
                Log.d(TAG, "DONE");
                pb.dismiss();
                if(nada == null) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.main_coord_layout), getResources().getString(R.string.error), Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    LocalPersistence.writeObjectToFile(getApplicationContext(), the_obj, LOCAL_FILE);
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.main_coord_layout), getResources().getString(R.string.load_success), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    update();
                }
            }
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d(TAG, "DO");
                pb = new ProgressDialog(MainNavigationDrawer.this);
                pb.setMessage(MainNavigationDrawer.this.getResources().getString(R.string.placeholder_saving));
                pb.show();
            }

        }.execute();
    }
    public void open() {

        Log.i(TAG, "Creating new contents.");
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }
    @Override
    public void onConnected(Bundle connectionHint) {
//        super.onConnected(connectionHint);
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{"text/plain", "text/html"})
                .build(mGoogleApiClient);
        try {
            startIntentSenderForResult(
                    intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.w(TAG, "Unable to send intent", e);
        }
    }
/*
    public void update() {
        if (isLoggedIn()) {
            new UpdateFiles(this, the_obj).execute();
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.error))
                    .setMessage(getResources().getString(R.string.relogin))
                    .setPositiveButton(android.R.string.ok, null) // dismisses by default
                    .create()
                    .show();
        }
    }
     */

    public void update() {
        if (!isLoggedIn()) {
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.error))
                    .setMessage(getResources().getString(R.string.relogin))
                    .setPositiveButton(android.R.string.ok, null) // dismisses by default
                    .create()
                    .show();
            return;
        }
        new AsyncTask<Void, String, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                Log.d(TAG, "loading general");

                String gid = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(PREF_FILE_ID, null);
                if(gid == null)
                    Log.d(TAG, "gid == null");
                if (gid == null)  { return null; }

//                byte[] by = REST.read(gid);
                String str = REST.read(gid);
                Log.d(TAG, "here");
                if(str == null) return  null;

//                Log.d(TAG, String.valueOf(str.hashCode()));

//                Object obj = bytesToSerializable(by);
                Object obj;
                try {
                    Gson gson = new GsonBuilder()
                            .setPrettyPrinting()
                            .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                            .create();
                    obj = gson.fromJson(str, GroupInfo.class);
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    obj = null;
                }

                List<Lesson> lessons;
                List<String> ids;
                Object result;
                if(obj != null) {
                    result = new Group();
                    ((Group)result).m_info = (GroupInfo)obj;
                    the_name = ((GroupInfo)obj).m_name;
                    the_contact = ((GroupInfo)obj).m_contact;
                    lessons = ((Group)result).m_listLessons;
                    ids = ((Group)result).m_info.m_listLessonsId;
                } else {
                    Log.d(TAG, "4");
                    try {
                        Gson gson = new GsonBuilder()
                                .setPrettyPrinting()
                                .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                                .create();

                        obj = gson.fromJson(str, LecturerInfo.class);
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                        obj = null;
                    }
                    if(obj == null) return  null;
                    result = new Lecturer();
                    ((Lecturer)result).m_info = (LecturerInfo)obj;
                    the_name = ((LecturerInfo)obj).m_fio;
                    the_contact = ((LecturerInfo)obj).m_contact;

                    lessons = ((Lecturer)result).m_listLessons;
                    ids = ((Lecturer)result).m_info.m_listLessonsId;
                }
                /*
                if(obj instanceof GroupInfo) {
                    result = new Group();
                    ((Group)result).m_info = (GroupInfo)obj;
                    the_name = ((GroupInfo)obj).m_name;
                    lessons = ((Group)result).m_listLessons;
                    ids = ((Group)result).m_info.m_listLessonsId;
                } else if(obj instanceof LecturerInfo) {

                    result = new Lecturer();
                    ((Lecturer)result).m_info = (LecturerInfo)obj;
                    the_name = ((LecturerInfo)obj).m_fio;
                    lessons = ((Lecturer)result).m_listLessons;
                    ids = ((Lecturer)result).m_info.m_listLessonsId;
                } else {
                    return null;
                }
*/
                Log.d(TAG, "1");
                for(int i = 0; i < ids.size(); ++i) {
//                    byte[] lby = REST.read(ids.get(i));
                    String lby = REST.read(ids.get(i));
                    if(lby == null) return  null;
//                    lessons.add(i, (Lesson)bytesToSerializable(lby));
                    Log.d(TAG, "2");
                    Gson gson = new GsonBuilder()
                            .setPrettyPrinting()
                            .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                            .create();

                    Log.d(TAG, "3");
                    Log.d(TAG, "exception on " + String.valueOf(i));
                    Log.d(TAG, lby);
                    Type type = new TypeToken<Lesson>() { }.getType();
                    Lesson l = gson.fromJson(lby, type);
                    lessons.add(i, l);
                    if(lessons.get(i) == null) return  null;
                    for(List<RegLessonInstance> lreg : lessons.get(i).m_reg.values()) {
                        for(RegLessonInstance reg: lreg) {
                            reg.parent = lessons.get(i);
                            for(RegLessonInstance.Homework hw: reg.m_homework.values()) {
                                hw.m_lesson = reg;
                            }
                        }
                    }
                    lessons.get(i).driveFileId = ids.get(i);
                }
                Log.d(TAG, "4");
                the_obj = result;
                LocalPersistence.writeObjectToFile(getApplicationContext(), the_obj, LOCAL_FILE);

                return true;
            }
            @Override
            protected void onProgressUpdate(String... strings) { super.onProgressUpdate(strings);
                Log.d(TAG, strings[0]);
            }
            @Override
            protected void onPostExecute(Boolean nada) {
                super.onPostExecute(nada);
                findViewById(R.id.toolbar_progress).setVisibility(View.INVISIBLE);
                Log.d(TAG, "DONE");
                if(nada == null) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.main_coord_layout), getResources().getString(R.string.error), Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.main_coord_layout), getResources().getString(R.string.load_success), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
            @Override
            protected void onPreExecute() {
                findViewById(R.id.toolbar_progress).setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    byte[] serializableToByte(Object obj) {
        // serialize the object
        try {
            Log.d(TAG, "0");
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            Log.d(TAG, "1");
            ObjectOutputStream so = new ObjectOutputStream(bo);
            Log.d(TAG, "2");
            so.writeObject(obj);
            Log.d(TAG, "3");
            so.flush();
            Log.d(TAG, "4");
            return bo.toByteArray();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    Object bytesToSerializable(byte[] b ) {
        // deserialize the object
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            return  (Object) si.readObject();
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    /*
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.main_coord_layout),
                        getResources().getString(R.string.connect_fail),
                        Snackbar.LENGTH_LONG);
        snackbar.show();
        if (((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).isChecked())
        {
            Log.i(TAG, "GoogleApiClient connection failed, redirecting to fragment");
            LoginFragment fragment = (LoginFragment) getFragmentManager()
                    .findFragmentByTag("login");
            LoginFragment lf = new LoginFragment();
            lf.toString();
            fragment.onConnectionFailed(result);

        }
        else {
            // Called whenever the API client fails to connect.
            Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
            if (!result.hasResolution()) {
                // show the localized error dialog.
                GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
                return;
            }
        }
    }

    @Override
    public void onResume() {
//        if(isLoggedIn())
//            mGoogleApiClient.connect();
        super.onResume();
    }

    @Override
    public void onPause() {
//        if(isLoggedIn())
//            mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected in activity.");
        Log.d(TAG, Plus.AccountApi.getAccountName(mGoogleApiClient));
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.main_coord_layout),
                        getResources().getString(R.string.connect_email) + " " + Plus.AccountApi.getAccountName(mGoogleApiClient),
                        Snackbar.LENGTH_LONG);
        snackbar.show();
        switch(whatToDoOnConnected) {
            case UPLOAD_FILES:
                new UpdateFiles(this, (Group)null).execute();
            case DOWNLOAD_FILES:
                if (the_group != null) {
                    new UpdateFiles(this, the_group).execute();
                }
                if (the_lect != null) {
                    new UpdateFiles(this, the_lect).execute();
                }
            case DELETE_FILES:
                assert ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).isChecked();
            case CREATE_FILES:
                assert ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).isChecked();
            case FIND_FILES:
                assert ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).isChecked();

        }
        if(isLoggedIn() && justStarted)
        if (!isLoggedIn()) {
            if (((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).isChecked())
            {
                if(!isOnline()) {
                    askForInternet();
                    return;
                }
                IntentSender intentSender = Drive.DriveApi
                        .newOpenFileActivityBuilder()
                        .setMimeType(new String[] { DriveFolder.MIME_TYPE })
                        .build(mGoogleApiClient);
                try {
                    startIntentSenderForResult(
                            intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    Log.w(TAG, "Unable to send intent", e);
                }
            }
        } else {
            new UpdateFiles(this, (Group)null).execute();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }
*/
    public class LocalDateSerializer implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate>
    {

        private static final String PATTERN = "'yyyy-MM-dd'";
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(PATTERN);


        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context)
        {
            String retVal = fmt.print(src);
            Log.v(TAG, "MY LOCALDATE SERIALIZED" + retVal);
//            return new JsonPrimitive(retVal);
            return new JsonPrimitive(src.toString());
        }


        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            String str = json.toString();
            str = str.substring(1, str.length() - 1);
            Log.v(TAG,"MY LOCALDATE DESERIALIZED" + str);
//            return fmt.parseLocalDate(str);
            return LocalDate.parse(str);
        }
    }

    public boolean isGroup() {
        if(the_obj != null)
            return the_obj.getClass().equals(Group.class);
        return false;
    }

    public boolean isOnline()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if ((netInfo != null) && netInfo.isConnectedOrConnecting())
            return true;
        return false;
    }

    public void askForInternet() {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.internet_dialog))
                .setMessage(getResources().getString(R.string.internet_dialog_text))
                .setPositiveButton(android.R.string.ok, null) // dismisses by default
                .create()
                .show();
    }
}