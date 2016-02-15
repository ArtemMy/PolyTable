package edu.amd.spbstu.polystudenttimetable;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.CardView;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimeTableFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimeTableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimeTableFragment extends Fragment
        implements CollapseCalendarView.OnDateSelect,
        CardView.OnCreateContextMenuListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static CalendarManager calendarManager;
    private static CollapseCalendarView calendarView;
    private OnFragmentInteractionListener mListener;

    private boolean mOnCreateCalled = false;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TimeTableFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TimeTableFragment newInstance(String param1, String param2) {
        TimeTableFragment fragment = new TimeTableFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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

        /*
        if(!StaticStorage.m_isInitialized) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, new SearchFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        }
        */

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
                /* calendar init */
        calendarManager = new CalendarManager(LocalDate.now(), CalendarManager.State.MONTH, LocalDate.now(), LocalDate.now().plusYears(1));
//        calendarManager.toggleView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_time_table, container, false);

        calendarManager.toggleView();
        calendarView = (CollapseCalendarView) view.findViewById(R.id.day_table_calendar);
        calendarView.setListener(this);
        calendarView.init(calendarManager);
        initTable(view);
        mOnCreateCalled = true;
        return view;
    }

    private void initTable (View view)
    {
        /* day init */
        LinearLayout dayTableLayout = (LinearLayout) view.findViewById(R.id.day_table);

        dayTableLayout.removeAllViews();
        LayoutInflater ltInflater = getActivity().getLayoutInflater();

        int w = calendarManager.getSelectedDay().getDayOfWeek() - 1;

        if(w == 6) {
            return;
        }
        Log.d("init", String.valueOf(StaticStorage.m_isInitialized));

        for (int i = w * 5; i < (w + 1) * 5; i++) {
            Lesson lesson = StaticStorage.m_listLessons.get(i);
            if(lesson.m_subject.isEmpty()) {
                /*
                int j = 1;
                while (i + 1 < (w + 1) * 5 && StaticStorage.m_listLessons.get(i + 1).m_subject.isEmpty()) {
                    j++;
                    i++;
                }
                    Log.d("init", String.valueOf(j));
               */
            }
            else {
                SingleClassView scv = new SingleClassView(getActivity());
                scv.init(lesson);
                dayTableLayout.addView(scv);
//                registerForContextMenu(scv);
            }
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.class_menu, menu);
        Toast.makeText(getActivity(), "loooong click", Toast.LENGTH_SHORT).show();
        MenuItem item = menu.getItem(0);
//        mClassViews.
    }

    /*
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenu.ContextMenuInfo info = (ContextMenu.ContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_class_cancel:
                ((SingleClassView) info.targetView).setCanceled(!((SingleClassView) info.targetView).getCanceled());
                return true;
            case R.id.menu_class_important:
                ((SingleClassView) info.targetView).setImportant(!((SingleClassView) info.targetView).getImportant());
                return true;
            case R.id.menu_class_edit:
                Toast.makeText(getActivity(), "ediiit", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
*/
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
           initTable(getView());
    }

/*
    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        private String[] dataSource;
        public RecyclerAdapter(String[] dataArgs){
            dataSource = dataArgs;

        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;


        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(dataSource[position]);
        }

        @Override
        public int getItemCount() {
            return dataSource.length;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder{
            protected TextView textView;
            public ViewHolder(View itemView) {
                super(itemView);
                textView =  (TextView) itemView.findViewById(R.id.list_item);

            }


        }
    }
    */
}
