package edu.amd.spbstu.polystudenttimetable;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.os.Parcel;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailedClassFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailedClassFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailedClassFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
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
        args.putParcelable("parsedLesson", param);
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
            mLesson = (Lesson)savedInstanceState.getParcelable("parsedLesson");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detailed_class, container, false);
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