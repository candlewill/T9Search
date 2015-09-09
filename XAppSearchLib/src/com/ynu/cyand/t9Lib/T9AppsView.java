
package com.ynu.cyand.t9Lib;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.x2ools.xappsearchlib.R;

import com.ynu.cyand.t9Lib.AppsGridView.HideViewCallback;

public class T9AppsView extends RelativeLayout {

    public static final boolean DEBUG = true;

    private static final String TAG = "T9AppsView";

    public T9AppsView(Context context) {
        super(context);
        mContext = context;
    }

    public T9AppsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    private Context mContext;

    private TextView mFilterView;

    private AppsGridView mAppsGridView;

    private StringBuilder mFilterText = new StringBuilder();

    OnLongClickListener mOnLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            int id = v.getId();
            if (id == R.id.button0 || id == R.id.button1
                    || id == R.id.button2 || id == R.id.button3
                    || id == R.id.button4 || id == R.id.button5
                    || id == R.id.button6 || id == R.id.button7
                    || id == R.id.button8 || id == R.id.button9) {
                int number = getNumberById(v.getId());
                int index = number == 0 ? 10 : number - 1;
                final boolean started = mAppsGridView.startAcivityByIndex(index);
                if (started) {
                    hideView();
                }
            } else if (id == R.id.buttonStar) {
                //hideView();
                //TODO force stop all apps
            } else if (id == R.id.buttonDelete) {
                clearFilter();
            }
            return false;
        }

    };

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.button0 || id == R.id.button1
                    || id == R.id.button2 || id == R.id.button3
                    || id == R.id.button4 || id == R.id.button5
                    || id == R.id.button6 || id == R.id.button7
                    || id == R.id.button8 || id == R.id.button9) {
                int number = getNumberById(view.getId());
                mFilterText.append(number);
                onTextChanged();
            } else if (id == R.id.buttonStar) {
                // show all apps, like a launcher
                mFilterText.delete(0, mFilterText.length());
                mFilterView.setText(mFilterText);
                mAppsGridView.setAllApplicationsData();
            } else if (id == R.id.buttonDelete) {
                if (TextUtils.isEmpty(mFilterText))
                    return;
                mFilterText.deleteCharAt(mFilterText.length() - 1);
                onTextChanged();
            }
        }

    };

    public void clearFilter() {
        mFilterText = new StringBuilder();
        onTextChanged();
    }

    private void onTextChanged() {
        mAppsGridView.filter(mFilterText.toString());
        mFilterView.setText(mFilterText);
    }

    private int getNumberById(int id) {
        if (id == R.id.button0) {
            return 0;
        } else if (id == R.id.button1) {
            return 1;
        } else if (id == R.id.button2) {
            return 2;
        } else if (id == R.id.button3) {
            return 3;
        } else if (id == R.id.button4) {
            return 4;
        } else if (id == R.id.button5) {
            return 5;
        } else if (id == R.id.button6) {
            return 6;
        } else if (id == R.id.button7) {
            return 7;
        } else if (id == R.id.button8) {
            return 8;
        } else if (id == R.id.button9) {
            return 9;
        } else {
            throw new RuntimeException("wrong number");
        }
    }

    @Override
    protected void onFinishInflate() {
        int[] buttons = new int[] {
                R.id.button1, R.id.button2, R.id.button3,

                R.id.button4, R.id.button5, R.id.button6,

                R.id.button7, R.id.button8, R.id.button9,

                R.id.buttonStar, R.id.button0, R.id.buttonDelete
        };

        for (int id : buttons) {
            findViewById(id).setOnClickListener(mOnClickListener);
        }
        for (int id : buttons) {
            findViewById(id).setOnLongClickListener(mOnLongClickListener);
        }
        setOnClickListener(mOnClickListener);
        mAppsGridView = (AppsGridView) findViewById(R.id.appsList);
        mFilterView = (TextView) findViewById(R.id.numFilter);
        
        HideViewCallback callback = new HideViewCallback() {
            
            @Override
            public void hideView() {
                T9AppsView.this.hideView();
            }
        };
        mAppsGridView.setCallback(callback);

        super.onFinishInflate();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "dispatchKeyEvent : " + KeyEvent.keyCodeToString(event.getKeyCode()));
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            hideView();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void hideView() {
        ((Activity) mContext).finish();
    }

    public void onMainViewShow() {
        mAppsGridView.setApplicationsData();
    }
}
