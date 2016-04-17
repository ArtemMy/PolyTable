package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.BulletSpan;
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
    static final String PREF_LES_COL= "poly_table_lesson_color";
    RegLessonInstance currentLesson;
    Activity act;
    LayoutInflater lInflater;
    ListView listView;
    boolean isMy;
    boolean isGroup;

    DayTableListAdapter(Activity activity, List<RegLessonInstance> list, LocalDate day, ListView listView, boolean isMy, boolean isGroup) {
        this.act = activity;
        this.list = list;
        this.week = day;
        this.lInflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listView = listView;
        this.isMy = isMy;
        this.isGroup = isGroup;
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
        TextView mTime1, mTime2, mWhere, mType, mName, mSubLine;
        ImageView mHomework, mCanceled, mImportant;
        CardView mCardView;
        RegLessonInstance mLesson;

        mTime1 = (TextView) view.findViewById(R.id.time1);
        mTime2 = (TextView) view.findViewById(R.id.time2);
        mWhere = (TextView) view.findViewById(R.id.where);
        mType = (TextView) view.findViewById(R.id.type);
        mName = (TextView) view.findViewById(R.id.class_name);
        mSubLine = (TextView) view.findViewById(R.id.sub_line);

        mHomework = (ImageView) view.findViewById(R.id.homework);
        if(!isMy)
            mHomework.setVisibility(View.GONE);
        mImportant = (ImageView) view.findViewById(R.id.important);
        if(!isMy)
            mImportant.setVisibility(View.GONE);
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

        if(!isGroup) {
            String str = "";
            for(int i = 0; i != lesson.parent.m_list_groups.size(); ++i) {
                str += lesson.parent.m_list_groups.get(i).m_name;
                if(i != lesson.parent.m_list_groups.size() - 1)
                    str += ", ";
            }
            mSubLine.setText(str);
        }
        else {
            if (lesson.parent.m_teacher.m_fio != act.getResources().getString(R.string.not_set))
                mSubLine.setText(lesson.parent.m_teacher.m_fio);
            else
                mSubLine.setText("");
        }
        int col = act.getSharedPreferences(act.getPackageName(), Context.MODE_PRIVATE).getInt(PREF_LES_COL+String.valueOf(lesson.parent.hashCode()), 0);
        mCardView.setCardBackgroundColor(act.getResources().getColor(StaticStorage.lesColor[col]));
        mCardView.setCardElevation(10);
        mCardView.setRadius(15);

        mCanceled.setVisibility(lesson.m_isCanceled.containsKey(week) ? View.VISIBLE : View.GONE);
        mHomework.setAlpha(lesson.m_homework.containsKey(week) ? 1.0f : 0.15f);
        mImportant.setAlpha(lesson.m_isImportant.containsKey(week) ? 1.0f : 0.15f);

        mCardView.setOnCreateContextMenuListener(this);
        mHomework.setOnClickListener(mOnHomeworkClickListener);
//        mImportant.setOnClickListener(mOnImportantClickListener);
        mCardView.setOnClickListener(mOnCardViewClickListener);

        return view;
    }
    private View.OnClickListener mOnHomeworkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = listView.getPositionForView((View) v.getParent());
            if(!list.get(position).m_homework.containsKey(week))
                return;

            Spannable formatedText = new SpannableString(list.get(position).m_homework.get(week).m_task);
            String[] lines = TextUtils.split(list.get(position).m_homework.get(week).m_task, "\n");
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            String line = null;
            for (int index = 0; index < lines.length; ++index) {
                line = lines[index];
                int length = spannableStringBuilder.length();
                spannableStringBuilder.append(line);
                if (index != lines.length - 1) {
                    spannableStringBuilder.append("\n");
                }
                if (TextUtils.isEmpty(line)) {
//                                spannableStringBuilder.append("\n");
                }
                else {
                    formatedText.setSpan(new BulletSpan(30), length, length + 1,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            new AlertDialog.Builder(act)
                    .setTitle(act.getResources().getString(R.string.homework))
                    .setMessage(formatedText)
                    .setPositiveButton(android.R.string.ok, null) // dismisses by default
                    .create()
                    .show();
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
            if(isMy) {
                MyDetailedClassFragment mFragment = MyDetailedClassFragment.newInstance(list.get(position).parent);
                MainNavigationDrawer mainActivity = (MainNavigationDrawer) v.getContext();
                mainActivity.switchContent(mFragment);
            } else {
                DetailedClassFragment mFragment = DetailedClassFragment.newInstance(list.get(position).parent);
                MainNavigationDrawer mainActivity = (MainNavigationDrawer) v.getContext();
                mainActivity.switchContent(mFragment);
            }
            Log.d("init", String.valueOf(position));
        }
    };
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
    }

}