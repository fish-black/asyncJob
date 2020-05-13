package com.fishblack.async.job.noop;

import com.fishblack.async.job.Job;
import com.fishblack.async.job.JobPriority;

/**
 * An simulator Job entity with No operation when execution.
 */
public class NoOpJob extends Job {

	public NoOpJob(String stripe, String twopartName, String objectType) {
		super(stripe, twopartName, objectType, Job.JobType.NOOP, JobPriority.LOW);
	}

}
