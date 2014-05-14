package com.deange.githubstatus.http;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.lang.reflect.Type;

public abstract class BaseApi<S, T> {

	protected final Context mContext;
	private static final OkHttpClient sClient = new OkHttpClient();

	public BaseApi(final Context context) {
		mContext = context;
	}

	public abstract String getBaseApiEndpoint();

	public OkHttpClient getClient() {
		return sClient;
	}

	public S get(final Type clazz, final String url) throws IOException {
		throw new UnsupportedOperationException();
	}

	public S post(final T entity, final String url) throws IOException {
		throw new UnsupportedOperationException();
	}

	public S put(final T entity, final String url) throws IOException {
		throw new UnsupportedOperationException();
	}

	public S delete(final T entity, final String url) throws IOException {
		throw new UnsupportedOperationException();
	}

}
