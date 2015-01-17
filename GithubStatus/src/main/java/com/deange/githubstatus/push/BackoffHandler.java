package com.deange.githubstatus.push;

import android.os.Handler;
import android.os.Looper;

import java.util.Random;

public abstract class BackoffHandler implements Runnable {

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
    private static final Random sRandom = new Random();

    private final int mMaxTries;
    private final boolean mAsync;
    private boolean mCancel;

    public BackoffHandler(final int maxTries) {
        this(maxTries, false);
    }

    public BackoffHandler(final int maxTries, final boolean async) {
        mMaxTries = maxTries;
        mAsync = async;
    }

    public boolean isAsync() {
        return mAsync;
    }

    public void start() {
        if (mAsync) {
            new Thread(this).start();
        } else {
            run();
        }
    }

    public void cancel() {
        mCancel = true;
    }

    @Override
    public void run() {

        long maxDelay = 500L;
        int attempt = 0;

        for (;;) {
            boolean exit = false;
            try {
                exit = performAction();
            } catch (final Throwable ignored) {
            }

            if (exit) {
                // Successful
                break;
            }

            if (++attempt == mMaxTries) {
                // Unsuccessful
                break;
            }

            try {
                final long delay = sRandom.nextLong() % maxDelay;
                Thread.sleep(delay);
            } catch (final InterruptedException e) {
                throw new RuntimeException("This thread cannot be waited on!", e);
            }

            if (mCancel || Thread.currentThread().isInterrupted()) {
                // Action cancelled
                break;
            }

            maxDelay *= 2;
        }

        final int totalAttempts = attempt;
        sMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onActionCompleted(!mCancel && totalAttempts != mMaxTries);
            }
        });
    }

    /**
     * @return <tt>true</tt> if this action completed successfully
     */
    public abstract boolean performAction() throws Throwable;

    /**
     * @param success <tt>true</tt> if this action completed successfully
     */
    public abstract void onActionCompleted(final boolean success);
}
