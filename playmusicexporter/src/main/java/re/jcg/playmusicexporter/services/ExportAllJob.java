package re.jcg.playmusicexporter.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import re.jcg.playmusicexporter.settings.PlayMusicExporterPreferences;

public class ExportAllJob extends JobService {
    public static final String TAG = "AutoGPME_ExportJob";
    public static final int AUTO_EXPORT_JOB_ID = 0;


    /**
     * Schedules an export with the current settings
     *
     * @param pContext
     */
    public static void scheduleExport(final Context pContext) {
        PlayMusicExporterPreferences.init(pContext);
        JobScheduler lJobScheduler = (JobScheduler) pContext.getSystemService(JOB_SCHEDULER_SERVICE);
        if (PlayMusicExporterPreferences.getAutoExportEnabled()) {
            long lInterval = PlayMusicExporterPreferences.getAutoExportFrequency();
            boolean lRequireUnmeteredNetwork = PlayMusicExporterPreferences.getAutoExportRequireUnmetered();
            boolean lRequireCharging = PlayMusicExporterPreferences.getAutoExportRequireCharging();
            ComponentName lComponentName = new ComponentName(pContext, ExportAllJob.class);
            JobInfo.Builder lBuilder = new JobInfo.Builder(AUTO_EXPORT_JOB_ID, lComponentName);
            lBuilder.setPeriodic(lInterval);
            lBuilder.setPersisted(true);
            if (lRequireUnmeteredNetwork)
                lBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            lBuilder.setRequiresCharging(lRequireCharging);
            lJobScheduler.schedule(lBuilder.build());
        } else {
            lJobScheduler.cancel(AUTO_EXPORT_JOB_ID);
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        PlayMusicExporterPreferences.init(this.getApplicationContext());
        Log.i(TAG, "Started Job: " + params.toString());
        if (PlayMusicExporterPreferences.getAutoExportEnabled()) {
            ExportAllService.startExport(this);
        } else {
            scheduleExport(getApplicationContext());
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "Stopped Job: " + params.toString());
        return true;
    }
}
