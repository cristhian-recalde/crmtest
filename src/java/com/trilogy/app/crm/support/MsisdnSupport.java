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
package com.trilogy.app.crm.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.numbermgn.LeastRecentVisitor;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;


/**
 * Support class for MSISDN.
 *
 * @author joe.chen@redknee.com
 */
public final class MsisdnSupport
{

    /**
     * Creates a new <code>MsisdnSupport</code> instance. This method is made private to
     * prevent instantiation of utility class.
     */
    private MsisdnSupport()
    {
        // empty
    }


    /**
     * Gets the Msisdn with the given mobile number.
     *
     * @param ctx
     *            The operating context.
     * @param number
     *            Mobile number.
     * @return The {@link Msisdn} object with the provided mobile number, or
     *         <code>null</code> if none exists.
     * @throws HomeException
     *             Thrown if there are problems looking up the mobile number.
     */
    public static Msisdn getMsisdn(Context ctx, final String number) throws HomeException
    {
        if (number == null)
        {
            return null;
        }

        Home home = (Home) ctx.get(MsisdnHome.class);
        
        final Msisdn msisdn = (Msisdn) home.find(ctx, number);
        return msisdn;
    }

    
    public static  Msisdn lookupMsisdn(final Context ctx,final String msisdn)
    throws HomeException
    {
    	final Home home = (Home)ctx.get(MsisdnHome.class);
    	if (home == null)
    	{
    		throw new HomeException(
            "Could not find MsisdnHome in context.");
    	}
    
    	final Msisdn msisdnObject = (Msisdn)home.find(ctx,msisdn);
    	if (msisdnObject == null)
    	{
    		throw new HomeException(
            "Could not find Msisdn for \"" + msisdn + "\".");
    	}

    	return msisdnObject;
    }
    
    /**
     * Retrieves the msisdn given the spid and mobile number
     * 
     * @param ctx
     * @param spid
     * @param number
     * @return
     * @throws HomeException
     */
    public static Msisdn getMsisdn(final Context ctx, final int spid, final String number) throws HomeException
    {
        if (number == null)
        {
            return null;
        }

        final Home home = (Home) ctx.get(MsisdnHome.class);
        And and = new And();
        and.add(new EQ(MsisdnXInfo.SPID, Integer.valueOf(spid)));
        and.add(new EQ(MsisdnXInfo.MSISDN, number));
        
        final Msisdn msisdn = (Msisdn) home.find(ctx, and);
        return msisdn;
    }

    /**
     * Sets the subscriber type of the MSISDN to the type of the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param number
     *            The MSISDN.
     * @param sub
     *            The subscriber whose type is used in the MSISDN.
     * @throws HomeException
     *             Thrown if there are problems updating the MSISDN.
     */
    public static void setMsisdnType(final Context ctx, final String number, final Subscriber sub) throws HomeException
    {
        setMsisdnType(ctx, number, sub.getSubscriberType());
    }


    /**
     * Sets the subscriber type of the MSISDN.
     *
     * @param ctx
     *            The operating context.
     * @param number
     *            The MSISDN.
     * @param type
     *            The subscriber type to set.
     * @throws HomeException
     *             Thrown if there are problems updating the MSISDN.
     */
    public static void setMsisdnType(final Context ctx, final String number, final SubscriberTypeEnum type)
        throws HomeException
    {
        final Msisdn msisdn = getMsisdn(ctx, number);
        if (msisdn != null)
        {
            msisdn.setSubscriberType(type);

            final Home msisdnHome = (Home) ctx.get(MsisdnHome.class);
            msisdnHome.store(ctx, msisdn);
        }
    }


