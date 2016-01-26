package com.example.foodnote;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ViewStepListAdapter extends BaseAdapter {
    private final List<AddStepItem> mItems = new ArrayList<>();
    Context mContext;
    ListView mListView;

    public ViewStepListAdapter(Context context, ListView listView) {
        mContext = context;
        mListView = listView;
    }

    public void add(AddStepItem item) {
        mItems.add(item);
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
    public AddStepItem getItem(int pos) {
        return mItems.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AddStepItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.add_step_item, parent, false);
        }

        // set step number
        TextView stepNumberView = (TextView)convertView.findViewById(R.id.addStepItemNumber);
        stepNumberView.setText(String.format(Integer.toString(position + 1) + ". "));

        final EditText stepAddEdittext = (EditText)convertView.findViewById(R.id.addStepItemEdittext);
        final TextView stepAddText = (TextView)convertView.findViewById(R.id.addStepItemText);
        final Button stepAddButton = (Button)convertView.findViewById(R.id.addStepItemEnterButton);

        stepAddEdittext.setVisibility(View.GONE);
        stepAddText.setVisibility(View.VISIBLE);
        stepAddButton.setVisibility(View.GONE);
        stepAddText.setText(item.getStep());

        return convertView;
    }
}