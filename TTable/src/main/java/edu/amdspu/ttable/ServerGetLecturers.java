package edu.amdspu.ttable;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vlad.shubnikov on 14.09.2015.
 */
public class ServerGetLecturers extends AsyncTask<String, String, String>
{
    private ListLecturers   m_listLec;

    public ServerGetLecturers(ListLecturers ll)
    {
        m_listLec = ll;
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
        parseHtmlLecturers(strResult);
        m_listLec.fillWithLecturers();
    }

    private void parseHtmlLecturers(String strHtml)
    {
        ArrayList<Lecturer>     listLecturers = StaticStorage.m_listLecturers;
        Lecturer                lecturer;
        Pattern                 pLecturer;
        Matcher                 match;
        int                     numPeople, i;

        pLecturer = Pattern.compile("person_id[^\\x3e]+\\x3e([^\\x3c]+)\\x3c", Pattern.MULTILINE);
        match = pLecturer.matcher(strHtml);
        while (match.find())
        {
            String strPerson = match.group(1);
            //Log.d("TT", "Person = " + strPerson);
            lecturer = new Lecturer();
            strPerson = strPerson.replaceAll("&nbsp;", " ");
            lecturer.m_fio = strPerson;
            listLecturers.add(lecturer);
        }
        Collections.sort(listLecturers, new Comparator<Lecturer>()
                {
                    public int compare(Lecturer lhs, Lecturer rhs)
                    {
                        int cmp = lhs.m_fio.compareTo(rhs.m_fio);
                        return cmp;
                    }
                }
        );
    }

}
