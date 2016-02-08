package edu.amd.spbstu.polystudenttimetable;

import java.io.Serializable;
import java.util.ArrayList;

public class StaticStorage implements Serializable
{
    public static boolean m_isInitialized = false;
    public static ArrayList<Faculty>    m_listFaculties = new ArrayList<Faculty>();
    public static int                   m_indexFaculty;

    public static ArrayList<Group>      m_listGroups = new ArrayList<Group>();
    public static ArrayList<String>     m_listGroupsName = new ArrayList<String>();
    public static int                   m_indexGroup;

    public static ArrayList<Lesson>      m_listLessons = new ArrayList<Lesson>();

    public static ArrayList<Lecturer>   m_listLecturers = new ArrayList<Lecturer>();
    public static int                   m_indexLecturer;


    public StaticStorage()
    {
        clear();
    }
    public static void clear()
    {
        m_listFaculties.clear();
        m_indexFaculty = -1;
        m_listGroups.clear();
        m_listGroupsName.clear();
        m_indexGroup = -1;
        m_listLessons.clear();
        for (int i = 0; i < 30; i++)
        {
            Lesson les = new Lesson();
            m_listLessons.add(les);
        }
        m_listLecturers.clear();
        m_indexLecturer = -1;
        m_isInitialized = false;
    }

}
