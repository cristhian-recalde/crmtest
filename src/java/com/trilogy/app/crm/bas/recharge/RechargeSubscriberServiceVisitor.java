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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ChargeFailureActionEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReason;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonHome;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonXInfo;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.service.MultiDayPeriodHandler;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.BalanceTransferSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQIC;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.snippet.log.Logger;


/**
 * Generates recurring recharge for all the services a subscriber has.
 *
 * @author larry.xia@redknee.com
 */
public class RechargeSubscriberServiceVisitor extends AbstractRechargeItemVisitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>RechargeSubscriberServiceVisitor</code>.
     *
     * @param billingDate
     *            Billing date.
     * @param agentName
     *            Name of the agent invoking this recurring charge.
     * @param servicePeriod
     *            Service period to charge.
     * @param subscriber
     *            The subscriber to be charged.
     * @param suspendOnFailure
     *            Whether to suspend on failure to charge.
     * @throws Exception 
     */
    public RechargeSubscriberServiceVisitor(Context ctx, final Date billingDate, final String agentName,
        final ChargingCycleEnum chargingPeriod, final Subscriber subscriber, final boolean suspendOnFailure,
        final Date startDate, final Date endDate, final double rate, final boolean preBilling, final boolean proRated, 
        final boolean preWarnNotificationOnly, final boolean allOrNothingChargingFirstPass, final boolean suspendAll) 
    {
        super(billingDate, agentName, chargingPeriod, startDate, endDate, rate, preBilling, proRated, preWarnNotificationOnly);
        setSub(subscriber);
        setSuspendOnFailure(suspendOnFailure);
        setAllOrNothingChargingFirstPass(allOrNothingChargingFirstPass);
        setSuspendAll(suspendAll);
        
        //setPricePlan(ctx, subscriber); // TODO : will remove in next drop 
    }

	public RechargeSubscriberServiceVisitor(final Context context, final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingPeriod, final Subscriber subscriber, final boolean suspendOnFailure,
            final double rate, final boolean preBilling, final boolean proRated, final boolean preWarnNotificationOnly) throws HomeException
    {
        super(context, billingDate, agentName, chargingPeriod, subscriber.getAccount(context).getBillCycleDay(context),
                subscriber.getSpid(), rate, preBilling, proRated, preWarnNotificationOnly);
        this.setSub(subscriber);
        this.setSuspendOnFailure(suspendOnFailure);
        setAllOrNothingChargingFirstPass(Boolean.FALSE);
        this.setSuspendAll(Boolean.FALSE);
    }

	private void setPricePlan(Context ctx, Subscriber subscriber) throws HomeException 
    {
		try
		{
			Home pricePlanHome = (Home) ctx.get(PricePlanHome.class);
			if(pricePlanHome != null)
			{
			
				PricePlan pp  = (PricePlan) pricePlanHome.find(ctx, subscriber.getPricePlan());
				if(pp  != null)
				{
					this.pricePlan_ = pp;
				}else
				{
				 throw new HomeException("Could not find price-plan for ID :" + subscriber.getPricePlan());	
				}
			}else
			{
				throw new HomeException("Could not find PricePlanHome in context!!");
			}
		}catch (HomeException e) 
		{
			throw e ;
		}
		
	}
	
	private PricePlan pricePlan_ = null;

    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj) throws AgentException
    {
        final Context subContext = ctx.createSubContext();
        final ServiceFee2 fee;
        if (obj instanceof FeeServicePair)
        {
            final FeeServicePair feeServicePair = (FeeServicePair) obj;
            fee = feeServicePair.getFee();
        }
        else
        {
            fee = (ServiceFee2) obj;
        }
        
        final StringBuilder sb = new StringBuilder();
        sb.append("Visitor = ");
        sb.append(getClass().getSimpleName());
        sb.append(", RecurringPeriod = ");
        sb.append(getChargingCycle());
        sb.append(", Subscriber = ");
        sb.append(getSub().getId());
        sb.append(", Service = ");
        sb.append(fee.getServiceId());
        final PMLogMsg pm = new PMLogMsg(RecurringRechargeSupport.getRecurringRechargePMModule(isPreWarnNotificationOnly()), "Service item", sb.toString());

        try
        {
        	
        	if(getChargingCycle().equals(ChargingCycleEnum.MULTIDAY))
        	{
        		/*
        		 *  modify end date for multi day service , since for multi-day services  enddate is dynamic 
        		 *  i.e. two services starting at the same day can have different end dates
        		 *  
        		 *  Same goes with start date
        		 */
            	
        		ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(ServicePeriodEnum.MULTIDAY);
            	
        		Date startDate = handler.calculateCycleStartDate(ctx, getBillingDate(), -1, -1, getSub().getId(), fee);
                super.setStartDate(startDate);
                
                /*
                 * For performance optimization, dont want calculateCycleEndDate to invoke calculateCycleStartDate again which it does internally 
                 * 	if CALCULATE_END_DATE_FROM_CYCLE_START is not set to false 
                 */
                subContext.put(MultiDayPeriodHandler.CALCULATE_END_DATE_FROM_CYCLE_START, false);
            	Date endDate = handler.calculateCycleEndDate(subContext, startDate, -1, -1, null,fee);
    			super.setEndDate(endDate);
                super.setOrigServiceEndDate(endDate);
                
                
        	}
        	
        	this.setChargeable(this.isChargeable(subContext, fee));  //Checking for 'Chargeable While In Arrears' and other stuff.
        	
        	boolean tempChargeableFlag = this.isChargeable();
        	if(!tempChargeableFlag){
        		tempChargeableFlag = this.isChargeableInDurationSuspended(subContext, fee); //checking specific to chargeableInDurationSuspended
        		this.setChargeable(tempChargeableFlag);
        	}
        	
            if (this.isChargeable())
            {
                if (this.isPreWarnNotificationOnly())
                {
                    try
                    {
                        handlePreWarnNotification(ctx, fee, fee.getFee());
                    }
                    catch (Throwable t)
                    {
                        Logger.minor(ctx, this, "Fail to retrieve service fee related service (id=" + 
                                fee.getServiceId() + 
                                ") for subscriber '" + getSub().getId() + "'. Not sending pre-warn sms notification for this item: " + t.getMessage(), t);
                    }
                }
                else
                {
                    handleServiceTransaction(subContext, fee);
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(subContext))
                {
                    LogSupport.debug(subContext, this, "Service :" + fee.getServiceId() + " is not chargable to subscriber : "
                        + this.getSub().getId());
                }
            }
        } catch (Exception e) {
        	throw new AgentException(e);
		}
        finally
        {
            pm.log(subContext);
        }
    }
    
    /**
     * Checking specific to charge for services chargeableInDurationSuspended
     * because [chargeableWhileSuspended 'Chargeable While In Arrears'] is handled above, so not changing the existing flow.
     * @param parentCtx
     * @param fee
     * @return
     */

    private boolean isChargeableInDurationSuspended(Context parentCtx, ServiceFee2 fee) {
 	
        Context context = parentCtx.createSubContext();
        context.put(Subscriber.class, getSub());

        boolean ret = false;
        final StringBuilder sb = new StringBuilder();
        sb.append("Inside RechargeSubscriberServiceVisitor::isChargeableInDurationSuspended, ");
        sb.append(getSub().getId());
        sb.append(" for service ");
        sb.append(fee.getServiceId());
        sb.append(" getSuspensionReason " );
        sb.append(getSub().getSuspensionReason());
        sb.append(" getSusbcriberSuspensionReason " );
        sb.append(getSusbcriberSuspensionReason(context, getSub()));
        
        try
        {
            if (isItemChargeable(context, fee.getService(context).isChargeableInDurationSuspended()) && getSusbcriberSuspensionReason(context, getSub()))
            {
                sb.append(" the service is chargeable while subscription is suspended.");
                ret = true;
            }
            
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, this, sb.toString());
            }
            
        }
        catch (final Throwable t)
        {
            LogSupport.minor(context, this, "fail to decide if the service " + fee.getServiceId() + " for subscriber "
                + getSub().getId() + " is suspended or charged ", t);
        }   
		
		return ret;
	}

	private boolean getSusbcriberSuspensionReason(Context ctx, Subscriber sub) {

		int spid = sub.getSpid();

		And where = new And();
		where.add(new EQ(SubscriptionSuspensionReasonXInfo.SPID, spid));
		where.add(new EQ(SubscriptionSuspensionReasonXInfo.REASONCODE, getSub().getSuspensionReason()));

		SubscriptionSuspensionReason bean = null;
		Home home = (Home) ctx.get(SubscriptionSuspensionReasonHome.class);

		try {
			bean = (SubscriptionSuspensionReason) home.find(ctx, where);
		} catch (HomeException e) {
			throw new RuntimeException(
					"Exception occured while finding Subscriber suspension reason");
		}
		if (bean == null) 
		{
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, this, "Subscriber Suspension Reason is null then also charge.");
			}
			return true;
		}
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "In RechargeSubscriberServiceVisitor::getSusbcriberSuspensionReason, SubscriptionSuspensionReason: " + bean.getSuspendmonthlycharges());
        }
		return bean.getSuspendmonthlycharges();
	}
		
	private boolean dropZeroChargeAmount(Context ctx, ServiceFee2 fee)
    {
        return fee.getFee()==0 && !isSubscriberSuspending() && getSub().isPrepaid() && (isDropZeroAmountTransactions(ctx) || isPreWarnNotificationOnly());
    }

    /**
     * Returns if the service can be charged. The service should not be charged in the
     * following cases: 1. charged in service package, 2. suspended 3. charged in same
     * billing cycle
     *
     * @param context
     *            The operating context.
     * @param fee
     *            Service being charged.
     * @return Whether the service should be charged.
     */
    public boolean isChargeable(final Context parentCtx, final ServiceFee2 fee)
    {
        Context context = parentCtx.createSubContext();
        context.put(Subscriber.class, getSub());

        boolean ret = true;
        boolean isSuspending = false;
        final StringBuilder sb = new StringBuilder();
        sb.append("Not charging subscriber ");
        sb.append(getSub().getId());
        sb.append(" for service ");
        sb.append(fee.getServiceId());
        sb.append(" because ");
        try
        {
            if (!isItemChargeable(context, fee.getService(context).isChargeableWhileSuspended()))
            {
                sb.append("the service is not chargeable while subscription is suspended/in arrears.");
                ret = false;
            }
            else if(isSuspendAll() && isSubscriberSuspending())
            {
            	// CANTLS-1599 : PickNPay specific delivery fix to override NRC date behaviour:
            	// Need to add new condition when primary service (suspended) is multiday but core services are not, they should still suspend.
            	// Added condition here to skip all further checks of dropZeroChargeAmount, isMatchingServicePeriod, chargingCycle..
            	// Refer to multiday check below for suspension of NON-PickNPay multiday services
            	
            	Service service = fee.getService(context);
            	short serviceType = service.getType().getIndex();
            	
            	if(serviceType == ServiceTypeEnum.VOICE_INDEX || serviceType == ServiceTypeEnum.SMS_INDEX || serviceType == ServiceTypeEnum.DATA_INDEX )
            	{
	            	if(fee.getChargeFailureAction().getIndex() == ChargeFailureActionEnum.REMOVE_INDEX)
	                {
	                    sb.append("it is being removed.");
	                    this.removeService(context, fee);
	                    ret = false;
	                }
	                else
	                {
	                	sb.append("it is being suspended.");
	                    this.suspendService(context, fee);
	                    ret = false;
	                }
	                isSuspending = true;
            	}
            }
            else if (dropZeroChargeAmount(context, fee))
	        {
	            sb.append("the service has 0 charge amount.");
	            ret = false;
	        }
            else if (isServiceInPackage(fee))
            {
                sb.append(" it is charged in service package.");
                ret = false;
            }
            else if (!isMatchingServicePeriod(fee))
            {
                sb.append("the charging period of this service is not " + getChargingCycle());
                ret = false;
            }
            else if (isServiceSuspended(context, fee))
            {
                sb.append("it is suspended.");
                ret = false;

            }
            else if (!getChargingCycle().equals(ChargingCycleEnum.MULTIDAY) && isSubscriberSuspending())
            {
                if(fee.getChargeFailureAction().getIndex() == ChargeFailureActionEnum.REMOVE_INDEX)
                {
                    sb.append("it is being removed.");
                    this.removeService(context, fee);
                    ret = false;
                }
                else
                {
                    /*
                     * Two multi-day services can have different cycle-start-date and cycle-end-date and thus suspension of one service should not cause the suspension of others. 
                     * Monthly , weekly , multi-monthly service would have same cycle-start-date and cycle-end-date and thus suspension of one would cause suspension of rest of the same type.
                     * 
                     * TT # 12051433017
                     * 
                     *	Another issue (TT# 12061157040) got introduced as a side effect of above fix : Two multi-Day services can have same cycle-start-date( i.e. same next-recurring-chargedate).
            	 	and in that case all of those should be suspended 
                     *		
                     *	See below fix - "else if (getChargingCycle().equals(ChargingCycleEnum.MULTIDAY) && isSubscriberSuspending())" 
                     * Note : i did not move isSuspending() check after isChargeable() to fix this issue but only for the special case which is multi-day. 
                     *	
                     */
                    sb.append("it is being suspended.");
                    this.suspendService(context, fee);
                    ret = false;
                }
                isSuspending = true;
            }
            else if (!isChargeable(context, fee, ChargedItemTypeEnum.SERVICE, fee.getService(context).getAdjustmentType(), fee.getFee(),
                        fee.getServicePeriod()))
            {
                sb.append("it is not chargeable (it was charged for the current billing cycle or on a future date).");
                ret = false;
            }
            else if (getChargingCycle().equals(ChargingCycleEnum.MULTIDAY) && isSubscriberSuspending())
            {
                if(fee.getChargeFailureAction().getIndex() == ChargeFailureActionEnum.REMOVE_INDEX)
                {
                    sb.append("it is being removed.");
                    this.removeService(context, fee);
                    ret = false;
                }
                else
                {
                    /*
                     * TT # 12061157040
                     * Code execution is here that means Multi-day service is chargeable and thus should be suspended because some other multi-day service has indicated that 
                     * all the services which are chargeable should be suspended. ( e.g. - is primary Multi Day service is suspended because of low balance in CPS all of the remaining 
                     * services , which are next in line for re-charging, should be suspended without attempting to re-charge it. 
                     * 
                     */
                	sb.append("it is being suspended.");
                    this.suspendService(context, fee);
                    ret = false;
                }
                isSuspending = true;
            }
            else if (ServicePeriodSupportHelper.get(context).usesSpecialHandler(context, fee.getServicePeriod()) && isProRated())
            {
                ServicePeriodHandler handler = ServicePeriodSupportHelper.get(context).getHandler(fee.getServicePeriod());
                double rate = handler.calculateRate(context, getBillingDate(), getBillCycle(context).getDayOfMonth(), getCRMSpid(context).getSpid(), getSub().getId(), fee);
                setItemRate(rate);
            }
            else if (isProRated())
            {
                setItemRate(getRate());
            }

            

            /*
             * log the error condition.
             */
            if (!ret && LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, this, sb.toString());
            }
            if(isSuspending && generateRechargeFailureErOnOCGBypass(context))
            {
            	createER(context, this.createTransaction(context, fee.getService(context).getAdjustmentType(), fee.getFee())
            			, String.valueOf(fee.getServiceId()), RECHARGE_FAIL_ABM_LOWBALANCE,
            			com.redknee.product.s2100.ErrorCode.NOT_ENOUGH_BAL);
            }
        }
        catch (final Throwable t)
        {
            LogSupport.minor(context, this, "fail to decide if the service " + fee.getServiceId() + " for subscriber "
                + getSub().getId() + " is suspended or charged ", t);
            /*
             * we charge the service if we can not decide if the service is suspended or
             * not
             */

        }

        return ret;

    }


    /**
     * Determines whether the charging period of service matches the service period being
     * charged.
     *
     * @param fee
     *            The service fee.
     * @return Returns whether the charging period of the service matches the service
     *         period being charged.
     */
    public boolean isMatchingServicePeriod(final ServiceFee2 fee)
    {
        final boolean result = SafetyUtil.safeEquals(fee.getServicePeriod().getChargingCycle(), getChargingCycle());
        return result;
    }


    /**
     * Determines whether the service is suspended.
     *
     * @param context
     *            The operating context.
     * @param fee
     *            Service to be charged.
     * @return Returns whether the service has been suspended.
     * @throws HomeException
     *             Thrown if there are problems looking up the suspension status of the
     *             service.
     */
    private boolean isServiceSuspended(final Context context, final ServiceFee2 fee) throws HomeException
    {
        boolean result = false;
        if (SuspendedEntitySupport.isSuspendedEntity(context, getSub().getId(), fee.getServiceId(), SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, ServiceFee2.class))
        {
            result = true;
        }
        return result;
    }


    /**
     * Determines whether the service is part of a service package.
     *
     * @param fee
     *            Service to be charged.
     * @return Whether the service is part of a service package.
     */
    private boolean isServiceInPackage(final ServiceFee2 fee)
    {
        return fee.getSource() != null && fee.getSource().startsWith("Package");
    }
    
    /**
     * Creates a transaction for the service.
     *
     * @param ctx
     *            The operating context.
     * @param fee
     *            Service being charged.
     * @return Whether the charge succeeded or failed.
     */
    public boolean handleServiceTransaction(final Context context, final ServiceFee2 fee)
    {
    	if(LogSupport.isDebugEnabled(context)){
            LogSupport.debug(context, this,">>Start of inside handleServiceTransaction method::");
        }
        Transaction trans = null;
        int result = RECHARGE_SUCCESS;
        int ocgResult = OCG_RESULT_SUCCESS;
        Context ctx = context.createSubContext();
        ctx.put(RecurringRechargeSupport.RECURRING_RECHARGE_CHARGED_ITEM, fee);
        boolean successBalanceTransferDebit = false;
        boolean successBalanceTransferCredit = false;
        SubscriberServices subscriberServices = null;
        try
        {
            final Service service = fee.getService(context);

            try
            {
                
                if (ServiceTypeEnum.TRANSFER.equals(service.getType()))
                {
                    fee.setFee(-Math.abs(fee.getFee()));
                }
                
                if(ServiceTypeEnum.CALLING_GROUP.equals(service.getType()))
                {
                	long totalfee = 0l;
                	boolean aggPPCugToOwner = false;
                	ClosedUserGroup cug = ClosedUserGroupSupport.getCug(ctx, getSub().getRootAccount(ctx).getBAN());
                	if(cug!= null)
                	{
                		aggPPCugToOwner = cug.getAuxiliaryService(ctx).getAggPPServiceChargesToCUGOwner();
                		if(aggPPCugToOwner && this.getSub().equals(cug.getOwner(ctx)))
                		{
                			totalfee = fee.getFee();
                			totalfee = totalfee * cug.getSubscribers().size();
                			AdjustmentType type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                	                AdjustmentTypeEnum.CUGPriceplanServiceCharges);
                			trans = this.createCUGOwnerTransaction(context, type.getCode(), totalfee, cug.getOwner(ctx));
                			trans.setSubscriberID(cug.getOwner(ctx).getId());
                		}
                		else if(!aggPPCugToOwner)
                		{
                			trans = this.createTransaction(ctx, service.getAdjustmentType(), fee.getFee());
                		}
                		else
                		{
                		    LogSupport.info(ctx, this, "Skipping charging for subscriber "+ getSub().getId()+
                		    		" for services "+ fee.getServiceId()+
                		    		". This will be accumulated and charged to CUG owner.");
                		    
                		    return false;
                		}
                	}
                	else
                    {
                        LogSupport.info(ctx, this, "Skipping charging for subscriber " + getSub().getId()
                                + " for services " + fee.getServiceId()
                                + ". Group account cug does not exist.");
                        return false;
                    }
                }
                else
                {
                	if(LogSupport.isDebugEnabled(ctx)){
                        LogSupport.debug(ctx, this,"Before fetching SubServices Details::");
                    } 
                	subscriberServices = SubscriberServicesSupport.getSubscriberServiceRecord(ctx,
                             ((Subscriber)ctx.get(Subscriber.class)).getId(), service.getID(), fee.getPath());
                	 if(LogSupport.isDebugEnabled(ctx)){
                		 LogSupport.debug(ctx, this,"After retriving SubServices Details:: "+subscriberServices);
                     }
                	 trans = this.createTransaction(ctx, service.getAdjustmentType(), fee.getFee());
                }
                
                if(LogSupport.isDebugEnabled(ctx))
                {
                    String msg = MessageFormat.format(
                        "Restrict Provisioning {0} for Service {1}, for subscriber {2}", 
                            new Object[]{service.getRestrictProvisioning()? "ON" : "OFF",
                                    Long.valueOf(service.getIdentifier()), getSub().getId()});
                    LogSupport.debug(ctx, this, msg);
                }
                

                subscriberServices = SubscriberServicesSupport.getSubscriberServiceRecord(ctx,
                        ((Subscriber)ctx.get(Subscriber.class)).getId(), service.getID(), fee.getPath());
                LogSupport.debug(ctx, this, "[handleServiceTransaction]Got SubscriberServices:" + subscriberServices);
                long personalisedfee=0;
                long amount=0;
                Calendar startCalendar = new GregorianCalendar();
                //startCalendar.setTime(new Date());
                startCalendar.setTime(subscriberServices.getNextRecurringChargeDate()); // Fix for ITSC-7851
                Calendar endCalendar = new GregorianCalendar();
        		endCalendar.setTime(subscriberServices.getEndDate());
        		int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        		int diffMonth = diffYear*12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        		LogSupport.minor(
    					ctx,
    					
    					"No Charging history found for service "
    							+ service.getIdentifier()
    							+ " , Subscriber "
    							+ subscriberServices.getMandatory()
    							+ "Difference of month value  "
    							+ diffMonth, OM_ATTEMPT);
        		
        		if (diffMonth>0){
                   personalisedfee=0;
                   amount=0;
                   
                   LogSupport.minor(
       					ctx,
       					
       					"No Charging history found for service "
       							+ service.getIdentifier()
       							+ " , Subscriber "
       							+ subscriberServices.getMandatory()
       							+ "Difference of month value so setting 0 value "
       							+ diffMonth, OM_ATTEMPT);
        			
        			
        		}
        		else{
        	       personalisedfee= subscriberServices.getPersonalizedFee();
        	       amount= fee.getFee();
        		}
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Recharge SubscriberService IS : " + subscriberServices);
                }
                if(subscriberServices != null && subscriberServices.isPersonalizedFeeSet())
                {
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Applying PERSONALIZED Fee for SubscriberService : " + subscriberServices);
                    }
                    
                    	trans = this.createTransaction(ctx, service.getAdjustmentType(), subscriberServices.getPersonalizedFee());
                   
                }
               
              //changing the amount of the trnas according to the service count       
                long orgAmount=trans.getAmount();
                long servQuantity=subscriberServices.getServiceQuantity();
                long newAmount=orgAmount*servQuantity;	
                trans.setAmount(newAmount);
                trans.setServiceQuantity(servQuantity);
                
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Service Quantity Details in Trasaction Object:: "+trans+" quantity:: "
                    		+servQuantity+" new Amount:: "+newAmount);
                }
                
                trans.setAllowCreditLimitOverride(fee.getServicePreference()==ServicePreferenceEnum.MANDATORY || !service.getRestrictProvisioning());
                
                if (fee.getServicePeriod().equals(ServicePeriodEnum.ONE_TIME) ) 
                {
                    // changed for Dory, but supposed to be accepted by all customers. 
                   trans.setServiceRevenueRecognizedDate(trans.getTransDate()); 
                } else
                {
                    trans.setServiceRevenueRecognizedDate(trans.getServiceEndDate());    
                }
                
                if (ServiceTypeEnum.TRANSFER.equals(service.getType()))
                {
                    Subscriber supporterSubscriber = BalanceTransferSupport.validateSubscribers(ctx, this.getSub());
                    BalanceTransferSupport.createDebitTransaction(ctx, trans, this.getSub(), supporterSubscriber, true);
                    successBalanceTransferDebit = true;
                }
                
                long serviceFeeAmount = fee.getFee();
                if(subscriberServices!= null && subscriberServices.isPersonalizedFeeSet())
                {
                    serviceFeeAmount = subscriberServices.getPersonalizedFee();
                }
                
                
                // PickNPay : Only at this stage we are sure if the charge will be a valid transaction and if servicefee is credit/debit, 
                // so accumulate charges here for balance check instead of charging
                if(this.isAllOrNothingChargingFirstPass())
                {
                	handleAllOrNothingChargeAccumulation(ctx, fee, fee.getFee());
                }
                else
                {
	                final PMLogMsg pm1 = new PMLogMsg(RecurringRechargeSupport.RECURRING_RECHARGE_PM_MODULE, "Service item - Create transaction", "");
	                ctx.put(ALL_OR_NOTHING_CHARGING_SUSPEND, isSuspendAll());
	                trans = CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, trans, true);
	                successBalanceTransferCredit = true;
	                pm1.log(ctx);
	                
	                //Fixed TT#13042229003.Removed the Code added in TT#13021413063. 
	                	final PMLogMsg pm2 = new PMLogMsg(RecurringRechargeSupport.RECURRING_RECHARGE_PM_MODULE, "Service item - Create history", "");
	                    SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, fee, getSub(), 
	                            HistoryEventTypeEnum.CHARGE, ChargedItemTypeEnum.SERVICE, trans.getAmount(), 
	                            serviceFeeAmount, trans, getBillingDate());
	                    pm2.log(ctx); 
	                               
	                this.addTransactionId(Long.valueOf(trans.getReceiptNum()));                
                }   
            }
            catch (final OcgTransactionException e)
            {
                ocgResult = e.getErrorCode();
                result = handleOCGError(ctx, ocgResult, fee);
                LogSupport.major(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for services "
                    + fee.getServiceId(), e);
                handleFailure(ctx, trans, result, ocgResult, "OCG Failure", fee.getServiceId(),
                    ChargedItemTypeEnum.SERVICE);
            }
        }
        catch (final HomeException e)
        {
            LogSupport.major(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for services "
                + fee.getServiceId(), e);
            result = RECHARGE_FAIL_XHOME;
            handleFailure(ctx, trans, result, ocgResult, "Xhome Exception:" + e.getMessage(), fee.getServiceId(),
                ChargedItemTypeEnum.SERVICE);
        }
        catch (final Throwable t)
        {

            LogSupport.minor(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for services "
                + fee.getServiceId(), t);
            result = RECHARGE_FAIL_UNKNOWN;
            handleFailure(ctx, trans, result, ocgResult, "Failure Unknown:" + t.getMessage(), fee.getServiceId(),
                ChargedItemTypeEnum.SERVICE);
        }
        finally
        {
        	if(!this.isAllOrNothingChargingFirstPass())
            {
	            if (successBalanceTransferDebit && !successBalanceTransferCredit)
	            {
	                try
	                {
	                    Currency currency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);
	                    
	                    BalanceTransferSupport.addSubscriberNote(ctx, getSub(), trans.getAdjustmentType(),
	                            "Unable to credit subscriber for the amount " + currency.formatValue(trans.getAmount())
	                                    + " regarding service " + fee.getServiceId()
	                                    + ", but PPSM supporter was already debited.", SystemNoteTypeEnum.RECURRINGCHARGE,
	                            SystemNoteSubTypeEnum.BALANCETRANSFER_FAIL_PoD);
	
	                    LogSupport.minor(ctx, this,
	                            "Unable to credit subscriber " + getSub().getId() + " for the amount " + currency.formatValue(trans.getAmount())
	                            + " regarding service " + fee.getServiceId()
	                                    + ", but PPSM subscriber has already been debited.");
	                }
	                catch (HomeException e)
	                {
	                    LogSupport.minor(ctx, this,
	                            "Unable to create subscriber note for subscriber " + getSub().getId(), e);
	                }
	            }
	            
	            final PMLogMsg pm3 = new PMLogMsg(RecurringRechargeSupport.RECURRING_RECHARGE_PM_MODULE, "Service item - Create ERs and OMs", "");
	            if(trans != null)
	            {
	                createER(ctx, trans, String.valueOf(fee.getServiceId()), result, ocgResult);
	            }
	            createOM(ctx, getAgentName(), result == RECHARGE_SUCCESS);
	            this.accumulateForOne(result == RECHARGE_SUCCESS, fee.getFee());
	            if(result == RECHARGE_SUCCESS)
	            {
	            	handleRechargeNotification(context, fee, fee.getFee());
	            }
	            else
	            {
	            	handleRechargeFailureNotification(context, fee, fee.getFee());
	            }
	            pm3.log(ctx);
            }
        }
        if(LogSupport.isDebugEnabled(context)){
            LogSupport.debug(context, this,">>End of inside handleServiceTransaction method and recharge Value:: "+result);
        }

        return result == RECHARGE_SUCCESS;
    }


    /**
     * Handles OCG error, suspend service and mapping error code.
     *
     * @param ctx
     *            The operating context.
     * @param code
     *            OCG error code.
     * @param fee
     *            Service being charged.
     * @return The recurring recharge result code.
     */
    public int handleOCGError(final Context ctx, final int code, final ServiceFee2 fee)
    {
        int ret = RECHARGE_FAIL_OCG_UNKNOWN;

        switch (code)
        {
            case com.redknee.product.s2100.ErrorCode.BAL_EXPIRED:
                ret = RECHARGE_FAIL_ABM_EXPIRED;
                suspendService(ctx, fee);
                break;
            case com.redknee.product.s2100.ErrorCode.BALANCE_INSUFFICIENT:
            case com.redknee.product.s2100.ErrorCode.NOT_ENOUGH_BAL:
                ret = RECHARGE_FAIL_ABM_LOWBALANCE;
                performChargeFailureAction(ctx, fee);
                break;
            case com.redknee.product.s2100.ErrorCode.INVALID_ACCT_NUM:
                ret = RECHARGE_FAIL_ABM_INVALIDPROFILE;
                suspendService(ctx, fee);
                break;
            default:

        }

        return ret;
    }


    /**
     * Suspends the service.
     *
     * @param ctx
     *            The operating context.
     * @param fee
     *            Service being suspended.
     */
    public void suspendService(final Context ctx, final ServiceFee2 fee)
    {
        boolean restrictProvisioningForPostpaid = false;
        try
        {
            restrictProvisioningForPostpaid = fee.getService(ctx).isRestrictProvisioning()
                && getSub().getSubscriberType() == SubscriberTypeEnum.POSTPAID;
        } 
        catch (HomeException e1)
        {
            if(LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, this, 
                        "Could not fetch service-template for the serviceFee: "+ 
                        fee.getServiceId(), e1);
            restrictProvisioningForPostpaid = false;
        }
        
        if (
                restrictProvisioningForPostpaid || 
                (isSuspendOnFailure() && getSub().getSubscriberType() == SubscriberTypeEnum.PREPAID))
        {
            String subscriberId = getSub().getId();
            long serviceId = fee.getServiceId();
            LogSupport.info(ctx, this, "Suspending service " + serviceId + " for subscriber "
                + subscriberId);
            try
            {
                getSub().insertSuspendedService(ctx, fee, SuspendReasonEnum.NONPAYMENT);
                getSub().setSuspendingEntities(true);
                this.incrementServiceCountSuspend();
            }
            catch (final HomeException e)
            {
                LogSupport.major(ctx, this, "Fail to suspend service " + fee.getServiceId() + " for subscriber "
                    + subscriberId, e);
            }
        }

    }


    /**
     * Accumulates the total charges and charges count.
     *
     * @param success
     *            Whether the charge was a success.
     * @param amount
     *            Amount being charged.
     */
    @Override
    protected synchronized void accumulateForOne(final boolean success, final long amount)
    {
        if (success)
        {
            this.incrementServicesCountSuccess();
            this.incrementChargeAmountSuccess(amount);
        }
        else
        {
            this.incrementServicesCountFailed();
            this.incrementChargeAmountFailed(amount);
        }
        this.incrementServicesCount();
        this.incrementChargeAmount(amount);
    }
    
    /**
     * Removes the service.
     *
     * @param ctx
     *            The operating context.
     * @param fee
     *            Service being removed.
     */
    private void removeService(final Context ctx, final ServiceFee2 fee)
    {
            String subscriberId = getSub().getId();
            long serviceId = fee.getServiceId();
            LogSupport.info(ctx, this, "Unprovisioning service " + serviceId + " for subscriber: "+ subscriberId);

            SubscriberServices object =  SubscriberServicesSupport.getSubscriberServiceRecord(ctx, subscriberId, serviceId, fee.getPath());
            List<SubscriberServices> subServices = new ArrayList<SubscriberServices>();
            subServices.add(object);
            try
            {
                SubscriberServicesSupport.unprovisionSubscriberServices(ctx, getSub(), subServices, getSub(), new HashMap<ExternalAppEnum, ProvisionAgentException>());
                SubscriberServicesSupport.deleteSubscriberServiceRecord(ctx, object);
                getSub().removeServiceFromIntentToProvisionServices(ctx, serviceId);
                getSub().setSuspendingEntities(true);
                SubscriptionNotificationSupport.sendPricePlanOptionRemovalNotification(ctx, getSub(), fee, null);
                
            }
            catch (HomeException e)
            {
                LogSupport.major(ctx, this, "Failed to unprovision subscriber serice with service id : "+serviceId);
            }
            
    }


    /**
     * Takes action whether to remove or suspend the service.
     *
     * @param ctx
     *            The operating context.
     * @param fee
     *            Service being suspended/removed.
     */
    private void performChargeFailureAction(final Context ctx, final ServiceFee2 fee)
    {
        String subscriberId = getSub().getId();
        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Looking up for Priceplan version level configuration for suspending/" +
                    "unprovisioning service " + fee.getServiceId() + " for subscriber "
                    + subscriberId);
        }
        
        if(fee.getChargeFailureAction().getIndex() == ChargeFailureActionEnum.REMOVE_INDEX)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Removing Service  " + fee.getServiceId() + " for subscriber "
                        + subscriberId);
            }
            removeService(ctx, fee);
        }
        else if(fee.getChargeFailureAction().getIndex() == ChargeFailureActionEnum.SUSPEND_INDEX)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Suspending Service  " + fee.getServiceId() + " for subscriber "
                        + subscriberId);
            }
            suspendService(ctx, fee);
        }
        
    }
    
    
    /**
	 * @return whether it's charge accumulation for first pass of all or nothing charging
	 */
	public boolean isAllOrNothingChargingFirstPass() {
		return allOrNothingChargingFirstPass_;
	}

	/**
	 * @param allOrNothingChargingFirstPass_ 
	 */
	public void setAllOrNothingChargingFirstPass(
			boolean allOrNothingChargingFirstPass_) {
		this.allOrNothingChargingFirstPass_ = allOrNothingChargingFirstPass_;
	}
    
	/**
	 * @return whether to suspend all services 
	 */
	public boolean isSuspendAll() {
		return suspendAll_;
	}

	/**
	 * @param suspendAll 
	 */
	public void setSuspendAll(boolean suspendAll) {
		this.suspendAll_ = suspendAll;
	}
	
	private boolean allOrNothingChargingFirstPass_;
	private boolean suspendAll_;
	
	public static final String ALL_OR_NOTHING_CHARGING_SUSPEND = "ALL_OR_NOTHING_CHARGING_SUSPEND";

}
