package com.fishblack.async.job.noop;

import com.fishblack.async.job.AbstractJobProcessor;
import com.fishblack.async.job.JobManager;
import com.fishblack.async.job.JobResult;
import com.fishblack.async.job.exception.JobException;

/**
 * A mock job processor to simulate the real job process.
 */
public class NoOpJobProcessor extends AbstractJobProcessor<NoOpJob> {

	public NoOpJobProcessor(JobManager jobManager) {
	    super(jobManager);
	}

    /**
     * May get jobManager using getJobManager(),
     * may get datasetService using jobManager.getDatasetService(),
     * may get jobDataProvider using datasetService.getJobDataProvider().
     * @param job the execution job
     * @return job execution result
     * @throws JobException
     */
    @Override
    public JobResult executeJob(NoOpJob job) throws JobException {
        JobResult result;
	    try {
            Thread.sleep(5000);
            for (int i = 0; i < 5; i++) {
                if (checkForRequestForCancel(job)){
                    result = new JobResult("Job cancelled", JobResult.ResultType.CANCELLED);
                    return result;
                }
                if (checkForForceCancelled(job)) {
                    result = new JobResult("Job cancelled", JobResult.ResultType.CANCELLED);
                    return result;
                }
                Thread.sleep(200);
            }
            result = new JobResult("Job succeeded", JobResult.ResultType.SUCCEEDED);
            return result;
        }
        catch (InterruptedException ex){
            result = new JobResult("Job interrupted by others", JobResult.ResultType.FAILED);
            return result;
        }
    }

    @Override
    public void handleRequestForCancel(NoOpJob job) {
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException ex){

        }
    }

    @Override
    public void handleForceCancelled(NoOpJob job) {
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException ex){

        }
    }
}