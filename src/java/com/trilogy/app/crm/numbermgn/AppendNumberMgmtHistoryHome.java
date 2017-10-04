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
package com.trilogy.app.crm.numbermgn;

import java.util.Date;

import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.framework.auth.AuthMgr;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Appends a Number Mgmt History in the DB.
 *
 * @author andre.liem@redknee.com
 */
public abstract class AppendNumberMgmtHistoryHome extends HomeProxy
{

    /**
     * Default user ID.
     */
    public static final String DEFAULT_USERID = "default";


    /**
     * only use this constructor if you do not intend to call the appendHistory methods.
     *
     * @param delegate
     *            Delegate of this home.
     */
    public AppendNumberMgmtHistoryHome(final Home delegate)
    {
        this(delegate, "");
    }


    /**
     * Create a new instance of <code>AppendNumberMgmtHistoryHome</code>.
     *
     * @param delegate
     *            Delegate of this home.
     * @param homeKey
     *            Key of this home in the context.
     */
    public AppendNumberMgmtHistoryHome(final Home delegate, final Object homeKey)
    {
        this(null, delegate, homeKey);
    }


    /**
     * Create a new instance of <code>AppendNumberMgmtHistoryHome</code>.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            Delegate of this home.
     * @param homeKey
     *            Key of this home in the context.
     */
    public AppendNumberMgmtHistoryHome(final Context ctx, final Home delegate, final Object homeKey)
    {
        super(ctx, delegate);
        this.homeKey_ = homeKey;
    }


