package com.fishblack.async.job;

/**
 * The Constants in Dss Job Management
 */
public class JobConstants {

    public static final int DURATION_ONE_DAY = 24 * 60 * 60 * 1000;

    public static final int DURATION_ONE_MINUTE = 60 * 1000;

    public static final int KEEP_ON_TRACK_JOB_TIME_INTERVAL = 60 * 1000;

    public static final int EXECUTOR_DEFAULT_MAX_QUEUE_SIZE = 500;

    public static final String JOB_RESCHEDULED_SUBMITTER = "RescheduledSubmitter";

    public static final String JOB_KEEP_ON_TRACK = "KeepOnTrackJobs";

    public static final String BACKGROUND_JOB_THREAD_NAME = "DSSJobThread";

}
