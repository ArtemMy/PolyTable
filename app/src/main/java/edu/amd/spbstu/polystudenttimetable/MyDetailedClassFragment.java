package edu.amd.spbstu.polystudenttimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.LocalDate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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
    static final String PREF_LES_COL= "poly_table_lesson_color";

    private static final String TAG = "polytable_log";
    private Lesson mLesson;
    private Lesson mEditLesson;
    boolean isEditMode = false;
    private ListView mGroupList;
    private ListView mInstList;
    private ArrayAdapter mGroupListAdapter;
    private ArrayAdapter mInstListAdapter;
    private OnFragmentInteractionListener mListener;

    private View view;
    private View mTitle;
    private ArrayList<View> mColors;
    private View mFio;
    private View mGroups;
    private View mInst;

    private int mCurCol;

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
        // Inflate the help for this fragment
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
                    if (((TextView) mFio.findViewById(R.id.detailed_item_value)).getText().toString() != "")
                        mEditLesson.m_teacher.m_fio = ((TextView) mFio.findViewById(R.id.detailed_item_value)).getText().toString();
                    if (((MainNavigationDrawer) getActivity()).isGroup()) {
                        Group gr = ((Group) ((MainNavigationDrawer) getActivity()).the_obj);
                        int pos = gr.m_listLessons.indexOf(mEditLesson);
                        if (pos >= 0)
                            gr.m_listLessons.set(pos, mEditLesson);
                        else {
                            pos = gr.m_listLessons.indexOf(mLesson);
                            gr.m_listLessons.set(pos, mEditLesson);
                        }
                    } else {
                        Lecturer le = ((Lecturer) ((MainNavigationDrawer) getActivity()).the_obj);
                        int pos = le.m_listLessons.indexOf(mEditLesson);
                        if (pos >= 0)
                            le.m_listLessons.set(pos, mEditLesson);
                        else {
                            pos = le.m_listLessons.indexOf(mLesson);
                            le.m_listLessons.set(pos, mEditLesson);
                        }
                    }
                    mLesson = mEditLesson;

                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(
                            getActivity().getPackageName(), Context.MODE_PRIVATE).edit();
                    editor.putInt(PREF_LES_COL+String.valueOf(mLesson.hashCode()), mCurCol);
                    editor.commit();
                    Log.d(TAG, String.valueOf(mCurCol));

                    ((MainNavigationDrawer)getActivity()).write(mLesson);
