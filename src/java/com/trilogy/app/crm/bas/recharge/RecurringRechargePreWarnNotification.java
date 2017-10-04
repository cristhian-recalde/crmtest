package com.trilogy.app.crm.bas.recharge;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeRecurringChargeEnum;
import com.trilogy.app.crm.bean.WeekDayEnum;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.EntryLogSupport;
import com.trilogy.framework.xlog.log.LogSupport;


public class RecurringRechargePreWarnNotification extends ContextAwareSupport implements RechargeConstants
{
    private final EntryLogSupport logger_;
    private final Date runningDate_;
    private final ChargingCycleEnum chargingCycle_;
    private final String agentName_;
    private final LifecycleAgentSupport agent_;
    
    public RecurringRechargePreWarnNotification(final Context ctx, final Date runningDate, ChargingCycleEnum chargingCycle, String agentName, final LifecycleAgentSupport agent )
    {
        setContext(ctx);
        logger_ = new EntryLogSupport(ctx, getClass().getName());
        runningDate_ = runningDate;
        chargingCycle_ = chargingCycle;
        agentName_ = agentName;
        agent_ = agent;        
    }
    
    
    
    public RecurringRechargePreWarnNotification(final Context ctx, final Date runningDate, String agentName, final LifecycleAgentSupport agent)
    {
        this(ctx, runningDate, null, agentName, agent);
    }

