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

    private SwipeRefreshLayout mSwipeLayout;
    private ViewGroup mContentLayout;
    private ViewGroup mProgressLayout;
    private TextView mLoadingView;
    private TextView mNothingView;
    private ListView mListview;

    private Status mStatus;
    private List<Status> mMessages;

    private MessagesAdapter mAdapter;
    private AutoScaleTextView mStatusView;

    private static final int TOTAL_COMPONENTS = 2;
    private final AtomicInteger mComponentsLoaded = new AtomicInteger();

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new MessagesAdapter(getActivity(), R.layout.list_item_message);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_main, null);

        mStatusView = (AutoScaleTextView) view.findViewById(R.id.fragment_status_text);
        mContentLayout = (ViewGroup) view.findViewById(R.id.fragment_main_content);
        mProgressLayout = (ViewGroup) view.findViewById(R.id.fragment_progress_layout);
        mLoadingView = (TextView) view.findViewById(R.id.loading_messages_view);
        mNothingView = (TextView) view.findViewById(R.id.no_messages_view);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorScheme(
                R.color.status_good,
                android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_dark);

        mListview = (ListView) view.findViewById(R.id.fragment_messages_list_view);
        mListview.setDivider(null);
        mListview.setAdapter(mAdapter);
        mListview.setOnScrollListener(this);

        updateVisibility();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Refresh status view
        if (mStatus == null) {
            setStatus(Status.getSpecialStatus(getActivity(), Status.SpecialType.LOADING));
            queryForStatus();

        } else {
            setStatus(mStatus);
        }

        // Refresh messages view
        if (mMessages == null) {
            queryForMessages();

        } else {
            setMessages(mMessages);
        }

        updateVisibility();

    }

    @Override
    public void onRefresh() {
        // Called by SwipeRefreshLayout
        Log.v(TAG, "onRefresh()");

        refresh();
    }

    public void refresh() {

        mSwipeLayout.setRefreshing(true);
        mComponentsLoaded.set(0);

        queryForStatus();
        queryForMessages();
    }

    private void queryForStatus() {

        GithubApi.getStatus(getActivity(), GithubApi.LAST_MESSAGE, new HttpTask.Listener<Status>() {
            @Override
            public void onGet(final Status entity, final Exception exception) {

                mComponentsLoaded.incrementAndGet();

                if (exception != null) {
                    setStatus(Status.getSpecialStatus(getActivity(), Status.SpecialType.ERROR));

                } else {
                    setStatus(entity);
                }

            }
        });

    }

    private void queryForMessages() {

        GithubApi.getMessages(getActivity(), GithubApi.LAST_MESSAGES, new HttpTask.Listener<List<Status>>() {
            @Override
            public void onGet(final List<Status> entity, final Exception exception) {

                mComponentsLoaded.incrementAndGet();

                if (exception != null) {
                    setMessages(new ArrayList<Status>());

                } else {
                    setMessages(entity);
                }

            }
        });

    }

    private void updateVisibility() {

        final boolean someDataLoaded = mComponentsLoaded.get() > 0;
        final boolean allDataLoaded  = mComponentsLoaded.get() == TOTAL_COMPONENTS;

        ViewUtils.setVisibility(mContentLayout, someDataLoaded);
        ViewUtils.setVisibility(mProgressLayout, !someDataLoaded);

        ViewUtils.setVisibility(mLoadingView, mMessages == null);

        if (allDataLoaded) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeLayout.setRefreshing(false);
                }
            }, 2000);

//            mSwipeLayout.setRefreshing(false);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int topRowVerticalPosition = (mListview == null || mListview.getChildCount() == 0)
                ? 0 : mListview.getChildAt(0).getTop();
        mSwipeLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
    }

    private void setStatus(final Status status) {
        mStatus = status;
        mStatus.calculateVersion();

        mStatusView.setTextColor(ViewUtils.resolveStatusColour(getActivity(), status));
        mStatusView.setText(status.getTranslatedStatus(getActivity()).toUpperCase());

        updateVisibility();
    }

    private void setMessages(final List<Status> response) {
        mMessages = response;
        mAdapter.refresh(response);

        updateVisibility();

        ViewUtils.setVisibility(mNothingView, (mMessages == null || mMessages.isEmpty()));
    }
}
