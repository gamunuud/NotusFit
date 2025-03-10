package com.notus.fit;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.Wearable;
import com.notus.fit.activities.DrawerPagerActivity;
import com.notus.fit.activities.StartActivity;
import com.notus.fit.fragments.DashboardFragment;
import com.notus.fit.models.RequestTime;
import com.notus.fit.models.api_models.User;
import com.notus.fit.network.alarm.AlarmReceiver;
import com.notus.fit.network.alarm.ResetNotificationsReceiver;
import com.notus.fit.network.misfit.MisfitDateRequest;
import com.notus.fit.network.services.ScheduleService;
import com.notus.fit.utils.PrefManager;
import com.notus.fit.utils.PreferenceUtils;
import com.notus.fit.utils.TimeUtils;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import butterknife.ButterKnife;

public class MainActivity extends DrawerPagerActivity {
    private boolean mBarChart;
    private DashboardFragment mLastWeekFrag;
    private GoogleApiClient mWearApiClient;
    private DashboardFragment mWeekFrag;

    public MainActivity() {
        mBarChart = true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        boolean serviceStarted = PrefManager.with(getApplicationContext()).getBoolean(PreferenceUtils.SERVICE_STARTED, false);
        if (PrefManager.with(getApplicationContext()).getString(User.OBJECT_ID, null) == null) {
            startActivity(new Intent(this, StartActivity.class));
            finish();
        }
        String avatarUrl = PrefManager.with(getApplicationContext()).getString(PreferenceUtils.AVATAR_URL, null);
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            signOut();
            startActivity(new Intent(this, StartActivity.class));
            finish();
        }
        String time = PrefManager.with(getApplicationContext()).getString("day_start", "08:00 AM");
        Log.d(LOG_TAG, "Start Time: " + time);
        String[] sTime = time.split(":");
        String[] mTime = sTime[1].split(" ");
        int hour = Integer.parseInt(sTime[0]);
        if (mTime[1].equals("PM")) {
            hour += 12;
        }
        int minutes = Integer.parseInt(mTime[0]);
        if (!serviceStarted) {
            AlarmReceiver alarmReceiver = new AlarmReceiver();
            ResetNotificationsReceiver resetNotificationsReceiver = new ResetNotificationsReceiver();
            alarmReceiver.setAlarm(this);
            resetNotificationsReceiver.setAlarm(this);
            PrefManager.with(getApplicationContext()).save(PreferenceUtils.SERVICE_STARTED, true);
        }
        try {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(ScheduleService.NOTIFICATION_ID);
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
        }
        mWeekFrag = new DashboardFragment();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.withDayOfWeek(1).withTime(0, 0, 0, 0);
        LocalDateTime lwEndDate = now.withDayOfWeek(7).minusWeeks(1).withTime(23, 59, 59, 999);
        LocalDateTime lwStartDate = startDate.minusWeeks(1).withTime(0, 0, 0, 0);
        MisfitDateRequest misfitDateRequest = new MisfitDateRequest(startDate.toDateTime().getMillis(), now.toDateTime().getMillis());
        MisfitDateRequest lwMisfit = new MisfitDateRequest(lwStartDate.toDateTime().getMillis(), lwEndDate.toDateTime().getMillis());
        Bundle weekBundle = new Bundle();
        weekBundle.putParcelable(RequestTime.REQUEST_TIME, Parcels.wrap(TimeUtils.getWeekRequest()));
        weekBundle.putInt(DashboardFragment.WEEK, 0);
        weekBundle.putInt(DashboardFragment.DEVICE, 0);
        weekBundle.putParcelable(MisfitDateRequest.MISFIT_DATE, Parcels.wrap(misfitDateRequest));
        mWeekFrag.setArguments(weekBundle);
        Bundle lastWeekBundle = new Bundle();
        lastWeekBundle.putParcelable(RequestTime.REQUEST_TIME, Parcels.wrap(TimeUtils.getPreviousWeekRequest()));
        lastWeekBundle.putInt(DashboardFragment.WEEK, 1);
        lastWeekBundle.putInt(DashboardFragment.DEVICE, 0);
        lastWeekBundle.putParcelable(MisfitDateRequest.MISFIT_DATE, Parcels.wrap(lwMisfit));
        mLastWeekFrag = new DashboardFragment();
        mLastWeekFrag.setArguments(lastWeekBundle);
        mFragmentList.add(mLastWeekFrag);
        mFragmentList.add(mWeekFrag);
        mTitleList.add("Last Week");
        mTitleList.add("This Week");
        initPager();
        mTabHost.setSelectedNavigationItem(1);
        mPager.setCurrentItem(1);
        mToolbarSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mWeekFrag.setDevice(mDeviceOrder.get(position));
                mLastWeekFrag.setDevice(mDeviceOrder.get(position));
                Log.d(LOG_TAG, "Selected Device: " + mDeviceOrder.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        startService(new Intent(this, ScheduleService.class));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        changeItemIcon(menu.findItem(R.id.graph_type));
        return super.onCreateOptionsMenu(menu);
    }

    public void changeItemIcon(MenuItem item) {
        if (mBarChart) {
            item.setIcon(R.drawable.ic_line_chart);
        } else {
            item.setIcon(R.drawable.ic_bar_chart);
        }
    }

    public GoogleApiClient initWearClient() {
        mWearApiClient = new Builder(this).addConnectionCallbacks(new ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {

            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        }).addOnConnectionFailedListener(new OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {

            }
        }).addApi(Wearable.API).build();
        mWearApiClient.connect();
        return mWearApiClient;
    }

    public void onStop() {
        super.onStop();
        if (mWearApiClient != null && mWearApiClient.isConnected()) {
            mWearApiClient.disconnect();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        mBarChart = !mBarChart;
        changeItemIcon(item);
        mWeekFrag.setmBarChart(mBarChart);
        mLastWeekFrag.setmBarChart(mBarChart);
        mWeekFrag.setDevice(mWeekFrag.deviceType);
        mLastWeekFrag.setDevice(mLastWeekFrag.deviceType);
        return super.onOptionsItemSelected(item);
    }
}
