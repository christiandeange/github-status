package com.deange.githubstatus.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.deange.githubstatus.R;
import com.deange.githubstatus.http.GithubApi;
import com.deange.githubstatus.http.GsonRequest;
import com.deange.githubstatus.http.Status;
import com.deange.githubstatus.ui.view.AutoScaleTextView;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class MainFragment extends Fragment {

    public static final String TAG = MainFragment.class.getSimpleName();

    private RequestQueue mQueue;
    private StatusHandler mStatusHandler;
    private MessagesHandler mMessagesHandler;
    private View mTitleLayout;
    private View mContentLayout;
    private View mProgressLayout;

    private boolean mLoadedData = false;
    private Status mStatus;
    private List<Status> mMessages;

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

        mContentLayout = view.findViewById(R.id.fragment_main_content);
        mProgressLayout = view.findViewById(R.id.fragment_progress_layout);
        mTitleLayout = view.findViewById(R.id.response_label_layout);

        mListview.setDivider(null);
        mListview.setAdapter(mAdapter);

        updateVisibility();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!mLoadedData) {
            queryForStatus();
            queryForMessages();
            mLoadedData = true;

        } else {
            setStatus(mStatus);
            setMessages(mMessages);
        }
    }

    private void queryForStatus() {
        mQueue.add(new GsonRequest<Status>(
                GithubApi.STATUS, Status.class, null, mStatusHandler, mStatusHandler));
    }

    private void queryForMessages() {
        final Type type = new TypeToken<List<Status>>() {}.getType();
        mQueue.add(new GsonRequest<List<Status>>(
                GithubApi.LAST_MESSAGES, type, null, mMessagesHandler, mMessagesHandler));
    }

    private void updateVisibility() {
        ViewUtils.setVisibility(mContentLayout, mLoadedData);
        ViewUtils.setVisibility(mProgressLayout, !mLoadedData);
    }

    private void setStatus(final Status status) {
        mStatus = status;
        mStatusView.setVisibility(status.getStatus() == null ? View.GONE : View.VISIBLE);
        mStatusView.setTextColor(ViewUtils.resolveStatusColour(getActivity(), status));
        mStatusView.setText(status.getStatus().toUpperCase());

        updateVisibility();
    }

    private void setMessages(final List<Status> response) {
        mMessages = response;
        mTitleLayout.setVisibility(View.VISIBLE);
        mAdapter.refresh(response);

        updateVisibility();
    }

    private class StatusHandler implements Response.ErrorListener, Response.Listener<Status> {

        @Override
        public void onErrorResponse(VolleyError error) {
            mTitleLayout.setVisibility(View.GONE);
            setStatus(Status.getErrorStatus(getActivity()));
        }

        @Override
        public void onResponse(Status response) {
            mTitleLayout.setVisibility(View.VISIBLE);
            setStatus(response);
        }
    }

    private class MessagesHandler implements Response.ErrorListener, Response.Listener<List<Status>> {

        @Override
        public void onErrorResponse(VolleyError error) {
            mTitleLayout.setVisibility(View.GONE);
            Log.v("TAG", error.toString());
            error.printStackTrace();
        }

        @Override
        public void onResponse(List<Status> response) {
            setMessages(response);
        }
    }

    private MainFragment() {
    }
}
