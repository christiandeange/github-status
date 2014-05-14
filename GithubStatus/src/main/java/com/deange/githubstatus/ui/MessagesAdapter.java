package com.deange.githubstatus.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.deange.githubstatus.R;
import com.deange.githubstatus.model.Status;

import java.util.List;

public class MessagesAdapter extends ArrayAdapter<Status> {

    public MessagesAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void refresh(final List<Status> items) {
        clear();

        if (items != null) {
            for (Status status : items) {
                if (status != null) {
                    add(status);
                }
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {

        final View view;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_message, null);
        } else {
            view = convertView;
        }

        final Status status = getItem(position);

        ((TextView) view.findViewById(R.id.list_item_status)).setText(status.getBody());

        final Time messageTime = status.getCreatedOn();
        if (messageTime != null) {
            ((TextView) view.findViewById(R.id.list_item_timestamp)).setText(
                    messageTime.format("%B %d %Y, %r"));
        }

        ViewUtils.setVisibility(view.findViewById(R.id.status_bar_top), position != 0);
        ViewUtils.setVisibility(view.findViewById(R.id.status_bar_bottom), position != getCount() - 1);

        final int statusColour = ViewUtils.resolveStatusColour(getContext(), status);

        final View statusIndicator = view.findViewById(R.id.status_indicator_circle);
        final Drawable drawable = statusIndicator.getBackground();
        drawable.mutate().setColorFilter(statusColour, PorterDuff.Mode.SRC_ATOP);
        statusIndicator.setBackgroundDrawable(drawable);

        return view;
    }
}
