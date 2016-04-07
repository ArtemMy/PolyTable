package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MyHomeworkFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private static final String TAG = "polytable_log";

    private AbsListView mListView;

    private ListAdapter mAdapter;

    private View view;
    private ExpandableListView lv;
    private ArrayList<LocalDate> dates;
    private ArrayList<ArrayList<RegLessonInstance.Homework> > hws;

    public static MyHomeworkFragment newInstance() {

        MyHomeworkFragment fragment = new MyHomeworkFragment();

        return fragment;
    }
    public MyHomeworkFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<Lesson> mLessonList;
        if (((MainNavigationDrawer)getActivity()).isGroup()) {
            mLessonList  = ((Group)(((MainNavigationDrawer)getActivity()).the_obj)).m_listLessons;
        } else {
            mLessonList = ((Lecturer)(((MainNavigationDrawer)getActivity()).the_obj)).m_listLessons;
        }
        hws = new ArrayList<ArrayList<RegLessonInstance.Homework>>();
        dates = new ArrayList<LocalDate>();
        for(Lesson lesson : mLessonList) {
            for(RegLessonInstance lessonInst : lesson.getAllLessonInstances()) {
                for(Map.Entry<LocalDate, RegLessonInstance.Homework> hw: lessonInst.m_homework.entrySet()) {
                    if (hw.getKey().isBefore(LocalDate.now())) {
                        continue;
                    }
                    int pos = dates.indexOf(hw.getKey());
                    if (pos >= 0)
                        hws.get(pos).add(hw.getValue());
                    else {
                        ArrayList<RegLessonInstance.Homework> l = new ArrayList<RegLessonInstance.Homework>();
                        l.add(hw.getValue());
                        dates.add(hw.getKey());
                        Collections.sort(dates);
                        hws.add(dates.indexOf(hw.getKey()), l);
                    }
                }
            }
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.homework));
        Log.d(TAG, String.valueOf(dates.size()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the help for this fragment
//        if(view == null)
        Log.d("init", "search fragment onCreateView");
        if (view == null)
            view = inflater.inflate(R.layout.homework_list, container, false);
        lv = (ExpandableListView) view.findViewById(R.id.homework_exp_list);
        lv.setAdapter(new ExpandableListAdapter());
        lv.setGroupIndicator(null);
        lv.setEmptyView(view.findViewById(R.id.hw_empty_class_view));
        Log.d(TAG, "homework list created view");
        lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
                final RegLessonInstance.Homework hmwrk = hws.get(groupPosition).get(childPosition);

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                final EditText edittext = new EditText(getActivity());

                edittext.setHint(R.string.homework_hint);

                edittext.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable e) {
/*
                    if (e.length() == 1 || (e.length() != 0 && e.charAt(e.length() - 1) == '\n')) {
                        e.append(" ");
                        e.setSpan(new BulletSpan(30), e.length() - 1, e.length(),
                                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                    */
                        BulletSpan toRemoveSpans[] = e.getSpans(0, e.length(), BulletSpan.class);
                        for (int i = 0; i < toRemoveSpans.length; i++)
                            e.removeSpan(toRemoveSpans[i]);
                        String[] lines = TextUtils.split(e.toString(), "\n");
                        Log.d(TAG, String.valueOf(lines.length));
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
                            } else {
                                e.setSpan(new BulletSpan(30), length, length + 1,
                                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                            }
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { /* no action */ }

                    @Override
                    public void onTextChanged(CharSequence val, int arg1, int arg2, int arg3) {
                    }
                });
                edittext.setText(hmwrk.m_task);

                alert.setTitle(R.string.homework);

                alert.setView(edittext);
                alert.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        hmwrk.m_task = edittext.getText().toString();

                        ((MainNavigationDrawer)getActivity()).write(hmwrk.m_lesson.parent);
//                        new WriteFile(getActivity(), hmwrk.m_lesson.parent).execute();
                        lv.setAdapter(new ExpandableListAdapter());
                    }
                });

                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
                return true;
            }
        });
        return view;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private final LayoutInflater inf;

        public ExpandableListAdapter() {
//            inf = LayoutInflater.from(getActivity());
            inf = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getGroupCount() {
            return dates.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return hws.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return dates.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return hws.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            Log.d(TAG, "howdy");
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater in_inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = in_inflater.inflate(R.layout.hw_inst, null);
            } else {
            }
            TextView textChild = (TextView) convertView.findViewById(R.id.hw_title);

            Spannable formatedText = new SpannableString(((RegLessonInstance.Homework) getChild(groupPosition, childPosition)).m_task);
            String[] lines = TextUtils.split(((RegLessonInstance.Homework) getChild(groupPosition, childPosition)).m_task, "\n");
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
            textChild.setText(formatedText);

            textChild = (TextView) convertView.findViewById(R.id.hw_lesson);
            textChild.setText(((RegLessonInstance.Homework) getChild(groupPosition, childPosition)).m_lesson.parent.m_subject);

            return convertView;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            Log.d(TAG, "hola");
            ViewHolder holder;

            if (convertView == null) {
                convertView = inf.inflate(R.layout.day_group_hw, parent, false);

//                holder = new ViewHolder();
//                holder.text = (TextView) convertView.findViewById(R.id.day_hw);
//                convertView.setTag(holder);
            } else {
//                holder = (ViewHolder) convertView.getTag();
            }

//            holder.text.setText(getGroup(groupPosition).toString());
            TextView tv = (TextView) convertView.findViewById(R.id.day_hw);
            if (((LocalDate)getGroup(groupPosition)).getDayOfYear() - LocalDate.now().getDayOfYear() < 1) {
                tv.setTextColor(getResources().getColor(R.color.colorAccent));
                tv.setText(getResources().getString(R.string.upcoming));
            } else {
                tv.setTextColor(getResources().getColor(R.color.text_normal));
                tv.setText(((LocalDate) getGroup(groupPosition)).toString("dd.MM", Locale.getDefault()));
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private class ViewHolder {
            TextView text;
        }
    }
}
