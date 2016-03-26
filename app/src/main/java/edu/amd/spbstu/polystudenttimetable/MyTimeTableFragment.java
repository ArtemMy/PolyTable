package edu.amd.spbstu.polystudenttimetable;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.wefika.calendar.CollapseCalendarView;
import com.wefika.calendar.manager.CalendarManager;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MyTimeTableFragment extends Fragment
        implements CollapseCalendarView.OnDateSelect,
        CardView.OnCreateContextMenuListener {
    private static final String ARG_LESSONS = "lessons";
    private static final String ARG_TITLE = "title";
    View mRootView = null;
    List<RegLessonInstance> mAllClasses;
    LocalDate mDay;

    private ArrayList<Lesson> mLessonList;
    private String mTitle;

    private static final String TAG = "polytable_log";
    private static CalendarManager calendarManager;
    private static CollapseCalendarView calendarView;
    private OnFragmentInteractionListener mListener;
    ListView daytableView;

    private boolean mOnCreateCalled = false;

    public MyTimeTableFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("init", "timetable fragment onCreate");

        if (((MainNavigationDrawer)getActivity()).isGroup()) {
            mLessonList = ((Group)(((MainNavigationDrawer)getActivity()).the_obj)).m_listLessons;
            mTitle = ((Group)(((MainNavigationDrawer)getActivity()).the_obj)).m_info.m_name;
        } else {
            mLessonList = ((Lecturer)(((MainNavigationDrawer)getActivity()).the_obj)).m_listLessons;
            mTitle = ((Lecturer)(((MainNavigationDrawer)getActivity()).the_obj)).m_info.m_fio;
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

        DayTableListAdapter adapter = new DayTableListAdapter(getActivity(), mAllClasses, mDay, daytableView, true);
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

        final int position = info.position;
        RegLessonInstance currentLesson;
        currentLesson = mAllClasses.get(position);
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
                new WriteFile(getActivity(), currentLesson.parent).execute();
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
                new WriteFile(getActivity(), currentLesson.parent).execute();
                return true;
            case R.id.menu_class_homework:
                Log.d("init", "mHomework");
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
                            }
                            else {
                                e.setSpan(new BulletSpan(30), length, length + 1,
                                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                            }
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { /* no action */ }

                    @Override
                    public void onTextChanged(CharSequence val, int arg1, int arg2, int arg3) { }
                });
                if(currentLesson.m_homework.containsKey(mDay))
                    edittext.setText(currentLesson.m_homework.get(mDay).m_task);

                alert.setTitle(R.string.homework);

                alert.setView(edittext);
                alert.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //What ever you want to do with the value
                        /*
                        String taskText = edittext.getText().toString();
                        String[] lines = TextUtils.split(taskText, "\n");
                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                        String line = null;
                        for (int index = 0; index < lines.length; ++index) {
                            line = lines[index];
                            int length = spannableStringBuilder.length();
                            spannableStringBuilder.append(line);
                            if (index != lines.length - 1) {
                                spannableStringBuilder.append("\n");
                            } else if (TextUtils.isEmpty(line)) {
                                spannableStringBuilder.append("\u200B");
                            }
                            spannableStringBuilder.setSpan(new BulletSpan(30), length, length + 1,
                                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        }
                        if(mAllClasses.get(position).m_homework.containsKey(mDay))
                            mAllClasses.get(position).m_homework.get(mDay).m_task = spannableStringBuilder;
                        else {
                            RegLessonInstance.Homework hw = mAllClasses.get(position).newHomework();
                            hw.m_task = spannableStringBuilder.toString();
                            mAllClasses.get(position).m_homework.put(mDay, hw);
                        }
                        */

                        if(mAllClasses.get(position).m_homework.containsKey(mDay))
                            mAllClasses.get(position).m_homework.get(mDay).m_task = edittext.getText().toString();
                        else {
                            RegLessonInstance.Homework hw = mAllClasses.get(position).newHomework();
                            hw.m_task = edittext.getText().toString();
                            mAllClasses.get(position).m_homework.put(mDay, hw);
                        }

                        new WriteFile(getActivity(), mAllClasses.get(position).parent).execute();
                        initTable();
                    }
                });

                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
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
