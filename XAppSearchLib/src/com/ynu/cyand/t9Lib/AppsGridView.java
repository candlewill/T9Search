
package com.ynu.cyand.t9Lib;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.x2ools.xappsearchlib.R;

import com.ynu.cyand.t9Lib.T9Search.ApplicationItem;
import com.ynu.cyand.t9Lib.T9Search.T9SearchResult;

import java.util.ArrayList;
import java.util.List;

public class AppsGridView extends GridView {
    private AppsAdapter mAppsAdapter;

    private static final String TAG = "AppsGridView";

    private static final boolean DEBUG = false;

    protected static final int MSG_SEARCH_INITED = 0;

    private Context mContext;

    private static T9Search sT9Search;

    private ArrayList<ApplicationItem> apps;
    private ArrayList<ApplicationItem> allApps;

    private PackageManager mPackageManager;

    private ActivityManager mActivityManager;

    private LayoutInflater mLayoutInflater;

    private String mFilterStr = null;

    private HideViewCallback mCallback;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEARCH_INITED:
                    if (!TextUtils.isEmpty(mFilterStr)) {
                        filter(mFilterStr);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    public AppsGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mPackageManager = context.getPackageManager();
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mLayoutInflater = LayoutInflater.from(context);
        // sT9Search = new T9Search(context);
        setApplicationsData();
        new Thread(new Runnable() {

            @Override
            public void run() {
                sT9Search = new T9Search(mContext);
                mHandler.sendEmptyMessage(MSG_SEARCH_INITED);
            }
        }).start();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        Log.d(TAG, "visibility changed to " + visibility);
        super.onVisibilityChanged(changedView, visibility);
    }

    public void setApplicationsData() {
        apps = getRecentApps();
        mAppsAdapter = new AppsAdapter(apps);
        setAdapter(mAppsAdapter);
        mAppsAdapter.notifyDataSetChanged();
    }
    
    public void setAllApplicationsData() {
        if (allApps == null) {
            allApps = getAllApps();
        }
        mAppsAdapter = new AppsAdapter(allApps);
        setAdapter(mAppsAdapter);
        mAppsAdapter.notifyDataSetChanged();
    }

    public boolean startAcivityByIndex(int index) {
        if (DEBUG) {
            dumpApplications();
        }
        if (index < apps.size()) {
            ApplicationItem item = apps.get(index);
            Intent i = mPackageManager.getLaunchIntentForPackage(item.packageName);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
            Log.d(TAG, "start " + item.packageName);
            return true;
        }
        return false;
    }

    public void dumpApplications() {
        for (ApplicationItem item : apps) {
            Log.d(TAG, "info.packageName " + item.packageName);
        }
    }

    public void filter(String string) {
        mFilterStr = string;
        if (sT9Search == null)
            return;
        if (TextUtils.isEmpty(string)) {
            apps = getRecentApps();
            mAppsAdapter = new AppsAdapter(apps);
            setAdapter(mAppsAdapter);
            mAppsAdapter.notifyDataSetChanged();
            return;
        }
        T9SearchResult result = sT9Search.search(string);
        if (result != null) {
            apps = result.getResults();
            mAppsAdapter = new AppsAdapter(apps);
            setAdapter(mAppsAdapter);
            mAppsAdapter.notifyDataSetChanged();
        }
    }

