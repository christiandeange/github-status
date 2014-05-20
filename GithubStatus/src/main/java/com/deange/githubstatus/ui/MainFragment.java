package com.deange.githubstatus.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.deange.githubstatus.R;
import com.deange.githubstatus.http.GithubApi;
import com.deange.githubstatus.http.HttpTask;
import com.deange.githubstatus.model.Status;
import com.deange.githubstatus.ui.view.AutoScaleTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainFragment
        extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {

    public static final String TAG = MainFragment.class.getSimpleName();
    private static final long MINIMUM_UPDATE_DURATION = 1000;
    private static final int TOTAL_COMPONENTS = 2;

    private SwipeRefreshLayout mSwipeLayout;
    private TextView mLoadingView;
    private TextView mNothingView;
    private ListView mListView;
    private AutoScaleTextView mStatusView;

    private Status mStatus;
    private List<Status> mMessages;

    private MessagesAdapter mAdapter;

    private final AtomicInteger mComponentsLoaded = new AtomicInteger();
    private final Handler mHandler = new Handler();
    private long mLastUpdate = 0;

    public static MainFragment newInstance() {
        final MainFragment fragment = new MainFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        mAdapter = new MessagesAdapter(getActivity(), R.layout.list_item_message);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView()");

        final ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_main, null);

        mStatusView = (AutoScaleTextView) view.findViewById(R.id.fragment_status_text);
        mLoadingView = (TextView) view.findViewById(R.id.loading_messages_view);
        mNothingView = (TextView) view.findViewById(R.id.no_messages_view);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorScheme(
                R.color.status_good,
                android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_dark);

        mListView = (ListView) view.findViewById(R.id.fragment_messages_list_view);
        mListView.setDivider(null);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);

        updateVisibility();

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);

        // Store this result ahead of time since the status and messages references
        // may point to intermediary (ie: pending) results
        final boolean shouldRefresh = (mStatus == null || mMessages == null);

        // Refresh status view
        mStatus = (mStatus == null) ? Status.getSpecialStatus(getActivity(), Status.SpecialType.LOADING) : mStatus;
        setStatus(mStatus);

        // Refresh messages view
        mMessages = (mMessages == null) ? new ArrayList<Status>() : mMessages;
        setMessages(mMessages);

        // Continue loading status info if necessary
        if (shouldRefresh) {
            refresh();
        }

    }

    @Override
    public void onRefresh() {
        // Called by SwipeRefreshLayout
        Log.v(TAG, "onRefresh()");

        refresh();
    }

    private void resetFieldsForRefresh() {
        mSwipeLayout.setRefreshing(true);
        mComponentsLoaded.set(0);
        mLastUpdate = System.currentTimeMillis();
    }

    public void refresh() {

        resetFieldsForRefresh();

        queryForStatus();
        queryForMessages();
    }

    private void queryForStatus() {

        GithubApi.getStatus(getActivity(), GithubApi.LAST_MESSAGE, new HttpTask.Listener<Status>() {
            @Override
            public void onGet(final Status entity, final Exception exception) {

                mComponentsLoaded.incrementAndGet();

                final Status status = (exception == null) ? entity : Status.getSpecialStatus(getActivity(), Status.SpecialType.ERROR);
                setStatus(status);
            }
        });
    }

    private void queryForMessages() {

        GithubApi.getMessages(getActivity(), GithubApi.LAST_MESSAGES, new HttpTask.Listener<List<Status>>() {
            @Override
            public void onGet(final List<Status> entity, final Exception exception) {

                mComponentsLoaded.incrementAndGet();

                final List<Status> statuses = (exception == null) ? entity : new ArrayList<Status>();
                setMessages(statuses);
            }
        });
    }

    private void updateVisibility() {

        final boolean allDataLoaded  = mComponentsLoaded.get() == TOTAL_COMPONENTS;

        ViewUtils.setVisibility(mLoadingView, mMessages == null);
        ViewUtils.setVisibility(mNothingView, (mMessages == null || mMessages.isEmpty()));

        if (allDataLoaded) {

            // We want to keep the refresh UI up for *at least* MINIMUM_UPDATE_DURATION
            // Otherwise it looks very choppy and overall not a pleasant look
            final long now = System.currentTimeMillis();
            final long delay = MINIMUM_UPDATE_DURATION - (now - mLastUpdate);
            mLastUpdate = 0;

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeLayout.setRefreshing(false);
                }
            }, delay);
        }
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {

        if (view == mListView) {
            int topRowVerticalPosition = (mListView == null || mListView.getChildCount() == 0)
                    ? 0 : mListView.getChildAt(0).getTop();
            mSwipeLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
        }
    }

    private void setStatus(final Status status) {

        mStatus = (status == null) ? Status.getSpecialStatus(getActivity(), Status.SpecialType.ERROR) : status;

        mStatusView.setTextColor(ViewUtils.resolveStatusColour(getActivity(), mStatus));
        mStatusView.setText(mStatus.getTranslatedStatus(getActivity()).toUpperCase());

        updateVisibility();
    }

    private void setMessages(final List<Status> response) {

        mMessages = (response == null) ? new ArrayList<Status>() : response;

        mAdapter.refresh(mMessages);

        updateVisibility();
    }
}
