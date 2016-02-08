package edu.amdspu.ttable;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.net.URLEncoder;
import java.util.ArrayList;

//public class ActivityMain extends AppCompatActivity
public class ActivityMain extends Activity
{
    private static final int STAGE_NA                   =-1;
    private static final int STAGE_SELECT_STUDENTS_OR_LECTURERS = 0;
    private static final int STAGE_LIST_LECTURERS               = 1;
    private static final int STAGE_LIST_FACULTIES               = 2;
    private static final int STAGE_LIST_GROUPS                  = 3;
    private static final int STAGE_LIST_TIMETABLE               = 4;
    private static final int STAGE_LIST_LECT_TIMETABLE          = 5;

    private     int             m_stage = STAGE_NA;
    private     ListFaculties   m_listFaculties;
//    private     ListGroups      m_listGroups;
    private     ListLecturers   m_listLecturers;
    private     boolean         m_activeApp;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d("TT", "onCreate ...");
        StaticStorage.clear();

        m_activeApp = true;

        //startDownloadListFaculties();
        selectStudentsOrLecturers();
    }
    protected void onPause()
    {
        m_activeApp = false;
        super.onPause();
    }
    protected void onDestroy()
    {
        m_activeApp = false;
        super.onDestroy();
    }

    public boolean isOnline()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if ((netInfo != null) && netInfo.isConnectedOrConnecting())
            return true;
        return false;
    }

    public void selectStudentsOrLecturers()
    {
        m_stage = STAGE_SELECT_STUDENTS_OR_LECTURERS;

        setContentView(R.layout.layout_stu_teacher);
        Button btnStu = (Button)findViewById(R.id.m_buttonStudents);
        Button btnLec = (Button)findViewById(R.id.m_buttonLecturers);
        btnStu.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onButtonStudents();
            }
        });
        btnLec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonLecturers();
            }
        });
    }
    private void onButtonStudents()
    {
        startDownloadListFaculties();
    }
    private void onButtonLecturers()
    {
        startDownloadListLecturers();
    }
    public void startDownloadListLecturers()
    {
        m_stage = STAGE_LIST_LECTURERS;
        setContentView(R.layout.layout_select_lecturer);

        ListView    listLec = (ListView)findViewById(R.id.m_listLecturers);
        m_listLecturers = new ListLecturers(this, listLec);
        StaticStorage.m_listLecturers.clear();

        Thread thread;
        m_activeApp = true;
        thread = new Thread() {
            public void run()
            {
                try {
                    while (m_activeApp)
                    {
                        Thread.sleep(200);
                        runOnUiThread(new Runnable()
                        {
                            ServerGetLecturers serverGetLects;
                            public void run()
                            {
                                if (!isOnline())
                                    m_listLecturers.setTurnOnInternet();
                            }
                        });
                        if (isOnline())
                        {
                            ServerGetLecturers serverGetLects;
                            serverGetLects = new ServerGetLecturers(m_listLecturers);
                            serverGetLects.execute("http://amd.stu.neva.ru/lecturers");
                            m_activeApp = false;
                        }
                    }
                }
                catch (Exception ex) { }
            }
        };
        thread.start();
    }


    public void startDownloadListFaculties()
    {
        setContentView(R.layout.layout_activity_main);

        ListView    listFac = (ListView)findViewById(R.id.m_listFac);
        m_listFaculties = new ListFaculties(this, listFac);
        StaticStorage.m_listFaculties.clear();

        Thread thread;
        m_activeApp = true;
        thread = new Thread() {
            public void run()
            {
                try
                {
                    while (m_activeApp)
                    {
                        Thread.sleep(200);
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                if (!isOnline())
                                    m_listFaculties.setTurnOnInternet();
                            }
                        });
                        if (isOnline())
                        {
//                            ServerGetFaculties serverGetFaclts = new ServerGetFaculties(m_listFaculties);
  //                          serverGetFaclts.execute("http://ruz2.spbstu.ru/api/v1/ruz/faculties/");
                            m_activeApp = false;
                        }
                    }       // while
                }
                catch (Exception ex) { }
            }

        };
        thread.start();
       m_stage = STAGE_LIST_FACULTIES;
    }


    public void startDownloadListGroups()
    {
        setContentView(R.layout.layout_list_groups);

        ListView    listGr = (ListView)findViewById(R.id.m_listGroups);
//        m_listGroups = new ListGroups(this, listGr);
        StaticStorage.m_listGroups.clear();
        StaticStorage.m_listGroupsName.clear();
//        ServerGetGroups serverGetGroups = new ServerGetGroups(m_listGroups);
        Faculty faculty = StaticStorage.m_listFaculties.get(StaticStorage.m_indexFaculty);
//        serverGetGroups.execute(faculty);
        m_stage = STAGE_LIST_GROUPS;
    }

    public void startDownloadGroupTimetable()
    {
        setContentView(R.layout.layout_list_ttable);

        Group group = StaticStorage.m_listGroups.get(StaticStorage.m_indexGroup);
        String strGetTimetable = "http://ruz2.spbstu.ru/api/v1/ruz/scheduler/" + String.valueOf(group.m_id);

        ListView    listTt = (ListView)findViewById(R.id.m_listTTable);
        ListTTable  llTable = new ListTTable(this, listTt);

//        ServerGetTTable  serverGetTTable = new ServerGetTTable(llTable);
//        serverGetTTable.execute(strGetTimetable);
        m_stage = STAGE_LIST_TIMETABLE;
    }
    public void startDownloadTimetableForLecturer()
    {
        setContentView(R.layout.layout_list_teachtable);

        ArrayList<Lecturer> listLecturers = StaticStorage.m_listLecturers;
        String strLecturer = listLecturers.get(StaticStorage.m_indexLecturer).m_fio;
        String strLecturerEnc = "";
        try
        {
            strLecturerEnc = URLEncoder.encode(strLecturer, "UTF-8");
        }
        catch (Exception ex)
        {
            Log.d("TT", "Exception for URLEncoder.encode");
            Log.d("TT", ex.getMessage());
            ex.printStackTrace();
        }

        Log.d("TT", "Encoded lecturer name is = " + strLecturerEnc);

        //String strGetLecturerUrl = "http://ruz2.spbstu.ru/api/v1/ruz/search/teachers?q=" + strLecturer;
        String strGetLecturerUrl = "http://ruz2.spbstu.ru/api/v1/ruz/search/teachers?q=" + strLecturerEnc;

        ListView    listLect = (ListView)findViewById(R.id.m_listTeachTable);
        ListLectTable  lectTable = new ListLectTable(this, listLect);

        ServerGetLectId  serverGetLectId = new ServerGetLectId(lectTable);
        serverGetLectId.execute(strGetLecturerUrl);
        m_stage = STAGE_LIST_LECT_TIMETABLE;
    }
    public void startDownloadTTForLecturer(int strLecturerId)
    {
        String strGetLecturerTableUrl = "http://ruz2.spbstu.ru/api/v1/ruz/teachers/" + String.valueOf(strLecturerId) + "/scheduler";

        ListView    listLect = (ListView)findViewById(R.id.m_listTeachTable);
        ListLectTable  lectTable = new ListLectTable(this, listLect);
        ServerGetLectTable serverGetLectTable = new ServerGetLectTable(lectTable);
        serverGetLectTable.execute(strGetLecturerTableUrl);
    }

    public void onBackPressed()
    {
        if (m_stage == STAGE_SELECT_STUDENTS_OR_LECTURERS)
        {
            // exit
            super.onBackPressed();
        }
        else if (m_stage == STAGE_LIST_FACULTIES)
            selectStudentsOrLecturers();
        else if (m_stage == STAGE_LIST_LECTURERS)
            selectStudentsOrLecturers();
        else if (m_stage == STAGE_LIST_GROUPS)
            startDownloadListFaculties();
        else if (m_stage == STAGE_LIST_TIMETABLE)
            startDownloadListGroups();
        else if (m_stage == STAGE_LIST_LECT_TIMETABLE)
            startDownloadListLecturers();
        else
        {
            // exit
            super.onBackPressed();
        }
    }


}
