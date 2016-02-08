package edu.amd.spbstu.polystudenttimetable;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by artem on 12/2/15.
 */
public class ServerGetTable extends AsyncTask<Group, String, String>
{
    private ArrayAdapter<String> m_listTTable;
    private Fragment m_ctx;
    private ProgressDialog pb;
    public ServerGetTable(ArrayAdapter<String> l, Fragment ctx)
    {
        m_listTTable = l;
        m_ctx = ctx;
    }

    protected String doInBackground(Group... group)
    {
        String strUrl = "http://ruz2.spbstu.ru/api/v1/ruz/scheduler/" + String.valueOf(group[0].m_id);
        URL url;
        Log.d("init", strUrl);
        try
        {
            Log.d("TT", "Start read web from " + strUrl);
            url = new URL(strUrl);
            InputStream iStream = url.openConnection().getInputStream();

            BufferedReader r = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null)
            {
                total.append(line);
            }
            line = total.toString();
            return line;
        }
        catch (Exception ex)
        {
            Log.d("TT", "Exception !");
            Log.d("TT", ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
    private void parseJson(String strJson)
    {
        Group                   group;
        String                  strSubject;
        int                     lessonType;
        String                  strTimeStart, strTimeEnd;
        String                  strTeacherFIO;
        String                  strRoomName;
        String                  strBldName;
        Lesson lesson;
        ArrayList<Lesson> listLessons;

        listLessons = StaticStorage.m_listLessons;

        // init lessons table with empty lessons
        for (int i = 0; i < 5*6; i++)
        {
            lesson = new Lesson();
            listLessons.set(i, lesson);
        }
        Log.d("init", "init lessons");
        try
        {
            JSONObject objRoot  = new JSONObject(strJson);
            JSONObject objWeek  = (JSONObject)objRoot.get("week");
            JSONArray arrDays  = (JSONArray)objRoot.get("days");
            JSONObject objGroup = (JSONObject)objRoot.get("group");

            String strWeekStart = (String)objWeek.get("date_start");
            String strWeekEnd   = (String)objWeek.get("date_end");
            boolean isOdd       = (boolean)objWeek.get("is_odd");

            strTeacherFIO = ""; strRoomName = ""; strBldName = "";
            for (int d = 0; d < 6; d++)
            {
                JSONObject dayWeek = (JSONObject) arrDays.getJSONObject(d);
                JSONArray arrLessons = (JSONArray) dayWeek.get("lessons");
                int numLes = arrLessons.length();
                for (int i = 0; i < numLes; i++)
                {
                    JSONObject objLesson = (JSONObject) arrLessons.getJSONObject(i);
                    strSubject = (String) objLesson.get("subject");
                    lessonType = (int) objLesson.get("type");
                    strTimeStart = (String) objLesson.get("time_start");
                    strTimeEnd = (String) objLesson.get("time_end");
                    strTeacherFIO = "";
                    JSONArray arrTeachers = (JSONArray) objLesson.get("teachers");
                    int numTeachers = arrTeachers.length();
                    for (int t = 0; t < numTeachers; t++)
                    {
                        JSONObject objTeacher = (JSONObject) arrTeachers.getJSONObject(t);
                        strTeacherFIO = (String) objTeacher.get("full_name");
                    }
                    strRoomName = "";
                    strBldName = "";
                    JSONArray arrRooms = (JSONArray) objLesson.get("auditories");
                    int numRooms = arrRooms.length();
                    for (int r = 0; r < numRooms; r++)
                    {
                        JSONObject objRoom = (JSONObject) arrRooms.getJSONObject(r);
                        strRoomName = (String) objRoom.get("name");
                        JSONObject objBuilding = (JSONObject) objRoom.get("building");
                        strBldName = (String) objBuilding.get("abbr");
                    } // for (r) rooms
                    lesson = new Lesson();
                    lesson.m_day = d;
                    lesson.m_subject = strSubject;
                    lesson.m_type = lessonType;
                    lesson.m_timeStart = strTimeStart;
                    lesson.m_timeEnd = strTimeEnd;
                    lesson.m_teacherFio = strTeacherFIO;
                    lesson.m_roomName = strRoomName;
                    lesson.m_buildingName = strBldName;
                    if (strTimeStart.equalsIgnoreCase("08:00"))
                        lesson.m_hour = 0;
                    if (strTimeStart.equalsIgnoreCase("10:00"))
                        lesson.m_hour = 1;
                    if (strTimeStart.equalsIgnoreCase("12:00"))
                        lesson.m_hour = 2;
                    if (strTimeStart.equalsIgnoreCase("14:00"))
                        lesson.m_hour = 3;
                    if (strTimeStart.equalsIgnoreCase("16:00"))
                        lesson.m_hour = 4;
                    int idx = d * 5 + lesson.m_hour;
                    listLessons.set(idx, lesson);

                }   // for (i) num lessons in day
            }       // for (d) num days in week
            StaticStorage.m_isInitialized = true;
        }
        catch (Exception ex)
        {
            Log.d("TT", "Exception JSON parse timetable !");
            Log.d("TT", ex.getMessage());
            ex.printStackTrace();
        }

    }

    @Override
    protected void onPostExecute(String strResult)
    {
        Log.d("TT", "Read completed with" + strResult.substring(0, 128));
        parseJson(strResult);
        pb.dismiss();
        FragmentManager fragmentManager = m_ctx.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, new TimeTableFragment());
        transaction.addToBackStack(null);
        transaction.commit();
//        fillWithTTable(m_listTTable);
    }
    @Override
    protected void onPreExecute()
    {
        FragmentManager fragmentManager = m_ctx.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, new TimeTableFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        pb = new ProgressDialog(m_ctx.getActivity());
        pb.setMessage("downloading ...");
        pb.show();

//        fillWithTTable(m_listTTable);
    }

    public void fillWithTTable(ArrayAdapter<String> aa)
    {
        int                     d, t, ind;
        ArrayList<Lesson>       listLessons = StaticStorage.m_listLessons;
        String                  strDayNames[] = new String[6];

        aa.clear();
        ind = 0;
        for (d = 0; d < 6; d++)
        {
            aa.add( strDayNames[d] );
            for (t = 0; t < 5; t++, ind++)
            {
                Lesson lesson;

                lesson = listLessons.get(ind);
                if (lesson.m_subject.length() < 1)
                {
                    int hr = 8 + t * 2;
                    String strTimeS = String.valueOf(hr) + ":00";
                    if (hr == 8)
                        strTimeS = "0" + strTimeS;
                    aa.add(strTimeS);
                    continue;
                }
                StringBuilder bld = new StringBuilder();
                bld.append(lesson.m_timeStart);
                bld.append("-");
                bld.append(lesson.m_timeEnd);
                bld.append(", ");
                bld.append(lesson.m_subject);
                bld.append("(");
                bld.append(lesson.m_teacherFio);
                bld.append("), ");
                bld.append(lesson.m_roomName);
                bld.append(" / ");
                bld.append(lesson.m_buildingName);
                String strPrint = bld.toString();
                aa.add(strPrint);

            }   // for (t)
        }       // for (d)

    }

}

