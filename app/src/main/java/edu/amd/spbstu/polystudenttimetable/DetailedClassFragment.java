package edu.amd.spbstu.polystudenttimetable;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.CollapsingToolbarLayout;
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detailed, container, false);

        TextView collapsingToolbar =
                (TextView)view.findViewById(R.id.detailed_title);
        collapsingToolbar.setText(mLesson.m_subject);

        LinearLayout detailed_class_list = (LinearLayout)view.findViewById(R.id.detailed_container);

        String[] titles = getResources().getStringArray(R.array.class_details);
        for(int i = 0; i < titles.length; ++i) {
            View item;
            switch(i) {
                case 0:
                default:
                    item = inflater.inflate(R.layout.detailed_item, container, false);
                    ((TextView) item.findViewById(R.id.detailed_item_title)).setText(titles[i]);
                    ((TextView) item.findViewById(R.id.detailed_item_value)).setText(mLesson.m_teacherFio);
                    break;
                case 1:
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

                    list.setAdapter(adapter);

                    // ListView Item Click Listener
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {

                            // ListView Clicked item index
                            int itemPosition = position;

                            // ListView Clicked item value
                            RegLessonInstance itemValue = (RegLessonInstance) parent.getItemAtPosition(position);
                            Log.d("init", itemValue.m_timeStart);
                        }

                    });

                    break;
            }
            detailed_class_list.addView(item);
        }
        return view;
    }

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