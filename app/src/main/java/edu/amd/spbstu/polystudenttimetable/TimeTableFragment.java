package edu.amd.spbstu.polystudenttimetable;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wefika.calendar.CollapseCalendarView;
import com.wefika.calendar.manager.CalendarManager;

import org.joda.time.LocalDate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class TimeTableFragment extends Fragment
        implements CollapseCalendarView.OnDateSelect,
        CardView.OnCreateContextMenuListener {
    private static final String ARG_LESSONS = "lessons";
    private static final String ARG_TITLE = "title";
    View mRootView = null;
    List<RegLessonInstance> mAllClasses;
    LocalDate mDay;
    RegLessonInstance currentLesson;

    private ArrayList<Lesson> mLessonList;
    private String mTitle;

    private static CalendarManager calendarManager;
    private static CollapseCalendarView calendarView;
    private OnFragmentInteractionListener mListener;
    ListView daytableView;

    private boolean mOnCreateCalled = false;

    public static TimeTableFragment newInstance(Lecturer lect) {
        TimeTableFragment fragment = new TimeTableFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LESSONS, lect.m_listLessons);
        args.putSerializable(ARG_TITLE, lect.m_fio);
        fragment.setArguments(args);
        return fragment;
    }

    public static TimeTableFragment newInstance(Group group) {
        TimeTableFragment fragment = new TimeTableFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LESSONS, group.m_listLessons);
        args.putSerializable(ARG_TITLE, group.m_name);
        fragment.setArguments(args);
        return fragment;
    }

    public TimeTableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("init", "timetable fragment onCreate");

        if (getArguments() != null) {
            mLessonList = (ArrayList<Lesson>)getArguments().getSerializable(ARG_LESSONS);
            mTitle = (String)getArguments().getSerializable(ARG_TITLE);
        }
        else {
            mLessonList = new ArrayList<Lesson>();
            mTitle = getResources().getString(R.string.error_title);
        }
        /* calendar init */
        calendarManager = new CalendarManager(LocalDate.now(), CalendarManager.State.MONTH, LocalDate.now(), LocalDate.now().plusYears(1));
        setHasOptionsMenu(true);
//        calendarManager.toggleView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_time_table, container, false);

        calendarManager.toggleView();
        calendarView = (CollapseCalendarView) mRootView.findViewById(R.id.day_table_calendar);
        calendarView.setListener(this);
        calendarView.init(calendarManager);
        initTable();
        mOnCreateCalled = true;

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mTitle);

        return mRootView;
    }

    private void initTable ()
    {
        if(mAllClasses != null)
            mAllClasses.clear();
        int w = calendarManager.getSelectedDay().getDayOfWeek() - 1;
        mDay = calendarManager.getSelectedDay();

        Log.d("init", String.valueOf(mLessonList.size()));
        mAllClasses = new ArrayList<RegLessonInstance>();
        for (Lesson lesson : mLessonList) {
            if(lesson.getLessonInstances(w) != null) {
                mAllClasses.addAll(lesson.getLessonInstances(w));
            }
        }
        Log.d("init", String.valueOf(mAllClasses.size()));

        daytableView = (ListView) mRootView.findViewById(R.id.daytable_view);

//        daytableView.setItemAnimator(new DefaultItemAnimator());
//        daytableView.setHasFixedSize(true);

        DayTableListAdapter adapter = new DayTableListAdapter(getActivity(), mAllClasses, mDay, daytableView);
        daytableView.setAdapter(adapter);
        registerForContextMenu(daytableView);
        daytableView.setEmptyView(mRootView.findViewById(R.id.empty_class_view));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.class_menu, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    }

    @Override
        public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        currentLesson = mAllClasses.get(info.position);
        switch (item.getItemId()) {
            case R.id.menu_class_cancel:
                Log.d("init", "Cancel class");
                if(currentLesson.m_isCanceled.containsKey(mDay)) {
                    currentLesson.m_isCanceled.remove(mDay);
                    initTable();
                }
                else {
                    currentLesson.m_isCanceled.put(mDay, true);
                    initTable();
                }
                return true;
            case R.id.menu_class_important:
                Log.d("init", "mImportant");
                if(currentLesson.m_isImportant.containsKey(mDay)) {
                    currentLesson.m_isImportant.remove(mDay);
                    initTable();
                }
                else {
                    currentLesson.m_isImportant.put(mDay, true);
                    initTable();
                }
                return true;
            case R.id.menu_class_homework:
                Log.d("init", "mHomework");
                if(currentLesson.m_isHomework.containsKey(mDay)) {
                    currentLesson.m_isHomework.remove(mDay);
                    initTable();
                }
                else {
                    currentLesson.m_isHomework.put(mDay, "");
                    initTable();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
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

    @Override
    public void onDateSelected(LocalDate date) {
        if(calendarManager.getState() == CalendarManager.State.MONTH) {
//            calendarManager.toggleView();
            calendarView.populateLayout();
        }
        if(mOnCreateCalled)
           initTable();
    }
}
