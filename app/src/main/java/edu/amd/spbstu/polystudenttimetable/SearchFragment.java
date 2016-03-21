package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import java.sql.Array;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemClickListener, TextWatcher {

    public enum t_type {
        GROUP,
        LECTURER
    };
    private static final String ARG_PARAM = "param";
    private static final String ARG_TYPE = "type";

    private View view;

    private String mParam;
    private t_type mType;

    ArrayAdapter<String>  adapter;

    private Handler autocompleteHandler = new Handler();
    private ServerGetLecturers loadingLecturers;

    private OnFragmentInteractionListener mListener;

    private boolean m_activeApp = false;
    private boolean m_download_finished = false;


    private AutoCompleteTextView textView;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    public static SearchFragment newInstance(String param1, int param2){
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param1);
        args.putInt(ARG_TYPE, param2);
        fragment.setArguments(args);
        Log.d("init", "search fragment onCreate");
        return fragment;
    }

    public SearchFragment() {
        Log.d("init", "search fragment onCreate");
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam = getArguments().getString(ARG_PARAM);
            mType = (getArguments().getInt(ARG_TYPE) == 0) ? t_type.GROUP : t_type.LECTURER;
        }
        Log.d("init", "search fragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        if(view == null)
        Log.d("init", "search fragment onCreateView");
        if(view == null)
            view = inflater.inflate(R.layout.fragment_search, container, false);
        if(textView == null) {
            textView = (AutoCompleteTextView)
                    view.findViewById(R.id.autocomplete_search);
            textView.setThreshold(1);

            if(mType == t_type.GROUP) {
                textView.setOnClickListener(this);
                textView.setOnItemClickListener(this);

                textView.setHint(R.string.group_search_placeholder);

                adapter = new ArrayAdapter<String>(getActivity(), R.layout.text_layout, StaticStorage.m_listGroupsName);
                textView.setAdapter(adapter);
                adapter.setNotifyOnChange(true);

                ListView list = (ListView) view.findViewById(R.id.recentList);
                final ArrayAdapter recent_adapter = new ArrayAdapter(inflater.getContext(), R.layout.detailed_item_list_item, new ArrayList(StaticStorage.m_recentGroups.values())){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent){
                        View holder;
                        Group group = (Group)getItem(position);
                        if(convertView == null){
                            // You should fetch the LayoutInflater once in your constructor
                            holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_item_list_item, parent, false);
                        }else{
                            holder = convertView;
                        }

                        TextView v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text1);
                        v.setText(group.m_info.m_name);
                        v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text2);
                        v.setText(group.m_info.m_faculty.m_abbr);
                        v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text3);
                        v.setText(group.m_info.m_spec);
                        return holder;
                    }
                };
                list.setAdapter(recent_adapter);
                // ListView Item Click Listener
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Group group = (Group) recent_adapter.getItem(position);

                        Log.d("init", group.toString());
                        if (((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu().getItem(0).isChecked()) {
                            new CreateFiles(getActivity(), group).execute();
                        } else {
                            ((MainNavigationDrawer) getActivity()).switchContent(TimeTableFragment.newInstance(group));
                            StaticStorage.m_recentGroups.put(group.m_info.m_id, group);
                        }
                    }
                });

                Button btn = (Button) view.findViewById(R.id.primat_list);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!((MainNavigationDrawer)getActivity()).isOnline()) {
                            ((MainNavigationDrawer)getActivity()).askForInternet();
                            return;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getResources().getString(R.string.str_depth));
                        builder.setIcon(R.drawable.logo_amd_mod);
