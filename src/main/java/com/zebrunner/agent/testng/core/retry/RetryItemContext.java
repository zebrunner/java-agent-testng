package com.zebrunner.agent.testng.core.retry;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class RetryItemContext {

    /**
     * key is retry count, value is a failed cause
     */
    @Getter
    @Setter
    private Map<Integer, String> retryFailedReasons;

    /**
     * Is true if retry analyzer is started
     */
    private boolean started;

    public void setStarted() {
        this.started = true;
    }

    public void setFinished() {
        this.started = false;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isFinished() {
        return !started;
    }
}
