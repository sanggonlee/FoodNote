package com.example.foodnote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ViewStepListAdapter extends BaseStepListAdapter {

    public ViewStepListAdapter(Context context, ListView listView) {
        super(context, listView);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final StepItem item = getItem(position);

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