    /**
     * finds the first free msisdn for a given spid and subscriber type.
     *
     * @param ctx
     *            context used for locating the Msisdn home and for logging
     * @param spid
     *            the spid of the msisdn that we want to find
     * @param type
     *            the type of the subscriber. Currently it is 0 for postpaid and 1 for
     *            prepaid
     * @return a free msisdn or null if none was found.
     */
    public static String getFreeMsisdn(final Context ctx, final int spid, final short type)
    {
        final Home h = (Home) ctx.get(MsisdnHome.class);

        final SubscriberTypeEnum subType = SubscriberTypeEnum.get(type);

        try
        {
            final And condition = new And();
            condition.add(new EQ(MsisdnXInfo.SPID, Integer.valueOf(spid)));
            condition.add(new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, subType));
            condition.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.AVAILABLE));

            final Msisdn msisdn = (Msisdn) h.find(ctx, condition);

            if (msisdn != null)
            {
                return msisdn.getMsisdn();
            }
        }
        catch (final HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg("[MsisdnSupport.getFreeMsisdn]", e.getMessage(), e).log(ctx);
            }
        }

        return null;
    }
    
    /**
     * finds the first free msisdn for a given spid and subscriber type.
     * 
     * @param ctx
     *            context used for locating the Msisdn home and for logging
     * @param spid
     *            the spid of the msisdn that we want to find
     * @param group
     *            the group of msisdn from which the msisdn must be found
     * @param subType
     *            the type of the subscriber. Currently it is 0 for postpaid and 1 for
     *            prepaid            
     * @return a free msisdn or null if none was found.
     */
    public static String getFreeMsisdnFromGroup(final Context ctx, final int spid, final int group, SubscriberTypeEnum subType) throws HomeException
    {
        final Home h = (Home) ctx.get(MsisdnHome.class);

        final And condition = new And();
        condition.add(new EQ(MsisdnXInfo.SPID, Integer.valueOf(spid)));
        condition.add(new EQ(MsisdnXInfo.GROUP, group));
        condition.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.AVAILABLE));
        condition.add(new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, subType));

        final Msisdn msisdn = (Msisdn) h.find(ctx, condition);
        if (msisdn != null)
        {
            return msisdn.getMsisdn();
        }else
        {
            return null;
        }
        
    }
    
    
    
    /**
     * finds the first free msisdn for a given spid and subscriber type.
     *
     * @param ctx
     *            context used for locating the Msisdn home and for logging
     * @param spid
     *            the spid of the msisdn that we want to find
     * @param type
     *            the type of the subscriber. Currently it is 0 for postpaid and 1 for
     *            prepaid
     * @return a free msisdn or null if none was found.
     */
    public static String getFreeMsisdn(final Context ctx, final int spid, final int group, SubscriberTypeEnum subType)
    {
        final Home h = (Home) ctx.get(MsisdnHome.class);
        try
        {
            final And condition = new And();
            condition.add(new EQ(MsisdnXInfo.SPID, Integer.valueOf(spid)));
            condition.add(new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, subType));
            condition.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.AVAILABLE));

            final Msisdn msisdn = (Msisdn) h.find(ctx, condition);

            if (msisdn != null)
            {
                return msisdn.getMsisdn();
            }
        }
        catch (final HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg("[MsisdnSupport.getFreeMsisdn]", e.getMessage(), e).log(ctx);
            }
        }

        return null;
    }
    


    /**
     * Return a collection of free mobile numbers belonging to a given service provider
     * and subscriber type. Synonym of {@link #getFreeMsisdns(Context, int, short, int)}.
     *
     * @param context
     *            The operating context.
     * @param spid
     *            Service provider ID.
     * @param type
     *            Subscriber type.
     * @param count
     *            Number of mobile numbers to return.
     * @return A collection of <code>count</code> number of free mobile numbers
     *         belonging to the provided service provider and subscriber type.
     */
    public static Collection<String> getFreeNumbers(final Context context, final int spid, final short type,
        final int count)
    {
        /*
         * TODO [Cindy] 2008-02-08: This method is not called anywhere in the code.  Remove?
         */
        return getFreeMsisdns(context, spid, type, count);
    }


    /**
     * Finds the first free msisdn for a given spid and subscriber type.
     *
     * @param context
     *            context used for locating the Msisdn home and for logging
     * @param spid
     *            the spid of the msisdn that we want to find
     * @param type
     *            the type of the subscriber. Currently it is 0 for postpaid and 1 for
     *            prepaid
     * @param count
     *            the number of msisdns to return
     * @return a free msisdn or null if none was found.
     */
    public static Collection<String> getFreeMsisdns(final Context context, final int spid, final short type,
        final int count)
    {
        /*
         * TODO [Cindy] 2008-02-08: This method is not called anywhere in the code.  Remove?
         */

        if (count <= 0)
        {
            return null;
        }

        Home home = (Home) context.get(MsisdnHome.class);
        final List<String> ret = new ArrayList<String>();

        final SubscriberTypeEnum subType = SubscriberTypeEnum.get(type);

        final And predicate = new And();
        predicate.add(new EQ(MsisdnXInfo.SPID, Integer.valueOf(spid)));
        predicate.add(new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, subType));
        predicate.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.AVAILABLE));
        home = home.where(context, predicate);

        try
        {
            home.forEach(context, new Visitor()
            {

                public void visit(final Context vCtx, final Object obj)
                {
                    if (ret.size() >= count)
                    {
                        throw new AbortVisitException("Processing is over");
                    }

                    ret.add(((Msisdn) obj).getMsisdn());
                }
            });
        }
        catch (final HomeException e)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg("[MsisdnSupport.getFreeMsisdn]", e.getMessage(), e).log(context);
            }
        }

        return ret;
    }


    /**
     * Removes a MSISDN from the home. Under normal circumstances, MSISDNs should not be
     * removed from the home. However, this ability may be needed in certain special
     * cases. In the future, we may consider adding deprecation to MSISDN instead.
     *
     * @param ctx
     *            The operating context.
     * @param number
     *            MSISDN to be removed.
     * @throws HomeException
     *             Thrown if there are problems removing the MSISDN.
     */
    public static void removeMsisdn(final Context ctx, final String number) throws HomeException
    {
        // TODO 2006-10-24 are we allowed to remove MSISDNS ???
        // TODO 2006-10-24 do not remove MSISDN. need to put in AVAILABLE, note on MSISDN
        // history
        final Msisdn msisdnObj = getMsisdn(ctx, number);

        if (msisdnObj == null)
        {
            throw new HomeException("Unable to lookup Msisdn.  No Msisdn object for msisdn: " + number);
        }
        final Home home = (Home) ctx.get(MsisdnHome.class);
        home.remove(msisdnObj);
    }

    
    
    @SuppressWarnings("unchecked")
    public static Collection<Msisdn> getAcquiredMsisdn(final Context ctx, final String accountId)
    {
        Collection<Msisdn> acquiredMsisdnColl = new ArrayList();

        Home home=(Home)ctx.get(MsisdnHome.class);
        try
        {
            acquiredMsisdnColl= home.where( ctx,
                                            new And()
                                                .add(new EQ(MsisdnXInfo.BAN,accountId))
                                                .add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.IN_USE))).selectAll();
        }
        catch (HomeException e)
        {
            LogSupport.major(ctx, MsisdnSupport.class, "Encountered a HomeException while fetching acquired MSISDNs of the Subscriber Account Id: ["+accountId+"]", e);
        }
        return acquiredMsisdnColl;
    }

    @SuppressWarnings("unchecked")
    public static Collection<Msisdn> getAcquiredAndHeldMsisdn(final Context ctx, final String accountId)
    {
        Collection<Msisdn> acquiredMsisdnColl = new ArrayList();

        Home home=(Home)ctx.get(MsisdnHome.class);
        try
        {
            acquiredMsisdnColl = home.where(
                ctx,
                new And()
                    .add(new EQ(MsisdnXInfo.BAN,accountId))
                    .add(new Or()
                        .add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.IN_USE))
                        .add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.HELD)))).selectAll();
        }
        catch (HomeException e)
        {
            LogSupport.major(ctx, MsisdnSupport.class, "Encountered a HomeException while fetching acquired MSISDNs of the Subscriber Account Id: ["+accountId+"]", e);
        }
        return acquiredMsisdnColl;
    }

    /**
     * Returns the BAN owning or last owned this MSISDN on the provided date.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            The MSISDN to do the look up on
     * @param date
     *            The date to look up.
     * @return The BAN owning or last owned this MSISDN on the provided date.  Returns null if the MSISDN was never owned.
     */
    public static String getBAN(final Context ctx, String msisdn, Date date)
    {
        String result = "";
        if (date == null)
        {
            new DebugLogMsg(MsisdnSupport.class.getName(), "Date is null. Will use current time to find the associated BAN for [msisdn=" + msisdn + "]", null)
                    .log(ctx);
            date = new Date();
        }

        new DebugLogMsg(MsisdnSupport.class.getName(), "Looking up BAN from MsisdnMgmtHistory for [msisdn=" + msisdn + "]...", null).log(ctx);
            
        Home historyHome =  (Home) ctx.get(MsisdnMgmtHistoryHome.class);
        historyHome = historyHome.where(ctx, new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, msisdn));
        try
        {
            final And and = new And();
            and.add(new LTE(MsisdnMgmtHistoryXInfo.TIMESTAMP, date));
            and.add(new GT(MsisdnMgmtHistoryXInfo.END_TIMESTAMP, date));

            MsisdnMgmtHistory history = (MsisdnMgmtHistory) historyHome.find(ctx, and);
            if (history != null)
            {
                new DebugLogMsg(MsisdnSupport.class.getName(), "Found a record in MSISDN History " + history, null).log(ctx);
                result = history.getBAN();
            }
            else
            {

                /*
                 * assuming the date is earlier than any period in the
                 * MsisdnMgmtHistory, attempt to match with the earliest period
                 * instead.
                 */
                final LeastRecentVisitor leastRecentVisitor = (LeastRecentVisitor) historyHome.forEach(ctx,
                    new LeastRecentVisitor(MsisdnMgmtHistoryXInfo.TIMESTAMP));
                history = (MsisdnMgmtHistory) leastRecentVisitor.getValue();
                if (history != null)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(MsisdnSupport.class.getName(), "Return least recent MSISDN history [record=" + history + "] for [date="
                            + date + "]", null).log(ctx);
                    }
                    result = history.getBAN();
                }
                else
                {
                    Msisdn msisdnObj = MsisdnSupport.getMsisdn(ctx, msisdn);
                    if (msisdnObj != null)
                    {
                        new DebugLogMsg(MsisdnSupport.class.getName(), "Returning current BAN in MSISDN table [" + msisdnObj + "]", null).log(ctx);
                        result = msisdnObj.getBAN();
                    }
                    else
                    {
                        new MinorLogMsg(MsisdnSupport.class.getName(), "MSISDN " + msisdn + " not found", null).log(ctx);
                        result = null;
                    }
                }
            }
        }
        catch (final HomeException hEx)
        {
            new MinorLogMsg(MsisdnSupport.class.getName(), "fail to look up BAN  for MSISDN " + msisdn + " from MsisdnMgmtHistory", hEx).log(ctx);
        }

        return result;
    }

}
