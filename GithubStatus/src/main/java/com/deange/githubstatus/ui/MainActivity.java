package com.deange.githubstatus.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.deange.githubstatus.R;
import com.deange.githubstatus.Utils;

import java.util.Calendar;

public class MainActivity extends FragmentActivity {

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

    @Override
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancel();
            mDialog = null;
        }

        super.onDestroy();
    }

    private void showInfoDialog() {

        final String developerName = getString(R.string.about_developer_name, Calendar.getInstance().get(Calendar.YEAR));
        final String versionName = getString(R.string.app_version, Utils.getVersionName(this));

        final View dialogContentView = getLayoutInflater().inflate(R.layout.dialog_about, null);
        ((TextView) dialogContentView.findViewById(R.id.dialog_about_developer_name)).setText(developerName);
        ((TextView) dialogContentView.findViewById(R.id.dialog_about_version_name)).setText(versionName);
        Linkify.addLinks((TextView) dialogContentView.findViewById(R.id.dialog_about_description), Linkify.WEB_URLS);

        mDialog = new AlertDialog.Builder(this)
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
                if (mFragment != null) {
                    mFragment.refresh();
                    return true;
                }

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
