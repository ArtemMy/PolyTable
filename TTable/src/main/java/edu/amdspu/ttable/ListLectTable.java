package edu.amdspu.ttable;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by vlad.shubnikov on 14.09.2015.
 */
public class ListLectTable
{
    public ArrayAdapter<String>     m_adapterTTable;
    public ListView                 m_list;
    private ActivityMain            m_ctx;

    public ListLectTable(ActivityMain ctx, ListView lv)
    {
        m_ctx = ctx;
        ArrayList<String> arrList = new ArrayList<String>();
        arrList.add(ctx.getString(R.string.str_downloading) );

        m_list = lv;
        m_adapterTTable = new ArrayAdapter<String>(ctx, R.layout.layout_text_black , arrList);
        m_list.setAdapter(m_adapterTTable);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
                                  {
                                      @Override
                                      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                      {
                                          //StaticStorage.m_indexGroup = position;
                                          Log.d("TT", "User clicked on " + String.valueOf(position));
                                          //m_ctx.startDownloadGroupTimetable();
                                      }
                                  }

        );
    }
    public void notFoundLecturer()
    {
        m_adapterTTable.clear();
        m_adapterTTable.add(m_ctx.getString(R.string.str_lecturer_not_found));
    }
    public void startDownloadLectorTable(int lecturerId)
    {
        m_ctx.startDownloadTTForLecturer(lecturerId);
    }

    public void fillWithTTable()
    {
        int                     d, t, ind;
        ArrayList<Lesson>       listLessons = StaticStorage.m_listLessons;
        String                  strDayNames[] = new String[6];

        strDayNames[0] = m_ctx.getString(R.string.str_day_monday);
        strDayNames[1] = m_ctx.getString(R.string.str_day_tuesday);
        strDayNames[2] = m_ctx.getString(R.string.str_day_wednesday);
        strDayNames[3] = m_ctx.getString(R.string.str_day_thursday);
        strDayNames[4] = m_ctx.getString(R.string.str_day_friday);
        strDayNames[5] = m_ctx.getString(R.string.str_day_saturday);

        m_adapterTTable.clear();
        ind = 0;
        for (d = 0; d < 6; d++)
        {
            m_adapterTTable.add( strDayNames[d] );
            for (t = 0; t < 5; t++, ind++)
            {
                Lesson lesson;

                lesson = listLessons.get(ind);
                if (lesson.m_subject.length() < 1)
                {
                    int hr = 8 + t * 2;
                    String strTimeS = String.valueOf(hr) + ":00";
                    if (hr == 8)
                        strTimeS = "0" + strTimeS;
                    m_adapterTTable.add(strTimeS);
                    continue;
                }
                StringBuilder bld = new StringBuilder();
                bld.append(lesson.m_timeStart);
                bld.append("-");
                bld.append(lesson.m_timeEnd);
                bld.append(", ");
                bld.append(lesson.m_subject);
                bld.append("(");
                //bld.append(lesson.m_teacherFio);
                bld.append(lesson.m_groupName);
                bld.append("), ");
                bld.append(lesson.m_roomName);
                bld.append(" / ");
                bld.append(lesson.m_buildingName);
                String strPrint = bld.toString();
                m_adapterTTable.add(strPrint);

            }   // for (t)
        }       // for (d)

    }

}
