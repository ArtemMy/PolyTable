package edu.amd.spbstu.polystudenttimetable;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class ServerGetLecturers extends AsyncTask<String, String, String>
{
    private ArrayAdapter<String> adapter;

    public ServerGetLecturers(ArrayAdapter<String> l)
    {
        adapter = l;
    }
    protected String doInBackground(String ... str)
    {
        Log.d("init", (String)str[0]);
        String strUrl = "http://ruz2.spbstu.ru/api/v1/ruz/search/teachers?q=" + str[0];
        Log.d("init", "Start read web from " + strUrl);
        URL url;
        try
        {
            url = new URL(strUrl);
            Log.d("TT", "Start read web from " + strUrl);
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
    protected void onPostExecute(String strResult)
    {
        if (strResult == null)
            return;
        parseJsonLecturerId(strResult);
    }
    private void parseJsonLecturerId(String strJson)
    {
        try
        {
            JSONObject objRoot = new JSONObject(strJson);
            JSONArray arrTeachers = (JSONArray) objRoot.get("teachers");
            int numLecturers = arrTeachers.length();
            adapter.clear();
            StaticStorage.m_listLecturers.clear();
            if (arrTeachers != null) {
                int len = arrTeachers.length();
                for (int i = 0; i < len; i++){
                    Lecturer lect = new Lecturer();
                    lect.m_chair = (String)arrTeachers.optJSONObject(i).get("chair");
                    lect.m_fio = (String)arrTeachers.optJSONObject(i).get("full_name");
                    lect.m_id = (int)arrTeachers.optJSONObject(i).get("id");
                    adapter.add(lect.m_fio);
                    StaticStorage.m_listLecturers.add(lect);
                }
                adapter.notifyDataSetChanged();
            }
        }
        catch (Exception ex)
        {
            Log.d("TT", "Exception !");
            Log.d("TT", ex.getMessage());
            ex.printStackTrace();
        }

    }

}
