package edu.amd.spbstu.polystudenttimetable;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by artem on 2/23/16.
 */
public class SearchTabFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private View mRootView;
    private ViewPagerAdapter adapter;
    private SearchFragment mGroupFrag, mLectFrag;
    private static final String TAG = "polytable_log";
    public SearchTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG", "tab search fragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mRootView == null)
            mRootView = inflater.inflate(R.layout.tab_search, container, false);

        if(tabLayout == null) {
            tabLayout = (TabLayout) mRootView.findViewById(R.id.sliding_tabs);
            tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.groups)));
            tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.lect)));
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        }

        viewPager = (ViewPager) mRootView.findViewById(R.id.viewpager);
        viewPager.removeAllViews();
        adapter = new ViewPagerAdapter(getChildFragmentManager());
        Log.d("TAG", String.valueOf(adapter.getCount()));

        if(mGroupFrag == null)
            mGroupFrag = SearchFragment.newInstance("", 0);
        if(mLectFrag == null)
            mLectFrag = SearchFragment.newInstance("", 1);

        adapter.addFragment(mGroupFrag, getResources().getString(R.string.groups));
        adapter.addFragment(mLectFrag, getResources().getString(R.string.lect));

        Log.d("TAG", String.valueOf(adapter.getCount()));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

/*        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
*/
        return mRootView;

    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(android.app.Fragment fragment, String title) {
            mFragmentList.add((Fragment)fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
