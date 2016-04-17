package edu.amd.spbstu.polystudenttimetable;

import com.google.android.gms.drive.DriveId;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

public class LecturerInfo implements Serializable
{
    @SerializedName("Chair")
    public String   m_chair;
    @SerializedName("Lecturer Name")
    public String   m_fio;
    @SerializedName("Lecturer Id")
    public int      m_id;
    @SerializedName("List of Lessons")
    public ArrayList<String> m_listLessonsId;
    @SerializedName("Contact Info")
    public ContactInfo m_contact;

    public LecturerInfo()
    {
        m_chair     = "";
        m_id        = -1;
        m_fio       = Locale.getDefault().getDisplayLanguage().equals("English") ? "Not set" : "Не задан";
//        m_fio       = Locale.getDefault().equals(Locale.ENGLISH)? "Не задан" : "Not set";
        m_listLessonsId = new ArrayList<String>();
        m_listLessonsId.clear();
        m_contact = new ContactInfo();
    }
}
