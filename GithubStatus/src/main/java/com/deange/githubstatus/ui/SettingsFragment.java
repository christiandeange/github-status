package com.deange.githubstatus.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Switch;

import com.deange.githubstatus.R;
import com.deange.githubstatus.model.SettingsInfo;
import com.deange.githubstatus.push.PushServerRegistrar;

public class SettingsFragment extends DialogFragment {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OnSettingsChangedListener)) {
            throw new IllegalStateException(
                    "Activity must implement OnSettingsChangedListener!");
        }
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final Activity activity = getActivity();
        final View root = View.inflate(activity, R.layout.fragment_settings, null);
        final Switch gcmSwitch = (Switch) root.findViewById(R.id.setting_gcm_switch);

        gcmSwitch.setChecked(PushServerRegistrar.isRegisteredOnServer(activity));

        return new AlertDialog.Builder(activity)
                .setView(root)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        ((OnSettingsChangedListener) activity).onSettingsChanged(
                                new SettingsInfo.Builder()
                                    .gcm(gcmSwitch.isChecked())
                                    .build()
                        );
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dismiss();
                    }
                })
                .show();
    }

    public interface OnSettingsChangedListener {
        void onSettingsChanged(final SettingsInfo settings);
    }

}
