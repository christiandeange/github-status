package com.deange.githubstatus.http;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Volley adapter for JSON requests that will be parsed into Java objects by Gson.
 */
public class GsonRequest<T> extends Request<T> {

    private final Gson mGson = new Gson();
    private final Type mType;
    private final Map<String, String> mHeaders;
    private final Listener<T> mListener;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param type Relevant class object, for Gson's reflection
     * @param headers Map of request mHeaders
     */
    public GsonRequest(final String url, final Type type, final Map<String, String> headers,
                       final Listener<T> listener, final ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mType = type;
        mHeaders = headers;
        mListener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders != null ? mHeaders : super.getHeaders();
    }

    @Override
    protected void deliverResponse(final T response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Response<T> parseNetworkResponse(final NetworkResponse response) {
        try {
            final String json = new String(
                    response.data, HttpHeaderParser.parseCharset(response.headers));
            return (Response<T>) Response.success(
                    mGson.fromJson(json, mType), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}