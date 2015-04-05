package com.deange.githubstatus.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.widget.ViewSwitcher;

import com.deange.githubstatus.R;
import com.deange.githubstatus.Utils;
import com.deange.githubstatus.controller.GsonController;
import com.deange.githubstatus.http.GithubApi;
import com.deange.githubstatus.http.HttpTask;
import com.deange.githubstatus.model.Status;
import com.deange.githubstatus.ui.view.SliceView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainFragment
        extends Fragment
        implements
        SwipeRefreshLayout.OnRefreshListener,
        AbsListView.OnScrollListener,
        ViewSwitcher.ViewFactory {

    public static final String TAG = MainFragment.class.getSimpleName();

    private static final String KEY_STATUS = TAG + ".status";
    private static final long MINIMUM_UPDATE_DURATION = 1000;
    private static final int TOTAL_COMPONENTS = 2;

    private View mView;
    private SwipeRefreshLayout mSwipeLayout;
    private SliceView mSliceView;
    private TextView mLoadingView;
    private TextView mNothingView;
    private ListView mListView;

    private ViewSwitcher mStatusView;
    private Status mStatus;

    private List<Status> mMessages;

    private MessagesAdapter mAdapter;
    private final AtomicInteger mComponentsLoaded = new AtomicInteger();
    private final Handler mHandler = new Handler();
    private long mLastUpdate = 0;
    private ValueAnimator mAnimator;
    private int mColour;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public MainFragment() {
        super();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        mAdapter = new MessagesAdapter(getActivity(), R.layout.list_item_message);

        if (savedInstanceState != null) {
            mStatus = GsonController.getInstance().fromJson(
                    savedInstanceState.getString(KEY_STATUS, null), Status.class);
        }
        if (mStatus == null) {
            mStatus = Status.getSpecialStatus(getActivity(), Status.SpecialType.LOADING);
        }
    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView()");

        mView = inflater.inflate(R.layout.fragment_main, null);

        mStatusView = (ViewSwitcher) mView.findViewById(R.id.fragment_status_text_flipper);
        mStatusView.setFactory(this);
        mLoadingView = (TextView) mView.findViewById(R.id.loading_messages_view);
        mNothingView = (TextView) mView.findViewById(R.id.no_messages_view);

        mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.fragment_swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.status_good);

        mSliceView = (SliceView) mView.findViewById(R.id.fragment_slice_view);

        mListView = (ListView) mView.findViewById(R.id.fragment_messages_list_view);
        mListView.setDivider(null);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);

        updateVisibility();

        return mView;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        Log.v(TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);

        if (mSliceView != null) {
            final double angle = Math.toDegrees(Math.atan2(
                    mSliceView.getSliceHeight(),
                    getResources().getDisplayMetrics().widthPixels));
            mStatusView.setRotation((float) angle);

            mStatusView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(
                        final View v,
                        final int left, final int top,
                        final int right, final int bottom,
                        final int oldLeft, final int oldTop,
                        final int oldRight, final int oldBottom) {

                    mStatusView.setPivotX(0);
                    mStatusView.setPivotY(mStatusView.getMeasuredHeight());
                }
            });
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);

        // Store this result ahead of time since the status and messages references
        // may point to intermediary (ie: pending) results
        final boolean shouldRefresh = (mStatus == null || mMessages == null);

        // Refresh status view
        mStatus = (mStatus == null)
                ? Status.getSpecialStatus(getActivity(), Status.SpecialType.LOADING)
                : mStatus;

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
    public void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putString(KEY_STATUS, GsonController.getInstance().toJson(mStatus));
        super.onSaveInstanceState(outState);
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

        GithubApi.getStatus(getActivity(), new HttpTask.Listener<Status>() {
            @Override
            public void onGet(final Status entity, final Exception exception) {

                mComponentsLoaded.incrementAndGet();

                final Status status = (exception == null)
                        ? entity
                        : Status.getSpecialStatus(getActivity(), Status.SpecialType.ERROR);
                setStatus(status);
            }
        });
    }

    private void queryForMessages() {

        GithubApi.getMessages(getActivity(), new HttpTask.Listener<List<Status>>() {
            @Override
            public void onGet(final List<Status> entity, final Exception exception) {

                mComponentsLoaded.incrementAndGet();

                final List<Status> statuses = (exception == null)
                        ? entity
                        : new ArrayList<Status>();
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
    public void onScroll(
            final AbsListView view,
            final int firstVisibleItem,
            final int visibleItemCount,
            final int totalItemCount) {

        if (view == mListView) {
            int topRowVerticalPosition = (mListView == null || mListView.getChildCount() == 0)
                    ? 0 : mListView.getChildAt(0).getTop();
            mSwipeLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
        }
    }

    private void setStatus(final Status status) {
        mStatus = (status == null)
                ? Status.getSpecialStatus(getActivity(), Status.SpecialType.ERROR)
                : status;

        mStatusView.setDisplayedChild(mStatusView.getDisplayedChild() == 0 ? 1 : 0);
        updateStatusView((TextView) mStatusView.getChildAt(mStatusView.getDisplayedChild()));
        updateVisibility();
    }

    private void setMessages(final List<Status> response) {
        mMessages = (response == null) ? new ArrayList<Status>() : response;
        mAdapter.refresh(mMessages);
        updateVisibility();
    }

    @Override
    public View makeView() {
        final TextView view = (TextView) LayoutInflater.from(getActivity()).inflate(
                R.layout.view_flipper_item, (ViewGroup) getView(), false);
        updateStatusView(view);
        return view;
    }

    private void animateColorFilter() {
        final int startColour = mColour;
        final int endColour = ViewUtils.resolveStatusColour(getActivity(), mStatus);
        final Drawable background = mView.getBackground();

        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = ObjectAnimator.ofInt(startColour, endColour);
        mAnimator.setEvaluator(new ArgbEvaluator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                mColour = (Integer) animation.getAnimatedValue();
                background.setColorFilter(mColour, PorterDuff.Mode.SRC_ATOP);
            }
        });

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                mAnimator = null;
            }
        });

        mAnimator.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        mAnimator.start();
    }

    private void updateStatusView(final TextView view) {
        if (Utils.showNiceView(getActivity())) {
            animateColorFilter();

        } else {
            view.setTextColor(ViewUtils.resolveStatusColour(getActivity(), mStatus));
        }

        view.setText(Status.getTranslatedStatus(getActivity(), mStatus).toUpperCase());
    }
}
