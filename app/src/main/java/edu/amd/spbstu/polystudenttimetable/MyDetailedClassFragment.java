package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.zip.Inflater;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyDetailedClassFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyDetailedClassFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyDetailedClassFragment extends Fragment {
    private static final String ARG_PARAM = "param1";

    // TODO: Rename and change types of parameters
    private static final String TAG = "polytable_log";
    private Lesson mLesson;
    private ListView classesList = null;
    private ArrayAdapter<String> cAdapter = null;
    private ArrayList<String> classes = null;
    boolean isEditMode = false;
    private ListView mGroupList;
    private ListView mInstList;
    private ArrayAdapter mGroupListAdapter;
    private ArrayAdapter mInstListAdapter;
    private OnFragmentInteractionListener mListener;

    private View view;
    private View mTitle;
    private View mFio;
    private View mGroups;
    private View mInst;

    FloatingActionButton myFab;

    /**
     * @param param Lesson parameter
     * @return A new instance of fragment DetailedClassFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyDetailedClassFragment newInstance(Lesson param) {
        MyDetailedClassFragment fragment = new MyDetailedClassFragment();
        Bundle args = new Bundle();
        args.putSerializable("parsedLesson", param);
        fragment.setArguments(args);
        return fragment;
    }

    public MyDetailedClassFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLesson = (Lesson) getArguments().getSerializable("parsedLesson");
        } else if (savedInstanceState != null) {
            mLesson = (Lesson) savedInstanceState.getSerializable("parsedLesson");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("init", mLesson.m_teacher.m_fio + String.valueOf(mLesson.m_teacher.m_id));

        if(view == null)
            view = inflater.inflate(R.layout.fragment_detailed, container, false);

        TextView collapsingToolbar =
                (TextView) view.findViewById(R.id.detailed_title);
        collapsingToolbar.setText(mLesson.m_subject);

        init();

        myFab = (FloatingActionButton) view.findViewById(R.id.edit_fab);
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) myFab.getLayoutParams();
        p.setBehavior(new FABBehavior()); //should disable default animations
        p.setAnchorId(R.id.detailed_app_bar_layout); //should let you set visibility
        myFab.setLayoutParams(p);
        myFab.setVisibility(View.VISIBLE); // View.INVISIBLE might also be worth trying

        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isEditMode) {
                    Log.d(TAG, "read");
                    myFab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.edit_ic));
                    myFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    isEditMode = false;
                    init();
                } else {
                    Log.d(TAG, "edit");
                    myFab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.tick));
                    myFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccept)));
                    isEditMode = true;
                    init();
                }
            }
        });
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        return view;
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        LinearLayout detailed_class_list = (LinearLayout) view.findViewById(R.id.detailed_container);
        detailed_class_list.removeAllViewsInLayout();

        String[] titles = getResources().getStringArray(R.array.class_details);

        //        #1
        mFio = inflater.inflate(R.layout.detailed_item, detailed_class_list, false);
        EditText mVal = ((EditText) mFio.findViewById(R.id.detailed_item_value));
        mVal.setText(mLesson.m_teacher.m_fio);
        if(!isEditMode) {
            mVal.setEnabled(false);
            mVal.setCursorVisible(false);
            mVal.setKeyListener(null);
            mVal.setBackgroundColor(Color.TRANSPARENT);
            mVal.setOnClickListener(mLectClickListener);
        }
        TextView mTitle = ((TextView) mFio.findViewById(R.id.detailed_item_title));
        mTitle.setText(titles[0]);
        detailed_class_list.addView(mFio);

        //        #2
        mGroups = inflater.inflate(R.layout.detailed_item_list, detailed_class_list, false);
        ((TextView) mGroups.findViewById(R.id.detailed_item_list_title)).

        setText(titles[1]);

        mGroupList = (ListView) mGroups.findViewById(R.id.detailed_item_list_list);
        mGroupListAdapter = new ArrayAdapter<GroupInfo>(inflater.getContext(), R.layout.detailed_item_list_item, mLesson.m_list_groups) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View holder;
                GroupInfo group = (GroupInfo) getItem(position);
                if (convertView == null) {
                    // You should fetch the LayoutInflater once in your constructor
                    holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_item_list_item, parent, false);
                } else {
                    holder = convertView;
                }

                TextView v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text1);
                v.setText(group.m_name);
                v = (TextView) holder.findViewById(R.id.detailed_item_list_item_text3);
                v.setText(group.m_spec);
                if(isEditMode) {
                    ((ImageView) holder.findViewById(R.id.detailed_item_delete)).setVisibility(View.VISIBLE);
                    ((ImageView) holder.findViewById(R.id.detailed_item_delete)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final int position = mGroupList.getPositionForView((View) v.getParent());
                            GroupInfo gr = (GroupInfo) mGroupListAdapter.getItem(position);
                            mGroupListAdapter.remove(gr);
                            mGroupListAdapter.notifyDataSetChanged();
                        }
                    });
                }
                return holder;
            }
        };

        int numberOfItems = mGroupListAdapter.getCount();
        // Get total height of all items.
        int totalItemsHeight = 0;
        for (
                int itemPos = 0;
                itemPos < numberOfItems; itemPos++)

        {
            View it = mGroupListAdapter.getView(itemPos, null, (ListView) mGroupList);
            it.measure(0, 0);
            totalItemsHeight += it.getMeasuredHeight();
        }

        int totalDividersHeight = mGroupList.getDividerHeight() * (numberOfItems - 1);

        ViewGroup.LayoutParams params = mGroupList.getLayoutParams();
        params.height = totalItemsHeight + totalDividersHeight;
        mGroupList.setLayoutParams(params);
        mGroupList.requestLayout();

        Log.d("init", "adapter.getCount():" + String.valueOf(mGroupListAdapter.getCount()));

        mGroupList.setAdapter(mGroupListAdapter);
        // ListView Item Click Listener
        if(!isEditMode)
            mGroupList.setOnItemClickListener(mGroupClickListener);
        detailed_class_list.addView(mGroups);

        if(isEditMode) {
            mGroupListAdapter.setNotifyOnChange(true);

            ImageView imv = (ImageView) mGroups.findViewById(R.id.detailed_item_add);
            imv.setVisibility(View.VISIBLE);
            imv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                                    mGroupListAdapter.add(tmpGroup);
                                }
                            });
                    final Dialog dialog = builder.create();
                    dialog.show();
                }
            });
        }

        //        #3
        mInst = inflater.inflate(R.layout.detailed_item_list, detailed_class_list, false);
        ((TextView) mInst.findViewById(R.id.detailed_item_list_title)).

                setText(titles[2]);

        mInstList = (ListView) mInst.findViewById(R.id.detailed_item_list_list);
        Log.d("init", String.valueOf(mLesson.getAllLessonInstances().

                        size()

        ));
        mInstListAdapter = new ArrayAdapter<RegLessonInstance>(inflater.getContext(), R.layout.detailed_item_list_item, mLesson.getAllLessonInstances()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View holder;
                RegLessonInstance lesson = (RegLessonInstance) getItem(position);
                if (convertView == null) {
                    // You should fetch the LayoutInflater once in your constructor
                    holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_item_list_item, parent, false);
                } else {
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
                if(isEditMode) {
                    ((ImageView) holder.findViewById(R.id.detailed_item_delete)).setVisibility(View.VISIBLE);
                    ((ImageView) holder.findViewById(R.id.detailed_item_delete)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final int position = mInstList.getPositionForView((View) v.getParent());
                            RegLessonInstance rli = (RegLessonInstance) mInstListAdapter.getItem(position);
                            mInstListAdapter.remove(rli);
                            mLesson.m_reg.get(rli.m_day).remove(rli);
                            mInstListAdapter.notifyDataSetChanged();
                        }
                    });
                }
                return holder;
            }
        };

        numberOfItems=mInstListAdapter.getCount();
        // Get total height of all items.
        totalItemsHeight=0;
        for(int itemPos = 0; itemPos<numberOfItems;itemPos++)
        {
            View it = mInstListAdapter.getView(itemPos, null, mInstList);
            it.measure(0, 0);
            totalItemsHeight += it.getMeasuredHeight();
        }

        totalDividersHeight=mInstList.getDividerHeight()*(numberOfItems-1);

        params=mInstList.getLayoutParams();
        params.height=totalItemsHeight+totalDividersHeight;
        mInstList.setLayoutParams(params);
        mInstList.requestLayout();

        mInstList.setAdapter(mInstListAdapter);
        detailed_class_list.addView(mInst);

        if(isEditMode) {
            mInstListAdapter.setNotifyOnChange(true);

            ImageView imv = (ImageView) mInst.findViewById(R.id.detailed_item_add);
            imv.setVisibility(View.VISIBLE);
            imv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.dialog_new_lesson, null);
                    dialogBuilder.setView(dialogView);

                    dialogBuilder.setTitle(getResources().getString(R.string.newn_str));
                    dialogBuilder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            RegLessonInstance rli = new RegLessonInstance(mLesson);
                            rli.m_day = ((Spinner) dialogView.findViewById(R.id.newn_day_val)).getSelectedItemPosition();
                            rli.m_type = ((Spinner) dialogView.findViewById(R.id.newn_type_val)).getSelectedItemPosition();
                            rli.m_weekly = RegLessonInstance.weekly_t.values()[((Spinner) dialogView.findViewById(R.id.newn_odd_val)).getSelectedItemPosition()];
                            rli.m_buildingName = ((EditText)dialogView.findViewById(R.id.newn_build)).getText().toString();
                            rli.m_roomName = ((EditText)dialogView.findViewById(R.id.newn_room)).getText().toString();
                            rli.m_timeStart= String.valueOf(((TimePicker)dialogView.findViewById(R.id.newn_start_val)).getCurrentHour()) + ":" +
                                    String.valueOf(((TimePicker)dialogView.findViewById(R.id.newn_start_val)).getCurrentMinute());
                            rli.m_timeEnd= String.valueOf(((TimePicker)dialogView.findViewById(R.id.newn_end_val)).getCurrentHour()) + ":" +
                                    String.valueOf(((TimePicker)dialogView.findViewById(R.id.newn_end_val)).getCurrentMinute());
                            if(mLesson.m_reg.containsKey(rli.m_day))
                                mLesson.m_reg.get(rli.m_day).add(rli);
                            else {
                                mLesson.m_reg.put(rli.m_day, new ArrayList<RegLessonInstance>());
                                mLesson.m_reg.get(rli.m_day).add(rli);
                            }
                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //pass
                        }
                    });
                    final Dialog dialog = dialogBuilder.create();
                    dialog.show();
                }
            });
        }
    }
    private AdapterView.OnItemClickListener mGroupClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
        int position, long id) {
            GroupInfo tmpGroup = (GroupInfo)mGroupListAdapter.getItem(position);

            Log.d("init", tmpGroup.toString());
            new ServerGetTable(tmpGroup, getActivity()).execute();
        }

    };
    private View.OnClickListener mLectClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
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

    public class FABBehavior extends FloatingActionButton.Behavior {

        public FABBehavior() {
        }

        public FABBehavior(Context context, AttributeSet attributeSet) {
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
            if(dependency instanceof Snackbar.SnackbarLayout) {
                return super.onDependentViewChanged(parent, child, dependency);
            } else if(dependency instanceof AppBarLayout) {
                this.updateFabVisibility(parent, (AppBarLayout)dependency, child);
            }

            return false;
        }

        private boolean updateFabVisibility(CoordinatorLayout parent, AppBarLayout appBarLayout, FloatingActionButton child) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
            if(lp.getAnchorId() != appBarLayout.getId()) {
                return false;
            } else {

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                int point = child.getTop() - params.topMargin;
                try {
                    Method method = AppBarLayout.class.getDeclaredMethod("getMinimumHeightForVisibleOverlappingContent");
                    method.setAccessible(true);
                    if(point <= (int) method.invoke(appBarLayout)) {
                        child.hide();
                    } else {
                        child.show();
                    }
                    return true;
                } catch (Exception e) {
                    return true;
                }
            }
        }
    }
}