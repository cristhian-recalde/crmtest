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
package com.trilogy.app.crm.home;

import java.util.Date;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.account.state.agent.AccountStateBlackListAgent;
import com.trilogy.app.crm.account.state.agent.AccountStateChangeOMLog;
import com.trilogy.app.crm.account.state.agent.AccountStateSubAccountStateAgent;
import com.trilogy.app.crm.account.state.agent.AccountStateSubscriberCreditLimitAgent;
import com.trilogy.app.crm.account.state.agent.AccountStateSubscriberStateAgent;
import com.trilogy.app.crm.ban.BANGenerator;
import com.trilogy.app.crm.ban.BANGeneratorFactory;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.NoteXInfo;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.TransactionMethodHome;
import com.trilogy.app.crm.dunning.DunningProcess;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.TaxAuthoritySupportHelper;


public class AccountProvisioningHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    // DZ: added for readibility
    public static final int EVENT_TYPE_ACTIVATION = 1;

    public static final int EVENT_TYPE_MODIFICATION = 2;

    public static final int PROCESS_RESULT_SUCCESS = 0;

    public static final int PROCESS_RESULT_GENERAL_ERROR = 9999;


    /**
     * Create a new instance of <code>AccountProvisioningHome</code>.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            Delegate of this home.
     */
    public AccountProvisioningHome(final Context ctx, final Home delegate)
    {
        super(delegate);
        setContext(ctx);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ACTIVATE_ATTEMPT).log(ctx);

        if (!(obj instanceof Account))
        {
            throw new HomeException("System Error: cannot save a non-account to AccountHome");
        }

        Account account = (Account) obj;
        /*
         * will only create a new account number if the ban is not set
         */
        if (!account.isBANSet())
        {
            account.setBAN(getBAN(ctx, account));
        }
        try
        {
            account = (Account) getDelegate().create(ctx, account);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "ADDED account: " + account.getBAN(), null).log(ctx);
            }
        }
        /*
         * TODO: to be inserted later to handle SQLExceptions catch (SQLException sqlEx) {
         * result = 3003; new OMLogMsg(Common.OM_MODULE,
         * Common.OM_ACCT_ACTIVATE_FAIL).log(ctx); }
         */
        catch (final HomeException hEx)
        {
            // int result = 9999;

            // the original exception... note how this isn't passed on [PS]
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, hEx.getMessage(), hEx).log(ctx);
            }

            logAccountProvisioningER(ctx, account, EVENT_TYPE_ACTIVATION, PROCESS_RESULT_GENERAL_ERROR);
            new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ACTIVATE_FAIL).log(ctx);
            throw hEx;
        }

        // Create Activation note
        try
        {
            createAccountNote(ctx, account);
            setPaymentMethodName(ctx, account);
        }
        catch (final Throwable t)
        {
            // Error creating a Note
            new InfoLogMsg(this, "Unable to generate Account Activation Note", t).log(ctx);

        }

        // Generate Account Provisioning ER
        logAccountProvisioningER(ctx, account, EVENT_TYPE_ACTIVATION, PROCESS_RESULT_SUCCESS);

        new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ACTIVATE_SUCCESS).log(ctx);
        return account;
    }


    /**
     * 
     * @param ctx
     * @param account
     * @return
     */
    private String getBAN(final Context ctx, final Account account) throws HomeException
    {
        // look for the next BAN
        final Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        if (spidHome == null)
        {
            throw new HomeException("System Error: CRMSpidHome does not exist in context");
        }
        final CRMSpid spid = (CRMSpid) spidHome.find(ctx, Integer.valueOf(account.getSpid()));
        if (spid == null)
        {
            throw new HomeException(
                    "Configuration Error: Service Provider is mandatory, make sure it exists before continuing");
        }
        
        //Default BAN generator
        BANGenerator generator = BANGeneratorFactory.getBeanGenerator(ctx, account.getSpid());
        return generator.generateBAN(ctx, account);
    }

    /**
     * The GUI contains this process but this is only in case The account is created
     * outside the GUI.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            Account being updated.
     * @throws HomeException
     *             Thrown if there are problems finding the transaction method.
     */
    private void setPaymentMethodName(final Context ctx, final Account account) throws HomeException
    {
        // Maybe it was made by the GUI no need to set it again
        if (account.getPaymentMethod() == null || account.getPaymentMethod().length() == 0)
        {
            final Home methodHome = (Home) ctx.get(TransactionMethodHome.class);

            TransactionMethod method = null;

            if (account.isResponsible())
            {
                method = (TransactionMethod) methodHome.find(ctx, Long.valueOf(account.getPaymentMethodType()));
            }

            if (method != null)
            {
                account.setPaymentMethod(method.getName());
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        int result = 0;
        boolean needsDunning = false;
        new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_MODIFY_ATTEMPT).log(ctx);

        if (!(obj instanceof Account))
        {
            throw new HomeException("System Error: cannot save a non-account to AccountHome");
        }

        final Account newAccount = (Account) obj;
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "INPUT for modified account: " + newAccount.getBAN(), null).log(ctx);
        }

        // find the old Account so that we could tell how the state changes
        Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        if (oldAccount == null)
        {
            // fallback. should not happen
            LogSupport.minor(ctx, this, "OLD_ACCOUNT is not set in Context");
            oldAccount = (Account) find(ctx, newAccount);
        }


        if (oldAccount == null)
        {
            throw new HomeException("Cann't find existing Account " + newAccount.getBAN() + ". Use create instead.");
        }

        // change Account state, TODO: handle Exception gracefully
        try
        {
            changeAccountState(ctx, newAccount, oldAccount);
        }
        catch (final Exception e)
        {
            new MinorLogMsg(this, "Account state change failed", e).log(ctx);
        }

        Object ret = null;
        boolean crmResult = false;

        /*
         * [Cindy Wong] TT 8062600017: Need to store the old account for comparison.
         */
        final Context subContext = ctx.createSubContext();
        subContext.put(Common.OLD_ACCOUNT, oldAccount);

        try
        {
            ret = super.store(ctx, newAccount);

            /*
             * once the account has been stored, retreive it to see if the updated info is
             * there
             */
            final Home accountHome = (Home) ctx.get(AccountHome.class);

            Account updated_account;
            try
            {
                updated_account = (Account) accountHome.find(ctx, newAccount);

                crmResult = true;

                if (updated_account != null)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "UPDATED account: " + updated_account.getBAN(), null).log(ctx);
                    }
                }
                else
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "UPDATED account: null", null).log(ctx);
                    }
                }
            }
            catch (final HomeException he)
            {
                new MinorLogMsg(this, "Error occurred when retreiving account with spid = " + newAccount.getBAN()
                    + ". Exception: " + he, null).log(ctx);
            }
        }
        /*
         * TODO: to be inserted later to handle SQLExceptions catch (SQLException sqlEx) {
         * result = 3003; new OMLogMsg(Common.OM_MODULE,
         * Common.OM_ACCT_MODIFY_FAIL).log(ctx); }
         */

        catch (final HomeException hEx)
        {
            if (hEx.getCause() instanceof CompoundIllegalStateException)
            {
                throw hEx;
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, hEx.getMessage(), hEx).log(ctx);
                }

                result = 3009;

                logAccountProvisioningER(ctx, newAccount, EVENT_TYPE_MODIFICATION, result);
                new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_MODIFY_FAIL).log(ctx);
                throw hEx;
            }
        }

        new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_MODIFY_SUCCESS).log(ctx);

        if (AccountStateEnum.PROMISE_TO_PAY.equals(newAccount.getState())
            && !AccountStateEnum.PROMISE_TO_PAY.equals(oldAccount.getState()))
        {
            /*
             * another dunning occurs when PTP --> Active/Dunned/Warned so no worry to
             * have it twice.
             */
            needsDunning = true;
        }
        else if (AccountStateEnum.ACTIVE.equals(newAccount.getState()))
        {
            // HLD obj 6771
            if (AccountStateEnum.SUSPENDED.equals(oldAccount.getState())
                || AccountStateEnum.PROMISE_TO_PAY.equals(oldAccount.getState())
                || AccountStateEnum.IN_COLLECTION.equals(oldAccount.getState()))
            {
                // invoke dunning
                needsDunning = true;
            }
        }

        // put dunning as late as possible
        // TT#11022854052: Dunning should only be run when parent account is not updating, otherwise
        // it will be run by one of the sub-accounts before parent account state is set to the correct value.
        if (needsDunning && !ctx.getBoolean(IS_PARENT_ACCOUNT_UPDATING, false))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Invoking dunning process for account " + newAccount.getBAN()
                    + " state change from " + oldAccount.getState() + " to " + newAccount.getState(), null).log(ctx);
            }
            try
            {
                final DunningProcess dunningProcess = (DunningProcess) ctx.get(DunningProcess.class);
                dunningProcess.processAccount(subContext, new Date(), newAccount);
            }
            catch (final Throwable t)
            {
                new MinorLogMsg(this, "Dunning Process has failed for Account [BAN " + newAccount.getBAN() + "]", t)
                    .log(ctx);
            }
        }
        else
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "No dunning required for account " + newAccount.getBAN() + " state change from "
                    + oldAccount.getState() + " to " + newAccount.getState(), null).log(ctx);
            }
        }

        // Generate note for updated accounts
        try
        {
            createAccountNote(ctx, oldAccount, newAccount, crmResult);
            setPaymentMethodName(ctx, newAccount);
        }
        catch (final Throwable t)
        {
            // Error creating a Note
            new InfoLogMsg(this, "Unable to generate Account Update Note", t).log(ctx);
        }

        logAccountProvisioningER(ctx, newAccount, EVENT_TYPE_MODIFICATION, PROCESS_RESULT_SUCCESS);

        return ret;
    }


    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        int result = 0;
        new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_REMOVE_ATTEMPT).log(ctx);

        if (!(obj instanceof Account))
        {
            throw new HomeException("System Error: cannot save a non-account to AccountHome");
        }

        final Account account = (Account) obj;

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Remove Account: " + account.toString(), null).log(ctx);
        }

        try
        {
            getDelegate().remove(ctx, account);
        }
        catch (final HomeException e)
        {
            result = 9999;

            new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_REMOVE_FAIL).log(ctx);

            logRemovalER(ctx, account.getSpid(), account.getBAN(), result);

            throw e;
        }

        logRemovalER(ctx, account.getSpid(), account.getBAN(), 0);

        new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_REMOVE_SUCCESS).log(ctx);

        // remove Note entry for this account
        removeNotes(ctx, account);
    }


    // DZ: create this method to "pretect the ER field from having the delimit char in the
    // content
    private String escapeLimit(final String s)
    {
        return ERLogger.addDoubleQuotes(s);

    }


    // DZ: I changed sigature to allow passing much more account information
    // to address the new req. in F.S.

    public void logAccountProvisioningER(final Context ctx, final Account account, final int eventType, final int result)
    {
        if (account == null)
        {
            return;
        }

        final User principal = (User) ctx.get(java.security.Principal.class);
        
        String teic = "";
        if (TaxAuthoritySupportHelper.get(ctx).isTEICEnabled(ctx, account.getSpid()))
        {
            teic = String.valueOf(account.getTEIC());
        }

        new ERLogMsg(760, 700, "Account Provisioning Event", account.getSpid(), new String[]
        {
            principal == null ? null : principal.getId(),
            account.getBAN(),
            String.valueOf(account.getTaxAuthority()),
            String.valueOf(eventType),
            String.valueOf(account.getBillCycleID()),	
            escapeLimit(account.getBillingAddress1()),
            escapeLimit(account.getBillingAddress2()),
            escapeLimit(account.getBillingAddress3()),
            escapeLimit(account.getBillingCity()),
            
            // VG added as TT 12061415056 
            escapeLimit(account.getBillingPostalCode()),
            
            escapeLimit(account.getBillingProvince()),
            escapeLimit(account.getBillingCountry()),
            escapeLimit(account.getCompanyName()),
            escapeLimit(account.getTradingName()),
            account.getRegistrationNumber(),
            escapeLimit(account.getCompanyAddress1()),
            escapeLimit(account.getCompanyAddress2()),
            escapeLimit(account.getCompanyCity()),
            escapeLimit(account.getCompanyProvince()),
            escapeLimit(account.getCompanyCountry()),
            String.valueOf(account.getCreditCategory()),
            account.getDealerCode(),
            escapeLimit(account.getFirstName()),
            escapeLimit(account.getLastName()),
            // TODO 2010-10-01 DateFormat access needs synchronization
            ERLogger.formatERDateDayOnly(new Date()),
            String.valueOf(account.getState().getIndex()), // F.S. did not specify the format
                                                // required.
            // DZ added as TT 402252021
            account.getAccountCategory(ctx).getName(),
            account.getPromiseToPayDate() == null ? "" : ERLogger.formatERDateDayOnly(account.getPromiseToPayDate()),
            account.getReason(),
            account.getLastBillDate() == null ? "" : ERLogger.formatERDateDayOnly(account.getLastBillDate()),
            account.getPaymentDueDate() == null ? "" : ERLogger.formatERDateDayOnly(account.getPaymentDueDate()),
            String.valueOf(account.getDiscountClass()),
            String.valueOf(0), /*
                     * Accumulated Balance is set to zero because the method to calculate
                     * (account.getAccumulatedBalance()) is much too expensive to run on
                     * accounts with many subscribers (> 500 subscribers)
                     */
            account.getAccountMgr(),
            //
            // ali: added fields for DataMart
            escapeLimit(account.getAccountName()), // dng: added double quotes
            String.valueOf(account.getSystemType().getIndex()), account.getParentBAN(),
            account.getResponsible() ? "1" : "0", account.getTaxExemption() ? "1" : "0", account.getLanguage(),
            account.getCurrency(),
            //

            String.valueOf(result),
            teic,
            String.valueOf(account.getCategory()),
            String.valueOf(account.getActId()),
            String.valueOf(account.getRole()),
            (eventType == EVENT_TYPE_MODIFICATION && !account.isPrepaid() && account.isInCollection() && 
                    account.isResponsible()) ? String.valueOf(account.getDebtCollectionAgencyId()) : "-1",
             account.getCsa() == null ? "" : account.getCsa()
            
        })

        .log(ctx);
    }


    public void logRemovalER(final Context ctx, final int spid, final String BAN, final int result)
    {

        final User principal = (User) ctx.get(java.security.Principal.class);

        new ERLogMsg(764, 700, "Account Removal Event", // DZ: make it consistent with FS
            spid, new String[]
            {
                principal == null ? null : principal.getId(), BAN, String.valueOf(result)
            }).log(ctx);
    }


    private void changeAccountState(final Context ctx, final Account newAccount, final Account oldAccount)
        throws HomeException
    {
        Context subCtx = ctx.createSubContext();
        subCtx.put(IS_PARENT_ACCOUNT_UPDATING, Boolean.TRUE);
        
        final AccountStateEnum oldState = oldAccount.getState();
        final AccountStateEnum newState = newAccount.getState();

        if (LogSupport.isDebugEnabled(subCtx))
        {
            new DebugLogMsg(this, "Account State Will Be Changed from: " + oldAccount.getState() + " to: "
                + newAccount.getState(), null).log(subCtx);
        }

        // state is the same don't do anything
        if (oldState.equals(newState))
        {
            return;
        }
        else
        {

            new AccountStateSubscriberCreditLimitAgent(subCtx, null).onStateChange(subCtx, oldAccount, newAccount);
            new AccountStateSubscriberStateAgent(subCtx, null).onStateChange(subCtx, oldAccount, newAccount);
            new AccountStateBlackListAgent(subCtx, null).onStateChange(subCtx, oldAccount, newAccount);
            new AccountStateSubAccountStateAgent(subCtx, null).onStateChange(subCtx, oldAccount, newAccount);
            new AccountStateChangeOMLog(subCtx, null).onStateChange(subCtx, oldAccount, newAccount);
            setInCollectionDate(subCtx, oldAccount, newAccount);
        }
    }


    private void setInCollectionDate(final Context ctx, final Account oldAccount, final Account newAccount)
        throws HomeException
    {
        if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.IN_COLLECTION))
        {
            newAccount.setInCollectionDate(new Date());
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Setting Account's InCollectionDate to " + new Date(), null).log(ctx);
            }
        }
    }


    // remove entries in AccountNote table for this account
    private void removeNotes(final Context ctx, final Account account)
    {
        final Home noteHome = (Home) ctx.get(Common.ACCOUNT_NOTE_HOME);
        final String ban = account.getBAN();        

        try
        {
            noteHome.where(ctx, new EQ(NoteXInfo.ID_IDENTIFIER,ban)).removeAll(ctx);
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(this, "Fail to remove Note for Account [BAN=" + account.getBAN() + "]", e).log(ctx);
        }
    }


    /**
     * This method creates a note entry when a new account is created. The Account must be
     * in a Terminated or suspended state in order for the store to occur on the
     * AccountNoteHome.
     *
     * @param newAccount -
     *            The new account bean
     */
    private void createAccountNote(final Context ctx, final Account newAccount)
    {
        AccountNoteHelper.createAccountNote(newAccount, ctx, this);
    }


    /**
     * This method creates a note entry when an account is updated. The content of the
     * note shows which values have been modified
     *
     * @param oldAccount -
     *            The old account data
     * @param newAccount -
     *            The new account data being stored
     */
    private void createAccountNote(final Context ctx, final Account oldAccount, final Account newAccount,
        final boolean crmResult)
    {
        AccountNoteHelper.createAccountNote(oldAccount, newAccount, crmResult, ctx, this);
    }
    
    public static final String IS_PARENT_ACCOUNT_UPDATING = "com.redknee.app.crm.account.ParentAccountUpdating";
}
