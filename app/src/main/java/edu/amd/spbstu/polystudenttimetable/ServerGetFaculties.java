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

public class ServerGetFaculties extends AsyncTask<String, String, String>
{
    private ArrayAdapter<String> m_adapterGroups;

    public ServerGetFaculties(ArrayAdapter<String> arrayAdapter)
    {
        m_adapterGroups = arrayAdapter;
    }
    protected String doInBackground(String ... s)
    {
        URL url;
        try
        {
            String str_url = "http://ruz2.spbstu.ru/api/v1/ruz/faculties/";
            Log.d("TT", "Start read web from " + str_url);
            url = new URL(str_url);
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
        ArrayList<Faculty>      listFaculties = StaticStorage.m_listFaculties;
        Faculty                 faculty;

        listFaculties.clear();
        try
        {
            JSONObject objRoot = new JSONObject(strJson);
            JSONArray arrFac = (JSONArray)objRoot.get("faculties");
            int numFac = arrFac.length();
            Log.d("TT", "Num faculties = " + String.valueOf(numFac));
            for (int i = 0; i < numFac; i++)
            {
                JSONObject objFac;

                objFac = arrFac.optJSONObject(i);
                faculty = new Faculty();
                faculty.m_name  = (String)objFac.get("name");
                faculty.m_id    = (int)objFac.get("id");
                faculty.m_abbr  = (String)objFac.get("abbr");
                listFaculties.add(faculty);
            }

        }
        catch (Exception ex)
        {
            Log.d("TT", "Exception !");
            Log.d("TT", ex.getMessage());
            ex.printStackTrace();
        }

    }

    @Override
    protected void onPostExecute(String strResult)
    {
        //Log.d("TT", "Read completed with" + strResult.substring(0, 128));
        //m_textView.setText(strResult);
        parseJson(strResult);

        ArrayList<Faculty>      listFaculties = StaticStorage.m_listFaculties;
        //StringBuilder           strBuilder = new StringBuilder();
        for (int i = 0; i < listFaculties.size(); i++)
        {
            Faculty f = listFaculties.get(i);
            ServerGetGroups serverGetGroups = new ServerGetGroups(m_adapterGroups);
            serverGetGroups.execute(f);
            //strBuilder.append("name=");
            //strBuilder.append(f.m_name);
            //strBuilder.append("(");
            //strBuilder.append(f.m_id);
            //strBuilder.append(")");
            //strBuilder.append(",");
        }
        //String strToPrint = strBuilder.toString();
    }
}

