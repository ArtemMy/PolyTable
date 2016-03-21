package edu.amd.spbstu.polystudenttimetable;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable
{
    public GroupInfo m_info;
    public ArrayList<Lesson> m_listLessons;

    public Group()
    {
        m_info = new GroupInfo();
        m_listLessons = new ArrayList<Lesson>();
        m_listLessons.clear();
    }
}
