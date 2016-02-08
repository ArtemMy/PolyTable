package edu.amdspu.ttable;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by vlad.shubnikov on 14.09.2015.
 */
public class ServerGetLectId extends AsyncTask<String, String, String>
{
    private ListLectTable   m_listTTable;
    private int             m_lecturerId;

    public ServerGetLectId(ListLectTable l)
    {
        m_listTTable = l;
        m_lecturerId = -1;
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
            Log.d("TT", "Exception !");
            Log.d("TT", ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
    protected void onPostExecute(String strResult)
    {
        if (strResult == null)
        {
            m_listTTable.notFoundLecturer();
            return;
        }
        Log.d("TT", "Read completed with" + strResult.substring(0, 128));
        parseJsonLecturerId(strResult);
        //m_listTTable.fillWithTTable();
        if (m_lecturerId > 1)
            m_listTTable.startDownloadLectorTable(m_lecturerId);
    }
    private void parseJsonLecturerId(String strJson)
    {
        try
        {
            JSONObject  objRoot = new JSONObject(strJson);
            JSONArray   arrTeachers = (JSONArray) objRoot.get("teachers");
            int numLecturers = arrTeachers.length();
            if (numLecturers != 1)
            {
                //
                m_listTTable.notFoundLecturer();
            }
            else
            {
                JSONObject  objLec = arrTeachers.optJSONObject(0);
                m_lecturerId = (int)objLec.get("id");
                Log.d("TT", "Found lecturer id = " + m_lecturerId);

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
