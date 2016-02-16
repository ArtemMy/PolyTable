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

import java.util.List;

/**
 * Created by artem on 2/16/16.
 */
public class DayTableAdapter extends RecyclerView.Adapter<DayTableAdapter.MyViewHolder> {
    List<Lesson> list;
    public DayTableAdapter(List<Lesson> list){
        this.list=list;
    }

    public MyViewHolder getCustomHolder(View v) {
        return new MyViewHolder(v){
            @Override
            public void onClick(View v) {
                int item = this.getAdapterPosition();
                Lesson lesson = list.get(item);
                switch(v.getId()){
                    case R.id.homework:
                        Log.d("init", "mHomework");
                        lesson.m_isHomework = !lesson.m_isHomework;
                        v.setAlpha(lesson.m_isHomework ? 1.0f : 0.15f);
                        break;
                    case R.id.important:
                        Log.d("init", "mImportant");
                        lesson.m_isImportant = !lesson.m_isImportant;
                        v.setAlpha(lesson.m_isImportant ? 1.0f : 0.15f);
                        break;
                    case R.id.card_view:
                        Log.d("init", "detailed view");

                        DetailedClassFragment mFragment = DetailedClassFragment.newInstance(lesson);
                        MainNavigationDrawer mainActivity = (MainNavigationDrawer)v.getContext();
                        Log.d("init", v.getContext().toString());
                        mainActivity.switchContent(mFragment);
                        break;
                }
                list.set(item, lesson);
            }
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                int item = this.getAdapterPosition();
                Lesson lesson = list.get(item);

                menu.setHeaderTitle(R.string.lesson_menu_title);
                menu.add(0, v.getId(), 0, R.string.lesson_menu_imp);//groupId, itemId, order, title
                menu.add(0, v.getId(), 0, R.string.lesson_menu_hw);
                menu.add(0, v.getId(), 0, R.string.lesson_menu_canc);
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
        Lesson lesson = list.get(i);

        vh.mLesson = lesson;

        vh.mTime1.setText(lesson.m_timeStart);
        vh.mTime2.setText(lesson.m_timeEnd);

        if (!lesson.m_roomName.isEmpty())
            vh.mWhere.setText(lesson.m_buildingName + ", " + lesson.m_roomName);
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

        vh.mName.setText(lesson.m_subject + class_type);

        vh.mCanceled.setVisibility(lesson.m_isCanceled ? View.VISIBLE : View.GONE);
        vh.mHomework.setAlpha(lesson.m_isHomework ? 1.0f : 0.15f);
        vh.mImportant.setAlpha(lesson.m_isImportant ? 1.0f : 0.15f);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener, View.OnCreateContextMenuListener {
        public TextView mTime1, mTime2, mWhere, mName;
        public ImageView mHomework, mCanceled, mImportant;
        public CardView mCardView;
        public Lesson mLesson;

        MyViewHolder(View view){
            super(view);

            this.mTime1 = (TextView) view.findViewById(R.id.time1);
            this.mTime2 = (TextView) view.findViewById(R.id.time2);
            this.mWhere = (TextView) view.findViewById(R.id.where);
            this.mName = (TextView) view.findViewById(R.id.class_name);

            this.mHomework = (ImageView) view.findViewById(R.id.homework);
            this.mImportant = (ImageView) view.findViewById(R.id.important);
            this.mCanceled = (ImageView) view.findViewById(R.id.canceled);

            this.mCardView = (CardView) view.findViewById(R.id.card_view);

            mHomework.setOnClickListener(this);
            mImportant.setOnClickListener(this);
            mCardView.setOnClickListener(this);
//            mCardView.setOnLongClickListener(this);
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