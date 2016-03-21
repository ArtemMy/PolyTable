package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                    if (hw.getKey().isBefore(LocalDate.now()))
                        continue;
                    int pos = dates.indexOf(hw.getKey());
                    if (pos > 0)
                        hws.get(pos).add(hw.getValue());
                    else {
                        ArrayList<RegLessonInstance.Homework> l = new ArrayList<RegLessonInstance.Homework>();
                        l.add(hw.getValue());
                        hws.add(l);
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
        // Inflate the layout for this fragment
//        if(view == null)
        Log.d("init", "search fragment onCreateView");
        if (view == null)
            view = inflater.inflate(R.layout.homework_list, container, false);
        lv = (ExpandableListView) view.findViewById(R.id.homework_exp_list);
        lv.setAdapter(new ExpandableListAdapter());
        lv.setGroupIndicator(null);
        TextView empty = new TextView(getActivity());
        empty.setText(getResources().getString(R.string.homework_list_empty));
        lv.setEmptyView(empty);
        Log.d(TAG, "homework list created view");

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
                convertView = inf.inflate(R.layout.hw_inst, null);
                holder = new ViewHolder();

                holder.text = (TextView) convertView.findViewById(R.id.hw_title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(((RegLessonInstance.Homework)getChild(groupPosition, childPosition)).m_task);

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
            tv.setText(getGroup(groupPosition).toString());
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
