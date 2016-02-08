package edu.amdspu.ttable;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by vlad.shubnikov on 14.09.2015.
 */
public class ServerGetLectTable extends AsyncTask<String, String, String>
{
    private ListLectTable       m_listTTable;

    public ServerGetLectTable(ListLectTable l)
    {
        m_listTTable = l;
    }

    protected String doInBackground(String... strUrl)
    {
        URL url;
        try
        {
            Log.d("TT", "Start read web from " + strUrl[0]);
            url = new URL(strUrl[0]);
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
            Log.d("TT", "Exception doInBackground for ServerGetLectTable!");
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
        String                  strGroupName;
        String                  strRoomName;
        String                  strBldName;
        Lesson                  lesson;
        ArrayList<Lesson>       listLessons;
        int                     weekDayIndex;

        listLessons = StaticStorage.m_listLessons;

        // init lessons table with empty lessons
        for (int i = 0; i < 5*6; i++)
        {
            lesson = new Lesson();
            listLessons.set(i, lesson);
        }

        try
        {
            JSONObject objRoot      = new JSONObject(strJson);
            JSONObject objWeek      = (JSONObject)objRoot.get("week");
            JSONArray arrDays       = (JSONArray)objRoot.get("days");
            JSONObject objTeacher   = (JSONObject)objRoot.get("teacher");

            String strWeekStart = (String)objWeek.get("date_start");
            String strWeekEnd   = (String)objWeek.get("date_end");
            boolean isOdd       = (boolean)objWeek.get("is_odd");

            strGroupName= ""; strRoomName = ""; strBldName = "";
            int numDays = arrDays.length();
            for (int d = 0; d < numDays; d++)
            {
                JSONObject dayWeek = (JSONObject) arrDays.getJSONObject(d);
                weekDayIndex = (int)dayWeek.get("weekday");
                JSONArray arrLessons = (JSONArray) dayWeek.get("lessons");
                int numLes = arrLessons.length();
                for (int i = 0; i < numLes; i++)
                {
                    JSONObject objLesson = (JSONObject) arrLessons.getJSONObject(i);
                    strSubject = (String) objLesson.get("subject");
                    lessonType = (int) objLesson.get("type");
                    strTimeStart = (String) objLesson.get("time_start");
                    strTimeEnd = (String) objLesson.get("time_end");
                    strGroupName= "";
                    JSONArray arrGroups = (JSONArray) objLesson.get("groups");
                    int numGroups = arrGroups.length();
                    for (int t = 0; t < numGroups; t++)
                    {
                        JSONObject objGroup = (JSONObject) arrGroups.getJSONObject(t);
                        if (strGroupName.length() > 1)
                            strGroupName += ",";
                        strGroupName += (String)objGroup.get("name");
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
                    lesson.m_groupName = strGroupName;
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
                    int idx = (weekDayIndex - 1) * 5 + lesson.m_hour;
                    listLessons.set(idx, lesson);

                }   // for (i) num lessons in day
            }       // for (d) num days in week

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
        m_listTTable.fillWithTTable();
    }


}
