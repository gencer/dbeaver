/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.runtime;

/**
 * DBPViewCallback
 */
public interface DBRRunnableContext
{
    /**
     * Runs blocking process.
     * If any exception will occure when running this process then it'll written in log
     * @param fork fork
     * @param cancelable cancalable
     * @param runnable runnable implementation
     */
    public void runAndWait(boolean fork, boolean cancelable, DBRRunnableWithProgress runnable);
}
