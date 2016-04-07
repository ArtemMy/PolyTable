package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
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
public class MyHelpFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private static final String TAG = "polytable_log";

    private AbsListView mListView;

    private ListAdapter mAdapter;

    private View view;
    private ExpandableListView lv;
    private Activity mAct;
    private String[] titles;
    private String[] texts;
    private int[] pics = {
            R.drawable.s1,
            R.drawable.s3,
            R.drawable.s2,
            R.drawable.s4,
            R.drawable.s5,
            R.drawable.s6,
            R.drawable.s7};
    public static MyHelpFragment newInstance(MainNavigationDrawer act) {

        MyHelpFragment fragment = new MyHelpFragment(act);

        return fragment;
    }
    public MyHelpFragment(MainNavigationDrawer act)
    {
        this.mAct = act;
        titles = mAct.getResources().getStringArray(R.array.help_titles);
        texts = mAct.getResources().getStringArray(R.array.help_texts);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<Lesson> mLessonList;



        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.help));
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
            return titles.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return titles[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return texts[groupPosition];
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
                convertView = in_inflater.inflate(R.layout.help_inst, null);
            } else {
            }
            TextView textChild = (TextView) convertView.findViewById(R.id.help_text);
            textChild.setText(texts[groupPosition]);
            ImageView imageChild = (ImageView) convertView.findViewById(R.id.help_pic);
            imageChild.setImageDrawable(getResources().getDrawable(pics[groupPosition]));
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
            tv.setText(titles[groupPosition]);

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
