package edu.amd.spbstu.polystudenttimetable;

import com.google.android.gms.drive.DriveId;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupInfo implements Serializable
{
    @SerializedName("Group Name")
    public String   m_name;
    @SerializedName("Group Id")
    public int      m_id;
    @SerializedName("Group Level")
    public int      m_level;
    @SerializedName("Group Specialty Number")
    public String   m_spec_number;
    @SerializedName("Group Specialty")
    public String   m_spec;
    @SerializedName("Group Faculty")
    public Faculty  m_faculty;
    @SerializedName("List of Lessons")
    public ArrayList<String> m_listLessonsId;
    @SerializedName("Contact Info")
    public ContactInfo m_contact;

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
        m_contact = new ContactInfo();
    }
    @Override
    public boolean equals(Object sl) {
        return (m_id == (((GroupInfo) sl).m_id) || m_name.equals(((GroupInfo) sl).m_name));
    }

    @Override
    public int hashCode() {
        return (this.m_name).hashCode();
    }

}
