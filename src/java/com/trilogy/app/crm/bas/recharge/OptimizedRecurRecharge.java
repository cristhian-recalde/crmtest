/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.bas.recharge;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.xdb.AbstractJDBCXDB;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.EntryLogSupport;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Apply Weekly service/annual licenses fees to active subscribers on their billing cycle date.
 * Optimised for the scenario that only small portion of subscribers have weekly service/aux service
 *
 * @author rchen
 *
 */
public final class OptimizedRecurRecharge extends ContextAwareSupport implements RechargeConstants
{
    private final EntryLogSupport logger_;
    private final Date billingDate_;
    private final String agentName_;
    private final LifecycleAgentSupport lifecycleAgent_;
    private final int spid_;
    private final int billCycleId_;
    
    public OptimizedRecurRecharge(final Context ctx, final Date billingDate, ChargingCycleEnum chargingCycle, String agentName)
    {
        this(ctx, billingDate, chargingCycle, agentName, -1, -1, null);
    }

    public OptimizedRecurRecharge(final Context ctx, final Date billingDate, ChargingCycleEnum chargingCycle, String agentName, LifecycleAgentSupport lifecycleAgent)
    {
        this(ctx, billingDate, chargingCycle, agentName, -1, -1, lifecycleAgent);
    }

    public OptimizedRecurRecharge(final Context ctx, final Date billingDate, ChargingCycleEnum chargingCycle, String agentName, int spid, int billCycleId)
    {
        this(ctx, billingDate, chargingCycle, agentName, spid, billCycleId, null);
    }

    public OptimizedRecurRecharge(final Context ctx, final Date billingDate, ChargingCycleEnum chargingCycle, String agentName, int spid, int billCycleId, LifecycleAgentSupport lifecycleAgent)
    {
        setContext(ctx);
        logger_ = new EntryLogSupport(ctx, getClass().getName());
        billingDate_ = billingDate;
        agentName_ = agentName;
        chargingCycle_ = chargingCycle;
        lifecycleAgent_ = lifecycleAgent;
        billCycleId_ = billCycleId;
        spid_ = spid;
    }

    public void execute()
    {
        Context ctx = getContext();
        Collection<CRMSpid> spids;
        if (ChargingCycleEnum.WEEKLY.equals(chargingCycle_))
        {
            final int currentDayOfWeek = BillCycleSupport.computeBillingDayOfWeek(getBillingDate());
            spids = getSpidsWithCurrentDayOfWeek(currentDayOfWeek);
        }
        else
        {
            spids = getAllSpids();
        }

        if (spids!=null && spids.size()>0)
        {
            String sql = retrieveSqlQuery(ctx, spids);

            ChargingCycleHandler handler = ChargingCycleSupportHelper.get(ctx).getHandler(chargingCycle_);
            
            final Date startDate = billingDate_;
            final Date endDate = handler.calculateCycleEndDate(ctx, billingDate_, -1, spids.iterator().next().getId());
            
            RechargeSubscriberVisitor delegate = 
                new RechargeSubscriberVisitor(getBillingDate(), getAgentName(), chargingCycle_, startDate, endDate, true, false, false);
            
            final ProcessSubscriberThreadPoolVisitor threadPoolVisitor = new ProcessSubscriberThreadPoolVisitor(ctx,
                    getThreadPoolSize(ctx), getThreadPoolQueueSize(ctx), delegate, lifecycleAgent_);
            
            try
            {
            	/**
            	 * Changing xdb.forEach with Visitors.foreach so that db connection can be released immediately avoiding bottleneck in DB.
            	 */
            	List<String> applicableSubIDs = getAllApplicableSubscriberIDs(ctx, sql);
                Visitors.forEach(ctx, applicableSubIDs, threadPoolVisitor);
            }
            catch (final AbortVisitException e)
            {
                throw e;
            }
            catch (final Throwable t)
            {
                LogSupport.crit(ctx, this, "Fail to apply optimized " + chargingCycle_ + " recurring recharges for billing date " + getBillingDate(), t);
                try
                {
                    RechargeErrorReportSupport.createReport(ctx, agentName_, null, RECHARGE_FAIL_XHOME, OCG_RESULT_UNKNOWN,
                        "Fail to apply optimized " + chargingCycle_ + " recurring recharges for billing date " + getBillingDate(), SYSTEM_LEVEL_ERROR_DUMMY_CHARGED_ITEM_ID, "", null, -1, "", "", this.getBillingDate(),
                        ChargedItemTypeEnum.UNKNOWN);
                }
                catch (final HomeException e)
                {
                    LogSupport.minor(ctx, this, "Fail to create error report for optimized " + chargingCycle_ + " recurring recharges ", e);
                }
            }
            finally
            {
                try
                {
                    threadPoolVisitor.getPool().shutdown();
                    threadPoolVisitor.getPool().awaitTerminationAfterShutdown(TIME_OUT_FOR_SHUTTING_DOWN);
                }
                catch (final Exception e)
                {
                    LogSupport.minor(ctx, this, "Exception catched during wait for completion of all recharge thread", e);
                }
    
                ERLogger.generateRechargeCountEr(ctx, delegate);
            }     
        }
    }
    
