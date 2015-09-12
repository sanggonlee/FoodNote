package com.example.foodnote;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sanggon on 2015-09-11.
 */
public class AddStepListAdapter extends BaseAdapter {
    private final List<AddStepItem> mItems = new ArrayList<>();
    Context mContext;
    ListView mListView;

    private static final int UNBOUNDED = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

    public AddStepListAdapter(Context context, ListView listView) {
        mContext = context;
        mListView = listView;
    }


    // To calculate the total height of all items in ListView call with items = adapter.getCount()
    public static int getItemHeightofListView(ListView listView, int items) {
        ListAdapter adapter = listView.getAdapter();

        int grossElementHeight = 10;
        for (int i = 0; i < items; i++) {
            View childView = adapter.getView(i, null, listView);
            childView.measure(UNBOUNDED, UNBOUNDED);
            grossElementHeight += childView.getMeasuredHeight();
        }
        return grossElementHeight;
    }

    public void add(AddStepItem item) {
        mItems.add(item);
        notifyDataSetChanged();

        if (getCount() == 1) {  // first time adding
            return;
        }

        // increase the ListView height
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = getItemHeightofListView(mListView, getCount());
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }

    public void clear() {
        mItems.clear();
        add(new AddStepItem(""));
        notifyDataSetChanged();
        Log.w("ASLA", "num of items after clear="+mItems.size());
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
        Log.w("ASLA", "getView, pos="+position);
        final AddStepItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.add_step_item, parent, false);
        }

        // set step number
        TextView stepNumberView = (TextView)convertView.findViewById(R.id.addStepItemNumber);
        stepNumberView.setText(Integer.toString(position+1)+". ");

        final EditText stepAddEdittext = (EditText)convertView.findViewById(R.id.addStepItemEdittext);
        final TextView stepAddText = (TextView)convertView.findViewById(R.id.addStepItemText);
        final Button stepAddButton = (Button)convertView.findViewById(R.id.addStepItemEnterButton);
        stepAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // replace EditText with TextView
                item.setIsEditing(false);
                item.step = stepAddEdittext.getText().toString();
                stepAddEdittext.setText("");
                stepAddEdittext.setVisibility(View.GONE);

                // create new step
                add(new AddStepItem(""));
                mListView.requestLayout();
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
            stepAddText.setText(item.step);
        }

        return convertView;
    }
}
