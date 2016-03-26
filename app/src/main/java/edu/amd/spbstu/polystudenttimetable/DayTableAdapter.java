package edu.amd.spbstu.polystudenttimetable;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by artem on 2/16/16.
 */
public class DayTableAdapter extends RecyclerView.Adapter<DayTableAdapter.MyViewHolder> {
    List<RegLessonInstance> list;
    LocalDate week;
    int position;
    RegLessonInstance currentLesson;
    private boolean isMy;

    public DayTableAdapter(List<RegLessonInstance> list, LocalDate week, RegLessonInstance currentLesson, boolean isMy){
        this.list = list;
        this.week = week;
        this.position = 0;
        this.currentLesson = currentLesson;
        this.isMy = isMy;
    }

    public MyViewHolder getCustomHolder(View v) {
        return new MyViewHolder(v){
            @Override
            public void onClick(View v) {
                int item = this.getAdapterPosition();
                RegLessonInstance lesson = list.get(item);
                switch(v.getId()){
                    case R.id.homework:
                        Log.d("init", "mHomework");
                        break;
                    case R.id.important:
                        Log.d("init", "mImportant");
                        break;
                    case R.id.card_view:
                        if(isMy) {
                            DetailedClassFragment mFragment = DetailedClassFragment.newInstance(lesson.parent);
                            MainNavigationDrawer mainActivity = (MainNavigationDrawer) v.getContext();
                            Log.d("init", v.getContext().toString());
                            mainActivity.switchContent(mFragment);
                        }
                        else {
                            MyDetailedClassFragment mFragment = MyDetailedClassFragment.newInstance(lesson.parent);
                            MainNavigationDrawer mainActivity = (MainNavigationDrawer)v.getContext();
                            Log.d("init", v.getContext().toString());
                            mainActivity.switchContent(mFragment);
                            break;
                        }

                        break;
                }
            }
            public boolean onLongClick(View v) {
                int item = this.getAdapterPosition();
                currentLesson = list.get(item);
                return true;
            }
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
/*
                menu.setHeaderTitle(R.string.lesson_menu_title);
                menu.add(0, v.getId(), 0, R.string.lesson_menu_imp);//groupId, itemId, order, title
                menu.add(0, v.getId(), 0, R.string.lesson_menu_hw);
                menu.add(0, v.getId(), 0, R.string.lesson_menu_canc);
                 */
            }
        };
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.singleclass, viewGroup, false);
        return getCustomHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder vh, int i) {
        RegLessonInstance lesson = list.get(i);

        vh.mLesson = lesson;

        vh.mTime1.setText(lesson.m_timeStart);
        vh.mTime2.setText(lesson.m_timeEnd);

        vh.mWhere.setText(lesson.m_buildingName + ", " + lesson.m_roomName);
        String class_type = vh.mName.getContext().getResources().getStringArray(R.array.lesson_type)[lesson.m_type];

        vh.mType.setText(class_type);
        vh.mName.setText(lesson.parent.m_subject);

        vh.mCanceled.setVisibility(lesson.m_isCanceled.containsKey(week) ? View.VISIBLE : View.GONE);
        vh.mHomework.setAlpha(lesson.m_homework.containsKey(week) ? 1.0f : 0.15f);
        vh.mImportant.setAlpha(lesson.m_isImportant.containsKey(week) ? 1.0f : 0.15f);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener,
                View.OnCreateContextMenuListener {
        public TextView mTime1, mTime2, mWhere, mType, mName;
        public ImageView mHomework, mCanceled, mImportant;
        public CardView mCardView;
        public RegLessonInstance mLesson;

        MyViewHolder(View view) {
            super(view);

            this.mTime1 = (TextView) view.findViewById(R.id.time1);
            this.mTime2 = (TextView) view.findViewById(R.id.time2);
            this.mWhere = (TextView) view.findViewById(R.id.where);
            this.mType = (TextView) view.findViewById(R.id.type);
            this.mName = (TextView) view.findViewById(R.id.class_name);

            this.mHomework = (ImageView) view.findViewById(R.id.homework);
            this.mImportant = (ImageView) view.findViewById(R.id.important);
            this.mCanceled = (ImageView) view.findViewById(R.id.canceled);

            this.mCardView = (CardView) view.findViewById(R.id.card_view);

            mHomework.setOnClickListener(this);
            mImportant.setOnClickListener(this);
            mCardView.setOnClickListener(this);
            mCardView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
        }

    }
}