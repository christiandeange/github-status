package com.deange.githubstatus.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.deange.githubstatus.R;
import com.deange.githubstatus.model.SettingsFragment;
import com.deange.githubstatus.push.PushBaseActivity;

import java.util.Calendar;

public class MainActivity
        extends PushBaseActivity
        implements View.OnClickListener, SettingsFragment.OnSettingsChangedListener {

    private MainFragment mFragment;
    private AlertDialog mDialog;

    private static final String AVATAR_URL = "https://plus.google.com/+ChristianDeAngelis";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setTitle(R.string.app_name);

        mFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
        if (mFragment == null) {
            mFragment = MainFragment.newInstance();
        }

        if (!mFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().add(
                    R.id.content_frame, mFragment, MainFragment.TAG).commit();
        }
    }

//    @Override
//    protected void onDestroy() {
//        if (mDialog != null && mDialog.isShowing()) {
//            mDialog.cancel();
//            mDialog = null;
//        }
//
//        super.onDestroy();
//    }

    private void showInfoDialog() {

        final String developerName = getString(R.string.about_developer_name, Calendar.getInstance().get(Calendar.YEAR));
        final View dialogContentView = getLayoutInflater().inflate(R.layout.dialog_about, null);
        ((TextView) dialogContentView.findViewById(R.id.dialog_about_developer_name)).setText(developerName);
        dialogContentView.findViewById(R.id.dialog_about_avatar).setOnClickListener(this);

        mDialog = new AlertDialog.Builder(this)
                .setView(dialogContentView)
                .show();
    }

    private void showSettings() {
        final String tag = SettingsFragment.TAG;
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SettingsFragment fragment =
                (SettingsFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            transaction.remove(fragment);
        }

        fragment = new SettingsFragment();
        fragment.show(transaction, tag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_sync:
                refresh();
                return true;

            case R.id.menu_settings:
                showSettings();
                return true;

            case R.id.menu_info:
                showInfoDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void refresh() {
        if (mFragment != null) {
            mFragment.refresh();
        }
    }

    @Override
    public void onPushMessageReceived(final Intent intent) {
        // Reload the fragment's content view
        refresh();
    }

    @Override
    public void onClick(final View v) {

        switch (v.getId()) {
            case R.id.dialog_about_avatar:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(AVATAR_URL)));
                break;
        }
    }

    @Override
    public void onSettingsChanged(final SettingsInfo settings) {
        if (settings.isGCMEnabled()) {
            // User is enabling push notifications
            registerIfNecessary();

        } else {
            // User is disabling push notifications
            unregisterIfNecessary();
        }
    }
}
