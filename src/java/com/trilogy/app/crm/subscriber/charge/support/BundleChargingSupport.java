package com.trilogy.app.crm.subscriber.charge.support;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bundle.ActivationFeeCalculationEnum;
import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bundle.BundleFeeXInfo;
import com.trilogy.app.crm.bundle.BundleTypeEnum;
import com.trilogy.app.crm.bundle.InvalidBundleApiException;
import com.trilogy.app.crm.bundle.QuotaTypeEnum;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.exception.CategoryNotExistException;
import com.trilogy.app.crm.bundle.service.CRMBundleCategory;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.crm.client.urcs.BundleTopupResponse;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.priceplan.BundleFeeExecutionOrderComparator;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.CrmChargingAccumulator;
import com.trilogy.app.crm.subscriber.charge.SubscriberChargingAccumulator;
import com.trilogy.app.crm.subscriber.charge.customize.TransactionCustomize;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.handler.GenericHandler;
import com.trilogy.app.crm.subscriber.charge.validator.DuplicationValidator;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.BooleanHolder;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;

public abstract class BundleChargingSupport implements ChargingConstants
{

    public static int transferRefund(
            final Context ctx, 
            final Collection provisionoedBundles,
            final Subscriber newSub,
            final Subscriber oldSub,
            final ChargeRefundResultHandler handler,
            final SubscriberChargingAccumulator accumulator)
    {
         //RUNNING_ERROR_STOP
        Collection bundles = getProvisionedBundles(ctx, SubscriberBundleSupport.getSubscribedBundles(ctx, oldSub).values(), 
                provisionoedBundles);
        
        return applyBundlesTransactions(ctx, bundles, newSub,oldSub, false, handler, ACTION_PROVISIONING_REFUND,
                accumulator, RUNNING_SUCCESS); 

    }


    public static int applyBundlesTransactionsByIds(
            final Context ctx, 
            final Collection provisionoedBundles,
            final Subscriber sub,
            final Subscriber oldSub,
            final boolean isActivation, 
            final ChargeRefundResultHandler handler,
            final int action, 
            final SubscriberChargingAccumulator accumulator, 
            final int parentContinueState)
    {
        if (parentContinueState == RUNNING_ERROR_STOP)
        {
            return parentContinueState;
        }
        
         
        Collection bundles = getProvisionedBundles(ctx, SubscriberBundleSupport.getSubscribedBundles(ctx, sub).values(), 
                provisionoedBundles);
        
        return applyBundlesTransactions(ctx, bundles, sub, oldSub, isActivation, handler, action, accumulator, parentContinueState); 

    }
        
