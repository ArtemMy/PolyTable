package edu.amd.spbstu.polystudenttimetable;

import android.os.Parcel;
import android.os.Parcelable;

public class SingleLesson implements Parcelable
{
    private Lesson parent;

    public int      m_day;  // 0, 1, 2, 3, 4, 5
    public int      m_type;  // 0, 1, 2

    public String   m_timeStart;
    public String   m_timeEnd;

    public String   m_subject;
    public String   m_teacherFio;

    public String   m_roomName;
    public String   m_buildingName;

    public boolean m_isCanceled;
    public boolean m_isImportant;
    public boolean m_isHomework;
    public SingleLesson(Lesson lesson)
    {
        parent = lesson;

        m_day           = -1;
        m_type          = -1;
        m_subject       = "";
        m_timeStart     = "";
        m_timeEnd       = "";
        m_roomName      = "";
        m_buildingName  = "";
        m_isCanceled = false;
        m_isImportant = false;
        m_isHomework = false;
    }

    public SingleLesson(Parcel in){
        String[] data_s = new String[7];
        in.readStringArray(data_s);
        this.m_subject = data_s[0];
        this.m_timeStart = data_s[1];
        this.m_timeEnd = data_s[2];
        this.m_roomName = data_s[5];
        this.m_buildingName = data_s[6];

        int[] data_i = new int[3];
        in.readIntArray(data_i);
        this.m_day = data_i[0];
        this.m_type = data_i[1];

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
                this.m_roomName,
                this.m_buildingName});
        dest.writeIntArray(new int[]{this.m_day,
                this.m_type});
        dest.writeBooleanArray(new boolean[]{this.m_isCanceled,
                this.m_isImportant,
                this.m_isHomework});
    }

    public static final Creator CREATOR = new Creator() {
        public SingleLesson createFromParcel(Parcel in) {
            return new SingleLesson(in);
        }

        public SingleLesson[] newArray(int size) {
            return new SingleLesson[size];
        }
    };
}

