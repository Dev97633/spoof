package com.perapplauncher;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppAdapter extends BaseAdapter {

    public interface OnAppClickListener { void onAppClick(AppEntry entry); }

    private final Context ctx;
    private final List<AppEntry> items;
    private final OnAppClickListener listener;

    public AppAdapter(Context ctx, List<AppEntry> items, OnAppClickListener listener) {
        this.ctx = ctx;
        this.items = items;
        this.listener = listener;
    }

    @Override public int getCount() { return items.size(); }
    @Override public Object getItem(int i) { return items.get(i); }
    @Override public long getItemId(int i) { return i; }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) v = LayoutInflater.from(ctx).inflate(R.layout.row_app, parent, false);
        AppEntry e = items.get(pos);
        ImageView icon = v.findViewById(R.id.app_icon);
        TextView name = v.findViewById(R.id.app_name);
        TextView pkg = v.findViewById(R.id.app_pkg);
        icon.setImageDrawable(e.icon != null ? e.icon : ctx.getDrawable(android.R.drawable.sym_def_app_icon));
        name.setText(e.label);
        pkg.setText(e.packageName);
        v.setOnClickListener(view -> { if (listener != null) listener.onAppClick(e); });
        return v;
    }
}
