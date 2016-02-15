package edu.amd.spbstu.polystudenttimetable;

import android.os.Parcel;
import android.os.Parcelable;

public class Lesson implements Parcelable
{
    public int      m_day;  // 0, 1, 2, 3, 4, 5
    public int      m_type;  // 0, 1, 2
    public int      m_hour;     // 0 for 8:00, 1 for 10:00,

    public String   m_subject;
    public String   m_timeStart;
    public String   m_timeEnd;

    public String   m_teacherFio;
    public String   m_groupName;

    public String   m_roomName;
    public String   m_buildingName;

    public boolean m_isCanceled;
    public boolean m_isImportant;
    public boolean m_isHomework;
    public Lesson()
    {
        m_day           = -1;
        m_type          = -1;
        m_hour          = -1;
        m_subject       = "";
        m_timeStart     = "";
        m_timeEnd       = "";
        m_teacherFio    = "";
        m_groupName     = "";
        m_roomName      = "";
        m_buildingName  = "";
        m_isCanceled = false;
        m_isImportant = false;
        m_isHomework = false;
    }

    public Lesson(Parcel in){
        String[] data_s = new String[7];
        in.readStringArray(data_s);
        this.m_subject = data_s[0];
        this.m_timeStart = data_s[1];
        this.m_timeEnd = data_s[2];
        this.m_teacherFio = data_s[3];
        this.m_groupName = data_s[4];
        this.m_roomName = data_s[5];
        this.m_buildingName = data_s[6];

        int[] data_i = new int[3];
        in.readIntArray(data_i);
        this.m_day = data_i[0];
        this.m_type = data_i[1];
        this.m_hour = data_i[2];

        boolean[] data_b = new boolean[3];
        in.readBooleanArray(data_b);
        this.m_isCanceled = data_b[0];
        this.m_isImportant = data_b[1];
        this.m_isHomework = data_b[2];

    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.m_subject,
                this.m_timeStart,
                this.m_timeEnd,
                this.m_teacherFio,
                this.m_groupName,
                this.m_roomName,
                this.m_buildingName});
        dest.writeIntArray(new int[]{this.m_day,
                this.m_type,
                this.m_hour});
        dest.writeBooleanArray(new boolean[]{this.m_isCanceled,
                this.m_isImportant,
                this.m_isHomework});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Lesson createFromParcel(Parcel in) {
            return new Lesson(in);
        }

        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };
}

