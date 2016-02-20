package edu.amd.spbstu.polystudenttimetable;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
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
    private static final String ARG_PARAM = "lessons";
    View mRootView = null;
    List<RegLessonInstance> mAllClasses;
    LocalDate mDay;
    RegLessonInstance currentLesson;
    private ArrayList<Lesson> mLessonList;
    private static CalendarManager calendarManager;
    private static CollapseCalendarView calendarView;
    private OnFragmentInteractionListener mListener;
    RecyclerView daytableView;

    private boolean mOnCreateCalled = false;

    public static TimeTableFragment newInstance(ArrayList<Lesson> listLesson) {
        TimeTableFragment fragment = new TimeTableFragment();
        Bundle args = new Bundle();
        args.putSerializable("lessons", listLesson);
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
            mLessonList = (ArrayList<Lesson>)getArguments().getSerializable(ARG_PARAM);
        }
        else
            mLessonList = new ArrayList<Lesson>();
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
        return mRootView;
    }

    private void initTable ()
    {
        int w = calendarManager.getSelectedDay().getDayOfWeek() - 1;
        mDay = calendarManager.getSelectedDay();
        if(w == 6) {
            return;
        }

        Log.d("init", String.valueOf(mLessonList.size()));
        mAllClasses = new ArrayList<RegLessonInstance>();
        for (Lesson lesson : mLessonList) {
            if(lesson.getLessonInstances(w) != null) {
                mAllClasses.addAll(lesson.getLessonInstances(w));
            }
        }
        Log.d("init", String.valueOf(mAllClasses.size()));

        daytableView = (RecyclerView)mRootView.findViewById(R.id.daytable_view);
        daytableView.setItemAnimator(new DefaultItemAnimator());
        daytableView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        daytableView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new DayTableAdapter(mAllClasses, mDay, currentLesson);
        daytableView.setAdapter(adapter);
        registerForContextMenu(daytableView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.class_menu, menu);
        Toast.makeText(getActivity(), "loooong click", Toast.LENGTH_SHORT).show();
    }

    @Override
        public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        currentLesson = mAllClasses.get(info.position);
        switch (item.getItemId()) {
            case R.id.menu_class_cancel:
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
