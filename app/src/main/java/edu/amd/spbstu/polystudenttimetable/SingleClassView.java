package edu.amd.spbstu.polystudenttimetable;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by artem on 11/28/15.
 */
public class SingleClassView extends CardView{

    private TextView mTime1, mTime2, mWhere, mName;
    private ImageView mHomework, mCanceled, mImportant;
    private boolean mIsCanceled, mIsImportant, mIsHomework;
    public SingleClassView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        inflate(context, R.layout.singleclass, this);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.singleclass, this, true);
    }

    public SingleClassView(Context context) {
        this(context, null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("init", "onFinishInflate");
        mTime1 = (TextView) findViewById(R.id.time1);
        mTime2 = (TextView) findViewById(R.id.time2);
        mName =  (TextView) findViewById(R.id.class_name);
        mWhere = (TextView) findViewById(R.id.where);
    }

    public void init(Lesson lesson) {
        mTime1 = (TextView) findViewById(R.id.time1);
        mTime2 = (TextView) findViewById(R.id.time2);
        mName =  (TextView) findViewById(R.id.class_name);
        mWhere = (TextView) findViewById(R.id.where);
        mHomework = (ImageView) findViewById(R.id.homework);
        mCanceled = (ImageView) findViewById(R.id.canceled);
        mImportant = (ImageView) findViewById(R.id.important);

        Log.d("init", lesson.m_timeStart + lesson.m_timeEnd + lesson.m_buildingName + ", ауд. " + lesson.m_roomName + String.valueOf(lesson.m_type) + ": " + lesson.m_subject);

        mCanceled.setVisibility(mIsCanceled ? VISIBLE : GONE);
        mHomework.setAlpha(mIsHomework ? 1.0f : 0.15f);
        mImportant.setAlpha(mIsImportant ? 1.0f : 0.15f);
        mTime1.setText(lesson.m_timeStart);
        mTime2.setText(lesson.m_timeEnd);

        if (!lesson.m_roomName.isEmpty())
            mWhere.setText(lesson.m_buildingName + ", " + lesson.m_roomName);
        String class_type = "";
        switch(lesson.m_type)
        {
            case 0:
                class_type = "Практика ";
                break;
            case 1:
                class_type = "Лабораторные: ";
                break;
            case 2:
                class_type = "Теория: ";
                break;
        }

        mName.setText(class_type + lesson.m_subject);
    }

    boolean getHomework () {
        return mIsHomework;
    }

    void setHomework (boolean hw) {
        mIsHomework = hw;
        mHomework.setAlpha(mIsHomework ? 1.0f : 0.15f);
    }

    boolean getImportant () {
        return mIsImportant;
    }

    void setImportant (boolean im) {
        mIsImportant = im;
        mImportant.setAlpha(mIsImportant ? 1.0f : 0.15f);
    }

    boolean getCanceled () {
        return mIsCanceled;
    }

    void setCanceled (boolean c) {
        mIsCanceled = c;
        mCanceled.setVisibility(mIsCanceled ? VISIBLE : GONE);
    }
}
