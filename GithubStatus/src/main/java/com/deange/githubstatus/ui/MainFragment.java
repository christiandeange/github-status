package com.deange.githubstatus.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.deange.githubstatus.R;
import com.deange.githubstatus.controller.NotificationController;
import com.deange.githubstatus.controller.StateController;
import com.deange.githubstatus.http.GithubApi;
import com.deange.githubstatus.http.HttpTask;
import com.deange.githubstatus.model.Status;
import com.deange.githubstatus.ui.view.AutoScaleTextView;

import java.util.ArrayList;
import java.util.List;

public class MainFragment
        extends Fragment {

    public static final String TAG = MainFragment.class.getSimpleName();

    private ViewGroup mContentLayout;
    private ViewGroup mProgressLayout;
    private TextView mLoadingView;
    private TextView mNothingView;

    private Status mStatus;
    private List<Status> mMessages;

    private MessagesAdapter mAdapter;
    private AutoScaleTextView mStatusView;

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

        final ListView listview = (ListView) view.findViewById(R.id.fragment_messages_list_view);
        listview.setDivider(null);
        listview.setAdapter(mAdapter);

        updateVisibility();

        return view;
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

    public void refresh() {
        queryForStatus();
        queryForMessages();
    }

    private void queryForStatus() {

        GithubApi.getStatus(getActivity(), new HttpTask.Listener<Status>() {
            @Override
            public void onGet(final Status entity, final Exception exception) {

                if (exception != null) {
                    setStatus(Status.getSpecialStatus(getActivity(), Status.SpecialType.ERROR));

                } else {
                    setStatus(entity);
                }

            }
        });

    }

    private void queryForMessages() {

        GithubApi.getMessages(getActivity(), new HttpTask.Listener<List<Status>>() {
            @Override
            public void onGet(final List<Status> entity, final Exception exception) {

                if (exception != null) {
                    setMessages(new ArrayList<Status>());

                } else {
                    setMessages(entity);
                }

            }
        });

    }

    private void updateVisibility() {

        final boolean someDataLoaded = (mStatus != null) || (mMessages != null);

        ViewUtils.setVisibility(mContentLayout, someDataLoaded);
        ViewUtils.setVisibility(mProgressLayout, !someDataLoaded);

        ViewUtils.setVisibility(mLoadingView, mMessages == null);
    }

    private void setStatus(final Status status) {
        mStatus = status;

        mStatusView.setTextColor(ViewUtils.resolveStatusColour(getActivity(), status));
        mStatusView.setText(Status.getTranslatedStatus(getActivity(), status).toUpperCase());

        StateController.getInstance().setStatus(status);

        updateVisibility();
    }

    private void setMessages(final List<Status> response) {
        mMessages = response;
        mAdapter.refresh(response);

        updateVisibility();

        ViewUtils.setVisibility(mNothingView, (mMessages == null || mMessages.isEmpty()));
    }

}