    public ArrayList<ApplicationItem> getRecentApps() {
        List<RecentTaskInfo> recentTasks = mActivityManager.getRecentTasks(9,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE | ActivityManager.RECENT_WITH_EXCLUDED);
        ArrayList<ApplicationItem> recents = new ArrayList<ApplicationItem>();
        if (DEBUG) {
            Log.d(TAG, "recentTasks:  " + recentTasks);
        }
        if (recentTasks != null) {
            for (RecentTaskInfo recentInfo : recentTasks) {
                try {
                    if (DEBUG) {
                        Log.d(TAG, "recentInfo.baseIntent:  "
                                + recentInfo.baseIntent.getComponent().getPackageName());

                    }
                    ApplicationInfo info = mPackageManager.getApplicationInfo(recentInfo.baseIntent
                            .getComponent().getPackageName(), 0);
                    if (mPackageManager.getLaunchIntentForPackage(info.packageName) == null)
                        continue;
                    boolean added = false;
                    for (ApplicationItem tmp : recents) {
                        if (tmp.packageName.equals(info.packageName))
                            added = true;
                    }
                    if (!added) {

                        if ((recentInfo.baseIntent != null)
                                && ((recentInfo.baseIntent.getFlags() & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) != 0)) {
                            Log.d(TAG, "This task has flag = FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
                            continue;
                        }

                        ApplicationItem item = new ApplicationItem();
                        item.name = info.loadLabel(mPackageManager).toString();
                        item.packageName = info.packageName;
                        item.drawable = info.loadIcon(mPackageManager);
                        item.taskId = recentInfo.id;
                        item.baseIntent = recentInfo.baseIntent;
                        recents.add(item);
                    }
                } catch (NameNotFoundException e) {
                    // Log.e(TAG, "cannot find package", e);
                }
            }
        }

        return recents;
    }

    public ArrayList<ApplicationItem> getAllApps() {
        List<ApplicationInfo> infos = mPackageManager.getInstalledApplications(0);
        ArrayList<ApplicationItem> items = new ArrayList<ApplicationItem>();
        for (ApplicationInfo info : infos) {
            if (mPackageManager.getLaunchIntentForPackage(info.packageName) == null)
                continue;
            boolean added = false;
            for (ApplicationItem tmp : items) {
                if (tmp.packageName.equals(info.packageName))
                    added = true;
            }
            if (!added) {
                ApplicationItem item = new ApplicationItem();
                item.name = info.loadLabel(mPackageManager).toString();
                item.packageName = info.packageName;
                item.drawable = info.loadIcon(mPackageManager);
                items.add(item);
            }
        }

        return items;
    }

    private boolean isTaskInRecentList(ApplicationItem item) {
        final int taskId = item.taskId;
        final Intent intent = item.baseIntent;
        if ((intent != null)
                && ((intent.getFlags() & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) != 0)) {
            // / M: Don't care exclude-from-recent app.
            Log.d(TAG, "This task has flag = FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
            return true;
        }
        final ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasks(20,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE);

        for (int i = 0; i < recentTasks.size(); ++i) {
            final ActivityManager.RecentTaskInfo info = recentTasks.get(i);
            if (info.id == taskId) {
                return true;
            }
        }

        Log.d(TAG, "This task is not in recent list for " + taskId);

        return false;
    }

    public class AppsAdapter extends BaseAdapter {

        private ArrayList<ApplicationItem> mAppItems;

        public AppsAdapter(ArrayList<ApplicationItem> apps) {
            mAppItems = apps;
        }

        @Override
        public int getCount() {
            return mAppItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mAppItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            final ApplicationItem item = (ApplicationItem) getItem(position);
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.package_item, null);
                viewHolder = new ViewHolder();

                viewHolder.textTitle = (TextView) convertView.findViewById(R.id.textTitle);
                viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (item.taskId >= 0 && isTaskInRecentList(item)) {
                        mActivityManager.moveTaskToFront(item.taskId,
                                ActivityManager.MOVE_TASK_WITH_HOME);
                        Log.v(TAG, "Move Task To Front for " + item.taskId);
                    } else if (item.baseIntent != null) {
                        Intent intent = item.baseIntent;
                        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                                | Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NEW_TASK);
                        Log.v(TAG, "Starting activity " + intent);
                        try {
                            mContext.startActivity(intent);
                        } catch (SecurityException e) {
                            Log.e(TAG, "Recents does not have the permission to launch " + intent,
                                    e);
                            mContext.startActivity(mPackageManager.getLaunchIntentForPackage(
                                    item.packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        } catch (ActivityNotFoundException e) {
                            Log.e(TAG, "Error launching activity " + intent, e);
                            mContext.startActivity(mPackageManager.getLaunchIntentForPackage(
                                    item.packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }

                    } else {
                        mContext.startActivity(mPackageManager.getLaunchIntentForPackage(
                                item.packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                    mCallback.hideView();

                }

            });

            convertView.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View arg0) {
                    Log.d(TAG, "onLongClick ");
                    Intent i = new Intent();
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    i.setData(Uri.parse("package:" + item.packageName));
                    mContext.startActivity(i);
                    mCallback.hideView();
                    return true;
                }

            });
            viewHolder.textTitle.setText(item.name);
            viewHolder.icon.setImageDrawable(item.drawable);
            return convertView;
        }
    }

    public interface HideViewCallback {
        public void hideView();
    }

    public void setCallback(HideViewCallback callback) {
        mCallback = callback;
    }

    static class ViewHolder {
        TextView textTitle;

        ImageView icon;
    }
}
