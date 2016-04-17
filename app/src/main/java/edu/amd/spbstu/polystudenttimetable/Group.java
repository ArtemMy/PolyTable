package edu.amd.spbstu.polystudenttimetable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable
{
    @SerializedName("Group Info")
    public GroupInfo m_info;
    @SerializedName("Group Lessons")
    public ArrayList<Lesson> m_listLessons;

    public Group()
    {
        m_info = new GroupInfo();
        m_listLessons = new ArrayList<Lesson>();
        m_listLessons.clear();
    }
}
