package com.fishblack.async.job;

import com.fishblack.async.job.exception.JobException;

/**
 * The job processor interface.
 */
public interface JobProcessor<T extends Job> {

    /**
     * A job processor for execution
     * @param job the job to be processed
     * @param callback callback handler
     * @throws JobException
     */
    void processJob(T job, JobCallback<T> callback) throws JobException;

}
