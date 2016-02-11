package com.example.foodnote;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/*
 *  Base class for AddStepListAdapter and ViewStepListAdapter
 */
public abstract class BaseStepListAdapter extends BaseAdapter {
    private final List<StepItem> mItems = new ArrayList<>();
    Context mContext;
    ListView mListView;

    public BaseStepListAdapter(Context context, ListView listView) {
        mContext = context;
        mListView = listView;
    }

    public void add(StepItem item) {
        mItems.add(item);
    }

    public void remove(int position) {
        mItems.remove(position);
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public StepItem getItem(int pos) {
        return mItems.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }
}