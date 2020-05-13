package com.fishblack.async.job.noop;

import com.fishblack.async.job.Job;
import com.fishblack.async.job.JobCallback;
import com.fishblack.async.job.JobManager;
import com.fishblack.async.job.JobResult;

import java.util.logging.Logger;

/**
 * An simulator Job callback with several operations when job complete or cancelled.
 */
public class NoOpJobCallback implements JobCallback {
	private static final Logger logger = Logger.getLogger(NoOpJobCallback.class.getName());

	public NoOpJobCallback() {}

	/**
	 * Callback method when job succeeded.
	 * @param jobManager
	 * @param job
	 * @param result
	 * @param job result type object
	 */
	@Override
	public void onSucceeded(JobManager jobManager, Job job, JobResult result) {
		logger.info(String.format("Job with id %s succeeded with result: %s", job.getJobId(), result.getResultData()));
	}

	/**
	 * Callback method when job cancelled.
	 * @param jobManager
	 * @param job
	 */
	@Override
	public void onCancelled(JobManager jobManager, Job job) {
		logger.info(String.format("Job with id %s cancelled", job.getJobId()));
	}

	/**
	 * Callback method when job failed.
	 * @param jobManager
	 * @param job
	 * @param result
	 */
	@Override
	public void onFailed(JobManager jobManager, Job job, JobResult result) {
		logger.info(String.format("Job with id %s failed %s", job.getJobId(), result.getFailedMessage()));
	}

	/**
	 * Callback method when job meet exception during execution.
	 * @param jobManager
	 * @param job
	 * @param cause
	 */
	@Override
	public void onException(JobManager jobManager, Job job, Throwable cause) {
		logger.info(String.format("Job with id %s execution error %s", job.getJobId(), cause==null?"":cause.getMessage()));
	}


}
