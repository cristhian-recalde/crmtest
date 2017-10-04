/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * @author victor.stratan@redknee.com
 * @since 8.5
 */
public class TestPerformanceOfDateFormatting extends ContextAwareTestCase
{
    private static final boolean DEBUG_LOCAL = false;

    /**
     * @param name
     */
    public TestPerformanceOfDateFormatting(final String name)
    {
        super(name);
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     * 
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to
     * be invoked by the Redknee XTest code, which provides the application's
     * operating context.
     * 
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);
        final TestSuite suite = new TestSuite(TestPerformanceOfDateFormatting.class);
        return suite;
    }


    public void testPerfomanceOfOneThread()
    {
        final int N_RUNS = 1000000;
        final Date now = new Date();

        // warm up
        for (int i = 0; i < 20000; i++)
        {
            formatDateSynchronized(now);
            formatDateNotSynchronized(now);
        }

        long synchTimeStart = System.currentTimeMillis();
        for (int i = 0; i < N_RUNS; i++)
        {
            formatDateSynchronized(now);
        }
        long synchTimeEnd = System.currentTimeMillis();

        long notSynchTimeStart = System.currentTimeMillis();
        for (int i = 0; i < N_RUNS; i++)
        {
            formatDateNotSynchronized(now);
        }
        long notSynchTimeEnd = System.currentTimeMillis();

        long threadLocalTimeStart = System.currentTimeMillis();
        for (int i = 0; i < N_RUNS; i++)
        {
            formatDateThreadLocal(now);
        }
        long threadLocalTimeEnd = System.currentTimeMillis();

        System.out.println("---=== One thread ===---");
        System.out.println("       Synch Time = " + (synchTimeEnd - synchTimeStart));
        System.out.println("   Not Synch Time = " + (notSynchTimeEnd - notSynchTimeStart));
        System.out.println("Thread Local Time = " + (threadLocalTimeEnd - threadLocalTimeStart));
        System.out.println();
    }

    public void testPerfomanceOfNCoresThreads()
    {
        final int N_CORES = Runtime.getRuntime().availableProcessors();
        System.out.println("---=== N cores threads ===---");
        runPerfomanceWithNThreads(N_CORES);
        System.out.println();
    }

    public void testPerfomanceOfNCoresx10Threads()
    {
        final int N_CORES = Runtime.getRuntime().availableProcessors();
        System.out.println("---=== 10 x N cores threads ===---");
        runPerfomanceWithNThreads(N_CORES * 10);
        System.out.println();
    }

    public void runPerfomanceWithNThreads(int N_THREADS)
    {
        final int N_RUNS = 1000000;
        final Date now = new Date();
        final Date notNow = new Date();

        // warm up
        for (int i = 0; i < 20000; i++)
        {
            formatDateSynchronized(now);
            formatDateNotSynchronized(now);
        }

        long synchTimeStart;
        long synchTimeEnd;
        {
            Thread[] synchThreads = new Thread[N_THREADS];
            CountDownLatch sychReady = new CountDownLatch(synchThreads.length);
            CountDownLatch sychJoin = new CountDownLatch(synchThreads.length);
            CountDownLatch sychLatch = new CountDownLatch(1);
            for (int i = 0 ; i < synchThreads.length; i++)
            {
                synchThreads[i] = new Thread(new SynchDateFormatter(i, N_RUNS, now, sychLatch, sychReady, sychJoin));
                synchThreads[i].start();
            }

            try
            {
                sychReady.await();
            }
            catch (InterruptedException e)
            {
                fail("Should not be interrupted!");
            }

            synchTimeStart = System.currentTimeMillis();
            sychLatch.countDown();
            if (DEBUG_LOCAL) System.out.println("   Started All");
            try
            {
                sychJoin.await();
            }
            catch (InterruptedException e)
            {
                fail("Should not be interrupted!");
            }
            if (DEBUG_LOCAL) System.out.println("   Join All");
            synchTimeEnd = System.currentTimeMillis();
        }

        long notSynchTimeStart;
        long notSynchTimeEnd;
        {
            Thread[] notSynchThreads = new Thread[N_THREADS];
            CountDownLatch notSychReady = new CountDownLatch(notSynchThreads.length);
            CountDownLatch notSychJoin = new CountDownLatch(notSynchThreads.length);
            CountDownLatch notSychLatch = new CountDownLatch(1);
            for (int i = 0 ; i < notSynchThreads.length; i++)
            {
                notSynchThreads[i] = new Thread(new NotSynchDateFormatter(i, N_RUNS, notNow, notSychLatch, notSychReady, notSychJoin));
                notSynchThreads[i].start();
            }

            try
            {
                notSychReady.await();
            }
            catch (InterruptedException e)
            {
                fail("Should not be interrupted!");
            }

            notSynchTimeStart = System.currentTimeMillis();
            notSychLatch.countDown();
            if (DEBUG_LOCAL) System.out.println("n  Started All");
            try
            {
                notSychJoin.await();
            }
            catch (InterruptedException e)
            {
                fail("Should not be interrupted!");
            }
            if (DEBUG_LOCAL) System.out.println("n  Join All");
            notSynchTimeEnd = System.currentTimeMillis();
        }

        long threadLocalTimeStart;
        long threadLocalTimeEnd;
        {
            Thread[] threadLocalThreads = new Thread[N_THREADS];
            CountDownLatch threadLocalReady = new CountDownLatch(threadLocalThreads.length);
            CountDownLatch threadLocalJoin = new CountDownLatch(threadLocalThreads.length);
            CountDownLatch threadLocalLatch = new CountDownLatch(1);
            for (int i = 0 ; i < threadLocalThreads.length; i++)
            {
                threadLocalThreads[i] = new Thread(new ThreadLocalDateFormatter(i, N_RUNS, notNow,
                        threadLocalLatch, threadLocalReady, threadLocalJoin));
                threadLocalThreads[i].start();
            }

            try
            {
                threadLocalReady.await();
            }
            catch (InterruptedException e)
            {
                fail("Should not be interrupted!");
            }

            threadLocalTimeStart = System.currentTimeMillis();
            threadLocalLatch.countDown();
            if (DEBUG_LOCAL) System.out.println(".  Started All");
            try
            {
                threadLocalJoin.await();
            }
            catch (InterruptedException e)
            {
                fail("Should not be interrupted!");
            }
            if (DEBUG_LOCAL) System.out.println(".  Join All");
            threadLocalTimeEnd = System.currentTimeMillis();
        }

        System.out.println("       Synch Time = " + (synchTimeEnd - synchTimeStart));
        System.out.println("   Not Synch Time = " + (notSynchTimeEnd - notSynchTimeStart));
        System.out.println("Thread Local Time = " + (threadLocalTimeEnd - threadLocalTimeStart));
    }

    public String formatDateSynchronized(Date date)
    {
        // this implementation is copied and not calling ERLogger because that implementation might change
        synchronized (DATE_FORMAT_DAY_ONLY)
        {
            return DATE_FORMAT_DAY_ONLY.format(date);
        }
    }

    public static final DateFormat DATE_FORMAT_DAY_ONLY = new SimpleDateFormat(ERLogger.DATE_FORMAT_DAY_ONLY_STRING);

    public String formatDateNotSynchronized(Date date)
    {
        final DateFormat dateFormat = new SimpleDateFormat(ERLogger.DATE_FORMAT_DAY_ONLY_STRING);
        return dateFormat.format(date);
    }

    public String formatDateThreadLocal(Date date)
    {
        // this implementation is copied and not calling ERLogger because that implementation might change
        final DateFormat dateFormat = DATE_FORMAT_THREAD_LOCAL.get();
        return dateFormat.format(date);
    }

    public static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<SimpleDateFormat>()
    {
        protected SimpleDateFormat initialValue()
        {
            return new SimpleDateFormat(ERLogger.DATE_FORMAT_DAY_ONLY_STRING);
        }
    };

    class SynchDateFormatter implements Runnable
    {
        int id;
        int runs;
        Date input;
        CountDownLatch startLatch;
        CountDownLatch readyLatch;
        CountDownLatch joinLatch;

        SynchDateFormatter(int i, int n, Date date, CountDownLatch start, CountDownLatch ready, CountDownLatch join)
        {
            id = i;
            runs = n;
            input = date;
            startLatch = start;
            readyLatch = ready;
            joinLatch = join;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            if (DEBUG_LOCAL) System.out.println("   Started [" + id + "]");
            readyLatch.countDown();
            try
            {
                startLatch.await();
            }
            catch (InterruptedException e)
            {
                System.out.println("   InterruptedException");
            }
            if (DEBUG_LOCAL) System.out.println("   Notified [" + id + "]");

            for (int i = 0; i < runs; i++)
            {
                String result = formatDateSynchronized(input);
            }
            if (DEBUG_LOCAL) System.out.println("   Ended [" + id + "]");
            joinLatch.countDown();
        }
    }

    class NotSynchDateFormatter implements Runnable
    {
        int id;
        int runs;
        Date input;
        CountDownLatch startLatch;
        CountDownLatch readyLatch;
        CountDownLatch joinLatch;

        NotSynchDateFormatter(int i, int n, Date date, CountDownLatch start, CountDownLatch ready, CountDownLatch join)
        {
            id = i;
            runs = n;
            input = date;
            startLatch = start;
            readyLatch = ready;
            joinLatch = join;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            if (DEBUG_LOCAL) System.out.println("n  Started [" + id + "]");
            readyLatch.countDown();
            try
            {
                startLatch.await();
            }
            catch (InterruptedException e)
            {
                System.out.println("   InterruptedException");
            }
            if (DEBUG_LOCAL) System.out.println("n  Notified [" + id + "]");

            for (int i = 0; i < runs; i++)
            {
                String result = formatDateNotSynchronized(input);
            }
            if (DEBUG_LOCAL) System.out.println("n  Ended [" + id + "]");
            joinLatch.countDown();
        }
    }

    class ThreadLocalDateFormatter implements Runnable
    {
        int id;
        int runs;
        Date input;
        CountDownLatch startLatch;
        CountDownLatch readyLatch;
        CountDownLatch joinLatch;

        ThreadLocalDateFormatter(int i, int n, Date date, CountDownLatch start, CountDownLatch ready, CountDownLatch join)
        {
            id = i;
            runs = n;
            input = date;
            startLatch = start;
            readyLatch = ready;
            joinLatch = join;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            if (DEBUG_LOCAL) System.out.println(".  Started [" + id + "]");
            readyLatch.countDown();
            try
            {
                startLatch.await();
            }
            catch (InterruptedException e)
            {
                System.out.println("   InterruptedException");
            }
            if (DEBUG_LOCAL) System.out.println(".  Notified [" + id + "]");

            for (int i = 0; i < runs; i++)
            {
                String result = formatDateThreadLocal(input);
            }
            if (DEBUG_LOCAL) System.out.println(".  Ended [" + id + "]");
            joinLatch.countDown();
        }
    }
}
