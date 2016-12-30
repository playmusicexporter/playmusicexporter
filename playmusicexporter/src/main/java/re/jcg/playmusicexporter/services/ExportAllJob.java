package re.jcg.playmusicexporter.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class ExportAllJob extends JobService {
    public static final String TAG = "AutoGPME_ExportJob";


    public static void scheduleExport(final Context pContext) {
        SharedPreferences lPreferences = PreferenceManager.getDefaultSharedPreferences(pContext);
        lPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                scheduleExport(pContext);
                Log.i(TAG, "Preference changed: " + key);
            }
        });

        long lInterval = Long.parseLong(lPreferences.getString("settings_export_frequency", "86400000"));
        boolean lRequireUnmeteredNetwork = lPreferences.getBoolean("settings_export_", false);
        boolean lRequireCharging = lPreferences.getBoolean("settings", true);

        JobScheduler lJobScheduler = (JobScheduler) pContext.getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName lComponentName = new ComponentName(pContext, ExportAllJob.class);
        JobInfo.Builder lBuilder = new JobInfo.Builder(42, lComponentName);
        lBuilder.setPeriodic(lInterval);
        lBuilder.setPersisted(true);
        if (lRequireUnmeteredNetwork)
            lBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        lBuilder.setRequiresCharging(lRequireCharging);
        lJobScheduler.schedule(lBuilder.build());
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "Started Job: " + params.toString());
        ExportAllService.startExport(this);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "Stopped Job: " + params.toString());
        return true;
    }
}