    /**
     * Appensd a history item.
     *
     * @param ctx
     *            The operating context.
     * @param history
     *            History item to be appended.
     * @return The history item appeneded.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    private Object appendHistory(final Context ctx, final NumberMgmtHistory history) throws HomeException,
        HomeInternalException
    {
        final Home home = getHome(ctx);
        if (!validate(ctx, history))
        {
            return null;
        }
        return home.create(ctx, history);
    }


    /**
     * Appensd a history item.
     *
     * @param ctx
     *            The operating context.
     * @param terminalId
     *            The item assciated with this history.
     * @param event
     *            The event associated with this history.
     * @param detail
     *            Detail of the history item.
     * @return The history item appeneded.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    public Object appendHistory(final Context ctx, final String terminalId, final HistoryEvent event,
        final String detail) throws HomeException, HomeInternalException
    {
        if (terminalId == null || terminalId.trim().length() == 0)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Cannot create history for empty trerminalId", null).log(ctx);
            }
            return null;
        }

        final PackageMgmtHistory history = new PackageMgmtHistory();
        history.setTimestamp(new Date());
        history.setTerminalId(terminalId);
        history.setEvent(event.getId());
        history.setUserId(getUserId(ctx));
        history.setDetail(detail);
        return appendHistory(ctx, history);
    }


    /**
     * Appends a TDMA history item.
     * Added during 9.6 feature "Dual Mode SIM" because we have modified packageMgmtHistory table to have 2 extra columns
     * 1) serialNumber and 2) ExternalMsisdn . To maintain the data, here adding a new method which would support 
     * Create a new bean for "PackageMgmtHistory" and not "NumberMgmtHistory". We have separated PackageMgmtHistory
     * from NumberMgmgtHistory . Now, PackageMgmtHistory extends NumberMgmtHistory
     * @param ctx
     *            The operating context.
     * @param terminalId
     *            The item associated with this history.
     * @param event
     *            The event associated with this history.
     * @param detail
     *            Detail of the history item.
     * @return The history item appeneded.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    protected Object appendTDMAHistory(final Context ctx, final String terminalId, final HistoryEvent event,TDMAPackage tdmaPackage,
        final String detail) throws HomeException, HomeInternalException
    {
        if (terminalId == null || terminalId.trim().length() == 0)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Cannot create history for empty trerminalId", null).log(ctx);
            }
            return null;
        }

        final PackageMgmtHistory history = new PackageMgmtHistory();
        history.setTimestamp(new Date());
        history.setTerminalId(terminalId);
        history.setEvent(event.getId());
        history.setUserId(getUserId(ctx));
        history.setDetail(detail);
        history.setExternalMSID(tdmaPackage.getExternalMSID());
        history.setSerialNo(tdmaPackage.getSerialNo());
        return appendHistory(ctx, history);
    }
    /**
     * Appensd a MSISDN history item.
     *
     * @param ctx
     *            The operating context.
     * @param history
     *            History item being appended.
     * @return The history item appeneded.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    private Object appendMsisdnHistory(final Context ctx, final MsisdnMgmtHistory history) throws HomeException,
        HomeInternalException
    {
        final Home home = getMsisdnHistoryHome(ctx);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "going to append history: " + history, null).log(ctx);
        }
        if (!validate(ctx, history))
        {
            return null;
        }
        return home.create(ctx, history);
    }


    /**
     * Appensd a IMSI history item.
     *
     * @param ctx
     *            The operating context.
     * @param history
     *            History item being appended.
     * @return The history item appeneded.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    public Object appendImsiHistory(final Context ctx, final ImsiMgmtHistory history) throws HomeException,
        HomeInternalException
    {
        final Home home = getImsiHistoryHome(ctx);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "going to append history: " + history, null).log(ctx);
        }
        if (!validate(ctx, history))
        {
            return null;
        }
        return home.create(ctx, history);
    }


    /**
     * Appensd a card package history item.
     *
     * @param ctx
     *            The operating context.
     * @param history
     *            History item being appended.
     * @return The history item appeneded.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    public Object appendPackageHistory(final Context ctx, final PackageMgmtHistory history) throws HomeException,
        HomeInternalException
    {
        final Home home = getPackageHistoryHome(ctx);
        if (!validate(ctx, history))
        {
            return null;
        }
        return home.create(ctx, history);
    }


    /**
     * Appends a Subscription MSISDN history item.
     *
     * @param ctx
     *            The operating context.
     * @param imsi
     *            The IMSI associated with this history.
     * @param subId
     *            The subscriber associated wit this history.
     * @param subscriptionTypeId
     * 			  The id of the subscription type of this subscription            
     * @param event
     *            The event associated with this history.
     * @param detail
     *            History item detail.
     * @return The history item appended.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    public Object appendSubscriptionMsisdnHistory(final Context ctx, final String msisdn, final String subId, final long subscriptionTypeId,
        final HistoryEvent event, final String detail) throws HomeException, HomeInternalException
    {
        final And filter = new And();

        Date curDate = new Date();
        
        filter.add(new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, msisdn));
        filter.add(new EQ(MsisdnMgmtHistoryXInfo.SUBSCRIBER_ID, subId));
        filter.add(new LTE(MsisdnMgmtHistoryXInfo.TIMESTAMP, curDate));
        filter.add(new GT(MsisdnMgmtHistoryXInfo.END_TIMESTAMP, curDate));

        toggleLatestMsisdnHistory(ctx, false, subId, new Date(), msisdn, filter);

        return appendSubscriptionMsisdnHistory(ctx, msisdn, subId, subscriptionTypeId, new Date(), event, true, detail);
    }
    
    /**
     * Appends a Account MSISDN history item.
     *
     * @param ctx
     *            The operating context.
     * @param imsi
     *            The IMSI associated with this history.
     * @param ban
     *            The id of the account associated with this history.            
     * @param event
     *            The event associated with this history.
     * @param detail
     *            History item detail.
     * @return The history item appended.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    public Object appendAccountMsisdnHistory(final Context ctx, final String msisdn, final String ban, 
        final HistoryEvent event, final String detail) throws HomeException, HomeInternalException
    {
    	// no need to toggle the latest flag as we don't need to maintain it for BAN history as there is no current use-case
        return appendAccountMsisdnHistory(ctx, msisdn, ban, new Date(), event, detail);
    }
    
    


    /**
     * Appensd a IMSI history item.
     *
     * @param ctx
     *            The operating context.
     * @param imsi
     *            The IMSI associated with this history.
     * @param subId
     *            The subscriber associated wit this history.
     * @param event
     *            The event associated with this history.
     * @param detail
     *            History item detail.
     * @return The history item appeneded.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    public Object appendImsiHistory(final Context ctx, final String imsi, final String subId,
        final HistoryEvent event, final String detail) throws HomeException, HomeInternalException
    {
        final And filter = new And();

        Date curDate = new Date();
        
        filter.add(new EQ(ImsiMgmtHistoryXInfo.TERMINAL_ID, imsi));
        filter.add(new EQ(ImsiMgmtHistoryXInfo.SUBSCRIBER_ID, subId));
        filter.add(new LTE(ImsiMgmtHistoryXInfo.TIMESTAMP, curDate));
        filter.add(new GT(ImsiMgmtHistoryXInfo.END_TIMESTAMP, curDate));
        
        toggleLatestImsiHistory(ctx, false, subId, new Date(), imsi, filter);

        return appendImsiHistory(ctx, imsi, subId, new Date(), event, true, detail);
    }


    /**
     * Appends a MSISDN history item for a Subscription detail change
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            The MSISDN associated with this history.
     * @param subId
     *            The subscriber associated wit this history.
     * @param subscriptionTypeId
     * 			  The id of the subscription type for the subscriber associated with this history.
     * @param timestamp
     *            Time stamp of this history item.
     * @param event
     *            The event associated with this history.
     * @param latest
     *            Whether this is the latest history item.
     * @param detail
     *            History item detail.
     * @return The history item appended.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    public Object appendSubscriptionMsisdnHistory(final Context ctx, final String msisdn, final String subId, final long subscriptionTypeId, final Date timestamp,
        final HistoryEvent event, final boolean latest, final String detail) throws HomeException,
        HomeInternalException
    {
        final MsisdnMgmtHistory history = new MsisdnMgmtHistory();
        
        Date effectiveDate = (Date) ctx.get(MsisdnChangeAppendHistoryHome.MSISDN_EFFECTIVE_DATE,null);
    	if(effectiveDate == null || effectiveDate.after(new Date()))
    	{
    		history.setTimestamp(timestamp);
    	}
    	else
    	{
    		history.setTimestamp(effectiveDate);
    	}
        
        history.setTerminalId(msisdn);
        history.setSubscriberId(subId);
        history.setSubscriptionType(subscriptionTypeId);
        history.setEvent(event.getId());
        history.setUserId(getUserId(ctx));
        history.setDetail(detail);
        history.setLatest(latest);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "going to append subscription history: " + history, null).log(ctx);
        }
        return appendMsisdnHistory(ctx, history);
    }

    /**
     * Appends a MSISDN history item for a Account detail change
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            The MSISDN associated with this history.
     * @param ban
     *            The id of the Account associated wit this history.
     * @param timestamp
     *            Time stamp of this history item.
     * @param event
     *            The event associated with this history.
     * @param latest
     *            Whether this is the latest history item.
     * @param detail
     *            History item detail.
     * @return The history item appended.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    public Object appendAccountMsisdnHistory(final Context ctx, final String msisdn, final String ban, final Date timestamp,
        final HistoryEvent event, final String detail) throws HomeException,
        HomeInternalException
    {
        final MsisdnMgmtHistory history = new MsisdnMgmtHistory();
        history.setTimestamp(timestamp);
        history.setTerminalId(msisdn);
        history.setBAN(ban);
        history.setEvent(event.getId());
        history.setUserId(getUserId(ctx));
        history.setDetail(detail);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "going to append account history: " + history, null).log(ctx);
        }
        return appendMsisdnHistory(ctx, history);
    }
    /**
     * Appensd a IMSI history item.
     *
     * @param ctx
     *            The operating context.
     * @param imsi
     *            The IMSI associated with this history.
     * @param subId
     *            The subscriber associated wit this history.
     * @param timestamp
     *            Time stamp of this history item.
     * @param event
     *            The event associated with this history.
     * @param latest
     *            Whether this is the latest history item.
     * @param detail
     *            History item detail.
     * @return The history item appeneded.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    public Object appendImsiHistory(final Context ctx, final String imsi, final String subId, final Date timestamp,
        final HistoryEvent event, final boolean latest, final String detail) throws HomeException,
        HomeInternalException
    {
        final ImsiMgmtHistory history = new ImsiMgmtHistory();
        history.setTimestamp(timestamp);
        history.setTerminalId(imsi);
        history.setSubscriberId(subId);
        history.setEvent(event.getId());
        history.setUserId(getUserId(ctx));
        history.setDetail(detail);
        history.setLatest(latest);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "going to append history: " + history, null).log(ctx);
        }
        return appendImsiHistory(ctx, history);
    }


    /**
     * Appensd a card package history item.
     *
     * @param ctx
     *            The operating context.
     * @param packId
     *            Package ID associated with this history item..
     * @param event
     *            Event associated with this history item.
     * @param detail
     *            Detail of the history item.
     * @return The history item appeneded.
     * @throws HomeException
     *             Thrown by home.
     * @throws HomeInternalException
     *             Thrown by home.
     */
    public Object appendPackageHistory(final Context ctx, final String packId, final HistoryEvent event,
        final String detail) throws HomeException, HomeInternalException
    {
        final PackageMgmtHistory history = new PackageMgmtHistory();
        history.setTimestamp(new Date());
        history.setTerminalId(packId);
        history.setEvent(event.getId());
        history.setUserId(getUserId(ctx));
        history.setDetail(detail);
        return appendPackageHistory(ctx, history);
    }


