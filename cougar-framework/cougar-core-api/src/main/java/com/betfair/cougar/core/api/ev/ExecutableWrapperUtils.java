package com.betfair.cougar.core.api.ev;

/**
 *
 */
public class ExecutableWrapperUtils {

    public static <T extends Executable> T findChild(Class<T> clazz, ExecutableWrapper wrapper) {
        Executable child = wrapper.getWrappedExecutable();
        return findChild(clazz, child);
    }

    public static <T extends Executable> T findChild(Class<T> clazz, Executable executable) {
        if (clazz.isAssignableFrom(executable.getClass())) {
            return (T) executable;
        }

        if (executable instanceof ExecutableWrapper) {
            return ((ExecutableWrapper)executable).findChild(clazz);
        }

        return null;
    }
}
