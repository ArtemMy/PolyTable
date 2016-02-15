package edu.amd.spbstu.polystudenttimetable;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by artem on 11/28/15.
 */
public class SingleClassView extends CardView{

    private TextView mTime1, mTime2, mWhere, mName;
    private ImageView mHomework, mCanceled, mImportant;
    private boolean mIsCanceled, mIsImportant, mIsHomework;
    private Lesson mLesson;

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
        mIsHomework = lesson.m_isHomework;
        mIsImportant = lesson.m_isImportant;
        mIsCanceled = lesson.m_isCanceled;

        mHomework = (ImageView) findViewById(R.id.homework);
        mCanceled = (ImageView) findViewById(R.id.canceled);
        mImportant = (ImageView) findViewById(R.id.important);

        mLesson = lesson;

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
                class_type = " (практика)";
                break;
            case 1:
                class_type = " (лабораторные)";
                break;
            case 2:
                class_type = " (теория)";
                break;
        }

        mName.setText(lesson.m_subject + class_type);

        mImportant.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("init", "click");
                setImportant(!getImportant());
            }
        });
        mHomework.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("init", "click");
                setHomework(!getHomework());
            }
        });
        this.setClickable(true);
        this.setOnClickListener(new CardView.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("init", "it clicked!");
                        /*
                        FragmentManager fragmentManager = getActivity().getFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.container, DetailedClassFragment.newInstance(((SingleClassView)view).getLesson()));
                        transaction.addToBackStack(null);
                        transaction.commit();
                        */
            }
        });

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

    Lesson getLesson () {
        return mLesson;
    }

    void setCanceled (boolean c) {
        mIsCanceled = c;
        mCanceled.setVisibility(mIsCanceled ? VISIBLE : GONE);
    }
}
