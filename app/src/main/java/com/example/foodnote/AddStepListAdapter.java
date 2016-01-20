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

public class AddStepListAdapter extends BaseAdapter {
    private final List<AddStepItem> mItems = new ArrayList<>();
    Context mContext;
    ListView mListView;

    private final int IMAGE_CHOOSE_REQ_CODE = 1;

    public AddStepListAdapter(Context context, ListView listView) {
        mContext = context;
        mListView = listView;
    }

    // To calculate the total height of all items in ListView
    public int getTotalHeightListview() {
        int totalHeight = 10;
        for (int i=0; i<getCount(); i++) {
            totalHeight += mItems.get(i).getHeight();
        }
        return totalHeight;
    }

    public void add(AddStepItem item) {
        mItems.add(item);
        notifyDataSetChanged();

        int total = 0;
        for (int i=0; i<getCount()-1; i++) {
            total += mListView.getChildAt(i).getHeight();
        }

        // increase the ListView height
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = total+item.getHeight();
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }

    public void clear() {
        mItems.clear();
        add(new AddStepItem(""));
        notifyDataSetChanged();
    }

    public void removeLast() {
        mItems.remove(mItems.size() - 1);
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

        final Button stepPictureAddButton = (Button)convertView.findViewById(R.id.addStepPictureButton);
        stepPictureAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                ((Activity) mContext).startActivityForResult(intent, IMAGE_CHOOSE_REQ_CODE);
            }
        });

        final EditText stepAddEdittext = (EditText)convertView.findViewById(R.id.addStepItemEdittext);
        stepAddEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // prevent inserting a newline
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });

        final TextView stepAddText = (TextView)convertView.findViewById(R.id.addStepItemText);

        final Button stepAddButton = (Button)convertView.findViewById(R.id.addStepItemEnterButton);
        stepAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // replace EditText with TextView
                item.setIsEditing(false);
                item.setStep(stepAddEdittext.getText().toString());
                stepAddEdittext.setText("");

                // create new step
                add(new AddStepItem(""));

                // hide soft keyboard
                View focusedView = ((Activity)mContext).getCurrentFocus();
                if (focusedView != null) {
                    InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        if (item.getIsEditing()) {
            stepAddEdittext.setVisibility(View.VISIBLE);
            stepAddText.setVisibility(View.GONE);
            stepAddButton.setVisibility(View.VISIBLE);
        } else {
            stepAddEdittext.setVisibility(View.GONE);
            stepAddText.setVisibility(View.VISIBLE);
            stepAddButton.setVisibility(View.GONE);
            stepAddText.setText(item.getStep());
        }

        return convertView;
    }
}
