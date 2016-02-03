package com.example.foodnote;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class AddStepListAdapter extends BaseStepListAdapter {
    public static final int DEFAULT_ADD_STEP_ITEM_HEIGHT = 200;

    ArrayAdapter<String> mStepAutoCompleteAdapter;

    public AddStepListAdapter(Context context, ListView listView) {
        super(context, listView);
        mStepAutoCompleteAdapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_dropdown_item_1line);
    }

    public static void recalculateListViewHeight(ListView input, ListView output) {
        int total = 0;
        for (int i=0; i<input.getCount()-1; i++) {
            total += input.getChildAt(i).getHeight();
        }

        // increase the ListView height
        ViewGroup.LayoutParams params = output.getLayoutParams();
        params.height = total + DEFAULT_ADD_STEP_ITEM_HEIGHT;
        output.setLayoutParams(params);
        output.requestLayout();
    }

    public void addAndAdjustHeight(StepItem item) {
        add(item);
        recalculateListViewHeight(mListView, mListView);
        notifyDataSetChanged();
    }

    public void setIngredients(String[] ingredients) {
        mStepAutoCompleteAdapter.clear();
        mStepAutoCompleteAdapter.addAll(ingredients);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final StepItem item = getItem(position);
        final int pos = position;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.add_step_item, parent, false);
        }

        // set step number
        TextView stepNumberView = (TextView)convertView.findViewById(R.id.addStepItemNumber);
        stepNumberView.setText(Integer.toString(position + 1) + ". ");

        final MultiAutoCompleteTextView stepAddEdittext =
                (MultiAutoCompleteTextView)convertView.findViewById(R.id.addStepItemEdittext);
        final TextView stepAddText = (TextView)convertView.findViewById(R.id.addStepItemText);
        final Button stepAddButton = (Button)convertView.findViewById(R.id.addStepItemEnterButton);

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
        stepAddEdittext.setAdapter(mStepAutoCompleteAdapter);
        stepAddEdittext.setTokenizer(new IngredientTokenizer());

        stepAddText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int pIndex=0; pIndex<getCount(); pIndex++) {
                    getItem(pIndex).setIsEditing(pIndex == pos);
                }
                item.setStep(stepAddText.getText().toString());
                notifyDataSetChanged();
                ((InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }
        });

        stepAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // replace EditText with TextView
                item.setIsEditing(false);
                item.setIsSubmitted(true);
                item.setStep(stepAddEdittext.getText().toString());
                stepAddEdittext.setText("");

                if (getCount() == pos+1) {
                    // create new step if footer was submitted
                    addAndAdjustHeight(new StepItem(""));
                } else {
                    notifyDataSetChanged();
                }
            }
        });

        if (item.getIsEditing()) {
            stepAddEdittext.setVisibility(View.VISIBLE);
            stepAddText.setVisibility(View.GONE);
            stepAddButton.setVisibility(View.VISIBLE);
            stepAddEdittext.setText(item.getStep());
            stepAddEdittext.requestFocus();
            /** Soft keyboard is blocking the Edittext upon second click.
             *  This is a bug on Android as per https://code.google.com/p/android/issues/detail?id=182191
             *  TODO: Find a workaround to fix this
             */
        } else {
            stepAddEdittext.setVisibility(View.GONE);
            stepAddText.setVisibility(View.VISIBLE);
            stepAddButton.setVisibility(View.GONE);
            stepAddText.setText(item.getStep());
        }

        return convertView;
    }

    public class IngredientTokenizer implements MultiAutoCompleteTextView.Tokenizer {
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;

            while (i > 0 && text.charAt(i-1) != ',' && text.charAt(i-1) != ' ') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (text.charAt(i) == ',' || text.charAt(i) == ' ' || text.charAt(i) == '.') {
                    // indicate end of the word with comma or space
                    return i;
                } else {
                    i++;
                }
            }
            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
            // we do not want the autocompleted text to end with comma or space
            return text;
        }
    }
}
