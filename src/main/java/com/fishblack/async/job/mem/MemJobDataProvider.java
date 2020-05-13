package com.fishblack.async.job.mem;

import com.fishblack.async.job.Job;
import com.fishblack.async.job.JobDataProvider;

import java.sql.Timestamp;
import java.util.List;

/**
 * This class is the implementation of @JobDataProvider interface for memory
 * which means the jobs will be stored in memory.
 */
public class MemJobDataProvider implements JobDataProvider {

	@Override
	public Job createJob(Job job) {
		return null;
	}

	@Override
	public Job getJob(String jobId) {
		return null;
	}

	@Override
	public Job getRunningJob(String stripe, String twopartName, String objType, Job.JobType jobType) {
		return null;
	}

	@Override
	public Job getLatestJob(String stripe, String twopartName, String objType, Job.JobType jobType) {
		return null;
	}

	@Override
	public int updateJobStatus(Job job, Job.Status status) {
		return 0;
	}

	@Override
	public int handleLegacyJobs(long jobMaxRunningTime) {
		return 0;
	}

	@Override
	public Timestamp getCurrentTimestamp() {
		return null;
	}

	@Override
	public List<String> cancelJobs(String stripeName) {
		return null;
	}

	@Override
	public void keepOnTrackJobs(List<String> jobIds) {}

}