//                        ListView modeList = new ListView(getActivity());
                        final ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_list_item_1, android.R.id.text1,
                                StaticStorage.m_primatGroupsName);
                        builder.setAdapter(modeAdapter,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        GroupInfo tmpGroup = StaticStorage.m_primatGroups.get(which);
                                        new ServerGetTable(tmpGroup, getActivity()).execute();
                                    }
                                });
                        final Dialog dialog = builder.create();
                        dialog.show();
                    }
                });

                if (!((MainNavigationDrawer)getActivity()).isOnline()) ((MainNavigationDrawer)getActivity()).askForInternet();
                else startDownloadListFaculties((ArrayAdapter<String>) textView.getAdapter());
            }
            else {
                if (!((MainNavigationDrawer)getActivity()).isOnline()) ((MainNavigationDrawer)getActivity()).askForInternet();
                textView.setOnItemClickListener(this);
                textView.setHint(R.string.lecturer_search_placeholder);
                adapter = new ArrayAdapter<String>(getActivity(), R.layout.text_layout, StaticStorage.m_listLecturerName);
                adapter.setNotifyOnChange(true);
                textView.setAdapter(adapter);
                textView.addTextChangedListener(this);

                ListView list = (ListView) view.findViewById(R.id.recentList);
                final ArrayAdapter recent_adapter = new ArrayAdapter(inflater.getContext(), R.layout.detailed_item_list_item, new ArrayList(StaticStorage.m_recentLecturers.values())){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent){
                        View holder;
                        Lecturer group = (Lecturer)getItem(position);
                        if(convertView == null){
                            // You should fetch the LayoutInflater once in your constructor
                            holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_item_list_item, parent, false);
                        }else{
                            holder = convertView;
                        }

                        TextView v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text1);
                        v.setText(group.m_info.m_fio);
                        v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text2);
                        v.setText("");
                        v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text3);
                        v.setText(group.m_info.m_chair);
                        return holder;
                    }
                };
                list.setAdapter(recent_adapter);
                // ListView Item Click Listener
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Lecturer lect = (Lecturer) recent_adapter.getItem(position);

                        Log.d("init", lect.toString());
                        if (((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu().getItem(0).isChecked()) {
                            new CreateFiles(getActivity(), lect).execute();
                        } else {
                            ((MainNavigationDrawer) getActivity()).switchContent(TimeTableFragment.newInstance(lect));
                            StaticStorage.m_recentLecturers.put(lect.m_info.m_id, lect);
                        }
                    }
                });
/*                Button btn = (Button) view.findViewById(R.id.primat_list);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getResources().getString(R.string.str_depth));

                        ListView modeList = new ListView(getActivity());
                        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_list_item_1, android.R.id.text1,
                                StaticStorage.m_primatLecturerName);
                        modeList.setAdapter(modeAdapter);

                        builder.setView(modeList);
                        final Dialog dialog = builder.create();

                        dialog.show();
                    }
                });
                */
                Button btn = (Button) view.findViewById(R.id.primat_list);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!((MainNavigationDrawer)getActivity()).isOnline()) ((MainNavigationDrawer)getActivity()).askForInternet();
                        else new ServerGetPrimatLecturers(getActivity()).execute();
                    }
                });

            }
        }

//        ((CollapsingToolbarLayout)getActivity().findViewById(R.id.collapsing_toolbar_layout)).setTitle(getResources().getString(R.string.search));
//        ((Toolbar)getActivity().findViewById(R.id.toolbar)).setTitle(getResources().getString(R.string.search));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.search));
        return view;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String term = s.toString();
        autocompleteHandler.removeMessages(0);
        if( term.length() > 3 ){
            if (loadingLecturers == null || loadingLecturers.getStatus() == AsyncTask.Status.FINISHED) {
                autocompleteHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String text = textView.getText().toString();
                            if (text.length() > 3) {
                                loadingLecturers = new ServerGetLecturers(adapter);
                                loadingLecturers.execute(text);
                                Log.d("init", "loading: " + text);
                            }
                        }
                }, 500);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                            long id) {
        String selected = (String) parent.getItemAtPosition(pos);

        Log.d("init", String.valueOf(mType));
        Log.d("init", String.valueOf(StaticStorage.m_listLecturers.size()));

        if(mType == t_type.GROUP) {
            int ind = StaticStorage.m_listGroupsName.indexOf(selected);
            GroupInfo tmpGroup = StaticStorage.m_listGroups.get(ind);

            new ServerGetTable(tmpGroup, getActivity()).execute();
        } else {
//            int ind = StaticStorage.m_listLecturerName.indexOf(selected);
            LecturerInfo tmpLecturer = StaticStorage.m_listLecturers.get(pos);
            new ServerGetTable(tmpLecturer, getActivity()).execute();
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
    @Override
    public void onClick (View v) {
        textView.showDropDown();
        if (!((MainNavigationDrawer)getActivity()).isOnline()) ((MainNavigationDrawer)getActivity()).askForInternet();
        else startDownloadListFaculties((ArrayAdapter<String>) textView.getAdapter());
    }

    public void startDownloadListFaculties(ArrayAdapter<String> arrayAdapter) {
        if (!m_activeApp) {
            StaticStorage.m_listFaculties.clear();
            Thread thread;
            m_activeApp = true;
            m_download_finished = false;
            textView.setCompletionHint(getActivity().getString(R.string.placeholder_downloading));
            ServerGetFaculties serverGetFaclts = new ServerGetFaculties((ArrayAdapter<String>) textView.getAdapter());
            serverGetFaclts.execute();
            m_activeApp = true;
            thread = new Thread() {
                public void run() {
                    try {
                        while (!m_download_finished) {
                            Thread.sleep(200);
                            if (!textView.getAdapter().isEmpty()) {
                                textView.setCompletionHint("");
                                m_download_finished = true;
                            }
                        }       // while
                    } catch (Exception ex) {
                    }
                }

            };
            thread.start();
        }
    }
}
