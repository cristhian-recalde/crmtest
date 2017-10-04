package com.trilogy.app.crm.home;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.FeePersonalizationRuleEnum;
import com.trilogy.app.crm.bean.Invoice;
//import com.trilogy.app.crm.bean.PaymentOptionEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author bdeshmuk
 * 
 */
public class SubscriberServicesPersonalizedFeeUpdateHome extends HomeProxy
{

    private static final long serialVersionUID = -2270024838939858116L;


    public SubscriberServicesPersonalizedFeeUpdateHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);

    }


    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {

        /*
         * If it is create operation means it is firt time provision, charges for this should be calculated and applied
         * in the provisioning pipeline. So do not do anything here.
         */
         
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Create Method of Personalized fee is called");
        }

        return super.create(ctx, obj);
    }


    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        checkPersonalizedFeeUpdate(ctx, obj);
        return super.store(ctx, obj);
    }


    private void checkPersonalizedFeeUpdate(Context ctx, Object obj) throws HomeException
    {
       
       
    	if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Store Method of Personalized fee is called");
        }
        SubscriberServices incomingSubscriberService = (SubscriberServices) obj;
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        if (sub == null)
        {
            sub = SubscriberSupport.getSubscriber(ctx, incomingSubscriberService.getSubscriberId());
            ctx.put(Subscriber.class, sub);
        }

        if (sub.getState().equals(SubscriberStateEnum.AVAILABLE) || sub.getState().equals(SubscriberStateEnum.PENDING))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Not considering personalized fee for serivce : "
                        + incomingSubscriberService.getServiceId()
                        + " since subscriber is in : " + sub.getState().getDescription() + " state.");
            }
            return;
        }

        /*
         * if back dating future dated service to current date or earlier (predating ) then reset the
         * next-recurring-charge-date to null . It will be set during transaction creation
         */

       SubscriberServices previousSubscriberService = SubscriberServicesSupport.getSubscriberServiceRecord(ctx,
                sub.getId(), incomingSubscriberService.getServiceId(), incomingSubscriberService.getPath());
        if (previousSubscriberService == null)
        {
            // This cannot happen as it is a STORE call
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this,
                        "INVALID OPERATION : Subscriber services ID : " + incomingSubscriberService.getServiceId()
                                + " does not exist. Skipping operation.");
            }
            return;
        }
        
        Service service = incomingSubscriberService.getService(ctx);
        if (service.isFeePersonalizationAllowed())
        {
            if (previousSubscriberService.getPersonalizedFee() != incomingSubscriberService.getPersonalizedFee())
            {
                if (!incomingSubscriberService.getIsfeePersonalizationApplied())
                {
                    chargeAndRefund(ctx, incomingSubscriberService, previousSubscriberService, true);
                }
                else
                {
                    chargeAndRefund(ctx, incomingSubscriberService, previousSubscriberService, false);
                }
            }
            
        }
    }


    /**
     * @param ctx
     * @param incomingSubscriberService
     * @param previousSubscriberService
     * @throws HomeException 
     */
    private void chargeAndRefund(Context ctx, SubscriberServices incomingSubscriberService,
            SubscriberServices previousSubscriberService, boolean revertToServiceFee) throws HomeException
    {
        LogSupport.info(ctx, this, "[Apply Personalised Fee] start.. ");
        Service service = incomingSubscriberService.getService(ctx);
        Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
       
      
        int billCycleDay = subscriber.getAccount(ctx).getBillCycle(ctx).getDayOfMonth();
        
        Invoice invoice = InvoiceSupport.getMostRecentInvoice(ctx, subscriber.getBAN());
        Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        CRMSpid spid = (CRMSpid) spidHome.find(ctx, Integer.valueOf(subscriber.getSpid()));
        Date currentBillingStartDate = BillCycleSupport.getDateForBillCycleStart
                (ctx, new Date(), billCycleDay);
        Date currentBillingEndDate = BillCycleSupport.getDateForBillCycleEnd(ctx, new Date(), billCycleDay);
        
        boolean prorateFee = Boolean.FALSE;
        //boolean isCurrentDateInBillDelayPeriod = BillCycleSupport.isCurrentDateInBillDelayPeriod(ctx, spid, billCycleDay);
        
        Date serviceStartDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(incomingSubscriberService.getStartDate()); 
        LogSupport.info(ctx, this, "[Apply Personalised Fee] Service start date::  "+serviceStartDate);
        
        if(invoice != null && invoice.getInvoiceDate() == currentBillingStartDate)
        {
            LogSupport.info(ctx, this, "[Apply Personalised Fee] Will not REFUND(CHARGE) for personalised fee as invoice has already been generated." +
                    " Personalised fee shall take affect from next recurring recharge..[Exit]");
            return;
        }
            
    	   if(((serviceStartDate.after(currentBillingStartDate) || serviceStartDate == currentBillingStartDate)
           		&& serviceStartDate.before(currentBillingEndDate)))
           {

                LogSupport.info(ctx, this, "[Apply Personalised Fee] Payment option is In advance for service id::  "+service.getIdentifier());
                SubscriberSubscriptionHistory history = SubscriberSubscriptionHistorySupport.getLatestHistoryForItem(
                        ctx, subscriber.getId(), ChargedItemTypeEnum.SERVICE, service.getIdentifier(), HistoryEventTypeEnum.CHARGE, -1);
                
                if(history != null)
                {
                    LogSupport.info(ctx, this, "[Apply Personalised Fee] Charging history found for service ::  "+service.getIdentifier()
                            +" History with txn id  = "+history.getTransactionId());
                    if(CalendarSupportHelper.get(ctx).getDayOfMonth(serviceStartDate) != billCycleDay)
                    {
                        if((serviceStartDate.after(currentBillingStartDate) || serviceStartDate == currentBillingStartDate) 
                                && serviceStartDate.before(currentBillingEndDate))
                        {
                            prorateFee = service.getActivationFee() == ActivationFeeModeEnum.PRORATE ? Boolean.TRUE : Boolean.FALSE;
                        }
                    }
                    And and = new And();
                    and.add(new EQ(TransactionXInfo.RECEIPT_NUM, history.getTransactionId()));
                    and.add(new EQ(TransactionXInfo.SPID, subscriber.getSpid()));
                    Transaction txn = HomeSupportHelper.get(ctx).findBean(ctx, Transaction.class, and);
                   
                    LogSupport.info(ctx, this, "[Apply Personalised Fee] Going to create REFUND (CHARGE) transactions");
                    ServiceChargingSupport.applyServicesPersonalizedChargeByIds(ctx, incomingSubscriberService, subscriber,
                            ChargingConstants.ACTION_PROVISIONING_REFUND, HistoryEventTypeEnum.REFUND, txn, null, ChargingConstants.RUNNING_SUCCESS, prorateFee, revertToServiceFee);
                    
                    ServiceChargingSupport.applyServicesPersonalizedChargeByIds(ctx, incomingSubscriberService, subscriber,
                            ChargingConstants.ACTION_PROVISIONING_CHARGE, HistoryEventTypeEnum.CHARGE, txn, null, ChargingConstants.RUNNING_SUCCESS, prorateFee, revertToServiceFee);
                }
            
    	   /*else  if(!isCurrentDateInBillDelayPeriod)
            {
                LogSupport.info(ctx, this, "[Apply Personalised Fee] Will not REFUND(CHARGE) for personalised fee as personalisation " +
                		" is not being done during bill delay period. Please check SPID configuration.[Exit]");
                return;
            }
       }
            
       else if(paymentOption == PaymentOptionEnum.PAY_IN_ARREARS_INDEX
                    || paymentOption == PaymentOptionEnum.STRICTLY_PAY_IN_ARREARS_INDEX)
       {
                LogSupport.info(ctx, this, "[Apply Personalised Fee] Payment option is In Arrears for service id::  "+service.getIdentifier());
                Date previousBillingStartDate = CalendarSupportHelper.get(ctx).getMonthsBefore(currentBillingStartDate, 1);
                Date previousBillingEndDate = CalendarSupportHelper.get(ctx).getMonthsBefore(currentBillingEndDate, 1);
                
                final Set<HistoryEventTypeEnum> eventTypes = new HashSet<HistoryEventTypeEnum>();
                eventTypes.add(HistoryEventTypeEnum.CHARGE);
                
           if(isCurrentDateInBillDelayPeriod)
            {
                
                 * Get latest charging event occurred in previous billing cycle since this is pay in arrears
                 
                SubscriberSubscriptionHistory previousHistory = SubscriberSubscriptionHistorySupport.getLastEventBetween(
                        ctx, subscriber.getId(), ChargedItemTypeEnum.SERVICE, service, previousBillingStartDate, 
                        previousBillingEndDate, eventTypes, true);
                if(previousHistory != null)
                {
                    LogSupport.info(ctx, this, "[Apply Personalised Fee] Charging history for previous bill cycle found " +
                    		" for service ::  "+service.getIdentifier()+
                            " History with txn id  = "+previousHistory.getTransactionId());
                   
                    if(CalendarSupportHelper.get(ctx).getDayOfMonth(serviceStartDate) != billCycleDay)
                    {
                        if((serviceStartDate.after(previousBillingStartDate) || serviceStartDate == previousBillingStartDate)
                                && serviceStartDate.before(previousBillingEndDate))
                        {
                            prorateFee = service.getActivationFee() == ActivationFeeModeEnum.PRORATE ? Boolean.TRUE : Boolean.FALSE;
                        }
                    }
                    
                    And and = new And();
                    and.add(new EQ(TransactionXInfo.RECEIPT_NUM, previousHistory.getTransactionId()));
                    and.add(new EQ(TransactionXInfo.SPID, subscriber.getSpid()));
                    
                    Transaction txnForPrevBillingPeriod = HomeSupportHelper.get(ctx).findBean(ctx, Transaction.class, and);
                   
                    if(txnForPrevBillingPeriod != null)
                    {
                        LogSupport.info(ctx, this, "[Apply Personalised Fee] Going to create REFUND (CHARGE) transactions for previous cycle..");
                        ServiceChargingSupport.applyServicesPersonalizedChargeByIds(ctx, incomingSubscriberService, subscriber,
                                ChargingConstants.ACTION_PROVISIONING_REFUND, HistoryEventTypeEnum.REFUND, txnForPrevBillingPeriod, null, ChargingConstants.RUNNING_SUCCESS, prorateFee, revertToServiceFee);
                        
                        ServiceChargingSupport.applyServicesPersonalizedChargeByIds(ctx, incomingSubscriberService, subscriber,
                                ChargingConstants.ACTION_PROVISIONING_CHARGE, HistoryEventTypeEnum.CHARGE, txnForPrevBillingPeriod, null, ChargingConstants.RUNNING_SUCCESS, prorateFee, revertToServiceFee);
                    }
                }
                
            }
                
                 * Get latest charging event occurred in current billing cycle {Mostly, charge applied during MRC will be returned}
                 
                SubscriberSubscriptionHistory currentBillPeriodHistory = SubscriberSubscriptionHistorySupport.getLastEventBetween(
                        ctx, subscriber.getId(), ChargedItemTypeEnum.SERVICE, service, currentBillingStartDate, 
                        currentBillingEndDate, eventTypes, true);
                if(currentBillPeriodHistory != null)
                {
                    LogSupport.info(ctx, this, "[Apply Personalised Fee] Charging history for current bill cycle found " +
                            " for service ::  "+service.getIdentifier()+
                            " History with txn id  = "+currentBillPeriodHistory.getTransactionId());
                    
                    if(CalendarSupportHelper.get(ctx).getDayOfMonth(serviceStartDate) != billCycleDay)
                    {
                        if((serviceStartDate.after(currentBillingStartDate) || serviceStartDate == currentBillingStartDate) 
                                && serviceStartDate.before(currentBillingEndDate))
                        {
                            prorateFee = service.getActivationFee() == ActivationFeeModeEnum.PRORATE ? Boolean.TRUE : Boolean.FALSE;
                        }
                    }
                    
                    And and = new And();
                    and.add(new EQ(TransactionXInfo.RECEIPT_NUM, currentBillPeriodHistory.getTransactionId()));
                    and.add(new EQ(TransactionXInfo.SPID, subscriber.getSpid()));
                    
                    Transaction txnForCurrentBillingPeriod = HomeSupportHelper.get(ctx).findBean(ctx, Transaction.class, and);
                    
                    if(txnForCurrentBillingPeriod != null)
                    {
                        LogSupport.info(ctx, this, "[Apply Personalised Fee] Going to create REFUND (CHARGE) transactions for current cycle..");
                        
                        ServiceChargingSupport.applyServicesPersonalizedChargeByIds(ctx, incomingSubscriberService, subscriber,
                                ChargingConstants.ACTION_PROVISIONING_REFUND, HistoryEventTypeEnum.REFUND, txnForCurrentBillingPeriod, null, ChargingConstants.RUNNING_SUCCESS, prorateFee, revertToServiceFee, currentBillPeriodHistory);
                        
                        ServiceChargingSupport.applyServicesPersonalizedChargeByIds(ctx, incomingSubscriberService, subscriber,
                            ChargingConstants.ACTION_PROVISIONING_CHARGE, HistoryEventTypeEnum.CHARGE, txnForCurrentBillingPeriod, null, ChargingConstants.RUNNING_SUCCESS, prorateFee, revertToServiceFee, currentBillPeriodHistory);
                    }
                }
        }*/
    }
   }
  }
