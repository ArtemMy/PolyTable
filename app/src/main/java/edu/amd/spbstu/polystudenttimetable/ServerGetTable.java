package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
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
public class ServerGetTable extends AsyncTask<Void, String, String>
{
    private FragmentManager fm;
    private Activity mAct;
    private ProgressDialog pb;
    private Group m_group;
    private Lecturer m_lect;

    private boolean isGroup;

    public ServerGetTable(Group group, FragmentManager fm, Activity act)
    {
        m_group = group;
        this.fm = fm;
        mAct = act;
        isGroup = true;
    }
    public ServerGetTable(Lecturer lecturer, FragmentManager fm, Activity act)
    {
        this.fm = fm;
        mAct = act;
        m_lect = lecturer;
        isGroup = false;
    }
    protected String doInBackground(Void... v)
    {
        String strUrl;
        if(isGroup)
            strUrl = "http://ruz2.spbstu.ru/api/v1/ruz/scheduler/" + String.valueOf(m_group.m_id);
        else
            strUrl = "http://ruz2.spbstu.ru/api/v1/ruz/teachers/" + m_lect.m_id + "/scheduler";
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

            for (int d = 0; d < arrDays.length(); d++) {
                JSONObject dayWeek = (JSONObject) arrDays.getJSONObject(d);
                int dayInt = (int) dayWeek.get("weekday") - 1;
                JSONArray arrLessons = (JSONArray) dayWeek.get("lessons");
                int numLes = arrLessons.length();
                for (int i = 0; i < numLes; i++) {
                    JSONObject objLesson = (JSONObject) arrLessons.getJSONObject(i);
                    strSubject = (String) objLesson.get("subject");
                    lessonType = (int) objLesson.get("type");
                    strTimeStart = (String) objLesson.get("time_start");
                    strTimeEnd = (String) objLesson.get("time_end");

                    Lecturer lect = new Lecturer();
                    if(!objLesson.getString("teachers").equals("null")) {
                        JSONArray arrTeachers = objLesson.getJSONArray("teachers");
                        int numTeachers = arrTeachers.length();
                        for (int t = 0; t < numTeachers; t++) {
                            JSONObject objTeacher = (JSONObject) arrTeachers.getJSONObject(t);
                            lect.m_id = (int) objTeacher.get("id");
                            lect.m_fio = (String) objTeacher.get("full_name");
                            lect.m_chair = (String) objTeacher.get("chair");
                        }
                    }
                    strRoomName = "";
                    strBldName = "";
                    if(!objLesson.getString("auditories").equals("null")) {
                        JSONArray arrRooms = (JSONArray) objLesson.get("auditories");
                        int numRooms = arrRooms.length();
                        for (int r = 0; r < numRooms; r++) {
                            JSONObject objRoom = (JSONObject) arrRooms.getJSONObject(r);
                            strRoomName = (String) objRoom.get("name");
                            JSONObject objBuilding = (JSONObject) objRoom.get("building");
                            strBldName = (String) objBuilding.get("abbr");
                        } // for (r) rooms
                    }
                    lesson = new Lesson();

                    if(!objLesson.getString("groups").equals("null")) {
                        JSONArray arrGroups = (JSONArray) objLesson.get("groups");
                        int numGroups = arrGroups.length();
                        for (int t = 0; t < numGroups; t++) {
                            JSONObject objGroup = (JSONObject) arrGroups.getJSONObject(t);
                            Group g = new Group();
                            g.m_name = (String) objGroup.get("name");
                            g.m_id = (int) objGroup.get("id");
                            String arr[] = ((String)objGroup.get("spec")).split(" ", 2);
                            g.m_spec_number = arr[0];
                            g.m_spec    = arr[1];
                            lesson.m_list_groups.add(g);
                        }
                    }
                    Log.d("init s", "add lesson");
                    lesson.m_subject = strSubject;
                    lesson.m_teacher = lect;
                    if (listLessons.contains(lesson)) {
                        int ind = listLessons.indexOf(lesson);
                        lesson = listLessons.get(ind);
                        lesson.add(dayInt, lessonType, strTimeStart, strTimeEnd, strRoomName, strBldName);
                        listLessons.set(ind, lesson);
                    } else {
                        lesson.add(dayInt, lessonType, strTimeStart, strTimeEnd, strRoomName, strBldName);
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
        FragmentTransaction transaction = fm.beginTransaction();
        if(isGroup) {
            m_group.m_listLessons = listLesson;
            transaction.replace(R.id.container, TimeTableFragment.newInstance(m_group));
            StaticStorage.m_recentGroups.add(m_group);
        }
        else {
            m_lect.m_listLessons = listLesson;
            transaction.replace(R.id.container, TimeTableFragment.newInstance(m_lect));
            StaticStorage.m_recentLecturers.add(m_lect);
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }
    @Override
    protected void onPreExecute()
    {
        pb = new ProgressDialog(mAct);
        pb.setMessage("downloading ...");
        pb.show();
    }

}

