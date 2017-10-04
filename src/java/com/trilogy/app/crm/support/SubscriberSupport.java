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

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.omg.CORBA.LongHolder;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bas.tps.CreationPreferenceEnum;
import com.trilogy.app.crm.bas.tps.PricePlanUnitEnum;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateHome;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateXInfo;
import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.NoteHome;
import com.trilogy.app.crm.bean.NoteXInfo;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.SMSUsageView;
import com.trilogy.app.crm.bean.SctAuxiliaryBundle;
import com.trilogy.app.crm.bean.SctAuxiliaryService;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberCSVSupport;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXDBHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionClass;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bundle.BundleStatusEnum;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.client.smsb.AppSmsbClient;
import com.trilogy.app.crm.dispute.DisputeAmountVisitor;
import com.trilogy.app.crm.extension.SubscriberTypeDependentExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.filter.SubscriberHomeAdaptedVisitor;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.app.crm.transfer.TransferDisputeHome;
import com.trilogy.app.crm.transfer.TransferDisputeStatusEnum;
import com.trilogy.app.crm.transfer.TransferDisputeXInfo;
import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.csv.Constants;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.Contains;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.FunctionVisitor;
import com.trilogy.framework.xhome.visitor.SetBuildingVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.xdb.Min;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xdb.visitor.SingleValueXDBVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriptionState;
import com.trilogy.product.s2100.ErrorCode;
import com.trilogy.product.s2100.oasis.param.ParameterSetHolder;
import com.trilogy.util.snippet.log.Logger;
import com.trilogy.app.crm.bean.DeactivationReasonCodeMappingXInfo;
import com.trilogy.app.crm.bean.DeactivationReasonCodeMapping;
import com.trilogy.app.crm.bean.DeactivationActionsEnum;

/**
 * Support class for subscriber.
 *
 * @author gary.anderson@redknee.com
 */
public class SubscriberSupport
{

    /**
     * Note content for subscriber note
     */
    private static final String SUBSCRIBER_MOVED_NOTE = "Subscriber moved";

    // subscriber will be assigned "NULLID" during conversion.
    // SubscriberIdAssignHome only assigns new id to the
    // converted subscriber when the id is empty string or NULL, but
    // setId method of subscriber profile does not accept empty string or NULL.
    // To resolve this problem, the converted subscriber will be given the "NULLID"
    // and SubscriberIdAssignHome will recognize this and assign new id to the
    // converted subscriber.

    public static final String NULLID = "NULLID";


    /**
     * Creates a new <code>SubscriberSupport</code> instance. This method is made private
     * to prevent instantiation of utility class.
     */
    private SubscriberSupport()
    {
        // empty
    }

    /**
     * Used to indicate that a calculated value is invalid.
     */
    public static final long INVALID_VALUE = Long.MIN_VALUE;
    public static final long MAX_VALUE = Long.MAX_VALUE;

    /**
     * Prefix for the monitor used to ensure that only one thread is acquiring a
     * subscriberId on a "per Account" basis.
     */
    private final static String SUBSCRIBER_ID_GENERATION_LOCK_PREFIX = "SubscriberIdGenerator:";
    private final static Map<String, Object> SUBSCRIBER_ID_GENERATION_LOCK_MAP = new WeakHashMap<String, Object>();


