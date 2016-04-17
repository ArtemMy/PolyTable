package edu.amd.spbstu.polystudenttimetable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Lecturer implements Serializable
{
    @SerializedName("Lecturer Info")
    public LecturerInfo m_info;
    @SerializedName("Lessons")
    public ArrayList<Lesson> m_listLessons;

    public Lecturer()
    {
        m_info = new LecturerInfo();
        m_listLessons = new ArrayList<Lesson>();
        m_listLessons.clear();
    }
}
