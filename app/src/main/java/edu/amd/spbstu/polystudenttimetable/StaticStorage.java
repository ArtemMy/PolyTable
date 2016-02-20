package edu.amd.spbstu.polystudenttimetable;

import java.io.Serializable;
import java.util.ArrayList;

public class StaticStorage implements Serializable
{
    public static boolean m_isInitialized = false;
    public static ArrayList<Faculty>    m_listFaculties = new ArrayList<Faculty>();

    public static ArrayList<Group>      m_listGroups = new ArrayList<Group>();
    public static ArrayList<String>     m_listGroupsName = new ArrayList<String>();

    public static ArrayList<Lesson>      m_listLessons = new ArrayList<Lesson>();

    public static ArrayList<Lecturer>   m_listLecturers = new ArrayList<Lecturer>();

    public StaticStorage()
    {
        clear();
    }
    public static void clear()
    {
        m_listFaculties.clear();
        m_listGroups.clear();
        m_listGroupsName.clear();
        m_listLessons.clear();
        m_listLecturers.clear();
    }

}
