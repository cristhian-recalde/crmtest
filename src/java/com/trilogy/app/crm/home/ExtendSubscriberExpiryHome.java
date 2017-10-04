package com.trilogy.app.crm.home;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.trilogy.app.crm.bas.recharge.RechargeSubscriberServiceVisitor;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ExpiryExtensionModeEnum;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.exception.codemapping.S2100ReturnCodeMsgMapping;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriptionState;

/**
 * 
 * @author kabhay
 * It executes super.create() first.
 */
public class ExtendSubscriberExpiryHome extends HomeProxy 
{

    private static final long serialVersionUID = 1L;
    public static final String SKIP_PROVISIONING_AND_CHARGING_CALCULATIONS = "SKIP_PROVISIONING_AND_CHARGING_CALCULATIONS";
    public static final String IS_SUBSCRIPTION_ALREADY_EXTENDED_FOR_SUBSCRIBER = "IS_SUBSCRIPTION_ALREADY_EXTENDED_FOR_SUBSCRIBER";
    
    public ExtendSubscriberExpiryHome(Home delegate) 
    {
        super(delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException 
    {
        Object return_obj = null;
        try
        {
        	if( (Boolean) ctx.get(RechargeSubscriberServiceVisitor.ALL_OR_NOTHING_CHARGING_SUSPEND, Boolean.FALSE).equals(Boolean.FALSE))
        	{
        		return_obj = super.create(ctx, obj);
        	}
        	else
        	{
        		// Since it's verified that subscriber balance is insufficient to charge PickNPay group amount during MRC execution, 
        		// We dont wan't to create actual transaction and OCG call for this amount
        		// Just simulate OCG error due to insufficient balance to reset subscriber expiry date 
        		int result = com.redknee.product.s2100.ErrorCode.BALANCE_INSUFFICIENT;
        		
        		String msg = "Can not debit MSISDN " + ((Transaction)obj).getMSISDN() + " for PickNPay MRC group amount " + ((Transaction)obj).getAmount()
                        + ". ";
                msg += S2100ReturnCodeMsgMapping.getMessage(result);

                final OcgTransactionException exception = new OcgTransactionException(msg, result);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "forwardTransactionToOcg Error",
                        exception);
                }
                throw exception;
        	}
        }
        catch(HomeException e)
        {
            if ( e instanceof OcgTransactionException)
            {
                try
                {
                    updateSubscriptionExpiry(ctx, (Transaction)obj, false);
                }
                catch(Exception t)
                {
                    LogSupport.major(ctx, this, "Exception occurred while reseting subscribr expiry!",t);
                }                
            }
            throw e;
        }
        
        try
        {
            updateSubscriptionExpiry(ctx, (Transaction) obj, true);

        }
        catch (Exception e) 
        {
            LogSupport.major(ctx, this, "Exception occurred while extending subscribr expiry!", e);
        }
        
        return return_obj;
    }
    
    /**
     * To get the price plan of a subscriber
     * 
     * @param ctx
     * @param subscriber
     * @return
     * @throws HomeException
     */
    private PricePlan getPricePlan(Context ctx, Subscriber subscriber) throws HomeException
    {
        try
        {
            Home pricePlanHome = (Home) ctx.get(PricePlanHome.class);
            if (pricePlanHome != null)
            {
                PricePlan pp = (PricePlan) pricePlanHome.find(ctx, subscriber.getPricePlan());
                if (pp != null)
                {
                    return pp;
                }
                else
                {
                    throw new HomeException("Could not find price-plan for ID :" + subscriber.getPricePlan());
                }
            }
            else
            {
                throw new HomeException("Could not find PricePlanHome in context!!");
            }
        }
        catch (HomeException e)
        {
            throw e;
        }
    }

    /**
     * updateSubscriptionExpiry
     * 
     * @param ctx
     * @param txn
     * @param isSuccessful
     * @throws Exception
     */
    private void updateSubscriptionExpiry(Context ctx, Transaction txn, boolean isSuccessful) throws Exception
    {
        if (!AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, txn.getAdjustmentType(), AdjustmentTypeEnum.Services) )
        {
            /* Expiry extension is applicable for price plan service only. */
            return;
        }
        
        //TODO : optimize it further
        if (txn.getAmount() <= 0 )
        {
            //no extension for credit or zero charge
            return;
        }
        
