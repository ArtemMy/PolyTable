package edu.amd.spbstu.polystudenttimetable;

public class Lesson
{
    public int      m_day;  // 0, 1, 2, 3, 4, 5
    public String   m_subject;
    public int      m_type;  // 0, 1, 2
    public String   m_timeStart;
    public String   m_timeEnd;
    public int      m_hour;     // 0 for 8:00, 1 for 10:00,

    public String   m_teacherFio;
    public String   m_groupName;

    public String   m_roomName;
    public String   m_buildingName;

    public boolean m_isCanceled;
    public boolean m_isImportant;
    public boolean m_isHomework;
    public Lesson()
    {
        m_day           = -1;
        m_type          = -1;
        m_hour          = -1;
        m_subject       = "";
        m_timeStart     = "";
        m_timeEnd       = "";
        m_teacherFio    = "";
        m_groupName     = "";
        m_roomName      = "";
        m_buildingName  = "";
        m_isCanceled = false;
        m_isImportant = false;
        m_isHomework = false;
    }
}

