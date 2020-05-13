package com.fishblack.async.job;

import com.fishblack.async.job.exception.JobException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Job submission task class which to decide to execute or wait or give up.
 */
@SuppressWarnings("unchecked")
public class JobSubmitTask implements Runnable, Comparable<JobSubmitTask> {
	private static final Logger logger = Logger.getLogger(JobSubmitTask.class.getName());
	private Job job;
	private JobCallback callback;
	private JobManager jobManager;

	public JobSubmitTask(JobManager jobManager, Job job, JobCallback callback) {
		super();
		this.jobManager = jobManager;
		this.job = job;
		this.callback = callback;
	}

    /**
     * The whole process of the submission task.
     * Do nothing when found latest job is not current job.
     * Do nothing when found latest job is running.
     * Request to cancel previous running job and then reschedule a submission.
     * Start to process current job.
     */
	@Override
	public void run() {
		try {
			Job latestJob = jobManager.getLatestJob(job.getStripe(), job.getTwopartName(), job.getObjectType(), job.getJobType());
			if (latestJob == null){
				//job cleaned.
				return;
			}
			//get Running job (contains job in RUNNING or REQUEST_FOR_CANCEL status)
			Job runningJob = jobManager.getRunningJob(job.getStripe(), job.getTwopartName(), job.getObjectType(), job.getJobType());

			if (!job.getJobId().equals(latestJob.getJobId())) {
				//other nodes submitted new job, do nothing
				return;
			}

			if (runningJob != null) {
				//request existing job to cancel
				if (runningJob.getStatus().equals(Job.Status.RUNNING)) {
					jobManager.updateJobStatus(runningJob, Job.Status.REQUEST_FOR_CANCEL);
				}
				//try to submit it again 10 seconds later
				jobManager.scheduleJob(job, 10, TimeUnit.SECONDS, callback);
				return;
			}

			JobProcessor processor = JobProcessorFactory.createJobProcessor(jobManager, job.getJobType());
			processor.processJob(job, callback);
		}
		catch (JobException ex){
			if (ex.getErrorCode().equals(JobException.DSSJobErrorCode.INTERRUPTED)){
				logger.log(Level.INFO, ex.getErrorMessage());
			}
			else {
				logger.log(Level.WARNING, "Job execution error", ex);
				try {
					jobManager.updateJobStatus(job, Job.Status.ERROR);
					callback.onException(jobManager, job, ex);
				} catch (JobException e) {
					callback.onException(jobManager, job, e);
				}
			}
		}

	}

	@Override
	public int compareTo(JobSubmitTask o) {
		return Integer.compare( job.getPriority().value() , o.job.getPriority().value());
	}
}