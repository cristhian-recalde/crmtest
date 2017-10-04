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
package com.trilogy.app.crm.account.state.agent;

import java.util.Arrays;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.filter.NotPredicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.account.filter.ResponsibleAccountPredicate;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.home.account.AccountHierachyValidator;
import com.trilogy.app.crm.state.InOneOfStatesPredicate;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;

/**
 * Propagates account state changes into sub accounts, including response and
 * non-response.
 *
 * @author joe.chen@redknee.com
 */
public class AccountStateSubAccountStateAgent extends AccountStateAgentHome
{

    /**
     * @param ctx the operating context
     * @param delegate home decorator to delegate calls to
     */
    public AccountStateSubAccountStateAgent(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }

    /*
     * (non-Javadoc)
     * @see com.redknee.app.crm.account.state.agent.AccountStateAgentHome#onStateChange(com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.Account, com.redknee.app.crm.bean.Account)
     */
    @Override
    public void onStateChange(final Context ctx, final Account oldAccount, final Account newAccount)
        throws HomeException
    {
        if (!oldAccount.isResponsible())
        {
            // nothing was done if the account is not responsible, so just return
            return;
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "State change from " + oldAccount.getState() + " to " + newAccount.getState(), null).log(ctx);
        }

        Collection<Account> subAccounts = AccountSupport.getNonResponsibleAccounts(ctx, oldAccount);
        // the following will exclude the parent Account from the list, since it is responsible
        subAccounts = CollectionSupportHelper.get(ctx).findAll(ctx, subAccounts, new ResponsibleAccountPredicate(false));

        if (subAccounts.size() == 0)
        {
            // no sub accounts left in the list, no need to stick arround
            return;
        }

        Collection<AccountStateEnum> dunnedStates = Arrays.asList(
                AccountStateEnum.IN_ARREARS,
                AccountStateEnum.NON_PAYMENT_WARN,
                AccountStateEnum.IN_COLLECTION,
                AccountStateEnum.PROMISE_TO_PAY,
                AccountStateEnum.NON_PAYMENT_SUSPENDED
        );

        if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.INACTIVE))
        {
            subAccounts = CollectionSupportHelper.get(ctx).findAll(ctx, subAccounts,
                    new NotPredicate(
                            new InOneOfStatesPredicate(
                                    AccountStateEnum.INACTIVE)));
        }
        else if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.SUSPENDED))
        {
            
            subAccounts = CollectionSupportHelper.get(ctx).findAll(
                    ctx, subAccounts, 
                    new InOneOfStatesPredicate(
                            AccountStateEnum.IN_ARREARS,
                            AccountStateEnum.NON_PAYMENT_WARN,
                            AccountStateEnum.PROMISE_TO_PAY,
                            AccountStateEnum.NON_PAYMENT_SUSPENDED,
                            AccountStateEnum.ACTIVE));
        }
        else if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, dunnedStates))
        {
            
            And predicate = new And();
            predicate.add(new InOneOfStatesPredicate(
                    AccountStateEnum.IN_ARREARS,
                    AccountStateEnum.NON_PAYMENT_WARN,
                    AccountStateEnum.PROMISE_TO_PAY,
                    AccountStateEnum.NON_PAYMENT_SUSPENDED,
                    AccountStateEnum.ACTIVE));
            
            if ( newAccount.getSystemType().equals(SubscriberTypeEnum.HYBRID))
            {
                predicate.add(new NEQ(AccountXInfo.SYSTEM_TYPE, SubscriberTypeEnum.PREPAID));
                
            }
            
            subAccounts = CollectionSupportHelper.get(ctx).findAll(ctx, subAccounts, predicate);
            
        }
        else if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.ACTIVE))
        {
            if (EnumStateSupportHelper.get(ctx).isOneOfStates(oldAccount, dunnedStates))
            {
                subAccounts = CollectionSupportHelper.get(ctx).findAll(ctx, subAccounts, new InOneOfStatesPredicate(dunnedStates));
            }
            else if (EnumStateSupportHelper.get(ctx).stateEquals(oldAccount, AccountStateEnum.SUSPENDED))
            {
                subAccounts = CollectionSupportHelper.get(ctx).findAll(
                        ctx, subAccounts, 
                        new InOneOfStatesPredicate(
                                AccountStateEnum.SUSPENDED,
                                AccountStateEnum.IN_ARREARS,
                                AccountStateEnum.NON_PAYMENT_WARN,
                                AccountStateEnum.IN_COLLECTION,
                                AccountStateEnum.PROMISE_TO_PAY,
                                AccountStateEnum.NON_PAYMENT_SUSPENDED));
            }
        }

        if (subAccounts.size() > 0)
        {
            try
            {
                final Context subCtx = ctx.createSubContext();
                subCtx.put(AccountHierachyValidator.BYPASS_CHANGE_CHILD_ACCOUNT_STATE_VALIDATION, Boolean.TRUE);
                
                // Update sub-accounts states
                Visitors.forEach(subCtx, subAccounts, new UpdateAccountStateVisitor(newAccount));
            }
            catch (AgentException e)
            {
                throw new HomeException(e);
            }
        }
    }
}

class UpdateAccountStateVisitor implements Visitor
{
    private Account srcAccount_;

    public UpdateAccountStateVisitor(final Account source)
    {
        this.srcAccount_ = source;
    }

    public void visit(final Context ctx, final Object obj)
    {
        final Account subAccount = (Account) obj;

        try
        {
            if (!EnumStateSupportHelper.get(ctx).stateEquals(subAccount, srcAccount_))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this,
                            "Changing acccount " + subAccount.getBAN()
                            + " state from " + subAccount.getState() + " to " + srcAccount_.getState()
                            + "(same state as account " + srcAccount_.getBAN() + ")", null).log(ctx);
                }
                
                final Home home = (Home) ctx.get(AccountHome.class);
                subAccount.setState(srcAccount_.getState());
                                
                if (AccountStateEnum.PROMISE_TO_PAY.equals(srcAccount_.getState()))
                {
                    subAccount.setPromiseToPayDate(srcAccount_.getPromiseToPayDate());
                }
                home.store(ctx, subAccount);
            }
        }
        catch (HomeException e)
        {

            new MinorLogMsg(this, "sub account state change failed. " + e + "parent account="
                    + srcAccount_.getBAN() + ", this account=" + subAccount.getBAN(), e).log(ctx);
            final ExceptionListener el = (ExceptionListener) ctx.get(HTMLExceptionListener.class);
            if (el != null)
            {
                el.thrown(e);
            }
        }
    }
}
