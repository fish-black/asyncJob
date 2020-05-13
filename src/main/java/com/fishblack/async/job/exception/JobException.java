package com.fishblack.async.job.exception;

/**
 * This class is the general class of the exceptions in the progress of dss jobs.
 */
public class JobException extends Exception {

    private static final long serialVersionUID = 8419091890056602218L;

    public enum DSSJobErrorCode {
        INVALID_INPUT,
        JOB_NOT_FOUND,
        CREATION_FAILED,
        UPDATE_FAILED,
        QUERY_ERROR,
        JOB_REJECTED,
        INTERNAL_ERROR,
        JOB_TYPE_NOT_SUPPORTED,
        CLEAN_JOB_FAILED,
        JOB_EXECUTION_FAILED,
        INTERRUPTED
    }

    private DSSJobErrorCode code;
    private String errorMessage;


    public JobException(DSSJobErrorCode code, String message) {
        super(message);
        this.code = code;
        this.errorMessage = message;
    }

    public JobException(DSSJobErrorCode code, String message, Exception ex) {
        super(ex);
        this.code = code;
        this.errorMessage = message;
    }

    public DSSJobErrorCode getErrorCode() {
        return this.code;
    }

    public String getErrorMessage() {
        return this.code.toString() + " : " + this.errorMessage;
    }


}
