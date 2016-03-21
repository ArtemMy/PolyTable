package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerGetPrimatLecturers extends AsyncTask<String, String, String>
{
    private Activity mAct;
    ArrayList<String>     listLecturers = new ArrayList<String>();
    private ProgressDialog pb;

    public ServerGetPrimatLecturers(Activity act)
    {
        mAct = act;
    }
    protected String doInBackground(String... strUrl)
    {
        try
        {
            URL url = new URL("http://amd.stu.neva.ru/lecturers");
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
        if(isCancelled()) {
            pb.dismiss();
            return;
        }

        parseHtmlLecturers(strResult);
        AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
        builder.setTitle(mAct.getResources().getString(R.string.str_depth));
        builder.setIcon(R.drawable.logo_amd_mod);
//                        ListView modeList = new ListView(getActivity());
        final ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(mAct,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                listLecturers);
        builder.setAdapter(modeAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str = listLecturers.get(which);
                        new ServerGetLecturers(mAct).execute(str);
                    }
                });
        pb.dismiss();
        final Dialog dialog = builder.create();
        dialog.show();

    }

    private void parseHtmlLecturers(String strHtml)
    {
        Pattern                 pLecturer;
        Matcher                 match;
        int                     numPeople, i;

        pLecturer = Pattern.compile("person_id[^\\x3e]+\\x3e([^\\x3c]+)\\x3c", Pattern.MULTILINE);
        match = pLecturer.matcher(strHtml);
        while (match.find())
        {
            String strPerson = match.group(1);
            //Log.d("TT", "Person = " + strPerson);
            strPerson = strPerson.replaceAll("&nbsp;", " ");
            listLecturers.add(strPerson);
        }
        Collections.sort(listLecturers);
    }
    @Override
    protected void onPreExecute()
    {
        if(!((MainNavigationDrawer)mAct).isOnline()) {
            ((MainNavigationDrawer) mAct).askForInternet();
            cancel(true);
            return;
        }

        pb = new ProgressDialog(mAct);
        pb.setMessage(mAct.getResources().getString(R.string.placeholder_downloading));

        pb.setCancelable(true);
        pb.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // actually could set running = false; right here, but I'll
                // stick to contract.
                cancel(true);
            }
        });
        pb.show();
    }
}
