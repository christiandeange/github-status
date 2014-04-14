package com.deange.githubstatus.http;

public class HttpTask {

    public static class Listener<T> implements OnHttpRequestDoneListener<T> {

        @Override
        public void onGet(final T entity, final Exception exception) {
        }

        @Override
        public void onPost(final T entity, final Exception exception) {
        }
    }

    public interface OnHttpRequestDoneListener<T> {
        public void onGet(final T entity, final Exception exception);

        public void onPost(final T entity, final Exception exception);
    }
}
