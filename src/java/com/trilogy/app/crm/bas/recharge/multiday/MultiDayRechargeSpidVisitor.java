package com.trilogy.app.crm.bas.recharge.multiday;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bas.recharge.AbstractRechargeVisitor;
import com.trilogy.app.crm.bas.recharge.ProcessAccountInfo;
import com.trilogy.app.crm.bas.recharge.RechargeErrorReportSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author kabhay
 *
 */
public class MultiDayRechargeSpidVisitor extends AbstractRechargeVisitor
{

    public MultiDayRechargeSpidVisitor(final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingCycle, final Visitor accountVisitor, final boolean recurringRecharge, final boolean proRated, final boolean smsNotificationOnly)
        {
            super(billingDate, agentName, chargingCycle, recurringRecharge, proRated, smsNotificationOnly);
            this.accountVisitor_ = accountVisitor;
        }

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Visitor accountVisitor_;

	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException {
        final CRMSpid spid = (CRMSpid) obj;
        Context subContext = ctx.createSubContext();
        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Applying recur charges for spid "
                    + spid.getId());
            }

            final SubscriberTypeEnum applicableSubscriberType = SpidSupport.getMultiDayRecurringChargeApplicableSub(ctx, spid.getSpid());
            subContext.put(CRMSpid.class, spid);
            
            double rate = 1.0;
            
            if(!SafetyUtil.safeEquals(getChargingCycle(), ChargingCycleEnum.MULTIDAY))
            {
            	throw new IllegalArgumentException("Only Multi-Day recurring recharge is supported");
            }
            
//            subContext.put(RecurringRechargeSupport.RECURRING_RECHARGE_START_DATE, startDate);
//            subContext.put(RecurringRechargeSupport.RECURRING_RECHARGE_END_DATE, endDate);
            
            // Adding rate to the context.
            subContext.put(RecurringRechargeSupport.PRORATED_RATE, Double.valueOf(rate));

            MSP.setBeanSpid(subContext, spid.getId());
            
            final ProcessAccountInfo info = new ProcessAccountInfo(getBillingDate(), applicableSubscriberType);
            subContext.put(ProcessAccountInfo.class, info);
            
            List <String> eligibleBANs = new ArrayList <String>();
            eligibleBANs = (List<String>) AccountSupport.getMultidayRecurringChargeAndNotificationEligibleBANsList(subContext, spid,  applicableSubscriberType);
            
            if(!eligibleBANs.isEmpty())
            {
            	Iterator <String> i = eligibleBANs.listIterator();
            	while(i.hasNext())
            	{
            		String ban = i.next();
            		Account account = HomeSupportHelper.get(subContext).findBean(subContext, Account.class, new EQ(AccountXInfo.BAN, ban));
            		
            		accountVisitor_.visit(subContext, account);
            	}
            }
            else
            {
            	LogSupport.info(subContext, this, "No eligible accounts found to apply MultiDay Recurring Charge for Service Provider "+spid.getId());
            }
        }
        catch (final Exception e)
        {
            LogSupport.minor(subContext, this, "Error when select account with spid "
                + spid.getId(), e);
            handleException(subContext, "Error when select account with spid " + spid.getId());
        }
    }

    private void handleException(final Context ctx, final String reason)
    {
        try
        {
            RechargeErrorReportSupport.createReport(ctx, getAgentName(), null, RECHARGE_FAIL_XHOME,
                OCG_RESULT_UNKNOWN, reason, SYSTEM_LEVEL_ERROR_DUMMY_CHARGED_ITEM_ID, "", null, -1, "", "", this
                    .getBillingDate(), ChargedItemTypeEnum.UNKNOWN);
        }
        catch (final HomeException e)
        {
            LogSupport.minor(ctx, this, "fail to create error report for transaction ", e);
        }
    }

    
}
