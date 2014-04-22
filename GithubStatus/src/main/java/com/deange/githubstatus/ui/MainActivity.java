package com.deange.githubstatus.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.deange.githubstatus.R;
import com.deange.githubstatus.Utils;
import com.deange.githubstatus.gcm.GCMBaseActivity;

import java.util.Calendar;

public class MainActivity
        extends GCMBaseActivity {

    private MainFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
        if (mFragment == null) {
            mFragment = MainFragment.newInstance();
        }

        if (!mFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().add(
                    R.id.content_frame, mFragment, MainFragment.TAG).commit();
        }
    }

    private void showInfoDialog() {

        final String developerName = getString(R.string.about_developer_name, Calendar.getInstance().get(Calendar.YEAR));
        final String versionName = getString(R.string.app_version, Utils.getVersionName(this));

        final View dialogContentView = getLayoutInflater().inflate(R.layout.dialog_about, null);
        ((TextView) dialogContentView.findViewById(R.id.dialog_about_developer_name)).setText(developerName);
        ((TextView) dialogContentView.findViewById(R.id.dialog_about_version_name)).setText(versionName);
        Linkify.addLinks((TextView) dialogContentView.findViewById(R.id.dialog_about_description), Linkify.WEB_URLS);

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setView(dialogContentView)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_info:
                showInfoDialog();
                return true;

            case R.id.menu_sync:
                refresh();
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
    public void onGcmMessageReceived(final Intent intent) {
        // Reload the fragment's content view
        refresh();
    }
}
