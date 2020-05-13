package com.fishblack.async.job;

import com.fishblack.async.job.exception.JobException;

import java.sql.Timestamp;
import java.util.List;

/**
 * This interface of job data provider.
 */
public interface JobDataProvider {

	/**
	 * Create a new background job.
	 * @param job the job to be saved
	 * @return the job entity, which with identifier generated before saved
	 * @exception JobException when any exception occurs
	 */
	Job createJob(Job job) throws JobException;

    /**
     * Get job detail information by unique identifier.
     * @param jobId unique identifier for a job, UUID
     * @return the latest job entity
	 * @exception JobException when any exception occurs
     */
	Job getJob(String jobId) throws JobException;

    /**
     * Get a running job especially of given parameters.
     * @param stripe stripe name
     * @param twopartName object twopart name
     * @param objType object type : DATASET etc.
     * @param jobType the type of a background job
     * @return a running job of particular object
	 * @exception JobException when any exception occurs
     */
	Job getRunningJob(String stripe, String twopartName, String objType, Job.JobType jobType) throws JobException;

    /**
     * Get the latest job especially of given parameters.
     * @param stripe stripe name
     * @param twopartName object twopart name
     * @param objType object type : DATASET etc.
     * @param jobType the type of a background job
     * @return the latest job of particular object
	 * @exception JobException when any exception occurs
     */
	Job getLatestJob(String stripe, String twopartName, String objType, Job.JobType jobType) throws JobException;

    /**
     * Update the job's status.
     * @param job the job to be updated
	 * @param status the status to be updated to
     * @return affected row of the operation
	 * @exception JobException when any exception occurs
     */
	int updateJobStatus(Job job, Job.Status status) throws JobException;

    /**
     * Handle the legacy jobs including deleting finished job, and curing outage job.
     * @param jobMaxRunningTime the limitation of max running time of legacy job with time unit MINUTES
     * @return deleted job count
     * @throws JobException when any exception occurs
     */
	int handleLegacyJobs(long jobMaxRunningTime) throws JobException;

	/**
     * Get current timestamp.
     * @return the timestamp
	 * @exception JobException when any exception occurs
     */
	Timestamp getCurrentTimestamp() throws JobException;

	/**
	 * Cancel the jobs of this service.
	 * @param stripeName The service ID
	 * @return Cancelled job id list.
	 */
	List<String> cancelJobs(String stripeName);

	/**
	 * Update job lastPingTime with jobId list for keeping alive.
	 * @param jobIds
	 */
	void keepOnTrackJobs(List<String> jobIds);

}
