package com.deange.githubstatus.http;

import android.content.Context;

import com.deange.githubstatus.Status;

public class GithubStatusApi extends SimpleApi<Status> {

    public GithubStatusApi(final Context context) {
        super(context);
    }

    @Override
    public String getBaseApiEndpoint() {
        return GithubApi.BASE_API_URL;
    }

}
