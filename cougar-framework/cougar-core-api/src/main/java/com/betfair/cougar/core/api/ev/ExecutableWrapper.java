package com.betfair.cougar.core.api.ev;

/**
 * Interface to be implemented by any Executables which wrap other executables. Used by various Cougar internals to
 * find the wrapping they need to implement certain hooks.
 */
public interface ExecutableWrapper extends Executable {
    /**
     * Gets the executable wrapped by the wrapper.
     */
    Executable getWrappedExecutable();

    /**
     * Finds a child (recursive) of the given type. If no child matching this type is found then this methods returns null.
     */
    <T extends Executable> T findChild(Class<T> clazz);
}
