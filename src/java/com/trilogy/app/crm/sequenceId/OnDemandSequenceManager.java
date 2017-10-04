package com.trilogy.app.crm.sequenceId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.OnDemandSequence;
import com.trilogy.app.crm.bean.OnDemandSequenceXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class OnDemandSequenceManager
{

    public static void reset(final Context ctx, final String key)
    {
        synchronized (key)
        {
            OnDemandSequence sequence = sequenceCache_.remove(key);
            try
            {
                sequence = HomeSupportHelper.get(ctx).findBean(ctx, OnDemandSequence.class,
                        new EQ(OnDemandSequenceXInfo.IDENTIFIER, key));
                sequence.setNextNum(sequence.getStartNum());
                sequence.setLastResetDate(new Date());
                HomeSupportHelper.get(ctx).storeBean(ctx, sequence);
            }
            catch (HomeException homeEx)
            {
                new MinorLogMsg(OnDemandSequenceManager.class, "Unable to reset sequence " + key, null).log(ctx);
            }
        }
    }


    public static void clearCache(final Context ctx, final String key)
    {
        new InfoLogMsg(OnDemandSequenceManager.class, " Clearing the cache for the ondemand sequence ", null).log(ctx);
        sequenceCache_.clear();
    }


    public static Long acquireNextIdentifier(final Context ctx, final String key, final int blockSize)
    {
        OnDemandSequence sequence = acquireNextOnDemandSequnceBlock(ctx, key, blockSize);
        if (sequence != null)
        {
            return Long.valueOf(sequence.getNextNum());
        }
        return null;
    }


    public static OnDemandSequence acquireNextOnDemandSequnceBlock(final Context ctx, final String key,
            final int blockSize)
    {
        if (blockSize == SINGLE_BLOCK_SIZE)
        {
            OnDemandSequence sequence = sequenceCache_.get(key);
            if (sequence != null)
            {
                synchronized (sequence)
                {
                    long nextNum = sequence.getNextNum();
                    if (nextNum >= sequence.getEndNum())
                    {
                        OnDemandSequence newSequence = acquireDirectlyFromSource(ctx, key, blockSize);
                        if ((newSequence.getNextNum() + blockSize) < newSequence.getEndNum())
                        {
                            sequence.setEndNum(newSequence.getNextNum() + DEFAULT_LARGE_BLOCK_SIZE);
                            sequence.setLastModified(newSequence.getLastModified());
                            sequence.setStartNum(newSequence.getStartNum());
                            sequence.setNextNum(newSequence.getNextNum());
                        }
                        else
                        {
                            sequence.setEndNum(newSequence.getEndNum());
                            sequence.setLastModified(newSequence.getLastModified());
                            sequence.setStartNum(newSequence.getStartNum());
                            sequence.setNextNum(newSequence.getNextNum());
                        }
                    }
                    else
                    {
                        sequence.setNextNum(nextNum + 1);
                    }
                    try
                    {
                        OnDemandSequence returnedSequence = (OnDemandSequence) sequence.clone();
                        returnedSequence.setEndNum(returnedSequence.getNextNum());
                        return returnedSequence;
                    }
                    catch (CloneNotSupportedException ex)
                    {
                        new MinorLogMsg(OnDemandSequenceManager.class, "unable to clone object ", ex).log(ctx);
                    }
                }
            }
            else
            {
                sequence = acquireDirectlyFromSource(ctx, key, DEFAULT_LARGE_BLOCK_SIZE);
                if (sequence != null)
                {
                    synchronized (lock_)
                    {
                        if ((sequence.getNextNum() + blockSize) < sequence.getEndNum())
                        {
                            sequence.setEndNum(sequence.getNextNum() + DEFAULT_LARGE_BLOCK_SIZE - 1);
                            sequence.setLastModified(sequence.getLastModified());
                            sequence.setStartNum(sequence.getStartNum());
                            sequence.setNextNum(sequence.getNextNum());
                        }
                        else
                        {
                            sequence.setEndNum(sequence.getEndNum());
                            sequence.setLastModified(sequence.getLastModified());
                            sequence.setStartNum(sequence.getStartNum());
                            sequence.setNextNum(sequence.getNextNum());
                        }
                        sequenceCache_.put(key, sequence);
                        try
                        {
                            OnDemandSequence returnedSequence = (OnDemandSequence) sequence.clone();
                            returnedSequence.setEndNum(returnedSequence.getNextNum());
                            return returnedSequence;
                        }
                        catch (CloneNotSupportedException ex)
                        {
                            new MinorLogMsg(OnDemandSequenceManager.class, "unable to clone object ", ex).log(ctx);
                        }
                        return sequence;
                    }
                }
            }
        }
        else if (blockSize > SINGLE_BLOCK_SIZE)
        {
            OnDemandSequence sequence = acquireDirectlyFromSource(ctx, key, blockSize);
            return sequence;
        }
        else
        {
            new MinorLogMsg(OnDemandSequenceManager.class, " Invalid block size of " + blockSize, null).log(ctx);
        }
        return null;
    }


    public static synchronized OnDemandSequence acquireDirectlyFromSource(final Context ctx, final String key,
            final int blockSize)
    {
        try
        {
            OnDemandSequence sequence = HomeSupportHelper.get(ctx).findBean(ctx, OnDemandSequence.class,
                    new EQ(OnDemandSequenceXInfo.IDENTIFIER, key));
            if (sequence != null)
            {
                if (sequence.isYearlyReset())
                {
                    Date date = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(sequence.getLastResetDate());
                    Date yearFromLastUpdate = CalendarSupportHelper.get(ctx).findDateMonthsAfter(12, date);
                    Date curDate = new Date();
                    if (curDate.after(yearFromLastUpdate))
                    {
                        reset(ctx, key);
                        sequence = HomeSupportHelper.get(ctx).findBean(ctx, OnDemandSequence.class,
                                new EQ(OnDemandSequenceXInfo.IDENTIFIER, key));
                    }
                }
                long nextNum = sequence.getNextNum();
                sequence.setNextNum(nextNum + blockSize);
                if ((nextNum + blockSize) > sequence.getEndNum())
                {
                    sequence.setNextNum(sequence.getEndNum());
                }
                HomeSupportHelper.get(ctx).storeBean(ctx, sequence);
                sequence.setNextNum(nextNum);
                if ((sequence.getNextNum() + blockSize) < sequence.getEndNum())
                {
                    sequence.setEndNum(sequence.getNextNum() + blockSize - 1);
                }
                return sequence;
            }
        }
        catch (HomeException homeEx)
        {
            new MinorLogMsg(OnDemandSequenceManager.class, "Unable to load sequence for key " + key, homeEx).log(ctx);
        }
        return null;
    }


    private void createReceiptSequence(final Context ctx)
    {
        OnDemandSequence bean = new OnDemandSequence();
        bean.setIdentifier(RECEIPT_SEQUENCE_KEY);
        bean.setNextNum(1);
        bean.setStartNum(1);
        bean.setEndNum(Long.MAX_VALUE);
        bean.isYearlyReset();
        bean.setLastModified(new Date());
        bean.setLastResetDate(new Date());
        try
        {
            HomeSupportHelper.get(ctx).createBean(ctx, bean);
        }
        catch (HomeException ex)
        {
        }
    }

    private static Map<String, OnDemandSequence> sequenceCache_ = new HashMap<String, OnDemandSequence>();
    private static Object lock_ = new Object();
    public final static int SINGLE_BLOCK_SIZE = 1;
    public final static int DEFAULT_LARGE_BLOCK_SIZE = 100;
    public final static String RECEIPT_SEQUENCE_KEY = "Receipt";
}
