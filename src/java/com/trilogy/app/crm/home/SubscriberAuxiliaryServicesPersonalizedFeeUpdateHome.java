package com.trilogy.app.crm.home;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.FeePersonalizationRuleEnum;
import com.trilogy.app.crm.bean.Invoice;
//import com.trilogy.app.crm.bean.PaymentOptionEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * @author bdeshmuk
 *
 */
public class SubscriberAuxiliaryServicesPersonalizedFeeUpdateHome extends HomeProxy
{
    private static final long serialVersionUID = -8246917159517221147L;


    public SubscriberAuxiliaryServicesPersonalizedFeeUpdateHome(Context ctx, Home delegate)
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
        SubscriberAuxiliaryService incomingSubscriberAuxService = (SubscriberAuxiliaryService) obj;
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        if (sub == null)
        {
            sub = SubscriberSupport.getSubscriber(ctx, incomingSubscriberAuxService.getSubscriberIdentifier());
        }

        if (sub.getState().equals(SubscriberStateEnum.AVAILABLE) || sub.getState().equals(SubscriberStateEnum.PENDING))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Not considering personalized fee for serivce : "
                        + incomingSubscriberAuxService.getAuxiliaryServiceIdentifier()
                        + " since subscriber is in : " + sub.getState().getDescription() + " state.");
            }
            return;
        }

        /*
         * if back dating future dated service to current date or earlier (predating ) then reset the
         * next-recurring-charge-date to null . It will be set during transaction creation
         */

        SubscriberAuxiliaryService previousSubscriberAuxService = 
                SubscriberAuxiliaryServiceSupport.getSubAuxServBySubIdAuxIdAndSecondaryId(ctx, sub.getId(), 
                        incomingSubscriberAuxService.getAuxiliaryServiceIdentifier(), 
                        incomingSubscriberAuxService.getSecondaryIdentifier());
        if (previousSubscriberAuxService == null)
        {
            // This cannot happen as it is a STORE call
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this,
                        "INVALID OPERATION : Subscriber services ID : " + incomingSubscriberAuxService.getAuxiliaryServiceIdentifier()
                                + " does not exist. Skipping operation.");
            }
            return;
        }

        if (previousSubscriberAuxService.getPersonalizedFee() != incomingSubscriberAuxService.getPersonalizedFee())
        {
            AuxiliaryService auxService = incomingSubscriberAuxService.getAuxiliaryService(ctx);
            if (auxService.isFeePersonalizationAllowed())
            {
                if (!incomingSubscriberAuxService.getIsfeePersonalizationApplied())
                {
                    chargeAndRefund(ctx, incomingSubscriberAuxService, previousSubscriberAuxService, true);
                }
                else
                {
                    chargeAndRefund(ctx, incomingSubscriberAuxService, previousSubscriberAuxService, false);
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
    private void chargeAndRefund(Context ctx, SubscriberAuxiliaryService incomingSubscriberService,
            SubscriberAuxiliaryService previousSubscriberService, boolean revertToServiceFee) throws HomeException
    {

        LogSupport.info(ctx, this, "[Apply Personalised Fee] start.. ");
        AuxiliaryService auxService = incomingSubscriberService.getAuxiliaryService(ctx);
        Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
      
        int billCycleDay = subscriber.getAccount(ctx).getBillCycle(ctx).getDayOfMonth();
        
        Invoice invoice = InvoiceSupport.getMostRecentInvoice(ctx, subscriber.getBAN());
        Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        CRMSpid spid = (CRMSpid) spidHome.find(ctx, Integer.valueOf(subscriber.getSpid()));
        Date currentBillingStartDate = BillCycleSupport.getDateForBillCycleStart
                (ctx, new Date(), billCycleDay);
        Date currentBillingEndDate = BillCycleSupport.getDateForBillCycleEnd(ctx, new Date(), billCycleDay);
        
        boolean prorateFee = Boolean.FALSE;
        boolean isCurrentDateInBillDelayPeriod = BillCycleSupport.isCurrentDateInBillDelayPeriod(ctx, spid, billCycleDay);
        
        Date serviceStartDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(incomingSubscriberService.getStartDate()); 
        LogSupport.info(ctx, this, "[Apply Personalised Fee] Aux Service start date::  "+serviceStartDate);
        
        if(invoice != null && invoice.getInvoiceDate() == currentBillingStartDate)
        {
            LogSupport.info(ctx, this, "[Apply Personalised Fee] Will not REFUND(CHARGE) for personalised fee as invoice has already been generated." +
                    " Personalised fee shall take affect from next recurring recharge..[Exit]");
            return;
        }

            
       
        	if(isCurrentDateInBillDelayPeriod || ((serviceStartDate.after(currentBillingStartDate) || serviceStartDate == currentBillingStartDate)
               		&& serviceStartDate.before(currentBillingEndDate)))
             {
                LogSupport.info(ctx, this, "[Apply Personalised Fee] Payment option is In advance for Aux service id::  "+auxService.getIdentifier());
                SubscriberSubscriptionHistory history = SubscriberSubscriptionHistorySupport.getLatestHistoryForItem(
                        ctx, subscriber.getId(), ChargedItemTypeEnum.AUXSERVICE, auxService.getIdentifier(), HistoryEventTypeEnum.CHARGE, 
                        incomingSubscriberService.getSecondaryIdentifier());
                
                if(history != null)
                {
                    LogSupport.info(ctx, this, "[Apply Personalised Fee] Charging history found for aux service ::  "+auxService.getIdentifier()
                            +" History with txn id  = "+history.getTransactionId());
                    if(CalendarSupportHelper.get(ctx).getDayOfMonth(serviceStartDate) != billCycleDay)
                    {
                        if((serviceStartDate.after(currentBillingStartDate) || serviceStartDate == currentBillingStartDate) 
                                && serviceStartDate.before(currentBillingEndDate))
                        {
                            prorateFee = auxService.getActivationFee() == ActivationFeeModeEnum.PRORATE ? Boolean.TRUE : Boolean.FALSE;
                        }
                    }
                    And and = new And();
                    and.add(new EQ(TransactionXInfo.RECEIPT_NUM, history.getTransactionId()));
                    and.add(new EQ(TransactionXInfo.SPID, subscriber.getSpid()));
                    Transaction txn = HomeSupportHelper.get(ctx).findBean(ctx, Transaction.class, and);
                   
                    LogSupport.info(ctx, this, "[Apply Personalised Fee] Going to create REFUND (CHARGE) transactions");
                    AuxServiceChargingSupport.applyServicesPersonalizedChargeByIds(ctx, incomingSubscriberService, subscriber,
                            ChargingConstants.ACTION_PROVISIONING_REFUND, HistoryEventTypeEnum.REFUND, txn, null, ChargingConstants.RUNNING_SUCCESS, prorateFee, revertToServiceFee);
                    
                    AuxServiceChargingSupport.applyServicesPersonalizedChargeByIds(ctx, incomingSubscriberService, subscriber,
                            ChargingConstants.ACTION_PROVISIONING_CHARGE, HistoryEventTypeEnum.CHARGE, txn, null, ChargingConstants.RUNNING_SUCCESS, prorateFee, revertToServiceFee);
                }
            }else if(!isCurrentDateInBillDelayPeriod)
            {
                LogSupport.info(ctx, this, "[Apply Personalised Fee] Will not REFUND(CHARGE) for personalised fee as personalisation " +
                        " is not being done during bill delay period. Please check SPID configuration.[Exit]");
                return;
            }
    
    }

}
