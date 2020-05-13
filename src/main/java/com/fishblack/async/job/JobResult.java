package com.fishblack.async.job;

/**
 * The job result entity interface.
 */
public class JobResult {

    private String resultData;
    private String failedMessage;
    private ResultType resultType;

    public enum ResultType {
        SUCCEEDED,
        CANCELLED,
        FAILED
    }

    /**
     * Constructor of this JobResult
     * @param resultData result data
     * @param resultType result type
     * @param failedMessage failed cause while result type is FAILED
     */
    public JobResult(String resultData, ResultType resultType, String failedMessage){
        this.resultData = resultData;
        this.resultType = resultType;
        this.failedMessage = failedMessage;
    }

    /**
     * Constructor of this JobResult
     * @param resultData result data
     * @param resultType result type
     */
    public JobResult(String resultData, ResultType resultType){
        this.resultData = resultData;
        this.resultType = resultType;
    }

    public String getResultData() {
        return resultData;
    }

    public void setResultData(String resultData) {
        this.resultData = resultData;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    public String getFailedMessage() {
        return failedMessage;
    }

    public void setFailedMessage(String failedMessage) {
        this.failedMessage = failedMessage;
    }
}
