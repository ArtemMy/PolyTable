package edu.amd.spbstu.polystudenttimetable;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable
{
    public String   m_name;
    public int      m_id;
    public int      m_level;
    public String   m_spec_number;
    public String   m_spec;
    public Faculty  m_faculty;
    public ArrayList<Lesson> m_listLessons = new ArrayList<Lesson>();

    public Group()
    {
        m_name      = "";
        m_id        = -1;
        m_level     = -1;
        m_spec      = "";
        m_listLessons.clear();
    }
}
