package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.plus.Plus;

import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MainNavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DetailedClassFragment.OnFragmentInteractionListener,
        TimeTableFragment.OnFragmentInteractionListener,
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener,
        SearchFragment.OnFragmentInteractionListener
{


//    DBHelper dbHelper;
    private static final String TAG = "polytable_log";

    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_OPENER = 4;

    static final String PREF_IS_LOGGED_IN= "poly_table_logged_in";
    static final String PREF_GROUP= "poly_table_is_group";
    private boolean isLoggedIn;

    public Object the_obj = null;

    public void onFragmentInteraction(Uri uri)
    {
        return;
    }
    private SearchTabFragment mSTF;
    static private  boolean justStarted = true;
//    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_navigation_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSTF = new SearchTabFragment();
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

        isLoggedIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_IS_LOGGED_IN, false);


        Map<Integer, Group> tmp_groups = (Map<Integer, Group>)(LocalPersistence.readObjectFromFile(getApplicationContext(), "recent_groups.data"));
        if(tmp_groups != null)
            StaticStorage.m_recentGroups = tmp_groups;
        Map<Integer, Lecturer> tmp_lects = (Map<Integer, Lecturer>)(LocalPersistence.readObjectFromFile(getApplicationContext(), "recent_lects.data"));
        if(tmp_lects != null)
            StaticStorage.m_recentLecturers = tmp_lects;
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

//        dbHelper = new DBHelper(this);

       StaticStorage.clear();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        // number in header
        View nav_header = LayoutInflater.from(this).inflate(R.layout.nav_header_main_navigation_drawer, null);
        Log.d("init", nav_header.toString());
        TextView tv = (TextView) nav_header.findViewById(R.id.header_days);
        Log.d("init", tv.toString());
        tv.setText(String.valueOf(daysToExams()));
        navigationView.addHeaderView(nav_header);

        navigationView.setNavigationItemSelectedListener(this);

        Log.d("init", "begin transaction");
        SearchTabFragment lf = new SearchTabFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.container, lf, lf.toString());
        ft.addToBackStack(null);
        ft.commit();
        navigationView.getMenu().getItem(3).setChecked(true);
    }

    @Override
    protected void onStart() {
        Log.d("init", "onStart");
        super.onStart();
        if(isLoggedIn()) {
            update();
        }
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
            if (getFragmentManager().getBackStackEntryCount() > 0 ){
                getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
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
        FragmentManager fragmentManager = getFragmentManager();
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch(id) {
            case R.id.nav_search:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switchContent(new SearchTabFragment());
                    }
                }, 275);
                break;
            case R.id.nav_about:
                FragmentManager fm = getFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                Intent i = new Intent(this, ActivityMain.class);
                startActivity(i);
                break;
            case R.id.nav_login:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.container, new LoginFragment(), "login");
                        ft.addToBackStack(null);
                        ft.commit();
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
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
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
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                    navigationView.getMenu().getItem(0).setChecked(true);
                    navigationView.getMenu().getItem(1).setChecked(false);
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
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment, fragment.toString());
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("google_drive_on_act_res", "hereherehere in activity");
        if(((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).isChecked()) {
                LoginFragment fragment = (LoginFragment) getFragmentManager()
                        .findFragmentByTag("login");
                LoginFragment lf = new LoginFragment();
                lf.toString();
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        else
            super.onActivityResult(requestCode, resultCode, data);
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
    public boolean isGroup() {
        return the_obj.getClass().equals(Group.class);
    }

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