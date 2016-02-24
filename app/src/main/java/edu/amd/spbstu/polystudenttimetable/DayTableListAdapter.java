package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by artem on 2/16/16.
 */
public class DayTableListAdapter extends BaseAdapter
        implements View.OnCreateContextMenuListener{
    List<RegLessonInstance> list;
    LocalDate week;
    int position;
    RegLessonInstance currentLesson;
    Activity act;
    LayoutInflater lInflater;
    ListView listView;

    DayTableListAdapter(Activity activity, List<RegLessonInstance> list, LocalDate day, ListView listView) {
        this.act = activity;
        this.list = list;
        this.week = day;
        this.lInflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listView = listView;
    }
    @Override
    public int getCount() {
        return list.size();
    }
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.singleclass, parent, false);
        }
        TextView mTime1, mTime2, mWhere, mType, mName;
        ImageView mHomework, mCanceled, mImportant;
        CardView mCardView;
        RegLessonInstance mLesson;

        mTime1 = (TextView) view.findViewById(R.id.time1);
        mTime2 = (TextView) view.findViewById(R.id.time2);
        mWhere = (TextView) view.findViewById(R.id.where);
        mType = (TextView) view.findViewById(R.id.type);
        mName = (TextView) view.findViewById(R.id.class_name);

        mHomework = (ImageView) view.findViewById(R.id.homework);
        mImportant = (ImageView) view.findViewById(R.id.important);
        mCanceled = (ImageView) view.findViewById(R.id.canceled);

        mCardView = (CardView) view.findViewById(R.id.card_view);

        RegLessonInstance lesson = list.get(position);

        mLesson = lesson;

        mTime1.setText(lesson.m_timeStart);
        mTime2.setText(lesson.m_timeEnd);

        mWhere.setText(lesson.m_buildingName + ", " + lesson.m_roomName);
        String class_type = mName.getContext().getResources().getStringArray(R.array.lesson_type)[lesson.m_type];

        mType.setText(class_type);
        mName.setText(lesson.parent.m_subject);

        mCanceled.setVisibility(lesson.m_isCanceled.containsKey(week) ? View.VISIBLE : View.GONE);
        mHomework.setAlpha(lesson.m_isHomework.containsKey(week) ? 1.0f : 0.15f);
        mImportant.setAlpha(lesson.m_isImportant.containsKey(week) ? 1.0f : 0.15f);

        mCardView.setOnCreateContextMenuListener(this);
        mHomework.setOnClickListener(mOnHomeworkClickListener);
        mImportant.setOnClickListener(mOnImportantClickListener);
        mCardView.setOnClickListener(mOnCardViewClickListener);

        return view;
    }
    private View.OnClickListener mOnHomeworkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = listView.getPositionForView((View) v.getParent());
            Log.d("init", String.valueOf(position));
        }
    };

    private View.OnClickListener mOnImportantClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = listView.getPositionForView((View) v.getParent());
            Log.d("init", String.valueOf(position));
        }
    };
    private CardView.OnClickListener mOnCardViewClickListener = new CardView.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = listView.getPositionForView((View) v.getParent());
            DetailedClassFragment mFragment = DetailedClassFragment.newInstance(list.get(position).parent);
            MainNavigationDrawer mainActivity = (MainNavigationDrawer)v.getContext();
            mainActivity.switchContent(mFragment);

            Log.d("init", String.valueOf(position));
        }
    };
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
    }

}