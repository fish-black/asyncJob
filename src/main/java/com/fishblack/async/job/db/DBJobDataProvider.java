package com.fishblack.async.job.db;

import com.fishblack.async.job.Job;
import com.fishblack.async.job.JobDataProvider;
import com.fishblack.async.job.exception.JobException;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is the implementation of JobDataProvider interface for database.
 */
public class DBJobDataProvider implements JobDataProvider {
	private static final Logger logger = Logger.getLogger(DBJobDataProvider.class.getName());

	@Override
	public Job createJob(Job job) throws JobException {
		return null;
	}

	@Override
	public Job getJob(String jobId) throws JobException {
		return null;
	}

	@Override
	public Job getRunningJob(String stripe, String twopartName, String objType, Job.JobType jobType) throws JobException {
		return null;
	}

	@Override
	public Job getLatestJob(String stripe, String twopartName, String objType, Job.JobType jobType) throws JobException {
		return null;
	}

	@Override
	public int updateJobStatus(Job job, Job.Status status) throws JobException {
		return 0;
	}

	@Override
	public int handleLegacyJobs(long jobMaxRunningTime) throws JobException {
		return 0;
	}

	@Override
	public Timestamp getCurrentTimestamp() throws JobException {
		return null;
	}

	@Override
	public List<String> cancelJobs(String stripeName) {
		return null;
	}

	@Override
	public void keepOnTrackJobs(List<String> jobIds) {

	}
}