    /**
     * Returns the user ID to be used for appending history in the provided context.
     *
     * @param ctx
     *            The operating context.
     * @return The user ID to be used for appending history items. This is the name of the
     *         current user of the context. If the context has no user associated with it,
     *         {@link AppendNumberMgmtHistoryHome#DEFAULT_USERID} is used.
     */
    public String getUserId(final Context ctx)
    {
        final AuthMgr authMgr = new AuthMgr(ctx);
        final User principal = (User) authMgr.getPrincipal();
        // FIXME: there will be no principal in a multinode envirnoment.
        if (principal != null)
        {
            return principal.getId();
        }
        return DEFAULT_USERID;
    }


    /**
     * Validates a history item.
     *
     * @param ctx
     *            The operating context.
     * @param history
     *            History item to be validated.
     * @return Returns <code>true</code> if the history item is valid,
     *         <code>false</code> otherwise.
     */
    private boolean validate(final Context ctx, final NumberMgmtHistory history)
    {
        return history != null && history.getTerminalId() != null && history.getTerminalId().length() > 0;
    }


    /**
     * Returns the home in current context.
     *
     * @param ctx
     *            The operating context.
     * @return Home in operating context.
     */
    private Home getHome(final Context ctx)
    {
        return (Home) ctx.get(this.homeKey_);
    }


