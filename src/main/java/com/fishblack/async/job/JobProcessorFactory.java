package com.fishblack.async.job;

import com.fishblack.async.job.exception.JobException;
import com.fishblack.async.job.noop.NoOpJobProcessor;

/**
 * This class is the factory for creating job processor.
 */
public class JobProcessorFactory {

	/**
	 * Generate the processor upon jobType.
	 * @param jobManager
	 * @param jobType
	 * @return the job processor for particular job type
	 * @exception JobException when unsupported job type passed in
	 */
	public static JobProcessor<? extends Job> createJobProcessor(JobManager jobManager, Job.JobType jobType)  throws JobException {
		switch (jobType) {
			case NOOP:
				return new NoOpJobProcessor(jobManager);
			default:
				throw new JobException(JobException.DSSJobErrorCode.JOB_TYPE_NOT_SUPPORTED,
						String.format("Not supported job type %s", jobType));
		}
	}

}
