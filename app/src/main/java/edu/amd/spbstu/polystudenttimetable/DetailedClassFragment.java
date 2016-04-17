package edu.amd.spbstu.polystudenttimetable;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailedClassFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailedClassFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailedClassFragment extends Fragment {
    private static final String ARG_PARAM = "param1";

    // TODO: Rename and change types of parameters
    private Lesson mLesson;
    private ListView classesList = null;
    private ArrayAdapter<String> cAdapter = null;
    private ArrayList<String> classes = null;

    private ListView mGroupList;
    private ArrayAdapter mGroupListAdapter;
    private OnFragmentInteractionListener mListener;

    /**
     * @param param Lesson parameter
     * @return A new instance of fragment DetailedClassFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailedClassFragment newInstance(Lesson param) {
        DetailedClassFragment fragment = new DetailedClassFragment();
        Bundle args = new Bundle();
        args.putSerializable("parsedLesson", param);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailedClassFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLesson = (Lesson)getArguments().getSerializable("parsedLesson");
        }
        else if (savedInstanceState != null) {
            mLesson = (Lesson) savedInstanceState.getSerializable("parsedLesson");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the help for this fragment
        Log.d("init", mLesson.m_teacher.m_fio + String.valueOf(mLesson.m_teacher.m_id));

        View view = inflater.inflate(R.layout.fragment_detailed, container, false);

        TextView collapsingToolbar =
                (TextView)view.findViewById(R.id.detailed_title);
        collapsingToolbar.setText(mLesson.m_subject);
        LinearLayout detailed_class_list = (LinearLayout)view.findViewById(R.id.detailed_container);

        FloatingActionButton myFab = (FloatingActionButton) view.findViewById(R.id.edit_fab);
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) myFab.getLayoutParams();
        p.setBehavior(null); //should disable default animations
        p.setAnchorId(View.NO_ID); //should let you set visibility
        myFab.setLayoutParams(p);
        myFab.setVisibility(View.GONE); // View.INVISIBLE might also be worth trying

        String[] titles = getResources().getStringArray(R.array.class_details);

        for(int i = 0; i < titles.length; ++i) {
            View item;
            switch(i) {
                case 0:
                default:
                    item = inflater.inflate(R.layout.detailed_item, container, false);
                    ((TextView) item.findViewById(R.id.detailed_item_title)).setText(titles[i]);
                    ((TextView) item.findViewById(R.id.detailed_item_value)).setText(mLesson.m_teacher.m_fio);
                    ((TextView) item.findViewById(R.id.detailed_item_value)).setOnClickListener(mLectClickListener);
                    break;
                case 1:
                    item = inflater.inflate(R.layout.detailed_item_list, container, false);
                    ((TextView) item.findViewById(R.id.detailed_item_list_title)).setText(titles[i]);
                    mGroupList = (ListView) item.findViewById(R.id.detailed_item_list_list);
                    Log.d("init", "m_list_groups.size():" + String.valueOf(mLesson.getAllLessonInstances().size()));
                    mGroupListAdapter = new ArrayAdapter(inflater.getContext(), R.layout.detailed_item_list_item, mLesson.m_list_groups){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent){
                            View holder;
                            GroupInfo group = (GroupInfo)getItem(position);
                            if(convertView == null){
                                // You should fetch the LayoutInflater once in your constructor
                                holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_item_list_item, parent, false);
                            }else{
                                holder = convertView;
                            }

                            TextView v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text1);
                            v.setText(group.m_name);
                            v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text3);
                            v.setText(group.m_spec);
                            return holder;
                        }
                    };

                    int numberOfItems = mGroupListAdapter.getCount();
                    // Get total height of all items.
                    int totalItemsHeight = 0;
                    for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                        View it = mGroupListAdapter.getView(itemPos, null, (ListView)mGroupList);
                        it.measure(0, 0);
                        totalItemsHeight += it.getMeasuredHeight();
                    }

                    int totalDividersHeight = mGroupList.getDividerHeight() *  (numberOfItems - 1);

                    ViewGroup.LayoutParams params = mGroupList.getLayoutParams();
                    params.height = totalItemsHeight + totalDividersHeight;
                    mGroupList.setLayoutParams(params);
                    mGroupList.requestLayout();

                    Log.d("init", "adapter.getCount():" + String.valueOf(mGroupListAdapter.getCount()));

                    mGroupList.setAdapter(mGroupListAdapter);
                    // ListView Item Click Listener
                    mGroupList.setOnItemClickListener(mGroupClickListener);

                    break;
                case 2:
                    item = inflater.inflate(R.layout.detailed_item_list, container, false);
                    ((TextView) item.findViewById(R.id.detailed_item_list_title)).setText(titles[i]);
                    ListView list = (ListView) item.findViewById(R.id.detailed_item_list_list);
                    Log.d("init", String.valueOf(mLesson.getAllLessonInstances().size()));
                    ArrayAdapter adapter = new ArrayAdapter(inflater.getContext(), R.layout.detailed_item_list_item, mLesson.getAllLessonInstances()){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent){
                            View holder;
                            RegLessonInstance lesson = (RegLessonInstance)getItem(position);
                            if(convertView == null){
                                // You should fetch the LayoutInflater once in your constructor
                                holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_item_list_item, parent, false);
                            }else{
                                holder = convertView;
                            }

                            TextView v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text1);
                            v.setText(getResources().getStringArray(R.array.abbr_week_day_array)[lesson.m_day]
                                    + ", "
                                    + lesson.m_timeStart
                                    + "-"
                                    + lesson.m_timeEnd);
                            v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text2);
                            v.setText(getResources().getStringArray(R.array.lesson_type)[lesson.m_type]);
                            v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text3);
                            v.setText(lesson.m_buildingName + ", " + lesson.m_roomName);
                            return holder;
                        }
                    };

                    numberOfItems = adapter.getCount();
                    // Get total height of all items.
                    totalItemsHeight = 0;
                    for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                        View it = adapter.getView(itemPos, null, (ListView)list);
                        it.measure(0, 0);
                        totalItemsHeight += it.getMeasuredHeight();
                    }

                    totalDividersHeight = list.getDividerHeight() *  (numberOfItems - 1);

                    params = list.getLayoutParams();
                    params.height = totalItemsHeight + totalDividersHeight;
                    list.setLayoutParams(params);
                    list.requestLayout();

                    list.setAdapter(adapter);
                    break;
            }
            detailed_class_list.addView(item);
        }
        getActivity().setTitle("");
        return view;
    }
    private AdapterView.OnItemClickListener mGroupClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
        int position, long id) {
            GroupInfo tmpGroup = (GroupInfo)mGroupListAdapter.getItem(position);

            Log.d("init", tmpGroup.toString());
            Log.d("init", tmpGroup.m_name);
            Log.d("init", String.valueOf(tmpGroup.m_id));
            new ServerGetTable(tmpGroup, getActivity()).execute();
        }

    };
    private View.OnClickListener mLectClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if(mLesson.m_teacher.m_fio != "Not set" && mLesson.m_teacher.m_fio != "Не задан")
                new ServerGetTable(mLesson.m_teacher, getActivity()).execute();
        }

    };
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}