package edu.amd.spbstu.polystudenttimetable;

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

    public Lesson parent;

    public int      m_day;  // 0, 1, 2, 3, 4, 5
    public int      m_type;  // 0, 1, 2
    public String   m_timeStart;
    public String   m_timeEnd;
    public String   m_roomName;
    public String   m_buildingName;

    public Map<LocalDate, Boolean> m_isCanceled;
    public Map<LocalDate, Boolean> m_isImportant;
    public Map<LocalDate, String> m_isHomework;

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
        m_isHomework = new HashMap<LocalDate, String>();
    }
}
