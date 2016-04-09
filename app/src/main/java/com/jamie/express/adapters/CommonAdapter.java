package com.jamie.express.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;
import java.util.Map;

/**
 * Created by jamie on 2016/3/22.
 */
public abstract class CommonAdapter extends BaseAdapter {

    protected Context context;
    protected List<Map<String, Object>> list;
    protected LayoutInflater inflater;

    public CommonAdapter(Context context, List<Map<String, Object>> list) {
        this.context = context;
        this.list = list;
        inflater = inflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
