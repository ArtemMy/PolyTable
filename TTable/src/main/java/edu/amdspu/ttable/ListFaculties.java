package edu.amdspu.ttable;


import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListFaculties
{
    public ArrayAdapter<String>     m_adapterFaculties;
    public ListView                 m_list;
    private ActivityMain            m_ctx;

    public ListFaculties(ActivityMain ctx, ListView lv)
    {
        m_ctx = ctx;
        ArrayList<String> arrList = new ArrayList<String>();
        arrList.add(ctx.getString(R.string.str_downloading) );

        m_list = lv;
        m_adapterFaculties = new ArrayAdapter<String>(ctx, R.layout.layout_text_black , arrList);
        m_list.setAdapter(m_adapterFaculties);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
                                  {
                                      @Override
                                      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                      {
                                          StaticStorage.m_indexFaculty = position;
                                          Log.d("TT", "User clicked on " + String.valueOf(position));
                                          m_ctx.startDownloadListGroups();
                                      }
                                  }

        );
    }
    public void setTurnOnInternet()
    {
        m_adapterFaculties.clear();
        m_adapterFaculties.add( m_ctx.getString(R.string.str_turn_on_internet));
    }

    public void fillWithFaculties()
    {
        int                     i, n;
        ArrayList<Faculty>      listFaculties = StaticStorage.m_listFaculties;

        m_adapterFaculties.clear();
        n = listFaculties.size();
        for (i = 0; i < n; i++)
        {
            Faculty faculty = listFaculties.get(i);
            m_adapterFaculties.add( faculty.m_name + "("  + faculty.m_abbr + ")"  );
        }
    }

}