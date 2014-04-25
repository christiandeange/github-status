package com.deange.githubstatus.http;

import android.content.Context;

public abstract class OneParamApi<T> extends BaseApi<T, T> {

    public OneParamApi(final Context context) {
        super(context);
    }

}
