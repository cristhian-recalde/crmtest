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
package com.trilogy.app.crm.subscriber.subscription.history;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.*;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.EventSuccessEnum;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryXDBHome;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryXInfo;
import com.trilogy.app.crm.bean.service.SubscriptionProvisioningHistory;
import com.trilogy.app.crm.bean.service.SubscriptionProvisioningHistoryHome;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Order;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xhome.elang.Order;
import java.util.*;

public class SubscriberSubscriptionHistorySupport
{
    static class DescendingHistoryComparator implements Comparator<SubscriberSubscriptionHistory>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(final SubscriberSubscriptionHistory o1, final SubscriberSubscriptionHistory o2)
        {
            int result = o2.getTimestamp_().compareTo(o1.getTimestamp_());
            if (result == 0)
            {
                final Long t1 = Long.valueOf(o1.getTransactionId());
                final Long t2 = Long.valueOf(o2.getTransactionId());
                result = t2.compareTo(t1);
            }
            return result;
        }
    }


    public static void moveSubscriber(final Context context, final String oldSubscriberId, final String newSubscriberId)
    {
        moveSubscriber(context, newSubscriberId,
                new EQ(SubscriberSubscriptionHistoryXInfo.SUBSCRIBER_ID, oldSubscriberId));
    }


    public static void moveSubscriber(final Context context, final String oldSubscriberId,
        final String newSubscriberId, final ChargedItemTypeEnum itemType)
    {
        final And and = new And();
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.SUBSCRIBER_ID, oldSubscriberId));
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_TYPE, itemType));
        moveSubscriber(context, newSubscriberId, and);
    }


    public static void moveSubscriber(final Context context, final String newSubscriberId,
            final Predicate selectionPredicate)
    {
        try
        {
            final Collection<SubscriberSubscriptionHistory> histories = getRecords(context, selectionPredicate);
            final Home home = (Home) context.get(SubscriberSubscriptionHistoryXDBHome.class);
            for (final SubscriberSubscriptionHistory history : histories)
            {
                history.setSubscriberId(newSubscriberId);
                home.store(context, history);
            }
        }
        catch (final HomeException exception)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                final StringBuilder sb = new StringBuilder();
                sb.append(exception.getClass().getSimpleName());
                sb.append(" caught in ");
                sb.append("SubscriberSubscriptionHistorySupport.moveSubscriber(): ");
                if (exception.getMessage() != null)
                {
                    sb.append(exception.getMessage());
                }
                LogSupport.debug(context, SubscriberSubscriptionHistorySupport.class.getName(), sb.toString(),
                        exception);
            }
        }
    }

    private static Collection<SubscriberSubscriptionHistory> getRecords(final Context context,
            final Predicate predicate) throws HomeException
    {
        final Home home = (Home) context.get(SubscriberSubscriptionHistoryHome.class);
        return home.select(context, predicate);
    }

    /**
     * Create and Subscriber Subscription record to denote Subscription provisioning/unprovisioning,
     * with the given information.
     * @param context
     * @param sub Subscriber who is provisioning the item
     * @param itemType ChargedItemTypeEnum
     * @param item identifier of the item being provisioned
     * @param state state of the service provisioning to the external client
     */
    public static void addProvisioningRecord(final Context context,
            final Subscriber sub,
            final HistoryEventTypeEnum eventType,
            final ChargedItemTypeEnum itemType,
            final Object item,
            final ServiceStateEnum state)
    {
        addProvisioningRecord(context, sub.getId(),eventType,itemType,item,state);
    }

    /**
     * Create and Subscriber Subscription record to denote Subscription provisioning/unprovisioning,
     * with the given information.
     * @param context
     * @param sub Subscriber who is provisioning the item
     * @param itemType ChargedItemTypeEnum
     * @param item identifier of the item being provisioned
     * @param state state of the service provisioning to the external client
     */
    public static void addProvisioningRecord(final Context context,
            final String subId,
            final HistoryEventTypeEnum eventType,
            final ChargedItemTypeEnum itemType,
            final Object item,
            final ServiceStateEnum state)
    {
        if (!eventType.equals(HistoryEventTypeEnum.PROVISION)
                && !eventType.equals(HistoryEventTypeEnum.UNPROVISION))
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, "SubscriberSubscriptionHistorySupport.addProvisioningRecord",
                        "Wrong usage of this method.  The Event Type must be either Provision or Unprovision.");
            }
            return;
        }

        SubscriptionProvisioningHistory record = new SubscriptionProvisioningHistory();
        inputData(context, subId, eventType,
                itemType, item, state,record);

        try
        {
            createRecord(context, record);
        }
        catch (HomeException e)
        {
            new MajorLogMsg("SubscriberSubscriptionHistorySupport.addProvisioningHistory",
                    "Failed to create Subscriber Subscription History record to denote provisioning. " + record,
                    e).log(context);
        }
    }

    /**
     * Create and Subscriber Subscription record to denote Subscription charging/refunding,
     * with the given information.
     * @param ctx
     *            The operating context.
     * @param item
     *            Service to be charged/refunded.
     * @param sub
     *            Subscriber to be charged/refunded.
     * @param eventType
     *            Only Charge or Refund Accepted
     * @param chargedItemType
     *            To distinguish between Services, Bundles, Auxiliary Services, and Service Packages
     * @param chargedAmount
     *            Charge/refund amount.
     * @param originalFullFee
     *            ServiceFee to charge/refund.
     * @param transaction
     *            If null, indicates no transaction was successfully created for this charge/refund.
     *            Otherwise, it is the reference for the transaction representing this charge/refund.
     * @return 
     */
    public static SubscriberSubscriptionHistory addChargingHistory(final Context ctx,
            final Object item,
            final Subscriber sub,
            final HistoryEventTypeEnum eventType,
            final ChargedItemTypeEnum chargedItemType,
            final long chargedAmount,
            final long originalFullFee,
            final Transaction transaction,
            final Date billingDate)
    {
        // Validation
        if (!eventType.equals(HistoryEventTypeEnum.CHARGE)
                && !eventType.equals(HistoryEventTypeEnum.REFUND))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, "SubscriberSubscriptionHistorySupport.addChargingHistory",
                        "Wrong usage of this method.  The Event Type must be either Charge or Refund.");
            }
            return null;
        }

        String chargedSubscriber = "";
        String transactionId = "";
        // In case the Transaction failed to be created
        if (transaction != null)
        {
            chargedSubscriber = transaction.getSubscriberID();
            transactionId = String.valueOf(transaction.getReceiptNum());
        }

        ServiceStateEnum state;

        if (chargedItemType.equals(ChargedItemTypeEnum.SERVICE))
        {
            //Must look up the Service.ProvisionedState value after provisioning and charging activities
            final SubscriberServices serviceRecord = SubscriberServicesSupport.getSubscriberServiceRecord(ctx,
                sub.getId(), ((ServiceFee2)item).getServiceId(), ((ServiceFee2)item).getPath());
            if (serviceRecord != null)
            {
                state = serviceRecord.getProvisionedState();
            }
            else
            {
                // If service can't be found, means it has been already unprovisioned.
                state = ServiceStateEnum.UNPROVISIONED;
            }
        }
        else
        {
            /* Once we get around to supporting a single State Enum for Service/Bundles/Auxiliary Services
             * we can properly fill in this data.  For now we will assume that a charge means the
             * Bundle/Auxiliary service is provisioned, while a refund means that it was unprovisioned.
             */
            if (eventType.equals(HistoryEventTypeEnum.CHARGE))
            {
                state = ServiceStateEnum.PROVISIONED;
            }
            else
            {
                // Refund
                state = ServiceStateEnum.UNPROVISIONED;
            }
        }

        // Add a Subscriber Subscription History record regardless of whether the charge/refund was $0
        return SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, sub, eventType, chargedItemType, item, state,
                originalFullFee, chargedSubscriber, chargedAmount, transactionId, billingDate);
    }


    /**
     * Create and Subscriber Subscription record to denote Subscription charging/refunding,
     * with the given information.
     * @param context
     * @param sub Subscriber who is provisioning the item
     * @param itemType ChargedItemTypeEnum
     * @param item identifier of the item being provisioned
     * @param state state of the service provisioning to the external client
     * @param fee original fee from price plan
     * @param chargedSubscriber the subscriber to whom the transaction was applied
     * @param chargedAmount the amount charged/refunded
     * @param transactionId reference to the transaction representing this charge/refund. Can
     *         be blank, indicating no transaction relation.
     * @return The actual record if created; <tt>null</tt> otherwise.
     */
    public static SubscriberSubscriptionHistory addChargingHistory(final Context parentCtx,
            final Subscriber sub,
            final HistoryEventTypeEnum eventType,
            final ChargedItemTypeEnum itemType,
            final Object item,
            final ServiceStateEnum state,
            final long fee,
            final String chargedSubscriber,
            final long chargedAmount,
            final String transactionId,
            final Date billingDate)
    {
        Context context = parentCtx.createSubContext();
        context.put(Subscriber.class, sub);
        
        SubscriberSubscriptionHistory record = new SubscriberSubscriptionHistory();
        inputData(context, sub.getId(), eventType,
                itemType, item, state, billingDate,record);
        try
        {
            //Fill in Charging-only data
            record.setChargedSubscriber(chargedSubscriber);
            record.setChargedAmount(chargedAmount);
            record.setServiceFee(fee);
            record.setTransactionId(transactionId);
            record.setRemainingAmount(chargedAmount);
            calculateRemainingAmount(parentCtx,chargedSubscriber,eventType,itemType,item,chargedAmount,record);
            
            CRMSpid spid = (CRMSpid) context.get(CRMSpid.class);
            BillCycle billCycle = (BillCycle) context.get(BillCycle.class);
            Account account = (Account) context.get(Account.class);
            final int billCycleDay;
            if (billCycle!=null && account!=null && account.getBAN().equals(sub.getBAN()) && billCycle.getBillCycleID()==account.getBillCycleID())
            {
                billCycleDay = billCycle.getDayOfMonth();
            }
            else if (account != null && account.getBAN().equals(sub.getBAN()))
            {
                billCycleDay = account.getBillCycleDay(context);
            }
            else
            {
                billCycleDay = SubscriberSupport.getBillCycleDay(context, sub);
            }
            
            if (spid==null || spid.getSpid() != sub.getSpid())
            {
                spid = SpidSupport.getCRMSpid(context, sub.getSpid());
            }
            
            ServicePeriodHandler handler = getHandler(context, item, itemType);
            
            final Date startDate = handler.calculateCycleStartDate(context, billingDate, billCycleDay, spid.getId(), sub.getId(), item);

            record.setLastBillCycleDate(startDate);

            createRecord(context, record);
            
            return record;
        }
        catch (HomeException e)
        {
            new MajorLogMsg("SubscriberSubscriptionHistorySupport.addChargingHistory",
                    "Failed to create Subscriber Subscription History record to denote charging. " + record,
                    e).log(context);

            FrameworkSupportHelper.get(context).notifyExceptionListener(context, e);
            return null;
        }
    }
    
    /**
     * This method used to calculate the remaining amount while refund scenarios.
     */   
     private static void calculateRemainingAmount(final Context parentCtx,
             final String sub,
             final HistoryEventTypeEnum eventType,
             final ChargedItemTypeEnum itemType,
             final Object item,
             final long chargedAmount,
             final SubscriberSubscriptionHistory record){
     	
 	    	if (LogSupport.isDebugEnabled(parentCtx))
 	        {
 	            new DebugLogMsg(
 	                    "SubscriberSubscriptionHistorySupport",
 	                    "Calculating the reaming amount begin in refund scenarios"
 	                    + sub
 	                    + ": "
 	                    + eventType,
 	                    null).log(parentCtx);
 	        } 
     	
     	if(eventType.getIndex() == HistoryEventTypeEnum.REFUND_INDEX){
     		
     		if(itemType.getIndex() == ChargedItemTypeEnum.SERVICE_INDEX){
     			try {
 					SubscriberSubscriptionHistory history = SubscriberSubscriptionHistorySupport
 					        .getLatestHistoryForItem(parentCtx, sub, ChargedItemTypeEnum.SERVICE, record.getItemIdentifier(),
 					                HistoryEventTypeEnum.CHARGE, -1);
 					
 					if(history != null){
 						long remainingAmount = history.getRemainingAmount();
 					         remainingAmount += chargedAmount;
 					         record.setRemainingAmount(remainingAmount);
 					}
 				} catch (HomeException e) {
 					// TODO Auto-generated catch block
 					LogSupport.minor(parentCtx,SubscriberSubscriptionHistorySupport.class, "Some thing Went wrong while calculating the remaining amount");
 					e.printStackTrace();
 				}
     		}else if (itemType.getIndex() == ChargedItemTypeEnum.AUXSERVICE_INDEX){
                 
                 	try {
                 		SubscriberAuxiliaryService subService = (SubscriberAuxiliaryService) item;
                 		Service associatedService = ServiceSupport.getService(parentCtx, subService.getAssociatedServiceId());
                 		SubscriberSubscriptionHistory history = null;
                 			if(associatedService != null){
                 				history = SubscriberSubscriptionHistorySupport
                 							.getLatestHistoryForItem(parentCtx, sub, ChargedItemTypeEnum.SERVICE, associatedService.getID(),
                 									HistoryEventTypeEnum.REFUND, -1);
                 				if(history != null){
                 						long remainingAmount = history.getRemainingAmount();
                 						if((remainingAmount + chargedAmount) > 0){
                 							remainingAmount += chargedAmount;
                     						history.setRemainingAmount(remainingAmount);
                     						SubscriberSubscriptionHistorySupport.updateSubscriberSubscriptionHistoryEvent(parentCtx, history);
                 						}else{
                 							history.setRemainingAmount(0);
                     						SubscriberSubscriptionHistorySupport.updateSubscriberSubscriptionHistoryEvent(parentCtx, history);
                 						}
                 				}
                 			}else{
                 				history = SubscriberSubscriptionHistorySupport
                                 .getLatestHistoryForItem(parentCtx, sub, ChargedItemTypeEnum.AUXSERVICE, subService.getAuxiliaryServiceIdentifier(),
                                         HistoryEventTypeEnum.CHARGE, -1);
                 				if(history != null){
                 						long remainingAmount = history.getRemainingAmount();
                 						remainingAmount += chargedAmount;
                 						record.setRemainingAmount(remainingAmount);
                 				}
                 			}
                     
                 	}catch (HomeException e) {
     					// TODO Auto-generated catch block
                 		LogSupport.minor(parentCtx,SubscriberSubscriptionHistorySupport.class, "Some thing Went wrong while calculating the remaining amount");
     					e.printStackTrace();
     				}
                 }
     		}
 		    	if (LogSupport.isDebugEnabled(parentCtx))
 		        {
 		            new DebugLogMsg(
 		                    "SubscriberSubscriptionHistorySupport",
 		                    "Calculating the reaming amount end in refund scenarios"
 		                    + sub
 		                    + ": "
 		                    + eventType,
 		                    null).log(parentCtx);
 		        } 
     }
     
     
     
    
	/**
	 * Create and Subscriber Subscription record to denote Subscription
	 * charging/refunding,
	 * with the given information.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param item
	 *            Service to be charged/refunded.
	 * @param account
	 *            Subscriber to be charged/refunded.
	 * @param eventType
	 *            Only Charge or Refund Accepted
	 * @param chargedItemType
	 *            To distinguish between Services, Bundles, Auxiliary Services,
	 *            and Service Packages
	 * @param chargedAmount
	 *            Charge/refund amount.
	 * @param originalFullFee
	 *            ServiceFee to charge/refund.
	 * @param transaction
	 *            If null, indicates no transaction was successfully created for
	 *            this charge/refund.
	 *            Otherwise, it is the reference for the transaction
	 *            representing this charge/refund.
	 */
	public static void addChargingHistory(final Context ctx, final Object item,
	    final Account account, final HistoryEventTypeEnum eventType,
	    final ChargedItemTypeEnum chargedItemType, final long chargedAmount,
	    final Transaction transaction,
	    final Date billingDate)
	{
		// Validation
		if (!eventType.equals(HistoryEventTypeEnum.CHARGE)
		    && !eventType.equals(HistoryEventTypeEnum.REFUND))
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport
				    .debug(
				        ctx,
				        "SubscriberSubscriptionHistorySupport.addChargingHistory",
				        "Wrong usage of this method.  The Event Type must be either Charge or Refund.");
			}
			return;
		}

		ServiceStateEnum state;

		/*
		 * Once we get around to supporting a single State Enum for
		 * Service/Bundles/Auxiliary Services
		 * we can properly fill in this data. For now we will assume that a
		 * charge means the
		 * Bundle/Auxiliary service is provisioned, while a refund means that it
		 * was unprovisioned.
		 */
		if (eventType.equals(HistoryEventTypeEnum.CHARGE))
		{
			state = ServiceStateEnum.PROVISIONED;
		}
		else
		{
			// Refund
			state = ServiceStateEnum.UNPROVISIONED;
		}

		// Add a Subscriber Subscription History record regardless of whether
		// the charge/refund was $0
		SubscriberSubscriptionHistorySupport
		    .addChargingHistory(ctx, account, eventType, chargedItemType, item,
		        state, chargedAmount, billingDate);
	}

	/**
	 * Create a Subscriber Subscription record to denote Account-level
	 * charging/refunding,
	 * with the given information.
	 * 
	 * @param context
	 * @param account
	 *            Account who is provisioning the item
	 * @param itemType
	 *            ChargedItemTypeEnum
	 * @param item
	 *            identifier of the item being provisioned
	 * @param state
	 *            state of the service provisioning to the external client
	 * @param chargedAmount
	 *            the amount charged/refunded
	 */
	private static void addChargingHistory(final Context parentCtx,
	    final Account account, final HistoryEventTypeEnum eventType,
	    final ChargedItemTypeEnum itemType, final Object item,
	    final ServiceStateEnum state, final long chargedAmount,
	    final Date billingDate)
	{
		Context context = parentCtx.createSubContext();
		context.put(Account.class, account);

		SubscriberSubscriptionHistory record =
		    new SubscriberSubscriptionHistory();
		inputData(context, account.getBAN(), eventType, itemType, item, state,
		    billingDate, record);
		try
		{
			// Fill in Charging-only data
			record.setChargedSubscriber(account.getBAN());
			record.setChargedAmount(chargedAmount);
			record.setServiceFee(chargedAmount);
			record.setRemainingAmount(chargedAmount);
			
			CRMSpid spid = (CRMSpid) context.get(CRMSpid.class);
			final int billCycleDay = account.getBillCycleDay(context);

			if (spid == null || spid.getSpid() != account.getSpid())
			{
				spid = SpidSupport.getCRMSpid(context, account.getSpid());
			}

			ServicePeriodHandler handler = getHandler(context, item, itemType);

			final Date startDate =
			    handler.calculateCycleStartDate(context, billingDate,
			        billCycleDay, spid.getId(), account.getBAN(), item);

			record.setLastBillCycleDate(startDate);

			createRecord(context, record);
		}
		catch (HomeException e)
		{
			new MajorLogMsg(
			    "SubscriberSubscriptionHistorySupport.addChargingHistory",
			    "Failed to create Subscriber Subscription History record to denote charging. "
			        + record, e).log(context);
		}
	}

    private static ServicePeriodHandler getHandler(Context ctx, Object item, ChargedItemTypeEnum itemType) throws HomeException
    {
        switch (itemType.getIndex())
        {
            case ChargedItemTypeEnum.SERVICE_INDEX:
                if (item instanceof Service)
                {
                    ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(((Service)item).getChargeScheme());
                    return handler;
                }
                else
                {
                    ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(((ServiceFee2)item).getServicePeriod());
                    return handler;
                }
            case ChargedItemTypeEnum.AUXSERVICE_INDEX:
                if (item instanceof AuxiliaryService)
                {
                    ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(((AuxiliaryService)item).getChargingModeType());
                    return handler;
                }
                else
                {
                    ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(((SubscriberAuxiliaryService)item).getAuxiliaryService(ctx).getChargingModeType());
                    return handler;
                }
            case ChargedItemTypeEnum.BUNDLE_INDEX:
            case ChargedItemTypeEnum.AUXBUNDLE_INDEX:
                if (item instanceof BundleProfile)
                {
                    return ServicePeriodSupportHelper.get(ctx).getHandler(ServicePeriodEnum.MONTHLY);
                }
                else
                {
                    ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(((BundleFee)item).getServicePeriod());
                    return handler;
                }
            case ChargedItemTypeEnum.SERVICEPACKAGE_INDEX:
                if (item instanceof ServicePackage)
                {
                    ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(((ServicePackage)item).getChargingMode());
                    return handler;
                }
                else
                {
                    ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(((ServicePackageFee)item).getServicePeriod());
                    return handler;
                }
			case ChargedItemTypeEnum.LATEFEE_INDEX:
			case ChargedItemTypeEnum.EARLYREWARD_INDEX:
				ServicePeriodHandler handler =
				    ServicePeriodSupportHelper.get(ctx).getHandler(
				        ServicePeriodEnum.MONTHLY);
				return handler;
            default:
                // Don't know how to retrieve the Item Identifier
                return ServicePeriodSupportHelper.get(ctx).getHandler(ServicePeriodEnum.MONTHLY);
        }
    }

    private static SubscriberSubscriptionHistory inputData(final Context context,
            final String subscriberId,
            final HistoryEventTypeEnum eventType,
            final ChargedItemTypeEnum itemType,
            final Object item,
            final ServiceStateEnum state, SubscriberSubscriptionHistory record)
    {
        return inputData(context, subscriberId, eventType, itemType, item, state, new Date(), record);
    }

        /**
     * Given the basic information, return a Subscriber Subscription History record with
     * the data filled in.
     * @param context
     * @param subscriberId Subscriber identifier
     * @param itemType ChargedItemTypeEnum
     * @param item identifier of the item being provisioned
     * @param state state of the service provisioning to the external client
     * @return
     */
    private static SubscriberSubscriptionHistory inputData(final Context context,
            final String subscriberId,
            final HistoryEventTypeEnum eventType,
            final ChargedItemTypeEnum itemType,
            final Object item,
            final ServiceStateEnum state,
            final Date billingDate, SubscriberSubscriptionHistory record)
    {

        if (billingDate != null)
        {
            record.setTimestamp_(billingDate);
        }
        else
        {
            record.setTimestamp_(new Date());
        }
        record.setSubscriberId(subscriberId);
        record.setEventType(eventType);
        record.setServiceState(state);

        /* For Provisioning, only mark the "event success" as either a success or a failure.
         * The resolved state is reserved for Charging. */

        try
        {
            if (SubscriberServicesSupport.ERROR_STATES.getByIndex(state.getIndex()) != null)
            {
                record.setEventSuccess(EventSuccessEnum.FAILURE);
            }
            else
            {
                record.setEventSuccess(EventSuccessEnum.SUCCESS);
            }
        }
        catch (java.lang.ArrayIndexOutOfBoundsException e)
        {
            // The Provisioned State is not one of the Error States.
            record.setEventSuccess(EventSuccessEnum.SUCCESS);
        }

        // Set the Item Type and Item Id
        record.setItemType(itemType);
        try
        {
        	if (LogSupport.isDebugEnabled(context))
			{
				LogSupport
				    .debug(
				    		context,
				        "SubscriberSubscriptionHistorySupport.addChargingHistory",
				        "item instance of " + item.getClass());
			}
            if (item instanceof SubscriberAuxiliaryService)
            {
                record = fillInAuxiliaryServiceRecord((SubscriberAuxiliaryService) item, record);
            }
            else if (item instanceof ServiceFee2)
			{
				record.setItemIdentifier(((ServiceFee2)item).getServiceId());
				record.setPath(((ServiceFee2)item).getPath());
			}
            else if (item instanceof SubscriberServices)
			{
				record.setItemIdentifier(((SubscriberServices)item).getServiceId());
				record.setPath(((SubscriberServices)item).getPath());
			}
			else if (item instanceof Number)
			{
				record.setItemIdentifier(((Number) item).longValue());
			}
            else
            {
                final Object key = XBeans.instantiate(item.getClass().getName(), context);
                final FacetMgr fMgr = XBeans.getFacetMgr(context);
                final IdentitySupport idSupport = (IdentitySupport) fMgr.getInstanceOf(context, key.getClass(),
                        IdentitySupport.class);
                record.setItemIdentifier(((Number)idSupport.ID(item)).longValue());
            }
        }
        catch (Exception e)
        {
            record.setItemIdentifier(decipherIdentifier(itemType, item));
        }
        
        record.setAgent(MsisdnManagement.getUserId(context));
        return record;
    }


    /**
     * Sets the identifier and secondary identifier for auxiliary service.
     *
     * @param item
     *            Subscriber auxiliary service association.
     * @param record
     *            History record.
     * @return The updated history record.
     */
    private static SubscriberSubscriptionHistory fillInAuxiliaryServiceRecord(final SubscriberAuxiliaryService item,
            final SubscriberSubscriptionHistory record)
    {
        record.setItemIdentifier(item.getAuxiliaryServiceIdentifier());
        record.setItemSecondaryIdentifier(item.getSecondaryIdentifier());
        return record;
    }

    /**
     * Fall back on determining the item identifier extraction by considering the item type.
     * @param itemType
     * @param item
     * @return
     */
    public static long decipherIdentifier(
            final ChargedItemTypeEnum itemType,
            final Object item)
    {
        switch (itemType.getIndex())
        {
            case ChargedItemTypeEnum.SERVICE_INDEX:
                if (item instanceof Service)
                {
                    return((Service)item).getID();
                }
                else
                {
                    return((ServiceFee2)item).getServiceId();
                }
            case ChargedItemTypeEnum.AUXSERVICE_INDEX:
                if (item instanceof AuxiliaryService)
                {
                    return((AuxiliaryService)item).getIdentifier();
                }
                else
                {
                    return((SubscriberAuxiliaryService)item).getAuxiliaryServiceIdentifier();
                }
            case ChargedItemTypeEnum.BUNDLE_INDEX:
            case ChargedItemTypeEnum.AUXBUNDLE_INDEX:
                if (item instanceof BundleProfile)
                {
                    return ((BundleProfile)item).getBundleId();
                }
                else
                {
                    return ((BundleFee)item).getId();
                }
            case ChargedItemTypeEnum.SERVICEPACKAGE_INDEX:
                if (item instanceof ServicePackage)
                {
                    return ((ServicePackage)item).getIdentifier();
                }
                else
                {
                    return ((ServicePackageFee)item).getPackageId();
                }
            case ChargedItemTypeEnum.PREPAYMENTCREDIT_INDEX:
                if (item instanceof com.redknee.app.crm.contract.SubscriptionContractTerm)
                {
                    return  ((com.redknee.app.crm.contract.SubscriptionContractTerm) item).getId();
                }
                else
                {
                    return ((com.redknee.app.crm.contract.SubscriptionContract) item).getContractId();
                }
            		
            default:
                // Don't know how to retrieve the Item Identifier
                return 0L;
        }
    }

    public static boolean isChargeable(final Context context, final String subscriberId,
            final ChargedItemTypeEnum itemType, final Object item, final Date billingDate, final Date startDate,
            final Date endDate) throws HomeException
    {
    	LongHolder itemFee = new LongHolder(0);
    	return isChargeable(context, subscriberId, itemType, item, billingDate, startDate, endDate, itemFee);
    }

    public static boolean isChargeable(final Context context, final String subscriberId,
            final ChargedItemTypeEnum itemType, final Object item, final Date billingDate, final Date startDate,
            final Date endDate, final LongHolder itemFee) throws HomeException
    {
        boolean result = false;
        final SubscriberSubscriptionHistory history = getLastChargingEventBetween(context, subscriberId, itemType,
                item, startDate, endDate);
        if (LogSupport.isDebugEnabled(context))
        {
            LogSupport.debug(context, SubscriberSubscriptionHistorySupport.class.getName(),
                    "History fetched for subscriber : " + subscriberId + ", " + history);
        }
        //Code Added for UMP-186
        ChargableItemResult chargableItemResult = (ChargableItemResult)context.get(ChargableItemResult.class);
        if(chargableItemResult!=null){
        	if (LogSupport.isDebugEnabled(context)) {
        	 LogSupport.debug(context, SubscriberSubscriptionHistorySupport.class.getName(),"Inside chargableItemResult and value:: "+chargableItemResult.getSkipValidation());
        	}
        	if(chargableItemResult.getSkipValidation()){
        		return true;
        	}
        }
        /*
         * If there is no transaction or last transaction is equals refund, means item is chargeable. On the other hand,
         * if there is a charge or refund after the billingDate, don't charge again.
         */
        if (history == null)
        {
            result = true;
        }
        else if (!history.getTimestamp_().before(billingDate))
        {
            result = false;
            itemFee.setValue(history.getServiceFee());
        }
        else if (history.getEventSuccess() != EventSuccessEnum.FAILURE && HistoryEventTypeEnum.REFUND.equals(history.getEventType()))
        {
            result = true;
        }
        else
        {
            itemFee.setValue(history.getServiceFee());
        }

        return result;
    }

    public static boolean isRefundedSince(final Context context, final String subscriberId,
            final ChargedItemTypeEnum itemType, final Object item, final Date date) throws HomeException
    {
        final Set<HistoryEventTypeEnum> eventType = new HashSet<HistoryEventTypeEnum>();
        eventType.add(HistoryEventTypeEnum.REFUND);
        return getLastEventSince(context, subscriberId, itemType, item, date, eventType) != null;
    }


    public static boolean isChargedSince(final Context context, final String subscriberId,
            final ChargedItemTypeEnum itemType, final Object item, final Date date) throws HomeException
    {
        final Set<HistoryEventTypeEnum> eventType = new HashSet<HistoryEventTypeEnum>();
        eventType.add(HistoryEventTypeEnum.CHARGE);
        return getLastEventSince(context, subscriberId, itemType, item, date, eventType) != null;
    }

    public static boolean isRefundedBetween(final Context context, final String subscriberId,
            final ChargedItemTypeEnum itemType, final Object item, final Date startDate, final Date endDate)
        throws HomeException
    {
        final Set<HistoryEventTypeEnum> eventType = new HashSet<HistoryEventTypeEnum>();
        eventType.add(HistoryEventTypeEnum.REFUND);
        return getLastEventBetween(context, subscriberId, itemType, item, startDate, endDate, eventType,true) != null;
    }


    public static boolean isChargedBetween(final Context context, final String subscriberId,
            final ChargedItemTypeEnum itemType, final Object item, final Date startDate, final Date endDate)
        throws HomeException
    {
        final Set<HistoryEventTypeEnum> eventType = new HashSet<HistoryEventTypeEnum>();
        eventType.add(HistoryEventTypeEnum.CHARGE);
        return getLastEventBetween(context, subscriberId, itemType, item, startDate, endDate, eventType, true) != null;
    }


    public static SubscriberSubscriptionHistory getLastChargingEventSince(final Context context,
            final String subscriberId, final ChargedItemTypeEnum itemType, final Object item, final Date date)
        throws HomeException
    {
        final Set<HistoryEventTypeEnum> eventTypes = new HashSet<HistoryEventTypeEnum>();
        eventTypes.add(HistoryEventTypeEnum.CHARGE);
        eventTypes.add(HistoryEventTypeEnum.REFUND);
        return getLastEventSince(context, subscriberId, itemType, item, date, eventTypes);
    }

    public static SubscriberSubscriptionHistory getLastEventSince(final Context context, final String subscriberId,
            final ChargedItemTypeEnum itemType, final Object item, final Date date,
            final Set<HistoryEventTypeEnum> eventTypes) throws HomeException
    {
        final Home home = new SortingHome(context, new DescendingHistoryComparator(),
                (Home) context.get(SubscriberSubscriptionHistoryHome.class));
        final And and = constructPredicate(subscriberId, item, date);
        final Collection<SubscriberSubscriptionHistory> items;
        items = home.select(context, and);
        if (items != null && !items.isEmpty())
        {
            for (final Iterator<SubscriberSubscriptionHistory> iterator = items.iterator(); iterator.hasNext();)
            {
                final SubscriberSubscriptionHistory history = iterator.next();
                if (eventTypes.contains(history.getEventType()))
                {
                    return history;
                }
            }
        }
        return null;
    }

    public static Collection<SubscriberSubscriptionHistory> getChargingEventsSince(final Context context,
            final String subscriberId, final Date date)
        throws HomeException
    {
        final Set<HistoryEventTypeEnum> eventTypes = new HashSet<HistoryEventTypeEnum>();
        eventTypes.add(HistoryEventTypeEnum.CHARGE);
        eventTypes.add(HistoryEventTypeEnum.REFUND);
        final Set<ChargedItemTypeEnum> itemTypes = new HashSet<ChargedItemTypeEnum>();
        itemTypes.add(ChargedItemTypeEnum.SERVICE);
        itemTypes.add(ChargedItemTypeEnum.AUXSERVICE);
        itemTypes.add(ChargedItemTypeEnum.BUNDLE);
        itemTypes.add(ChargedItemTypeEnum.AUXBUNDLE);
        return getEventsSince(context, subscriberId, itemTypes, date, eventTypes);
    }
    
    @SuppressWarnings("unchecked")
    public static Collection<SubscriberSubscriptionHistory> getEventsSince(final Context context, final String subscriberId,
            final Set<ChargedItemTypeEnum> itemTypes, final Date date, final Set<HistoryEventTypeEnum> eventTypes) 
        throws HomeException
    {
        final Home home = new SortingHome(context, new DescendingHistoryComparator(),
                (Home) context.get(SubscriberSubscriptionHistoryHome.class));
        
        final And and = new And();
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.SUBSCRIBER_ID, subscriberId));
        and.add(new In(SubscriberSubscriptionHistoryXInfo.ITEM_TYPE, itemTypes));
        addStartDate(and, date);
        
        Collection<SubscriberSubscriptionHistory> items;
        items = home.select(context, and);
        if (items!= null && !items.isEmpty())
        {
            items = CollectionSupportHelper.get(context).findAll(context,  items, new In(SubscriberSubscriptionHistoryXInfo.EVENT_TYPE, eventTypes));
        }
        if (LogSupport.isDebugEnabled(context))
        {
            LogSupport.debug(context,  SubscriberSubscriptionHistorySupport.class.getName(), "Charging history found: " + String.valueOf(items));
        }
        
        return items;
    }
    
    @SuppressWarnings("unchecked")
    public static Collection<SubscriberSubscriptionHistory> getEventsBetween(final Context context, final String subscriberId,
            final Set<ChargedItemTypeEnum> itemTypes, final Date startDate, final Date endDate, final Set<HistoryEventTypeEnum> eventTypes,
            final Service service) 
        throws HomeException
    {
        final Home home = new SortingHome(context, new DescendingHistoryComparator(),
                (Home) context.get(SubscriberSubscriptionHistoryHome.class));
        
        final And and = new And();
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.SUBSCRIBER_ID, subscriberId));
        and.add(new In(SubscriberSubscriptionHistoryXInfo.ITEM_TYPE, itemTypes));
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_IDENTIFIER,service.getIdentifier()));
        addStartDate(and, startDate);
        addEndDate(and, endDate);
        
        Collection<SubscriberSubscriptionHistory> items;
        items = home.select(context, and);
        if (items!= null && !items.isEmpty())
        {
            items = CollectionSupportHelper.get(context).findAll(context,  items, new In(SubscriberSubscriptionHistoryXInfo.EVENT_TYPE, eventTypes));
        }
        if (LogSupport.isDebugEnabled(context))
        {
            LogSupport.debug(context,  SubscriberSubscriptionHistorySupport.class.getName(), "Charging history found: " + String.valueOf(items));
        }
        
        return items;
    }
    
    @SuppressWarnings("unchecked")
    public static Collection<SubscriberSubscriptionHistory> getEventsBetweenForAuxService(final Context context, final String subscriberId,
            final Set<ChargedItemTypeEnum> itemTypes, final Date startDate, final Date endDate, final Set<HistoryEventTypeEnum> eventTypes,
            final AuxiliaryService auxService,final long secondarIdentifier) 
        throws HomeException
    {
        final Home home = new SortingHome(context, new DescendingHistoryComparator(),
                (Home) context.get(SubscriberSubscriptionHistoryHome.class));
        
        final And and = new And();
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.SUBSCRIBER_ID, subscriberId));
        and.add(new In(SubscriberSubscriptionHistoryXInfo.ITEM_TYPE, itemTypes));
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_IDENTIFIER,auxService.getID()));
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_SECONDARY_IDENTIFIER,secondarIdentifier));
        addStartDate(and, startDate);
        addEndDate(and, endDate);
        
        Collection<SubscriberSubscriptionHistory> items;
        items = home.select(context, and);
        if (items!= null && !items.isEmpty())
        {
            items = CollectionSupportHelper.get(context).findAll(context,  items, new In(SubscriberSubscriptionHistoryXInfo.EVENT_TYPE, eventTypes));
        }
        if (LogSupport.isDebugEnabled(context))
        {
            LogSupport.debug(context,  SubscriberSubscriptionHistorySupport.class.getName(), "Charging history found: " + String.valueOf(items));
        }
        
        return items;
    }
    
    public static Collection<SubscriberSubscriptionHistory> findEventType(final Context context, 
            final Collection<SubscriberSubscriptionHistory> items, final HistoryEventTypeEnum eventType)
    {
        return CollectionSupportHelper.get(context).findAll(context,  items, new EQ(SubscriberSubscriptionHistoryXInfo.EVENT_TYPE, eventType));
    }

    public static boolean isLastRefundedBetween(final Context context, final String subscriberId,
            final ChargedItemTypeEnum itemType, final Object item, final Date startDate, final Date endDate)
        throws HomeException
    {
        final HistoryEventTypeEnum eventType = getLastChargingEventTypeBetween(context, subscriberId, itemType, item,
            startDate, endDate);
        return HistoryEventTypeEnum.REFUND == eventType;
    }


    public static boolean isLastChargedBetween(final Context context, final String subscriberId,
            final ChargedItemTypeEnum itemType, final Object item, final Date startDate, final Date endDate)
        throws HomeException
    {
        final HistoryEventTypeEnum eventType = getLastChargingEventTypeBetween(context, subscriberId, itemType, item,
            startDate, endDate);
        return HistoryEventTypeEnum.CHARGE == eventType;
    }


    public static HistoryEventTypeEnum getLastChargingEventTypeBetween(final Context context,
            final String subscriberId, final ChargedItemTypeEnum itemType, final Object item, final Date startDate,
            final Date endDate) throws HomeException
    {
        final SubscriberSubscriptionHistory history = getLastChargingEventBetween(context, subscriberId, itemType,
            item, startDate, endDate);
        if (history != null)
        {
            return history.getEventType();
        }
        return null;
    }
    
    /*
     * Product catalogue: 10.1.3 
     * This method will get the latest record of charging/refunding 
     * for a particular service/Aux. Service
     * Whenever service fee gets personalized, We need to refund the previous transaction
     * and charge for the new amount. For charging we need to know the last transaction detail. 
     * 
     */
    public static SubscriberSubscriptionHistory getLatestHistoryForItem(final Context context,
            final String subId, final ChargedItemTypeEnum itemType, final long itemIdentifier,
            final HistoryEventTypeEnum eventType, long secondaryIdentifier) throws HomeException
        {
            final And and = new And();
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.SUBSCRIBER_ID, subId));
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_TYPE, itemType));
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_IDENTIFIER, itemIdentifier));
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.EVENT_TYPE, eventType));  
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_SECONDARY_IDENTIFIER, secondaryIdentifier));
            
            //and.add(new Order().add(SubscriberSubscriptionHistoryXInfo.TIMESTAMP_, false));
            
            List<SubscriberSubscriptionHistory> list =
                    (List<SubscriberSubscriptionHistory>) getRecords(context, and);
            if(list != null && !list.isEmpty())
            {
                return list.get(0);
            }
            else
            {
                return null;
            }
        }
    
    /*
     * Payer Payee: 10.1.7 
     * This method will get the latest record of charging/refunding 
     * for a particular service/Aux. Service
     * Whenever service fee gets personalized, We need to refund the previous transaction
     * and charge for the new amount. For charging we need to know the last transaction detail. 
     * 
     */
    public static SubscriberSubscriptionHistory getLatestHistoryForItem(final Context context,
            final String subId, final ChargedItemTypeEnum itemType, final long itemIdentifier,
            final HistoryEventTypeEnum eventType, long secondaryIdentifier, ServiceStateEnum serviceState) throws HomeException
        {
            final And and = new And();
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.SUBSCRIBER_ID, subId));
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_TYPE, itemType));
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_IDENTIFIER, itemIdentifier));
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.EVENT_TYPE, eventType)); 
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.SERVICE_STATE, serviceState));
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_SECONDARY_IDENTIFIER, secondaryIdentifier));
            
            and.add(new Order().add(SubscriberSubscriptionHistoryXInfo.TIMESTAMP_, false));
            
            List<SubscriberSubscriptionHistory> list =
                    (List<SubscriberSubscriptionHistory>) getRecords(context, and);
            if(list != null && !list.isEmpty())
            {
                return list.get(0);
            }
            else
            {
                return null;
            }
        }


    public static SubscriberSubscriptionHistory getLastChargingEventBetween(final Context context,
            final String subscriberId, final ChargedItemTypeEnum itemType, final Object item, final Date startDate,
            final Date endDate) throws HomeException
    {
        final Set<HistoryEventTypeEnum> eventTypes = new HashSet<HistoryEventTypeEnum>();
        eventTypes.add(HistoryEventTypeEnum.CHARGE);
        eventTypes.add(HistoryEventTypeEnum.REFUND);
        return getLastEventBetween(context, subscriberId, itemType, item, startDate, endDate, eventTypes, true);
    }

    public static SubscriberSubscriptionHistory getLastEventBetween(final Context context,
            final String subscriberId, final ChargedItemTypeEnum itemType, final Object item, final Date startDate,
            final Date endDate, final Set<HistoryEventTypeEnum> eventTypes, boolean isChargingEvents) throws HomeException
    {
        Home home = new SortingHome(context, new DescendingHistoryComparator(),
                (Home) context.get(SubscriberSubscriptionHistoryHome.class));
        if (!isChargingEvents)
        {
            home = new SortingHome(context, new DescendingHistoryComparator(),
                    (Home) context.get(SubscriptionProvisioningHistoryHome.class));            
        }
        
		final And and =
		    constructPredicate(subscriberId, itemType, item, startDate, endDate);
        final Collection<SubscriberSubscriptionHistory> items;
        items = home.select(context, and);

        if (items != null && !items.isEmpty())
        {
            for (final Iterator<SubscriberSubscriptionHistory> iterator = items.iterator(); iterator.hasNext();)
            {
                final SubscriberSubscriptionHistory history = iterator.next();
                if (eventTypes.contains(history.getEventType()))
                {
                    return history;
                }
            }
        }
        return null;
    }
    
    public static void updateSubscriberSubscriptionHistoryEvent(final Context context, SubscriberSubscriptionHistory history)
    {
        Home home = (Home) context.get(SubscriberSubscriptionHistoryHome.class);
        try
        {
            home.store(context, history);
        }
        catch (HomeException e)
        {
            LogSupport.major(context, SubscriberSubscriptionHistorySupport.class.getName(),
                    "Failed to update Subscriber subscription provisoning history : " + history);
        }
    }
    
    /*
     * Added for fetching auxliliary service's charge/refund history between 
     * two dates when SubAuxService Entry is not available
     */
    
    public static SubscriberSubscriptionHistory getLastChargingEventBetweenForAux(final Context context,
            final String subscriberId, final ChargedItemTypeEnum itemType, final Object item, final Date startDate,
            final Date endDate,final long secondaryIdentifier) throws HomeException
    {
        final Set<HistoryEventTypeEnum> eventTypes = new HashSet<HistoryEventTypeEnum>();
        eventTypes.add(HistoryEventTypeEnum.CHARGE);
        eventTypes.add(HistoryEventTypeEnum.REFUND);
        return getLastEventBetweenForAux(context, subscriberId, itemType, item, startDate, endDate, eventTypes, true, secondaryIdentifier);
    }

    public static SubscriberSubscriptionHistory getLastEventBetweenForAux(final Context context,
            final String subscriberId, final ChargedItemTypeEnum itemType, final Object item, final Date startDate,
            final Date endDate, final Set<HistoryEventTypeEnum> eventTypes, boolean isChargingEvents,final long secondaryIdentifier) throws HomeException
    {
        Home home = new SortingHome(context, new DescendingHistoryComparator(),
                (Home) context.get(SubscriberSubscriptionHistoryHome.class));
        if (!isChargingEvents)
        {
            home = new SortingHome(context, new DescendingHistoryComparator(),
                    (Home) context.get(SubscriptionProvisioningHistoryHome.class));            
        }
        
		final And and =
		    constructPredicate(subscriberId, itemType, item, startDate, endDate);
		and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_SECONDARY_IDENTIFIER, secondaryIdentifier));
        final Collection<SubscriberSubscriptionHistory> items;
        items = home.select(context, and);

        if (items != null && !items.isEmpty())
        {
            for (final Iterator<SubscriberSubscriptionHistory> iterator = items.iterator(); iterator.hasNext();)
            {
                final SubscriberSubscriptionHistory history = iterator.next();
                if (eventTypes.contains(history.getEventType()))
                {
                    return history;
                }
            }
        }
        return null;
    }

    private static ChargedItemTypeEnum getChargedItemType(final Object item)
    {
        ChargedItemTypeEnum result = ChargedItemTypeEnum.SERVICE;
        if (item instanceof AuxiliaryService || item instanceof SubscriberAuxiliaryService)
        {
            result = ChargedItemTypeEnum.AUXSERVICE;
        }
        else if (item instanceof BundleProfile)
        {
            if (((BundleProfile) item).isAuxiliary())
            {
                result = ChargedItemTypeEnum.AUXBUNDLE;
            }
            else
            {
                result = ChargedItemTypeEnum.BUNDLE;
            }
        }
        else if (item instanceof BundleFee)
        {
            if (((BundleFee) item).isAuxiliarySource())
            {
                result = ChargedItemTypeEnum.AUXBUNDLE;
            }
            else
            {
                result = ChargedItemTypeEnum.BUNDLE;
            }
        }
        else if (item instanceof ServicePackage)
        {
            result = ChargedItemTypeEnum.SERVICEPACKAGE;
        }
        else if ((item instanceof SubscriptionContractTerm) || (item instanceof SubscriptionContractTerm))
        {
            result = ChargedItemTypeEnum.PREPAYMENTCREDIT;
        }
        return result;
    }


    private static And constructPredicate(final String subscriberId, final Object item, final Date date)
    {
        And and;
        if (item instanceof SubscriberAuxiliaryService)
        {
            final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) item;
            and = constructPredicate(ChargedItemTypeEnum.AUXSERVICE, association.getAuxiliaryServiceIdentifier(),
                    association.getSecondaryIdentifier());
        }
        else
        {
            final ChargedItemTypeEnum itemType = getChargedItemType(item);
            and = constructPredicate(itemType, decipherIdentifier(itemType, item));
        }

        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.SUBSCRIBER_ID, subscriberId));

        return addStartDate(and, date);
    }


	private static And constructPredicate(final String subscriberId,
	    ChargedItemTypeEnum itemType, final Object item, final Date startDate,
            final Date endDate)
    {
        And and;
        if (item instanceof SubscriberAuxiliaryService)
        {
            final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) item;
            and = constructPredicate(ChargedItemTypeEnum.AUXSERVICE, association.getAuxiliaryServiceIdentifier(),
                association.getSecondaryIdentifier());
        }
		else if (item instanceof Integer)
		{
			and = constructPredicate(itemType, ((Number) item).longValue());
		}
        else
        {
			final ChargedItemTypeEnum determinedItemType =
			    getChargedItemType(item);
			and =
			    constructPredicate(determinedItemType,
			        decipherIdentifier(itemType, item));
        }

        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.SUBSCRIBER_ID, subscriberId));

        and = addStartDate(and, startDate);
        return addEndDate(and, endDate);
    }


    private static And addStartDate(final And and, final Date startDate)
    {
        and.add(new GTE(SubscriberSubscriptionHistoryXInfo.TIMESTAMP_, startDate));
        return and;
    }


    private static And addEndDate(final And and, final Date endDate)
    {
        if(endDate != null)
        {
            and.add(new LTE(SubscriberSubscriptionHistoryXInfo.TIMESTAMP_, endDate));
        }
        return and;
    }


    private static And constructPredicate(final ChargedItemTypeEnum itemType, final long identifier)
    {
        final And and = new And();
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_TYPE, itemType));
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_IDENTIFIER, Long.valueOf(identifier)));
        return and;
    }


    private static And constructPredicate(final ChargedItemTypeEnum itemType, final long identifier,
        final long secondaryIdentifier)
    {
        final And and = new And();
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_TYPE, itemType));
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_IDENTIFIER, Long.valueOf(identifier)));
        and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_SECONDARY_IDENTIFIER,
                Long.valueOf(secondaryIdentifier)));
        return and;
    }

    public static void createRecord(final Context context, SubscriberSubscriptionHistory record)
        throws HomeException
    {
        Home home = null;
        if ( record.getEventType() == HistoryEventTypeEnum.PROVISION || 
                record.getEventType() == HistoryEventTypeEnum.UNPROVISION)
        {
            home = (Home) context.get(SubscriptionProvisioningHistoryHome.class);
        }
        else
        {
            home = (Home) context.get(SubscriberSubscriptionHistoryHome.class);
        }
        
        home.create(context, record);
    }
    
  
    
}
