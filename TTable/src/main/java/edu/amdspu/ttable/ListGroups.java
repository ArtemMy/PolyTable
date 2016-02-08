package edu.amdspu.ttable;


import android.app.Activity;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class ListGroups
{
    public ArrayAdapter<String>     m_adapterGroups;
    private Activity                m_ctx;

    public ListGroups(Activity ctx)
    {
        m_ctx = ctx;
        ArrayList<String> arrList = new ArrayList<String>();
        m_adapterGroups = new ArrayAdapter<String>(ctx, R.layout.layout_text_black , arrList);
    }
    public void fillWithGroups()
    {
        int                     i, n;
        ArrayList<Group>        listGroups = StaticStorage.m_listGroups;

        m_adapterGroups.clear();
        n = listGroups.size();
        for (i = 0; i < n; i++)
        {
            Group group = listGroups.get(i);
            m_adapterGroups.add(group.m_name);
        }
    }

}