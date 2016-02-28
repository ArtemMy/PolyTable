package edu.amd.spbstu.polystudenttimetable;

import java.io.Serializable;
import java.util.ArrayList;

public class Lecturer implements Serializable
{
    public String   m_chair;
    public String   m_fio;
    public int      m_id;
    public ArrayList<Lesson> m_listLessons = new ArrayList<Lesson>();

    public Lecturer()
    {
        m_chair     = "";
        m_id        = -1;
        m_fio       = "";
        m_listLessons.clear();
    }
}
