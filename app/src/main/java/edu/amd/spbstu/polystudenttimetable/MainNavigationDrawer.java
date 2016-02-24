package edu.amd.spbstu.polystudenttimetable;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
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
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

public class MainNavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DetailedClassFragment.OnFragmentInteractionListener,
        TimeTableFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener {
//       GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener {

//    DBHelper dbHelper;
//    GoogleApiClient mGoogleApiClient;

    private static final int REQUEST_CODE_RESOLUTION = 3;

    public void onFragmentInteraction(Uri uri)
    {
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

/*
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
*/
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
        getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SearchTabFragment())
                    .commit();
    }

    @Override
    protected void onStart() {
        Log.d("init", "onStart");
        super.onStart();
//        mGoogleApiClient.connect();
    }
    /*
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("init", "GoogleApiClient connection suspended");
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i("init", "API client connected.");
    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d("init", "onActivityResult");
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }
    */

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

        Fragment fragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_search) {
            fragment = new SearchTabFragment();
        }
        if(fragment != null) {
            ViewGroup vg = (ViewGroup) findViewById(R.id.container);
            vg.removeAllViews();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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

    protected void setToolbarTitle(String s) {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        tb.setTitle(s);
        super.onStart();
//        mGoogleApiClient.connect();
    }

    /*
    // database

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("init", "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("create table mytable ("
                    + "id integer primary key autoincrement,"
                    + "name text,"
                    + "email text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
    */

    /*
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            return rootView;
        }
    }
}