    /**
     * Returns package history home.
     *
     * @param ctx
     *            The operating context.
     * @return The package history home in the current context.
     */
    private Home getPackageHistoryHome(final Context ctx)
    {
        return (Home) ctx.get(PackageMgmtHistoryHome.class);
    }


    /**
     * Returns the MSISDN history home.
     *
     * @param ctx
     *            The operating context.
     * @return The MSISDN history home in the current context.
     */
    private Home getMsisdnHistoryHome(final Context ctx)
    {
        return (Home) ctx.get(MsisdnMgmtHistoryHome.class);
    }


    /**
     * Returns the IMSI history home.
     *
     * @param ctx
     *            The operating context.
     * @return The IMSI history home in the current context.
     */
    private Home getImsiHistoryHome(final Context ctx)
    {
        return (Home) ctx.get(ImsiMgmtHistoryHome.class);
    }


    /**
     * Returns the history support class.
     *
     * @param ctx
     *            The operating context.
     * @return The history support class in the current context.
     */
    public HistoryEventSupport getHistoryEventSupport(final Context ctx)
    {
        return (HistoryEventSupport) ctx.get(HistoryEventSupport.class);
    }


    /**
     * Toggles the latest bean in the msisdn history table with the end timestamp
     * required.
     *
     * @param ctx
     *            the operating context
     * @param latest
     *            checks if it's the latest in the table
     * @param subscriberId
     *            the subscriber id to test
     * @param endTimestamp
     *            the timestamp to set
     * @param msisdn
     *            the msisdn to filter
     * @param predicate
     *            The predicate to execute on the where home
     * @return true if adding a new bean is needed
     * @throws HomeException
     *             the wrapping home exception
     */
    public boolean addNewMsisdnHistoryRequired(final Context ctx, final boolean latest, final String subscriberId,
        final Date endTimestamp, final String msisdn, final Object predicate) throws HomeException
    {

        final Home historyHome = (Home) ctx.get(MsisdnMgmtHistoryHome.class);

        final ToggleLatestVisitor visitor = (ToggleLatestVisitor) historyHome.where(ctx, predicate).forEach(ctx,
            new ToggleLatestVisitor(latest, subscriberId, endTimestamp, historyHome));

        final boolean requires = visitor.isAddNewHistoryRequired();

        return requires;
    }


