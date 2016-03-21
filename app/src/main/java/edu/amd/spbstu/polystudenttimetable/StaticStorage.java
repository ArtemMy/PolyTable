package edu.amd.spbstu.polystudenttimetable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class StaticStorage implements Serializable
{
    public static Map<Integer, Group>  m_recentGroups = new LinkedHashMap<Integer, Group>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, Group> eldest)
        {
            return this.size() > 3;
        }
    };
    public static Map<Integer, Lecturer>  m_recentLecturers = new LinkedHashMap<Integer, Lecturer>()  {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, Lecturer> eldest)
        {
            return this.size() > 3;
        }
    };


    public static boolean m_isInitialized = false;
    public static ArrayList<Faculty>    m_listFaculties = new ArrayList<Faculty>();

    public static ArrayList<GroupInfo>  m_listGroups = new ArrayList<GroupInfo>();
    public static ArrayList<String>     m_listGroupsName = new ArrayList<String>();

    public static ArrayList<LecturerInfo>   m_listLecturers = new ArrayList<LecturerInfo>();
    public static ArrayList<String>     m_listLecturerName = new ArrayList<String>();

    public static ArrayList<GroupInfo>  m_primatGroups = new ArrayList<GroupInfo>();
    public static ArrayList<String>     m_primatGroupsName = new ArrayList<String>();

    public StaticStorage()
    {
        clear();
    }
    public static void clear()
    {
        m_listFaculties.clear();
        m_listGroups.clear();
        m_listGroupsName.clear();
        m_listLecturers.clear();
        m_primatGroups.clear();
        m_primatGroupsName.clear();
    }

}
