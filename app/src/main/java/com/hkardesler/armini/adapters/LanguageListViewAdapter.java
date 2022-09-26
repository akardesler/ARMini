/*
 * *
 *  * Created by Alper Kardesler on 3.06.2022 03:29
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hkardesler.armini.R;

import java.util.ArrayList;

public class LanguageListViewAdapter extends BaseAdapter {

    private final TypedArray languages;
    private final TypedArray icons;
    Context mContext;

    public LanguageListViewAdapter(Context mContext, TypedArray languages, TypedArray icons) {
        this.mContext = mContext;
        this.languages = languages;
        this.icons = icons;
    }

    public int getCount() {
        return this.languages.length();
    }

    public Object getItem(int i) {
        return this.languages.getResourceId(i, 0);
    }

    public long getItemId(int i) {
        return 0;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        @SuppressLint("ViewHolder")
        View v = ((LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_dropdown_list, viewGroup, false);
        ((TextView) v.findViewById(R.id.tvitem)).setText(this.languages.getString(i));
        v.findViewById(R.id.image).setBackgroundResource(icons.getResourceId(i, 0));
        return v;
    }
}