//                    new WriteFile(getActivity(), mLesson).execute();
                    init();
                } else {
                    Log.d(TAG, "edit");
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(mLesson);
                        oos.flush();
                        oos.close();

                        ObjectInputStream in = new ObjectInputStream(
                                new ByteArrayInputStream(bos.toByteArray()));
                        mEditLesson = (Lesson) in.readObject();
                        for (List<RegLessonInstance> lreg : mEditLesson.m_reg.values()) {
                            for (RegLessonInstance reg : lreg) {
                                reg.parent = mEditLesson;
                                for (RegLessonInstance.Homework hw : reg.m_homework.values()) {
                                    hw.m_lesson = reg;
                                }
                            }
                        }

                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                    catch(ClassNotFoundException cnfe) {
                        cnfe.printStackTrace();
                    }
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

    public static void getTotalHeightofListView(ListView listView) {

        ListAdapter mAdapter = listView.getAdapter();

        int totalHeight = 0;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            View mView = LayoutInflater.from(listView.getContext()).inflate(R.layout.detailed_item_list_item, null, false);

            mView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT, ListView.LayoutParams.WRAP_CONTENT));
            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            totalHeight += mView.getMeasuredHeight();
            Log.w("HEIGHT" + i, String.valueOf(totalHeight));

        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (mAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        LinearLayout detailed_class_list = (LinearLayout) view.findViewById(R.id.detailed_container);
        detailed_class_list.removeAllViewsInLayout();

        LinearLayout colorViews = (LinearLayout) view.findViewById(R.id.colors_bar);
        colorViews.removeAllViewsInLayout();
        colorViews.removeAllViews();

        if(isEditMode) {
            if(mColors == null) {
                mColors = new ArrayList<>();
            } else {
                mColors.clear();
            }

            mCurCol = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE).getInt(PREF_LES_COL+String.valueOf(mLesson.hashCode()), 0);
            Log.d(TAG, String.valueOf(mCurCol));

            for (int i = 0; i < 6; i++) {
                final Button b = new Button(getActivity());

                GridView.LayoutParams params = new GridView.LayoutParams(80, 80);
                b.setLayoutParams(params);

                final GradientDrawable gd = new GradientDrawable();
                gd.setColor(getResources().getColor(StaticStorage.lesColor[i]));
                gd.setCornerRadius(5);
                if (i == mCurCol) {
                    gd.setStroke(10, 0xCC333333);
                } else {
                    gd.setStroke(7, 0x77CCCCCC);
                }
                b.setBackgroundDrawable(gd);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(int i = 0; i < mColors.size(); ++i) {
                            final GradientDrawable gd = new GradientDrawable();
                            gd.setColor(getResources().getColor(StaticStorage.lesColor[i]));
                            gd.setCornerRadius(5);
                            gd.setStroke(7, 0x77CCCCCC);
                            ((Button) mColors.get(i)).setBackgroundDrawable(gd);
                        }
                        final GradientDrawable gd = new GradientDrawable();
                        gd.setColor(getResources().getColor(StaticStorage.lesColor[mColors.indexOf(b)]));
                        gd.setCornerRadius(5);
                        gd.setStroke(10, 0xCC333333);

                        ((Button) b).setBackgroundDrawable(gd);

                        mCurCol = mColors.indexOf(b);
                        Log.d(TAG, String.valueOf(mCurCol));

                    }
                });
                mColors.add(b);
                colorViews.addView(b);
            }
        }

        String[] titles = getResources().getStringArray(R.array.class_details);

        //        #1
        mFio = inflater.inflate(R.layout.detailed_item, detailed_class_list, false);
        TextView mVal = ((TextView) mFio.findViewById(R.id.detailed_item_value));

        if(!isEditMode) {
            mVal.setOnClickListener(mLectClickListener);
            mVal.setText(mLesson.m_teacher.m_fio);
        } else {
            mVal.setOnClickListener(mLectChooseListener);
            mVal.setText(mEditLesson.m_teacher.m_fio);
        }
        TextView mTitle = ((TextView) mFio.findViewById(R.id.detailed_item_title));
        mTitle.setText(titles[0]);
        detailed_class_list.addView(mFio);

        //        #2
        mGroups = inflater.inflate(R.layout.detailed_item_list, detailed_class_list, false);
        ((TextView) mGroups.findViewById(R.id.detailed_item_list_title)).

        setText(titles[1]);

        mGroupList = (ListView) mGroups.findViewById(R.id.detailed_item_list_list);

        List<GroupInfo> listGroupInfo;
        if(isEditMode)
            listGroupInfo = mEditLesson.m_list_groups;
        else
            listGroupInfo = mLesson.m_list_groups;

        mGroupListAdapter = new ArrayAdapter<GroupInfo>(inflater.getContext(), R.layout.detailed_item_list_item, listGroupInfo) {
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
                            getTotalHeightofListView(mGroupList);
                        }
                    });
                }
                return holder;
            }
        };

        mGroupList.setAdapter(mGroupListAdapter);

        getTotalHeightofListView(mGroupList);

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
                                    mGroupListAdapter.notifyDataSetChanged();
                                    getTotalHeightofListView(mGroupList);
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
        List<RegLessonInstance> listInst;
        if(isEditMode)
            listInst = mEditLesson.getAllLessonInstances();
        else
            listInst = mLesson.getAllLessonInstances();

        mInstListAdapter = new ArrayAdapter<RegLessonInstance>(inflater.getContext(), R.layout.detailed_item_list_item, listInst) {
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
                            mEditLesson.m_reg.get(rli.m_day).remove(rli);
                            mInstListAdapter.notifyDataSetChanged();
                            getTotalHeightofListView(mInstList);
                        }
                    });
                }
                return holder;
            }
        };
        mInstList.setAdapter(mInstListAdapter);
        getTotalHeightofListView(mInstList);
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
                    dialogBuilder.setPositiveButton(R.string.btn_ok, null);
                    dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //pass
                        }
                    });
                    final AlertDialog dialog = dialogBuilder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface d) {

                            ((TimePicker)dialogView.findViewById(R.id.newn_start_val)).setIs24HourView(true);
                            ((TimePicker)dialogView.findViewById(R.id.newn_end_val)).setIs24HourView(true);

                            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            b.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {

                                    RegLessonInstance rli = new RegLessonInstance(mEditLesson);

                                    rli.m_day = ((Spinner) dialogView.findViewById(R.id.newn_day_val)).getSelectedItemPosition();
                                    rli.m_type = ((Spinner) dialogView.findViewById(R.id.newn_type_val)).getSelectedItemPosition();
                                    rli.m_weekly = RegLessonInstance.weekly_t.values()[((Spinner) dialogView.findViewById(R.id.newn_odd_val)).getSelectedItemPosition()];
                                    rli.m_buildingName = ((EditText)dialogView.findViewById(R.id.newn_build)).getText().toString();
                                    rli.m_roomName = ((EditText)dialogView.findViewById(R.id.newn_room)).getText().toString();
                                    rli.m_timeStart= String.valueOf(((TimePicker)dialogView.findViewById(R.id.newn_start_val)).getCurrentHour()) + ":" +
                                            String.valueOf(((TimePicker)dialogView.findViewById(R.id.newn_start_val)).getCurrentMinute());
                                    rli.m_timeEnd= String.valueOf(((TimePicker)dialogView.findViewById(R.id.newn_end_val)).getCurrentHour()) + ":" +
                                            String.valueOf(((TimePicker)dialogView.findViewById(R.id.newn_end_val)).getCurrentMinute());

                                    if(rli.m_buildingName.isEmpty()) {
                                        ((TextView)dialogView.findViewById(R.id.newn_message)).setText(getResources().getString(R.string.newn_wrn_bld));
                                        return;
                                    }
                                    if(rli.m_roomName.isEmpty()) {
                                        ((TextView)dialogView.findViewById(R.id.newn_message)).setText(getResources().getString(R.string.newn_wrn_room));
                                        return;
                                    }
                                    if(((TimePicker)dialogView.findViewById(R.id.newn_start_val)).getCurrentHour() > 18
                                            || ((TimePicker)dialogView.findViewById(R.id.newn_start_val)).getCurrentHour() < 8
                                            || ((TimePicker)dialogView.findViewById(R.id.newn_end_val)).getCurrentHour() > 18
                                            || ((TimePicker)dialogView.findViewById(R.id.newn_end_val)).getCurrentHour() < 8
                                            || ((TimePicker)dialogView.findViewById(R.id.newn_start_val)).getCurrentHour() >
                                            ((TimePicker)dialogView.findViewById(R.id.newn_end_val)).getCurrentHour()
                                            || (((TimePicker)dialogView.findViewById(R.id.newn_start_val)).getCurrentHour() ==
                                            ((TimePicker)dialogView.findViewById(R.id.newn_end_val)).getCurrentHour()
                                            && ((TimePicker)dialogView.findViewById(R.id.newn_start_val)).getCurrentMinute() >
                                            ((TimePicker)dialogView.findViewById(R.id.newn_end_val)).getCurrentMinute())
                                            ) {
                                        Log.d(TAG, ((TimePicker)dialogView.findViewById(R.id.newn_start_val)).getCurrentHour().toString());
                                        Log.d(TAG, ((TimePicker)dialogView.findViewById(R.id.newn_end_val)).getCurrentHour().toString());
                                        ((TextView)dialogView.findViewById(R.id.newn_message)).setText(getResources().getString(R.string.newn_wrn_time));
                                        return;
                                    }

                                    if(mEditLesson.m_reg.containsKey(rli.m_day))
                                        mEditLesson.m_reg.get(rli.m_day).add(rli);
                                    else {
                                        mEditLesson.m_reg.put(rli.m_day, new ArrayList<RegLessonInstance>());
                                        mEditLesson.m_reg.get(rli.m_day).add(rli);
                                    }
                                    mInstListAdapter.clear();
                                    mInstListAdapter.addAll(mEditLesson.getAllLessonInstances());
                                    mInstListAdapter.notifyDataSetChanged();
                                    getTotalHeightofListView(mInstList);

                                    dialog.dismiss();
                                }
                            });
                        }
                    });
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
            ((MainNavigationDrawer)getActivity()).switchContent(MyDetailedObjFragment.newInstance(tmpGroup));
