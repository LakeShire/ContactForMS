package com.example.contacts;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.adapter.ContactAdapter;
import com.example.contacts.adapter.InfoAdapter;
import com.example.contacts.manager.ContactManager;
import com.example.contacts.model.Contact;
import com.example.contacts.util.BaseUtil;
import com.example.contacts.util.StatusUtil;
import com.example.contacts.view.FixLinearLayoutManager;
import com.example.contacts.view.OnViewPagerListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;

public class MainActivity extends AppCompatActivity implements ContactManager.Callback {

    private static final String EXTRA_POSITION = "extra_position";
    private RecyclerView mRvContacts;
    private RecyclerView mRvInfo;
    private ContactAdapter mContactAdapter;
    private List<Contact> mContactList = new ArrayList<>();
    private List<Contact> mInfoList = new ArrayList<>();
    private InfoAdapter mInfoAdapter;
    private OnViewPagerListener mContactListener;
    private OnViewPagerListener mInfoListener;
    private int mInfoHeight;
    private int mContactSize;
    private int mDummyCount;
    private RecyclerView.OnScrollListener mContactScrollListener;
    private RecyclerView.OnScrollListener mInfoScrollListener;
    private int mOrientation;
    private int mAvailableHeight;
    private int mPosition = -1;
    private int mScreenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // Immersive Status Bar
        StatusUtil.setStatusBarColor(this, R.color.color_title_bar);
        StatusUtil.setLightStatusBar(this, true);

