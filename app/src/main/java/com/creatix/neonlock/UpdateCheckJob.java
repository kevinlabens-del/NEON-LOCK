package com.creatix.neonlock;

import android.app.job.JobParameters;
import android.app.job.JobService;

/** Best-effort 12-hour background update discovery with optional notification. */
public final class UpdateCheckJob extends JobService {
    @Override public boolean onStartJob(JobParameters params){
        NeonUpdates.checkSilently(getApplicationContext(),()->jobFinished(params,false));
        return true;
    }
    @Override public boolean onStopJob(JobParameters params){
        return true;
    }
}
