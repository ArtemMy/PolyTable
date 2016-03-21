package edu.amd.spbstu.polystudenttimetable;

import java.io.Serializable;
import java.util.ArrayList;

public class Lecturer implements Serializable
{
    public LecturerInfo m_info;
    public ArrayList<Lesson> m_listLessons;

    public Lecturer()
    {
        m_info = new LecturerInfo();
        m_listLessons = new ArrayList<Lesson>();
        m_listLessons.clear();
    }
}