        // Recover the state when recreated
        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(EXTRA_POSITION, -1);
        }

        // Portrait UI is not easy to use for when the screen is landscape
        // calculate contact avatar size for each orientation to make the screen just contain 3 items for landscape and 5 items for portrait
        // And the dummy item to add before and after data list will be 1 and 2
        Configuration config = this.getResources().getConfiguration();
        mOrientation = config.orientation;
        mAvailableHeight = (int) (BaseUtil.getScreenHeight(this) - getResources().getDimension(R.dimen.title_bar_height));
        if (mOrientation == ORIENTATION_LANDSCAPE) {
            mContactSize = mAvailableHeight / 3;
            mDummyCount = 1;
        } else {
            mContactSize = BaseUtil.getScreenWidth(this) / 5;
            mDummyCount = 2;
        }

        mScreenWidth = BaseUtil.getScreenWidth(this);

        ContactManager.getInstance().setCallback(this);
        initContacts();
        initInfo();
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ContactManager.getInstance().removeCallback();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(EXTRA_POSITION, mPosition);
        super.onSaveInstanceState(outState);
    }

    private void loadData() {
        ContactManager.getInstance().getContacts(this, false);
    }

    private void initInfo() {
        mRvInfo = findViewById(R.id.rv_info);
        final PagerSnapHelper helper = new PagerSnapHelper();
        // We use a PagerSnapHelper to slide the list one page one time

        mInfoListener = new OnViewPagerListener() {
            @Override
            public void onPageSelected(int position) {
                // when the page is selected, highlight the corresponding avatar
                // because the detail list don't need dummy item, so there is a position transform
                mPosition = position;
                highLight(position + mDummyCount);
//                adjustContactToCenter(position + mDummyCount);
            }
        };
        helper.attachToRecyclerView(mRvInfo);
        final RecyclerView.LayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRvInfo.setLayoutManager(lm);
        mInfoScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE) {
                    // when scrolling is done, we got current position from SnapHelper
                    mRvContacts.addOnScrollListener(mContactScrollListener);
                    View view = helper.findSnapView(lm);
                    if (view != null) {
                        int position = lm.getPosition(view);
                        if (mInfoListener != null) {
                            mInfoListener.onPageSelected(position);
                        }
                    }
                } else {
                    // when scrolling, remove the listener for avatar list or they will influence each other
                    // we add it back when idle
                    mRvContacts.removeOnScrollListener(mContactScrollListener);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // when the detail list is scrolled, the avatar list will be scrolled as well, using the same rate;
                float rate = ((float) dy) / mInfoHeight;
                int distance = (int) (mContactSize * rate);
                if (mOrientation == ORIENTATION_LANDSCAPE) {
                    mRvContacts.scrollBy(0, distance);
                } else {
                    mRvContacts.scrollBy(distance, 0);
                }
            }
        };
        mRvInfo.addOnScrollListener(mInfoScrollListener);
        mInfoAdapter = new InfoAdapter(this, mInfoList);
        mRvInfo.setAdapter(mInfoAdapter);
    }

    private void initContacts() {
        mRvContacts = findViewById(R.id.rv_contacts);
        final LinearSnapHelper helper = new LinearSnapHelper();
        mContactListener = new OnViewPagerListener() {
            @Override
            public void onPageSelected(int position) {
                mPosition = position - mDummyCount;
                highLight(position);
                mRvInfo.scrollToPosition(position - mDummyCount);
            }
        };
        final RecyclerView.LayoutManager lm = new FixLinearLayoutManager(this, mOrientation == ORIENTATION_LANDSCAPE ? RecyclerView.VERTICAL : RecyclerView.HORIZONTAL, false);
        mRvContacts.setLayoutManager(lm);
        helper.attachToRecyclerView(mRvContacts);
        mContactScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE) {
                    mRvInfo.addOnScrollListener(mInfoScrollListener);
                    View view = helper.findSnapView(lm);
                    if (view != null) {
                        int position = lm.getPosition(view);
                        if (mContactListener != null) {
                            mContactListener.onPageSelected(position);
                        }
                    }
                } else {
                    mRvInfo.removeOnScrollListener(mInfoScrollListener);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                float rate;
                if (mOrientation == ORIENTATION_LANDSCAPE) {
                    rate = ((float) dy) / mContactSize;
                } else {
                    rate = ((float) dx) / mContactSize;
                }
                int y = (int) (mInfoHeight * rate);
                mRvInfo.scrollBy(0, y);
            }
        };
        mRvContacts.addOnScrollListener(mContactScrollListener);
        mContactAdapter = new ContactAdapter(this, mContactList, mOrientation);
        mContactAdapter.setOnItemClickListener(new ContactAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                // highlight the avatar and move it to the center of screen
                highLight(position);
                adjustContactToCenter(position);
            }
        });
        mRvContacts.setAdapter(mContactAdapter);
    }

    private void adjustContactToCenter(int position) {
        RecyclerView.LayoutManager manager = mRvContacts.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            int firstPosition = ((LinearLayoutManager) manager).findFirstVisibleItemPosition();
            int lastPosition = ((LinearLayoutManager) manager).findLastCompletelyVisibleItemPosition();
//            int startP = position - firstPosition;
//            int endP = lastPosition - position;
//            if (startP >= 0 && startP < mRvContacts.getChildCount() && endP >= 0 && endP < mRvContacts.getChildCount()) {
//                if (mOrientation == ORIENTATION_LANDSCAPE) {
//                    int top = mRvContacts.getChildAt(startP).getTop();
//                    int bottom = mRvContacts.getChildAt(endP).getTop();
//                    mRvContacts.smoothScrollBy(0, (top - bottom) / 2, new LinearInterpolator(), 10000);
//                } else {
//                    int left = mRvContacts.getChildAt(startP).getLeft();
//                    int right = mRvContacts.getChildAt(endP).getLeft();
//                    int d = (left - right) / 2;
//                    mRvContacts.smoothScrollBy(d, 0, new LinearInterpolator(), 1000);
//                }
//            }
            int pos = position - firstPosition;
            View view = mRvContacts.getChildAt(pos);
            if (view != null) {
                if (mOrientation == ORIENTATION_LANDSCAPE) {
                    int offset = (view.getTop() + (view.getBottom() - view.getTop()) / 2) - mAvailableHeight / 2;
                    mRvContacts.smoothScrollBy(0, offset);
                } else {
                    int offset = (view.getLeft() + (view.getRight() - view.getLeft()) / 2) - mScreenWidth / 2;
                    mRvContacts.smoothScrollBy(offset, 0);
                }
            }
        }
    }

    private void highLight(int position) {
        for (Contact contact : mContactList) {
            contact.setSelected(false);
        }
        if (position >= 0 && position < mContactList.size()) {
            Contact contact = mContactList.get(position);
            if (contact != null) {
                contact.setSelected(true);
            }
        }
        mContactAdapter.notifyDataSetChanged();
    }

    @Override
    public void onContactsGot(List<Contact> contacts) {
        if (contacts != null) {
            mContactList.clear();

            // the recyclerview can't scroll the first position to the center of screen
            // it will make the selection difficult
            // so we add dummy item as a placeholder
            // if 5 items can displayed on screen, we add 2 dummy item, and the first item
            // will be just int the center
            for (int i = 0; i < mDummyCount; i++) {
                mContactList.add(new Contact(true));
            }
            mContactList.addAll(contacts);
            for (int i = 0; i < mDummyCount; i++) {
                mContactList.add(new Contact(true));
            }
            mContactAdapter.notifyDataSetChanged();

            mInfoList.clear();
            mInfoList.addAll(contacts);
            mInfoAdapter.notifyDataSetChanged();

            mRvContacts.post(new Runnable() {
                @Override
                public void run() {
                    RecyclerView.LayoutManager manager = mRvContacts.getLayoutManager();
                    if (manager instanceof LinearLayoutManager) {
                        if (mPosition >= 0) {
                            // recover the state
                            mRvContacts.scrollToPosition(mPosition);
                        } else {
                            // or scroll the list to put the first item in the center
                            if (mOrientation == ORIENTATION_LANDSCAPE) {
                                mRvContacts.scrollBy(0, mAvailableHeight / 2);
                            } else {
                                mRvContacts.scrollBy(BaseUtil.getScreenWidth(MainActivity.this) / 2, 0);
                            }
                        }
                        if (mOrientation == ORIENTATION_LANDSCAPE) {
                            // this is just a trick to notify the SnapHelper to work
                            // because I found the scrollBy method didn't work
                            mRvContacts.smoothScrollBy(0, 1);
                        } else {
                            mRvContacts.smoothScrollBy(1, 0);
                        }
                    }
                }
            });

            mRvInfo.post(new Runnable() {
                @Override
                public void run() {
                    RecyclerView.LayoutManager manager = mRvInfo.getLayoutManager();
                    if (manager instanceof LinearLayoutManager && !mInfoList.isEmpty()) {
                        View item = mRvInfo.getChildAt(0);
                        mInfoHeight = item.getBottom() - item.getTop();
                    }
                }
            });
        }
    }
}