    private Collection<CRMSpid> getAllSpids()
    {
        Collection<CRMSpid> result = null;
        Context ctx = getContext();
        Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        
        if (spidHome != null)
        {
            try
            {
                if (spid_!=-1)
                {
                    spidHome = spidHome.where(ctx, new EQ(CRMSpidXInfo.ID, Integer.valueOf(spid_)));
                }

                result = spidHome.selectAll(ctx);
            }
            catch (HomeException e)
            {
                logger_.major("Failed to fetch Spids: " + e.getMessage(), e);
            }
        }
        else
        {
            logger_.major("Failed to fetch Spids: Spid home not found in context.");
        }

        return result;
    }
    
    private Collection<CRMSpid> getSpidsWithCurrentDayOfWeek(final int currentDayOfWeek)
    {
        Collection<CRMSpid> spidWithCurrentDayofWeek = null;
        Context subCtx = getContext().createSubContext();
        
        try
        {
            final WeekDayEnum nameOfDay = getNameofDay(currentDayOfWeek);
            
            if (spid_!=-1)
            {
                Home spidHome = (Home) subCtx.get(CRMSpidHome.class);
                spidHome = spidHome.where(subCtx, new EQ(CRMSpidXInfo.ID, Integer.valueOf(spid_)));
                subCtx.put(CRMSpidHome.class, spidHome);
            }

            spidWithCurrentDayofWeek = SpidSupport.selectByRecurDayOfWeek(subCtx, nameOfDay);
            
            if (spidWithCurrentDayofWeek != null && spidWithCurrentDayofWeek.size() > 0)
            {
                logger_.info("Total# of SPID(s) configured with Recurring Day Of Week = " + nameOfDay.toString() + " are "
                    + spidWithCurrentDayofWeek.size());
            }
            else
            {
                logger_.info("No Spid(s) configured with Recurring Day Of Week = " + nameOfDay.toString());
            }
        }
        catch (final HomeException he)
        {
            logger_.major("Failed to fetch Spid(s) having current Day of Week");
        }

        return spidWithCurrentDayofWeek;
    }
    

    
    private String retrieveSqlQuery(Context ctx, Collection<CRMSpid> spids)
    {
        SysFeatureCfg sysCfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT S.ID FROM SUBSCRIBER S ");
        
        if (billCycleId_!=-1)
        {
            sql.append("INNER JOIN ACCOUNT A ");
            sql.append("ON S.BAN=A.BAN ");
        }
        
        sql.append("LEFT OUTER JOIN SUBSCRIBERAUXILIARYSERVICE SAS ");
        sql.append("ON SAS.SUBSCRIBERIDENTIFIER = S.ID ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("(SELECT SUBSCRIBERID,SERVICEID, SERVICEPERIOD ");
        sql.append("from SUBSCRIBERSERVICES where (1=0");
        
        Iterator<ServicePeriodEnum> servicePeriodEnumCollectionIterator = ServicePeriodEnum.COLLECTION.iterator();
        Set<ServicePeriodEnum> servicePeriodSet = new HashSet<ServicePeriodEnum>();
        
        while (servicePeriodEnumCollectionIterator.hasNext())
        {
            ServicePeriodEnum servicePeriod = servicePeriodEnumCollectionIterator.next();
            if (servicePeriod.getChargingCycle().equals(chargingCycle_))
            {
                servicePeriodSet.add(servicePeriod);
                sql.append(" OR SERVICEPERIOD=");
                sql.append(servicePeriod.getIndex());
            }
        }
        
        sql.append(")) SS ");
        sql.append("ON SS.SUBSCRIBERID = S.ID ");
        sql.append(" LEFT OUTER JOIN SERVICE SVC ");
        sql.append("ON SVC.ID=SS.SERVICEID ");
        sql.append("LEFT OUTER JOIN AUXILIARYSERVICE AUX ");
        sql.append("ON SAS.AUXILIARYSERVICEIDENTIFIER=AUX.IDENTIFIER AND (1=0");
        
        for (ServicePeriodEnum servicePeriod : servicePeriodSet)
        {
            sql.append(" OR AUX.CHARGINGMODETYPE=");
            sql.append(servicePeriod.getIndex());
        }

        sql.append(")");
        sql.append(" WHERE ");
        
        if (billCycleId_!=-1)
        {
            sql.append("A.BILLCYCLEID=");
            sql.append(billCycleId_);
            sql.append(" AND ");
        }
        
        sql.append("(SS.SUBSCRIBERID IS NOT NULL OR ");
        sql.append("(SAS.SUBSCRIBERIDENTIFIER IS NOT NULL AND AUX.IDENTIFIER IS NOT NULL");
        
        // Dropping 0 amount transactions for aux services
        if (sysCfg.isDropZeroAmountTransaction())
        {
            sql.append(" AND (S.SUBSCRIBERTYPE=");
            sql.append(SubscriberTypeEnum.POSTPAID_INDEX);
            sql.append(" OR AUX.CHARGE<>0)");
        }
        sql.append(")) AND (1=0");
        for (CRMSpid spid : spids)
        {
            // Adding spid filter
            sql.append(" OR (S.SPID=");
            sql.append(spid.getSpid());
            
            // Adding postpaid or prepaid filter per spid
            if (ChargingCycleEnum.WEEKLY.equals(chargingCycle_))
            {
                if (spid.getRecurChargeSubscriberType().equals(SubscriberTypeRecurringChargeEnum.POSTPAID))
                {
                    sql.append(" AND S.SUBSCRIBERTYPE=");
                    sql.append(SubscriberTypeEnum.POSTPAID_INDEX);
                }
                else if (spid.getRecurChargeSubscriberType().equals(SubscriberTypeRecurringChargeEnum.PREPAID))
                {
                    sql.append(" AND S.SUBSCRIBERTYPE=");
                    sql.append(SubscriberTypeEnum.PREPAID_INDEX);
                }
            }
            else
            {
                if (spid.getRecurChargeSubscriberType().equals(SubscriberTypeRecurringChargeEnum.POSTPAID))
                {
                    sql.append(" AND S.SUBSCRIBERTYPE=");
                    sql.append(SubscriberTypeEnum.POSTPAID_INDEX);
                }
                else if (spid.getRecurChargeSubscriberType().equals(SubscriberTypeRecurringChargeEnum.PREPAID))
                {
                    sql.append(" AND S.SUBSCRIBERTYPE=");
                    sql.append(SubscriberTypeEnum.PREPAID_INDEX);
                }
            }
            sql.append(")");
        }
        sql.append(")");
        
        // Adding states filter
        sql.append(" AND (1=0");
        for (int state : RechargeConstants.RECHARGE_SUBSCRIBER_STATES)
        {
            sql.append(" OR S.STATE=");
            sql.append(state);
        }
        
        sql.append(" OR ((1=0");
        for (CRMSpid spid : spids)
        {
            if (spid.isApplyRecurringChargeForSuspendedSubscribers())
            {
                sql.append(" OR S.SPID=");
                sql.append(spid.getSpid());
            }
        }
        sql.append(") AND (1=0");

        for (int state : RechargeConstants.RECHARGE_SUBSCRIBER_STATES_SUSPENDED)
        {
            sql.append(" OR S.STATE=");
            sql.append(state);
        }
        sql.append(")"); 

        // TODO: Add query to filter only charge on suspended services/auxiliary services
        sql.append(" AND ((AUX.CHARGEABLEWHILESUSPENDED IS NOT NULL AND AUX.CHARGEABLEWHILESUSPENDED = 'y') OR (SVC.CHARGEABLEWHILESUSPENDED IS NOT NULL AND SVC.CHARGEABLEWHILESUSPENDED = 'y'))");
        
        sql.append(")"); 

        sql.append(")"); // Closign OR for states
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "SQL: " + sql);
        }
        return sql.toString();
    }

 

