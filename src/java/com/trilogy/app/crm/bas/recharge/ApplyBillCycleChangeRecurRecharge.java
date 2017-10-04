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
package com.trilogy.app.crm.bas.recharge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.BillCycleChangeChargingCycleSupport;
import com.trilogy.app.crm.support.BillCycleChangeServicePeriodSupport;
import com.trilogy.app.crm.support.ChargingCycleSupport;
import com.trilogy.app.crm.support.ServicePeriodSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SupportHelper;


/**
 * This agent runs recurring charges for all accounts in the hierarchy.  The result will be that
 * charges will be run from the end of the old bill cycle date to the start of the new bill cycle date.
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class ApplyBillCycleChangeRecurRecharge implements ContextAgent, RechargeConstants
{
    protected final BillCycleHistory history_;
    protected final Account account_;
    protected final Date billingDate_;
    protected final String agentName_;

    public ApplyBillCycleChangeRecurRecharge(final Account rootAccount, final BillCycleHistory history, final String agentName, final Date billingDate)
    {
        this.billingDate_ = billingDate;
        this.agentName_ = agentName;
        this.account_ = rootAccount;
        this.history_ = history;
    }

    public void execute(final Context ctx) throws AgentException
    {
        try
        {   
            // For bill cycle changes, we only want to execute recurring charges for a single account hierarchy.
            // This means that in order to re-use existing recurring charge logic we must hide the other SPIDs, bill cycles, and accounts.
            Context sCtx = ctx.createSubContext();
            sCtx.put(CommonTime.RUNNING_DATE, billingDate_);
            
            MSP.setBeanSpid(sCtx, account_.getSpid());
            
            sCtx.put(ProcessAccountInfo.class, new ProcessAccountInfo(billingDate_, null));
            final CRMSpid spid = SpidSupport.getCRMSpid(sCtx, account_.getSpid());
            
            SupportHelper.register(sCtx, ChargingCycleSupport.class, BillCycleChangeChargingCycleSupport.instance());
            SupportHelper.register(sCtx, ServicePeriodSupport.class, BillCycleChangeServicePeriodSupport.instance());
            
            And filter = new And();
            filter.add(new In(AccountXInfo.STATE, RechargeBillCycleVisitor.getAccountRechargeStateSet(spid.isApplyRecurringChargeForSuspendedSubscribers())));
            Collection<Account> descendents = AccountSupport.getTopologyEx(sCtx, account_, filter, null, true, true, null, false);
            Visitor v = new RechargeAccountVisitor(billingDate_, agentName_, ChargingCycleEnum.MONTHLY, true, true, false);
            
            Collection<String> failedBANs = new ArrayList<String>();
            for (Account descendent : descendents)
            {
                try
                {
                    v.visit(sCtx, descendent);
                }
                catch (Exception e)
                {
                    new MinorLogMsg(this, "Error performing recurring charges for bill cycle change for account " + descendent.getBAN() + ".", e).log(ctx);
                    failedBANs.add(descendent.getBAN());
                }
            }
            
            if (failedBANs.size() > 0)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed performing recurring charges for account(s) [");
                for (String failedBAN : failedBANs)
                {
                    sb.append(failedBAN).append(", ");
                }
                sb.append("] during bill cycle move for account " + account_.getBAN());
                throw new AgentException(sb.toString());
            }
        }
        catch (HomeException e)
        {
            throw new AgentException(e);
        }
    }
}