    /**
     * Acquires the next available subscriber identifier for the given account.
     *
     * @param context
     *            The operating context.
     * @param accountIdentifier
     *            The identifier of the account.
     * @return A subscriber identifier.
     * @throws HomeException
     *             Thrown if there are problems determining the next available subscriber
     *             identifier.
     */
    public static String acquireNextSubscriberIdentifier(final Context context, final String accountIdentifier)
        throws HomeException
    {
        Home home = (Home) context.get(Common.ACCOUNT_CACHED_HOME);
        if (home == null)
        {
            home = (Home) context.get(AccountHome.class);
        }
        if (home == null)
        {
            throw new HomeException("System error: could not locate AccountHome in context.");
        }

        final String lockKey = SUBSCRIBER_ID_GENERATION_LOCK_PREFIX + accountIdentifier;
        Object lock = null;
        synchronized (SUBSCRIBER_ID_GENERATION_LOCK_MAP)
        {
            lock = SUBSCRIBER_ID_GENERATION_LOCK_MAP.get(lockKey);
            if (lock != null)
            {
                // Unwrap the weak reference, which could still return null if the GC got
                // to it since we
                // looked it up in the map. See the java.lang.ref.WeakReference JavaDoc
                // for more info
                lock = ((WeakReference<String>) lock).get();
            }

            if (lock == null)
            {
                // The lock was no longer in the WeakHashMap, so we have to put it in for
                // other threads
                // to use. It is guaranteed to not be garbage collected until the the last
                // thread holding
                // the lockKey instance falls out of scope.
                SUBSCRIBER_ID_GENERATION_LOCK_MAP.put(lockKey, new WeakReference<String>(lockKey));
                lock = lockKey;
            }

            // At this point, we are holding a reference to the lockKey so the WeakHashMap
            // will not remove this entry
        }
        synchronized (lock)
        {
            final Account account = (Account) home.find(context, accountIdentifier);
            final Account lookupAccount = (Account) context.get(Lookup.ACCOUNT);
            final Account ctxAccount = (Account) context.get(Account.class);

            final int nextIdentifier = account.getNextSubscriberId();

            account.setNextSubscriberId(nextIdentifier + 1);
            
            // TT#9080616015: Updating the nextSubscriberId also in the account objects in the context.
            if (lookupAccount != null && lookupAccount.getBAN().equals(account.getBAN()))
            {
                lookupAccount.setNextSubscriberId(nextIdentifier + 1);
            }
            
            if (ctxAccount != null && ctxAccount.getBAN().equals(account.getBAN()))
            {
                ctxAccount.setNextSubscriberId(nextIdentifier + 1);
            }

            home.store(context, account);
            
            if (LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY))
            {
                return accountIdentifier + String.format("%05d", nextIdentifier);
            } else
            {    
                return accountIdentifier + "-" + Integer.toString(nextIdentifier);
            }    
                
        }

    }


    /**
     * Looks-up and returns the account to which the given subscriber belongs.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to look-up an account.
     * @return The account of the given subscriber.
     */
    public static Account lookupAccount(final Context context, final Subscriber subscriber)
    {
        return lookupAccount(context, subscriber.getBAN());
    }

    /**
     * Looks-up and returns the account to which the given subscriber belongs.
     *
     * @param context
     *            The operating context.
     * @param ban
     *            The ban for which to look-up an account.
     * @return The account of the given subscriber.
     */
    public static Account lookupAccount(final Context context, final String ban)
    {
        // shortcut
        Account account = (Account) context.get(Lookup.ACCOUNT);
        if (account != null)
        {
            if (account.getBAN().equals(ban))
            {
                return account;
            }
            else if (LogSupport.isDebugEnabled(context))
            {
                final String msg = "CONTEXT: different Account in context! BAN: [" + account.getBAN()
                        + "] were looking for BAN: [" + ban + "]";
                LogSupport.debug(context, SubscriberSupport.class.getName(), msg, new Exception(msg));
            }
        }

        /*
         * because find on an account with null is going to try to set the accountID in a
         * bean for finding purposes we'll check beforehand if the search should not be
         * done at all
         */
        if (ban != null && ban.length() > 0)
        {
            try
            {
                account = HomeSupportHelper.get(context).findBean(context, Account.class, ban);
            }
            catch (Exception e)
            {
                LogSupport.minor(context, SubscriberSupport.class.getName(),
                        "Exception while fetching Account BAN: [" + ban + "]", e);
            }
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, SubscriberSupport.class.getName(),
                    "lookupAccount()[findByPrimaryKey] Account BAN: [" + ban + "]");
            }
        }
        return account;
    }

    /**
     * Look up a subscriber in the Subscriber home.
     * If you need to check the context first, use getSubscriber()
     *
     * @param context
     *            The operating context.
     * @param subId
     *            Subscriber ID.
     * @return The subscriber with the provided ID.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscriber.
     */
    public static Subscriber lookupSubscriberForSubId(final Context context, final String subId) throws HomeException
    {
        final Home home = (Home) context.get(SubscriberHome.class);
        if (home == null)
        {
            throw new HomeException("Application Initialization Error: subscriber home not found in context");
        }
        return (Subscriber) home.find(context, subId);
    }


    /**
     * Looks-up and returns the earliest known activity date for the given account. That
     * is, the date of the earliest found adjustment or transaction for the account. The
     * current date is returned if no activity is found.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to get the earliest activity date.
     * @return The earliest known activity date.
     */
    public static Date lookupEarliestActivityDate(final Context ctx, final Subscriber subscriber)
    {
        // TODO - 2003-12-05 - Refactor this taking into account the similar code in AccountSupport.

        Date earliestDate = new Date();

        // First check the CallDetails.
        if (subscriber != null && subscriber.getId() != null && subscriber.getId().length() > 0)
        {
            try
            {
                final Date earliestCallDetailDate = CallDetailSupportHelper.get(ctx).getEarliestDate(ctx, subscriber.getBAN(),
                        subscriber.getId());

                if (earliestCallDetailDate != null && earliestCallDetailDate.before(earliestDate))
                {
                    earliestDate = earliestCallDetailDate;
                }
            }
            catch (final HomeException e)
            {
                Logger.debug(ctx, AccountSupport.class, "Lookup of earliest CallDetail failed for subscriber ["
                        + subscriber.getId() + "]", e);
            }
        }

        // Second, check the Transactions.
        {
            final XDB xdb = (XDB) ctx.get(XDB.class);

            final String tableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, TransactionHome.class,
                TransactionXInfo.DEFAULT_TABLE_NAME);

            final XStatement sql = new SimpleXStatement("select min(receiveDate) from " + tableName
                + " where subscriberID = ? ")
            {

                @Override
                public void set(final Context ctx, final XPreparedStatement ps) throws SQLException
                {
                    ps.setString(subscriber.getId());
                }
            };

            Date earliestTransactionDate = null;
            try
            {
                /* The condition to check if the XDB class was installed in the context was added to 
                 * ease our unit testing problems.  Rather than hacking and installing a dumb XBD configuration
                 * (it is impossible to do unless you duplicate com.redknee.framework.xhome.xdb.Install), 
                 * we prefer to catch the use here and delegate to a the Home query if the XDB query is not 
                 * possible. */
                if (xdb != null)
                {
                    earliestTransactionDate = SingleValueXDBVisitor.getDate(ctx, xdb, sql);
                }
                else
                {
                    Home home = (Home) ctx.get(TransactionHome.class);
                    Transaction transaction = (Transaction) home.cmd(ctx, 
                            new Min(TransactionXInfo.TRANS_DATE, new EQ(TransactionXInfo.SUBSCRIBER_ID, subscriber.getId())));
                    if (transaction != null)
                    {
                        earliestTransactionDate = transaction.getTransDate();
                    }
                }
            }
            catch (final HomeException exception)
            {
                LogSupport.major(ctx, SubscriberSupport.class.getName(),
                        "Lookup of earliest Transaction failed for SubscriberID : " + subscriber.getId(), exception);
            }

            if (earliestTransactionDate != null && earliestTransactionDate.before(earliestDate))
            {
                earliestDate = earliestTransactionDate;
            }
        }

        return earliestDate;
    }


    /**
     * Looks-up and returns the Msisdn object for the given MSISDN string.
     *
     * @param context
     *            The operating context.
     * @param msisdn
     *            The MSISDN of the subscriber to look-up.
     * @return The Msisdn object if one exists for the MSISDN; null otherwise.
     * @throws HomeException
     *             Thrown if there is a problem with the SubscriberHome.
     */
    public static Msisdn lookupMsisdnObjectForMSISDN(final Context context, final String msisdn) throws HomeException
    {
        final Home msisdnHome = (Home) context.get(MsisdnHome.class);
        if (msisdnHome == null)
        {
            throw new HomeException("Could not find MsisdnHome in context.");
        }

        final Msisdn msisdnObject = (Msisdn) msisdnHome.find(context, msisdn);

        return msisdnObject;
    }


    /**
     * Looks-up and returns the Subscriber ID for the given MSISDN.
     *
     * @param context
     *            The operating context.
     * @param msisdn
     *            The MSISDN of the subscriber to look-up.
     * @param activateDate
     *            The date the Msisdn is active.
     * @return The subscriber ID if one exists for the MSISDN; null otherwise.
     * @throws HomeException
     *             Thrown if there is a problem with the SubscriberHome.
     */
    public static String lookupSubscriberIdForMSISDN(final Context context, final String msisdn, final Date activateDate)
        throws HomeException
    {
        SubscriptionType subscriptionType = SubscriptionType.getINSubscriptionType(context);
        
        if( subscriptionType == null )
        {
            new DebugLogMsg(SubscriberSupport.class, "Unable to find IN Subscription, please make sure that a IN subscription is defined.", null).log(context);
            throw new HomeException("Request to lookupSubscriberIdForMSISDN received without a subscriptionType specified, but we are unable to find a IN Subscripion Type defined in the system.");
        }

        return lookupSubscriberIdForMSISDN(context, msisdn, subscriptionType.getId(), activateDate);
    }
    
    /**
     * 
     * @param context
     * @param msisdn
     * @param subscriptionTypeId
     * @param activateDate
     * @return Found Subscriber-ID or null if not found
     * @throws HomeException
     */
    public static String lookupSubscriberIdForMSISDN(final Context context, final String msisdn, final long subscriptionTypeId, final Date activateDate)
    throws HomeException
    {
        final Msisdn msisdnObject = MsisdnSupport.getMsisdn(context, msisdn);
        if (msisdnObject == null)
        {
            if(LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(SubscriberSupport.class.getName(),"Msisdn object want not found. Returning null - Subscriber Does not exist",null).log(context);
            }
            // why throw an exception..it is a valid case
            // throw new HomeException("Could not find MSISDN record for \"" + msisdn +
            // "\".");
            return null;
        }

        final String subId = msisdnObject.getSubscriberID(context, subscriptionTypeId, activateDate);
        if (subId == null || SafetyUtil.safeEquals(Subscriber.DEFAULT_ID, subId))
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, SubscriberSupport.class, ("Cannot find subscriber for MSISDN " + msisdn
                    + " on " + activateDate));
            }
        }
        return subId;
    }
    /**
     * Looks-up and returns the Subscriber for the given MSISDN.
     *
     * @param context
     *            The operating context.
     * @param msisdn
     *            The MSISDN of the subscriber to look-up.
     * @return The subscriber if one exists for the MSISDN; null otherwise.
     * @throws HomeException
     *             Thrown if there is a problem with the SubscriberHome.
     */
    public static Subscriber lookupSubscriberForMSISDN(final Context context, final String msisdn) throws HomeException
    {
        return lookupSubscriberForMSISDN(context, msisdn, null);
    }


    /**
     * Looks-up and returns the Subscriber for the given MSISDN.
     *
     * @param context
     *            The operating context.
     * @param msisdn
     *            The MSISDN of the subscriber to look-up.
     * @param activateDate
     *            The date the Msisdn is active.
     * @return The subscriber if one exists for the MSISDN; null otherwise.
     * @throws HomeException
     *             Thrown if there is a problem with the SubscriberHome.
     */
    public static Subscriber lookupSubscriberForMSISDN(final Context context, final String msisdn,
        final Date activateDate) throws HomeException
    {
        final SubscriptionType subscriptionType = SubscriptionType.getINSubscriptionType(context);

        if (subscriptionType == null)
        {
            new DebugLogMsg(SubscriberSupport.class, "Unable to find IN Subscription, please make sure that a IN subscription is defined.", null).log(context);
            throw new HomeException("Request to lookupSubscriberIdForMSISDN received without a subscriptionType specified, but we are unable to find a IN Subscripion Type defined in the system.");
        }

        return lookupSubscriberForMSISDN(context, msisdn, subscriptionType.getId(), activateDate);
    }


    /**
     * Looks-up and returns the Subscriber for the given MSISDN.
     *
     * @param context
     *            The operating context.
     * @param msisdn
     *            The MSISDN of the subscriber to look-up.
     * @param activateDate
     *            The date the Msisdn is active.
     * @return The subscriber if one exists for the MSISDN; null otherwise.
     * @throws HomeException
     *             Thrown if there is a problem with the SubscriberHome.
     */
    public static Subscriber lookupSubscriberForMSISDN(final Context context, final String msisdn,
        final long subscriptionTypeId, final Date activateDate) throws HomeException
    {
        final Home subscriberHome = (Home) context.get(SubscriberHome.class);
        if (subscriberHome == null)
        {
            throw new HomeException("Could not look-up subscriber.  No SubscriberHome in context.");
        }

        final String subId = lookupSubscriberIdForMSISDN(context, msisdn, subscriptionTypeId, activateDate);
        if (subId != null)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(SubscriberSupport.class.getName(), "looking up subscriber by subscriber id " + subId
                    + " with msisdn " + msisdn, null).log(context);
            }
            return getSubscriber(context, subId);
        }

        return null;
    }    
    
    
    /**
     * Looks up a limited subscriber profile with the provided MSISDN on the provided
     * date. If found, the subscriber returned will only contain subscriber ID, BAN,
     * MSISDN, and subscriber type.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            MSISDN to look up.
     * @param activateDate
     *            The date the MSISDN is active.
     * @return A subscriber owning the provided MSISDN on the provided date, containing
     *         only subscriber ID, BAN, MSISDN, and subscriber type. Null when not found
     * @throws HomeException
     *             Thrown if there are problems looking up the subscriber.
     */
    public static Subscriber lookupSubscriberForMSISDNLimited(final Context ctx, final String msisdn,
        final Date activateDate) throws HomeException
    {
        final String subId = lookupSubscriberIdForMSISDN(ctx, msisdn, activateDate);
        if (subId == null)
        {
            return null;
        }
        return lookupSubscriberLimited(ctx, subId);
    }


    /**
     * Looks up a limited subscriber profile with the provided subscriber ID. If found,
     * the subscriber returned will only contain subscriber ID, BAN, MSISDN, and
     * subscriber type.
     *
     * @param ctx
     *            The operating context.
     * @param subscriberId
     *            ID of the subscriber to look up.
     * @return A subscriber owning the provided MSISDN on the provided date, containing
     *         only subscriber ID, BAN, MSISDN, and subscriber type.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscriber.
     */
    public static Subscriber lookupSubscriberLimited(final Context ctx, final String subscriberId) throws HomeException
    {
        try
        {
            final XDB xdb = (XDB) ctx.get(XDB.class);
            final SubscriberHomeAdaptedVisitor visitor = new SubscriberHomeAdaptedVisitor();
            xdb.forEach(ctx, visitor, new SimpleXStatement(
                "select ID,BAN,MSISDN,SUBSCRIBERTYPE, STATE from SUBSCRIBER where ID = ?")
            {

                @Override
                public void set(final Context ctx, final XPreparedStatement ps) throws SQLException
                {
                    ps.setString(subscriberId);
                }
            });

            if (visitor.getResult() != null)
            {
                final Subscriber sub = visitor.getResult();
                sub.setId(subscriberId);
                return sub;
            }
        }
        catch (final Throwable th)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, SubscriberSupport.class,
                    "Null pointer exception caught when looking up limited subscriber profile, "
                        + "attempt to look up the full profile instead", th);
            }
        }

        final Home subscriberHome = (Home) ctx.get(SubscriberHome.class);
        if (subscriberHome == null)
        {
            throw new HomeException("Could not look-up subscriber.  No SubscriberHome in context.");
        }

        final Subscriber subscriber = (Subscriber) subscriberHome.find(ctx, subscriberId);

        return subscriber;
    }


    /**
     * Returns a comma-delimited string of all columns of subscriber table which are not
     * XML blobs.
     *
     * @return A comma-delimited string of all columns of subscriber table which are not
     *         XML blobs.
     */
    private static String getSubscriberNonXMLBlobColumns()
    {
        // lazy initialization
        if (subscriberNonXMLBlobColumns == null)
        {
            /*
             * Cindy 2007-10-01: Replaced the hard-coded string by examining
             * SubscriberXInfo.
             */
            final StringBuilder columns = new StringBuilder();
            for (final Object element : SubscriberXInfo.PROPERTIES)
            {
                final PropertyInfo property = (PropertyInfo) element;
                if (property.isPersistent() && !Collection.class.isAssignableFrom(property.getType()))
                {
                    if (columns.length() != 0)
                    {
                        columns.append(", ");
                    }
                    columns.append(property.getName());
                }
            }
            subscriberNonXMLBlobColumns = columns.toString();
        }
        return subscriberNonXMLBlobColumns;
    }


    /**
     * Safe look up for subscriber that excludes the XMLBlob attributes.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            The MSISDN to look up.
     * @param activateDate
     *            The date the MSISDN is active.
     * @return A subscriber containing all but the XML blobs.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscriber.
     */
    public static Subscriber lookupSubscriberForMSISDNLimitedColumns(final Context ctx, final String msisdn,
        final Date activateDate) throws HomeException
    {
        final String subId = lookupSubscriberIdForMSISDN(ctx, msisdn, activateDate);
        if (subId == null)
        {
            return null;
        }

        return lookupSubscriberLimitedColumns(ctx, subId);
    }


    /**
     * Safe look up for subscriber that excludes the XMLBlob attributes.
     *
     * @param ctx
     *            The operating context.
     * @param subscriberId
     *            The ID of the subscriber to look up.
     * @return A subscriber containing all but the XML blobs.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscriber.
     */
    public static Subscriber lookupSubscriberLimitedColumns(final Context ctx, final String subscriberId)
        throws HomeException
    {
        final XDB xdb = (XDB) ctx.get(XDB.class);
        final SubscriberHomeAdaptedVisitor visitor = new SubscriberHomeAdaptedVisitor();

        final StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append(getSubscriberNonXMLBlobColumns());
        sb.append(" from SUBSCRIBER where ID = ?");

        xdb.forEach(ctx, visitor, new SimpleXStatement(sb.toString())
        {

            @Override
            public void set(final Context ctx, final XPreparedStatement ps) throws SQLException
            {
                ps.setString(subscriberId);
            }
        });

        if (visitor.getResult() != null)
        {
            final Subscriber sub = visitor.getResult();
            sub.setId(subscriberId);
            return sub;
        }

        final Home subscriberHome = (Home) ctx.get(SubscriberHome.class);
        if (subscriberHome == null)
        {
            throw new HomeException("Could not look-up subscriber.  No SubscriberHome in context.");
        }

        final Subscriber subscriber = (Subscriber) subscriberHome.find(ctx, subscriberId);

        return subscriber;
    }


    /**
     * Looks-up and returns the Subscriber for the given MSISDN.
     *
     * @param context
     *            The operating context.
     * @param msisdn
     *            The MSISDN of the subscriber to look-up.
     * @return The subscriber if one exists for the MSISDN; null otherwise.
     * @throws HomeException
     *             Thrown if there is a problem with the SubscriberHome.
     */
    public static Subscriber lookupActiveSubscriberForMSISDN(final Context context, final String msisdn)
        throws HomeException
    {
        final Msisdn msisdnObject = lookupMsisdnObjectForMSISDN(context, msisdn);
        if (msisdnObject == null || !msisdnObject.getState().equals(MsisdnStateEnum.IN_USE))
        {
            throw new HomeException("Could not find Subscriber for \"" + msisdn + "\".");
        }

        final Home subscriberHome = (Home) context.get(SubscriberHome.class);

        if (subscriberHome == null)
        {
            throw new HomeException("Could not look-up subscriber.  No SubscriberHome in context.");
        }

        final String subId = msisdnObject.getSubscriberID(context);
        return getSubscriber(context, subId);
    }


    /**
     * Looks up a subscriber by the subscriber ID, first in the context. Fails back to the home.
     * If you need a guaranted read from the home, use lookupSubscriberForSubId()
     *
     * @param context
     *            The operating context.
     * @param subscriberId
     *            Subscriber ID.
     * @return The subscriber with the provided ID, or <code>null</code> if none exists.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscriber.
     */
    public static Subscriber getSubscriber(final Context context, final String subscriberId) throws HomeException
    {
        final Subscriber fromContext = (Subscriber) context.get(Subscriber.class);
        if (fromContext != null)
        {
            if (SafetyUtil.safeEquals(subscriberId, fromContext.getId()))
            {
                return fromContext;
            }
            else if (LogSupport.isDebugEnabled(context))
            {
                final String msg = "CONTEXT: different subscriber in context! subID: [" + fromContext.getId()
                        + "] we were looking for [" +  subscriberId + "]";
                LogSupport.debug(context, SubscriberSupport.class, msg, new Exception(msg));
            }
        }

        return lookupSubscriberForSubId(context, subscriberId);
    }


    /**
     * Looks-up and returns the Subscriber for the given IMSI.
     *
     * @param context
     *            The operating context.
     * @param imsi
     *            The IMSI of the subscriber to look-up.
     * @return The subscriber if one exists for the MSISDN; null otherwise.
     * @throws HomeException
     *             Thrown if there is a problem with the SubscriberHome.
     */
    public static Subscriber lookupSubscriberForIMSI(final Context context, final String imsi) throws HomeException
    {
        final Home subscriberProfileHome = (Home) context.get(SubscriberHome.class);
        if (subscriberProfileHome == null)
        {
            final String m = "System Error: SubscriberHome not found in context.";
            throw new HomeException(m);
        }

        final Subscriber sub = (Subscriber) subscriberProfileHome.find(context, new EQ(SubscriberXInfo.IMSI, imsi));
        return sub;
    }


    /**
     * Retrieves the current subscriber from context.
     *
     * @param context
     *            The operating context.
     * @param association
     *            The subscriber-auxiliary service association to look up for.
     * @return The subscriber in context.
     * @throws HomeException
     *             Thrown if the subscriber is not found.
     */
    public static Subscriber getSubscriberForAuxiliaryService(final Context context,
        final SubscriberAuxiliaryService association) throws HomeException
    {
        final Subscriber subscriber = SubscriberSupport.getSubscriber(context, association.getSubscriberIdentifier());
        if (subscriber == null)
        {
            throw new HomeException("Cannot find subscriber [" + association.getSubscriberIdentifier()
                    + "] of SubscriberAuxiliaryService [" + association.getIdentifier() + "] on service ["
                   + association.getAuxiliaryServiceIdentifier() +"]");
        }

        return subscriber;
    }


    /**
     * Convenience method for looking-up the number of free SMS sent in SMSB.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber.
     * @return SMS usages of the subscriber.
     * @throws IllegalStateException
     *             Thrown if AppSmsbClient is not found in the context.
     * @deprecated no longer used
     */
    @Deprecated
    public static SMSUsageView getSMSUsage(final Context context, final Subscriber subscriber)
    {
        final AppSmsbClient smsbClient = (AppSmsbClient) context.get(AppSmsbClient.class);

        boolean getUsage = true;
        if (subscriber == null || smsbClient == null)
        {
            getUsage = false;
        }

        /*
         * [Cindy] 2008-02-29 TT8020600010: Don't display usage if subscriber is inactive.
         */
        else if (SafetyUtil.safeEquals(subscriber.getState(), SubscriberStateEnum.INACTIVE))
        {
            getUsage = false;
        }
        SMSUsageView usage = null;

        if (getUsage)
        {

            final String msisdn = subscriber.getMSISDN();
            final int smsSent = smsbClient.getSmsSent(msisdn);

            int totalFreeSMSAllowed = 0;

            final int freeSMSSent = Math.min(smsSent, totalFreeSMSAllowed);
            final int freeSMSAvailable = totalFreeSMSAllowed - freeSMSSent;

            usage = new SMSUsageView();
            usage.setFreeSMSSent(freeSMSSent);
            usage.setFreeSMSAvailable(freeSMSAvailable);
        }
        return usage;
    }


    /**
     * Determines whether or not the given subscriber owns its voice MSISDN. It is
     * possible for deactivated subscribers to lose ownership of their voice MSISDN. In
     * such cases, we should not attempt to update external systems keyed off MSISDN for
     * those subscribers.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber to check for MSISDN ownership.
     * @return True if the subscriber owns its voice MSISDN; false otherwise.
     * @throws HomeException
     *             Thrown if there are problems accessing data in the given context.
     */
    public static boolean ownsMSISDN(final Context context, final Subscriber subscriber) throws HomeException
    {
        final Msisdn msisdn = lookupMsisdnObjectForMSISDN(context, subscriber.getMSISDN());

        final boolean ownsMSISDN = msisdn != null
            && SafetyUtil.safeEquals(subscriber.getId(), msisdn.getSubscriberID(context));

        return ownsMSISDN;
    }


    /**
     * Gets the BillCycle day for a given subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The given subscriber.
     * @return The BillCycle day to be returned.
     * @throws HomeException
     *             Thrown if failed to look for a BillCycle in the BillCycleHome.
     */
    public static int getBillCycleDay(final Context context, final Subscriber subscriber) throws HomeException
    {
        final Account account = lookupAccount(context, subscriber);
        if (account == null)
        {
            throw new HomeException("Error: No account for subscriber " + subscriber.getId());
        }

        final Home billCycleHome = (Home) context.get(BillCycleHome.class);
        if (billCycleHome == null)
        {
            throw new HomeException("System Error: BillCycleHome does not exist in context");
        }

        final BillCycle billCycle = account.getBillCycle(context);
        if (billCycle == null)
        {
            throw new HomeException("Error: No billCycle " + account.getBillCycleID() + " for account " + subscriber.getBAN());
        }

        final int billCycleDay;
        
        if (billCycle.getDayOfMonth() != BillCycleSupport.SPECIAL_BILL_CYCLE_DAY)
        {
            billCycleDay = billCycle.getDayOfMonth();
        }
        else
        {
            /*
             * lookupAccount returns account in the context.  In case of Activation
             * of subs with auto bill cycle the value for billcycle is changed but not stored
             * in context since we require it for verification at various stages, hence trying
             * to get updated account if the bill cycle day of month the same as
             * SPECIAL_BILL_CYCLE_DAY.getSubs
             */
            final Account dbAccount = AccountSupport.getAccount(context, subscriber.getBAN());
            if (dbAccount.getBillCycleID() != billCycle.getIdentifier())
            {
                BillCycle dbBillCycle = dbAccount.getBillCycle(context);
                billCycleDay = dbBillCycle.getDayOfMonth(); 
            }
            else
            {
                billCycleDay = BillCycleSupport.SPECIAL_BILL_CYCLE_DAY;
            }
        }
        
        return billCycleDay;
    }

    /**
     * Set the subscriber state on ABM as Active with the latest billCycle.
     *
     * @param context
     *            The operating context.
     * @param account
     *            The Account of the subscriber being updated.
     * @param subscriber
     *            Subscriber being updated.
     * @throws HomeException
     *             Thrown if there are problems updating the subscriber state.
     */
    public static void updateSubStateOnCrmAbmBM(final Context context, final Account account, final Subscriber subscriber)
        throws HomeException
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(SubscriberSupport.class, "Subscriber State Transition from Available ---> Active .", null)
                .log(context);
        }

        final Calendar rightNow = Calendar.getInstance();
        final int billingDay = SubscriberSupport.setSubscribersBillingDayToActivationDay(context, subscriber,
                rightNow.get(Calendar.DATE));

        try
        {
            final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(context);
            client.updateBillingDay(context, account, billingDay);
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            throw new HomeException("Unable to update the subscriber BillCycleDay on BM. result = "
                    + exception.getErrorCode(), exception);
        }

        final AppOcgClient client = (AppOcgClient) context.get(AppOcgClient.class);
        if (client == null)
        {
            throw new HomeException("Cannot find AppOcgClient in context.");
        }

        /* Activating subscriber on ABM */
        final LongHolder outputBalance = new LongHolder();
        final LongHolder outputOverdraftBalance = new LongHolder();
        final LongHolder outputOverdraftDate = new LongHolder();
        final ParameterSetHolder outputParams = new ParameterSetHolder();

        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(SubscriberSupport.class, "requestBalance2 called to activate the sub on ABM and BM", null)
                .log(context);
        }
        
        SubscriptionType subscriptionType = subscriber.getSubscriptionType(context);
        if (subscriptionType == null)
        {
            throw new HomeException("Subscription Type " + subscriber.getSubscriptionType() + " not found for subscription " + subscriber.getId());
        }
        final int rc = client.requestBalance2(subscriber.getMSISDN(), subscriber.getSubscriberType(),
                subscriber.getCurrency(context),
        // sendExpiry
            true,
            // activationFlag = true to activate the subscriber
            true,
            // erReference
            subscriber.getMSISDN(), subscriptionType.getId(), outputBalance,
                outputOverdraftBalance, outputOverdraftDate, outputParams);
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(SubscriberSupport.class,
                "requestBalance2 called to activate the sub on ABM and BM got result code :: " + rc, null).log(context);
        }

        if (rc != ErrorCode.NO_ERROR)
        {
            HomeException e;
            e = new HomeException("Failed to query balance remaining for the prepaid subscriber. result = " + rc);
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, SubscriberSupport.class,
                    "requestBalance2 called to activate the sub on ABM and BM got result code :: " + rc, e);
            }
            throw e;
        }
        subscriber.setBalanceRemaining(outputBalance.value);
        subscriber.setOverdraftBalance(outputOverdraftBalance.value);
        subscriber.setOverdraftDate(outputOverdraftDate.value);
    }


    /**
     * Set the subscriber state on ABM as Active with the latest billCycle.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being updated.
     * @throws HomeException
     *             Thrown if there are problems updating the subscriber.
     */
    public static void updateExpiryOnCrmAbmBM(final Context context, final Subscriber subscriber) throws HomeException
    {
        final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(context);
        
        Parameters profile;
        
        try
        {
            profile = client.querySubscriptionProfile(context, subscriber);
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            new MinorLogMsg("SubscriberSupport", "Failed to query BM for subscription " + subscriber.getId(),
                    exception).log(context);
            profile = null;
        }

        if (profile == null)
        {
            new MinorLogMsg(SubscriberSupport.class, "Cannot find subscriber " + subscriber.getMSISDN(),
                    null).log(context);
            return;
        }
        final long crmExpiryDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(subscriber.getExpiryDate()).getTime();
        final long abmExpiryDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(profile.getExpiryDate()).getTime();
        if (abmExpiryDate < crmExpiryDate)
        {
            try
            {
                client.updateExpiryDate(context, subscriber, subscriber.getExpiryDate());
            }
            catch (final SubscriberProfileProvisionException exception)
            {
                new MinorLogMsg(SubscriberSupport.class, "Failed to update subscriber " + subscriber.getMSISDN()
                    + " expiry date to " + new SimpleDateFormat("yyyy-MM-dd").format(subscriber.getExpiryDate())
                    + " on ABM with result [" + exception.getErrorCode() + "]", null).log(context);
            }
        }
        final Date now = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(new Date());
        if (subscriber.getState().equals(SubscriberStateEnum.EXPIRED) && subscriber.getExpiryDate().after(now))
        {
            final Home subscriberHome = (Home) context.get(SubscriberHome.class);
            subscriber.setState(SubscriberStateEnum.ACTIVE);
            subscriberHome.store(context, subscriber);
        }
    }

    /**
     * Set the subscriber state on ABM as Active with the latest billCycle.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being updated.
     * @throws HomeException
     *             Thrown if there are problems updating the subscriber.
     */
    public static void updateExpiryDateSubscriptionProfile(final Context context, final Subscriber subscriber) throws HomeException
    {
        final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(context);
        
        Parameters profile;
        
        try
        {
            profile = client.querySubscriptionProfile(context, subscriber);
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            new MinorLogMsg("SubscriberSupport", "Failed to query BM for subscription " + subscriber.getId(),
                    exception).log(context);
            profile = null;
        }

        if (profile == null)
        {
            new MinorLogMsg(SubscriberSupport.class, "Cannot find subscriber " + subscriber.getMSISDN(),
                    null).log(context);
            return;
        }
        try
        {
            client.updateExpiryDate(context, subscriber, subscriber.getExpiryDate());
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            new MinorLogMsg(SubscriberSupport.class, "Failed to update subscriber " + subscriber.getMSISDN()
                + " expiry date to " + new SimpleDateFormat("yyyy-MM-dd").format(subscriber.getExpiryDate())
                + " on ABM with result [" + exception.getErrorCode() + "]", null).log(context);
        }
    }
    
    
    /**
     * Gets the subscriber credit limit and balance from ABM. Note the ABM balance is
     * multiplied by -1 to match CRM so that negative balances mean a credit to
     * subscribers.
     *
     * @param subscriber
     *            The subscriber to fetch the ABM info
     */
    public static void updateSubscriberSummaryABM(final Context ctx, Subscriber subscriber)
    {
        updateSubscriberSummaryABMReturnParameterList(ctx,subscriber);        
    }

    /**
     * Gets the subscriber credit limit and balance from ABM. Note the ABM balance is
     * multiplied by -1 to match CRM so that negative balances mean a credit to
     * subscribers.
     *
     * @param subscriber
     *            The subscriber to fetch the ABM info
     */
    public static Parameters updateSubscriberSummaryABMReturnParameterList(final Context ctx, Subscriber subscriber)
    {
        if (subscriber.getState() == SubscriberStateEnum.PENDING)
        {
            subscriber.setRealTimeBalance(0);
            subscriber.setAbmCreditLimit(0);
            subscriber.setMonthlySpendAmount(0);
            return null;
        }

        if (ctx == null)
        {
            return null;
        }

        // Get the subscriber BM Credit limit and balance
        final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
        try
        {
            final Parameters profile = client.querySubscriptionProfile(ctx, subscriber);
            if (profile != null)
            {
                subscriber.setRealTimeBalance(profile.getBalance() * -1);
                subscriber.setAbmCreditLimit(profile.getCreditLimit()); 
                subscriber.setURCSOverdraftBalanceLimit(profile.getOverdraftBalanceLimit());
                try
                {
                    subscriber.setMonthlySpendAmount(profile.getMonthlySpendUsage());
                }
                catch (IllegalArgumentException iEx)
                {
                    subscriber.setMonthlySpendAmount(0);
                }

            }
            else if (subscriber.getState().equals(SubscriberStateEnum.INACTIVE))
            {
                subscriber.setRealTimeBalance(0);
                subscriber.setAbmCreditLimit(0);
                subscriber.setMonthlySpendAmount(0);
            }
            return profile;
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(SubscriberSupport.class.getName(), "Failed to get balance and credit limit from BM.",
                    exception).log(ctx);
        }
        catch (SubscriberProfileProvisionException exception)
        {
            new MinorLogMsg(SubscriberSupport.class.getName(), "Failed to get balance and credit limit from BM.",
                    exception).log(ctx);
        }
        return null;
        
    }

    public static void updateSubscriptionBlockedBalance(final Context ctx, Subscriber sub)
    {
        sub.setBlockedBalance(0L);

        And where = new And();
        where.add(new EQ(TransferDisputeXInfo.RECP_SUB_ID, sub.getId()));
        where.add(new NEQ(TransferDisputeXInfo.STATE, TransferDisputeStatusEnum.ACCEPTED));
        where.add(new NEQ(TransferDisputeXInfo.STATE, TransferDisputeStatusEnum.REJECTED));

        try
        {
            Home disputeHome = (Home)ctx.get(TransferDisputeHome.class);
            HashSet<Long> unresolvedTransactions = new HashSet<Long>();

            Iterator i = disputeHome.select(ctx, where).iterator();
            while(i.hasNext())
            {
                TransferDispute dispute = (TransferDispute)i.next();
                List l = dispute.getAssociatedTransactions();
                if(null != l)
                {
                    Iterator associatedTransactionIt = l.iterator();
                    while(associatedTransactionIt.hasNext())
                    {
                        com.redknee.framework.xhome.holder.LongHolder associatedTransactionReceiptNum = (com.redknee.framework.xhome.holder.LongHolder)associatedTransactionIt.next();
                        unresolvedTransactions.add(associatedTransactionReceiptNum.getValue());
                    }
                }
            }

            long totalBlockedBalance = 0;
            
            if(unresolvedTransactions.size() > 0)
            {
                Home transactionHome = (Home)ctx.get(TransactionHome.class);
                where = new And();
                where.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, sub.getId()));
                where.add(new In(TransactionXInfo.RECEIPT_NUM, unresolvedTransactions));
                where.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.DisputeRecipientBlockDebit)));
    
                
                i = transactionHome.select(ctx, where).iterator();
                while(i.hasNext())
                {
                    Transaction t = (Transaction)i.next();
                    totalBlockedBalance += t.getAmount();
                }
            }
            
            XDB xdb = (XDB) ctx.get(XDB.class);
            
            DisputeAmountVisitor visitor = new DisputeAmountVisitor();
            
            xdb.forEach(ctx, visitor, DisputeAmountVisitor.getQuery(sub.getId()));
            
            totalBlockedBalance += visitor.getTotalUnresolvedAmount();
            
            sub.setBlockedBalance(totalBlockedBalance);
        }
        catch(Exception e)
        {
            LogSupport.minor(ctx, SubscriberSupport.class.getName(), "Error trying to update blocked balance.", e);
        }
    }
    
    
    /**
    * @param ctx
    * @param sub
    * @return AdjustmentTypeEnum
    */
    public static AdjustmentTypeEnum getWriteoffSubscriptionBalanceAdjustment(Context ctx, Subscriber prepaidSub) throws HomeException
    {
        SubscriptionType subscriptionType = prepaidSub.getSubscriptionType(ctx);
        if (subscriptionType == null)
        {
            throw new HomeException("Subscription Type " + prepaidSub.getSubscriptionType() + " not found for subscription " + prepaidSub.getId());
        }
        
        if (subscriptionType.isOfType(SubscriptionTypeEnum.AIRTIME))
        {
            return AdjustmentTypeEnum.BalanceWriteOffAirtime;
        }
        else if (subscriptionType.isWallet())
        {
            return AdjustmentTypeEnum.WriteOffMoneyBalance;
        }
        else
        {
            return AdjustmentTypeEnum.DebitBalance;
        }
    }
    
    /**
     * Subscriber's remaining balance could be written off if subscriber
     * is in a true prepaid mobile money wallet subscription. 
     * Once the balance is removed, state may be set to deactivated.
     * @param ctx
     *            The operating context.
     * @param prepaidSub
     *            Prepaid subscriber.
     * @return balance that is written off
     * @throws HomeException
     *             Thrown if there are making ad adjustment through transaction.
     */
    public static long writeoffSubscriptionBalance(Context ctx, Subscriber prepaidSub) throws HomeException
    {
        long balance = 0;
        if (prepaidSub.isPrepaid() && SystemSupport.supportsAllowWriteOffForPrepaidSubscription(ctx))
        {
            final AdjustmentTypeEnum adjustmentTypeEnum = getWriteoffSubscriptionBalanceAdjustment(ctx, prepaidSub);
            final AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, adjustmentTypeEnum);
            if (adjustmentType == null)
            {
                throw new HomeException("Required adjustment type not found: " + adjustmentTypeEnum.getDescription()
                        + " [ " + adjustmentTypeEnum.getIndex() + " ]");
            }
            try
            {
                final SubscriberProfileProvisionClient subProvClient = BalanceManagementSupport
                        .getSubscriberProfileProvisionClient(ctx);
                final Parameters subscription = subProvClient.querySubscriptionProfile(ctx, prepaidSub);
                if (subscription == null)
                {
                    throw new HomeException("For Subscriber: " + prepaidSub.getId()
                            + " - Could not find profile on BM for Subscriber: " + prepaidSub.getId());
                }
                balance = subscription.getBalance();
                if (balance > 0)
                {
                    try
                    {
                        // activate the state to active for write-off/debit
                        // better way would be to have an api method at BM for write-off
                        // (atomic)
                        subProvClient.updateState(ctx, prepaidSub, SubscriptionState.ACTIVE);
                        TransactionSupport.createTransaction(ctx, prepaidSub, balance, adjustmentType);
                    }
                    catch (SubscriberProfileProvisionException e)
                    {
                        final String message = "For Subscriber: " + prepaidSub.getId()
                                + " - Could not update state at BM. BM Error Code [" + e.getErrorCode() + "]";
                        new MinorLogMsg(SubscriberSupport.class.getName(), message, e).log(ctx);
                        throw new HomeException(message, e);
                    }
                    finally
                    {
                        try
                        {
                            // no matter what; restore state to original
                            subProvClient.updateState(ctx, prepaidSub, subscription.getState());
                        }
                        catch (SubscriberProfileProvisionException e)
                        {
                            new MinorLogMsg(SubscriberSupport.class.getName(),
                                    "Could not restore Subscriber-Subscription " + prepaidSub.getId()
                                            + " state at BM. BM Error Code [" + e.getErrorCode() + "]", e).log(ctx);
                            // eat the exception. Subscriber's balance has been anulled.
                        }
                    }
                }
                else
                {
                    new InfoLogMsg(SubscriberSupport.class.getName(), "No Balance, No need to write-off", null)
                            .log(ctx);
                }
            }
            catch (SubscriberProfileProvisionException e)
            {
                final String message = "For Subscriber: " + prepaidSub.getId()
                        + " - Could not fetch subscription at BM [" + e.getErrorCode() + "]";
                new MinorLogMsg(SubscriberSupport.class.getName(), message, e).log(ctx);
                throw new HomeException(message, e);
            }
        }
        return balance;
    }


    /**
     * Returns the PPSM subscriber of the provided prepaid subscriber, if the prepaid
     * subscriber is in a valid PPSM state.
     *
     * @param ctx
     *            The operating context.
     * @param prepaidSub
     *            Prepaid subscriber.
     * @return postpaid msisdn, if validation passed.
     * @throws HomeException
     *             Thrown if there are problems retrieving the PPSM subscriber.
     * @see #isValidPPSMState(SubscriberStateEnum)
     */
    public static Subscriber validateSubscriberPPSM(final Context ctx, final Subscriber subscriber)
        throws HomeException
    {
        Subscriber postpaidSub = null;
        PPSMSupporteeSubExtension extension = PPSMSupporteeSubExtension.getPPSMSupporteeSubscriberExtension(ctx, subscriber.getId());
        if (extension != null)
        {
            postpaidSub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, extension.getSupportMSISDN());
        }

        return postpaidSub;
    }


    /**
     * Gets the Service Provider for a given subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The given subscriber.
     * @return The Service Provider to be returned.
     * @throws HomeException
     *             Thrown if failed to look for a CRMSpid in the CRMSpidHome.
     */
    public static CRMSpid getServiceProvider(final Context context, final Subscriber subscriber) throws HomeException
    {
        final Home home = (Home) context.get(CRMSpidHome.class);
        if (home == null)
        {
            throw new IllegalStateException("System Error: CRMSpidHome not found in context.");
        }

        final CRMSpid spid = (CRMSpid) home.find(context, Integer.valueOf(subscriber.getSpid()));
        if (spid == null)
        {
            throw new HomeException("Error: No SPID " + subscriber.getSpid() + " found with subscriber "
                + subscriber.getId());
        }

        return spid;
    }


    /**
     * Apply subscriber service activation template.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber being updated.
     * @param satId
     *            ID of the service activation template to be applied.
     * @throws HomeException
     *             Thrown if there are problems looking up or applying the service
     *             activation template.
     * @return ServiceActivationTemplate that is applied
     */
    public static ServiceActivationTemplate applySubServiceActivationTemplate(Context ctx, final Subscriber sub,
            final long satId)
        throws HomeException
    {
        final Home home = (Home) ctx.get(ServiceActivationTemplateHome.class);
        final ServiceActivationTemplate sat = (ServiceActivationTemplate) home.find(ctx, Long.valueOf(satId));

        if (sat == null)
        {
            new DebugLogMsg(SubscriberSupport.class, "satId= " + satId + " not found.", null).log(ctx);
        }
        else
        {
            applySubServiceActivationTemplate(ctx, sub, sat);
        }

        return sat;
    }


    /**
     * Apply subscriber service activation template.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber being updated.
     * @param sat
     *            Service activation template to be applied.
     */
    public static void applySubServiceActivationTemplate(Context ctx, final Subscriber sub,
        final ServiceActivationTemplate sat) throws HomeException
    {
        if (sat == null)
        {
            return;
        }

        if (sat.getSpid() != sub.getSpid())
        {
            new MinorLogMsg(SubscriberSupport.class, "SCT [" + sat.getIdentifier() + " - " + sat.getName()
                    + "] SPID does not match the SPID of the Subscription.", null).log(ctx);
        }
        else
        {            
            sub.setInitialBalance(sat.getInitialBalance());            
            sub.setMaxBalance(sat.getMaxBalance());                        
            sub.setMaxRecharge(sat.getMaxRecharge());               
            sub.setReactivationFee(sat.getReactivationFee());            
            sub.switchPricePlan(ctx, sat.getPricePlan());            
            sub.setSecondaryPricePlan(sat.getSecondaryPricePlan());            
            sub.setSecondaryPricePlanStartDate(SubscriberSupport.calculateSecondaryPPStartDate(ctx, sub));
            sub.setSecondaryPricePlanEndDate(SubscriberSupport.calculateSecondaryPPEndDate(ctx, sub));
            sub.setQuotaType(sat.getQuotaType());            
            sub.setQuotaLimit(sat.getQuotaLimit()); 
            sub.setSubscriptionClass(sat.getSubscriptionClass());
            sub.setTechnology(sat.getTechnology());
            
            applySubscriptionClassToSubscriber(ctx, sub);

            final Collection auxFromSct = getAuxiliaryServicesfromSCT(ctx, sat.getIdentifier());
            sub.getAuxiliaryServices(ctx).addAll(auxFromSct);

            final Map auxBuns = getAuxiliaryBundleFeesFromSCT(ctx, sat.getIdentifier());
            sub.getBundles().putAll(auxBuns);

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(SubscriberSupport.class, "Applied Values from SCT [" + sat.getIdentifier() + " - "
                        + sat.getName() + "] to the Subscription profile", null).log(ctx);
            }
        }
    }

    private static void applySubscriptionClassToSubscriber(final Context ctx, final Subscriber subscriber)
            throws HomeException
    {
        final SubscriptionClass subClass = subscriber.getSubscriptionClass(ctx);
        if (subClass == null)
        {
            throw new HomeException("Subscription Class field cannot be parsed.");
        }
        subscriber.setSubscriptionType(subClass.getSubscriptionType());
    }
    
    public static void applyServiceActivationTemplateMandatoryMode(Context ctx, final long satId)
    {
        final Home home = (Home) ctx.get(ServiceActivationTemplateHome.class);
        ServiceActivationTemplate sat;
        try
        {
            sat = (ServiceActivationTemplate) home.find(ctx, Long.valueOf(satId));
        }
        catch (Exception e)
        {
            new MajorLogMsg(SubscriberSupport.class.getName(),
                    "Failed to apply subscriber creation template mandatory field mode.", e).log(ctx);
            return;
        }     
        applyMandatoryModeIfNeeded(ctx, sat.getInitialBalancePreference(), SubscriberXInfo.INITIAL_BALANCE);
        applyMandatoryModeIfNeeded(ctx, sat.getMaxBalancePreference(), SubscriberXInfo.MAX_BALANCE);
        applyMandatoryModeIfNeeded(ctx, sat.getMaxRechargePreference(), SubscriberXInfo.MAX_RECHARGE);
        applyMandatoryModeIfNeeded(ctx, sat.getReactivationFeePreference(), SubscriberXInfo.REACTIVATION_FEE);
        applyMandatoryModeIfNeeded(ctx, sat.getPricePlanPreference(), SubscriberXInfo.PRICE_PLAN);
        applyMandatoryModeIfNeeded(ctx, sat.getSecondaryPricePlanPreference(), SubscriberXInfo.SECONDARY_PRICE_PLAN);
        applyMandatoryModeIfNeeded(ctx, sat.getQuotaTypePreference(), SubscriberXInfo.QUOTA_TYPE);
        applyMandatoryModeIfNeeded(ctx, sat.getQuotaLimitPreference(), SubscriberXInfo.QUOTA_LIMIT);
    }
    
    public static void applyMandatoryModeIfNeeded(final Context ctx, final CreationPreferenceEnum mandatory,
            final PropertyInfo property)
    {
        if (mandatory.equals(CreationPreferenceEnum.MANDATORY))
        {
            // use ReadOnlyAwareProxyWebControl() to correctly persist read only values
            AbstractWebControl.setMode(ctx, property, ViewModeEnum.READ_ONLY);
        }
    }

    /**
     * Calculates the secondary price plan start date based on the subscriber activation
     * date and the primary price plan duration and returns the date.
     *
     * @param ctx
     *            Context object
     * @param subscriber
     *            Subscriber object
     * @return Date secondary Price Plan start date to be set
     */
    public static Date calculateSecondaryPPStartDate(final Context ctx, final Subscriber subscriber)
    {
        Date startDt = null;

        final Date subCreateDt = subscriber.getDateCreated();
        final ServiceActivationTemplate sat = getServiceActivationTemplate(ctx, subscriber);

        if (sat != null)
        {
            final int primPlanPeriods = sat.getPrimaryPricePlanPeriods();
            final short primPeriodUnits = sat.getPrimaryPricePlanUnit().getIndex();

            if (primPlanPeriods > 0)
            {
                switch (primPeriodUnits)
                {
                    case PricePlanUnitEnum.DAYS_INDEX:
                        startDt = CalendarSupportHelper.get(ctx).findDateDaysAfter(primPlanPeriods, subCreateDt);
                        break;
                    case PricePlanUnitEnum.WEEKS_INDEX:
                        startDt = CalendarSupportHelper.get(ctx).findDateDaysAfter(primPlanPeriods * 7, subCreateDt);
                        break;
                    case PricePlanUnitEnum.MONTHS_INDEX:
                        startDt = CalendarSupportHelper.get(ctx).findDateMonthsAfter(primPlanPeriods, subCreateDt);
                        break;
                    case PricePlanUnitEnum.YEARS_INDEX:
                        startDt = CalendarSupportHelper.get(ctx).findDateYearsAfter(primPlanPeriods, subCreateDt);
                        break;
                    default:
                        startDt = CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, subCreateDt);
                        break;
                }
            }
        }

        if (startDt == null)
        {
            startDt = CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, subCreateDt);
        }

        return startDt;
    }


    /**
     * Calculates the secondary price plan end date based on the secondary price plan
     * start date and the secondary price plan duration and returns the date.
     *
     * @param ctx
     *            Context object
     * @param subscriber
     *            Subscriber object
     * @return Date Secondary Price Plan end date to be set
     */
    public static Date calculateSecondaryPPEndDate(final Context ctx, final Subscriber subscriber)
    {
        Date endDt = null;

        final Date secPlanStartDt = subscriber.getSecondaryPricePlanStartDate();
        final ServiceActivationTemplate sat = getServiceActivationTemplate(ctx, subscriber);

        if (sat != null)
        {
            final int secPlanPeriods = sat.getSecondaryPricePlanPeriods();
            final short secPlanPeriodUnits = sat.getSecondaryPricePlanUnit().getIndex();

            if (secPlanPeriods > 0)
            {
                switch (secPlanPeriodUnits)
                {
                    case PricePlanUnitEnum.DAYS_INDEX:
                        endDt = CalendarSupportHelper.get(ctx).findDateDaysAfter(secPlanPeriods, secPlanStartDt);
                        break;
                    case PricePlanUnitEnum.WEEKS_INDEX:
                        endDt = CalendarSupportHelper.get(ctx).findDateDaysAfter(secPlanPeriods * 7, secPlanStartDt);
                        break;
                    case PricePlanUnitEnum.MONTHS_INDEX:
                        endDt = CalendarSupportHelper.get(ctx).findDateMonthsAfter(secPlanPeriods, secPlanStartDt);
                        break;
                    case PricePlanUnitEnum.YEARS_INDEX:
                        endDt = CalendarSupportHelper.get(ctx).findDateYearsAfter(secPlanPeriods, secPlanStartDt);
                        break;
                    default:
                        endDt = CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, secPlanStartDt);
                        break;
                }
            }
        }
        if (endDt == null)
        {
            endDt = CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, secPlanStartDt);
        }

        return endDt;
    }


    /**
     * This method returns the ServiceActivationTemplate object for the Subscriber satId.
     *
     * @param ctx
     *            Context Object
     * @param sub
     *            Subscriber object
     * @return ServiceActivationTemplate object
     */
    private static ServiceActivationTemplate getServiceActivationTemplate(final Context ctx, final Subscriber sub)
    {
        final Home home = (Home) ctx.get(ServiceActivationTemplateHome.class);
        ServiceActivationTemplate sat = null;
        try
        {
            final EQ condition = new EQ(ServiceActivationTemplateXInfo.IDENTIFIER, Long.valueOf(sub.getSatId()));
            sat = (ServiceActivationTemplate) home.find(ctx, condition);
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(SubscriberSupport.class.getName(), "Error while finding the SCT for the id "
                + sub.getSatId(), e).log(ctx);
        }
        return sat;
    }


    /**
     * Sets the IMSI of the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber to be set.
     * @param packId
     *            SIM package ID.
     * @throws HomeException
     *             Thrown if there are problems looking up the SIM package.
     */
    public static void setIMSI(final Context ctx, final Subscriber sub, final String packId) throws HomeException
    {
        String imsi = PackageSupportHelper.get(ctx).retrieveIMSIorMIN(ctx, packId, sub.getTechnology(), sub.getSpid());
        sub.setIMSI(imsi);
    }



    /**
     * Utility function to get CSV file easily from BeanShell.
     *
     * @param ctx
     *            The operating context.
     * @param subId
     *            Subscriber ID.
     * @return CSV string representing the subscriber.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscriber.
     */
    public static String toCSVString(final Context ctx, final String subId) throws HomeException
    {
        final StringBuffer sb = new StringBuffer();
        final Subscriber sub = lookupSubscriberForSubId(ctx, subId);
        if (sub != null)
        {
            final SubscriberCSVSupport csv = new SubscriberCSVSupport();
            csv.append(sb, Constants.DEFAULT_SEPERATOR, sub);
        }
        return sb.toString();
    }


    /**
     * Determines whether the price plan version of two subscribers are identical.
     *
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     * @return Returns <code>true</code> if the two subscribers have the same price plan.
     * @throws HomeException
     *             Thrown if there are problems looking up the price plans.
     */
    public static boolean isSamePricePlanVersion(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        final PricePlanVersion oldPricePlan = oldSub.getRawPricePlanVersion(ctx);
        final PricePlanVersion newPricePlan = newSub.getRawPricePlanVersion(ctx);

        return oldPricePlan.getId() == newPricePlan.getId() && oldPricePlan.getVersion() == newPricePlan.getVersion();
    }

    public static boolean isCLCTChange(final Subscriber oldSub, final Subscriber newSub)
    {
        return newSub.isClctChange();
    }

    public static boolean isSamePricePlanVersion(Subscriber oldSub, Subscriber newSub) 
    {
        if ( oldSub == null || newSub == null )
        {
            return false; 
        }
        
        if ( oldSub.getPricePlan() == newSub.getPricePlan() &&
                oldSub.getPricePlanVersion() == newSub.getPricePlanVersion())
           {
               return true; 
           } else 
           {
               return false; 
           }
    }
    
    /**
     * Returns the default future end date.
     *
     * @return The default future end date, set 20 years after today.
     */
    public static Date getFutureEndDate(final Date date)
    {
        return CalendarSupportHelper.get().findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, date);
    }


    /**
     * Returns the bundle status.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber being examined.
     * @param bundleId
     *            Bundle ID.
     * @return Status of that bundle for the provided subscriber.
     */
    public static BundleStatusEnum getBundleStatus(final Context ctx, final Subscriber subscriber, final Long bundleId)
    {
        BundleStatusEnum result = BundleStatusEnum.OK;

        if (subscriber.getSuspendedBundles(ctx).containsKey(bundleId))
        {
            result = BundleStatusEnum.SUSPENDED;
        }

        return result;
    }


    /**
     * Returns the service of a specific type which the subscriber has subscribed to.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being examined.
     * @param serviceType
     *            The service type to look up.
     * @return The service of the provided type which the subscriber has subscribed to.
     */
    public static Service getService(final Context ctx, final Subscriber sub, final ServiceTypeEnum serviceType)
    {
        return getService(ctx, sub, serviceType, SubscriberSupport.class);
    }


    /**
     * Returns the service of a specific type which the subscriber has subscribed to.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being examined.
     * @param serviceType
     *            The service type to look up.
     * @param logSrc
     *            Caller of this function; used for logging purposes.
     * @return The service of the provided type which the subscriber has subscribed to.
     */
    public static Service getService(final Context ctx, final Subscriber sub, final ServiceTypeEnum serviceType,
        final Object logSrc)
    {
        final Object src;
        if (logSrc == null)
        {
            src = SubscriberSupport.class;
        }
        else
        {
            src = logSrc;
        }
        try
        {
            final Home serHome = (Home) ctx.get(ServiceHome.class);
            final PricePlanVersion pp = sub.getRawPricePlanVersion(ctx);
            final Object serviceId = CollectionSupportHelper.get(ctx).findFirst(ctx, pp.getServices(ctx), new Predicate()
            {

                private static final long serialVersionUID = 1L;


                /**
                 * {@inheritDoc}
                 */
                public boolean f(final Context pCtx, final Object obj) throws AbortVisitException
                {
                    try
                    {
                        final Service service = (Service) serHome.find(pCtx, obj);
                        return service != null && SafetyUtil.safeEquals(serviceType, service.getType());
                    }
                    catch (final Exception e)
                    {
                        new MinorLogMsg(src, "Failed to get " + serviceType.getDescription() + " service", e).log(pCtx);
                        return false;
                    }
                }
            });
            if (serviceId != null)
            {
                return (Service) serHome.find(ctx, serviceId);
            }
        }
        catch (final Exception e)
        {
            new MinorLogMsg(src, "Failed to get " + serviceType.getDescription() + " service", e).log(ctx);
        }
        return null;
    }
    
    /**
     * Utility method to calculate the Secondary Price Plan start Date based on the
     * subscriber secondary price plan start date from SCT.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber being examined.
     * @return Secondary price plan start date based on SCT.
     */
    public static Date calculateSecondaryPPStartDate2(final Context ctx, final Subscriber subscriber)
    {
        // setting the default secondary PP start date to 20 years from current date
        if (ctx == null || subscriber == null)
        {
            return CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, new Date());
        }
        Date secPPStartDt = subscriber.getSecondaryPricePlanStartDate();
        final Date subCreateDt = subscriber.getDateCreated();
        final Date subActivateDt = subscriber.getStartDate();

        final long diffDays = CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(subCreateDt, subActivateDt);

        if (secPPStartDt != null)
        {
            secPPStartDt = CalendarSupportHelper.get(ctx).findDateDaysAfter((int) diffDays, secPPStartDt);
        }
        else if (subActivateDt != null)
        {
            // Setting the secondary PP Start date to 20 years from Activation date.
            secPPStartDt = CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, subActivateDt);
        }

        return secPPStartDt;
    }


    /**
     * Determines if auto activation is required for the subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being examined.
     * @return true if subscriber is prepaid, individual and its bill cycle = -1 ( which
     *         is a special bill cycle set on CRM for AutoActivation )
     * @throws HomeException
     *             Thrown if there are problems determining whether auto activation is
     *             required.
     */
    public static boolean isAutoActivationRequired(final Context context, final Subscriber subscriber)
        throws HomeException
    {
        if (subscriber.isPrepaid())
        {
            final Account account = AccountSupport.getAccount(context, subscriber.getBAN());
            if (account == null)
            {
                throw new HomeException("Parent account " + subscriber.getBAN() + " does not exist");
            }
            if (account.isIndividual(context)
                    && account.getBillCycle(context).getDayOfMonth() == BillCycleSupport.SPECIAL_BILL_CYCLE_DAY)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    new DebugLogMsg(SubscriberSupport.class, "AutoActivation for the subscriber " + subscriber.getMSISDN()
                            + " is required", null).log(context);
                }
                return true;
            }
        }
        return false;
    }


    /**
     * Sets the subscriber's billing day to the activation day.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being updated.
     * @param activationDay
     *            is the day on which subscriber activated Set the subscriber's billingDay
     *            = ActivationDay
     * @return Billing day of the subscriber.
     * @throws HomeException
     *             Thrown if there are problems setting the billing day.
     */
    public static int setSubscribersBillingDayToActivationDay(final Context ctx, final Subscriber sub,
        final int activationDay) throws HomeException
    {
        final Home accountHome = (Home) ctx.get(AccountHome.class);
        if (accountHome == null)
        {
            throw new HomeException("System Error: AccountHome does not exist in context");
        }
        
        final Account account = AccountSupport.getAccount(ctx, sub.getBAN());
        if (account == null)
        {
            throw new HomeException("Parent account " + sub.getBAN() + " does not exist");
        }

        final int billingDay = BillCycleSupport.getBillCycleDayForActivationDay(activationDay);
        final int billingID = BillCycleSupport.getAutoBillCycleID(ctx, billingDay, sub.getSpid());
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(SubscriberSupport.class, "BillCycleID =" + billingID + " - Billing Day" + billingDay, null)
                .log(ctx);
        }
        
        if (billingID == -1)
        {
            throw new HomeException("BillCycleID for billingDay - " + billingDay + "  does not exsist ");
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(SubscriberSupport.class, "Setting BillCycleID ER909--account.billCycleID--" + billingID,
                null).log(ctx);
        }

        account.setBillCycleID(billingID);

        // Storing the Account
        accountHome.store(ctx, account);
        ctx.put(Lookup.ACCOUNT, account);

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(SubscriberSupport.class, "Setting subscriber BillingID  = " + account.getBillCycleID(),
                null).log(ctx);
        }

        return billingDay;
    }


    /**
     * internally used by remove to generate OM
     *
     * @param ctx
     *            The operating context.
     * @param el
     *            Exception listener.
     * @param lastResult
     *            Last result.
     * @param msg
     *            Log message.
     * @param resultCode
     *            Result code.
     * @param error
     *            Error message for OM.
     * @param caller
     *            Caller of this function.
     */
    public static void generateOM(final Context ctx, final HTMLExceptionListener el, final int lastResult,
            final String msg, final int resultCode, final String error, final Object caller)
    {
        generateOM(ctx, el, lastResult, msg, resultCode, error, caller, null);
    }
    
    public static void generateOM(final Context ctx, final HTMLExceptionListener el, final int lastResult,
        final String msg, final int resultCode, final String error, final Object caller, Exception cause)
    {
        if (el != null)
        {
            if (cause == null)
            {
                el.thrown(new HomeException(msg));
            }
            else
            {
                el.thrown(cause);
            }
        }

        // generate subscriber -removal failed - Out of Sync alarm
        final String[] str = new java.lang.String[]
        {
            String.valueOf(lastResult),
        };
        new EntryLogMsg(10573, caller, "", String.valueOf(resultCode), str, null).log(ctx);
        new OMLogMsg(Common.OM_MODULE, error).log(ctx);

        new MinorLogMsg(caller, error + " : " + msg + " . ResultCode: " + resultCode, null).log(ctx);
        if (cause != null
                && LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(caller, error + " : " + msg + " . ResultCode: " + resultCode, cause).log(ctx);
        }
    }


    /**
     * Returns a subscriber with the provided BAN and the provided MSISDN on the provided
     * date.
     *
     * @param context
     *            The operating context.
     * @param ban
     *            BAN of the account owning the subscriber.
     * @param msisdn
     *            The MSISDN being looked up.
     * @param date
     *            The date used for determining the MSISDN.
     * @return The subscriber owned the provided BAN with the provided MSISDN on the
     *         provided date.
     */
    public static Subscriber getSubscriber(final Context context, final String ban, final String msisdn, final Date date)
    {
        final Home subscriberProfileHome = (Home) context.get(SubscriberHome.class);
        if (subscriberProfileHome == null)
        {
            return null;
        }

        final And and = new And();
        and.add(new EQ(SubscriberXInfo.BAN, ban));
        and.add(new EQ(SubscriberXInfo.MSISDN, msisdn));
        and.add(new EQ(SubscriberXInfo.DATE_CREATED, date));

        try
        {
            final Collection t = subscriberProfileHome.where(context, and).selectAll(context);
            final Iterator it = t.iterator();

            while (it.hasNext())
            {
                final Subscriber sub = (Subscriber) it.next();
                if (ownsMSISDN(context, sub))
                {
                    return sub;
                }

            }
        }
        catch (final Throwable t)
        {
            LogSupport.debug(context, SubscriberSupport.class, "Exception caught: " + t.getClass().getSimpleName(), t);
        }

        return null;

    }


    /**
     * Returns a subscriber with the provided BAN
     * 
     * @param context
     *            The operating context.
     * @param ban
     *            BAN of the account owning the subscriber.
     * @return The subscriber with provided BAN
     */
    public static Subscriber getSubscriberIndividualAccount(final Context context, final String ban)
    {
        final And and = new And();
        and.add(new EQ(SubscriberXInfo.BAN, ban));
        try
        {
            Subscriber sub = HomeSupportHelper.get(context).findBean(context, Subscriber.class,
                    new EQ(SubscriberXInfo.BAN, ban));
            return sub;
        }
        catch (final Throwable t)
        {
            LogSupport.debug(context, SubscriberSupport.class, "Exception caught: " + t.getClass().getSimpleName(), t);
        }
        return null;
    }
    

    /**
     * Determines whether the subscriber ID should be auto-created.
     *
     * @param spid
     *            Service provider.
     * @param subscriber
     *            Subscriber to be created.
     * @return Returns <code>true</code> if the subscriber ID should be auto-created,
     *         <code>false</code> otherwise.
     */
    public static boolean isAutoCreateSubscriberId(final CRMSpid spid, final Subscriber subscriber)
    {
        boolean result = true;
        /*
         * TT 7091400011: If no subscriber ID is specified, use auto-created ID.
         */
        if (spid.getAllowToSpecifySubscriberId())
        {
            result = subscriber.getId() == null || subscriber.getId().equals(AbstractSubscriber.DEFAULT_ID)
                || subscriber.getId().equals(NULLID);
        }
        return result;
    }


    /**
     * Determines whether the subscriber moved in the given bill cycle.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being examined.
     * @param billingDate
     *            Billing date to use.
     * @return True if the subscriber moved in the given billing cycle.
     * @throws HomeException
     *             Thrown if there are problems looking up the bill cycle of the
     *             subscriber or notes associated with the subscriber.
     */
    public static boolean movedInCurrentBillCycle(final Context parentCtx, final Subscriber sub, final Date billingDate, final ServicePeriodEnum servicePeriod, Object item)
        throws HomeException
    {
        Context ctx = parentCtx.createSubContext();
        ctx.put(Subscriber.class, sub);
        
        final int spid = sub.getSpid();
        Date startDate;
        final Date endDate;

        ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(servicePeriod);
        final int billingCycleDay = SubscriberSupport.getBillCycleDay(ctx, sub);
        startDate = handler.calculateCycleStartDate(ctx, billingDate, billingCycleDay, spid, sub.getId(), item);
        endDate = handler.calculateCycleStartDate(ctx, billingDate, billingCycleDay, spid, sub.getId(), item);

        final Home noteHome = (Home) ctx.get(NoteHome.class);

        final And condition = new And();
        condition.add(new EQ(NoteXInfo.ID_IDENTIFIER, sub.getId()));
        condition.add(new GTE(NoteXInfo.CREATED, startDate));
        condition.add(new LTE(NoteXInfo.CREATED, endDate));
        condition.add(new Contains(NoteXInfo.NOTE, SubscriberSupport.SUBSCRIBER_MOVED_NOTE));
        return noteHome.find(ctx, condition) != null;
    }

    /**
     * Columns of subscriber table which are not XML blobs. Access this string via
     * {@link #getSubscriberNonXMLBlobColumns()} to ensure the string has been properly
     * initialized.
     */
    private static String subscriberNonXMLBlobColumns = null;

    public static Set<String> getSubscriptionIdsByMSISDN(Context ctx, String msisdn) throws HomeException
    {
        return getSubscriptionIdsByMSISDN(ctx, msisdn, new Date());
    }
    
    public static Set<String> getSubscriptionIdsByMSISDN(Context ctx, String msisdn, Date effectiveDate)
        throws HomeException
    {
        // get all the Msisdn History records for the MSISDN at that time
        final And filter = new And();
        filter.add(new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, msisdn));
        filter.add(new NEQ(MsisdnMgmtHistoryXInfo.SUBSCRIPTION_TYPE, MsisdnMgmtHistory.DEFAULT_SUBSCRIPTIONTYPE));
        filter.add(new LTE(MsisdnMgmtHistoryXInfo.TIMESTAMP, effectiveDate));
        filter.add(new GT(MsisdnMgmtHistoryXInfo.END_TIMESTAMP, effectiveDate));
        
        Home msisdnHistoryHome = (Home)ctx.get(MsisdnMgmtHistoryHome.class);

        final FunctionVisitor visitor = (FunctionVisitor) msisdnHistoryHome.forEach(ctx, new FunctionVisitor(
                new Function()
                {
                    /**
                     * Serial version UID.
                     */
                    private static final long serialVersionUID = 1L;


                    /**
                     * {@inheritDoc}
                     */
                    public Object f(final Context fCtx, final Object obj)
                    {
                        return ((MsisdnMgmtHistory)obj).getSubscriberId();
                    }
                }, new SetBuildingVisitor()), filter);
        
        return (Set<String>) visitor.getDelegate();
    }


    public static Collection<Subscriber> getSubscriptionsByMSISDN(Context ctx, String msisdn) throws HomeException
    {
        return getSubscriptionsByMSISDN(ctx, msisdn, new Date());
    }
    
    public static Collection<Subscriber> getSubscriptionsByMSISDN(Context ctx, String msisdn, Date effectiveDate)
        throws HomeException
    {
        Collection<Subscriber> subs = new ArrayList<Subscriber>();
        
        Set<String> subIds = getSubscriptionIdsByMSISDN(ctx, msisdn, effectiveDate);
        
        for (String subId : subIds)
        {
            Subscriber sub = null;
            HomeException exception = null;
            try
            {
                sub = SubscriberSupport.getSubscriber(ctx, subId);
            }
            catch (HomeException e)
            {
                new DebugLogMsg(SubscriberSupport.class, "Encountered a HomeException while trying to retreive Subscriber for subId for [id=" + subId + "]", e).log(ctx);
                exception = e;
            }
            
            if(sub == null)
            {
                String msg = "Unable to find Subscriber for [id=" + subId + "] which is referenced in the MsisdnMgmtHistory home.  "
                        + "History needs to be cleaned to bring the data into sync.";
                new MajorLogMsg(SubscriberSupport.class, msg, null).log(ctx);
                throw new HomeException(msg, exception);
            } else
            {
                subs.add(sub);
            }
        }
        
        return subs;
    }
    
    
    /**
     * 
     * @param ctx
     * @param subID
     * @return The subscriber's language or null if not found or on error
     * @throws HomeException
     */
    public static String getSubsLanguage(final Context ctx, final String subID) throws HomeException
    {
        return getSubLanguage(ctx, getSubscriber(ctx, subID));
    }

    /**
     * 
     * @param ctx
     * @param sub
     * @return The subscriber's language or null if not found or on error or not supported
     * @throws HomeException
     */
    public static String getSubLanguage(final Context ctx, final Subscriber sub) throws HomeException
    {
        if(!SystemSupport.supportsMultiLanguage(ctx))
        {
            return null;
        }
        try
        {
            return MultiLanguageSupport.getSubscriberLanguageWithDefault(ctx, sub.getSpid(), sub.getMSISDN());
        }
        catch (ProvisioningHomeException e)
        {
            new MinorLogMsg(SubscriberSupport.class,
                    "Exception encountered on look up of subscriber language for msisdn " + sub.getMSISDN()
                            + " due to " + e.getMessage() + ". Returning Null language.", null).log(ctx);
            // the stack trace is distracting in the MinorLogMessage, especially since the
            // issue is handled by returning NULL.
            new DebugLogMsg(SubscriberSupport.class, "Exception: " + e.getMessage(), e).log(ctx);
            return null;
        }
    }

    /**
     * Builds a Collection of SubscriberAuxiliaryService beans based on a Collection of SctAuxiliaryService beans.
     *
     * @param ctx the operating context
     * @param sctId sat for which to determine retrieve the auxiliary services
     * @return collection containing auxiliary services from the provided SCT
     */
    public static Collection getAuxiliaryServicesfromSCT(final Context ctx, final long sctId)
    {
        final Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        final Date today = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate);

        final Collection<SctAuxiliaryService> sctAuxServices = SubscriberCreateTemplateSupport.getSctAuxiliaryServices(ctx, sctId);
        final Collection<SubscriberAuxiliaryService> newAssociations = new ArrayList<SubscriberAuxiliaryService>();
        for (SctAuxiliaryService sctAux : sctAuxServices)
        {
            AuxiliaryService service = null;
            try
            {
                service = HomeSupportHelper.get(ctx).findBean(ctx, AuxiliaryService.class, sctAux.getAuxiliaryServiceIdentifier());
            }
            catch (HomeException e)
            {
                new MinorLogMsg(SubscriberSupport.class, "Error retrieving Auxiliary Service " + sctAux.getAuxiliaryServiceIdentifier() + ".  Skipping this service in template.", e).log(ctx);
                continue;
            }
            if (service != null
                    && EnumStateSupportHelper.get(ctx).stateEquals(service, AuxiliaryServiceStateEnum.ACTIVE))
            {
                SubscriberAuxiliaryService association = null;
                try
                {
                    association = (SubscriberAuxiliaryService) XBeans.instantiate(SubscriberAuxiliaryService.class, ctx);
                }
                catch (final Exception e)
                {
                    new MinorLogMsg(SubscriberSupport.class,
                            "Cannot instantiate SubscriberAuxiliaryService class.", e).log(ctx);
                    break;
                }

                association.setAuxiliaryServiceIdentifier(sctAux.getAuxiliaryServiceIdentifier());
                association.setCreated(runningDate);
                association.setStartDate(today);
                association.setPaymentNum(sctAux.getPaymentNum());

                newAssociations.add(association);
            }
        }

        return newAssociations;
    }

    public static Map getAuxiliaryBundleFeesFromSCT(final Context ctx, final long sctId)
    {
        final Collection auxBuns = SubscriberCreateTemplateSupport.getSctAuxiliaryBundles(ctx, sctId);
        final Map result = new HashMap(auxBuns.size());

        for (final Iterator it = auxBuns.iterator(); it.hasNext();)
        {
            final SctAuxiliaryBundle sctAuxBundle = (SctAuxiliaryBundle) it.next();
            BundleProfile bundle = null;
            try
            {
                // TODO 2009-01-21 investigate retreiving all bundles in one call
                bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, sctAuxBundle.getAuxiliaryBundleIdentifier());
            }
            catch (Exception e)
            {
                new MinorLogMsg(SubscriberSupport.class,
                        "cannot look up bundle " + sctAuxBundle.getAuxiliaryBundleIdentifier(), e).log(ctx);
            }

            if (bundle != null)
            {
                final BundleFee sctBundle = new BundleFee();
                sctBundle.setId(sctAuxBundle.getAuxiliaryBundleIdentifier());
                sctBundle.setPaymentNum(sctAuxBundle.getPaymentNum());

                sctBundle.setSource(BundleFee.AUXILIARY);
                sctBundle.setServicePreference(ServicePreferenceEnum.OPTIONAL);

                sctBundle.setFee(bundle.getAuxiliaryServiceCharge());

                // if this is a one time bundle, only add it to provisionable list if it isn't expired yet
                if (!bundle.getRecurrenceScheme().isOneTime() || (bundle.getEndDate() == null) || !bundle.getEndDate().before(new Date()) || bundle.getEndDate().equals(new Date(0)))
                {
                    result.put(Long.valueOf(sctBundle.getId()), sctBundle);
                }
            }
            else
            {
                new MinorLogMsg(SubscriberSupport.class,
                        "cannot look up bundle " + sctAuxBundle.getAuxiliaryBundleIdentifier(), null).log(ctx);
            }
        }

        return result;
    }
    
    /**
     * 
     * @param ctx
     * @param spid
     * @param creditCategory
     * @param billCycleId
     * @return
     * @throws HomeException
     */
    public static Map<String, String> getSubscriberMsisdn(Context ctx, final int spid, final int creditCategory, final int billCycleId, long subscriptionType)
    	throws HomeException
    {
    	final Map<String, String> subscriberIdentifiers = new HashMap<String, String>();

    	final XDB xdb = (XDB) ctx.get(XDB.class);

    	final XStatement sql = new XStatement()
    	{

    		private static final long serialVersionUID = 1L;


    		public String createStatement(final Context ctx)
    		{
    			return " SELECT ID, MSISDN FROM SUBSCRIBER S WHERE S.STATE = 1  AND S.BAN IN(" +
    					"SELECT BAN FROM ACCOUNT A WHERE A.SPID = ? AND A.CREDITCATEGORY= ? AND A.BILLCYCLEID= ? AND A.SYSTEMTYPE = 0)";
    		}


    		public void set(final Context ctx, final XPreparedStatement ps) throws SQLException
    		{
    			ps.setInt(spid);
    			ps.setInt(creditCategory);
    			ps.setInt(billCycleId);
    		}
    	};

    	xdb.forEach(ctx, new Visitor()
    	{

    		private static final long serialVersionUID = 1L;


    		public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
    		{
    			try
    			{
    				subscriberIdentifiers.put(((XResultSet) obj).getString(1), ((XResultSet) obj).getString(2));
    			}
    			catch (final SQLException e)
    			{
    				throw new AgentException(e);
    			}
    		}
    	}, sql);

    	return subscriberIdentifiers;
    }
    

    public static Collection<Class> getExtensionTypes(final Context ctx, final SubscriberTypeEnum type)
    {
        Set<Class<SubscriberExtension>> extClasses = ExtensionSupportHelper.get(ctx).getRegisteredExtensions(ctx,
                SubscriberExtension.class);
        Collection<Class> desiredExtTypes = new ArrayList<Class>();
        for (Class<SubscriberExtension> ext : extClasses)
        {
            try
            {
                if (type != null && SubscriberTypeDependentExtension.class.isAssignableFrom(ext))
                {
                    SubscriberTypeDependentExtension typeDependentExt = (SubscriberTypeDependentExtension) XBeans
                            .instantiate(ext, ctx);
                    if (typeDependentExt.isValidForSubscriberType(type))
                    {
                        desiredExtTypes.add(ext);
                    }
                }
                else
                {
                    desiredExtTypes.add(ext);
                }
            }
            catch (Exception ex)
            {
                new MajorLogMsg(AuxiliaryService.class, " Unable to instantiate ext class " + ext, ex).log(ctx);
            }
        }
        return desiredExtTypes;
    } 
    
    public static Integer getSubscriberSpid(final Context context, final String subscriberId) throws HomeException
    {
    	Subscriber sub = getSubscriber(context, subscriberId);
    	if(sub != null)
    	{
    		return sub.getSpid();
    	}
    	
    	return null;
    }

    
    public static boolean isBalanceThresholdAllowed(Context ctx, Subscriber subscriber)
    {
        if (subscriber.isPostpaid())
        {
            return false;
        }

        CRMSpid crmSpid;
        try
        {
            crmSpid = SpidSupport.getCRMSpid(ctx, subscriber.getSpid());

            if (crmSpid != null)
            {
                if (subscriber.getPricePlan() != -1l)
                {
                    PricePlan pricePlan = PricePlanSupport.getPlan(ctx, subscriber.getPricePlan());
                    switch (pricePlan.getPricePlanSubType().getIndex())
                    {
                        case PricePlanSubTypeEnum.MRC_INDEX:
                            return crmSpid.isBalThresholdCCAtuAllowedForMrc();

                        case PricePlanSubTypeEnum.PAYGO_INDEX:
                            return crmSpid.isBalThresholdCCAtuAllowedForPaygo();

                        case PricePlanSubTypeEnum.LIFETIME_INDEX:
                            return crmSpid.isBalThresholdCCAtuAllowedForLifetime();
                            
                        case PricePlanSubTypeEnum.PICKNPAY_INDEX:
                            return crmSpid.isBalThresholdCCAtuAllowedForPickNPay();
                    }
                }
            }
        }
        catch (HomeException e)
        {
            return false;
        }

        return false;
    }
    
    public static boolean isCCAtuScheduleAllowed(Context ctx, Subscriber subscriber)
    {
        CRMSpid crmSpid;
        try
        {
            crmSpid = SpidSupport.getCRMSpid(ctx, subscriber.getSpid());

            if (crmSpid != null)
            {
                if (subscriber.getPricePlan() != -1l)
                {
                    PricePlan pricePlan = PricePlanSupport.getPlan(ctx, subscriber.getPricePlan());
                    switch (pricePlan.getPricePlanSubType().getIndex())
                    {
                        case PricePlanSubTypeEnum.MRC_INDEX:
                            return crmSpid.isRecurCCAtuAllowedForMrc();

                        case PricePlanSubTypeEnum.PAYGO_INDEX:
                            return crmSpid.isRecurCCAtuAllowedForPaygo();

                        case PricePlanSubTypeEnum.LIFETIME_INDEX:
                            return crmSpid.isRecurCCAtuAllowedForLifetime();
                        
                        case PricePlanSubTypeEnum.PICKNPAY_INDEX:
                        	return crmSpid.isRecurCCAtuAllowedForPickNPay();
                        	
                    }
                }
            }
        }
        catch (HomeException e)
        {
            return false;
        }

        return false;
    }
    
    public static Collection<TopUpSchedule> getSubscriberTopUpSchedules(Context ctx, String subscriberId) 
    		throws HomeInternalException, HomeException
    {
        return HomeSupportHelper.get(ctx).getBeans(ctx, TopUpSchedule.class,
                new And().add(new EQ(TopUpScheduleXInfo.SUBSCRIPTION_ID, subscriberId)));
    }
    public static boolean validateSusbcriberStateActive(Subscriber subscriber){
    	if(subscriber.getState().equals(SubscriberStateEnum.ACTIVE))
    		return true;
    	else
    		return false;
    }
    
    public static Date getDefaultEndDate(Context ctx)
    {
        GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return new Date();
        //CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(gc.getDefaultEndDate());
    }
    
    public static boolean subscriberDepositReleasePostTermination(final Context context, Subscriber subscriber)
	{
		DeactivationReasonCodeMapping deactivationReasonCodeMapping = null;
        And filter = new And().add(new EQ(DeactivationReasonCodeMappingXInfo.SPID, subscriber.getSpid()));
        filter.add(new EQ(DeactivationReasonCodeMappingXInfo.DEACTIVATION_REASON, subscriber.getDeactivatedReason()));
		try 
		{
			deactivationReasonCodeMapping = HomeSupportHelper.get(context).findBean(context, DeactivationReasonCodeMapping.class, filter);
		}
		catch(HomeException e)
		{
			LogSupport.minor(context, SubscriberSupport.class.getName(), "Exception caught during collecting deactivationReasonCodeMapping object in SubscriberReleaseVisitor, for SPID"
					+subscriber.getSpid()+" and Deactivated Reason index : "+subscriber.getDeactivatedReason().getIndex()+" : "+  e);
		}
        if(deactivationReasonCodeMapping != null)
        {
        	if(deactivationReasonCodeMapping.getDeactivationReasonAction().getIndex() == DeactivationActionsEnum.DEPOSIT_HOLD_INDEX)
        	{
        		if (LogSupport.isDebugEnabled(context))
    			{
    				LogSupport.debug(context,SubscriberSupport.class.getName(), "Holding deposit for subscriber "+subscriber.getId()+" since deactivation reason code is mapped to hold deposit on deactivation");
    			}
        		return false;
        	}
        }
		return true;
	}
}
    

