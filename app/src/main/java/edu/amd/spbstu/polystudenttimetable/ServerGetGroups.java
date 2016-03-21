package edu.amd.spbstu.polystudenttimetable;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ServerGetGroups extends AsyncTask<Faculty, String, String>
{
    private ArrayAdapter<String>  m_adapterGroups;
    private static String url_beg = "http://ruz2.spbstu.ru/api/v1/ruz/faculties/";
    private static String url_end = "/groups";
    Faculty search_faculty;

    public ServerGetGroups(ArrayAdapter<String> ag)
    {
        m_adapterGroups = ag;
    }
    protected String doInBackground(Faculty ... faculty)
    {
        try
        {
            search_faculty = faculty[0];
            String strUrl = url_beg + String.valueOf(faculty[0].m_id) + url_end;
            URL url;
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
    private ArrayList<GroupInfo> parseJson(String strJson)
    {
        ArrayList<GroupInfo>       listGroups = new ArrayList<GroupInfo>();
        GroupInfo                  group;

        try
        {
            JSONObject objRoot = new JSONObject(strJson);
            JSONArray arrGroups = (JSONArray)objRoot.get("groups");
            int numGroups = arrGroups.length();
            Log.d("TT", "Num groups = " + String.valueOf(numGroups));
            for (int i = 0; i < numGroups; i++)
            {
                JSONObject objGr;

                objGr = arrGroups.optJSONObject(i);
                group = new GroupInfo();
                group.m_name    = (String)objGr.get("name");
                group.m_id      = (int)objGr.get("id");
                group.m_level   = (int)objGr.get("level");
                group.m_spec_number    = "";
                group.m_spec    = "";
                if(!((String)objGr.get("spec")).equals("")) {
                    String arr[] = ((String)objGr.get("spec")).split(" ", 2);
                    group.m_spec_number    = arr[0];
                    if(arr.length > 1)
                        group.m_spec    = arr[1];
                }
                group.m_faculty = (Faculty) search_faculty;
                listGroups.add(group);
            }
        }
        catch (Exception ex)
        {
            Log.d("TT", "Exception !");
            Log.d("TT", ex.getMessage());
            ex.printStackTrace();
        }
        Comparator<GroupInfo> compGroups = new Comparator<GroupInfo>() {
            @Override
            public int compare(GroupInfo lhs, GroupInfo rhs)
            {
                if (lhs.m_level < rhs.m_level)
                    return -1;
                if (lhs.m_level > rhs.m_level)
                    return +1;
                // save level: compare name
                int cmp = lhs.m_name.compareTo(rhs.m_name);
                return cmp;
            }
        };
        Collections.sort(listGroups, compGroups);
//        for (Group mygroup : listGroups) {
//            StaticStorage.m_listGroupsName.add(String.valueOf(mygroup.m_name));
//        }
        return listGroups;
    }

    @Override
    protected void onPostExecute(String strResult)
    {
        Log.d("TT", "Read completed with" + strResult.substring(0, 128));
        //m_textView.setText(strResult);
        ArrayList<GroupInfo> listGroups = parseJson(strResult);

        ArrayList<Faculty>      listFaculties = StaticStorage.m_listFaculties;
        int                     i, n;

        n = listGroups.size();
        for (i = 0; i < n; i++)
        {
            GroupInfo grp = listGroups.get(i);
            StaticStorage.m_listGroups.add(grp);
            m_adapterGroups.add(grp.m_name);
            if(grp.m_spec.contains("Прикладная математика")) {
                StaticStorage.m_primatGroups.add(grp);
                StaticStorage.m_primatGroupsName.add(grp.m_name);
            }
        }
        m_adapterGroups.notifyDataSetChanged();
    }
}

