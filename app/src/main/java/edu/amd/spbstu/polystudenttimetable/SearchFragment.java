package edu.amd.spbstu.polystudenttimetable;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private boolean m_activeApp = true;
    private boolean m_download_finished = true;


    private AutoCompleteTextView textView;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2){
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Log.d("init", "search fragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        textView = (AutoCompleteTextView)
                view.findViewById(R.id.autocomplete_group);
        textView.setThreshold(1);
        textView.setOnClickListener(this);

        textView.setOnItemClickListener(this);
/*
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                Log.d("init", "search1");
            }
        });
        textView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {

                Log.d("init", "search4");
                Fragment fragment = null;
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.container, new TimeTableFragment());
                transaction.addToBackStack(null);
                transaction.commit();

                Toast.makeText(getActivity(), arg1 + "",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        textView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("init", "search2");
                switch (parent.getId()) {
                    case R.id.textView:
                        Log.d("init", "search3");
                        Fragment fragment = null;
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.container, new TimeTableFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                //TODO:
            }
        });
        */
        ArrayAdapter<String>  adapterGroups = new ArrayAdapter<String>(getActivity(), R.layout.text_layout, StaticStorage.m_listGroupsName);
        textView.setAdapter(adapterGroups);
        adapterGroups.setNotifyOnChange(true);
        if (StaticStorage.m_listGroupsName.isEmpty()) {
            textView.setCompletionHint(getActivity().getString(R.string.placeholder_downloading));
            startDownloadListFaculties((ArrayAdapter<String>) textView.getAdapter());
        }
        return view;

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                            long id) {
        ServerGetTable sgtt = new ServerGetTable((ArrayAdapter) textView.getAdapter(), this);
        Group tmpGroup = new Group();
        String selected = (String) parent.getItemAtPosition(pos);
        int ind = StaticStorage.m_listGroupsName.indexOf(selected);
        tmpGroup.m_id = StaticStorage.m_listGroups.get(ind).m_id;
        Log.d("init", String.valueOf(StaticStorage.m_listGroups.get(ind).m_level));
        Log.d("init", StaticStorage.m_listGroups.get(ind).m_faculty.m_name);
        Log.d("init", StaticStorage.m_listGroups.get(ind).m_name);
        sgtt.execute(tmpGroup);
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
    public void onClick (View v) {
        textView.showDropDown();
    }

    public void startDownloadListFaculties(ArrayAdapter<String> arrayAdapter)
    {
        StaticStorage.m_listFaculties.clear();
        Thread thread;
        m_activeApp = true;
        m_download_finished = false;
        thread = new Thread() {
            public void run()
            {
                try
                {
                    while (!m_download_finished)
                    {
                        Thread.sleep(200);
                        if(m_activeApp) {
                            if (isOnline()) {
                                textView.setCompletionHint(getActivity().getString(R.string.placeholder_downloading));
                                ServerGetFaculties serverGetFaclts = new ServerGetFaculties((ArrayAdapter<String>) textView.getAdapter());
                                serverGetFaclts.execute();
                                m_activeApp = false;
                            }
                            else
                                textView.setCompletionHint(getActivity().getString(R.string.placeholder_turn_on_internet));
                        }
                        else
                            if(!textView.getAdapter().isEmpty()) {
                                textView.setCompletionHint("");
                                m_download_finished = true;
                            }
                    }       // while
                }
                catch (Exception ex) { }
            }

        };
        thread.start();
    }
    public boolean isOnline()
    {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if ((netInfo != null) && netInfo.isConnectedOrConnecting())
            return true;
        return false;
    }

}
