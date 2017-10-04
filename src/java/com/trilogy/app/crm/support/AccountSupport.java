/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.account.AccountHierarchyService;
import com.trilogy.app.crm.account.BANAware;
import com.trilogy.app.crm.account.filter.ResponsibleAccountPredicate;
import com.trilogy.app.crm.bas.recharge.RechargeBillCycleVisitor;
import com.trilogy.app.crm.bas.tps.TPSSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCSVSupport;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AccountCreationTemplateHome;
import com.trilogy.app.crm.bean.AccountCreationTemplateXInfo;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXDBHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ContactTypeEnum;
import com.trilogy.app.crm.bean.IdentificationGroup;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.bean.account.ContactHome;
import com.trilogy.app.crm.bean.account.ContactXInfo;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.dunning.DunningProcessServer;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.SubscriberTypeDependentExtension;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.AccountExtensionHolder;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.supportInterface.AccountInterfaceSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.csv.Constants;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.elang.Order;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.FunctionVisitor;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.InternalLogSupport;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.contract.AccountContract;
import com.trilogy.app.crm.contract.AccountContractStatusEnum;
import com.trilogy.app.crm.contract.AccountContractXInfo;

/**
 * This is a collection of utility methods for working with Accounts. Review:
 * Several of the methods in this class populate usage information in an Account
 * object. This is such a bad design because it prevents those fields from being
 * moved elsewhere. The usage information is no longer displayed in the Account
 * but rather through the AccountUsage class but still these fields cannot be
 * removed from Account without changing how these methods work. If nobody else
 * expects those values in Account then they should be removed and this should
 * be cleaned up or, even better, moved to AccountUsage. KGR
 * 
 * @author kevin.greer@redknee.com
 */