    /**
     * Toggles the latest bean in the imsi history table with the end timestamp
     * required.
     *
     * @param ctx
     *            the operating context
     * @param latest
     *            checks if it's the latest in the table
     * @param subscriberId
     *            the subscriber id to test
     * @param endTimestamp
     *            the timestamp to set
     * @param imsi
     *            the imsi to filter
     * @param predicate
     *            The predicate to execute on the where home
     * @return true if adding a new bean is needed
     * @throws HomeException
     *             the wrapping home exception
     */
    public boolean addNewImsiRequired(final Context ctx, final boolean latest, final String subscriberId,
        final Date endTimestamp, final String imsi, final Object predicate) throws HomeException
    {

        final Home historyHome = (Home) ctx.get(ImsiMgmtHistoryHome.class);

        final ToggleLatestVisitor visitor = (ToggleLatestVisitor) historyHome.where(ctx, predicate).forEach(ctx,
            new ToggleLatestVisitor(latest, subscriberId, endTimestamp, historyHome));

        final boolean requires = visitor.isAddNewHistoryRequired();

        return requires;
    }


    /**
     * Toggles the previous row from the previous history record
     *
     * @param ctx
     *            the operating context
     * @param latest
     *            checks if it's the latest in the table
     * @param subscriberId
     *            the subscriber id to test
     * @param endTimestamp
     *            the timestamp to set
     * @param msisdn
     *            the msisdn to filter
     * @param predicate
     *            The predicate to execute on the where home
     * @throws HomeException
     *             the wrapping home exception
     */
    private void toggleLatestMsisdnHistory(final Context ctx, final boolean latest, final String subscriberId,
        final Date endTimestamp, final String msisdn, final Object predicate) throws HomeException
    {
        final Home historyHome = (Home) ctx.get(MsisdnMgmtHistoryHome.class);

        historyHome.where(ctx, predicate).forEach(ctx,
            new ToggleLatestSubscriberVisitor(latest, subscriberId, endTimestamp, historyHome));

    }


    /**
     * Toggles the previous row from the previous history record
     *
     * @param ctx
     *            the operating context
     * @param latest
     *            checks if it's the latest in the table
     * @param subscriberId
     *            the subscriber id to test
     * @param endTimestamp
     *            the timestamp to set
     * @param msisdn
     *            the msisdn to filter
     * @param predicate
     *            The predicate to execute on the where home
     * @throws HomeException
     *             the wrapping home exception
     */
    private void toggleLatestImsiHistory(final Context ctx, final boolean latest, final String subscriberId,
        final Date endTimestamp, final String imsi, final Object predicate) throws HomeException
    {
        final Home historyHome = (Home) ctx.get(ImsiMgmtHistoryHome.class);

        historyHome.where(ctx, predicate).forEach(ctx,
            new ToggleLatestSubscriberVisitor(latest, subscriberId, endTimestamp, historyHome));

    }

    /**
     * The key used by this home.
     */
    private final Object homeKey_;
}
