package com.fishblack.async.job;

import com.fishblack.async.job.exception.JobException;

import java.util.logging.Logger;

public abstract class AbstractJobProcessor<T extends Job> implements JobProcessor<T> {
    private static final Logger logger = Logger.getLogger(AbstractJobProcessor.class.getName());
    private JobManager jobManager;

    /**
     * Construct
     * @param jobManager
     */
    public AbstractJobProcessor (JobManager jobManager) {
        this.jobManager = jobManager;
    }

    /**
     * The job processor for execution.
     * @param job the job to be processed
     * @param callback callback handler
     * @throws JobException
     */
    @Override
    public void processJob(T job, JobCallback<T> callback) throws JobException {
        boolean running = jobManager.updateJobStatus(job, Job.Status.RUNNING);
        if(!running) {
            return;
        }
        JobResult result = executeJob(job);
        if (result.getResultType().equals(JobResult.ResultType.SUCCEEDED)) {
            jobManager.updateJobStatus(job, Job.Status.SUCCEEDED);
            callback.onSucceeded(jobManager, job, result);
        }
        else if (result.getResultType().equals(JobResult.ResultType.FAILED)){
            jobManager.updateJobStatus(job, Job.Status.FAILED);
            callback.onFailed(jobManager, job, result);
        }
        else if (result.getResultType().equals(JobResult.ResultType.CANCELLED)){
            callback.onCancelled(jobManager, job);
        }
    }

    /**
     * Get job manager
     * @return job manager
     */
    public JobManager getJobManager() {
        return jobManager;
    }

    /**
     * Check the job if it was updated to request for cancel status.
     * @param job the job to be checked
     * @return boolean value of check result
     * @throws JobException
     */
    public boolean checkForRequestForCancel(T job) throws JobException {
        if(jobManager.isJobRequestForCancel(job)) {
            handleRequestForCancel(job);
            jobManager.updateJobStatus(job, Job.Status.CANCELLED);
            return true;
        }
        return false;
    }

    /**
     * Check the job if it was forced updated to cancelled status, e.g. cancelled by clean task for job expiration.
     * @param job the job to be checked
     * @return boolean value of check result
     * @throws JobException
     */
    public boolean checkForForceCancelled(T job) throws JobException {
        if(jobManager.isJobCancelled(job)) {
            handleForceCancelled(job);
            return true;
        }
        return false;
    }

    /**
     * The job execution method to be implemented.
     * @param job the execution job
     * @return {@code JobResult} Job execution result
     * @throws JobException execution failure exception
     */
    public abstract JobResult executeJob(T job) throws JobException;

    /**
     * Implement this method to handle request for cancellation job , e.g. clean up temp files, etc.
     * @param job the job to be cancelled
     */
    public abstract void handleRequestForCancel(T job);

    /**
     * Implement this method to handle that job cancelled by clean task for job expiration, e.g. clean up temp files, etc.
     * @param job the cancelled job
     */
    public abstract void handleForceCancelled(T job);
}