public final class AccountSupport
implements AccountInterfaceSupport
{

    /**
     * Serial version UID.
     */
    public static final long serialVersionUID = -5813963152248135622L;
    /**
     * Used to indicate that a calculated value is invalid.
     */
    public static final int INVALID_VALUE = -9999;

    /**
     * Creates a new <code>AccountSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    public AccountSupport()
    {
       
    }

    /**
     * Returns the account id (BAN) of a msisdn.
     * 
     * @param ctx
     *            the context used as locator
     * @param msisdn
     *            the msisdn that we want to find the BAN for.
     * @return the BAN
     */
    public static String getBAN(final Context ctx, final String msisdn)
    {
        return getBAN(ctx, msisdn, (Date) null);
    }

    /**
     * Returns the account id (BAN) of a msisdn.
     * 
     * @param ctx
     *            the context used as locator
     * @param msisdn
     *            the msisdn that we want to find the BAN for
     * @param date
     *            the date that msisdn is still effective
     * @return the BAN
     */
    public static String getBAN(final Context ctx, final String msisdn, final Date date)
    {
        if (ctx == null)
        {
            InternalLogSupport.major(logModule_,
                "[AccountSupport.getBAN()] The context passed into getBAN is null. Cannot continue.", null);
            return null;
        }

        String accountNum = null;

        try
        {
            if (msisdn != null && !"".equals(msisdn.trim()))
            {
                try
                {
                    final Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDNLimited(ctx, msisdn, date);
                    if (sub != null)
                    {
                        accountNum = sub.getBAN();
                    }
                }
                catch (final Throwable th)
                {
                    new InfoLogMsg(logModule_, "[AccountSupport.getBAN()] This MSISDN " + msisdn
                        + " got an exception. " + th.getMessage(), th).log(ctx);
                }
            }
        }
        catch (final Exception e)
        {
            new InfoLogMsg(logModule_, "[AccountSupport.getBAN()] " + e.getMessage(), e).log(ctx);
        }

        return accountNum;
    }

    /**
     * Gets the subscribers for the given account.
     * 
     * @param context
     *            The operating context.
     * @param account
     *            the account for which we want the subscriber list
     * @return A collection of subscribers.
     * @deprecated use getImmediateChilderSubscribers.
     * @exception IllegalStateException
     *                Thrown if there are any problems interacting with the
     *                SubscriberHome.
     */
    @Deprecated
    public static Collection getSubscribers(final Context context, final Account account) throws IllegalStateException
    {
        return getSubscribers(context, account.getBAN());
    }

    /**
     * Gets the subscribers for the given account number.
     * 
     * @param context
     *            The operating context.
     * @param accountNumber
     *            the account number for which we want the subscriber list
     * @return A collection of subscribers.
     * @exception IllegalStateException
     *                Thrown if there are any problems interacting with the
     *                SubscriberHome.
     * @deprecated use getImmediateChilderSubscribers.
     */
    @Deprecated
    public static Collection getSubscribers(final Context context, final String accountNumber)
        throws IllegalStateException
    {
        Collection subscribers = null;
        try
        {
            subscribers = getImmediateChildrenSubscribers(context, accountNumber);
        }
        catch (final HomeException exception)
        {
            final IllegalStateException newException = new IllegalStateException(
                "Could not initialize Subscribers collection.");

            newException.initCause(exception);

            throw newException;
        }

        return subscribers;
    }

    /**
     * Gets a collection of the identifiers of subscribers in the account.
     * 
     * @param context
     *            The operating context.
     * @param account
     *            The account for which to return subscriber identifiers.
     * @return A collection of subscriber identifiers. Possibly empty but always
     *         non-null.
     * @exception HomeException
     *                Thrown if there is a problem accessing the Home data in
     *                the context.
     */
    public static Collection<String> getSubscriberIdentifiers(final Context context, final Account account)
        throws HomeException
    {
        return getSubscriberIdentifiers(context, account.getBAN());
    }

    /**
     * Gets a collection of the identifiers of subscribers in the account.
     * 
     * @param context
     *            The operating context.
     * @param accountIdentifier
     *            The identifier of the account for which to return subscriber
     *            identifiers.
     * @return A collection of subscriber identifiers. Possibly empty but always
     *         non-null.
     * @exception HomeException
     *                Thrown if there is a problem accessing the Home data in
     *                the context.
     */
    public static Collection<String> getSubscriberIdentifiers(final Context context, final String accountIdentifier)
        throws HomeException
    {
        final Home home = (Home) context.get(SubscriberHome.class);
        if (home == null)
        {
            throw new HomeException("Subscriber home not found in context!");
        }
        final Home filteredHome = home.where(context, new EQ(SubscriberXInfo.BAN, accountIdentifier));
        final FunctionVisitor visitor = (FunctionVisitor) filteredHome.forEach(context, new FunctionVisitor(
            SubscriberXInfo.ID, new ListBuildingVisitor()));
        return (ListBuildingVisitor) visitor.getDelegate();
    }

    /**
     * Since CRM 8.2: Return the "beginning of time": Date(0). the account
     * hierarchy restructuring in CRM 8.0 (Mobile Money) has broken the basic
     * assumption of the old lookup method (the top account in a hierarchy has
     * the first activity). Rather than doing expensive lookups to the Call
     * Detail and Transaction tables for the earliest activity date, we assume
     * the earliest activity date was the "beginning of time". The use of this
     * class is strictly to be used only after a verification that no previous
     * invoice exists. Prior to CRM 8.2: Looks-up and returns the earliest known
     * activity date for the given account. That is, the date of the earliest
     * found adjustment or transaction for the account. The current date is
     * returned if no activity is found.
     * 
     * @param ctx
     *            The operating context.
     * @param account
     *            The account for which to get the earliest activity date.
     * @return The earliest known activity date.
     */
    public static Date lookupEarliestActivityDate(final Context ctx, final Account account)
    {
        Date earliestDate = new Date(0);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(logModule_, "The earliest Activity Date for Account " + account.getBAN() + " is date="
                + earliestDate, null).log(ctx);
        }
        return earliestDate;
    }

	/**
	 * Updates the account summary calculations stored in the properties of this
	 * account. (Blocked Balance)
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param account
	 *            The account to update.
	 */
	public static void updateBlockedBalance(final Context ctx,
	    final Account account)
	{
		long amount = 0L;
		if (null == ctx)
		{
			return;
		}
		String ban = account.getBAN();
		try
		{
			amount = calculateBlockedBalance(amount, ctx, ban);
		}
		catch (HomeException e)
		{
			new MinorLogMsg(logModule_,
			    "Error featching transaction details for BAN: " + ban, e);
		}
		// TODO: implement logic
		account.setBlockedBalance(amount);
	}

    /**
     * @param amount
     * @param ctx
     * @param ban
     * @return
     * @throws HomeException
     */
    private static long calculateBlockedBalance(long amount, final Context ctx,
            String ban) throws HomeException
    {
		// TODO this should be moved to AppCrmCalculation
        final AdjustmentType adjustmentTypeDebit = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
            AdjustmentTypeEnum.DisputeRecipientBlockDebit);
        final AdjustmentType adjustmentTypeCredit = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
            AdjustmentTypeEnum.DisputeUnblockRecipientCredit);
        if (adjustmentTypeDebit != null && adjustmentTypeCredit != null)
        {
            final Number collectTransDebitAmount = HomeSupportHelper.get(ctx).sum(
                    ctx,
                    TransactionXInfo.AMOUNT,
                    new And().add(new EQ(TransactionXInfo.BAN, ban)).add(
                            new EQ(TransactionXInfo.ADJUSTMENT_TYPE, adjustmentTypeDebit.getCode())));
            final Number collectTransCreditAmount = HomeSupportHelper.get(ctx).sum(
                    ctx,
                    TransactionXInfo.AMOUNT,
                    new And().add(new EQ(TransactionXInfo.BAN, ban)).add(
                            new EQ(TransactionXInfo.ADJUSTMENT_TYPE, adjustmentTypeCredit.getCode())));
            amount = collectTransDebitAmount.longValue() + collectTransCreditAmount.longValue();
            if (amount < 0)
            {
                new MinorLogMsg(logModule_, "Dispute un-block sums up to be more than blocked amount sum. BAN: "
                    + ban, null).log(ctx);
                amount = 0;
            }
        }
        else
        {
            new MinorLogMsg(logModule_, "Blcoked Balance Calculation for BAN: " + ban
                + "Adjustment Type not found: " + AdjustmentTypeEnum.DisputeRecipientBlockDebit.getDescription()
                + " or " + AdjustmentTypeEnum.DisputeUnblockRecipientCredit, null).log(ctx);
        }
        return amount;
    }

    /**
     * Updates the "last bill date" and "payment due date" of the account.
     * 
     * @param account
     *            The account to update.
     */
    public static void updateInvoiceDates(final Account account)
    {
        final Context ctx = account.getContext();

        if (ctx == null)
        {
            return;
        }

        CalculationService service = (CalculationService) ctx.get(CalculationService.class);
        Invoice lastInvoice = null;
        try
        {
            lastInvoice = service.getMostRecentInvoice(ctx, account.getBAN());
        }
        catch (CalculationServiceException e)
        {
            new MinorLogMsg(TPSSupport.class, "Exception while fetching Most recent invoice for account", e).log(ctx);
        }

        if (lastInvoice != null)
        {
            account.setLastBillDate(lastInvoice.getInvoiceDate());
            account.setPaymentDueDate(lastInvoice.getDueDate());
        }
    }

    /**
     * Gets Service Provider object for account.
     * 
     * @param ctx
     *            The operating context.
     * @param account
     *            The account whose service provider is being looked up.
     * @return The service provider of this account.
     * @throws HomeException
     *             Thrown if there are problems looking up the service provider.
     */
    public static CRMSpid getServiceProvider(final Context ctx, final Account account) throws HomeException
    {
        return SpidSupport.getCRMSpid(ctx, account.getSpid());
    }

    /**
     * Gets the account with the given identifier.
     * 
     * @param ctx
     *            The operating context.
     * @param identifier
     *            The account identifier (BAN).
     * @return The account, or null if no such account exists.
     * @exception HomeException
     *                Thrown if there are problems accessing Home data in the
     *                context. Copied from ProvisionHome.java - JC 2004.11.04
     */
    public static Account getAccount(final Context ctx, final String identifier) throws HomeException
    {

        if (identifier == null)
        {
            throw new HomeException("Unable to lookup account.  Null identifier.");
        }

        if (ctx == null)
        {
            throw new HomeException("Unable to lookup account.  No context set.");
        }

        if (identifier.length() == 0)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(logModule_, "Cannot find account with accountID empty.", null).log(ctx);
            }

            return null;
        }

        return HomeSupportHelper.get(ctx).findBean(ctx, Account.class, identifier);
    }

    /**
     * Provides one utility function to get CSV file easily from beanShell.
     * 
     * @param ctx
     *            The operating context.
     * @param ban
     *            The BAND of the account.
     * @return The CSV string representing this account.
     * @throws HomeException
     *             Thrown if there are problems retrieving the account.
     */
    public static String toCSVString(final Context ctx, final String ban) throws HomeException
    {
        final StringBuffer sb = new StringBuffer();
        final Account account = getAccount(ctx, ban);
        if (account != null)
        {
            final AccountCSVSupport csv = new AccountCSVSupport();
            csv.append(sb, Constants.DEFAULT_SEPERATOR, account);
        }

        return sb.toString();
    }

    /**
     * Return the path from root to the current account.
     * 
     * @param ctx
     *            The operating context.
     * @param currentNodeBAN
     *            The current account BAN.
     * @return The path from root to the current account.
     */
    public static String getAccountPath(final Context ctx, final String currentNodeBAN)
    {
        final Home acctHome = (Home) ctx.get(AccountHome.class);

        final StringBuilder path = new StringBuilder();

        try
        {
            Account childAccount = (Account) acctHome.find(ctx, currentNodeBAN);
            String childAccountBAN = null;

            for (;;)
            {
                if (childAccount.getParent() != null)
                {
                    childAccountBAN = (String) childAccount.getParent();

                    path.append(childAccountBAN);
                    path.append(File.pathSeparator);

                    childAccount = (Account) acctHome.find(ctx, childAccountBAN);
                }
                else if (childAccount.getParent() == null
                    && !SafetyUtil.safeEquals(childAccount.getParent(), childAccountBAN)) // reached
                {
                    break;
                }
                else if (SafetyUtil.safeEquals(childAccount.getParent(), childAccountBAN))
                {
                    break;
                }
            }

            path.append(currentNodeBAN);
            path.append(File.pathSeparator);
            return path.toString();
        }
        catch (final Exception e)
        {
            new MinorLogMsg(logModule_, "fail to find account path.", e).log(ctx);
            return "";
        }
    }

    /**
     * Check if one account is the descendant of a "parentAccount".
     * 
     * @param ctx
     *            The operating context.
     * @param ancestorRef
     *            The ancestorRef to check for.
     * @param descendant
     *            The descendant to check for.
     * @return Returns <code>true</code> if the provided <code>descendant</code>
     *         is indeed a descendant of <code>ancestorRef</code>.
     * @throws HomeException
     *             Thrown if there are problems looking up.
     */
    public static boolean isDecendentAccount(final Context ctx, final Account ancestorRef, final Account descendant)
        throws HomeException
    {
        boolean isDecendent = false;
        if (ancestorRef != null && descendant != null)
        {
            for (Account descendentAccount : getDescendentAccounts(ctx, ancestorRef))
            {
                if (descendentAccount.getBAN().equals(descendant.getBAN()))
                {
                    isDecendent = true;
                    break;
                }
            }
        }
        return isDecendent;
    }

    /**
     * Gets all descendant accounts, not including itself.
     * 
     * @param ctx
     *            The operating context.
     * @param rootAccount
     *            The root account whose descendants are being looked up.
     * @return A collection of descendant accounts.
     * @throws HomeException
     *             Thrown if there are problems looking up the descendants of
     *             the account.
     */
    public static <ACCOUNT extends BANAware & SpidAware> List<Account> getDescendentAccounts(final Context ctx, final ACCOUNT rootAccountRef)
        throws HomeException
    {
        return getTopologyEx(ctx, rootAccountRef, null, null, true, false, null, false);
    }

    /**
     * Gets all components under the account, including all subscribers and
     * sub-accounts, recursively including itself.
     * 
     * @param ctx
     *            The operating context.
     * @param ancestorRef
     *            The ancestorRef account.
     * @return Every account and subscriber belonging to this account, including
     *         itself.
     * @throws HomeException
     *             Thrown if there are problems looking up the topology.
     */
    public static <ACCOUNT extends BANAware & SpidAware> List getTopology(final Context ctx, final ACCOUNT ancestorRef) throws HomeException
    {
        return getTopologyEx(ctx, ancestorRef, null, null, true, true, null, true);
    }

    /**
     * Traverse the tree, adding subscribers, sub accounts accordingly.
     * 
     * @param ctx
     *            The operating context.
     * @param ancestorRef
     *            The ancestorRef account.
     * @param subAccountFilter
     *            Predicate to determine whether which sub-accounts needs to be
     *            visited while traversing the tree. Use <code>null</code> if no
     *            special filtering is required.
     * @param subAccountAddFilter
     *            Predicate to determine whether which sub-accounts needs to be
     *            included in the result set. Use <code>null</code> if no
     *            special filtering is required.
     * @param addAccountToList
     *            Whether accounts are added to list.
     * @param addItselfToList
     *            Whether the ancestorRef account should be added to the list
     * @param subscriberFilter
     *            Predicate to determine whether subscriber needs to be
     *            included.
     * @param addSubscriberToList
     *            Whether subscribers are added to list.
     * @return A list of sub-accounts/subscribers belonging to the account.
     * @throws HomeException
     *             Thrown if there are problems looking up the topology.
     */
    public static <ACCOUNT extends BANAware & SpidAware> List getTopologyEx(final Context ctx, final ACCOUNT ancestorRef, final Predicate subAccountFilter,
        final Predicate subAccountAddFilter, final boolean addAccountToList, final boolean addItselfToList,
        final Predicate subscriberFilter, final boolean addSubscriberToList) throws HomeException
    {
        // for checking circular references
        final HashSet<String> allAccountIds = new HashSet<String>();
        allAccountIds.add(ancestorRef.getBAN());
        return getTopologyExNoLoop(ctx, ancestorRef, subAccountFilter, subAccountAddFilter, addAccountToList,
            addItselfToList, subscriberFilter, addSubscriberToList, allAccountIds);
    }

    public static <ACCOUNT extends BANAware & SpidAware> List getTopologyExNoLoop(final Context ctx, final ACCOUNT ancestorRef, final Predicate subAccountFilter,
        final Predicate subAccountAddFilter, final boolean addAccountToList, final boolean addItselfToList,
        final Predicate subscriberFilter, final boolean addSubscriberToList, final Set<String> allAccountIds)
        throws HomeException
    {
        // build a collection of children beans
        final List list = new ArrayList();

        if (addAccountToList && addItselfToList)
        {
            if (subAccountAddFilter == null || subAccountAddFilter.f(ctx, ancestorRef))
            {
                list.add(ancestorRef);
            }
        }

        if (addSubscriberToList)
        {
            // filter subscribers
            Collection<Subscriber> childrenSubs = getImmediateChildrenSubscribers(ctx, ancestorRef);

            if (subscriberFilter != null)
            {
                childrenSubs = CollectionSupportHelper.get(ctx).findAll(ctx, childrenSubs, subscriberFilter);
            }

            list.addAll(childrenSubs);
        }

        // filter accounts and recursively getting...
        // TODO 2007-05-24 reimplement as a Home Visitor to avoid loading all
        // beans into
        // memory
        Collection<Account> childrenAcct = getImmediateChildrenAccounts(ctx, ancestorRef.getBAN());

        if (subAccountFilter != null)
        {
            childrenAcct = CollectionSupportHelper.get(ctx).findAll(ctx, childrenAcct, subAccountFilter);
        }

        for (final Iterator<Account> iter = childrenAcct.iterator(); iter.hasNext();)
        {
            final Account childAcct = iter.next();

            if (allAccountIds.contains(childAcct.getBAN()))
            {
                throw new HomeException("Circular reference in account topology, root=" + ancestorRef.getBAN()
                    + ", child=" + childAcct.getBAN());
            }

            allAccountIds.add(childAcct.getBAN());

            list.addAll(getTopologyExNoLoop(ctx, childAcct, subAccountFilter, subAccountAddFilter, addAccountToList,
                true, subscriberFilter, addSubscriberToList, allAccountIds));
        }

        return list;
    }

    /**
     * Get all immediate subscribers and all subscribers under its
     * non-responsible sub accounts.
     * 
     * @param ctx
     *            The operating context.
     * @param ancestorRef
     *            The ancestorRef account.
     * @return A collection of all immediate subscribers belonging the account,
     *         and all subscribers under any of its non-responsible descendants.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscribers.
     */
    public static <ACCOUNT extends BANAware & SpidAware> Collection<Subscriber> getNonResponsibleSubscribers(final Context ctx, final ACCOUNT ancestorRef)
        throws HomeException
    {
        return getTopologyEx(ctx, ancestorRef, new ResponsibleAccountPredicate(false), null, false, true, null, true);
    }

    /**
     * Get all immediate subscribers and all subscribers under its
     * sub accounts.
     * 
     * @param ctx
     *            The operating context.
     * @param ancestorRef
     *            The ancestorRef account.
     * @return A collection of all immediate subscribers belonging the account,
     *         and all subscribers under any of its non-responsible descendants.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscribers.
     */
    public static <ACCOUNT extends BANAware & SpidAware> Collection<Subscriber> getAllSubscribers(final Context ctx, final ACCOUNT ancestorRef)
        throws HomeException
    {
        return getTopologyEx(ctx, ancestorRef, null, null, false, true, null, true);
    }

    /**
     * Returns all non-responsible sub-accounts of an account, plus itself.
     * 
     * @param ctx
     *            The operating context.
     * @param ancestorRef
     *            The account to be looked up.
     * @return a collection of non responsible accounts, including itself
     * @throws HomeException
     *             Thrown if there are problems looking up the accounts.
     */
    public static <ACCOUNT extends BANAware & SpidAware> Collection<Account> getNonResponsibleAccounts(final Context ctx, final ACCOUNT ancestorRef)
        throws HomeException
    {
        return getTopologyEx(ctx, ancestorRef, new ResponsibleAccountPredicate(false), null, true, true, null, false);
    }

    /**
     * Returns all immediate subscribers of the account, i.e., only the
     * subscribers whose BAN is the same as the provided account.
     * 
     * @param ctx
     *            The operating context.
     * @param parentAccount
     *            The account to look up.
     * @return The collection of all immediate subscribers.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscribers.
     */
    public static <ACCOUNT extends BANAware & SpidAware> Collection<Subscriber> getImmediateChildrenSubscribers(final Context ctx, final ACCOUNT parentAccountRef)
        throws HomeException
    {
        return getImmediateChildrenSubscribers(ctx, parentAccountRef.getBAN());
    }

    /**
     * Returns all immediate subscribers of the account, i.e., only the
     * subscribers whose BAN is the same as the provided account.
     * 
     * @param ctx
     *            The operating context.
     * @param parentBAN
     *            The account to look up.
     * @return The collection of all immediate subscribers.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscribers.
     */
    public static Collection<Subscriber> getImmediateChildrenSubscribers(final Context ctx, final String parentBAN)
        throws HomeException
    {
        return HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, new EQ(SubscriberXInfo.BAN, parentBAN));
    }

    /**
     * Returns all active immediate subscribers of the account, i.e., only the
     * active subscribers whose BAN is the same as the provided account.
     * 
     * @param context
     *            The operating context.
     * @param ban
     *            The account to look up.
     * @return The collection of all immediate subscribers.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscribers.
     */
    public static Collection<Subscriber> getActiveImmediateChildrenSubscribers(final Context context, final String ban)
        throws HomeException
    {
        And filter = new And();
        filter.add(new EQ(SubscriberXInfo.BAN, ban));
        filter.add(new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.ACTIVE));
        return HomeSupportHelper.get(context).getBeans(context, Subscriber.class, filter);
    }

    /**
     * retrieve the number of immediate children subscribers.
     * 
     * @param ctx
     *            The operating context.
     * @param ban
     *            The BAN being processed.
     * @return The number of subscribers with the provided BAN.
     * @throws HomeException
     *             Thrown if there are problems tallying the number.
     * @by Candy Wong
     * @date April 22, 2005
     */
    public static long totalImmediateSubscribers(final Context ctx, final String ban) throws HomeException
    {
        return HomeSupportHelper.get(ctx).getBeanCount(ctx, Subscriber.class, new EQ(SubscriberXInfo.BAN, ban));
    }

    /**
     * Gets immediate Children Accounts list.
     * 
     * @param ctx
     *            The operating context.
     * @param parentId
     *            The ID of the parent account.
     * @return A collection of immediate children (accounts) of the provided
     *         account.
     * @throws HomeException
     *             Thrown if there are problems looking up the children
     *             accounts.
     * @deprecated Use (@link #getImmediateChildrenAccountHome(Context, String))
     */
    @Deprecated
    public static Collection<Account> getImmediateChildrenAccounts(final Context ctx, final String parentId)
        throws HomeException
    {
        return getImmediateChildrenAccounts(ctx, parentId, null);
    }

    /**
     * Gets the collection of immediate children accounts which are
     * non-responsible.
     * 
     * @param ctx
     *            The operating context.
     * @param parentId
     *            The ID of the parent account.
     * @return A collection of immediate children (accounts) of the provided
     *         account which are non-responsible.
     * @throws HomeException
     *             Thrown if there are problems looking up the children
     *             accounts.
     * @deprecated Use (@link
     *             #getImmediateNonResponsibleChildrenAccountHome(Context,
     *             String))
     */
    @Deprecated
    public static Collection<Account> getImmediateNonResponsibleChildrenAccounts(final Context ctx,
        final String parentId) throws HomeException
    {
        return getImmediateChildrenAccounts(ctx, parentId, Boolean.FALSE);
    }

    /**
     * Gets the collection of immediate children accounts which are
     * non-responsible.
     * 
     * @param ctx
     *            The operating context.
     * @param parentId
     *            The ID of the parent account.
     * @return A collection of immediate children (accounts) of the provided
     *         account which are non-responsible.
     * @throws HomeException
     *             Thrown if there are problems looking up the children
     *             accounts.
     * @deprecated Use (@link
     *             #getImmediateNonResponsibleChildrenAccountHome(Context,
     *             String))
     */
    @Deprecated
    public static Collection<Account> getImmediateResponsibleChildrenAccounts(final Context ctx, final String parentId)
        throws HomeException
    {
        return getImmediateChildrenAccounts(ctx, parentId, Boolean.TRUE);
    }

    /**
     * Returns a collection of immediate children accounts.
     * 
     * @param ctx
     *            The operating context.
     * @param parentId
     *            The parent ID.
     * @param responsible
     *            indicating whether we need to check responsible field, if
     *            null, we ignore this
     * @return A collection of immediate children accounts of the provided BAN.
     * @throws HomeException
     *             Thrown if there are problems looking up the children.
     */
    protected static Collection<Account> getImmediateChildrenAccounts(final Context ctx, final String parentId,
        final Boolean responsible) throws HomeException
    {
        Home home = getImmediateChildrenAccountHome(ctx, parentId);

        if (responsible != null)
        {
            home = home.where(ctx, new EQ(AccountXInfo.RESPONSIBLE, responsible));
        }

        return home.selectAll(ctx);
    }

    /**
     * Returns an account home filtered by parent ID. All accounts in the
     * resulting home will have the provided ID as parent.
     * 
     * @param ctx
     *            The operating context.
     * @param parentId
     *            The ID of the parent.
     * @return An account home filtered by parent ID.
     */

    public static Home getImmediateChildrenAccountHome(final Context ctx, final String parentId)
    {
        final Home home = (Home) ctx.get(AccountHome.class);
        return home.where(ctx, new EQ(AccountXInfo.PARENT_BAN, parentId));
    }

    /**
     * Returns an account home filtered by parent ID and are non-responsible.
     * All accounts in the resulting home will have the provided ID as parent,
     * and are non-responsible.
     * 
     * @param ctx
     *            The operating context.
     * @param parentId
     *            The ID of the parent.
     * @return An account home filtered by parent ID and are non-responsible.
     */
    public static Home getImmediateNonResponsibleChildrenAccountHome(final Context ctx, final String parentId)
    {
        final Home home = getImmediateChildrenAccountHome(ctx, parentId);
        return home.where(ctx, new EQ(AccountXInfo.RESPONSIBLE, Boolean.FALSE));
    }

    /**
     * Returns an account home filtered by parent ID and are responsible. All
     * accounts in the resulting home will have the provided ID as parent, and
     * are responsible.
     * 
     * @param ctx
     *            The operating context.
     * @param parentId
     *            The ID of the parent.
     * @return An account home filtered by parent ID and are responsible.
     */
    public static Home getImmediateResponsibleChildrenAccountHome(final Context ctx, final String parentId)
    {
        final Home home = getImmediateChildrenAccountHome(ctx, parentId);
        return home.where(ctx, new EQ(AccountXInfo.RESPONSIBLE, Boolean.TRUE));
    }

    /**
     * Returns the account of a msisdn.
     * 
     * @param ctx
     *            the context used as locator
     * @param msisdn
     *            the msisdn that we want to find the BAN for
     * @return the BAN
     * @throws HomeException
     *             Thrown if there are problems looking up the BAN or acocunt.
     */
    public static Account getAccountByMsisdn(final Context ctx, final String msisdn) throws HomeException
    {
        final String ban = getBAN(ctx, msisdn);

        if (ban == null)
        {
            return null;
        }
        return getAccount(ctx, ban);
    }

    /**
     * Returns if the Account's PaymentPlan field has changed to a valid
     * PaymentPlan value.
     * 
     * @param ctx
     *            the context used as locator
     * @param newAccount
     *            the msisdn that we want to find the BAN for
     * @return the result
     */
    public static boolean hasPaymentPlanChanged(final Context ctx, final Account newAccount)
    {
        try
        {
            final long newPaymentPlan = newAccount.getPaymentPlan();
            final long oldPaymentPlan = getPaymentPlanFromPersistentAccount(ctx, newAccount);
            return oldPaymentPlan != newPaymentPlan;
        }
        catch (final HomeException e)
        {
            // return a default value.
        }
        return false;
    }

    /**
     * Returns the Payment Plan ID found in the Persistent Account. If the
     * persistent account doesn't exists it returns the Default Payment Plan ID.
     * 
     * @param ctx
     *            The operating context.
     * @param newAccount
     *            The new account.
     * @return The payment plan ID found in the persistent account.
     * @throws HomeException
     *             Thrown if there are problems retrieving the payment plan ID.
     */
    public static long getPaymentPlanFromPersistentAccount(final Context ctx, final Account newAccount)
        throws HomeException
    {
        final Home home = (Home) ctx.get(AccountHome.class);
        final Account oldAccount = (Account) home.find(ctx, newAccount.getBAN());
        // If no persistent account exists, check that the new account has other
        // than
        // "---" payment plan
        if (oldAccount == null)
        {
            return PaymentPlanSupport.INVALID_PAYMENT_PLAN_ID;
        }
        return oldAccount.getPaymentPlan();
    }

    /**
     * Returns if the Account's PaymentPlan field has changed from null Payment
     * Plan (i.e. "---") Pre: newAccount has to have a valid PaymentPlan.
     * 
     * @param ctx
     *            the context used as locator
     * @param newAccount
     *            the msisdn that we want to find the BAN for
     * @return the result
     * @throws HomeException
     *             Thrown if there are problems determining the payment plan.
     */
    public static boolean isSettingPaymentPlan(final Context ctx, final Account newAccount) throws HomeException
    {
        final long oldPaymentPlan = getPaymentPlanFromPersistentAccount(ctx, newAccount);

        return oldPaymentPlan == PaymentPlanSupport.INVALID_PAYMENT_PLAN_ID && hasPaymentPlanChanged(ctx, newAccount)
            && PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, newAccount.getPaymentPlan());
    }

    /**
     * Returns the number of Active postpaid subscribers in the given account's
     * topology.
     * 
     * @param ctx
     *            The operating context.
     * @param parentAccount
     *            The parent account.
     * @return The number of active postpaid subscribers in the account's
     *         topology.
     */
    public static int getNumberOfActivePostpaidSubscribersInTopology(final Context ctx, final Account parentAccount)
    {
        final AccountHierarchyService service = (AccountHierarchyService) ctx.get(AccountHierarchyService.class);
        return service.getNumberOfActivePostpaidSubscribersInTopology(parentAccount);
    }

    /**
     * Returns TRUE if there is more than 1 active postpaid subscriber in the
     * account hierarchy. (This check includes the given account's immediate
     * children subscribers as well as the children of the non-responsible
     * sub-accounts with the given account as a root account.) Returns FALSE,
     * otherwise.
     * 
     * @param ctx
     *            The operating context.
     * @param parentAccount
     *            root account
     * @return Whether the account has more than one active postpaid subscribers
     *         in its hierarchy.
     */
    public static boolean hasMoreThanOnePostpaidSubscriber(final Context ctx, final Account parentAccount)
    {
        final int numPostpaidSubs = getNumberOfActivePostpaidSubscribersInTopology(ctx, parentAccount);
        return numPostpaidSubs > 1;
    }

    /**
     * Account to state mapping for Prepaid/Postpaid subscriber.
     * 
     * @param accountState
     *            The account state.
     * @param subType
     *            The subscriber type.
     * @return The subscriber state corresponding to the provided account state.
     */
    public static SubscriberStateEnum accountStateToSubStateMapping(final AccountStateEnum accountState,
        final short subType)
    {
        SubscriberStateEnum result = null;
        switch (accountState.getIndex())
        {
            case AccountStateEnum.ACTIVE_INDEX:
                result = SubscriberStateEnum.ACTIVE;
                break;
            /*
             * for Prepaid subscriber suspended account state is mapped to
             * Barred state of subscriber.
             */
            case AccountStateEnum.SUSPENDED_INDEX:
                if (subType == SubscriberTypeEnum.PREPAID_INDEX)
                {
                    result = SubscriberStateEnum.LOCKED;
                }
                else
                {
                    result = SubscriberStateEnum.SUSPENDED;
                }
                break;
            case AccountStateEnum.INACTIVE_INDEX:
                result = SubscriberStateEnum.INACTIVE;
                break;

            case AccountStateEnum.NON_PAYMENT_WARN_INDEX:
                result = SubscriberStateEnum.NON_PAYMENT_WARN;
                break;

            case AccountStateEnum.NON_PAYMENT_SUSPENDED_INDEX:
                result = SubscriberStateEnum.NON_PAYMENT_SUSPENDED;
                break;

            case AccountStateEnum.PROMISE_TO_PAY_INDEX:
                result = SubscriberStateEnum.PROMISE_TO_PAY;
                break;

            case AccountStateEnum.IN_ARREARS_INDEX:
                result = SubscriberStateEnum.IN_ARREARS;
                break;

            case AccountStateEnum.IN_COLLECTION_INDEX:
                result = SubscriberStateEnum.IN_COLLECTION;
                break;

            default:
                // this case will never happen in current scenario
        }
        return result;
    }

    /**
     * Apply account creation template values to the account.
     * 
     * @param ctx
     *            Context.
     * @param account
     *            Account object.
     * @param actId
     *            Account creation template identifier.
     * @throws HomeException
     *             , CloneNotSupportedException
     */
    public static void applyAccountCreationTemplate(final Context ctx, final Account account, final int actId)
        throws HomeException, CloneNotSupportedException
    {
        applyAccountCreationTemplate(ctx, account, actId, false);
    }

    /**
     * Apply account creation template values to the account.
     * 
     * @param ctx
     *            Context.
     * @param account
     *            Account object.
     * @param actId
     *            Account creation template identifier.
     * @param mandatoryOnly
     *            Apply only mandatory fields.
     * @throws HomeException
     *             , CloneNotSupportedException
     */
    public static void applyAccountCreationTemplate(final Context ctx, final Account account, final int actId,
        final boolean mandatoryOnly) throws HomeException, CloneNotSupportedException
    {
        final Home home = (Home) ctx.get(AccountCreationTemplateHome.class);
        final AccountCreationTemplate act = (AccountCreationTemplate) home.find(ctx, Long.valueOf(actId));
        if (act == null)
        {
            new DebugLogMsg(AccountSupport.class, "actId= " + actId + " not found.", null).log(ctx);
        }
        else
        {
            applyAccountCreationTemplate(ctx, account, act, mandatoryOnly);
        }
    }

    public static void applyAccountCreationTemplate(final Context ctx, final Account account,
        final AccountCreationTemplate act, final boolean mandatoryOnly)
    {
        if (act == null)
        {
            return;
        }
        if (act.getSpid() != account.getSpid())
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(AccountSupport.class, "ACT " + act.getName() + " - invalid Spid.", null).log(ctx);
            }
        }
        else
        {
            final List mandatoryFields = act.getMandatoryFields();
            Predicate shouldApply = new Predicate() {

                @Override
                public boolean f(Context context, Object obj) throws AbortVisitException
                {
                    boolean result = !mandatoryOnly;
                    if (obj instanceof PropertyInfo && mandatoryFields != null)
                    {
                        result |= mandatoryFields.contains(new StringHolder(((PropertyInfo) obj).getName()));
                    }
                    return result;
                }
            };

            if (shouldApply.f(ctx, AccountCreationTemplateXInfo.GROUP_TYPE))
            {
                account.setGroupType(act.getGroupType());
            }
            if (shouldApply.f(ctx, AccountCreationTemplateXInfo.TYPE))
            {
                account.setType(act.getType());
            }
            if (shouldApply.f(ctx, AccountCreationTemplateXInfo.SYSTEM_TYPE))
            {
                account.setSystemType(act.getSystemType());
            }
            if (shouldApply.f(ctx, AccountCreationTemplateXInfo.TAX_EXEMPTION))
            {
                account.setTaxExemption(act.getTaxExemption());
            }
            if (shouldApply.f(ctx, AccountCreationTemplateXInfo.BILLING_MSG_PREFERENCE))
            {
                account.setBillingMsgPreference(act.getBillingMsgPreference());
            }

            // Only set responsible to a different value if the account is not a
            // Root
            // Account.
            if (!account.isRootAccount() && shouldApply.f(ctx, AccountCreationTemplateXInfo.RESPONSIBLE))
            {
                account.setResponsible(act.getResponsible());
            }

            if (act.getCreditCategory() > -1 && shouldApply.f(ctx, AccountCreationTemplateXInfo.CREDIT_CATEGORY))
            {
                account.setCreditCategory(act.getCreditCategory());
            }

            if (act.getDealerCode() != null && !"".equals(act.getDealerCode().trim())
                && shouldApply.f(ctx, AccountCreationTemplateXInfo.DEALER_CODE))
            {
                account.setDealerCode(act.getDealerCode());
            }

            if (act.getDiscountClass() > -1 && shouldApply.f(ctx, AccountCreationTemplateXInfo.DISCOUNT_CLASS))
            {
                account.setDiscountClass(act.getDiscountClass());
            }

            if (act.getTaxAuthority() > -1 && shouldApply.f(ctx, AccountCreationTemplateXInfo.TAX_AUTHORITY))
            {
                account.setTaxAuthority(act.getTaxAuthority());
            }

            if (act.getLanguage() != null && !"".equals(act.getLanguage().trim())
                && shouldApply.f(ctx, AccountCreationTemplateXInfo.LANGUAGE))
            {
                account.setLanguage(act.getLanguage());
            }

            if (act.getBillCycleID() > -1 && shouldApply.f(ctx, AccountCreationTemplateXInfo.BILL_CYCLE_ID))
            {
                account.setBillCycleID(act.getBillCycleID());
            }

            if (act.getBillingCountry() != null && !"".equals(act.getBillingCountry().trim())
                && shouldApply.f(ctx, AccountCreationTemplateXInfo.BILLING_COUNTRY))
            {
                account.setBillingCountry(act.getBillingCountry());
            }

            if (act.getBillingProvince() != null && !"".equals(act.getBillingProvince().trim())
                && shouldApply.f(ctx, AccountCreationTemplateXInfo.BILLING_PROVINCE))
            {
                account.setBillingProvince(act.getBillingProvince());
            }

            if (act.getPaymentMethodType() > -1 && shouldApply.f(ctx, AccountCreationTemplateXInfo.PAYMENT_METHOD_TYPE))
            {
                account.setPaymentMethodType(act.getPaymentMethodType());
            }

            if (act.getInvoiceDeliveryOption() > -1
                && shouldApply.f(ctx, AccountCreationTemplateXInfo.INVOICE_DELIVERY_OPTION))
            {
                account.setInvoiceDeliveryOption(act.getInvoiceDeliveryOption());
            }

            if (act.getAccountExtensions().size() > 0
                && shouldApply.f(ctx, AccountCreationTemplateXInfo.ACCOUNT_EXTENSIONS))
            {
                applyExtensionsTemplates(ctx, account, act);
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(logModule_, "Applied Values from ACT " + act.getName() + " to the Account profile",
                    null).log(ctx);
            }
        }
    }

    private static void applyExtensionsTemplates(final Context ctx, final Account account,
        final AccountCreationTemplate act)
    {
        for (Object obj : act.getAccountExtensions())
        {
            AccountExtension actExtension = (AccountExtension) ((ExtensionHolder) obj).getExtension();
            AccountExtension accountExtension = actExtension;
            try
            {
                accountExtension = (AccountExtension) actExtension.deepClone();
            }
            catch (CloneNotSupportedException e)
            {
                new DebugLogMsg(logModule_, e.getClass().getSimpleName() + " occurred in "
                    + AccountSupport.class.getSimpleName() + ".applyExtensionsTemplates(): " + e.getMessage(), e)
                    .log(ctx);
            }
            updateExtensionInAccount(account, accountExtension);

        }
    }

    private static void updateExtensionInAccount(final Account account, final AccountExtension accountExtension)
    {
        boolean extensionUpdated = false;
        accountExtension.setBAN(account.getBAN());
        accountExtension.setSpid(account.getSpid());
	
        for (Object obj : account.getAccountExtensions())
        {
            Extension currentExtension = ((ExtensionHolder) obj).getExtension();

            if (currentExtension.getClass().equals(accountExtension.getClass()))
            {
                ((ExtensionHolder) obj).setExtension(accountExtension);
                extensionUpdated = true;
                break;
            }
        }

        if (!extensionUpdated)
        {
            ExtensionHolder holder = new AccountExtensionHolder();
            holder.setExtension(accountExtension);

            List<ExtensionHolder> newExtensions = new ArrayList<ExtensionHolder>(account.getAccountExtensions());
            newExtensions.add(holder);
            account.setAccountExtensions(newExtensions);
        }

    }

    /**
     * Creates an empty account identification group list in the provided
     * account, with the groups and number of identifications defined in the
     * spid indentification groups configuration.
     * 
     * @param ctx
     * @param account
     * @throws HomeException
     */
    public static void createEmptyAccountIdentificationGroupsList(Context ctx, Account account) throws HomeException
    {
        SpidIdentificationGroups idGroups = HomeSupportHelper.get(ctx).findBean(ctx, SpidIdentificationGroups.class,
            account.getSpid());
        createEmptyAccountIdentificationGroupsList(ctx, account, idGroups);

    }

    /**
     * Creates an empty account identification group list in the provided
     * account, with the groups and number of identifications defined in the
     * spid indentification groups configuration.
     * 
     * @param ctx
     * @param account
     * @throws HomeException
     */
    public static void createEmptyAccountIdentificationGroupsList(Context ctx, Account account,
        SpidIdentificationGroups idGroups)
    {
        List idList = createEmptyAccountIdentificationGroupsList(ctx, idGroups);
        account.setIdentificationGroupList(idList);
    }

	/**
     * @param ctx
     * @param idGroups
     * @return
     */
	public static List<AccountIdentificationGroup> createEmptyAccountIdentificationGroupsList(Context ctx,
        SpidIdentificationGroups idGroups)
    {
	    List<AccountIdentificationGroup> idList = new ArrayList<AccountIdentificationGroup>();
        if (idGroups != null)
        {
            Iterator<IdentificationGroup> iter = idGroups.getGroups().iterator();
            while (iter.hasNext())
            {
                IdentificationGroup group = iter.next();
                if (group.getRequiredNumber() > 0)
                {
                    AccountIdentificationGroup accountIdGroup = null;
                    try
                    {
                        accountIdGroup = (AccountIdentificationGroup) XBeans.instantiate(
                            AccountIdentificationGroup.class, ctx);
                    }
                    catch (Exception e)
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport
                                .debug(
                                    ctx,
                                    AccountSupport.class,
                                    "Exception while instantiating a new AccountIdentificationGroup object. Creating an empty one.",
                                    e);
                        }
                        accountIdGroup = new AccountIdentificationGroup();
                    }
                    accountIdGroup.setIdGroup(group.getIdGroup());
                    accountIdGroup.setGroup(group.getName());
                    for (int i = 0; i < group.getRequiredNumber(); i++)
                    {
                        AccountIdentification ai = null;
                        try
                        {
                            ai = (AccountIdentification) XBeans.instantiate(AccountIdentification.class, ctx);
                        }
                        catch (Exception e)
                        {
                            if (LogSupport.isDebugEnabled(ctx))
                            {
                                LogSupport
                                    .debug(
                                        ctx,
                                        AccountSupport.class,
                                        "Exception while instantiating a new AccountIdentification object. Creating an empty one.",
                                        e);
                            }
                            ai = new AccountIdentification();
                        }
                        ai.setIdGroup(group.getIdGroup());
                        accountIdGroup.getIdentificationList().add(ai);
                    }
                    idList.add(accountIdGroup);
                }
            }
        }
	    return idList;
    }

    public static Contact lookupContactOfAccount(Context context, Account account) throws HomeException
    {
        Home home = (Home) context.get(ContactHome.class);
        if (home == null)
        {
            throw new HomeException("Contact home not found in context!");
        }
        if (account == null)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, AccountSupport.class,
                    "No account supplied to lookupContactOfAccount, returning null");
            }
            return null;
        }

        And and = new And();
        and.add(new EQ(ContactXInfo.ACCOUNT, account.getBAN()));
        Collection contacts = home.select(context, and);
        if (contacts == null)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, AccountSupport.class, "No contact found for BAN " + account.getBAN()
                    + "; returning null");
            }
            return null;
        }

        for (Object obj : contacts)
        {
            Contact contact = (Contact) obj;
            switch (contact.getType())
            {
                case ContactTypeEnum.PERSON_INDEX:
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        LogSupport.debug(context, AccountSupport.class,
                            "Personal contact found for BAN " + account.getBAN());
                    }
                    return contact;
                }
                case ContactTypeEnum.COMPANY_INDEX:
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        LogSupport.debug(context, AccountSupport.class,
                            "Company contact found for BAN " + account.getBAN());
                    }
                    return contact;
                }
            }
        }

        if (LogSupport.isDebugEnabled(context))
        {
            LogSupport.debug(context, AccountSupport.class, "No contact found for BAN " + account.getBAN()
                + "; returning null");
        }
        return null;
    }

    
    
    @Override
	public Account getAccountByBan(Context ctx, String ban) 
    throws HomeException
    {
    		return this.getAccount( ctx, ban);
    	
	}


    public static Collection<Class> getExtensionTypes(final Context ctx, final Account account)
    {
        return getExtensionTypes(ctx, (account != null && account.isPooled(ctx)));
    }


    public static Collection<Class> getExtensionTypes(final Context ctx, final boolean isPooled)
    {
        Set<Class<AccountExtension>> extClasses = ExtensionSupportHelper.get(ctx).getRegisteredExtensions(ctx,
                AccountExtension.class);
        Collection<Class> desiredExtTypes = new ArrayList<Class>();
        for (Class<AccountExtension> ext : extClasses)
        {
            if (PoolExtension.class.isAssignableFrom(ext) && isPooled)
            {
                desiredExtTypes.add(ext);
            }
            else
            {
                desiredExtTypes.add(ext);
            }
        }
        return desiredExtTypes;
    }
    
    public static long getTotalAccountsMoved(Context ctx, Account rootAccount)
    {
        long totalAccounts = 0;
        List accountsAndSubs = new ArrayList<Account>();
        try
        {
            accountsAndSubs = getTopologyEx(ctx, rootAccount, null, null, true, true, null, true);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(logModule_, "Error retrieving descendent accounts of " + rootAccount.getBAN(), e).log(ctx);
        }
        if (accountsAndSubs != null)
        {            
            for (Object obj : accountsAndSubs)
            {
                if (obj instanceof Account)
                {
                    Account account = (Account) obj;
                    if (account != null) 
                    {
                        totalAccounts ++;
                    }
                }
            }
        }
        return totalAccounts;
    }


    /**
     * Returns all non deactive subscribers of the account, i.e., only the
     * active subscribers whose BAN is the same as the provided account.
     * 
     * @param ctx
     *            The operating context.
     * @param ban
     *            The account to look up.
     * @return The collection of all subscribers.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscribers.
     */
    public static Collection<Subscriber> getNonDeActiveChildrenSubscribers(final Context ctx, final Account account)
        throws HomeException
    {
        List<Subscriber> subscriberList = null;
        subscriberList = getTopologyEx(ctx, account, new NEQ(AccountXInfo.RESPONSIBLE, true), null, false, false, new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE), true);
        return subscriberList;
    }
    
   /**
    *  
    * @param ctx
    * @param account
    * @return First Active subscriber for the Account
    * @throws HomeException
    */
    public static Subscriber getFirstActiveSubscriber(final Context ctx, final Account account)
            throws HomeException
        {
            List<Subscriber> subscriberList = null;
            Subscriber subscriber = null;
            And subFilter = new And();
            subFilter.add(new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.ACTIVE));
            subFilter.add(new Order().add(SubscriberXInfo.ID, true));
            subscriberList = getTopologyEx(ctx, account, null, null, false, false, subFilter, true);
            if(subscriberList.size() > 0)
            {
                subscriber = subscriberList.get(0);
            }
            return subscriber;
        }
    
    public static Integer getAccountSpid(final Context ctx, final String identifier) throws HomeException
    {
    	Account account = getAccount(ctx, identifier);
    	if(account != null)
    	{
    		return account.getSpid();
    	}
    	
    	return null;
    }
    
    /**
     * Returns an account home filtered by parent ID. All accounts in the
     * resulting home will have the provided ID as parent.
     * 
     * @param ctx
     *            The operating context.
     * @param parentId
     *            The ID of the parent.
     * @return An account home filtered by parent ID.
     * @throws HomeException 
     * @throws UnsupportedOperationException 
     * @throws HomeInternalException 
     */

    public static int getImmediateChildrenActiveAccountCount(final Context ctx, final String parentId) throws HomeException
    {
        final Home home = (Home) ctx.get(AccountHome.class);
        And subFilter = new And();
        subFilter.add(new EQ(AccountXInfo.PARENT_BAN, parentId));
        subFilter.add(new EQ(AccountXInfo.STATE, AccountStateEnum.ACTIVE));
        Collection coll = home.select(ctx, subFilter);
        return coll.size();
    }
    
    /**
     * Return the list of BANs of accounts which are eligible for 
     * MultiDay Recurring Charge to be applied on.
     * This is created in order to avoid visitor operating on complete resultset of accounts.
     * Instead individual account is picked up and operated upon for MultiDay RC.
     * 
     * @param ctx The operating context.
     * @param spid Service Provider
     * @return List of BANs eligible for MultiDay Recurring Charge
     * @throws HomeException
     */
    
    public static Collection <String> getMultidayRecurringChargeAndNotificationEligibleBANsList(final Context ctx, CRMSpid spid, SubscriberTypeEnum applicableSubscriberType) throws HomeException
    {
    	final Collection <String> bansList = new ArrayList<String>();
    	
    	final XDB xdb = (XDB) ctx.get(XDB.class);
    	
    	final String accountTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, AccountHome.class,
                AccountXInfo.DEFAULT_TABLE_NAME);
    	
    	StringBuilder query = new StringBuilder();
    	query.append("select BAN from ").append(accountTableName);
    	query.append(" where SPID = ").append(spid.getId());
    	
    	if (applicableSubscriberType !=null) {
    		query.append(" and SYSTEMTYPE IN (");  
    		Set<SubscriberTypeEnum> setApplicableSubType = SpidSupport.getSystemTypeToCharge(applicableSubscriberType);

    		Iterator <SubscriberTypeEnum> subStateEnumIter  = setApplicableSubType.iterator();
    		while(subStateEnumIter.hasNext())
    		{
    			query.append(((SubscriberTypeEnum) subStateEnumIter.next()).getIndex());
    			if(subStateEnumIter.hasNext())
    			{
    				query.append(" , ");
    			}
    		}
    		query.append(" )");
    	}
    	
    	Set<AccountStateEnum> accRechargeStateSet = RechargeBillCycleVisitor.getAccountRechargeStateSet(spid.isApplyRecurringChargeForSuspendedSubscribers());
    	if(accRechargeStateSet != null)
    	{
    		query.append(" and STATE in (");
	    	
	    	Iterator <AccountStateEnum> stateEnumIter  = accRechargeStateSet.iterator();
	    	while(stateEnumIter.hasNext())
	    	{
	    		query.append(((AccountStateEnum) stateEnumIter.next()).getIndex());
	    		if(stateEnumIter.hasNext())
	    		{
	    			query.append(" , ");
	    		}
	    	}
	    	query.append(" )");
    	}
    	
    	String sql = query.toString(); 
    	try
    	{
	    	xdb.forEach(ctx, 
	    			new Visitor()
			    	{
						private static final long serialVersionUID = 1L;
			
						public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException 
						{
							try 
							{
								bansList.add(((XResultSet) obj).getString(1));
							} catch (SQLException e) 
							{
								throw new AgentException(e);
							}
						}
			    	}
	    			, sql);
    	}
    	catch(Exception ex)
    	{
    		throw new HomeException(ex);
    	}
    	
    	return bansList;
    }
    
    /**
     * 
     * @param ctx
     * @param billCycleId
     * @param creditCategory
     * @param responsible
     * @return
     * @throws HomeException
     */
    
    public static Collection <String> getBANsListPerCreditCategory(final Context ctx, long billCycleId, int creditCategory, boolean responsible) throws HomeException
    {
    	final Collection <String> bansList = new ArrayList<String>();
    	
    	final XDB xdb = (XDB) ctx.get(XDB.class);
    	
    	final String accountTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, AccountHome.class,
                AccountXInfo.DEFAULT_TABLE_NAME);
    	
    	StringBuilder query = new StringBuilder();
    	query.append("select BAN from ").append(accountTableName);
    	query.append(" where BILLCYCLEID = ").append(billCycleId);
    	query.append(" and creditCategory = ").append(creditCategory);
    	query.append(" and responsible = ").append(responsible?"'y'":"'n'");
    	
    	String sql = query.toString(); 
    	try
    	{
    		xdb.forEach(ctx, 
    				new Visitor()
    		{
    			private static final long serialVersionUID = 1L;

    			public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException 
    			{
    				try 
    				{
    					bansList.add(((XResultSet) obj).getString(1));
    				} catch (SQLException e) 
    				{
    					throw new AgentException(e);
    				}
    			}
    		}
    		, sql);
    	}
    	catch(Exception ex)
    	{
    		throw new HomeException(ex);
    	}
    	
    	return bansList;
    }
    
    /**
     * 
     * @param ctx
     * @param sql
     * @return
     */
    public static String getQueryForDunning(Context ctx, String sql)
    {
    	final String accountTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, AccountHome.class,
                AccountXInfo.DEFAULT_TABLE_NAME);
    	
    	StringBuilder query = new StringBuilder();
    	query.append("select BAN from ").append(accountTableName);
    	query.append(" where ");
    	query.append(sql);
    	
    	return query.toString();
    }
    /**
     * Gets a predicate for accounts to be processed by the dunning agent.
     * @param ctx
     * @return
     */
    public static String getQueryForDunnProcess(Context ctx)
    {
    	final String accountTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, AccountHome.class,
                AccountXInfo.DEFAULT_TABLE_NAME);
    	
    	StringBuilder query = new StringBuilder();
    	query.append("select BAN from ").append(accountTableName);
    	query.append(" where ((((not ( state IN (");
    	query.append( AccountStateEnum.INACTIVE.getIndex());
    	query.append(" , ");
    	query.append( AccountStateEnum.SUSPENDED.getIndex());
    	query.append(" ))) ");
    	query.append(" or ptpTermsTightened = ").append("'y'))");
    	query.append(" and responsible = ").append("'y'");
    	query.append(" and systemType <> ").append(SubscriberTypeEnum.PREPAID.getIndex());
    	query.append(" ) ");
    	return query.toString(); 
    }
     
    /**
     * Gets a predicate for accounts to be processed by the dunning agent.
     * @return
     */
    public static String getQueryForDunnProcessWithBillCycle(Context ctx, int billCycleId)
    {
    	StringBuilder query = new StringBuilder();
    	query.append(getQueryForDunnProcess(ctx));
    	query.append(" and billCycleID = ").append(billCycleId);
    	
    	return query.toString(); 
    }
     
    /**
     * Gets a predicate for accounts to be processed by the dunning agent.
     * @return
     */
    public static String getQueryForDunnProcessWithSPID(Context ctx, int spid)
    {
    	StringBuilder query = new StringBuilder();
    	query.append(getQueryForDunnProcess(ctx));
    	query.append(" and spid = ").append(spid);
    	
    	return query.toString(); 
    }
    /**
     * 
     * @param ctx
     * @param creditCategory
     * @return
     */
    public static String getQueryForDunnProcessWithCrediCategory(Context ctx, int creditCategory) 
    {
    	StringBuilder query = new StringBuilder();
    	query.append(getQueryForDunnProcess(ctx));
    	
    	query.append(" and ( state = ").append(AccountStateEnum.PROMISE_TO_PAY.getIndex());
    	query.append(" or state = ").append(AccountStateEnum.NON_PAYMENT_WARN.getIndex());
    	query.append(" or state = ").append(AccountStateEnum.NON_PAYMENT_SUSPENDED.getIndex());
    	query.append(" or state = ").append(AccountStateEnum.IN_ARREARS.getIndex());
    	query.append(" ) and creditCategory = ").append(creditCategory);
    	
    	return query.toString(); 
    }
    
   
    /**
     * Return Collection of executing query
     * @param ctx
     * @param sql
     * @return
     * @throws HomeException
     */
    public static Collection <Object> getQueryDataList(final Context ctx, String sql) throws HomeException
    {
    	final Collection <Object> queryList = new ArrayList<Object>();
    	
    	final XDB xdb = (XDB) ctx.get(XDB.class);
    	
    	try
    	{
    		xdb.forEach(ctx, 
    				new Visitor()
    		{
    			private static final long serialVersionUID = 1L;

    			public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException 
    			{
    				try 
    				{
    					queryList.add(((XResultSet) obj).getString(1));
    				} catch (SQLException e) 
    				{
    					LogSupport.minor(ctx, this, "Error getting while get date from Resultset. ", e);
    					throw new AgentException(e);
    				}
    			}
    		}
    		, sql);
    	}
    	catch(Exception ex)
    	{
    		LogSupport.minor(ctx,AccountSupport.class.getSimpleName() , "Error getting execute query. ", ex);
    		throw new HomeException(ex);
    	}
    	
    	return queryList;
    }
    
    public static long getAccountContract(Context ctx, String ban)
    {
        try
        {
            AccountContract contract =  (AccountContract)HomeSupportHelper.get(ctx).findBean(ctx, AccountContract.class, new EQ(AccountContractXInfo.BAN, ban));
            if(null!=contract && (contract.getStatus().equals(AccountContractStatusEnum.APPLICABLE)|| contract.getStatus().equals(AccountContractStatusEnum.PREESTABLISHED)))
            {
            	return contract.getContractId();
            }else
            {
            	 return  -1;
            }
           
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, AccountSupport.class.getSimpleName() , "Error while searching for Account in AccountContract table. ", e);
        }
        return -1;
    }
    
    
    /**
     * Return the list of BANs of accounts which are eligible for 
     * Monthly / Weekly Recurring Charge tasks as well as Notification tasks.
     * This is created in order to avoid visitor operating on complete resultset of accounts.
     * Instead individual account is picked up and operated upon.
     * 
     * @param ctx The operating context.
     * @param spid Service Provider
     * @return List of eligible BANs
     * @throws HomeException
     */
    
    public static Collection <String> getMonthlyWeeklyRechargeAndNotificationEligibleBANsList(final Context ctx, int billCycleId, CRMSpid spid, SubscriberTypeEnum applicableSubscriberType) throws HomeException
    {
    	final Collection <String> bansList = new ArrayList<String>();
    	
    	final XDB xdb = (XDB) ctx.get(XDB.class);
    	
    	final String accountTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, AccountHome.class,
                AccountXInfo.DEFAULT_TABLE_NAME);
    	
    	StringBuilder query = new StringBuilder();
    	query.append("select BAN from ").append(accountTableName);
    	query.append(" where BILLCYCLEID = ").append(billCycleId);
    	
    	Set<AccountStateEnum> accRechargeStateSet = RechargeBillCycleVisitor.getAccountRechargeStateSet(spid.isApplyRecurringChargeForSuspendedSubscribers());
    	if(accRechargeStateSet != null)
    	{
    		query.append(" and STATE in (");
	    	
	    	Iterator <AccountStateEnum> stateEnumIter  = accRechargeStateSet.iterator();
	    	while(stateEnumIter.hasNext())
	    	{
	    		query.append(((AccountStateEnum) stateEnumIter.next()).getIndex());
	    		if(stateEnumIter.hasNext())
	    		{
	    			query.append(" , ");
	    		}
	    	}
	    	query.append(" )");
    	}
    	
    	Set<SubscriberTypeEnum> systemTypeToChargeSet = RechargeBillCycleVisitor.getSystemTypeToCharge(applicableSubscriberType);
    	if(systemTypeToChargeSet !=null)
    	{
    		query.append(" and SYSTEMTYPE in (");
    		
    		Iterator <SubscriberTypeEnum> sysTypeIter = systemTypeToChargeSet.iterator();
    		while(sysTypeIter.hasNext())
    		{
    			query.append(((SubscriberTypeEnum)sysTypeIter.next()).getIndex());
    			if(sysTypeIter.hasNext())
    			{
    				query.append(" , ");
    			}
    		}
    		query.append(" )");
    	}
    	
    	String sql = query.toString(); 
    	try
    	{
	    	xdb.forEach(ctx, 
	    			new Visitor()
			    	{
						private static final long serialVersionUID = 1L;
			
						public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException 
						{
							try 
							{
								bansList.add(((XResultSet) obj).getString(1));
							} 
							catch (SQLException e) 
							{
								throw new AgentException(e);
							}
						}
			    	}
	    			, sql);
    	}
    	catch(Exception ex)
    	{
    		throw new HomeException(ex);
    	}
    	
    	return bansList;
    }
    
    public static List<Account> getParentAccounts(Context ctx, String ban) {
		List<Account>  accountBeans = null;
		try {
 			 And filter = new And();
			 filter.add(new EQ(AccountXInfo.PARENT_BAN,ban));
			 accountBeans = (List<Account>)HomeSupportHelper.get(ctx).getBeans(ctx, Account.class, filter);
		} catch (Exception e) {
			LogSupport.minor(ctx, logModule_, "Cannot find Parent accounts: "+ban);
		}
		return accountBeans;
	}
	
	public static long getSubscriptionCount(Context ctx,String ban) {
		long count = 0;
		try {
			 And filter = new And();
			 filter.add(new EQ(SubscriberXInfo.BAN,ban));
			count = HomeSupportHelper.get(ctx).getBeanCount(ctx, Subscriber.class, filter);
		} catch (Exception e) {
			LogSupport.minor(ctx, logModule_, "Cannot find Subscriber for account: "+ban);
		}
		return count;
	}
    
	private static String logModule_ = AccountSupport.class.getName();
}
