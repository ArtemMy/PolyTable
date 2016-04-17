package edu.amd.spbstu.polystudenttimetable;

import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by artem on 2/17/16.
 */
public class RegLessonInstance implements Serializable{

    public enum lesson_type {
        PRACTISE,
        LECTURE,
        LAB
    }
    public enum weekly_t {
        ALL,
        EVEN,
        ODD
    }

    Homework newHomework() {
        return new Homework(this);
    }

    public static class Homework implements Serializable{

        public Homework() {
            m_task = "";
            this.m_lesson = null;
        }

        public Homework(RegLessonInstance lesson) {
            m_task = "";
            this.m_lesson = lesson;
        }

        @SerializedName("Task")
        public String m_task;
        @SerializedName("Parent Lesson")
        public transient RegLessonInstance m_lesson;
    }
    @SerializedName("Parent")
    public transient Lesson parent;

    @SerializedName("Day Number")
    public int      m_day;  // 0, 1, 2, 3, 4, 5
    @SerializedName("Type")
    public int      m_type;  // 0, 1, 2
    @SerializedName("Start Time")
    public String   m_timeStart;
    @SerializedName("End Time")
    public String   m_timeEnd;
    @SerializedName("Room Name")
    public String   m_roomName;
    @SerializedName("Building Name")
    public String   m_buildingName;
    @SerializedName("Weekly")
    public weekly_t m_weekly;

    @SerializedName("Canceled instances")
    public Map<LocalDate, Boolean> m_isCanceled;
    @SerializedName("Important instances")
    public Map<LocalDate, Boolean> m_isImportant;
    @SerializedName("Homeworks")
    public Map<LocalDate, Homework> m_homework;

    public RegLessonInstance(Lesson lesson)
    {
        parent = lesson;
        m_day = -1;  // 0, 1, 2, 3, 4, 5
        m_type = -1;  // 0, 1, 2
        m_timeStart = "";
        m_timeEnd= "";
        m_roomName = "";
        m_buildingName = "";
        m_isCanceled = new HashMap<LocalDate, Boolean>();
        m_isImportant = new HashMap<LocalDate, Boolean>();
        m_homework = new HashMap<LocalDate, Homework>();
        m_weekly = weekly_t.ALL;
    }
}