    public void execute()
    {
        
        if (ChargingCycleEnum.MULTIDAY.equals(getChargingCycle()))
        {
            executeMultiDayChargesNotification();
        }
        else if (ChargingCycleEnum.MONTHLY.equals(getChargingCycle()))
        {
            executeMonthlyChargesNotification();
        }
        else if (ChargingCycleEnum.WEEKLY.equals(getChargingCycle()))
        {
            executeChargesNotification(getChargingCycle());
        }
        else if (getChargingCycle()==null)
        {
            executeMultiDayChargesNotification();
            executeMonthlyChargesNotification();
            executeChargesNotification(ChargingCycleEnum.WEEKLY);
        }
        else
        {
            throw new IllegalArgumentException("Only multi-day, monthly and weekly recurring recharges are supported for pre-warning notifications.");
        }
    }
    
    
    private void executeMultiDayChargesNotification()
    {
        Context subCtx = getContext().createSubContext();
        LogSupport.info(subCtx, this, "BEGIN - executeMultiDayChargesNotification.");
        try
        {
            final Home spidHome = (Home) subCtx.get(CRMSpidHome.class);
            final And spidFilter = new And();
            spidFilter.add(new EQ(CRMSpidXInfo.RECURRING_CHARGE_PREPAID_NOTIFICATION, Boolean.TRUE));
            spidFilter.add(new NEQ(CRMSpidXInfo.RECURRING_CHARGE_SUB_TYPE, SubscriberTypeRecurringChargeEnum.POSTPAID));
            
            Collection<CRMSpid> spids = spidHome.select(subCtx, spidFilter);
            for (CRMSpid spid: spids)
            {
                int noticePeriod = 0;
                
                if(subCtx.getBoolean(RecurringRechargeSupport.INSUFFICIENT_BALANCE_NOTIFICATION))
                {
                	noticePeriod = spid.getRecChrgPrepdInsufBalNotifDaysBefore();
                }
                else
                {
                	noticePeriod = spid.getRecurringChargePrepaidNotificationDaysBefore();
                }
                
                Calendar billingCalendar = Calendar.getInstance();
                billingCalendar.setTime(getRunningDate());
                billingCalendar.add(Calendar.DAY_OF_MONTH, noticePeriod);
                int billingDay = billingCalendar.get(Calendar.DAY_OF_MONTH);
                Date billingDate = CalendarSupportHelper.get(subCtx).getDateWithNoTimeOfDay(billingCalendar.getTime());
                
                ChargingCycleHandler handler = ChargingCycleSupportHelper.get(subCtx).getHandler(ChargingCycleEnum.MULTIDAY);
                subCtx.put(RecurringRechargeSupport.RECURRING_RECHARGE_START_DATE, 
                        handler.calculateCycleStartDate(subCtx, billingDate, billingDay, spid.getSpid()));
                subCtx.put(RecurringRechargeSupport.RECURRING_RECHARGE_END_DATE, 
                        handler.calculateCycleEndDate(subCtx, billingDate, billingDay, spid.getSpid()));

                final RechargeAccountVisitor delegate = new RechargeAccountVisitor(billingDate, agentName_, 
                        ChargingCycleEnum.MULTIDAY, true, false, true);
                
                final ProcessAccountThreadPoolVisitor accountThreadPoolVisitor = new ProcessAccountThreadPoolVisitor(subCtx,
                        getThreadPoolSize(subCtx), getThreadPoolQueueSize(subCtx), delegate, agent_);
                
                MSP.setBeanSpid(subCtx, spid.getId());
                
                SubscriberTypeEnum applicableSubType = SpidSupport.getSubscriberTypeEnum(spid.getRecurringChargeSubType());
                
                final ProcessAccountInfo info = new ProcessAccountInfo(billingDate, applicableSubType);
                subCtx.put(ProcessAccountInfo.class, info);
                
                try
                {
                    List <String> eligibleBANs = new ArrayList <String>();
                    eligibleBANs = (List<String>) AccountSupport.getMultidayRecurringChargeAndNotificationEligibleBANsList(subCtx, spid, applicableSubType);
                    
                    if(!eligibleBANs.isEmpty())
                    {
                    	Iterator <String> i = eligibleBANs.listIterator();
                    	while(i.hasNext())
                    	{
                    		String ban = i.next();
                    		Account account = HomeSupportHelper.get(subCtx).findBean(subCtx, Account.class, new EQ(AccountXInfo.BAN, ban));
                    		
                    		accountThreadPoolVisitor.visit(subCtx, account);
                    	}
                    }
                    else
                    {
                    	LogSupport.info(subCtx, this, "No eligible accounts found to send MultiDay Recurring Charges PreWarning Notification for Service Provider "+spid.getId());
                    }
                }
                catch (final Throwable t)
                {
                    String msg = "Error sending MultiDay recurring charges pre-warning notifications for SPID "  + spid.getId();
                    LogSupport.major(subCtx, this, msg + ": " + t.getMessage(), t);
                }
                finally
                {
                    try
                    {
                        accountThreadPoolVisitor.getPool().shutdown();
                        accountThreadPoolVisitor.getPool().awaitTerminationAfterShutdown(TIME_OUT_FOR_SHUTTING_DOWN);
                    }
                    catch (final Exception e)
                    {
                        String msg = "Exception during wait for completion of MultiDay recurring charges pre-warning notification thread: ";
                        LogSupport.major(subCtx, this, msg + e.getMessage(), e);
                    }
                }                    
            }
        }
        catch (final Throwable t)
        {
            String msg = "Error retrieving SPIDs while sending MultiDay recurring charges pre-warning notifications: ";
            LogSupport.major(subCtx, this, msg + t.getMessage(), t);
        }
        LogSupport.info(subCtx, this, "END - executeMultiDayChargesNotification.");
    }
    
    
    private void executeMonthlyChargesNotification()
    {
        Context context = getContext();
        LogSupport.info(context, this, "BEGIN - executeMonthlyChargesNotification.");
        try
        {
            final Home spidHome = (Home) context.get(CRMSpidHome.class);
            final Home billCycleHome = (Home) context.get(BillCycleHome.class);
            And spidFilter = new And();
            spidFilter.add(new EQ(CRMSpidXInfo.RECURRING_CHARGE_PREPAID_NOTIFICATION, Boolean.TRUE));
            spidFilter.add(new NEQ(CRMSpidXInfo.RECURRING_CHARGE_SUB_TYPE, SubscriberTypeRecurringChargeEnum.POSTPAID));
            Collection<CRMSpid> spids = spidHome.select(context, spidFilter);
            for (CRMSpid spid: spids)
            {
                Context subContext = context.createSubContext();
                int daysBeforeBillingCycle = spid.getRecurringChargePrepaidNotificationDaysBefore();
                Calendar billingCycleCalendar = Calendar.getInstance();
                billingCycleCalendar.setTime(getRunningDate());
                billingCycleCalendar.add(Calendar.DAY_OF_MONTH, daysBeforeBillingCycle);
                int billingCycleDay = billingCycleCalendar.get(Calendar.DAY_OF_MONTH);
                Date billingCycleDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(billingCycleCalendar.getTime());
                
                ChargingCycleHandler handler = ChargingCycleSupportHelper.get(context).getHandler(ChargingCycleEnum.MONTHLY);
                Date startDate = handler.calculateCycleStartDate(subContext, billingCycleDate, billingCycleDay, spid.getSpid());
                Date endDate = handler.calculateCycleEndDate(subContext, billingCycleDate, billingCycleDay, spid.getSpid());

                subContext.put(RecurringRechargeSupport.RECURRING_RECHARGE_START_DATE, startDate);
                subContext.put(RecurringRechargeSupport.RECURRING_RECHARGE_END_DATE, endDate);

                final RechargeAccountVisitor delegate = new RechargeAccountVisitor(billingCycleDate, agentName_, ChargingCycleEnum.MONTHLY,
                        true, false, true);
                final ProcessAccountThreadPoolVisitor threadPoolVisitor = new ProcessAccountThreadPoolVisitor(subContext,
                        getThreadPoolSize(subContext), getThreadPoolQueueSize(subContext), delegate, agent_);
                final RechargeBillCycleVisitor billCycleVisitor = new RechargeBillCycleVisitor(billingCycleDate, agentName_, ChargingCycleEnum.MONTHLY,
                        threadPoolVisitor, true, false, true);
                
                try
                {
                    And billCycleFilter = new And();
                    billCycleFilter.add(new EQ(BillCycleXInfo.SPID, Integer.valueOf(spid.getId())));
                    billCycleFilter.add(new EQ(BillCycleXInfo.DAY_OF_MONTH, Integer.valueOf(billingCycleDay)));
    
                    billCycleHome.where(subContext, billCycleFilter).forEach(subContext, billCycleVisitor);
                }
                catch (final Throwable t)
                {
                    LogSupport.minor(context, this, "Error sending monthly recurring recharges pre-warning notifications for SPID "  + spid.getId() + ": " + t.getMessage(), t);
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
                        LogSupport.minor(subContext, this,
                                "Exception catched during wait for completion of monthly recurring recharges pre-warning notification thread: "
                                        + e.getMessage(), e);
                    }
                }                    
            }
        }
        catch (final Throwable t)
        {
            LogSupport.minor(context, this, "Error retrieving SPIDs while sending monthly recurring recharges pre-warning notifications: " + t.getMessage(), t);
        }
        LogSupport.info(context, this, "END - executeMonthlyChargesNotification.");
    }
    
    private void executeChargesNotification(ChargingCycleEnum chargingCycle)
    {
        Context context = getContext();
        Collection<CRMSpid> spids = getSpidsWithPrepaidServicesPendingNotificationToday(context, chargingCycle);

        if (spids.size()>0)
        {
            for (CRMSpid spid: spids)
            {
                Context subContext = context.createSubContext();
                
                final XDB xdb=(XDB) subContext.get(XDB.class);
                String sql = retrieveWeeklySqlQuery(subContext, spid, chargingCycle);

                int daysBeforeBillingCycle = spid.getRecurringChargePrepaidNotificationDaysBefore();
                Calendar billingCycleCalendar = Calendar.getInstance();
                billingCycleCalendar.setTime(getRunningDate());
                billingCycleCalendar.add(Calendar.DAY_OF_MONTH, daysBeforeBillingCycle);
                int billingCycleDay = billingCycleCalendar.get(Calendar.DAY_OF_MONTH);
                Date billingCycleDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(billingCycleCalendar.getTime());
                
                ChargingCycleHandler handler = ChargingCycleSupportHelper.get(context).getHandler(chargingCycle);
                Date startDate = handler.calculateCycleStartDate(subContext, billingCycleDate, billingCycleDay, spid.getSpid());
                Date endDate = handler.calculateCycleEndDate(subContext, billingCycleDate, billingCycleDay, spid.getSpid());
                
                RechargeSubscriberVisitor delegate = 
                    new RechargeSubscriberVisitor(billingCycleDate, getAgentName(), chargingCycle, startDate, endDate, true, false, true);
                
                final ProcessSubscriberThreadPoolVisitor threadPoolVisitor = new ProcessSubscriberThreadPoolVisitor(subContext,
                        getThreadPoolSize(subContext), getThreadPoolQueueSize(subContext), delegate, agent_);
                
                if (ChargingCycleEnum.MONTHLY.equals(chargingCycle))
                {
                    subContext.put(RecurringRechargeSupport.RECURRING_RECHARGE_PRE_WARNING_NOTIFICATION_BILLING_CYCLE_DAY, Integer.valueOf(billingCycleDay));
                }
                
                try
                {
                	List<String> applicableSubIDs = getAllApplicableSubscriberIDs(subContext, sql);
                    Visitors.forEach(subContext, applicableSubIDs, threadPoolVisitor);
                }
                catch (final Throwable t)
                {
                    LogSupport.minor(subContext, this, "Error sending " + chargingCycle + " recurring recharges pre-warning notifications for SPID "  + spid.getId() + ": " + t.getMessage(), t);
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
                        LogSupport.minor(subContext, this,
                                "Exception catched during wait for completion of " + chargingCycle + " recurring recharges pre-warning notification thread: "
                                        + e.getMessage(), e);
                    }
                }    
            }
        }
    }
    
    private Collection<CRMSpid> getSpidsWithPrepaidServicesPendingNotificationToday(final Context ctx, final ChargingCycleEnum chargingCycle)
    {
        Collection<CRMSpid> spids = null;
        final int currentDayOfWeek = BillCycleSupport.computeBillingDayOfWeek(getRunningDate());
        
        try
        {
            final Home spidHome = (Home) ctx.get(CRMSpidHome.class);
            if (spidHome == null)
            {
                throw new HomeException("Failed to locate CRMSpidHome in the context.");
            }
            
            And spidFilter = new And();
            spidFilter.add(new EQ(CRMSpidXInfo.RECURRING_CHARGE_PREPAID_NOTIFICATION, Boolean.TRUE));
            
            if (ChargingCycleEnum.MONTHLY.equals(chargingCycle))
            {
                spidFilter.add(new NEQ(CRMSpidXInfo.RECURRING_CHARGE_SUB_TYPE, SubscriberTypeRecurringChargeEnum.POSTPAID));
            }
            else if (ChargingCycleEnum.WEEKLY.equals(chargingCycle))
            {
                spidFilter.add(new NEQ(CRMSpidXInfo.RECUR_CHARGE_SUBSCRIBER_TYPE, SubscriberTypeRecurringChargeEnum.POSTPAID));
            }
            else
            {
                throw new IllegalArgumentException("Only monthly and weekly recurring recharges are supported for pre-warning notifications.");
            }

            spidFilter.add(new Predicate(){
                        public boolean f(Context ctx, Object obj)
                        {
                            boolean result = false;

                            CRMSpid spid = (CRMSpid) obj;

                            int daysBeforeBillingDate = spid.getRecurringChargePrepaidNotificationDaysBefore();
                            
                            Calendar billingDayCalendar = Calendar.getInstance();
                            billingDayCalendar.setTime(getRunningDate());
                            billingDayCalendar.add(Calendar.DAY_OF_MONTH, daysBeforeBillingDate);
                            Date billingDayDate = billingDayCalendar.getTime();

                            
                            if (ChargingCycleEnum.WEEKLY.equals(chargingCycle))
                            {
                                WeekDayEnum spidBillingDay = spid.getWeeklyRecurChargingDay();
                                int chargingWeekDay = BillCycleSupport.computeBillingDayOfWeek(billingDayDate);
                                
                                if (getNameofDay(chargingWeekDay).equals(spidBillingDay))
                                {
                                    result = true;
                                }
                            }
                            else if (ChargingCycleEnum.MONTHLY.equals(chargingCycle))
                            {
                                int billingCycleDay = billingDayCalendar.get(Calendar.DAY_OF_MONTH);
                                Home billCycleHome = (Home) ctx.get(BillCycleHome.class);
                                
                                And billCycleFilter = new And();
                                billCycleFilter.add(new EQ(BillCycleXInfo.SPID, Integer.valueOf(spid.getId())));
                                billCycleFilter.add(new EQ(BillCycleXInfo.DAY_OF_MONTH, Integer.valueOf(billingCycleDay)));
                                try
                                {
                                    if (billCycleHome.select(ctx, billCycleFilter).size()>0)
                                    {
                                        result = true;
                                    }
                                }
                                catch (Throwable t)
                                {
                                    LogSupport.minor(ctx, this, "Unable to retrieve billing cycles for monthly recurring recharges pre-warning notifications for SPID "  + spid.getId() + ": " + t.getMessage(), t);
                                }
                            }
                            
                            return result;
                        }
                    });
        
            spids = spidHome.select(ctx, spidFilter);
            
            
            if (spids != null && spids.size() > 0)
            {
                logger_.info("Total# of " + chargingCycle + " Spid(s) needing recurring recharge pre-warning notification today  = "
                    + spids.size());
            }
            else
            {
                logger_.info("No " + chargingCycle + " Spid(s) needing recurring recharge pre-warning notification today");
            }
        }
        catch (final HomeException he)
        {
            logger_.minor("Failed to fetch " + chargingCycle + " Spid(s) needing recurring recharge pre-warning notification  today");
        }

        return spids;
    }
    
    private Collection<ServicePeriodEnum> getServicePeriods(ChargingCycleEnum chargingCycle)
    {
        Collection<ServicePeriodEnum> servicePeriods = new ArrayList<ServicePeriodEnum>();
        Iterator<ServicePeriodEnum> iter = ServicePeriodEnum.COLLECTION.iterator();
        while (iter.hasNext())
        {
            ServicePeriodEnum servicePeriod = iter.next();
            if (chargingCycle.equals(servicePeriod.getChargingCycle()))
            {
                servicePeriods.add(servicePeriod);
            }
        }
        return servicePeriods;
    }

    
    private String retrieveWeeklySqlQuery(Context ctx, CRMSpid spid, ChargingCycleEnum chargingCycle)
    {
        Collection<ServicePeriodEnum> servicePeriods = getServicePeriods(chargingCycle);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT S.ID FROM SUBSCRIBER S ");
        sql.append("LEFT OUTER JOIN SUBSCRIBERSERVICES SS ");
        sql.append("ON S.ID=SS.SUBSCRIBERID AND ( 1=0 ");
        for (ServicePeriodEnum servicePeriod : servicePeriods)
        {
            sql.append(" OR SS.SERVICEPERIOD=");
            sql.append(servicePeriod.getIndex());
        }
        sql.append(")");
        sql.append(" LEFT OUTER JOIN SUBSCRIBERAUXILIARYSERVICE SAS ");
        sql.append("ON S.ID=SAS.SUBSCRIBERIDENTIFIER ");
        sql.append("LEFT OUTER JOIN AUXILIARYSERVICE AUX ");
        sql.append("ON SAS.AUXILIARYSERVICEIDENTIFIER=AUX.IDENTIFIER AND (1=0 ");
        for (ServicePeriodEnum servicePeriod : servicePeriods)
        {
            sql.append(" OR AUX.CHARGINGMODETYPE=");
            sql.append(servicePeriod.getIndex());
        }
        
        sql.append(") WHERE (SS.SUBSCRIBERID IS NOT NULL OR ");
        sql.append("(SAS.SUBSCRIBERIDENTIFIER IS NOT NULL AND AUX.IDENTIFIER IS NOT NULL");

        // No need to go through 0 charge auxiliary services.
        sql.append(" AND AUX.CHARGE<>0");

        sql.append(")) AND S.SUBSCRIBERTYPE=");
        sql.append(SubscriberTypeEnum.PREPAID_INDEX);
        
        sql.append(" AND S.SPID=");
        sql.append(Integer.valueOf(spid.getId()));

        // Adding states filter
        sql.append(" AND (1=0");
        for (int state : RechargeConstants.RECHARGE_SUBSCRIBER_STATES)
        {
            sql.append(" OR S.STATE=");
            sql.append(state);
        }
        sql.append(")");
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "SQL: " + sql);
        }
        return sql.toString();
    }
    
private List<String> getAllApplicableSubscriberIDs(final Context ctx, final String sqlQuery) throws HomeException {
    	
    	final XDB xdb = (XDB) ctx.get(XDB.class);
    	final List <String> subscriberList = new ArrayList<String>();
    	
    	Context subContext = ctx.createSubContext();
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
    		LogSupport.debug(ctx, RecurringRechargePreWarnNotification.class.getName(), "Finished Fetching all the subscribers applicable for daily recurring charges: time :: " + (System.currentTimeMillis() - startTime));
    	}
    	return subscriberList;
    }

 

    private WeekDayEnum getNameofDay(final int numberedDayOfWeek)
    {
        return (WeekDayEnum) WeekDayEnum.COLLECTION.getByIndex((short) numberedDayOfWeek);
    }

    private Date getRunningDate()
    {
        return runningDate_;
    }
    
    private ChargingCycleEnum getChargingCycle()
    {
        return chargingCycle_;
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
