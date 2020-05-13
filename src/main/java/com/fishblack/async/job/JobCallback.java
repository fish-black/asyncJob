package com.fishblack.async.job;

/**
 * This interface defines the callback methods in kinds of job termination status.
 */
public interface JobCallback<T extends Job> {

    /**
     * Callback method when job succeeded.
     * @param jobManager
     * @param job
     * @param result
     */
	void onSucceeded(JobManager jobManager, T job, JobResult result);

    /**
     * Callback method when job cancelled.
     * @param jobManager
     * @param job
     */
	void onCancelled(JobManager jobManager, T job);

    /**
     * Callback method when job failed.
     * @param jobManager
     * @param job
     * @param result
     */
	void onFailed(JobManager jobManager, T job, JobResult result);

    /**
     * Callback method when job process thread meet exceptions.
     * @param jobManager
     * @param job
     * @param cause
     */
	void onException(JobManager jobManager, T job, Throwable cause);

}