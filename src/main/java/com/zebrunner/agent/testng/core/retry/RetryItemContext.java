package com.zebrunner.agent.testng.core.retry;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RetryItemContext {

    /**
     * key is retry count, value is a failed cause
     */
    private Map<Integer, String> retryFailedReasons;

    /**
     * Is true if retry analyzer is started
     */
    private boolean started;

    /**
     * Is true if retry analyzer is finished
     */
    private boolean finished;

}
