package com.example.bryan.odginformar.utils;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by bryan on 9/21/2017.
 */

public class CustomLayoutManager extends LinearLayoutManager {
    private boolean isScrollEnabled = true;

    public CustomLayoutManager(Context context, int orientation, boolean reverseLayout){
        super(context, orientation, reverseLayout);
    }

    public void setScrollEnabled(boolean flag){
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollHorizontally(){
        return isScrollEnabled && super.canScrollHorizontally();
    }
}
