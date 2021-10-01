package com.zebrunner.agent.testng.core.retry;

import lombok.ToString;

@ToString
public class RetryItemContext {

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

    public boolean isFinished() {
        return !started;
    }

}
