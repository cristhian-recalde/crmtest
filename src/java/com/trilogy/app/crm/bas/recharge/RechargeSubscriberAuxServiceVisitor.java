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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.auxiliaryservice.MultiSimAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.GroupChargingAuxSvcExtension;
import com.trilogy.app.crm.service.MultiDayPeriodHandler;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport73;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.snippet.log.Logger;





/**
 * Visitor to generate recurring charge for each auxiliary service.
 *
 * @author larry.xia@redknee.com
 */
public class RechargeSubscriberAuxServiceVisitor extends AbstractRechargeItemVisitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>RechargeSubscriberAuxServiceVisitor</code>.
     *
     * @param billingDate
     *            Date to charge the subscriber on.
     * @param agentName
     *            Name of the agent generating the charges.
     * @param servicePeriod
     *            Service period to charge.
     * @param subscriber
     *            The subscriber to be charged.
     * @param suspendOnFailure
     *            Whether to suspend on failure to charge.
     * @param activeAssociations
     *            Active subscriber-auxiliary service associations.
     */
    public RechargeSubscriberAuxServiceVisitor(final Date billingDate, final String agentName,
        final ChargingCycleEnum chargingPeriod, final Subscriber subscriber, final boolean suspendOnFailure,
        final Date startDate, final Date endDate, final double rate, final boolean preBilling, 
        final boolean proRated, final boolean preWarnNotificationOnly)
    {
        super(billingDate, agentName, chargingPeriod, startDate, endDate, rate, preBilling, proRated, preWarnNotificationOnly);
        setSub(subscriber);
        this.setSuspendOnFailure(suspendOnFailure);        
    }


    public RechargeSubscriberAuxServiceVisitor(final Context context, final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingPeriod, final Subscriber subscriber, final boolean suspendOnFailure,
            final double rate, final boolean preBilling, final boolean proRated, final boolean preWarnNotificationOnly, 
            final boolean allOrNothingCharging) throws HomeException
    {
        super(context, billingDate, agentName, chargingPeriod, subscriber.getAccount(context).getBillCycleDay(context),
                subscriber.getSpid(), rate, preBilling, proRated, preWarnNotificationOnly);
        this.setSub(subscriber);
        this.setSuspendOnFailure(suspendOnFailure);
    }


    /**
     * {@inheritDoc}
     */
    public void visit(final Context context, final Object obj) throws AgentException
    {
        final Context subContext = context.createSubContext();
        final SubscriberAuxiliaryService service = (SubscriberAuxiliaryService) obj;
        final StringBuilder sb = new StringBuilder();
        sb.append("Visitor = ");
        sb.append(getClass().getSimpleName());
        sb.append(", RecurringPeriod = ");
        sb.append(getChargingCycle());
        sb.append(", Subscriber = ");
        sb.append(getSub().getId());
        sb.append(", Auxiliary service = ");
        sb.append(service.getAuxiliaryServiceIdentifier());
        sb.append(", Secondary identifier = ");
        sb.append(service.getSecondaryIdentifier());
        final PMLogMsg pm = new PMLogMsg(RecurringRechargeSupport.getRecurringRechargePMModule(isPreWarnNotificationOnly()), "Auxiliary service item", sb.toString());

        try
        {
        	if(getChargingCycle().equals(ChargingCycleEnum.MULTIDAY))
        	{
        		/*
        		 *  modify end date for multi day service , since for multi-day services , enddate is dynamic 
        		 *  i.e. two services starting at the same day can have different end date
        		 *  
        		 *  same goes with start date
        		 */
            	
        		ServicePeriodHandler handler = ServicePeriodSupportHelper.get(context).getHandler(ServicePeriodEnum.MULTIDAY);
            	
        		
        		Date startDate = handler.calculateCycleStartDate(context, getBillingDate(), -1, -1, getSub().getId(), service);
                super.setStartDate(startDate);
                
                /*
                 * For performance optimization, dont want calculateCycleEndDate to invoke calculateCycleStartDate again which it does internally 
                 * 	if CALCULATE_END_DATE_FROM_CYCLE_START is not set to false 
                 */
                subContext.put(MultiDayPeriodHandler.CALCULATE_END_DATE_FROM_CYCLE_START, false);
            	Date endDate = handler.calculateCycleEndDate(subContext,  startDate, -1, -1, null,service.getAuxiliaryService(context));
    			super.setEndDate(endDate);
                super.setOrigServiceEndDate(endDate);
        	}
        	
            this.setChargeable(this.isChargeable(subContext, service));

            if (this.isChargeable())
            {
                if (this.isPreWarnNotificationOnly())
                {
                    try
                    {
                        handlePreWarnNotification(context, service.getAuxiliaryService(context), getFee(context, service));
                    }
                    catch (Throwable t)
                    {
                        Logger.minor(context, this, "Fail to retrieve subscriber auxiliary service related service (id=" + 
                                service.getAuxiliaryServiceIdentifier() + 
                                ") for subscriber '" + getSub().getId() + "'. Not sending pre-warn sms notification for this item: " + t.getMessage(), t);
                    }
                }
                else
                {
                    handleServiceTransaction(subContext, service);
                }
            }
            else
            {
                if (Logger.isDebugEnabled())
                {
                    Logger.debug(subContext, this, "Auxiliary Service :" + service.getIdentifier()
                        + " is not chargable to subscriber : " + this.getSub().getId());
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
    
    private boolean dropZeroChargeAmount(Context ctx, AuxiliaryService auxService)
    {
        return auxService.getCharge()==0 && !isSubscriberSuspending() && getSub().isPrepaid() && (isDropZeroAmountTransactions(ctx) || isPreWarnNotificationOnly());
    }


    /**
     * Returns if the auxiliary service can be charged. The auxiliary service should not
     * be charged in the following cases: 1. charged in service package, 2. suspended 3.
     * charged in same billing cycle.
     *
     * @param ctx
     *            The operating context.
     * @param service
     *            Auxiliary service to be charged.
     * @return Whether the auxiliary service can be charged.
     */
    public boolean isChargeable(final Context parentCtx, final SubscriberAuxiliaryService service)
    {
        Context ctx = parentCtx.createSubContext();
        ctx.put(Subscriber.class, getSub());
        
        boolean ret = true;
        boolean isServiceSuspending = false;
        try
        {
            String msg = "";
            AuxiliaryService auxService = service.getAuxiliaryService(ctx);

            if (!isItemChargeable(ctx, auxService.isChargeableWhileSuspended()))
            {
                msg = "the auxiliary service is not chargeable while subscription is suspended/in arrears.";
                ret = false;
            }
            else if (dropZeroChargeAmount(ctx, auxService))
            {
                msg = "the auxiliary service has 0 charge amount.";
                ret = false;
            }
            else if (isAuxiliaryServiceEnded(service))
            {
                msg = "the auxiliary service subscription has ended.";
                ret = false;
            }
            else if (!isMatchingServicePeriod(ctx, auxService))
            {
                msg = "the charging period of this auxiliary service is not " + getChargingCycle();
                ret = false;
            }
            else if (isFutureActivation(service))
            {
                msg = "the auxiliary service subscriber has not yet started.";
                ret = false;
            }
            else if (isAuxiliaryServiceSuspended(ctx, service))
            {
                msg = "it has been suspended.";
                ret = false;
            }
            else if (!getChargingCycle().equals(ChargingCycleEnum.MULTIDAY) && isSubscriberSuspending())
            {
            	/*
            	 * Two multi-day services can have different cycle-start-date and cycle-end-date and thus suspension of one service should not cause the suspension of others. 
            	 * Monthly , weekly , multi-monthly service would have same cycle-start-date and cycle-end-date and thus suspension of one would cause suspension of rest of the same type.
            	 * 
            	 * TT # 12051433017
            	 * 	Another issue (TT# 12061157040) got introduced as a side effect of above fix : Two multi-Day services can have same cycle-start-date( i.e. same next-recurring-chargedate).
            	 	and in that case all of those should be suspended 
            	 *		
            	 *	See below fix - "else if (getChargingCycle().equals(ChargingCycleEnum.MULTIDAY) && isSubscriberSuspending())" 
            	 * 	Note : i did not move isSuspending() check after isChargeable() to fix this issue but only for the special case which is multi-day. 
            	 * 
            	 */
            	
                msg = "it is being suspended.";
                this.suspendService(ctx, service);
                isServiceSuspending = true;
                ret = false;
            }
            else if (auxService.isPrivateCUG(ctx) && !ClosedUserGroupSupport73.isOwner(ctx, getSub(), service))
            {
            		return false; 
            }
            else if (!isChargeable(ctx, service, ChargedItemTypeEnum.AUXSERVICE, auxService.getAdjustmentType(),
                            auxService.getCharge(), auxService.getChargingModeType()))
            {
                // for VPN service the charge history would be tracked on the member subscribers,
                // but the transactions will go to the leader.
                msg = "it is not chargeable (has already been charged in the bill cycle or future)";
                ret = false;
            } 
            else if (getChargingCycle().equals(ChargingCycleEnum.MULTIDAY) && isSubscriberSuspending())
            {
            	/*
            	 * TT # 12061157040
            	 * Code execution is here that means Multi-day service is chargeable and thus should be suspended because some other multi-day service has indicated that 
            	 * all the services which are chargeable should be suspended. ( e.g. - is primary Multi Day service is suspended because of low balance in CPS all of the remaining 
            	 * services , which are next in line for re-charging, should be suspended without attempting to re-charge it. 
            	 * 
            	 */
            	msg = "it is being suspended.";
                this.suspendService(ctx, service);
                isServiceSuspending = true;
                ret = false;
                
            }
            else if (ServicePeriodSupportHelper.get(ctx).usesSpecialHandler(ctx, auxService.getChargingModeType()) && isProRated())
            {
                ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(auxService.getChargingModeType());
                double rate = handler.calculateRate(ctx, getBillingDate(), getBillCycle(ctx).getDayOfMonth(), getCRMSpid(ctx).getSpid(), getSub().getId(), service);
                setItemRate(rate);
            }
            else if (isProRated())
            {
                setItemRate(getRate());
            }
                

            /*
             * Log error message.
             */
            if (!ret && Logger.isDebugEnabled())
            {
                final StringBuilder sb = new StringBuilder();
                sb.append("Not charging subscriber ");
                sb.append(getSub().getId());
                sb.append(" for auxiliary service ");
                sb.append(service.getAuxiliaryServiceIdentifier());
                sb.append(" because ");
                sb.append(msg);

                Logger.debug(ctx, this, sb.toString());
            }
            if(isServiceSuspending && generateRechargeFailureErOnOCGBypass(ctx))
            {
            	createER(ctx, this.createTransaction(ctx, service.getAuxiliaryService(ctx).getAdjustmentType(), service.getAuxiliaryService(ctx).getCharge()), 
            			String.valueOf(service.getAuxiliaryServiceIdentifier()), RECHARGE_FAIL_ABM_LOWBALANCE,
            			com.redknee.product.s2100.ErrorCode.NOT_ENOUGH_BAL);
            }
        }
        catch (final Throwable t)
        {
            Logger.minor(ctx, this, "fail to decide if the Auxiliary service " + service.getIdentifier()
                + " for subscriber " + getSub().getId() + " is suspended or charged ", t);
            /*
             * we charge the service if we can not decide if the service is suspended or not.
             */
        }

        return ret;
    }


    /**
     * Determines whether the charging period of auxiliary service matches the service
     * period being charged.
     *
     * @param context
     *            The operating context.
     * @param service
     *            The auxiliary service to be charged.
     * @return Returns whether the charging period of the auxiliary service matches the
     *         service period being charged.
     */
    public boolean isMatchingServicePeriod(final Context context, final AuxiliaryService service)
    {
        boolean result = false;
        if (service != null)
        {
            result = SafetyUtil.safeEquals(service.getChargingModeType().getChargingCycle(), getChargingCycle());
        }
        return result;
    }


    /**
     * Returns whether the auxiliary service subscription has ended.
     *
     * @param subAuxSvc
     *            Subscriber auxiliary service association.
     * @return Whether the auxiliary service subscription has ended.
     */
    private boolean isAuxiliaryServiceEnded(final SubscriberAuxiliaryService subAuxSvc)
    {
        boolean result = false;
        final Date billingDate = CalendarSupportHelper.get().getDateWithNoTimeOfDay(getBillingDate());
        final Date endDate = CalendarSupportHelper.get().getEndOfDay(subAuxSvc.getEndDate());
        if (endDate.before(billingDate))
        {
            result = true;
        }
        return result;
    }


    /**
     * Returns whether the auxiliary service has a future activation date.
     *
     * @param subAuxSvc
     *            Subscribe auxiliary service association.
     * @return Whether the auxiliary service has a future activation date.
     */
    private boolean isFutureActivation(final SubscriberAuxiliaryService subAuxSvc)
    {
        boolean result = false;
        final Date billingDate = CalendarSupportHelper.get().getDateWithNoTimeOfDay(getBillingDate());
        final Date startDate = CalendarSupportHelper.get().getDateWithNoTimeOfDay(subAuxSvc.getStartDate());
        if (billingDate.before(startDate))
        {
            result = true;
        }
        return result;
    }


    /**
     * Returns whether the auxiliary service has been suspended.
     *
     * @param ctx
     *            The operating context.
     * @param service
     *            Auxiliary service.
     * @return Whether the auxiliary service has been suspended.
     * @throws HomeException
     *             Thrown if there are problems determining the suspension status of the
     *             auxiliary service.
     */
    private boolean isAuxiliaryServiceSuspended(final Context ctx, final SubscriberAuxiliaryService service)
        throws HomeException
    {
        return SuspendedEntitySupport.isSuspendedEntity(ctx, getSub().getId(), service.getIdentifier(),
                service.getSecondaryIdentifier(), AuxiliaryService.class);
    }


    /**
     * Charges the subscriber for an auxiliary service.
     *
     * @param ctx
     *            The operating context.
     * @param service_
     *            The auxiliary service to be charged.
     * @return Whether the charge succeeded or failed.
     */
    public boolean handleServiceTransaction(final Context context, final SubscriberAuxiliaryService association)
    {
        Transaction trans = null;
        int result = RECHARGE_SUCCESS;
        int ocgResult = OCG_RESULT_SUCCESS;
        Context ctx = context.createSubContext();
        ctx.put(RecurringRechargeSupport.RECURRING_RECHARGE_CHARGED_ITEM, association);

        AuxiliaryService service = null;
        try
        {
            service = association.getAuxiliaryService(ctx);
            
            try
            {

            	/**
            	 * 
            	 * The below if block is for MultiSIM Aux services.
            	 * 
            	 * For MultiSIM services which are charged per SIM,
            	 * there's a default entry in the SubscriberAuxiliaryService
            	 * table which is for the service being assigned to the Subscriber. Henceforth,
            	 * when an additional SIM(multiSIM) is added to the subscriber an entry is added
            	 * in the SubscriberAuxiliaryService table per SIM added. So for 'n' SIMs(multiSIM)
            	 * added by the subscriber there'll be 'n + 1' entries in the 
            	 * SubscriberAuxiliaryService table. The entry added for assigning the service
            	 * to the subscriber needs to be ignored while charging in chargePerSim mode.
            	 * 
            	 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
            	 * 
            	 */
            	if(AuxiliaryServiceTypeEnum.MultiSIM.equals(service.getType()))
            	{
            		boolean isChargePerSim = Boolean.FALSE;

            		List<ExtensionHolder> extensionHolderList = service.getAuxiliaryServiceExtensions();

            		for(ExtensionHolder extnHolder : extensionHolderList)
            		{
            			if( extnHolder.getExtension() instanceof MultiSimAuxSvcExtension )
            			{
            				isChargePerSim = ((MultiSimAuxSvcExtension)extnHolder.getExtension()).getChargePerSim();
            			}
            		}

            		if("".equals(association.getMultiSimPackage()) && isChargePerSim)
            		{
            			LogSupport.info(context, this, "The multiSIM service is charged per sim. For SUBSCRIBERAUXILIARYSERVICE.AUXILIARYSERVICEIDENTIFIER=" +
            					association.getAuxiliaryServiceIdentifier() + " multisimpackage is null/empty, hence not being charged." );

            			return Boolean.TRUE;
            		}
            	}

            	final PMLogMsg pm1 = new PMLogMsg(RecurringRechargeSupport.RECURRING_RECHARGE_PM_MODULE, "Auxiliary service item - Create transaction", "");
            	
            	trans = this.createTransaction(ctx, service.getAdjustmentType(), service.getCharge());
            	
            	if(association.isPersonalizedFeeSet())
            	{
            		if(LogSupport.isDebugEnabled(ctx))
            		{
            			LogSupport.debug(ctx, this, "Applying PERSONALIZED Fee for AuxService : " + association.getIdentifier());
            		}
            		
            			trans = this.createTransaction(ctx, service.getAdjustmentType(), association.getPersonalizedFee());
            	}


            	if (service.getChargingModeType().equals(ServicePeriodEnum.ONE_TIME) ) 
            	{
            		// changed for Dory, but supposed to be accepted by all customers. 
            		trans.setServiceRevenueRecognizedDate(trans.getTransDate()); 
            	} else
            	{
            		trans.setServiceRevenueRecognizedDate(trans.getServiceEndDate());    
            	}

            	if(LogSupport.isDebugEnabled(ctx))
            	{
            		String msg = MessageFormat.format(
            				"Restrict Provisioning {0} for Aux-Service {1}, for subscriber {2}", 
            				new Object[]{service.getRestrictProvisioning()? "ON" : "OFF",
            						Long.valueOf(service.getIdentifier()), getSub().getId()});
            		LogSupport.debug(ctx, this, msg);
            	}

            	trans.setAllowCreditLimitOverride(!service.getRestrictProvisioning());

            	if (service.isPrivateCUG(ctx))
            	{
            		trans = handlePrivateCug(ctx, association, trans); 
            	} 
            	else 
            	{
            		handleVpnAndGroupTransaction(ctx, getSub(), service, trans);
            		trans = CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, trans, true);
            		pm1.log(ctx);

            		final PMLogMsg pm2 = new PMLogMsg(RecurringRechargeSupport.RECURRING_RECHARGE_PM_MODULE, "Auxiliary service item - Create history", "");
            		SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, association, getSub(), 
            				HistoryEventTypeEnum.CHARGE, ChargedItemTypeEnum.AUXSERVICE, trans.getAmount(), 
            				trans.getFullCharge(), trans, getBillingDate());
            		this.addTransactionId(Long.valueOf(trans.getReceiptNum()));  
            		pm2.log(ctx);
            	}

            }
            catch (final OcgTransactionException e)
            {
                ocgResult = e.getErrorCode();
                result = handleOCGError(ctx, ocgResult, association);
                Logger.major(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for services "
                    + association.getAuxiliaryServiceIdentifier(), e);
                handleFailure(ctx, trans, result, ocgResult, "OCG Failure", association.getAuxiliaryServiceIdentifier(),
                    ChargedItemTypeEnum.AUXSERVICE);
            }
        }
        catch (final HomeException e)
        {
            Logger.major(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for services "
                + association.getAuxiliaryServiceIdentifier(), e);
            result = RECHARGE_FAIL_XHOME;
            handleFailure(ctx, trans, result, ocgResult, "Xhome Exception:" + e.getMessage(), association.getAuxiliaryServiceIdentifier(),
                ChargedItemTypeEnum.AUXSERVICE);
        }
        catch (final InvalidVPNGroupLeaderException ve)
        {
            Logger.major(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for services "
                + association.getAuxiliaryServiceIdentifier(), ve);
            result = RECHARGE_FAIL_VPN_LEADER_NOT_FOUND;
            handleFailure(ctx, trans, result, ocgResult, "VPN group leader not found", association.getAuxiliaryServiceIdentifier(),
                ChargedItemTypeEnum.AUXSERVICE);
        }
        catch (final Throwable t)
        {
            Logger.minor(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for services "
                + association.getAuxiliaryServiceIdentifier(), t);
            result = RECHARGE_FAIL_UNKNOWN;
            handleFailure(ctx, trans, result, ocgResult, "Failure Unknown:" + t.getMessage(), association.getAuxiliaryServiceIdentifier(),
                ChargedItemTypeEnum.AUXSERVICE);
        }
        finally
        {
            createER(ctx, trans, String.valueOf(association.getAuxiliaryServiceIdentifier()), result, ocgResult);
            createOM(ctx, getAgentName(), result == RECHARGE_SUCCESS);
            if (service!=null)
            {
            	/**
            	 * TT 13080130013
            	 * Transaction amount = serviceCharge*itemRate. We should use transaction amount instead of service charge. 
            	 * In case of CUG, transaction amount is sum of all the members charges +owner charges.
            	 */
            	this.accumulateForOne(result == RECHARGE_SUCCESS, trans.getAmount());
            }
          
            try
            {
            	
            	AuxiliaryService aux = association.getAuxiliaryService(ctx);
	            if(result == RECHARGE_SUCCESS)
	            {
	            	handleRechargeNotification(ctx, aux, aux.getCharge());
	            }
	            else
	            {
	            	handleRechargeFailureNotification(ctx, aux, aux.getCharge());
	            }
            }
            catch(HomeException e)
            {
            	LogSupport.major(ctx, this, "Exception sending Failure notification for Aux Service." + association.getAuxiliaryServiceIdentifier() + ". Error Message:" + e.getMessage());
            }
        }

        return result == RECHARGE_SUCCESS;
    }


    /**
     * Redirect charge to group leader if the service is VPN.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber of the auxiliary service.
     * @param service
     *            VPN auxiliary service to be charged.
     * @param trans
     *            Transaction of the VPN auxiliary service charge.
     * @throws HomeException
     *             Thrown if there are problems creating the VPN transaction.
     * @throws InvalidVPNGroupLeaderException
     *             Thrown if there are problems with the VPN group leader.
     */
    public static void handleVpnAndGroupTransaction(final Context ctx, final Subscriber subscriber,
            final AuxiliaryService service, final Transaction trans)
        throws HomeException, InvalidVPNGroupLeaderException
    {
        if (service.isGroupChargable())
        {
            long groupCharge = GroupChargingAuxSvcExtension.DEFAULT_GROUPCHARGE;
        
            GroupChargingAuxSvcExtension groupChargingAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service, GroupChargingAuxSvcExtension.class);
            if (groupChargingAuxSvcExtension!=null)
            {
                groupCharge = groupChargingAuxSvcExtension.getGroupCharge();
            }
            // this implementation is not generic enough for Group Chargeable services
            final Subscriber groupLeader;
            final Account account = (Account) ctx.get(Account.class);
            
            if (account != null && account.getBAN().equals(subscriber.getBAN()))
            {
                groupLeader = service.getGroupLeaderForCharging(ctx, account);
            }
            else
            {
                groupLeader = service.getGroupLeaderForCharging(ctx, subscriber);
            }
            
            if (groupLeader != null && subscriber.getId().equals(groupLeader.getId()))
            {
                int groupAdjustmentType = GroupChargingAuxSvcExtension.DEFAULT_GROUPADJUSTMENTTYPE;
                
                if (groupChargingAuxSvcExtension!=null)
                {
                    groupAdjustmentType = groupChargingAuxSvcExtension.getGroupAdjustmentType();
                }
                // for group charges the charge history would be tracked on the original adjustment type and charge,
                // but the transactions will be on the group adjustment type and group charge.
                trans.setAdjustmentType(groupAdjustmentType);
                trans.setAmount(groupCharge);
                return;
            }

            if (service.getType() == AuxiliaryServiceTypeEnum.Vpn)
            {
                if (groupLeader == null)
                {
                    throw new InvalidVPNGroupLeaderException("No VPN MSISDN Specified for root Account of "
                            + subscriber.getId());
                }

                // for VPN service the charge history would be tracked on the member subscribers,
                // but the transactions will go to the leader.
                trans.setSupportedSubscriberID(trans.getSubscriberID());
                trans.setBAN(groupLeader.getBAN());
                trans.setSpid(groupLeader.getSpid());
                trans.setMSISDN(groupLeader.getMSISDN());
                trans.setSubscriberID(groupLeader.getId());
            }
        }
    }


    /**
     * Handles OCG error, mapping OCG result code to recharge result code, and suspend the
     * service.
     *
     * @param ctx
     *            The operating context.
     * @param code
     *            OCG error code.
     * @param service
     *            Auxiliary service.
     * @return Recurring recharge error codes.
     */
    public int handleOCGError(final Context ctx, final int code, final SubscriberAuxiliaryService service)
    {
        int ret = RECHARGE_FAIL_OCG_UNKNOWN;

        switch (code)
        {
            case com.redknee.product.s2100.ErrorCode.BAL_EXPIRED:
                ret = RECHARGE_FAIL_ABM_EXPIRED;
                suspendService(ctx, service);
                break;
            case com.redknee.product.s2100.ErrorCode.BALANCE_INSUFFICIENT:
            case com.redknee.product.s2100.ErrorCode.NOT_ENOUGH_BAL:
                ret = RECHARGE_FAIL_ABM_LOWBALANCE;
                suspendService(ctx, service);
                break;
            case com.redknee.product.s2100.ErrorCode.INVALID_ACCT_NUM:
                ret = RECHARGE_FAIL_ABM_INVALIDPROFILE;
                suspendService(ctx, service);
                break;
            default:
        }

        return ret;
    }


    /**
     * Suspends an auxiliary service.
     *
     * @param ctx
     *            The operating context.
     * @param service
     *            The auxiliary service to be suspended.
     */
    public void suspendService(final Context ctx, final SubscriberAuxiliaryService service)
    {
        boolean restrictProvisioningForPostpaid = false;
        try
        {
            restrictProvisioningForPostpaid = service.getAuxiliaryService(ctx).isRestrictProvisioning() 
                    && getSub().getSubscriberType().equals(SubscriberTypeEnum.POSTPAID);
        } catch (HomeException e1)
        {
            if(LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, this, 
                        "Could not fetch AuxService-template for the SubAuxService: "+ 
                        service.getIdentifier(), e1);
            restrictProvisioningForPostpaid = false;
        }
        
        
        if (
                restrictProvisioningForPostpaid ||
                (isSuspendOnFailure() && getSub().getSubscriberType().equals(SubscriberTypeEnum.PREPAID)))
        {
            if (Logger.isInfoEnabled())
            {
                Logger.info(ctx, this, "Suspending auxiliary service " + service.getIdentifier() + " for subscriber "
                        + getSub().getId());
            }
            try
            {
                getSub().insertSuspendedAuxService(ctx, service);
                getSub().setSuspendingEntities(true);
                this.incrementServiceCountSuspend();
            }
            catch (final HomeException e)
            {
                Logger.major(ctx, this, "fail to suspend Auxiliary service " + service.getIdentifier()
                    + " for subscriber " + getSub().getId(), e);
            }
        }

    }


    /**
     * Accumulate the total charges and count.
     *
     * @param success
     *            Whether this is a success or failure.
     * @param amount
     *            Amount of the charge.
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
    
    private long getFee(Context ctx, SubscriberAuxiliaryService service) throws HomeException
    {
        long result = service.getAuxiliaryService(ctx).getCharge();
        
        if (service.getAuxiliaryService(ctx).isPrivateCUG(ctx))
        {
            ClosedUserGroup cug = ClosedUserGroupSupport.getCug(ctx, service.getSecondaryIdentifier()); 
            result = ClosedUserGroupSupport73.getTotalChargeForPCUG(ctx, service.getAuxiliaryService(ctx), cug.getSubscribers().keySet(), cug.getSpid());
        }
        return result;
    }
    
	private Transaction handlePrivateCug(Context ctx, SubscriberAuxiliaryService service, Transaction trans)
	throws HomeException 
	{
        ClosedUserGroup cug = ClosedUserGroupSupport.getCug(ctx, service.getSecondaryIdentifier()); 
	    long fee = getFee(ctx, service);
        trans.setAmount(Math.round(fee * getItemRate()));
		trans.setFullCharge(fee);
		try
		{
			trans =  CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, trans);
			long ownerFee = trans.getFullCharge();
			long ownerAmount = trans.getAmount();

	        long serviceChargeExternal = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEEXTERNAL;
	        long serviceChargePostpaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPOSTPAID;
	        long serviceChargePrepaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPREPAID;
	        
	        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,
	                service.getAuxiliaryService(ctx), CallingGroupAuxSvcExtension.class);
	        if (callingGroupAuxSvcExtension!=null)
	        {
	            serviceChargeExternal = callingGroupAuxSvcExtension.getServiceChargeExternal();
	            serviceChargePostpaid = callingGroupAuxSvcExtension.getServiceChargePostpaid();
	            serviceChargePrepaid = callingGroupAuxSvcExtension.getServiceChargePrepaid();
	        }
	        
			Set cugSubscribers = cug.getSubscribers().keySet(); 
	        for(Iterator i = cugSubscribers.iterator(); i.hasNext(); )
	        {
	            String number = (String) i.next(); 
                Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, number); 
                if (msisdn != null && !msisdn.getMsisdn().equals(cug.getOwnerMSISDN()))
                {
                    final Subscriber memberSub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn.getMsisdn());
                    if (memberSub!=null)
                    {
                        long memberFee;
                        if (msisdn.getSpid() != cug.getSpid())
                        {
                            memberFee = serviceChargeExternal;
                        }
                        else if (msisdn.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
                        {
                            memberFee = serviceChargePostpaid;
                        }
                        else
                        {
                            memberFee = serviceChargePrepaid;
                        }
                        long memberAmount = Math.round(memberFee*getItemRate());
                        
                        final PMLogMsg pm2 = new PMLogMsg(RecurringRechargeSupport.RECURRING_RECHARGE_PM_MODULE, "Auxiliary service item - Create history", "");
                        SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, service, memberSub, 
                                HistoryEventTypeEnum.CHARGE, ChargedItemTypeEnum.AUXSERVICE, memberAmount, 
                                memberFee, trans, getBillingDate());
                        pm2.log(ctx);
                        
                        ownerFee -= memberFee;
                        ownerAmount -= memberAmount;
                        
                        updateMemberNextRecurringChargeDate(ctx,memberSub,cug);
                    }
                }
			}

	        final PMLogMsg pm2 = new PMLogMsg(RecurringRechargeSupport.RECURRING_RECHARGE_PM_MODULE, "Auxiliary service item - Create history", "");
            SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, service, getSub(), 
                    HistoryEventTypeEnum.CHARGE, ChargedItemTypeEnum.AUXSERVICE, ownerAmount, 
                    ownerFee, trans, getBillingDate());
            pm2.log(ctx);
            
            return trans;
            
		} 
		catch (OcgTransactionException e)
		{
			//by pass suspension 
			HomeException he =  new HomeException(e.getMessage()); 
			he.setStackTrace(e.getStackTrace()); 
			throw he; 
		}
	}


	private void updateMemberNextRecurringChargeDate(Context ctx,
			Subscriber memberSub, ClosedUserGroup cug) 
	{
	
			try {
				Subscriber owner = getSub();
				long auxServiceId = cug.getAuxiliaryService(ctx).getIdentifier();
			
				SubscriberAuxiliaryService ownerSAS  = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryService(ctx, owner.getId(),auxServiceId , cug.getID());
				SubscriberAuxiliaryService memSAS  = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryService(ctx, memberSub.getId(),auxServiceId , cug.getID());
				
			
				if(ownerSAS != null && memSAS != null)
				{
					memSAS.setNextRecurringChargeDate(ownerSAS.getNextRecurringChargeDate());
					SubscriberAuxiliaryServiceSupport.updateSubscriberAuxiliaryService(ctx, memSAS);
				}
			} catch (HomeException e) {
				LogSupport.major(ctx, this, "Exception occurred while updating next-recurring-charge-date of private cug of member - " + memberSub.getId(),e);
			}
			
	}
	
	@Override
	protected boolean isSubscriberSuspending()
	{
		if(isEnforceAuxServiceRecharge() && getSub().getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
		{
			if(getSub().getNewlySuspendedEntities().getSuspendedAuxServices().size() > 0)
			{
				return true;
			}else
				return false;
		}
		else{
			return super.isSubscriberSuspending();
		}
	}
	
	private boolean enforceRecharging_;


	protected void setEnforceRecharging(boolean enforceRecharging_) {
		this.enforceRecharging_ = enforceRecharging_;
	}


	protected boolean isEnforceAuxServiceRecharge() {
		return enforceRecharging_;
	}

}
