package com.trilogy.app.crm.bas.recharge.multiday;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.bas.recharge.ProcessAccountInfo;
import com.trilogy.app.crm.bas.recharge.ProcessAccountThreadPoolVisitor;
import com.trilogy.app.crm.bas.recharge.ProcessSubscriberThreadPoolVisitor;
import com.trilogy.app.crm.bas.recharge.RechargeAccountVisitor;
import com.trilogy.app.crm.bas.recharge.RechargeConstants;
import com.trilogy.app.crm.bas.recharge.RechargeErrorReportSupport;
import com.trilogy.app.crm.bas.recharge.RechargeSpidVisitor;
import com.trilogy.app.crm.bas.recharge.RechargeSubscriberVisitor;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeRecurringChargeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.WeekDayEnum;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xlog.log.EntryLogSupport;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author kabhay
 *
 */
public class MultiDayRecurRecharge extends ContextAwareSupport implements RechargeConstants
{
    private final EntryLogSupport logger_;
    private final Date billingDate_;
    private final String agentName_;
    private final LifecycleAgentSupport lifecycleAgent_;
    
    
    private ChargingCycleEnum chargingCycle_;
    
    public MultiDayRecurRecharge(final Context ctx, final Date billingDate, ChargingCycleEnum chargingCycle, String agentName, LifecycleAgentSupport lifecycleAgent)
    {
        setContext(ctx);
        logger_ = new EntryLogSupport(ctx, getClass().getName());
        billingDate_ = billingDate;
        agentName_ = agentName;
        chargingCycle_ = chargingCycle;
        lifecycleAgent_ = lifecycleAgent;

    }
    
    public void execute()
    {
    	
    	try
    	{
    		
            final RechargeAccountVisitor delegate = new RechargeAccountVisitor(getBillingDate(), agentName_, chargingCycle_,
                    true, isProrate(), false);
            final ProcessAccountThreadPoolVisitor threadPoolVisitor = new ProcessAccountThreadPoolVisitor(getContext(),
                    getAccountVisitorThreadPoolSize(getContext()), getAccountVisitorThreadPoolQueueSize(getContext()), delegate, lifecycleAgent_);
          
          final MultiDayRechargeSpidVisitor spidVisitor = new MultiDayRechargeSpidVisitor(getBillingDate(), agentName_, chargingCycle_,
                  threadPoolVisitor, true, false, false);
            
          final Home home = (Home) getContext().get(CRMSpidHome.class);
          home.forEach(getContext(), spidVisitor);
            
            
            
    	}catch (Throwable th) 
    	{
		
    			LogSupport.major(getContext(), this, "Exception occurred while applying Multi-Day recurring recharge !!",th);
    	}
    }
    
    int getAccountVisitorThreadPoolSize(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getRecurringChargeThreads();
    }
    
    int getAccountVisitorThreadPoolQueueSize(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getRecurringChargeQueueSize();
    }
    
    private boolean isProrate() {
		return false;
	}

	


    private Date getBillingDate()
    {
        return billingDate_;
    }
    
    private String getAgentName()
    {
        return agentName_;
    }

    private int getThreadPoolSize(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getRecurringChargeThreads();
    }

    /**
     * Get queue size from configuration.
     * 
     * @param ctx
     *            The operating context.
     * @return The recurring charge thread pool queue size.
     */
    private int getThreadPoolQueueSize(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getRecurringChargeQueueSize();
    }

    public static final long TIME_OUT_FOR_SHUTTING_DOWN = 60 * 1000;

    
    
}
