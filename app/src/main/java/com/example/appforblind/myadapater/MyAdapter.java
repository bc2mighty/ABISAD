package com.example.appforblind.myadapater;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.appforblind.R;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Integer> listId;
    private ArrayList<String> nameList;
    private ArrayList<String> numberList;

    public MyAdapter(Context context, ArrayList<Integer> listId, ArrayList<String> nameList, ArrayList<String> numberList) {
        this.context = context;
        this.listId = listId;
        this.nameList = nameList;
        this.numberList = numberList;
    }

    @Override
    public int getCount() {
        return nameList.size();
    }

    @Override
    public Object getItem(int position) {
        return nameList.get(position );
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(context, R.layout.item_list, null);
        }

        ImageView images = (ImageView) convertView.findViewById(R.id.imageView);
        TextView texts = (TextView) convertView.findViewById(R.id.textView);
        TextView numbers = (TextView) convertView.findViewById(R.id.textView2);

//        images.setImageResource(listId.get(position));
        texts.setText(nameList.get(position));
        numbers.setText(numberList.get(position));
        return convertView;
    }
}
