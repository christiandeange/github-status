package com.deange.githubstatus.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.deange.githubstatus.R;
import com.deange.githubstatus.http.GithubApi;
import com.deange.githubstatus.http.GsonRequest;
import com.deange.githubstatus.model.Status;
import com.deange.githubstatus.ui.view.AutoScaleTextView;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    public static final String TAG = MainFragment.class.getSimpleName();

    private RequestQueue mQueue;
    private StatusHandler mStatusHandler;
    private MessagesHandler mMessagesHandler;
    private ViewGroup mContentLayout;
    private ViewGroup mProgressLayout;
    private TextView mLoadingView;
    private TextView mNothingView;

    private Status mStatus;
    private List<Status> mMessages;

    private static final Object sTag = new Object();

    private MessagesAdapter mAdapter;
    private ListView mListview;
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
        mStatusHandler = new StatusHandler();
        mMessagesHandler = new MessagesHandler();

        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_main, null);

        mStatusView = (AutoScaleTextView) view.findViewById(R.id.fragment_status_text);
        mListview = (ListView) view.findViewById(R.id.fragment_messages_list_view);

        mContentLayout = (ViewGroup) view.findViewById(R.id.fragment_main_content);
        mProgressLayout = (ViewGroup) view.findViewById(R.id.fragment_progress_layout);
        mLoadingView = (TextView) view.findViewById(R.id.loading_messages_view);
        mNothingView = (TextView) view.findViewById(R.id.no_messages_view);

        mListview.setDivider(null);
        mListview.setAdapter(mAdapter);

        updateVisibility();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mStatus == null) {
            setStatus(Status.getSpecialStatus(getActivity(), Status.SpecialType.LOADING));
            queryForStatus();

        } else {
            setStatus(mStatus);
        }

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
        final Request<Status> request = new GsonRequest<Status>(
                GithubApi.STATUS, Status.class, null, mStatusHandler, mStatusHandler);
        request.setTag(sTag);
        request.setShouldCache(false);

        mQueue.add(request);
    }

    private void queryForMessages() {
        final Type type = new TypeToken<List<Status>>() {}.getType();
        final Request<List<Status>> request = new GsonRequest<List<Status>>(
                GithubApi.LAST_MESSAGES, type, null, mMessagesHandler, mMessagesHandler);

        request.setTag(sTag);
        request.setShouldCache(false);

        mQueue.add(request);
    }

    private void updateVisibility() {

        final boolean someDataLoaded = (mStatus != null) || (mMessages != null);

        ViewUtils.setVisibility(mContentLayout, someDataLoaded);
        ViewUtils.setVisibility(mProgressLayout, !someDataLoaded);

        ViewUtils.setVisibility(mLoadingView, mMessages == null);
    }

    private void setStatus(final Status status) {
        mStatus = status;
        mStatus.calculateVersion();

        mStatusView.setTextColor(ViewUtils.resolveStatusColour(getActivity(), status));
        mStatusView.setText(status.getStatus().toUpperCase());

        updateVisibility();
    }

    private void setMessages(final List<Status> response) {
        mMessages = response;
        mAdapter.refresh(response);

        updateVisibility();

        ViewUtils.setVisibility(mNothingView, (mMessages == null || mMessages.isEmpty()));
    }

    private class StatusHandler implements Response.ErrorListener, Response.Listener<Status> {

        @Override
        public void onErrorResponse(VolleyError error) {
            setStatus(Status.getSpecialStatus(getActivity(), Status.SpecialType.ERROR));
        }

        @Override
        public void onResponse(Status response) {
            setStatus(response);
        }
    }

    private class MessagesHandler implements Response.ErrorListener, Response.Listener<List<Status>> {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.v("TAG", error.toString());
            error.printStackTrace();
            setMessages(new ArrayList<Status>());
        }

        @Override
        public void onResponse(List<Status> response) {
            setMessages(response);
        }
    }

}
