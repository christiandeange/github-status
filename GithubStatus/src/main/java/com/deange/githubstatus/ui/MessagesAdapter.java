package com.deange.githubstatus.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.deange.githubstatus.R;
import com.deange.githubstatus.http.Status;

import java.util.List;

public class MessagesAdapter extends ArrayAdapter<Status> {

    public MessagesAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void refresh(final List<Status> items) {
        clear();

        if (items != null) {
            for (Status status : items) {
                add(status);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_message, null);
        }

        final Status status = getItem(position);

        ((TextView) convertView.findViewById(R.id.list_item_status)).setText(status.getBody());
        ((TextView) convertView.findViewById(R.id.list_item_timestamp)).setText(
                status.getCreatedOn().format("%B %d %Y, %r"));

        ViewUtils.setVisibility(convertView.findViewById(R.id.status_bar_top), position != 0);
        ViewUtils.setVisibility(convertView.findViewById(R.id.status_bar_bottom), position != getCount() - 1);

        final int statusColour = ViewUtils.resolveStatusColour(getContext(), status);

        final View statusIndicator = convertView.findViewById(R.id.status_indicator_circle);
        final Drawable drawable = statusIndicator.getBackground();
        drawable.mutate().setColorFilter(statusColour, PorterDuff.Mode.SRC_ATOP);
        statusIndicator.setBackgroundDrawable(drawable);

        return convertView;
    }
}