//            new ServerGetTable(tmpGroup, getActivity()).execute();
        }

    };

    private View.OnClickListener mLectChooseListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if(StaticStorage.m_primatLectName.isEmpty()) {
                new ServerGetPrimatLecturers(getActivity(), false).execute();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getActivity().getResources().getString(R.string.str_depth));
            builder.setIcon(R.drawable.logo_amd_mod);
//                        ListView modeList = new ListView(getActivity());
            final ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, android.R.id.text1,
                    StaticStorage.m_primatLectName);
            builder.setAdapter(modeAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String str = StaticStorage.m_primatLectName.get(which);
                            mEditLesson.m_teacher.m_fio = str;
                            ((TextView) mFio.findViewById(R.id.detailed_item_value)).setText(str);
                            dialog.dismiss();
                        }
                    });
            final Dialog dialog = builder.create();
            dialog.show();

        }

    };

    private View.OnClickListener mLectClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if(mLesson.m_teacher.m_fio != "Not set" && mLesson.m_teacher.m_fio != "Не задан")
//                new ServerGetTable(mLesson.m_teacher, getActivity()).execute();
//                new ServerGetLecturers(getActivity()).execute(mLesson.m_teacher.m_fio);
            ((MainNavigationDrawer)getActivity()).switchContent(MyDetailedObjFragment.newInstance(mLesson.m_teacher));
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