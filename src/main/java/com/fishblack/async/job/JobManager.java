package com.fishblack.async.job;

import com.fishblack.async.job.exception.JobException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.fishblack.async.job.JobConstants.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * The class is for managing background jobs.
 * It provides the basic operation methods through the life-cycle of a job.
 * It is the unified api interface for background job client
 */
public class JobManager {
	private static final Logger logger = Logger.getLogger(JobManager.class.getName());
	private JobDataProvider jobDataProvider;
    private ThreadPoolExecutor executor;
    private int maxQueueSize;

	private static volatile Timer scheduleTimer;
	//Do not initialize the scheduleTimer in constructor but first required
	private final Object timerLock = new Object();

	private static Timer daemonTimer;
	private static volatile JobManager instance = null;
	private List<String> runningJobIds = new Vector<>();

    /**
     * Constructor of this class
     * @param corePoolSize  the corePoolSize of Scheduled thread pool executor
     * @param maxQueueSize the thread pool blocking queue size, if job request count exceeded this value, may receive
     *                     JobException for rejection
     * @param jobDataProvider with the jobDataProvider to take actions on jobs
     */
	private JobManager(int corePoolSize,
					   int maxPoolSize,
					   int maxQueueSize,
					   JobDataProvider jobDataProvider) {
		this.jobDataProvider = jobDataProvider;
		this.maxQueueSize = maxQueueSize;
		logger.log(Level.INFO, "Creating job manager thread pool with corePoolSize={0}, maxPoolSize={1}, maxQueueSize={2}", new Object[] {corePoolSize, maxPoolSize, maxQueueSize});
		this.executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0, MILLISECONDS,
				new PriorityBlockingQueue<Runnable>(maxQueueSize), new JobThreadFactory(BACKGROUND_JOB_THREAD_NAME));
	}

	/**
	 * Singleton instance of this class.
	 * @param corePoolSize  the corePoolSize of Scheduled thread pool executor
	 * @param maxPoolSize  the maxPoolSize of Scheduled thread pool executor
	 * @param maxQueueSize the thread pool blocking queue size, if job request count exceeded this value, may receive
	 *                     JobException for rejection
	 * @param jobDataProvider with the jobDataProvider to take actions on jobs
	 */
	public static JobManager getInstance(int corePoolSize,
										 int maxPoolSize,
										 int maxQueueSize,
										 JobDataProvider jobDataProvider){
		if (instance == null){
			synchronized(JobManager.class) {
				if (instance == null){
					instance = new JobManager(corePoolSize, maxPoolSize, maxQueueSize, jobDataProvider);
				}
				instance.scheduleDaemonTimer(KEEP_ON_TRACK_JOB_TIME_INTERVAL, KEEP_ON_TRACK_JOB_TIME_INTERVAL);
			}
		}
		return instance;
	}

    /**
     * Submit a job
     * @param job the job to submit into job queue
     * @param callback the callback handler for job completion
	 * @exception JobException when job data is empty or any exception occurs
     */
	public void submitJob(Job job, JobCallback callback) throws JobException{
		if (executor == null || executor.isShutdown()){
			logger.fine("The executor has been shut down.");
			return;
		}
		if (job == null){
            throw new JobException(JobException.DSSJobErrorCode.INVALID_INPUT, "Job creation failed due to empty job entity");
        }
        if (jobDataProvider == null){
            throw new JobException(JobException.DSSJobErrorCode.INTERNAL_ERROR, "Empty jobDataProvider found");
        }
		Job jobCreated = jobDataProvider.createJob(job);
		if (jobCreated != null) {
            logger.log(Level.FINE, "A new job submitted {0}", job.toString());

            //The scheduled thread pool using unlimited blocking queue, so using this queue size to limit the request count
            //Getting this queue size ignoring thread-unsafe for not bring bottle neck.
		    int size = executor.getQueue().size();
		    if ( size < this.maxQueueSize ) {
		        try {
                    executor.execute(new JobSubmitTask(this, jobCreated, callback));
                }
                catch (RejectedExecutionException ex){
                    throw new JobException(JobException.DSSJobErrorCode.JOB_REJECTED, "Job rejected by executor", ex);
                }
            }
            else {
                throw new JobException(JobException.DSSJobErrorCode.JOB_REJECTED, "Job rejected by executor");
            }
		}
		else {
			logger.warning("Job submit failed");
            throw new JobException(JobException.DSSJobErrorCode.CREATION_FAILED, "Job creation failed");
		}
	}

    /**
     * Schedule a delayed job.
     * @param job the job to be executed
     * @param delay the time from now to delay execution
     * @param unit the time unit of the delay parameter
     * @param callback the callback handler for job completion
	 * @exception JobException when job data is empty or not illegal
     */
	public void scheduleJob(Job job, long delay, TimeUnit unit, JobCallback callback) throws JobException{
		if (executor == null || executor.isShutdown()){
			logger.fine("The executor has been shut down.");
			return;
		}
		if (job != null) {
			JobManager jobManager = this;
			if (scheduleTimer == null){
				synchronized (timerLock){
					if (scheduleTimer == null) {
						scheduleTimer = new Timer(JOB_RESCHEDULED_SUBMITTER);
					}
				}
			}
			scheduleTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					executor.execute(new JobSubmitTask(jobManager, job, callback));
				}
			}, MILLISECONDS.convert(delay, unit));
		}
		else {
			logger.warning("Job re-scheduled failed due to empty job entity");
			throw new JobException(JobException.DSSJobErrorCode.INTERNAL_ERROR, "Job re-scheduled failed due to empty job entity");
		}

	}
	
    /**
     * Check if the job's status changed to Cancelled.
     * @param job the job to be checked
     * @return boolean value of cancelled or not
	 * @exception JobException when job data error or any exception occurs
     */
	public boolean isJobCancelled(Job job) throws JobException {
		return Job.Status.CANCELLED.equals(getJobStatus(job.getJobId()));
	}

	/**
	 * Check if the job's status changed to Request for Cancel.
	 * @param job the job to be checked
	 * @return boolean value of Request for Cancel or not
	 * @exception JobException when job data error or any exception occurs
	 */
	public boolean isJobRequestForCancel(Job job) throws JobException {
		return Job.Status.REQUEST_FOR_CANCEL.equals(getJobStatus(job.getJobId()));
	}

    /**
     * Check if the job's status is Running.
     * @param job the job to be checked
     * @return boolean value of Running or not
	 * @exception JobException when job data error or any exception occurs
     */
	public boolean isJobRunning(Job job) throws JobException {
		return Job.Status.RUNNING.equals(getJobStatus(job.getJobId()));
	}

    /**
     * Check if the job's status is Success or Failed or Cancelled.
     * @param job the job to be checked
     * @return boolean value of done or not
	 * @exception JobException when job data error or any exception occurs
     */
	public boolean isJobDone(Job job) throws JobException {
	    Job.Status currentStatus = getJobStatus(job.getJobId());
		return Job.Status.SUCCEEDED.equals(currentStatus) ||
				Job.Status.FAILED.equals(currentStatus) ||
				Job.Status.CANCELLED.equals(currentStatus) ||
				Job.Status.ERROR.equals(currentStatus);
	}

    /**
     * Get job details.
     * @param jobId job identifier
     * @return latest details of a job
	 * @exception JobException when backend service exception occurs
     */
	public Job getJob(String jobId) throws JobException {
		return jobDataProvider.getJob(jobId);
	}

    /**
     * Get latest job of a object.
     * @param stripe
     * @param twopartName
     * @param objType
     * @param jobType
     * @return the latest job of a object
	 * @exception JobException when any parameter are illegal or any exception occurs
     */
	public Job getLatestJob(String stripe, String twopartName, String objType, Job.JobType jobType) throws JobException {
		logger.fine(String.format("Get latest Job with stripe %s, twopartName %s, objType %s, jobType %s" , stripe, twopartName, objType, jobType));
		return jobDataProvider.getLatestJob(stripe, twopartName, objType, jobType);
	}

    /**
     * Get the running job of a object.
     * @param stripe
     * @param twopartName
     * @param objType
     * @param jobType
     * @return the running job of a object
	 * @exception JobException when parameter are illegal or any exception occurs
     */
	public Job getRunningJob(String stripe, String twopartName, String objType, Job.JobType jobType) throws JobException {
		logger.fine(String.format("Get Running Job with stripe %s, twopartName %s, objType %s, jobType %s" , stripe, twopartName, objType, jobType));
		return jobDataProvider.getRunningJob(stripe, twopartName, objType, jobType);
	}

    /**
     * Get current status of a job.
     * @param jobId identifier of a job
     * @return the job status
	 * @exception JobException when backend service exception occurs
     */
	public Job.Status getJobStatus(String jobId) throws JobException {
		Job job = this.getJob(jobId);
		if (job == null){
		    throw new JobException(JobException.DSSJobErrorCode.JOB_NOT_FOUND, String.format("Job not found for this jobId %s", jobId));
        }
		return job.getStatus();
	}

    /**
     * Update only job status and related timestamp properties.
     * @param job the job to be updated
     * @param status the status to be updated with
	 * @return boolean value of update result
	 * @exception JobException when job data or status is illegal or any exception occurs
     */
	public boolean updateJobStatus(Job job, Job.Status status) throws JobException {
        logger.fine(String.format("job with id %s is going to be updated with status %s." , job.getJobId(), status.toString()));
		job.setStatus(status);
		int retCode = jobDataProvider.updateJobStatus(job, status);
		logger.fine(String.format("job with id %s was updated with status %s. Return code is  %s." , job.getJobId(), status.toString(), retCode));
		if (retCode == 1){
			switch (status){
				case NOT_STARTED:
					break;
				case REQUEST_FOR_CANCEL:
					addRunningJob(job);
					break;
				case RUNNING:
					addRunningJob(job);
					break;
				default:
					removeRunningJob(job);
					break;
			}
			return true;
		}
        return false;
    }

    /**
     * Get current timestamp.
     * @return current timestamp
	 * @exception JobException when backend service exception occurs
     */
    public Timestamp getCurrentTimestamp() throws JobException {
        return jobDataProvider.getCurrentTimestamp();
    }

	/**
	 * Cancel the jobs of this service.
	 * @param stripeName The service ID
	 */
	public void cancel(String stripeName) {
        List<String> cancelledIds = jobDataProvider.cancelJobs(stripeName);
		logger.warning(String.format("All the running jobs of this service '%s' have been cancelled", stripeName));
		if (cancelledIds != null) {
			runningJobIds = runningJobIds.stream().filter(id -> !cancelledIds.contains(id)).collect(Collectors.toList());
		}
	}

	/**
	 * Shutdown the job thread pool.
	 */
	public void stop() {
		if (scheduleTimer != null){
			scheduleTimer.cancel();
			scheduleTimer = null;
		}
		if (executor != null && !executor.isShutdown()){
			executor.shutdown();
			try {
				// Give any running task a chance to finish
				boolean isDown = false;
				int count = 0;
				while (!isDown && count < 5) {
					isDown = executor.awaitTermination(1, TimeUnit.SECONDS);
					count ++ ;
				}
			} catch (InterruptedException e) {
				logger.info("Wait for threads termination complete");
			}
			executor.shutdownNow();
			executor = null;
			logger.warning("Job manager thread pool is shut down");
		}
	}

	/**
	 * Add running jobId into memory.
	 * @param job
	 * @return
	 */
	public boolean addRunningJob(Job job){
		if (!runningJobIds.contains(job.getJobId())) {
			return runningJobIds.add(job.getJobId());
		}
		return true;
	}

	/**
	 * Remove running jobId from memory.
	 * @param job
	 * @return
	 */
	public boolean removeRunningJob(Job job){
		return runningJobIds.remove(job.getJobId());
	}

	/**
	 * Keep update running job lastPingTime.
	 */
	public void keepOnTrackJobs(){
		jobDataProvider.keepOnTrackJobs(runningJobIds);
	}

	/**
	 * Init or restart the daemon timer for keep on track jobs.
	 * @param delay
	 * @param period
	 */
	public void scheduleDaemonTimer(int delay, int period){
		if (daemonTimer != null) {
			daemonTimer.cancel();
		}
		daemonTimer = new Timer(JOB_KEEP_ON_TRACK, true);
		daemonTimer.schedule(new KeepOnTrackJobsTask(), delay, period);
	}

	class KeepOnTrackJobsTask extends TimerTask {
		@Override
		public void run() {
			keepOnTrackJobs();
		}
	}
}