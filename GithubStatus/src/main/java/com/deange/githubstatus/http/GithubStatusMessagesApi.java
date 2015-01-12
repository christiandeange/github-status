package com.deange.githubstatus.http;

import android.content.Context;

import com.deange.githubstatus.Status;

import java.util.List;

public class GithubStatusMessagesApi extends SimpleApi<List<Status>> {

    public GithubStatusMessagesApi(final Context context) {
        super(context);
    }

    @Override
    public String getBaseApiEndpoint() {
        return GithubApi.BASE_API_URL;
    }

}
