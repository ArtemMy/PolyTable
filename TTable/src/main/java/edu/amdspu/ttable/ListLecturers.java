package edu.amdspu.ttable;


import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListLecturers
{
    public ArrayAdapter<String>     m_adapterLecturers;
    public ListView                 m_list;
    private ActivityMain            m_ctx;

    public ListLecturers(ActivityMain ctx, ListView lv)
    {
        m_ctx = ctx;
        ArrayList<String> arrList = new ArrayList<String>();
        arrList.add(ctx.getString(R.string.str_downloading) );

        m_list = lv;
        m_adapterLecturers = new ArrayAdapter<String>(ctx, R.layout.layout_text_black , arrList);
        m_list.setAdapter(m_adapterLecturers );
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
                                  {
                                      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                      {
                                          StaticStorage.m_indexLecturer = position;
                                          Log.d("TT", "User clicked on " + String.valueOf(position));
                                          m_ctx.startDownloadTimetableForLecturer();
                                      }
                                  }

        );
    }
    public void setTurnOnInternet()
    {
        m_adapterLecturers.clear();
        m_adapterLecturers.add( m_ctx.getString(R.string.str_turn_on_internet));
    }

    public void fillWithLecturers()
    {
        int                     i, n;
        ArrayList<Lecturer>     listLecturers = StaticStorage.m_listLecturers;

        m_adapterLecturers.clear();
        n = listLecturers.size();
        for (i = 0; i < n; i++)
        {
            Lecturer lecturer = listLecturers.get(i);
            m_adapterLecturers.add( lecturer.m_fio  );
        }
    }

}