    private WeekDayEnum getNameofDay(final int numberedDayOfWeek)
    {
        return (WeekDayEnum) WeekDayEnum.COLLECTION.getByIndex((short) numberedDayOfWeek);
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
    
    private List<String> getAllApplicableSubscriberIDs(final Context ctx, final String sqlQuery) throws HomeException {
    	
    	final XDB xdb = (XDB) ctx.get(XDB.class);
    	final List <String> subscriberList = new ArrayList<String>();
    	
    	Context subContext = ctx.createSubContext();
    	//subContext.put(AbstractJDBCXDB.FETCH_SIZE_KEY, 10000);
    	long startTime = 0;
    	
    	if (LogSupport.isDebugEnabled(subContext)) {
    		startTime = System.currentTimeMillis();
    	}
    	
    	try
    	{
	    	xdb.forEach(subContext, 
	    			new Visitor()
			    	{
						private static final long serialVersionUID = 1L;
			
						public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException 
						{
							try 
							{
								subscriberList.add(((XResultSet) obj).getString(1));
							} 
							catch (SQLException e) 
							{
								throw new AgentException(e);
							}
						}
			    	}
	    			, sqlQuery);
    	}
    	catch(Exception ex)
    	{
    		throw new HomeException(ex);
    	}
    	
    	if (LogSupport.isDebugEnabled(subContext)) {
    		LogSupport.debug(ctx, MODULE, "Finished Fetching all the subscribers applicable for daily recurring charges: time :: " + (System.currentTimeMillis() - startTime));
    	}
    	return subscriberList;
    }

    public static final long TIME_OUT_FOR_SHUTTING_DOWN = 60 * 1000;
    
    private ChargingCycleEnum chargingCycle_;
    private static final String MODULE = OptimizedRecurRecharge.class.getName();
}