        Service service = ServiceSupport.getServiceByAdjustment(ctx, txn.getAdjustmentType());
        if (service == null)
        {
            /* It may be aux-service or bundle or something else */
            return;
        }
        
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        if (sub == null)
        {
            sub = SubscriberSupport.getSubscriber(ctx, txn.getSubscriberID());
        }
        
        PricePlanVersion pricePlan = sub.getPricePlan(ctx);
        Map<ServiceFee2ID, ServiceFee2> serviceFees = pricePlan.getServiceFees(ctx);
        
        ServiceFee2 serviceFee2 = serviceFees.get(new ServiceFee2ID(service.getID(), SubscriberServicesUtil.DEFAULT_PATH));
        if (serviceFee2 == null)
        {
            throw new Exception("Could not find service-fee2 for service-ID : " + service.getID());
        }
        
        /* if PREPAID subscriber and debit transaction then update expiry of subscriber */
        if (!sub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID) )
        {
            return;
        }
        //The code should be put in a cleaner way. It would require a design change like a state transition model and hence adding a flag as of now.
        Context subCtx = ctx.createSubContext();
        subCtx.put(SKIP_PROVISIONING_AND_CHARGING_CALCULATIONS, Boolean.TRUE);
        
        Context appCtx = (Context) ctx.get("app");
        Object subExtensionEntry = appCtx.get(IS_SUBSCRIPTION_ALREADY_EXTENDED_FOR_SUBSCRIBER + sub.getId()); 
        if (!isSuccessful)
        {
            if (subExtensionEntry != null && ((Boolean) subExtensionEntry == Boolean.TRUE))
            {
                return;
            }
            resetSubscriberExpiryToCurrentDate(subCtx, sub);
        }
        else
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Extending subscriber expiry request received.");
            }
            if (subExtensionEntry != null && ((Boolean) subExtensionEntry == Boolean.TRUE))
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Skipping : Extending subscriber expiry.");
                }
                return;
            }
            extendSubscriberExpiry(subCtx, txn, sub,serviceFee2);
            appCtx.put(IS_SUBSCRIPTION_ALREADY_EXTENDED_FOR_SUBSCRIBER + sub.getId(), Boolean.TRUE);
             
        }
    }
    
    /**
     * resetSubscriberExpiryToCurrentDate
     * 
     * @param ctx
     * @param sub
     * @throws Exception
     */
    
    private void resetSubscriberExpiryToCurrentDate(Context ctx, Subscriber sub) throws Exception
    {
    	final Date now = new Date();
        SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
        if (client == null)
        {
            LogSupport.major(ctx, this, "Could not update expiry: SubscriberProfileProvisionClient is not found!");
            return;
        }
        PricePlan pricePlan = getPricePlan(ctx, sub);
        Date newExpiryDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(now);
        Calendar expiryCalendar = Calendar.getInstance();
        
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        PricePlan oldPricePlan = null;
        if(oldSub != null)
        {
        	oldPricePlan = getPricePlan(ctx, oldSub);
        }
        
        if(oldPricePlan != null && oldSub != null)
        {
        	if(PricePlanSubTypeEnum.PAYGO.equals(pricePlan.getPricePlanSubType()))
        	{
        		newExpiryDate = calculateSubscriberExpiryDateForPayGo(ctx, oldPricePlan, getRealTimeBlanace(ctx, client, sub), sub);
        	}
        	// -- PP change MRC-LifeTime, PayGo-LifeTime, LifeTime-LifeTime
        	else if(PricePlanSubTypeEnum.LIFETIME.equals(pricePlan.getPricePlanSubType()))
        	{
        		newExpiryDate = sub.getExpiryDate();
        	}
        	//-- PP change MRC-MRC, PayGo-MRC, LifeTime-MRC and Any-PickNPay
        	else if(PricePlanSubTypeEnum.MRC.equals(pricePlan.getPricePlanSubType()) || PricePlanSubTypeEnum.PICKNPAY.equals(pricePlan.getPricePlanSubType()))
        	{
        		newExpiryDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(now);
        	}
        }
        else if(oldPricePlan == null)
        {
        	//--MRC Fall back scenario
        	if(PricePlanSubTypeEnum.MRC.equals(pricePlan.getPricePlanSubType()))
        	{
        		long boltonService = SpidSupport.getCRMSpid(ctx, sub.getSpid()).getBoltOnForMRCToPaygoFallback();
        		long realTimeBalance = getRealTimeBlanace(ctx, client, sub);
        		if(Long.valueOf(pricePlan.getPaygoBalanceThreshold()) >0 && realTimeBalance > Long.valueOf(pricePlan.getPaygoBalanceThreshold())
        				&& pricePlan.getPaygoExtenssionDays() > 0 && boltonService != -100)
        		{
        			SubscriberAuxiliaryService subAuxSvc = SubscriberAuxiliaryServiceSupport.createAssociationForSubscriber(ctx, sub, 
        					boltonService, SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, AuxiliaryServiceTypeEnum.URCS_Promotion);
        			sub.setPaygoEnabled(true);
        			
        			new MinorLogMsg(this, "Bolt on service : " + subAuxSvc.getIdentifier()  + " has been provisioned to Subscriber " + sub.getId()
    						+ "and Paygo mode is enabled", null).log(ctx);
        			
        			expiryCalendar = Calendar.getInstance();
        			expiryCalendar.setTime(sub.getExpiryDate());
        			expiryCalendar.add(Calendar.DAY_OF_YEAR, (int)pricePlan.getPaygoExtenssionDays());
        			newExpiryDate  = expiryCalendar.getTime();

        			// Send PayGo mode Enabled notification to Subscriber
        			SubscriptionNotificationSupport.sendPayGoModeEnabledNotification(ctx, sub);
        		}
        	}
        	else if(PricePlanSubTypeEnum.PAYGO_INDEX == pricePlan.getPricePlanSubType().getIndex())
        	{
            	expiryCalendar.setTime(sub.getExpiryDate());
        		newExpiryDate  = expiryCalendar.getTime();
        	}
        	
        	else if(PricePlanSubTypeEnum.LIFETIME_INDEX == pricePlan.getPricePlanSubType().getIndex())
        	{
        		newExpiryDate = calculateSubscriberExpiryDateForLifeTime(ctx, sub);
        	}
        }
        if (!sub.getExpiryDate().after(now) && !sub.getExpiryDate().equals(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date(0))))
        {
            /* Don't reset ExpiryDate if it is not in future. 
             * TT#13021413063 : Also don't reset it on creation in activated state / on first activation of subscriber */
            return;
        }
        
        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Updating expiryDate to " + newExpiryDate  + " for Subscriber " + sub.getId());
        }
        
        /* Update in CPS */
        client.updateExpiryDate(ctx, sub, newExpiryDate);
        
        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Resetting / Extending subscriber expirty to : " + newExpiryDate + " from : " + sub.getExpiryDate());    
        }
        /* Update subscriber's expiry in BSS */
        sub.setExpiryDate(newExpiryDate);
        
        /* TT#12061422045 need cascade change to dcrm which is done in subscriber pipeline. */
        Home subHome = (Home)ctx.get(SubscriberHome.class);
        Subscriber clonedSub = (Subscriber) sub.clone();
        subHome.store(ctx,clonedSub);
    }
    
    /**
     * extendSubscriberExpiry
     * 
     * @param ctx
     * @param txn
     * @param sub
     * @param fee
     * @throws Exception
     */
    private void extendSubscriberExpiry(Context ctx, Transaction txn, Subscriber sub, ServiceFee2 fee ) throws Exception
    {
    	PricePlan newPricePlan = getPricePlan(ctx, sub);
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        PricePlan oldPricePlan = null;
        Date newExpiryDate = null;
        if(oldSub != null)
        {
        	oldPricePlan = getPricePlan(ctx, oldSub);
        }
        
        SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
        if (client == null)
        {
            LogSupport.major(ctx, this, "Could not extend expiry: SubscriberProfileProvisionClient not found!");
            return;
        }
        
        if(oldPricePlan == null)
        {
        	if(PricePlanSubTypeEnum.MRC.equals(newPricePlan.getPricePlanSubType()))
        	{
        		if(newPricePlan.getExpiryExtention() <= 0)
            	{
            		return;
            	}
        		else
        		{
        			newExpiryDate = calculateSubscriberExpiryDateForMRC(ctx, sub, newPricePlan, txn, fee, oldPricePlan);
        		}
        	}
        	else if(PricePlanSubTypeEnum.LIFETIME.equals(newPricePlan.getPricePlanSubType()))
        	{
        		newExpiryDate = calculateSubscriberExpiryDateForLifeTime(ctx, sub);
        	}
        	else if(PricePlanSubTypeEnum.PAYGO.equals(newPricePlan.getPricePlanSubType()))
        	{
        		return;
        	}
        	else if(PricePlanSubTypeEnum.PICKNPAY.equals(newPricePlan.getPricePlanSubType()))
        	{
        		newExpiryDate = calculateSubscriberExpiryDate(ctx, newPricePlan, txn, oldSub, fee, oldPricePlan);
        	}
        }
        
        if(oldPricePlan != null)
        {
        	if(oldPricePlan.getId() == newPricePlan.getId())
        	{
        		if(PricePlanSubTypeEnum.MRC.equals(newPricePlan.getPricePlanSubType()))
        		{
        			if(newPricePlan.getExpiryExtention() <= 0)
        			{
        				return;
        			}
        			else
        			{
        				newExpiryDate = calculateSubscriberExpiryDateForMRC(ctx, sub, newPricePlan, txn, fee, oldPricePlan);
        			}
        		}
        		else if(PricePlanSubTypeEnum.LIFETIME.equals(newPricePlan.getPricePlanSubType()))
        		{
        			newExpiryDate = calculateSubscriberExpiryDateForLifeTime(ctx, sub);
        		}
        		else if(PricePlanSubTypeEnum.PAYGO.equals(newPricePlan.getPricePlanSubType()))
        		{
        			return;
        		}
        		else if(PricePlanSubTypeEnum.PICKNPAY.equals(newPricePlan.getPricePlanSubType()))
            	{
            		newExpiryDate = calculateSubscriberExpiryDate(ctx, newPricePlan, txn, oldSub, fee, oldPricePlan);
            	}
        	}
        	else if(oldPricePlan.getId() != newPricePlan.getId())
        	{

        		if(PricePlanSubTypeEnum.LIFETIME.equals(newPricePlan.getPricePlanSubType()))
        		{
        			newExpiryDate = calculateSubscriberExpiryDateForLifeTime(ctx, sub);
        		}
        		else if(PricePlanSubTypeEnum.MRC.equals(newPricePlan.getPricePlanSubType()))
        		{
        			if(newPricePlan.getExpiryExtention() <= 0)
        			{
        				return;
        			}
        			else
        			{
        				newExpiryDate = calculateSubscriberExpiryDateForMRC(ctx, oldSub, newPricePlan, txn, fee, oldPricePlan);
        			}
        		}
        		else if(PricePlanSubTypeEnum.PAYGO.equals(newPricePlan.getPricePlanSubType()))
        		{
        			long realTimeBalance = getRealTimeBlanace(ctx, client, sub);
        			newExpiryDate = calculateSubscriberExpiryDateForPayGo(ctx, oldPricePlan, realTimeBalance, sub);
        		}
        		else if(PricePlanSubTypeEnum.PICKNPAY.equals(newPricePlan.getPricePlanSubType()))
            	{
            		newExpiryDate = calculateSubscriberExpiryDate(ctx, newPricePlan, txn, oldSub, fee, oldPricePlan);
            	}
        	}
        }
              
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Updating expiryDate to " + newExpiryDate + " for Subscriber " + sub.getId());
        }
        
        /* 
         * Update in CPS
         */
        if (CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(newExpiryDate).after(
                CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date())) )
        {
            /*
             * Activate it only if new expiry date is in future
             */
            client.updateStateAndExpiryDate(ctx,sub, SubscriptionState.ACTIVE, CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(newExpiryDate));
            
            /*
             * Just in case the state of subscriber in BSS is EXPIRED
             */
            if (sub.getState().getIndex() == SubscriberStateEnum.EXPIRED_INDEX )
            {
                sub.setState(SubscriberStateEnum.ACTIVE);
            }
        }
        else
        {

            /*
             * If new expiry date is not in future then update only expiry date
             */
            client.updateExpiryDate(ctx, sub, CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(newExpiryDate));
        }
        
        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Extending subscriber expirty to : " + newExpiryDate + " from : " + sub.getExpiryDate());    
        }
        /*
         * Update subscriber's expiry in BSS table
         */
        sub.setExpiryDate(newExpiryDate);
        
        
        
       
        /*
         * TT#12061422045 need cascade change to dcrm which is done in subscriber pipeline. 
         */ 
        Home subHome = (Home)ctx.get(SubscriberHome.class);
        Subscriber clonedSub = (Subscriber) sub.clone();
        subHome.store(ctx,clonedSub);
    }
    
    /**
     * calculateSubscriberExpiryDate
     * @param ctx
     * @param newPricePlan
     * @param txn
     * @param sub
     * @param fee
     * @param oldPricePlan
     * @return
     */
    public Date calculateSubscriberExpiryDate(Context ctx, PricePlan newPricePlan, Transaction txn, Subscriber sub, ServiceFee2 fee, 
    		PricePlan oldPricePlan)
    {
    	
    	Date extendFromDate = null;

    	if (ExpiryExtensionModeEnum.EXTEND_FROM_CURRENT_EXPIRYDATE.equals(newPricePlan.getExpiryExtensionMode()))
    	{
    		if(oldPricePlan != null)
    		{
    			if(PricePlanSubTypeEnum.LIFETIME.equals(oldPricePlan.getPricePlanSubType()) || sub.getExpiryDate().before(new Date()))
    			{
    				extendFromDate = new Date();
    			}
    		}
    		else
    		{
    			if(sub.getExpiryDate().before(new Date()))
    			{
    				extendFromDate = new Date();
    			}
    			else
    			{
    				extendFromDate = sub.getExpiryDate();
    			}
    		}
    	}
    	else if (ExpiryExtensionModeEnum.EXTEND_FROM_SERVICEFEE_CHARGEDATE.equals(newPricePlan.getExpiryExtensionMode()))
    	{
    		extendFromDate =  txn.getReceiveDate();
    	}

    	//TT#12050706010 Fixed. If the Subscriber date is epoch date, then considering Current Date for calculation.
    	if (extendFromDate.compareTo(Subscriber.NEVER_EXPIRE_CUTOFF_DATE) <= 0)
    	{
    		extendFromDate =  txn.getReceiveDate();
    	}
    	
    	Calendar expiryCalendar = Calendar.getInstance();
    	expiryCalendar.setTime(extendFromDate);
    	
    	if(newPricePlan.getPricePlanSubType()!= null && PricePlanSubTypeEnum.PICKNPAY.equals(newPricePlan.getPricePlanSubType()))
    	{ 
    		if(LogSupport.isDebugEnabled(ctx))
    		{
    			LogSupport.debug(ctx, this, "Since subscriber's priceplan is PickNPay, expiry extension will be same as recurrence of service "+fee.getServiceId());
    		}
    		expiryCalendar.add(Calendar.DAY_OF_YEAR, fee.getRecurrenceInterval());
    	}
    	else
    	{
    		expiryCalendar.add(Calendar.DAY_OF_YEAR, newPricePlan.getExpiryExtention());
    	}
    	
    	
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, this, "extendFromDate: " + extendFromDate + ", newExpiryDate: " + expiryCalendar.getTime());
    	}
    	
    	return expiryCalendar.getTime();
    }
    
    /**
     * getRealTimeBlanace
     * 
     * @param context
     * @param client
     * @param sub
     * @return
     */
    private long getRealTimeBlanace(Context context, SubscriberProfileProvisionClient client, Subscriber sub)
    {
    
	    long realTimeBalance = 0;
	    Parameters profile = null;
	    final Parameter[] inParamSet = new Parameter[0];
		try 
		{
			profile = client.getSubscriptionProfile(context, sub.getMsisdn(), (int)sub.getSubscriptionType(), inParamSet);
		} 
		catch (final SubscriberProfileProvisionException exception)
	    {
	        new MinorLogMsg("SubscriberSupport", "Failed to query BM for subscription " + sub.getId(),
	                exception).log(context);
	        profile = null;
	    }
		catch (HomeException exception) {
			new MinorLogMsg("SubscriberSupport", "Failed to query BM for subscription " + sub.getId(),
	                exception).log(context);
	        profile = null;
		} 
		if(profile != null)
		{
			realTimeBalance = profile.getBalance();
		}
		return realTimeBalance;
    }
    
    /**
     * calculateSubscriberExpiryDateForMRC
     *
     * @param ctx
     * @param sub
     * @param newPricePlan
     * @param txn
     * @param fee
     * @param oldPricePlan
     * @return
     * @throws HomeException
     */
    private Date calculateSubscriberExpiryDateForMRC(Context ctx, Subscriber sub, PricePlan newPricePlan, Transaction txn, 
    		ServiceFee2 fee, PricePlan oldPricePlan) throws Exception
    {
    	if(sub.getPaygoEnabled())
    	{
    		Collection<SubscriberAuxiliaryService> subAuxSvc = null;
    		long boltonService = SpidSupport.getCRMSpid(ctx, sub.getSpid()).getBoltOnForMRCToPaygoFallback();

    		if(boltonService != -100)
    		{
    			subAuxSvc = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(ctx, sub.getId(), boltonService);
    		}
    		if (subAuxSvc == null || subAuxSvc.size() == 0)
    		{
    			sub.setPaygoEnabled(false);
    			new MinorLogMsg(this, "Cannot find SubscriberAuxiliaryService ID : " + boltonService, null).log(ctx);
    			new MinorLogMsg(this, "Paygo mode has been disabled for subscriber : " + sub.getId(), null).log(ctx);
    		}
    		else
    		{
    		    SubscriberAuxiliaryService subscriberAuxiliaryService = subAuxSvc.iterator().next();
    			sub.setPaygoEnabled(false);
    			subscriberAuxiliaryService.setProvisionActionState(false);
    			
    			SubscriberAuxiliaryServiceSupport.removeSubscriberAuxiliaryService(ctx, subscriberAuxiliaryService);
    			
    			new MinorLogMsg(this, "Bolt on service : " + subscriberAuxiliaryService.getIdentifier()  + " has been un-provisioned to subscriber " + sub.getId()
						+ "and Paygo mode is disabled", null).log(ctx);
    		}
    		//-- Send subscriber notification as PayGo mode has been disabled.
    		SubscriptionNotificationSupport.sendPayGoModeDisabledNotification(ctx, sub);
    	}
		return calculateSubscriberExpiryDate(ctx, newPricePlan, txn, sub, fee, oldPricePlan);
    }
    
    /**
     * calculateSubscriberExpiryDateForLifeTime
     * 
     * @param ctx
     * @param sub
     * @return
     * @throws HomeException
     */
    private Date calculateSubscriberExpiryDateForLifeTime(Context ctx, Subscriber sub) throws HomeException
    {
    	int lifteTimeValidity = SpidSupport.getCRMSpid(ctx, sub.getSpid()).getValidityForLifetimeSubscriber();
    	Date newExpiryDate = null;
    	Calendar expiryCalendar = Calendar.getInstance();
    	expiryCalendar = Calendar.getInstance();
		expiryCalendar.setTime(new Date());
        expiryCalendar.add(Calendar.YEAR, lifteTimeValidity);
        newExpiryDate  = expiryCalendar.getTime(); 
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "newExpiryDate is : " + newExpiryDate + " as subscriber is moving from MRC to Lifetime PP");
        }
        return newExpiryDate;
    }
    
    /**
     * calculateSubscriberExpiryDateForPayGo
     * 
     * @param ctx
     * @param oldPricePlan
     * @param realTimeBalance
     * @param sub
     * @return
     * @throws HomeException
     */
    private Date calculateSubscriberExpiryDateForPayGo(Context ctx, PricePlan oldPricePlan, long realTimeBalance, Subscriber sub)
    		throws HomeException
    {
    	Date newExpiryDate = null;
    	Calendar expiryCalendar = Calendar.getInstance();
    	if(!PricePlanSubTypeEnum.PAYGO.equals(oldPricePlan.getPricePlanSubType()))
    	{
    		if(Long.valueOf(oldPricePlan.getPaygoBalanceThreshold()) >0 && realTimeBalance > Long.valueOf(oldPricePlan.getPaygoBalanceThreshold())
    				&& oldPricePlan.getPaygoExtenssionDays() > 0)
    		{
    			
    			if(PricePlanSubTypeEnum.LIFETIME.equals(oldPricePlan.getPricePlanSubType()) || sub.getExpiryDate().before(new Date()))
    			{
    				expiryCalendar.setTime(new Date());
    			}
    			else
    			{
    				expiryCalendar.setTime(sub.getExpiryDate());
    			}
    			expiryCalendar.add(Calendar.DAY_OF_YEAR, (int)oldPricePlan.getPaygoExtenssionDays());
    			newExpiryDate  = expiryCalendar.getTime();
    		}
    		else
        	{
        		expiryCalendar.setTime(sub.getExpiryDate());
        		newExpiryDate  = expiryCalendar.getTime();
        	}
    	}
    	else
    	{
    		expiryCalendar.setTime(sub.getExpiryDate());
    		newExpiryDate  = expiryCalendar.getTime();
    	}
    	return newExpiryDate;
    }
}