    /**
     * 
     * @param ctx
     * @param bundleProfile Cached value (optimization); can be <tt>null</tt>. If null, {@code fee.getBundleProfile(ctx)} 
     * is taken.
     * @param fee
     * @param subscriber
     * @param rollBack Whether to roll back the transaction. This is true if, a forward transaction was posted, but the bundle
     * topup failed subsequently.
     * @return A holder for the results, transaction instance, amount. <b>Note:</b> This method doesn't throw any Exception.
     */
    public static TransactionResultHolder applyBundleRepurchaseTransaction(
            final Context ctx, BundleProfile bundleProfile, final BundleFee fee,
            final Subscriber subscriber, final boolean rollBack
            ) 
    {
        if(bundleProfile==null)
        {
            // Unlikely, but a safety net
            try
            {
                bundleProfile  = fee.getBundleProfile(ctx, subscriber.getSpid());
            } 
            catch(Exception e)
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, BundleChargingSupport.class.getName(), "Could not fetch bundleProfile with id: "+ fee.getId(), e);
                }
                return new TransactionResultHolder(null, ChargingConstants.TRANSACTION_FAIL_DATA_ERROR, 0, e);
            }
        }
        
        long rawAmount = fee.isAuxiliarySource()? bundleProfile.getAuxiliaryServiceCharge() : fee.getFee();
        rawAmount = rollBack ? -rawAmount : rawAmount;
        try
        {
            
            Transaction trans = SubscriberChargingSupport.createTransaction(ctx,
                    getAdjustmentType(ctx, fee), BigDecimal.valueOf(rawAmount), subscriber);
            
            /*
             * Repurchase is for one-time only. However, this chech is outside the scope of this
             * method. So, no restrictions/assumptions here.
             */
            trans.setServiceRevenueRecognizedDate(
                    fee.getServicePeriod().equals(ServicePeriodEnum.ONE_TIME) ?
                            trans.getTransDate() : trans.getServiceEndDate()
                            );
            trans.setRatio(rollBack ? -1.00 : 1.0);
            trans.setFullCharge(rawAmount); 
            
            try 
            {
                SecondaryBalanceChargeOptionSupport.setSecondaryBalanceChargeOption(ctx, subscriber);
                trans = CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, trans);
            }
            catch (OcgTransactionException e )
            {
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, BundleChargingSupport.class.getName(), 
                            "Could not post repurchase transaction to charging for bundle id: "+ fee.getId(), e);
                return new TransactionResultHolder(trans, ChargingConstants.TRANSACTION_FAIL_OCG, rawAmount, e);  
            }
            
            return new TransactionResultHolder(trans, ChargingConstants.TRANSACTION_SUCCESS, rawAmount, null);
        }
        catch (InvalidBundleApiException e)
        {
            if(LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, BundleChargingSupport.class.getName(), 
                        "Could not fetch adjustmet type for the bundle id: "+ fee.getId(), e);
            return new TransactionResultHolder(null, ChargingConstants.TRANSACTION_FAIL_DATA_ERROR, rawAmount, e);
        } 
        catch ( Throwable t )
        {
            if(LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, BundleChargingSupport.class.getName(), 
                        "Could not create repurchase transaction for bundle id: "+ fee.getId(), t);
            return new TransactionResultHolder(null, ChargingConstants.TRANSACTION_FAIL_UNKNOWN, rawAmount, 
                    t instanceof Exception ? (Exception)t : null);    
        } 
    }
    
    public static int applyBundlesTransactions(
            final Context ctx, 
            final Collection bundles,
            final Subscriber sub,
            final Subscriber oldSub,
            final boolean isActivation, 
            final ChargeRefundResultHandler handler,
            final int action, 
            final SubscriberChargingAccumulator accumulator, 
            final int parentContinueState)
    {
        if (parentContinueState == RUNNING_ERROR_STOP)
        {
            return parentContinueState;
        }
        
        int continueState = parentContinueState;
        
        List<BundleFee> bundleFees = new ArrayList<BundleFee>(bundles);
        
        if (ACTION_PROVISIONING_REFUND == action)
        {
            Collections.sort(bundleFees, new BundleFeeExecutionOrderComparator(ctx, false));
        }
        else if (ACTION_PROVISIONING_CHARGE == action)
        {
            Collections.sort(bundleFees, new BundleFeeExecutionOrderComparator(ctx, true));
        }
        
        for (Iterator i = bundleFees.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next(); 
            
           
            BundleProfile bundleProfile = null;             
            try 
            {               
                 bundleProfile =  BundleSupportHelper.get(ctx).getBundleProfile(ctx, fee.getId()); 
             }
            catch ( Throwable t )
            {
            	// unlike to happen in this stage
            	continue; 
            } 

            ChargableItemResult ret = new ChargableItemResult( action, 
                    ChargingConstants.CHARGABLE_ITEM_BUNDLE,  fee, fee.getId(), 
                    sub , oldSub) ;
            ret.setAction(action); 
            ret.isActivation= isActivation; 
            ret.setChargableObjectRef(bundleProfile);
  			initializedTransaction(ctx, ret); 

	  	    if (ret.getChargeResult() == TRANSACTION_SUCCESS)
	  	    {	
	            ret.setChargeResult(getEventType(ctx,sub, fee, bundleProfile, action));           
	            if  (ret.getChargeResult() == TRANSACTION_SUCCESS)
	            {

	            	if ( continueState == RUNNING_SUCCESS)
	            	{ 
	            		if (isForceChargingApplicable(ctx,ret))
	            		{
	            			if(!ctx.has(BundleChargingSupport.IS_ALREADY_FORCE_CHARGED))
	            			{
	            				Context subCtx = ctx.createSubContext();
	            				subCtx.put(BundleChargingSupport.IS_ALREADY_FORCE_CHARGED, Boolean.TRUE);
	            				handleBundleTransaction(subCtx, ret,  null);
	            			}
	            		}
	            		else{
	            			SubscriberChargingSupport.validateTransaction(ctx, ret, null); 
	            			SubscriberChargingSupport.updateTransactionValue(ctx, ret);
	            			if (ret.getChargeResult() == TRANSACTION_VALIDATION_SUCCESS)
	            			{
	            				ret = handleBundleTransaction(ctx, ret,  null); 
	            			}	
	            		}

	            	}
	            	else if ( continueState == RUNNING_CONTINUE_SUSPEND)
                    {
                        ret.setChargeResult(TRANSACTION_SKIPPED_SUSPEND); 
                        ret.setRunningState(RUNNING_CONTINUE_SUSPEND); 
                    }
        	   }  
	           //Code Review: this will likely never run
        	   else if ( continueState == RUNNING_CONTINUE_SUSPEND)
        	   {
        		   ret.setChargeResult(TRANSACTION_SKIPPED_SUSPEND); 
        		   ret.setRunningState(RUNNING_CONTINUE_SUSPEND); 
        	   }
           }   
            
            ChargeRefundResultHandler realHandler = ChargeRefundResultHandlerSupport.getHandler(ctx, handler, ret);
            
            if ( realHandler != null )
            {    
                realHandler.handleTransaction(ctx, ret);
                continueState = ret.getRunningState(); 
            
                if( continueState == RUNNING_ERROR_STOP )
                {
                    return continueState; 
                } 
            }
            
            if ( accumulator != null )
            {
                accumulate(accumulator, ret);
            }
        }
        
        return continueState; 
    }
    
    
    /*
     * Perform force charging
     */
    static private boolean isForceChargingApplicable(final Context ctx, final ChargableItemResult chargeableItemResult)
    {
    	
    	Subscriber oldSubscriber = chargeableItemResult.getOldSubscriber();
    	Subscriber newSubscriber = chargeableItemResult.getSubscriber();
    	BundleProfile profile = (BundleProfile) chargeableItemResult.getChargableObjectRef();
    	
    	if (oldSubscriber == null || newSubscriber == null)
    	{
    		if(LogSupport.isDebugEnabled(ctx))
    		{
    			LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Bundle ID : " + profile.getBundleId() + " is NOT applicable for Force Charging. Either Old or New subscriber is found to be null");
    		}
    		return false;
    	}
    	
    	if (chargeableItemResult.getAction() == ACTION_PROVISIONING_CHARGE && profile.isForceCharging() 
    			&& (oldSubscriber.getPricePlan() != newSubscriber.getPricePlan()  
    			|| SubscriberChargingSupport.getChargeType(ctx, newSubscriber, oldSubscriber) == PROVISIONING_CHARGE_ACTION_TYPE_SIMPLE)) 
    	{
    		if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Bundle ID : " + profile.getBundleId() + 
                		" for Subscriber ID : " + newSubscriber.getId() + " is applicable for Force Charging");
            }
    		return true;
    	}
    	else
    	{
    		if(LogSupport.isDebugEnabled(ctx))
    		{
    			LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Bundle ID : " + profile.getBundleId() + 
            		" for Subscriber ID : " + newSubscriber.getId() + " is NOT applicable for Force Charging");
    		}
    		return false;
    	}
    }

    public static void initializedTransaction(Context parentCtx, final ChargableItemResult result)
    {
        Context ctx = parentCtx.createSubContext();
        ctx.put(Subscriber.class, result.getSubscriber());
        
    	BundleFee fee = (BundleFee) result.getChargableObject();
    	BundleProfile bundleProfile = (BundleProfile) result.getChargableObjectRef(); 

        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        
        int ret = TRANSACTION_SUCCESS;
        try 
        {
 
            long rawAmount = fee.getFee();
            
            if (fee.isAuxiliarySource())
            {
                rawAmount = bundleProfile.getAuxiliaryServiceCharge();
            }    

            result.setChargingCycleType(ServiceChargingSupport.getChargingCycleType(fee.getServicePeriod()));
            ServicePeriodHandler handler = SubscriberChargingSupport.getServicePeriodHandler(ctx, result.getChargingCycleType());

            final double ratio;
           
            
            boolean postPaidSubCreationOnly = bundleProfile.getPostpaidSubCreationOnly();  
            
            if(postPaidSubCreationOnly == false && result.getSubscriber().isPostpaid())
            {
            	/*
            	 * refund should be full 
            	 */
            	if (SubscriberChargingSupport.isRefund(result.action))
            	{
            		ratio = -1.0d;	
            	}
            	/*
            	 * charge should be full  
            	 */
            	else
            	{
            		ratio = 1.0d;
            	}
            	
            	
            }else if (SubscriberChargingSupport.isRefund(result.action))
            {
                ratio = handler.calculateRefundRate(ctx, runningDate, 
                    SubscriberSupport.getBillCycleDay(ctx, result.getSubscriber()), 
                    result.getSubscriber().getSpid(), 
                    result.getSubscriber().getId(), fee);
            }
            else 
            {
            	boolean chargeFull = false;
                if (fee.isAuxiliarySource())
                {
                    chargeFull = ActivationFeeCalculationEnum.FULL.equals(bundleProfile.getActivationFeeCalculation()) && SubscriberChargingSupport.isAuxiliaryActivation(ctx, result, fee);
                }
                else
                {
                	chargeFull = ActivationFeeCalculationEnum.FULL.equals(bundleProfile.getActivationFeeCalculation()) && SubscriberChargingSupport.isActivation(ctx, result);
                }

                if (chargeFull)
                {
                    ratio = 1.0;
                }
                else
                {
                    ratio = handler.calculateRate(ctx, runningDate, 
                            SubscriberSupport.getBillCycleDay(ctx, result.getSubscriber()), 
                            result.getSubscriber().getSpid(), 
                            result.getSubscriber().getId(), fee);
                }
            }

            Transaction trans = new Transaction();
            // For the bundles associated with CUG Aux service
            boolean aggPPCugToOwner = false;
            ClosedUserGroup cug = ClosedUserGroupSupport.getCug(ctx, result.getSubscriber().getRootAccount(ctx).getBAN());
            if(cug != null)
            {
                String bundleId = String.valueOf(fee.getBundleProfile(ctx).getBundleId());
                aggPPCugToOwner = cug.getAuxiliaryService(ctx).getAggPPServiceChargesToCUGOwner();
                String auxPrepaidBundleIds = cug.getAuxiliaryService(ctx).getPrepaidBundles();
                String auxPostpaidBundleIds = cug.getAuxiliaryService(ctx).getPostpaidBundles();
                if (auxPrepaidBundleIds.contains(bundleId) || auxPostpaidBundleIds.contains(bundleId)
                        && aggPPCugToOwner)
                {
                	             	    
             	   
             	     BooleanHolder alreadyPresent = (BooleanHolder)ctx.get(ClosedUserGroupSupport.SUBSCRIBER_AS_CUGMEMEBR);
             	    if(alreadyPresent !=null)
             	    {
             	        alreadyPresent.setBooleanValue(true);
             	    }
             	    else
             	    {
             	        ctx.put(ClosedUserGroupSupport.SUBSCRIBER_AS_CUGMEMEBR, new BooleanHolder(true));
             	    }
             	 

                    final BigDecimal amount = SubscriberChargingSupport.getAmount(result.action, rawAmount, ratio);
                    
                    trans = SubscriberChargingSupport.createCUGOwnerTransaction(ctx,
                            getAdjustmentType(ctx, fee), amount, cug.getOwner(ctx), result.getSubscriber().getId());
                    trans.setServiceEndDate(handler.calculateCycleEndDate(ctx, runningDate, 
                            SubscriberSupport.getBillCycleDay(ctx, result.getSubscriber()), 
                            result.getSubscriber().getSpid(), 
                            result.getSubscriber().getId(), fee));
                    result.setSubscriber(cug.getOwner(ctx));
                }
                else
                {
                    final BigDecimal amount = SubscriberChargingSupport.getAmount(result.action, rawAmount, ratio);
                    trans = SubscriberChargingSupport.createTransaction(ctx, getAdjustmentType(ctx, fee),
                            amount, result.getSubscriber());
                }
            }
            else
            {
                final BigDecimal amount = SubscriberChargingSupport.getAmount( 
                        result.action, rawAmount, ratio); 

                trans = SubscriberChargingSupport.createTransaction(ctx,
                        getAdjustmentType(ctx, fee), amount, result.getSubscriber());
            }
            
            if(LogSupport.isDebugEnabled(ctx))
            {
                String msg = MessageFormat.format(
                    "Restrict Provisioning {0} for Service {1}, for subscriber {2}", 
                        new Object[]{bundleProfile.getRestrictProvisioning()? "ON" : "OFF",
                                Long.valueOf(fee.getId()), result.getSubscriber().getId()});
                LogSupport.debug(ctx, BundleChargingSupport.class.getName(), msg);
            }
            
            trans.setAllowCreditLimitOverride(fee.isMandatory() || !bundleProfile.getRestrictProvisioning());
            /*trans.setServiceEndDate(handler.calculateCycleEndDate(ctx, runningDate, 
                    SubscriberSupport.getBillCycleDay(ctx, result.getSubscriber()), 
                    result.getSubscriber().getSpid(), 
                    result.getSubscriber().getId(), fee));*/
            
            if (fee.getServicePeriod().equals(ServicePeriodEnum.ONE_TIME) ) 
            {
                // changed for Dory, but supposed to be accepted by all customers. 
               trans.setServiceRevenueRecognizedDate(trans.getTransDate()); 
            } else
            {
                trans.setServiceRevenueRecognizedDate(trans.getServiceEndDate());    
            }
            
            trans.setRatio(ratio);
            trans.setFullCharge(rawAmount); 
            result.setTrans(trans); 
            
        }
        catch ( Throwable t )
        {
            ret = TRANSACTION_FAIL_UNKNOWN;    
            result.thrownObject = t; 
            
        } 
        
        result.setChargeResult(ret);

        
    }        

    
    private static ChargableItemResult handleBundleTransaction(final Context ctx, final ChargableItemResult result,
            final TransactionCustomize transCustomizer)
    {
        int ret = TRANSACTION_SUCCESS;
        try
        {

            if (transCustomizer != null)
            {
                result.setTrans(transCustomizer.customize(ctx, result.getTrans()));
            }

            SecondaryBalanceChargeOptionSupport.setSecondaryBalanceChargeOption(ctx, result.getSubscriber());
            result.setTrans(CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, result.getTrans()));

        }
        catch (OcgTransactionException e)
        {
            ret = TRANSACTION_FAIL_OCG;
            result.thrownObject = e;
        }

        catch (Throwable t)
        {
            ret = TRANSACTION_FAIL_UNKNOWN;
            result.thrownObject = t;

        }

        result.setChargeResult(ret);

        return result;
    }        
    
    public static int handleSingleBundleTransactionWithoutRedirecting(
    		final Context ctx, 
       		final ChargableItemResult ret,
            final ChargeRefundResultHandler handler, 
            final CrmChargingAccumulator accumulator,
            final TransactionCustomize transCustomizer,
            final DuplicationValidator validator
    )
    {
         
		SubscriberChargingSupport.validateTransaction(ctx, ret, validator); 
		SubscriberChargingSupport.updateTransactionValue(ctx, ret);
		
		if (ret.getChargeResult() == TRANSACTION_VALIDATION_SUCCESS)
		{	

		        handleBundleTransaction(ctx, ret, transCustomizer);                     				
		} 

        ChargeRefundResultHandler realHandler = ChargeRefundResultHandlerSupport.getHandler(ctx, handler, ret);
        
        if ( realHandler != null )
        {    
          realHandler.handleTransaction(ctx, ret);
        }    
        
        if ( accumulator != null)
        {
            SubscriberChargingSupport.accumulate(accumulator, ret);
            
        }

        return ret.getChargeResult();
  
    }
    
   
    public static void accumulate(final SubscriberChargingAccumulator accumulator, 
            ChargableItemResult ret)
    {
        
        if ( ret.getChargeResult() == TRANSACTION_SUCCESS)
        {
            if ( ret.getAction() != ACTION_PROVISIONING_REFUND )
            {    
                accumulator.addChargedBundle(ret.getId());
            } 
            else 
            {
                accumulator.addRefundBundle(ret.getId());
                
            }
         } 
        SubscriberChargingSupport.accumulate(accumulator, ret);        
        
    }
    
    public static Collection getProvisionedBundles(Context ctx, 
            Collection bundlefees, Collection provisionedBundles  )
    {
        Collection ret = new HashSet();
        for ( Iterator i = provisionedBundles.iterator(); i.hasNext();)
        {
            Long bundleId = (Long)i.next();
            CollectionSupportHelper.get(ctx).findAll(ctx, bundlefees, 
                    new EQ(BundleFeeXInfo.ID, bundleId),
                    ret); 
        }
        return ret;
    }

    public static BundleProfile getBundleProfile(Context ctx, long id)
    {
        try 
        {
            return  BundleSupportHelper.get(ctx).getBundleProfile(ctx, id);
            
        } catch ( Exception e)
        {
            new MinorLogMsg(BundleChargingSupport.class, "fail to find bundle " + id, e).log(ctx);
          
        }
        return null; 
    }
    
    
    public static Collection getAuxiliaryBundles(Collection bundles)
    {
       Collection ret = new HashSet(); 
        for ( Iterator i = bundles.iterator(); i.hasNext();)
        {
            BundleFee fee = (BundleFee) i.next();
            if ( fee.isAuxiliarySource())
            {
                ret.add(new Long(fee.getId())); 
            }
        }
        
        return ret; 
    }
    

    public static Collection getAuxiliaryBundles(Context ctx, Subscriber sub)
    {
        return getAuxiliaryBundles(getProvisionedBundles(ctx, 
                SubscriberBundleSupport.getSubscribedBundles(ctx, sub).values(), 
                sub.getBundles().keySet()));
        
    }    
    
    
    
    public static Collection getUnchangedAuxiliaryBundle(Context ctx, Subscriber newSub, Subscriber oldSub)
    {
        Collection oldAuxiliaryBundles = BundleChargingSupport.getAuxiliaryBundles(ctx, oldSub); 
        Collection newAuxiliaryBundles = BundleChargingSupport.getAuxiliaryBundles(ctx, newSub); 
        
        //auxiliary bundles removed after updating
        Collection unprovisionedAuxiliaryBundles = new HashSet(); 
        unprovisionedAuxiliaryBundles.addAll(oldAuxiliaryBundles); 
        unprovisionedAuxiliaryBundles.removeAll(newAuxiliaryBundles); 
        
        //auxiliary bundles kept 
        Collection unchangedAuxiliaryBundles = new HashSet();
        unchangedAuxiliaryBundles.addAll(oldAuxiliaryBundles);
        unchangedAuxiliaryBundles.removeAll(unprovisionedAuxiliaryBundles); 

        return unchangedAuxiliaryBundles; 
    }
    
    
    private static int getEventType(
            final Context ctx, 
            final Subscriber sub, 
            final BundleFee fee,  
            final BundleProfile bundle, 
            final int action
            )
    {
        try {
            if ( fee.getSource().startsWith("Package") )
            {
               return TRANSACTION_SKIPPED_IN_PACKAGE; 
            }
            
            // check for one time bundle
            if(bundle.getRecurrenceScheme().equals(RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL) ||
                bundle.getRecurrenceScheme().equals(RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE))
            {
                return TRANSACTION_REDIRECT_ONE_TIME; 
            }
            
            
        } catch (Exception e)
        {
            new MinorLogMsg(ServiceChargingSupport.class, "fail when query suspended entity table for sub" + sub.getId() 
                    + " bundle " + fee.getId(), e).log(ctx); 
        }
       return TRANSACTION_SUCCESS; 
    }
    
    
    
    public static int applyOverUsageCharge(
            final Context ctx,
            final Subscriber sub,
            final ChargeRefundResultHandler handler, 
            final int parentContinueState, 
            final Subscriber oldSub)
    {
        if (parentContinueState == RUNNING_ERROR_STOP || sub.getBundleOverUsage() == null)
        {
            return parentContinueState;
        }
        
        int continueState = parentContinueState;
        for (Iterator<Map.Entry<Long, Long>> i = sub.getBundleOverUsage().entrySet().iterator(); i.hasNext();)
        {
            Map.Entry<Long, Long> entry = i.next();

            Long bundleId = entry.getKey();
            long amount = entry.getValue().longValue();
            ChargableItemResult ret = null; 
            
            if ( continueState == RUNNING_SUCCESS)
            {    
                 ret = handleOverUsage(ctx, bundleId, sub, amount, oldSub);
                 
                 ChargeRefundResultHandler realHandler;
                 
                 // We should not create subscriber subscription history for overusage.
                 if (handler == null)
                 {
                     realHandler = new GenericHandler();
                 }
                 else
                 {
                     realHandler = ChargeRefundResultHandlerSupport.getHandler(ctx, handler, ret);
                 }
                 
                 if ( realHandler != null )
                 {    
                     realHandler.handleTransaction(ctx, ret);
                     continueState = ret.getRunningState(); 
                 
                     if( continueState == RUNNING_ERROR_STOP )
                     {
                         return continueState; 
                     } 
                 }
            }  
        }
        //make sure no duplicate charge made for overusage. 
        sub.setBundleOverUsage(new HashMap<Long, Long>()); 
        
        return continueState; 
    }
    
    
    protected  static ChargableItemResult handleOverUsage(
            final Context ctx, 
            final Long bundleId, 
            final Subscriber sub, 
            final long amount, 
            final Subscriber oldSub)
    {
        final int action = ACTION_BUNDLE_OVERUSAGE_CHARGE;  
        final ChargableItemResult ret = new ChargableItemResult( action, 
                ChargingConstants.CHARGABLE_ITEM_BUNDLE,  null, bundleId.longValue(), 
                sub) ;
        
        try 
        {
            final BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundleId);
            ret.setChargableObject(bundle);
            Transaction trans = overUsageCharge(ctx, sub, bundleId.longValue(), amount, oldSub);
            
            if ( trans == null)
            {
                ret.chargeResult = TRANSACTION_SKIPPED_OVERUSAGE; 
            } else 
            { 
                ret.chargeResult = TRANSACTION_SUCCESS;
                ret.trans = trans;
            }

        }catch (OcgTransactionException e )
        {
            ret.chargeResult = TRANSACTION_FAIL_OCG;
            ret.thrownObject = e; 
         } catch (Throwable t)
        {
            ret.chargeResult = TRANSACTION_FAIL_UNKNOWN; 
            ret.thrownObject = t; 
        }

        return ret; 
    }
    

    /** Handle Bucket overusage charge. 
     * This method need refactoring, it is moved from SubscriberBundleSupport. 
     * 
     * @param ctx
     * @param msisdn
     * @param bundleId
     * @param amount
     * @return
     * @throws Exception
     */
    static public Transaction overUsageCharge(Context ctx, Subscriber sub, long bundleId, long amount, Subscriber oldSub) 
    throws Exception
    {
        CRMBundleCategory catService = (CRMBundleCategory)ctx.get(CRMBundleCategory.class);
        String msisdn = sub.getMSISDN(); 
       
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(BundleChargingSupport.class.getName(),
                    "Overusage charge " + msisdn + " " + bundleId + " " + amount, null).log(ctx);
        }

        if (amount <= 0)
        {
            return null;
        }

        BundleProfile bundle = null;
        try
        {
            bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundleId);
            if (bundle == null)
            {
                throw new HomeException("Internal Error: unknown bundle '" + bundleId + "'");
            }
        }
        catch (InvalidBundleApiException e)
        {
            throw new HomeException("Internal Error: unknown bundle '" + bundleId + "'");
        }

        
        if (bundle.getQuotaScheme() == QuotaTypeEnum.UNLIMITED_QUOTA)
        {
            // do not charge overusage on unlimited bundles
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(BundleChargingSupport.class.getName(),
                        "NOT charging overusage on subscriber \"" + msisdn + "\" for UNLIMITED bundle " + bundleId + " !",
                        null).log(ctx);
            }
            return null;
        }

        if (bundle.getQuotaScheme() == QuotaTypeEnum.MOVING_QUOTA)
        {
            // do not charge overusage on moving Quota bundles which are Loyalty Points bundles
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(BundleChargingSupport.class.getName(),
                        "NOT charging overusage on subscriber \"" + msisdn + "\" for Loyalty Points bundle " + bundleId + " !",
                        null).log(ctx);
            }
            return null;
        }
        
        UnitTypeEnum unitType;
        
        if (bundle.isSingleService())
        {
            BundleCategory category = null;
            try
            {
                category = catService.getCategory(ctx, bundle.getBundleCategoryId());
                if (category == null)
                {
                    throw new HomeException("Internal Error: unknown bundle category '"
                            + bundle.getBundleCategoryId() + "'");
                }
    
            }
            catch (CategoryNotExistException e)
            {
                throw new HomeException("Internal Error: unknown category '" + bundle.getBundleCategoryId() + "'");
            }
            catch (BundleManagerException e)
            {
                throw new HomeException("Internal Error: unknown category '" + bundle.getBundleCategoryId() + "'");
            }
            
            unitType = category.getUnitType();
    
        }
        else if (bundle.isCurrency())
        {
            unitType = UnitTypeEnum.CURRENCY;
        }
        else
        {
            unitType = UnitTypeEnum.CROSS_UNIT;
        }

        // adjust the amount based on the bundle's unit type multiplier
        // Ex. Voice overUsageCharge is returned from BM in seconds but CRM uses Minutes
        BigDecimal famount = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf((double) unitType.getMultiplier(ctx)));

        BigDecimal rate = null;

        final Subscriber sub_ppv = oldSub==null?sub:oldSub; 
        
        Map<Long, BundleFee> bundleFeeMap = sub_ppv.getRawPricePlanVersion(ctx).getServicePackageVersion(ctx).getBundleFees(ctx);  
        long bundleFee = 0;
        
        for (final Iterator<Map.Entry<Long, BundleFee>> i = bundleFeeMap.entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, BundleFee> entry = i.next();
            final BundleFee fee = entry.getValue();
            if (fee.getBundleProfile(ctx).getBundleId() == bundleId)
            {
            	bundleFee = fee.getFee();
            }
        }
        
        switch (bundle.getType())
        {
            case BundleTypeEnum.VOICE_INDEX:
                rate = BigDecimal.valueOf(sub_ppv.getRawPricePlanVersion(ctx).getOverusageVoiceRate());
                break;
            case BundleTypeEnum.SMS_INDEX:
                rate = BigDecimal.valueOf(sub_ppv.getRawPricePlanVersion(ctx).getOverusageSmsRate());
                break;
            case BundleTypeEnum.DATA_INDEX:
                rate = BigDecimal.valueOf(sub_ppv.getRawPricePlanVersion(ctx).getOverusageDataRate());
                break;
            case BundleTypeEnum.CROSS_SERVICE_INDEX:
            	if (bundle.isAuxiliary())
            	{
            		rate = BigDecimal.valueOf(bundle.getAuxiliaryServiceCharge()).divide(BigDecimal.valueOf(bundle.getInitialBalanceLimit()));
            	}
            	else
            	{
            		rate = BigDecimal.valueOf(bundleFee).divide(BigDecimal.valueOf(bundle.getInitialBalanceLimit()));
            	}
                break;
            case BundleTypeEnum.MONETARY_INDEX:
                rate = BigDecimal.valueOf(1);
                break;
            default:
                return null;
        }

        BigDecimal sum = rate.multiply(famount);
       
        final AdjustmentType type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, OVERUSAGE_ADJUSTMENT);
        if (type == null)
        {
            throw new HomeException(
                    "System error: no AdjustmentType " + OVERUSAGE_ADJUSTMENT + " found in context.");
        }

        
        Transaction trans = SubscriberChargingSupport.createTransaction(ctx,type.getCode(), sum, sub);
        trans =  CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, trans);
        return trans; 
    }
    
    
    
    
    public static Map<Long, BundleFee> getSubscribedBundles(Context ctx, final Subscriber sub)
    {
        final Map<Long, BundleFee> inMap = sub.getBundles(); 
        final Map<Long, BundleFee> outMap = new HashMap<Long, BundleFee>();
        final Date today = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        for (final Iterator<Map.Entry<Long, BundleFee>> i = inMap.entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, BundleFee> entry = i.next();
            final Long key = entry.getKey();
            final BundleFee fee = entry.getValue();
            if (fee.getEndDate().after(today) && fee.getStartDate().compareTo(today) <= 0)
            {
                outMap.put(key, fee);
            }
        }
        return outMap;
    }

    
    public static int getAdjustmentType(final Context context, final BundleFee fee) throws InvalidBundleApiException,
    HomeException
    {
    	final BundleProfile api = getBundleProfile(context, fee.getId());
    	return fee.isAuxiliarySource() ? api.getAuxiliaryAdjustmentType() : api.getAdjustmentType();
    }
    
    /**
     * 
     * @param ctx
     * @param subscriber
     * @param profile
     * @param bundleFee
     * @param option
     * @param paramsParser
     * @param resHolder
     * @return
     * @throws HomeException
     */
    
    public static int invokeRepurchaseBundles(Context ctx,
            Subscriber subscriber, BundleProfile profile, BundleFee bundleFee)  throws HomeException
    {    
        try
        {
            if(profile==null)
                profile = bundleFee.getBundleProfile(ctx, subscriber.getSpid());
        } 
        catch (Exception e)
        {
            throw new HomeException("Could not obtain bundle profile for bundle id: "+ bundleFee.getId(), e);
        }
        
        String msisdn = subscriber.getMsisdn();
        String msg = MessageFormat.format("Bundle top up request for Bundle-Id {0} for Subscriber {1}",
                new Object[]{Long.valueOf(profile.getBundleId()), msisdn});
        if (LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, BundleChargingSupport.class, msg);
 
        /*
         * First check if AccountOperations service is up
         */
        com.redknee.app.crm.client.urcs.AccountOperationsClientV4 topUpClient = (com.redknee.app.crm.client.urcs.AccountOperationsClientV4) ctx
            .get(com.redknee.app.crm.client.urcs.AccountOperationsClientV4.class);        
        if(!topUpClient.isAlive(ctx))
        {
            msg = MessageFormat.format(
                    "Bundle re-purchase failed for Subscriber {0}. Could not initiate balance updates as the AccountOperations service is down.",
                    new Object[]{msisdn});
            return -1;
        }        
        
        /*
         * Make debit transaction (Forward to OCG)
         */
        TransactionResultHolder tranRes = BundleChargingSupport.applyBundleRepurchaseTransaction(ctx, profile, bundleFee, subscriber, false);
        long amount = tranRes.getTransactionAmount();

        msg = MessageFormat.format(
                "Attempting to Debit Subscriber: {0}, with amount: {1}",
                new Object[]{msisdn, Long.valueOf(amount)});
        if (LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, BundleChargingSupport.class.getName(), msg);
        
        if(!tranRes.isSuccess())
        {
            msg = MessageFormat.format(
                    "Debit Transaction failed for Subscriber: {0}, for amount: {1}, for bundle repurchase of bundle-id: {2}; Txn result code: {3}",
                    new Object[]{msisdn, Long.valueOf(amount), Long.valueOf(profile.getBundleId()),
                            Integer.valueOf(tranRes.getTransactionResultCode())});
            if (LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, BundleChargingSupport.class.getName(), msg);
            
            return -1;
        } 
        
        msg = MessageFormat.format(
                "Debit Transaction successful for Subscriber: {0}, for amount: {1}, for bundle repurchase of bundle-id: {2}.",
                new Object[]{msisdn, Long.valueOf(amount), Long.valueOf(profile.getBundleId())});
        if (LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, BundleChargingSupport.class.getName(), msg);
        
        /*
         * Top up repurchasable bundle
         */
        BundleTopupResponse response = new BundleTopupResponse(-1, -1, null);
        try
        {
            response = topUpClient.topupBundle(ctx, msisdn, 
                    TimeZone.getTimeZone(subscriber.getTimeZone(ctx)), 
                        subscriber.getCurrency(ctx), profile, "AppCrm-repurchase-" + msisdn);
        }
        catch (RemoteServiceException e)
        {
            msg = MessageFormat.format(
                    "Bundle top up failed for subscriber: {0}, amount: {1}, currency: {2}, bundle: {3}",
                    new Object[]{msisdn, Long.valueOf(amount), 
                            subscriber.getCurrency(ctx), Long.valueOf(profile.getBundleId())});
            LogSupport.minor(ctx, BundleChargingSupport.class.getName(), msg, e);
            response.setServiceResponseCode(-1);
            
        }        
        
        /*
         * Logging, notes, Charging History updates.
         */
        
        /*
         * Update Bundle expiry (if sent by URCS/CPS)
         */
        msg = MessageFormat.format(
                "Successful Repurchase; New Expiry: {0} for the bundle {1}, for subscriber {2}.",
                new Object[]{
                        response.getNewExpiryDate()!=null ? response.getNewExpiryDate().getTime() : "<no change>", 
                                Long.valueOf(profile.getBundleId()), msisdn
                        });
        if (LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, BundleChargingSupport.class.getName(),
                    msg);
        
        if(response.getNewExpiryDate() != null)
        {
            bundleFee.setRepurchaseHappened(true);
            bundleFee.setEndDate(response.getNewExpiryDate().getTime());
        }
        
        SubscriberSubscriptionHistory record = SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, bundleFee, subscriber,
                HistoryEventTypeEnum.CHARGE, 
                    (bundleFee.isAuxiliarySource() ? ChargedItemTypeEnum.AUXBUNDLE : ChargedItemTypeEnum.BUNDLE), 
                        tranRes.getTransactionAmount(), tranRes.getTransactionAmount(), tranRes.getTransaction(), 
                            CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(
                                    tranRes.getTransaction().getTransDate(), subscriber.getTimeZone(ctx))
                            );
        
        StringBuilder noteBuff = new StringBuilder();
        noteBuff.append(msg);
        SubscriberNoteSupport.createSubscriberNote(ctx, BundleChargingSupport.class.getName(), 
            SubscriberNoteSupport.getCsrAgent(ctx, subscriber), 
                subscriber.getId(), SystemNoteTypeEnum.BUNDLE_REPURCHASE, 
                    SystemNoteSubTypeEnum.BUNDLE_REPURCHASE_ROLLBACK, noteBuff, true);
        
        return 0;
    }
    
    
    public static final int OVERUSAGE_ADJUSTMENT = 32711;
    private static final String CLASS_NAME_FOR_LOGGING = BundleChargingSupport.class.getName();
    public static final String IS_ALREADY_FORCE_CHARGED = "IS_ALREADY_FORCE_CHARGED";
}
