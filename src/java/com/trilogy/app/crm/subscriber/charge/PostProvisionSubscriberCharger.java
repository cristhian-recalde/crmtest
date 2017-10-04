package com.trilogy.app.crm.subscriber.charge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateAction;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bundle.BundleFee;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.home.ExtendSubscriberExpiryHome;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.BundleChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.SubscriberChargingSupport;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.subscriber.charge.support.PackageChargingSupport;



/**
 * this charger is for current subscriber provision pipe line. 
 * because the behavior of subscriber pipeline is weird, and the 
 * call flow is not very clear and straight forward, so we still 
 * need some FW style magic, such as put charger in context for 
 * reuse in case of subscriber creation. 
 * 
 * In future when provision is separated from subscriber pipeline. 
 * we should make the charger more accurate for each case. 
 * 
 *  
 * @author lxia
 *
 */
public class PostProvisionSubscriberCharger 
extends AbstractSubscriberProvisioningCharger
{

    public PostProvisionSubscriberCharger(Subscriber sub)
    {
        this.subscriber = sub; 
    }
    
    public PostProvisionSubscriberCharger(Subscriber newSub, Subscriber oldSub)
    {
        this.subscriber = newSub; 
        this.oldSub = oldSub; 
    }
    
    @Override
    protected int calculateOnCreate(Context ctx, ChargeRefundResultHandler handler) 
    {
        if (subscriber!=null && subscriber.isPostpaid() && 
                EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber.getState(), getNonChargeableStates()) &&
                handlePredatedChargeInNonChargeableStates(ctx))
        {
            return CALCULATION_PREDATED_DUNNING;
        }
        else if (skipCalculation(ctx))
        {
            return CALCULATION_SKIPPED;
        }
        
        clearToBeSet(); 
        
        packagesToBeCharged.addAll( subscriber.getProvisionedPackageIdsBackup()); 
        servicesToBeCharged.addAll( subscriber.getProvisionedSubscriberServices(ctx)); 
        bundlesToBeCharged.addAll(  BundleChargingSupport.getSubscribedBundles(ctx, subscriber).keySet());        	
       // auxServicesToBeCharged.addAll(subscriber.getProvisionedAuxiliaryServices(ctx)); 
        auxServicesToBeCharged.addAll(SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, subscriber.getId())); 
        
        if(isPicknPay(ctx, subscriber))
        {
       	 this.handleMRCGroup(ctx);
        }
        
       return CALCULATION_SUCCESS;
    }
    
     
    @Override
    protected int calculateOnSimpleProvisionUnprovison(Context ctx, ChargeRefundResultHandler handler)
    {
        if (subscriber!=null && getOldSubscriber(ctx) != null && subscriber.isPostpaid() && 
                EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber.getState(), getNonChargeableStates()) &&
                handlePredatedChargeInNonChargeableStates(ctx))
        {
            return CALCULATION_PREDATED_DUNNING;
        }
        else if ( subscriber == null || getOldSubscriber(ctx) == null || 
                (EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber.getState(), getNonChargeableStates())))
        {
            return CALCULATION_SKIPPED; 
        }
        
         clearToBeSet();
         
         // Fixed TT#13042229003, TT#13021413063. When Subscriber Pipeline is invoked from Transaction Pipeline, 
         // via ExtendSubscriberExpiryHome, then Provisioning/Charging needs to be skipped. 
         boolean serviceCharged = (Boolean) ctx.get(ExtendSubscriberExpiryHome.SKIP_PROVISIONING_AND_CHARGING_CALCULATIONS, Boolean.FALSE);
         if(serviceCharged){
         		LogSupport.info(ctx,  this, "ExtendSubscriberExpiryHome.SKIP_PROVISIONING_AND_CHARGING_CALCULATIONS flag is true. Hence skipping Charging calculations as this is only an Expiry Extension call for Subscriber iD : " + subscriber.getId());
         	return CALCULATION_SKIPPED;

         }      
         
         packagesToBeRefund.addAll( getOldSubscriber(ctx).getProvisionedPackageIdsBackup()); 
         packagesToBeRefund.removeAll(subscriber.getProvisionedPackageIdsBackup());  
         servicesToBeRefund.addAll(getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values()); 
         // Consider clct suspended services for refund, if these are removed
         servicesToBeRefund.addAll(getOldSubscriber(ctx).getCLCTSuspendedServicesBackup(ctx).values());
         //consider the below code add for those service whose old status is "failed to Unprove" and current status is "Unprove" so need to be refund
         servicesToBeRefund.addAll(getOldSubscriber(ctx).getUnProvisionedErrorServicesBackup(ctx).values());
         // remove those clct services which are not modified         
         servicesToBeRefund.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeRefund, subscriber.getCLTCSubscriberServices(ctx)));
         servicesToBeRefund.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeRefund, subscriber.getProvisionedSubscriberServices(ctx)));     
         //Not refund those services whose current status(on database) are "failed to unprove". 
         //remove for those services whose old status and current status are both "failed to Unprove" 
         servicesToBeRefund.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeRefund, SubscriberServicesSupport.getUnProvisionedWithErrorSubscriberServices(ctx, subscriber.getId(),false) ));
         
         bundlesToBeRefund.addAll(  BundleChargingSupport.getSubscribedBundles(ctx, getOldSubscriber(ctx)).keySet()); 
         bundlesToBeRefund.removeAll(BundleChargingSupport.getSubscribedBundles(ctx, subscriber).keySet());         
         auxServicesToBeRefund.addAll( getOldSubscriber(ctx).getProvisionedAuxiliaryServices(ctx));
         auxServicesToBeRefund.removeAll( AuxServiceChargingSupport.getCrossed(ctx, auxServicesToBeRefund, 
        		 SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, subscriber.getId()) ));  
         
         packagesToBeCharged.addAll( subscriber.getProvisionedPackageIdsBackup()); 
         packagesToBeCharged.removeAll(PackageChargingSupport.getProvisionedServicePackageIds(ctx,getOldSubscriber(ctx)));              
         servicesToBeCharged.addAll( subscriber.getProvisionedSubscriberServices(ctx)); 
         servicesToBeCharged.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeCharged, getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values()));
         bundlesToBeCharged.addAll( BundleChargingSupport.getSubscribedBundles(ctx, subscriber).keySet()); 
         bundlesToBeCharged.removeAll(BundleChargingSupport.getSubscribedBundles(ctx, getOldSubscriber(ctx)).keySet());            
         auxServicesToBeCharged.addAll(SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, subscriber.getId())); 
         auxServicesToBeCharged.removeAll( AuxServiceChargingSupport.getCrossed(ctx, auxServicesToBeCharged, getOldSubscriber(ctx).getProvisionedAuxiliaryServices(ctx)));  
 
         this.handleRefundable(ctx);
         
         this.setBundleOverUsageCharge(subscriber.getBundleOverUsage()); 
         
         this.handleSuspendedEntity(ctx);
        
         // exclude services that are cltc suspended. 
         this.HandleCltc(ctx); 

         if(isPicknPay(ctx, subscriber))
         {
        	 this.handleMRCGroup(ctx);
         }
         

         return CALCULATION_SUCCESS;

    }
    
    
    private boolean isPicknPay(Context ctx,Subscriber sub)
    {
    	boolean picknPayFlag = false;
        PricePlan pp;
		try {
			pp = HomeSupportHelper.get(ctx).findBean(ctx, PricePlan.class, new EQ(PricePlanXInfo.ID, sub.getPricePlan()));

	        picknPayFlag = pp.getPricePlanSubType().equals(PricePlanSubTypeEnum.PICKNPAY);
	        
		} catch (HomeException e) {			
			LogSupport.minor(ctx, this,  "Error while finding the subscriber price plan home");
		}
		
		return picknPayFlag;

    }
    

    private boolean skipCalculation(Context ctx)
    {
        boolean result = false;
        
        if (subscriber == null || subscriber.isPrepaid()
                || EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber.getState(), getNonChargeableStates()))
        {
            if ((subscriber.isPrepaid() && 
                    !SystemSupport.supportsPrepaidCreationInActiveState(ctx)) || 
                      EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber.getState(), getNonChargeablePostpaidStates()))
            {
                result = true;
            }
        }
        
        return result;
    }

    
    @Override
    protected int calculateOnSimplePPVChange(Context ctx, ChargeRefundResultHandler handler)
    {
    	if(LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, this, this.getClass().getName() + "::calculateOnSimplePPVChange");
    	}
        
        if (subscriber!=null && getOldSubscriber(ctx) != null && subscriber.isPostpaid() && 
                EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber.getState(), getNonChargeableStates()) &&
                handlePredatedChargeInNonChargeableStates(ctx))
        {
            return CALCULATION_PREDATED_DUNNING;
        }
        else if ( subscriber == null || getOldSubscriber(ctx) == null || 
                (EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber.getState(), getNonChargeableStates())))
        {
            return CALCULATION_SKIPPED; 
        }

        clearToBeSet();
        
        Collection unchangedAuxiliaryBundles = BundleChargingSupport.getUnchangedAuxiliaryBundle(ctx, subscriber, getOldSubscriber(ctx));  
        
        
        //TT#12062825046
        Set<Long> excludeSimilarOneTimeBundles = getSimilarOneTimeBundlesToBeExcluded(ctx, getOldSubscriber(ctx), getNewSubscriber());
        
        packagesToBeCharged.addAll( subscriber.getProvisionedPackageIdsBackup()); 
        servicesToBeCharged.addAll( subscriber.getProvisionedSubscriberServices(ctx));        
        bundlesToBeCharged.addAll( BundleChargingSupport.getSubscribedBundles(ctx, subscriber).keySet());        
        bundlesToBeCharged.removeAll(unchangedAuxiliaryBundles);   
        bundlesToBeCharged.removeAll(excludeSimilarOneTimeBundles);//TT#12062825046
        //aux service should be still be delta provisioning. 
        auxServicesToBeRefund.addAll( getOldSubscriber(ctx).getProvisionedAuxiliaryServices(ctx));
        auxServicesToBeRefund.removeAll( AuxServiceChargingSupport.getCrossed(ctx, auxServicesToBeRefund, 
        		SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, subscriber.getId())));  

        packagesToBeRefund.addAll( PackageChargingSupport.getProvisionedServicePackageIds(ctx,getOldSubscriber(ctx))); 
        servicesToBeRefund.addAll(getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values());  
        // consider suspended clct services for refund
        servicesToBeRefund.addAll(getOldSubscriber(ctx).getCLCTSuspendedServicesBackup(ctx).values());
        bundlesToBeRefund.addAll( BundleChargingSupport.getSubscribedBundles(ctx, getOldSubscriber(ctx)).keySet()); 
        bundlesToBeRefund.removeAll(unchangedAuxiliaryBundles); 
        bundlesToBeRefund.removeAll(excludeSimilarOneTimeBundles);//TT#12062825046
        auxServicesToBeCharged.addAll(SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, subscriber.getId())); 
        auxServicesToBeCharged.removeAll( AuxServiceChargingSupport.getCrossed(ctx, auxServicesToBeCharged, getOldSubscriber(ctx).getProvisionedAuxiliaryServices(ctx)));  

        if(LogSupport.isDebugEnabled(ctx))
        {
        	LogSupport.debug(ctx, this, "bundlesToBeCharged :: " + bundlesToBeCharged);
        	LogSupport.debug(ctx, this, "bundlesToBeRefund :: " + bundlesToBeRefund);
            LogSupport.debug(ctx, this, "packagesToBeRefund :: " + packagesToBeRefund);
        }
        
        this.handleRefundable(ctx);
        
        this.handleSuspendedEntity(ctx);
        
        this.setBundleOverUsageCharge(subscriber.getBundleOverUsage()); 

        this.HandleCltc(ctx); 
        
        if(isPicknPay(ctx, subscriber) || isPicknPay(ctx, getOldSubscriber(ctx)))
        {
       	 this.handleMRCGroup(ctx);
        }
        
        return CALCULATION_SUCCESS;
    }    
    
    /**
     * TT#12062825046
     * 
     * @param ctx
     * @param oldSub
     * @param newSub
     * @return
     */
    private Set<Long> getSimilarOneTimeBundlesToBeExcluded(Context ctx , Subscriber oldSub, Subscriber newSub)
    {
    	Set<Long> excludeBundles = new HashSet<Long>();
    	
    	Set<Long> oldBundleIds = BundleChargingSupport.getSubscribedBundles(ctx, oldSub).keySet();
    	
    	for (Long newBundleId : BundleChargingSupport.getSubscribedBundles(ctx, newSub).keySet())
    	{
    		if(oldBundleIds.contains(newBundleId))
    		{
    			try
    			{
    				BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, newBundleId);
    				if(bundle.getChargingRecurrenceScheme().equals(ServicePeriodEnum.ONE_TIME))
    				{
    					excludeBundles.add(bundle.getBundleId());
    				}
    			}
    			catch(Exception e)
    			{
    				LogSupport.major(ctx, this, "getSimilarOneTimeBundlesToBeExcluded" , e);
    			}
    		}
    	}
    	
    	
		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "getSimilarOneTimeBundlesToBeExcluded::" + excludeBundles);
		}
    	
    	return excludeBundles;
    }
    
    @Override
    protected int  calculateOnSimpleStateChange(Context ctx , ChargeRefundResultHandler handler)
    {
        SubscriberStateAction action = SubscriberChargingSupport.getSubscriberStateAction(ctx, subscriber, getOldSubscriber(ctx));  
        
        int ret = CALCULATION_SUCCESS;
        
        if ( action != null)
        {
            switch (action.getActionCharge())
            {
              case SUBSCRIBER_STATE_ACTION_CHARGE_ALL:
              case SUBSCRIBER_STATE_ACTION_CHARGE_EXCLUDE_SMART_SUSPENSION:
                calculateChargeOnSimpleStateChange(ctx);
                allowChargeOnStateChange = true;
                break;
            case SUBSCRIBER_STATE_ACTION_REFUND_ALL:
            	calculateRefundAll(ctx); 
                break;
            case SUBSCRIBER_STATE_ACTION_REFUND_INCLUDE_SMART_SUSPENSION:
                calculateRefundOnSimpleStateChangeIncludeSmartSuspension(ctx); 
                break;
            case SUBSCRIBER_STATE_ACTION_REFUND_EXCLUDE_SMART_SUSPENSION:
                calculateRefundOnSimpleStateChangeExcludeSmartSuspension(ctx);            
            }
        
        } 
        
       this.setBundleOverUsageCharge(subscriber.getBundleOverUsage()); 
       this.handleSuspendedEntity(ctx);
       
       if(isPicknPay(ctx, subscriber))
       {
      	 this.handleMRCGroup(ctx);
       }
       
       return ret; 
    }
    
    
    
    @Override
    protected int calculateOnMultipleUpdate(Context ctx, ChargeRefundResultHandler handler)
    {
        int ret = CALCULATION_SUCCESS;
        if ( subscriber == null || getOldSubscriber(ctx) == null)
        {
            return  ret; 
        }
        
        if ( subscriber.getState().equals(SubscriberStateEnum.ACTIVE))
        {    
            if ( getOldSubscriber(ctx).getState().equals(SubscriberStateEnum.SUSPENDED) ||
                    getOldSubscriber(ctx).getState().equals(SubscriberStateEnum.LOCKED) )
            {
                  calculateOnSuspendToActiveWithPPVChange(ctx);
                
            }
            else if ( getOldSubscriber(ctx).getState().equals(SubscriberStateEnum.INACTIVE))
            {
                 ret = calculateOnSimpleStateChange(ctx, handler);
            }    

        } else {
            //it should not happen, since we don't support such update.if it is happens, we treat it as 
            // simple ppv change. 
             ret = calculateOnSimplePPVChange(ctx, handler);   
            
        }
        this.setBundleOverUsageCharge(subscriber.getBundleOverUsage()); 
        this.handleSuspendedEntity(ctx);
        this.HandleCltc(ctx); 
        
        if(isPicknPay(ctx, subscriber) || isPicknPay(ctx, getOldSubscriber(ctx)))
        {
       	 this.handleMRCGroup(ctx);
        }
        
        return ret;
    }
    
    
    
    private void calculateChargeOnSimpleStateChange(Context ctx)
    {
        clearToBeChargedSet();
        packagesToBeCharged.addAll( subscriber.getProvisionedPackageIdsBackup()); 
        boolean serviceCharged = (Boolean) ctx.get(ExtendSubscriberExpiryHome.SKIP_PROVISIONING_AND_CHARGING_CALCULATIONS, Boolean.FALSE);
        if(serviceCharged){
        	/*
        	* Skip the primary and other services from charging if the subscriber has moved from expired to active state.
        	* If the Subscriber Price plan version is changed subscribers would already get charged on switching price plan version.
        	*/
        	if (LogSupport.isDebugEnabled(ctx))
        	{
        		LogSupport.debug(ctx,  this, "calculateChargeOnSimpleStateChange::Since the subscriber is moved from expired to active state, the services would be already be charged when retrying to apply reccuring charges. Hence skip charging on state change");
        	}
        	return;

        }       
    	servicesToBeCharged.addAll(subscriber.getProvisionedSubscriberServices(ctx)); 
    	
        bundlesToBeCharged.addAll( BundleChargingSupport.getSubscribedBundles(ctx, subscriber).keySet());
        
        // For TT 13040244079 - filtering out all the onetime bundles if old state is barred/locked
        Subscriber oldSub = getOldSubscriber(ctx);
        if(oldSub.getState() == SubscriberStateEnum.LOCKED)
        {
            Set<Long> excludeSimilarOneTimeBundles = getSimilarOneTimeBundlesToBeExcluded(ctx, oldSub, getNewSubscriber());
            bundlesToBeCharged.removeAll(excludeSimilarOneTimeBundles);
        }
        auxServicesToBeCharged.addAll(SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, subscriber.getId())); 

    }    
    
    private void calculateRefundAll(Context ctx)
    {
    	calculateRefundProvisioned(ctx);
    	handleRefundable(ctx);
    	//need include services that are cltc suspended. 
    	addAllCLTCServices(ctx); 
    }
    
    private void calculateRefundProvisioned(Context ctx)
    {
        clearToBeRefundSet();
        packagesToBeRefund.addAll( getOldSubscriber(ctx).getProvisionedPackageIdsBackup()); 
        servicesToBeRefund.addAll( getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values()); 
        bundlesToBeRefund.addAll( BundleChargingSupport.getSubscribedBundles(ctx, getOldSubscriber(ctx)).keySet()); 
        auxServicesToBeRefund.addAll(getOldSubscriber(ctx).getProvisionedAuxiliaryServices(ctx)); 
 
    }
    
    private void calculateRefundOnSimpleStateChangeExcludeSmartSuspension(Context ctx)
    {
        calculateRefundProvisioned(ctx); 
        // filter out services that are smart suspension
        handleSmartSupensionRefund(ctx); 
     }

    private void calculateRefundOnSimpleStateChangeIncludeSmartSuspension(Context ctx)
    {
        calculateRefundProvisioned(ctx); 
        //refund applied to only smart suspension services,
        handleSmartSupensionRefundForUnprovision(ctx); 
        //need include services that are cltc suspended. 
        addAllCLTCServices(ctx);

    }
    
    
    /**
     * this method is only for the case when subscriber state from suspension to active with 
     * price plan verision updating. 
     * @param ctx
     * @param oldSubscriber
     * @param getOldSub(ctx)
     */
    private void calculateOnSuspendToActiveWithPPVChange(Context ctx)
    {
        calculateChargeOnSuspendToActiveWithPPVChange(ctx); 
        calculateRefundOnSuspendToActiveWithPPVChange(ctx);
    }
    
    

    /**
         * this method is only for the case when subscriber state from suspension to active with 
         * price plan verision updating. 
         * 1. find new entities 
         * 2. find old entities that include in new ppv
         * 3. filter smart suspension of out put in stpe2.
         * 3. new entities of step 1 + filtered entities of step 3. 
         * @param ctx
         * 
         * @param oldSubscriber
         * @param getOldSub(ctx)
         */
    private void calculateChargeOnSuspendToActiveWithPPVChange(Context ctx)
    { 
        this.clearToBeChargedSet(); 
        
        //get provisioned service in both ppv
        HashSet<SubscriberServices> servicesFromNewPPV = new HashSet<SubscriberServices>(); 
        servicesFromNewPPV.addAll(subscriber.getProvisionedSubscriberServices(ctx));
        servicesFromNewPPV.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesFromNewPPV, getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values()));
        servicesToBeCharged.addAll(subscriber.getProvisionedSubscriberServices(ctx));
        servicesToBeCharged.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeCharged, servicesFromNewPPV));
        
        HashSet packagesFromNewPPV = new HashSet(); 
        packagesFromNewPPV.addAll(subscriber.getProvisionedPackageIdsBackup());
        packagesFromNewPPV.removeAll(getOldSubscriber(ctx).getProvisionedPackageIdsBackup());
        packagesToBeCharged.addAll(subscriber.getProvisionedPackageIdsBackup());
        packagesToBeCharged.removeAll(packagesFromNewPPV);
        
        HashSet<Long> bundlesFromNewPPV = new HashSet<Long>(); 
        bundlesFromNewPPV.addAll(BundleChargingSupport.getSubscribedBundles(ctx, subscriber).keySet());
        bundlesFromNewPPV.removeAll(BundleChargingSupport.getSubscribedBundles(ctx, getOldSubscriber(ctx)).keySet());
        bundlesToBeCharged.addAll(BundleChargingSupport.getSubscribedBundles(ctx, subscriber).keySet());
        bundlesToBeCharged.removeAll(bundlesFromNewPPV);
         
        HashSet<SubscriberAuxiliaryService> auxServicesFromNewPPV = new HashSet<SubscriberAuxiliaryService>(); 
        auxServicesFromNewPPV.addAll(subscriber.getAuxiliaryServices(ctx));
        auxServicesToBeCharged.addAll(AuxServiceChargingSupport.getCrossed(ctx, auxServicesFromNewPPV, getOldSubscriber(ctx).getProvisionedAuxiliaryServices(ctx)));
        auxServicesFromNewPPV.removeAll(AuxServiceChargingSupport.getCrossed(ctx, auxServicesFromNewPPV, auxServicesToBeCharged));
        
        // filter out these are with smart suspension
        handleSmartSupensionCharge(ctx);
          
        //add the services from new ppv but not in old ppv
        servicesToBeCharged.addAll(servicesFromNewPPV);
        packagesToBeCharged.addAll(packagesFromNewPPV);
        bundlesToBeCharged.addAll(bundlesFromNewPPV);
        auxServicesToBeCharged.addAll(auxServicesFromNewPPV);
                
        
    }  
    
    
    /**
     * this method is only for the case when subscriber state from suspension to active with 
     * price plan verision updating. 
     * @param ctx
     * @param oldSubscriber
     * @param getOldSub(ctx)
     */
    private void calculateRefundOnSuspendToActiveWithPPVChange(Context ctx)
    {
        this.clearToBeRefundSet(); 
        
        // get service in old ppv but not in new ppv. unprovisioned services 
        servicesToBeRefund.addAll(getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values());
        // Consider CLCT suspended services
        servicesToBeRefund.addAll(getOldSubscriber(ctx).getCLCTSuspendedServicesBackup(ctx).values());
        servicesToBeRefund.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeRefund, subscriber.getCLTCSubscriberServices(ctx)));
        servicesToBeRefund.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeRefund, subscriber.getProvisionedSubscriberServices(ctx)));
        
        packagesToBeRefund.addAll(getOldSubscriber(ctx).getProvisionedPackageIdsBackup());
        packagesToBeRefund.removeAll(subscriber.getProvisionedPackageIdsBackup()); 

        bundlesToBeRefund.addAll(BundleChargingSupport.getSubscribedBundles(ctx, getOldSubscriber(ctx)).keySet());
        bundlesToBeRefund.removeAll(BundleChargingSupport.getSubscribedBundles(ctx, subscriber).keySet()); 
        
        auxServicesToBeRefund.addAll(getOldSubscriber(ctx).getProvisionedAuxServiceBackup(ctx));
        //auxServicesToBeRefund.removeAll( AuxServiceChargingSupport.getCrossed(ctx, auxServicesToBeRefund, subscriber.getProvisionedAuxiliaryServices(ctx)));  
        auxServicesToBeRefund.removeAll( AuxServiceChargingSupport.getCrossed(ctx, auxServicesToBeRefund, 
        		SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, subscriber.getId())));  
        
        // filter out these are not smart suspension 
        handleSmartSupensionRefundForUnprovision(ctx);
           
    }
    
     

  
    public int getChargerType()
    {
        return CHARGER_TYPE_POST_PROVISION_SUBSCRIBER;
    }
}
