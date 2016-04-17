package edu.amd.spbstu.polystudenttimetable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyDetailedObjFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyDetailedObjFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyDetailedObjFragment extends Fragment {
    private static final String ARG_PARAM = "param1";

    // TODO: Rename and change types of parameters
    static final String PREF_LES_COL= "poly_table_lesson_color";

    private static final String TAG = "polytable_log";
    private GroupInfo mGroup;
    private LecturerInfo mLect;
    private ContactInfo mContact;
    private ContactInfo mEditContact;

    boolean isEditMode = false;
    private ListView mGroupList;
    private ListView mInstList;
    private ArrayAdapter mGroupListAdapter;
    private ArrayAdapter mInstListAdapter;
    private OnFragmentInteractionListener mListener;

    private View view;
    private View mNote;
    private View mEmail;
    private View mPhone;
    private View mWebsite;

    FloatingActionButton myFab;

    /**
     * @param param Lesson parameter
     * @return A new instance of fragment DetailedClassFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyDetailedObjFragment newInstance(LecturerInfo param) {
        MyDetailedObjFragment fragment = new MyDetailedObjFragment();
        Bundle args = new Bundle();
        args.putSerializable("lecturer", param);
        fragment.setArguments(args);
        return fragment;
    }
    public static MyDetailedObjFragment newInstance(GroupInfo param) {
        MyDetailedObjFragment fragment = new MyDetailedObjFragment();
        Bundle args = new Bundle();
        args.putSerializable("group", param);
        fragment.setArguments(args);
        return fragment;
    }

    public MyDetailedObjFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGroup = (GroupInfo)getArguments().getSerializable("group");
            mLect = (LecturerInfo)getArguments().getSerializable("lecturer");
        } else if (savedInstanceState != null) {
            mGroup = (GroupInfo) savedInstanceState.getSerializable("group");
            mLect = (LecturerInfo) savedInstanceState.getSerializable("lecturer");
        }
        if(mGroup != null) {
            mContact = mGroup.m_contact;
        } else if(mLect != null) {
            mContact = mLect.m_contact;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the help for this fragment

        if(view == null)
            view = inflater.inflate(R.layout.fragment_detailed, container, false);

        TextView collapsingToolbar =
                (TextView) view.findViewById(R.id.detailed_title);
        if(mGroup != null) {
            collapsingToolbar.setText(mGroup.m_name);
        } else if(mLect != null) {
            collapsingToolbar.setText(mLect.m_fio);
        }

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

                    mEditContact.m_note = ((EditText) mNote.findViewById(R.id.detailed_item_value_edit)).getText().toString();
                    mEditContact.m_phone = ((EditText) mPhone.findViewById(R.id.detailed_item_value_edit)).getText().toString();
                    mEditContact.m_email = ((EditText) mEmail.findViewById(R.id.detailed_item_value_edit)).getText().toString();
                    mEditContact.m_site = ((EditText) mWebsite.findViewById(R.id.detailed_item_value_edit)).getText().toString();

                    mContact = mEditContact;
                    if(mGroup != null) {
                        mGroup.m_contact = mEditContact;
                    } else {
                        mLect.m_contact = mEditContact;
                    }

                    MainNavigationDrawer act = ((MainNavigationDrawer)getActivity());
                    List<Lesson> list;
                    if(act.isGroup())
                        list = ((Group)act.the_obj).m_listLessons;
                    else
                        list = ((Lecturer)act.the_obj).m_listLessons;

                    if(mGroup != null) {
                        for (Lesson lesson : list) {
                            int i = lesson.m_list_groups.indexOf(mGroup);
                            if(i != -1) {
                                lesson.m_list_groups.set(i, mGroup);
                                ((MainNavigationDrawer)getActivity()).write(lesson);
                            }
                        }
                    } else if(mLect != null) {
                        for (Lesson lesson : list) {
                            if(lesson.m_teacher.m_fio.equals(mLect.m_fio)) {
                                lesson.m_teacher = mLect;
                                ((MainNavigationDrawer)getActivity()).write(lesson);
                            }
                        }
                    }
                    init();
                } else {
                    Log.d(TAG, "edit");
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(mContact);
                        oos.flush();
                        oos.close();

                        ObjectInputStream in = new ObjectInputStream(
                                new ByteArrayInputStream(bos.toByteArray()));
                        mEditContact = (ContactInfo) in.readObject();
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

        String[] titles = getResources().getStringArray(R.array.contact_details);

        //        #1
        mNote = inflater.inflate(R.layout.detailed_item, detailed_class_list, false);
        TextView mVal = ((TextView) mNote.findViewById(R.id.detailed_item_value));
        EditText mValEdit = ((EditText) mNote.findViewById(R.id.detailed_item_value_edit));

        if (!isEditMode) {
            mVal.setVisibility(View.VISIBLE);
            mValEdit.setVisibility(View.GONE);
            mVal.setText(mContact.m_note);
        } else {
            mVal.setVisibility(View.GONE);
            mValEdit.setVisibility(View.VISIBLE);
            mValEdit.setText(mEditContact.m_note);
        }
        TextView mTitle = ((TextView) mNote.findViewById(R.id.detailed_item_title));
        mTitle.setText(titles[0]);
        detailed_class_list.addView(mNote);

        //        #2
        mPhone = inflater.inflate(R.layout.detailed_item, detailed_class_list, false);
        final TextView mVal2 = ((TextView) mPhone.findViewById(R.id.detailed_item_value));
        mValEdit = ((EditText) mPhone.findViewById(R.id.detailed_item_value_edit));

        mVal2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", mVal2.getText());
                clipboard.setPrimaryClip(clip);
                Snackbar snackbar = Snackbar
                        .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.copied), Snackbar.LENGTH_LONG);
                snackbar.show();

            }
        });

        if (!isEditMode) {
            mVal2.setVisibility(View.VISIBLE);
            mValEdit.setVisibility(View.GONE);
            mVal2.setText(mContact.m_phone);
        } else {
            mValEdit.setInputType(InputType.TYPE_CLASS_PHONE);
            mVal2.setVisibility(View.GONE);
            mValEdit.setVisibility(View.VISIBLE);
            mValEdit.setText(mEditContact.m_phone);
        }
        mTitle = ((TextView) mPhone.findViewById(R.id.detailed_item_title));
        mTitle.setText(titles[1]);
        detailed_class_list.addView(mPhone);

        //        #3
        mEmail = inflater.inflate(R.layout.detailed_item, detailed_class_list, false);
        final TextView mVal3 = ((TextView) mEmail.findViewById(R.id.detailed_item_value));
        mValEdit = ((EditText) mEmail.findViewById(R.id.detailed_item_value_edit));

        mVal3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", mVal3.getText());
                clipboard.setPrimaryClip(clip);
                Snackbar snackbar = Snackbar
                        .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.copied), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });

        if (!isEditMode) {
            mVal3.setVisibility(View.VISIBLE);
            mValEdit.setVisibility(View.GONE);
            mVal3.setText(mContact.m_email);
        } else {
            mValEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            mVal3.setVisibility(View.GONE);
            mValEdit.setVisibility(View.VISIBLE);
            mValEdit.setText(mEditContact.m_email);
        }
        mTitle = ((TextView) mEmail.findViewById(R.id.detailed_item_title));
        mTitle.setText(titles[2]);
        detailed_class_list.addView(mEmail);

        //        #4
        mWebsite = inflater.inflate(R.layout.detailed_item, detailed_class_list, false);
        final TextView mVal4 = ((TextView) mWebsite.findViewById(R.id.detailed_item_value));
        mValEdit = ((EditText) mWebsite.findViewById(R.id.detailed_item_value_edit));

        mVal4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", mVal4.getText());
                clipboard.setPrimaryClip(clip);
                Snackbar snackbar = Snackbar
                        .make(getActivity().findViewById(R.id.main_coord_layout), getResources().getString(R.string.copied), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });

        if (!isEditMode) {
            mVal4.setVisibility(View.VISIBLE);
            mValEdit.setVisibility(View.GONE);
            mVal4.setText(mContact.m_site);
        } else {
            mVal4.setVisibility(View.GONE);
            mValEdit.setVisibility(View.VISIBLE);
            mValEdit.setText(mEditContact.m_site);
        }
        mTitle = ((TextView) mWebsite.findViewById(R.id.detailed_item_title));
        mTitle.setText(titles[3]);
        detailed_class_list.addView(mWebsite);


        //        #5

        if (!isEditMode) {
            Button btn = new Button(getActivity());
            btn.setText(getString(R.string.gotott));
            btn.setBackgroundColor(getResources().getColor(R.color.cool_green));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mGroup != null) {
                        new ServerGetTable(mGroup, getActivity()).execute();
                    } else if(mLect != null){
                        new ServerGetTable(mLect, getActivity()).execute();
                    }
                }
            });
            detailed_class_list.addView(btn);
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