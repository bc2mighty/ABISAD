package com.example.appforblind.myadapater;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.appforblind.R;

import java.util.ArrayList;

public class TextEditorAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> titles;
    private ArrayList<String> contents;

    public TextEditorAdapter(Context context, ArrayList<String> titles, ArrayList<String> contents) {
        this.context = context;
        this.titles = titles;
        this.contents = contents;
    }

    @Override
    public int getCount() {
        return  titles.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = View.inflate(context, R.layout.text_editor_list, null);
        }

        ImageView images = (ImageView) convertView.findViewById(R.id.textEditorImageView);
        TextView textTitles = (TextView) convertView.findViewById(R.id.titleTextView);

        textTitles.setText(titles.get(position));
        return convertView;
    }
}
