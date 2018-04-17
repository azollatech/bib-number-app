package com.henneth.rtsBibNumber;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hennethcheng on 17/6/16.
 */
public class CustomAdapter extends ArrayAdapter<String> {
    protected static final int TRANSPARENT = Color.TRANSPARENT;
    protected final int DONE_COLOR = Color.rgb(0,150,100);

    private ArrayList<String> items;
    private LayoutInflater mInflater;
    private int viewResourceId;
    ArrayList<String> doneList = new ArrayList<String>();

    public CustomAdapter(Activity activity, int resourceId, ArrayList<String> list) {
        super(activity, resourceId, list);

        // Sets the layout inflater
        mInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Set a copy of the layout to inflate
        viewResourceId = resourceId;

        // Set a copy of the list
        items = list;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int size = items.size();
        int reversePosition = size - position - 1;

        TextView tv = (TextView) convertView;

        if (convertView == null) {
            tv = (TextView) mInflater.inflate(viewResourceId, null);
        }
        tv.setText(items.get(position));

        if (doneList.contains(String.valueOf(reversePosition))) {
            tv.setTextColor(DONE_COLOR);
        } else {
            tv.setTextColor(Color.BLACK);
        }

        return tv;
    }

    public void setDoneList(String position) {
        doneList.add(position);
    }

    public void setWholeDoneList(ArrayList<String> list) {
        for (String s : list) {
            doneList.add(s);
        }
    }

    public void resetDoneList() {
        doneList = new ArrayList<>();
    }
}
