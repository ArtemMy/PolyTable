package edu.amd.spbstu.polystudenttimetable;

import android.os.Parcel;
import android.os.Parcelable;

import com.wefika.calendar.manager.Week;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lesson implements Serializable
{
    public Map<Integer, List<RegLessonInstance> > m_reg;

    public List<Group> m_list_groups;
    public String      m_subject;
    public Lecturer    m_teacher;

    public Lesson()
    {
        m_subject       = "";
        m_teacher    = new Lecturer();
        m_reg = new HashMap<Integer, List<RegLessonInstance> >();
        m_list_groups = new ArrayList<Group>();
    }

    @Override
    public boolean equals(Object sl){
        return (m_subject.equals(((Lesson)sl).m_subject) && m_teacher.m_fio.equals(((Lesson)sl).m_teacher.m_fio));
    }
    @Override
    public int hashCode() {
        return (this.m_subject + this.m_teacher.m_fio).hashCode();
    }
    public void add(int d, int lessonType, String strTimeStart, String strTimeEnd, String strRoomName, String strBldName) {
        RegLessonInstance reg = new RegLessonInstance(this);
        reg.m_day = d;
        reg.m_type = lessonType;
        reg.m_timeStart = strTimeStart;
        reg.m_timeEnd = strTimeEnd;
        reg.m_roomName = strRoomName;
        reg.m_buildingName = strBldName;
        List <RegLessonInstance> l;
        if(m_reg.containsKey(d))
            l = m_reg.get(d);
        else
            l = new ArrayList<RegLessonInstance>();
        l.add(reg);
        m_reg.put(d, l);
    }

    public List<RegLessonInstance> getLessonInstances(Integer dayOfWeek) {
        return m_reg.get(dayOfWeek);
    }
    public List<RegLessonInstance> getAllLessonInstances() {
        List<RegLessonInstance> res = new ArrayList<>();
        for (List<RegLessonInstance> list : m_reg.values()) {
            res.addAll(list);
        }
        return res;
    }
/*
    public Lesson(Parcel in){
        String[] data_s = new String[7];
        in.readStringArray(data_s);
        this.m_subject = data_s[0];
        this.m_teacherFio = data_s[3];

        int[] data_i = new int[3];
        in.readIntArray(data_i);

        boolean[] data_b = new boolean[3];
        in.readBooleanArray(data_b);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.m_subject,
                this.m_teacherFio});
        dest.writeInt(m_reg.size());
        for(Map.Entry<String,String> entry : m_reg.entrySet()){
            dest.writeString(m_reg.getKey());
            dest.writeString(m_reg.getValue());
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Lesson createFromParcel(Parcel in) {
            return new Lesson(in);
        }

        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };
    */
}

