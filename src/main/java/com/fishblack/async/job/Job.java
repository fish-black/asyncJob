package com.fishblack.async.job;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Background Job Entity Class.
 */
public class Job {
	
	private String jobId;
	private String stripe;
	private String objectType;
	private String objectName;
	private String nameSpace;
	private String twopartName;
	private Timestamp requestTime;
	private Timestamp startTime;
	private Timestamp endTime;
	private JobType jobType;
	private String jobDetail;
	private Status status;
	private JobPriority priority;
	private Timestamp lastPingTime;

	public enum JobType {
		PARQUET_CONVERSION,
		NOOP,
		DATA_INSIGHTS
	}

	public enum Status {
		NOT_STARTED,
		RUNNING,
		SUCCEEDED,
		REQUEST_FOR_CANCEL,
		CANCELLED,
		FAILED,
        ERROR
	}

	public Job(String stripe, JobType jobType) {
		this(stripe, null, null, jobType);
	}

	public Job(String stripe, String twopartName, String objectType, JobType jobType) {
		this(stripe, twopartName, objectType, jobType, JobPriority.MEDIUM);
	}

	public Job(String stripe, String twopartName, String objectType, JobType jobType, JobPriority jobPriority) {
		this.jobId = UUID.randomUUID().toString();
		this.stripe = stripe;
		this.twopartName = twopartName;
		this.objectType = objectType;
		this.jobType = jobType;
		this.status = Status.NOT_STARTED;
		this.priority = jobPriority;
	}

	public String getStripe() {
		return stripe;
	}

	public void setStripe(String stripe) {
		this.stripe = stripe;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public String getTwopartName() {
		return twopartName;
	}

	public void setTwopartName(String twopartName) {
		this.twopartName = twopartName;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public Timestamp getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(Timestamp requestTime) {
		this.requestTime = requestTime;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public JobType getJobType() {
		return jobType;
	}

	public void setJobType(JobType jobType) {
		this.jobType = jobType;
	}

	public Status getStatus(Connection conn) {
		return Status.NOT_STARTED;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getJobDetail() {
		return jobDetail;
	}

	public void setJobDetail(String jobDetail) {
		this.jobDetail = jobDetail;
	}

	public JobPriority getPriority() {
		return priority;
	}

	public void setPriority(JobPriority priority) {
		this.priority = priority;
	}

	public Timestamp getLastPingTime() {
		return lastPingTime;
	}

	public void setLastPingTime(Timestamp lastPingTime) {
		this.lastPingTime = lastPingTime;
	}

	@Override
	public int hashCode(){
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((jobId == null) ? 0 : jobId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;
		Job e = (Job) o;
		return getJobId().equals(e.getJobId());
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		buf.append("[DssJob")
				.append(": jobId=").append(jobId)
				.append(", stripeName=").append(stripe)
				.append(", objectType=").append(objectType)
				.append(", objectName=").append(objectName)
				.append(", objectFQDN=").append(twopartName)
				.append(", objectNamespace=").append(nameSpace)
				.append(", jobType=").append(jobType.toString())
                .append(", requestTime=").append(requestTime)
                .append(", startTime=").append(startTime)
                .append(", endTime=").append(endTime)
				.append(", status=").append(status.toString())
				.append(", jobDetail=").append(jobDetail)
				.append(", priority=").append(priority.toString())
				.append("]");

		return buf.toString();
	}
	
	public boolean hasFailed() {
		return status == Status.REQUEST_FOR_CANCEL ||
			   status == Status.CANCELLED ||
			   status == Status.FAILED ||
			   status == Status.ERROR;	
	}
	
	public boolean isPending() {
		return status == Status.NOT_STARTED ||
			   status == Status.RUNNING;
	}

	public boolean isRunning(){
		return status == Status.RUNNING;
	}

	public boolean isWaiting(){
		return status == Status.NOT_STARTED;
	}

	public boolean hasSucceded() {
		return status == Status.SUCCEEDED;
	}

}
