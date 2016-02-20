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
    private ArrayList<Lesson> parseJson(String strJson) {
        Group group;
        String strSubject;
        int lessonType;
        String strTimeStart, strTimeEnd;
        String strTeacherFIO;
        String strRoomName;
        String strBldName;
        Lesson lesson;
        ArrayList<Lesson> listLessons = new ArrayList<Lesson>();

        Log.d("init", "init lessons");
        try {
            JSONObject objRoot = new JSONObject(strJson);
            JSONObject objWeek = (JSONObject) objRoot.get("week");
            JSONArray arrDays = (JSONArray) objRoot.get("days");
            JSONObject objGroup = (JSONObject) objRoot.get("group");

            String strWeekStart = (String) objWeek.get("date_start");
            String strWeekEnd = (String) objWeek.get("date_end");
            boolean isOdd = (boolean) objWeek.get("is_odd");

            strTeacherFIO = "";
            strRoomName = "";
            strBldName = "";
            for (int d = 0; d < 6; d++) {
                JSONObject dayWeek = (JSONObject) arrDays.getJSONObject(d);
                JSONArray arrLessons = (JSONArray) dayWeek.get("lessons");
                int numLes = arrLessons.length();
                for (int i = 0; i < numLes; i++) {
                    JSONObject objLesson = (JSONObject) arrLessons.getJSONObject(i);
                    strSubject = (String) objLesson.get("subject");
                    lessonType = (int) objLesson.get("type");
                    strTimeStart = (String) objLesson.get("time_start");
                    strTimeEnd = (String) objLesson.get("time_end");
                    strTeacherFIO = "";
                    JSONArray arrTeachers = (JSONArray) objLesson.get("teachers");
                    int numTeachers = arrTeachers.length();
                    for (int t = 0; t < numTeachers; t++) {
                        JSONObject objTeacher = (JSONObject) arrTeachers.getJSONObject(t);
                        strTeacherFIO = (String) objTeacher.get("full_name");
                    }
                    strRoomName = "";
                    strBldName = "";
                    JSONArray arrRooms = (JSONArray) objLesson.get("auditories");
                    int numRooms = arrRooms.length();
                    for (int r = 0; r < numRooms; r++) {
                        JSONObject objRoom = (JSONObject) arrRooms.getJSONObject(r);
                        strRoomName = (String) objRoom.get("name");
                        JSONObject objBuilding = (JSONObject) objRoom.get("building");
                        strBldName = (String) objBuilding.get("abbr");
                    } // for (r) rooms
                    lesson = new Lesson();
                    Log.d("init s", "add lesson");
                    lesson.m_subject = strSubject;
                    lesson.m_teacherFio = strTeacherFIO;
                    if (listLessons.contains(lesson)) {
                        int ind = listLessons.indexOf(lesson);
                        lesson = listLessons.get(ind);
                        lesson.add(d, lessonType, strTimeStart, strTimeEnd, strRoomName, strBldName);
                        listLessons.set(ind, lesson);
                    } else {
                        lesson.add(d, lessonType, strTimeStart, strTimeEnd, strRoomName, strBldName);
                        listLessons.add(lesson);
                    }
                }   // for (i) num lessons in day
            }       // for (d) num days in week
        } catch (Exception ex) {
            Log.d("TT", "Exception JSON parse timetable !");
            Log.d("TT", ex.getMessage());
            ex.printStackTrace();
        }
        return listLessons;
    }

    @Override
    protected void onPostExecute(String strResult)
    {
        Log.d("TT", "Read completed with" + strResult.substring(0, 128));
        ArrayList<Lesson> listLesson = parseJson(strResult);
        pb.dismiss();
        FragmentManager fragmentManager = m_ctx.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, TimeTableFragment.newInstance(listLesson));
        transaction.addToBackStack(null);
        transaction.commit();
//        fillWithTTable(m_listTTable);
    }
    @Override
    protected void onPreExecute()
    {
        /*
        FragmentManager fragmentManager = m_ctx.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, new TimeTableFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        */
        pb = new ProgressDialog(m_ctx.getActivity());
        pb.setMessage("downloading ...");
        pb.show();
    }

}

