package com.ynu.cyand.t9;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

public class T9AppsView extends com.ynu.cyand.t9Lib.T9AppsView {
    
    private Context mContext;

    public T9AppsView(Context context) {
        super(context);
        mContext = context;
    }

    public T9AppsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public void hideView() {
        mContext.sendBroadcast(new Intent(ViewManagerService.ACTION_HIDE_VIEW));
    }
    
    

}
