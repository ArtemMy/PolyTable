package edu.amd.spbstu.polystudenttimetable;

import com.google.android.gms.drive.DriveId;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupInfo implements Serializable
{
    public String   m_name;
    public int      m_id;
    public int      m_level;
    public String   m_spec_number;
    public String   m_spec;
    public Faculty  m_faculty;
    public ArrayList<String> m_listLessonsId;

    public GroupInfo()
    {
        m_name          = "";
        m_id            = -1;
        m_level         = -1;
        m_spec          = "";
        m_spec_number   = "";
        m_faculty = new Faculty();
        m_listLessonsId = new ArrayList<String>();
        m_listLessonsId.clear();
    }
}
