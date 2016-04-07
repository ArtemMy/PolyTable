package edu.amd.spbstu.polystudenttimetable;

import com.google.android.gms.drive.DriveId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

public class LecturerInfo implements Serializable
{
    public String   m_chair;
    public String   m_fio;
    public int      m_id;
    public ArrayList<String> m_listLessonsId;

    public LecturerInfo()
    {
        m_chair     = "";
        m_id        = -1;
        m_fio       = Locale.getDefault().getDisplayLanguage().equals("English") ? "Not set" : "Не задан";
        m_listLessonsId = new ArrayList<String>();
        m_listLessonsId.clear();
    }
}
