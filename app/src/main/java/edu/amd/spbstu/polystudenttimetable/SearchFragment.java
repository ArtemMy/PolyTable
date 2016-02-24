package edu.amd.spbstu.polystudenttimetable;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
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
import android.widget.Filter;
import android.widget.TextView;

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
    public static SearchFragment newInstance(String param1, int param2){
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param1);
        args.putInt(ARG_TYPE, param2);
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
            mParam = getArguments().getString(ARG_TYPE);
            mType = (getArguments().getInt(ARG_TYPE) == 0) ? t_type.GROUP : t_type.LECTURER;
        }
        Log.d("init", "search fragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(view == null)
            view = inflater.inflate(R.layout.fragment_search, container, false);
        textView = (AutoCompleteTextView)
                view.findViewById(R.id.autocomplete_search);
        textView.setThreshold(1);

/*        ArrayAdapter adapterGroups = new ArrayAdapter(inflater.getContext(), R.layout.list_item_2_text, StaticStorage.m_listGroups){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View holder;
                if(convertView == null){
                    // You should fetch the LayoutInflater once in your constructor
                    holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_2_text, parent, false);
                }else{
                    holder = convertView;
                }

                TextView v = (TextView) holder.findViewById(R.id.item_list_text1);
                v.setText(((Group) getItem(position)).m_name);
                v = (TextView) holder.findViewById(R.id.item_list_text2);
                v.setText(((Group) getItem(position)).m_spec);
                return holder;
            }
            @Override
            public Filter getFilter() {
                return nameFilter;
            }

            Filter nameFilter = new Filter() {
                @Override
                public String convertResultToString(Object resultValue) {
                    String str = ((Group)(resultValue)).m_name;
                    return str;
                }
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    if(constraint != null) {
                        suggestions.clear();
                        for (Customer customer : itemsAll) {
                            if(customer.getName().toLowerCase().startsWith(constraint.toString().toLowerCase())){
                                suggestions.add(customer);
                            }
                        }
                        FilterResults filterResults = new FilterResults();
                        filterResults.values = suggestions;
                        filterResults.count = suggestions.size();
                        return filterResults;
                    } else {
                        return new FilterResults();
                    }
                }
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    ArrayList<Customer> filteredList = (ArrayList<Customer>) results.values;
                    if(results != null && results.count > 0) {
                        clear();
                        for (Customer c : filteredList) {
                            add(c);
                        }
                        notifyDataSetChanged();
                    }
                }
            };

        };
        */
        if(mType == t_type.GROUP) {
            textView.setOnClickListener(this);
            textView.setOnItemClickListener(this);

            textView.setHint(R.string.group_search_placeholder);

            adapter = new ArrayAdapter<String>(getActivity(), R.layout.text_layout, StaticStorage.m_listGroupsName);
            textView.setAdapter(adapter);
            adapter.setNotifyOnChange(true);

            if (StaticStorage.m_listGroupsName.isEmpty()) {
                textView.setCompletionHint(getActivity().getString(R.string.placeholder_downloading));
                startDownloadListFaculties((ArrayAdapter<String>) textView.getAdapter());
            }
        }
        else {
            textView.setOnItemClickListener(this);
            textView.setHint(R.string.lecturer_search_placeholder);
            adapter = new ArrayAdapter<String>(getActivity(), R.layout.text_layout, StaticStorage.m_listLecturerName);
            adapter.setNotifyOnChange(true);
            textView.setAdapter(adapter);
            textView.addTextChangedListener(this);
        }
        getActivity().setTitle(getResources().getString(R.string.search));
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
            Group tmpGroup = StaticStorage.m_listGroups.get(ind);

            Log.d("init", String.valueOf(StaticStorage.m_listGroups.get(pos).m_listLessons.size()));
            new ServerGetTable(tmpGroup, this).execute();
        } else {
//            int ind = StaticStorage.m_listLecturerName.indexOf(selected);
            Lecturer tmpLecturer = StaticStorage.m_listLecturers.get(pos);
            Log.d("init", String.valueOf(StaticStorage.m_listLecturers.get(pos).m_listLessons.size()));
            new ServerGetTable(tmpLecturer, this).execute();
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
