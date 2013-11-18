package com.betfair.cougar.core.api.ev;

/**
 * Hints as to when an execution processor should be run.
 */
public enum ExecutionRequirement {
    /**
     * Execution should be optimised so that this processor should be run exactly once, but at any execution point.
     */
    EXACTLY_ONCE,

    /**
     * Execution should only performed prior to pushing an execution request into the Executor pool.
     */
    PRE_QUEUE,

    /**
     * Execution should only performed prior to executing the target executable.
     */
    PRE_EXECUTE,

    /**
     * Execution should be performed at every location it is possible to do so.
     */
    EVERY_OPPORTUNITY
}
