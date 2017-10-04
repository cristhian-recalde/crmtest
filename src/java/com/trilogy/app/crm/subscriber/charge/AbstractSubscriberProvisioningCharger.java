package com.trilogy.app.crm.subscriber.charge;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.home.ExtendSubscriberExpiryHome;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.BundleChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.PackageChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.SubscriberChargingSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public abstract class AbstractSubscriberProvisioningCharger 
extends AbstractSubscriberCharger
{
        
    public int chargeAndRefund(Context ctx, 
            ChargeRefundResultHandler handler)
    {
        this.calculationResult = calculate(ctx,  handler);
        
        if (calculationResult == CALCULATION_PREDATED_DUNNING)
        {
        	
        	handleRefundable(ctx);
        	
            this.chargeResult = createTransactionsForProvisioningPostpaidInDunned(ctx, handler);           
        }
        else if ( calculationResult != CALCULATION_SKIPPED)
        {
            this.chargeResult = createTransactionsForProvisioning(ctx, handler);           
        }
        
        return getResult();
    }
    
    
    public int charge(Context ctx,
            ChargeRefundResultHandler handler)
    {
        return OPERATION_NOT_SUPPORT; 
    }
    
    public int refund(Context ctx,
            ChargeRefundResultHandler handler)
    {
        return OPERATION_NOT_SUPPORT; 
    }

    
    protected int calculate(Context ctx, ChargeRefundResultHandler handler)
    {
        
        switch ( SubscriberChargingSupport.getChargeType(ctx, subscriber, getOldSubscriber(ctx)))
       {
           case PROVISIONING_CHARGE_ACTION_TYPE_CREATE: 
               return calculateOnCreate(ctx, handler); 
           case PROVISIONING_CHARGE_ACTION_TYPE_SIMPLE:
               return calculateOnSimpleProvisionUnprovison(ctx, handler);  
           case PROVISIONING_CHARGE_ACTION_TYPE_PPV_CHANGE:
               return calculateOnSimplePPVChange(ctx, handler); 
           case PROVISIONING_CHARGE_ACTION_TYPE_STATE_CHANGE:
               return calculateOnSimpleStateChange(ctx, handler); 
           case PROVISIONING_CHARGE_ACTION_TYPE_MULTI:
               return calculateOnMultipleUpdate(ctx, handler); 
           default:   
        
          
       }
        return CALCULATION_SKIPPED;
    }
 
 
 
    protected int createTransactionsForProvisioning(Context ctx, 
            ChargeRefundResultHandler handler)
    {
        //make sure do refund before the charge, otherwise, in case 
        //of ppv change, the charge will be rejected due to duplicate charge 
        //in same billing cycle
        int retRefund = createRefundTransactions(ctx, handler);
        int retChargeOfOverUsage = createOverUsageTransaction(ctx, handler); 
        int retCharge = createChargeTransactions(ctx,SubscriberChargingSupport.isActivation(ctx, subscriber), 
                 ACTION_PROVISIONING_CHARGE, handler);
        
        if ( (retRefund + retCharge) != RUNNING_SUCCESS)
        {
            return RUNNING_ERROR_STOP;
        }
         return RUNNING_SUCCESS;   
    }

    
    protected int createTransactionsForProvisioningPostpaidInDunned(Context ctx, 
            ChargeRefundResultHandler handler)
    {
        int retCharge = createChargeTransactions(ctx,SubscriberChargingSupport.isActivation(ctx, subscriber), 
                 ACTION_PROVISIONING_CHARGE, handler);
        int retRefund = createRefundTransactions(ctx, handler);
        int retChargeOfOverUsage = createOverUsageTransaction(ctx, handler); 
        
        if ( (retRefund + retCharge) != RUNNING_SUCCESS)
        {
            return RUNNING_ERROR_STOP;
        }
         return RUNNING_SUCCESS;   
    }

    protected int createOverUsageTransaction(final Context context, ChargeRefundResultHandler handler)
    {
        Context subCtx = context.createSubContext();
        subCtx.put(Subscriber.class, subscriber);
        if ( subscriber == null)
        {
            new MajorLogMsg(AbstractSubscriberProvisioningCharger.class, 
                    "can not find subscriber in context", null).log(subCtx); 
            return RUNNING_ERROR_STOP; 
        }

        this.clearRefundSet();
        int continueState = RUNNING_SUCCESS;

        continueState = BundleChargingSupport.applyOverUsageCharge(subCtx, 
                subscriber,  handler, continueState, oldSub);
        
        return continueState;
        
    }
    
    
    /**
     * 
     * @param ctx
     * @param action, for now only provision refund is available
     * @param handler
     * @return
     */
    protected int createRefundTransactions( final Context context,             
            ChargeRefundResultHandler handler)
    {
        Context subCtx = context.createSubContext();
        final int action = ACTION_PROVISIONING_REFUND; 
        Subscriber sub = getOldSubscriber(subCtx); 
        if ( sub == null)
        {
            new MajorLogMsg(AbstractSubscriberProvisioningCharger.class, 
                    "can not find subscriber in context", null).log(subCtx); 
            return RUNNING_ERROR_STOP; 
        }

        subCtx.put(Subscriber.class, sub);
        this.clearRefundSet();
        int continueState = RUNNING_SUCCESS;
        subCtx.put(ChargingConstants.IS_REFUND_TRANSACTION,true);

       continueState = PackageChargingSupport.applyPackagesChargeByIds(subCtx, packagesToBeRefund,  
               sub, false, handler, action,  this, continueState);
        
        continueState = ServiceChargingSupport.applyServicesChargeByIds(subCtx, servicesToBeRefund, 
                sub, getOldSubscriber(subCtx), false, handler, action, this, continueState);
        
        continueState = BundleChargingSupport.applyBundlesTransactionsByIds(subCtx, bundlesToBeRefund, 
                sub,getOldSubscriber(subCtx), false, handler, action, this, continueState);
        
        continueState = AuxServiceChargingSupport.applyAuxServicesChargeByIds(subCtx, auxServicesToBeRefund, 
                sub, getOldSubscriber(subCtx), false, handler, action, this, continueState);
  
        return continueState;
    }

    
    /**
     * calculate entities to be charged and refund in case of creation
     * @param ctx
     * @param oldSubscriber
     */
    protected abstract int calculateOnCreate(Context ctx, 
            ChargeRefundResultHandler handler);
    
    /**
     * calculate entities to be charged and refund in case of delta service provisioning
     * @param ctx
     * @param oldSubscriber
     * @param getOldSub(ctx)
     */
    protected abstract int calculateOnSimpleProvisionUnprovison(Context ctx, 
            ChargeRefundResultHandler handler);
    
    /**
     * calculate entities to be charged and refund in case of price plan or price plan version change
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    protected abstract int calculateOnSimplePPVChange(Context ctx, 
            ChargeRefundResultHandler handler); 
    
    
    /**
     * calculate entities to be charged and refund in case of subscriber state change 
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    protected abstract int calculateOnSimpleStateChange(Context ctx, 
            ChargeRefundResultHandler handler); 
    
    
    /**
     * calculate entities to be charged and refund in case both state and ppv changed. 
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    protected abstract int calculateOnMultipleUpdate(Context ctx, 
            ChargeRefundResultHandler handler); 
    
    protected boolean handlePredatedChargeInNonChargeableStates(Context ctx)
    {
        boolean hasPredatedServices = false;
        Date today = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(Calendar.getInstance().getTime());

        clearToBeSet();
        
        servicesToBeCharged.addAll(subscriber.getProvisionedSubscriberServices(ctx)); 
        servicesToBeCharged.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeCharged, getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values()));
        Iterator<SubscriberServices> iter = servicesToBeCharged.iterator();
        while (iter.hasNext())
        {
            SubscriberServices service = iter.next();
            if (!service.getStartDate().before(today))
            {
                iter.remove();
            }
        }
        servicesToBeRefund.addAll(servicesToBeCharged);
        
        auxServicesToBeCharged.addAll(SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(ctx, subscriber.getId())); 
        auxServicesToBeCharged.removeAll( AuxServiceChargingSupport.getCrossed(ctx, auxServicesToBeCharged, getOldSubscriber(ctx).getAuxiliaryServices(ctx)));  

        Iterator<SubscriberAuxiliaryService> iterAuxServices = auxServicesToBeCharged.iterator();
        while (iterAuxServices.hasNext())
        {
            SubscriberAuxiliaryService service = iterAuxServices.next();
            if (!service.getStartDate().before(today))
            {
                iterAuxServices.remove();
            }
        }
        auxServicesToBeRefund.addAll(auxServicesToBeCharged);
        
        
        if (servicesToBeCharged.size()>0 || auxServicesToBeCharged.size()>0)
        {
            hasPredatedServices = true;
        }
        return hasPredatedServices;
    }
        
    /**
     * filter out entities with smart suspension for charging
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    protected void  handleSmartSupensionCharge(Context ctx)
    {
        HashSet chargeWaived = new HashSet(); 
        for ( Iterator i = getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values().iterator(); i.hasNext();)
        {
            SubscriberServices subService = (SubscriberServices) i.next();
            Service service = ServiceChargingSupport.getServiceById(ctx, subService.getServiceId());
            if ( service == null || service.isSmartSuspension())
            {
                    chargeWaived.add(subService);
            }
             
        }
        servicesToBeCharged.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeCharged, chargeWaived));
        
        chargeWaived.clear();
        for(Iterator i = getOldSubscriber(ctx).getBundles().keySet().iterator(); i.hasNext();)
        {
            Long bundleId = (Long) i.next();
            BundleProfile bundleProfile = BundleChargingSupport.getBundleProfile(ctx, bundleId.longValue()); 
            if ( bundleProfile == null || bundleProfile.isSmartSuspensionEnabled())
            {
                    chargeWaived.add(bundleId);
            } 
        }        
        bundlesToBeCharged.removeAll(chargeWaived); 
        
        chargeWaived.clear(); 
        for ( Iterator<SubscriberAuxiliaryService> i = getOldSubscriber(ctx).getProvisionedAuxServiceBackup(ctx).iterator(); i.hasNext();)
        {
            SubscriberAuxiliaryService subService = i.next();
            Long serviceId = subService.getAuxiliaryServiceIdentifier();
            AuxiliaryService service = AuxServiceChargingSupport.getAuxiliaryServicById(ctx, serviceId.longValue());
            if ( service == null || service.isSmartSuspension())
            {
                chargeWaived.add(subService);
            }
        }
        
        auxServicesToBeCharged.removeAll(AuxServiceChargingSupport.getCrossed(ctx, auxServicesToBeCharged, chargeWaived));
        
    }

    
    
    /**
     * filter out entities with non smart suspension or not in same billing cycle for refund. 
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    protected void  handleSmartSupensionRefund(Context ctx)
    {
        HashSet refundWaived = new HashSet(); 
        for ( Iterator i = getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values().iterator(); i.hasNext();)
        {
            SubscriberServices subService = (SubscriberServices) i.next();
            Service service = ServiceChargingSupport.getServiceById(ctx, subService.getServiceId());
            if ( service == null || service.isSmartSuspension())
            {
                refundWaived.add(subService);
            }
            
        }
        servicesToBeRefund.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeRefund, refundWaived));
        
        
        refundWaived.clear();
        for(Iterator i = getOldSubscriber(ctx).getBundles().keySet().iterator(); i.hasNext();)
        {
            Long bundleId = (Long) i.next();
            BundleProfile bundleProfile = BundleChargingSupport.getBundleProfile(ctx, bundleId.longValue()); 
            if ( bundleProfile == null || bundleProfile.isSmartSuspensionEnabled())
            {
                refundWaived.add(bundleId);
            }
        }
        bundlesToBeRefund.removeAll(refundWaived); 

        
        
        refundWaived.clear(); 
        for ( Iterator i = getOldSubscriber(ctx).getProvisionedAuxServiceIdsBackup(ctx).iterator(); i.hasNext();)
        {
            Long serviceId = (Long) i.next();
            AuxiliaryService service = AuxServiceChargingSupport.getAuxiliaryServicById(ctx, serviceId.longValue());
            if ( service == null || service.isSmartSuspension())
            {
                Iterator<SubscriberAuxiliaryService> iter = auxServicesToBeRefund.iterator();
                while (iter.hasNext())
                {
                    SubscriberAuxiliaryService sas = iter.next();
                    if (sas.getAuxiliaryServiceIdentifier() == service.getIdentifier())
                    {
                        iter.remove();
                    }
                }
            }
        }
    }
    
    /**
     * the service which is not refund when changing state to suspension due to smart suspension, 
     * should be refund if it is not in the new price plan version when subscriber is activated in same 
     * billing cycle. In case not price plan version updating, this is not necessary. 
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    protected void  handleSmartSupensionRefundForUnprovision(Context ctx)
    {
        HashSet chargeWaived = new HashSet(); 
        for ( Iterator i = getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values().iterator(); i.hasNext();)
        {
            SubscriberServices subService = (SubscriberServices) i.next();
            Service service = ServiceChargingSupport.getServiceById(ctx, subService.getServiceId());
            if ( service == null || !service.isSmartSuspension())
            {
                    chargeWaived.add(subService);
            }
             
        }
        servicesToBeRefund.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeRefund, chargeWaived));
        
        chargeWaived.clear();
        for(Iterator i = getOldSubscriber(ctx).getBundles().keySet().iterator(); i.hasNext();)
        {
            Long bundleId = (Long) i.next();
            BundleProfile api = BundleChargingSupport.getBundleProfile(ctx, bundleId.longValue()); 
            if ( api != null || !api.isSmartSuspensionEnabled())
            {
                    chargeWaived.add(bundleId);
            }
        }     
        bundlesToBeRefund.removeAll(chargeWaived); 
        
        chargeWaived.clear(); 
        for ( Iterator i = getOldSubscriber(ctx).getProvisionedAuxServiceIdsBackup(ctx).iterator(); i.hasNext();)
        {
            Long serviceId = (Long) i.next();
            AuxiliaryService service = AuxServiceChargingSupport.getAuxiliaryServicById(ctx, serviceId.longValue());
            if ( service == null || !service.isSmartSuspension())
            {
                Iterator<SubscriberAuxiliaryService> iter = auxServicesToBeRefund.iterator();
                while (iter.hasNext())
                {
                    SubscriberAuxiliaryService sas = iter.next();
                    if (sas.getAuxiliaryServiceIdentifier() == service.getIdentifier())
                    {
                        iter.remove();
                    }
                }
            }
        }

    }
    
  
    /**
     * if a suspended service is in refund set, then we can charge, but not be 
     * able to refund. if charge is successful, then next we can unsuspend 
     * for example, if one service is unprovisioned and refund, the refund 
     * could be enough to pay for a suspended service and get it reprovisioned. 
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    protected void handleSuspendedEntity(Context ctx)
    {
        Collection suspendedToBeRefundEntities = new HashSet(); 
        suspendedToBeRefundEntities.addAll(this.packagesToBeRefund);
        suspendedToBeRefundEntities.retainAll(getOldSubscriber(ctx).getSuspendedPackages(ctx).keySet());
        this.packagesToBeRefund.removeAll(suspendedToBeRefundEntities);
        this.packagesToBeCharged.removeAll(getOldSubscriber(ctx).getSuspendedPackages(ctx).keySet());
        this.packagesToBeCharged.addAll(suspendedToBeRefundEntities);
        suspendedToBeRefundEntities.clear();
        
        //the subscriber service refactoring is not complete, as result, CLTC is a in state of suspend, 
        // but with a suspend reason CLTC. charge and refund policy is different for CLTC with others. 
        // need a good thinking how to refactoring in next stage. but for now, we simply remove cltc 
        // services. 
        suspendedToBeRefundEntities.addAll(this.servicesToBeRefund);

        for ( Iterator<SubscriberServices> i = suspendedToBeRefundEntities.iterator(); i.hasNext();)
        {
            SubscriberServices subService = i.next();
            
            if (!getOldSubscriber(ctx).getSuspendedServices(ctx).keySet().contains(Long.valueOf(subService.getServiceId())))
            {
                i.remove();
            }
        }

        suspendedToBeRefundEntities.removeAll(ServiceChargingSupport.getCrossed(ctx, suspendedToBeRefundEntities, this.oldSub.getCLTCSubscriberServices(ctx)));

        this.servicesToBeRefund.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeRefund, suspendedToBeRefundEntities));
        
        /*for ( Iterator<SubscriberServices> i = servicesToBeCharged.iterator(); i.hasNext();)
        {
            SubscriberServices subService = i.next();
            
            if (getOldSubscriber(ctx).getSuspendedServices(ctx).keySet().contains(Long.valueOf(subService.getServiceId())))
            {
                i.remove();
            }
        }*/
        
        this.servicesToBeCharged.addAll(suspendedToBeRefundEntities);

        /*
         * Sujeet: Avoiding ClassCastEx.
         * We better use Typed (Generics) Collection 
         */
        suspendedToBeRefundEntities.clear();
        
        suspendedToBeRefundEntities.addAll(this.bundlesToBeRefund);
        suspendedToBeRefundEntities.retainAll(getOldSubscriber(ctx).getSuspendedBundles(ctx).keySet());
        this.bundlesToBeRefund.removeAll(suspendedToBeRefundEntities);
        this.bundlesToBeCharged.removeAll(getOldSubscriber(ctx).getSuspendedBundles(ctx).keySet());
        this.bundlesToBeCharged.addAll(suspendedToBeRefundEntities);
 
        
        /*
         * Sujeet: Avoiding ClassCastEx.
         * We better use Typed (Generics) Collection 
         */
        suspendedToBeRefundEntities.clear();
        
        suspendedToBeRefundEntities.addAll(this.auxServicesToBeRefund);
       
        suspendedToBeRefundEntities.retainAll(AuxServiceChargingSupport.getCrossed(ctx, suspendedToBeRefundEntities, getOldSubscriber(ctx).getSuspendedAuxServicesList(ctx)));
       
        this.auxServicesToBeRefund.removeAll(AuxServiceChargingSupport.getCrossed(ctx, auxServicesToBeRefund, suspendedToBeRefundEntities));
        
        this.auxServicesToBeCharged.removeAll(AuxServiceChargingSupport.getCrossed(ctx, auxServicesToBeCharged, getOldSubscriber(ctx).getSuspendedAuxServicesList(ctx)));
        
        this.auxServicesToBeCharged.addAll(suspendedToBeRefundEntities);
                  
          
    }
    
    
    // when service is suspended due to cltc, not refund. 
    // when a cltc suspended service is unprovisioned. we need refund if 
    // it is in same billing cycle. 
    public void HandleCltc(Context ctx)
    {
       	this.servicesToBeRefund.addAll(getUnprovisionedCLTCService(ctx));
    }
    
    /**
     * filter out entities which are Not Refundable i.e. for which "Is Refundable" flag is set to false. 
     * @param ctx
     *
     * @author sajid.memon@redknee.com
     * @since 9.3
     */
    protected void  handleRefundable(Context ctx)
    {
        HashSet refundWaived = new HashSet(); 
        HashSet provError = new HashSet(); 
        HashSet unProvError = new HashSet(); 
        
        for ( Iterator i = getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values().iterator(); i.hasNext();)
        {
            SubscriberServices subService = (SubscriberServices) i.next();
            Service service = ServiceChargingSupport.getServiceById(ctx, subService.getServiceId());
            if ( service != null && !service.isRefundable())
            {
            	refundWaived.add(subService);
            	
            	if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Refund to be Waived off for Service ID : " + service.getIdentifier() + " for Subscriber ID : " + getOldSubscriber(ctx).getId() + ", as Is Refundable flag : " + service.isRefundable());
                }

            }
            
        }
        servicesToBeRefund.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeRefund, refundWaived));
        
        
        for ( Iterator i = subscriber.getProvisionedErrorServicesBackup(ctx).values().iterator(); i.hasNext();)
        {
            SubscriberServices subService = (SubscriberServices) i.next();
            Service service = ServiceChargingSupport.getServiceById(ctx, subService.getServiceId());
            if ( service != null )
            {
            	provError.add(subService);
            	
            	if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Add new subscriber ProvisionError service to  provError set, Service ID : " + service.getIdentifier() + " for Subscriber ID : " + getOldSubscriber(ctx).getId() + ", as Is Refundable flag : " + service.isRefundable());
                }
            }
        }
        
        for ( Iterator i = getOldSubscriber(ctx).getUnProvisionedErrorServicesBackup(ctx).values().iterator(); i.hasNext();)
        {
            SubscriberServices subService = (SubscriberServices) i.next();
            Service service = ServiceChargingSupport.getServiceById(ctx, subService.getServiceId());
            if ( service != null )
            {
            	unProvError.add(subService);
            	
            	if (LogSupport.isDebugEnabled(ctx))
                {
            		LogSupport.debug(ctx, this, "Add old subscriber ProvisionError service to  unProvError set, Service ID : " + service.getIdentifier() + " for Subscriber ID : " + getOldSubscriber(ctx).getId() + ", as Is Refundable flag : " + service.isRefundable());
                }

            }
        }
        
        servicesToBeRefund.removeAll(ServiceChargingSupport.getCrossed(ctx, unProvError, provError));
        
        provError.clear();
        unProvError.clear();
        refundWaived.clear();
        for(Iterator i = getOldSubscriber(ctx).getBundles().keySet().iterator(); i.hasNext();)
        {
            Long bundleId = (Long) i.next();
            BundleProfile bundleProfile = BundleChargingSupport.getBundleProfile(ctx, bundleId.longValue());
            if ( bundleProfile != null && !bundleProfile.isRefundable())
            {
                refundWaived.add(bundleId);
                
            	if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Refund to be Waived off for Bundle Profile ID : " + bundleId + " for Subscriber ID : " + getOldSubscriber(ctx).getId() + ", as Is Refundable flag : " + bundleProfile.isRefundable());
                }

            }
        }
        
        bundlesToBeRefund.removeAll(refundWaived); 
        
        refundWaived.clear(); 
        for ( Iterator<SubscriberAuxiliaryService> i = getOldSubscriber(ctx).getProvisionedAuxServiceBackup(ctx).iterator(); i.hasNext();)
        {
            SubscriberAuxiliaryService subAuxService = i.next();
            Long serviceId = subAuxService.getAuxiliaryServiceIdentifier();
            AuxiliaryService auxService = AuxServiceChargingSupport.getAuxiliaryServicById(ctx, serviceId.longValue());
            if ( auxService != null && !auxService.isRefundable())
            {
                refundWaived.add(subAuxService);
                
            	if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Refund to be Waived off for AuxiliaryService ID : " + serviceId + " for Subscriber ID : " + getOldSubscriber(ctx).getId()
                    					+ ", as Is Refundable flag : " + auxService.isRefundable());
                }
            }
        }

        auxServicesToBeRefund.removeAll(AuxServiceChargingSupport.getCrossed(ctx, auxServicesToBeRefund, refundWaived));
        
    }
    
    
    /**
     * Specifically for PIck n Pay Price Plan
     * 1. filter out entities which falls under MRC group "they are non refundable even their refundable flag is on " .
     * 2. Add the services which falls under MRC group to charge again considering it as a complete plan change.
     * 
     * ex: `1. voice service ($25)
     * 		2.`data service ($10)
     * 
     * case 1 : if one more service is added of charge $10 then total of already provisioned service and newly added service
     * 			is considered as a charge which is $45.
     * case 2 : If data service is deleted from it then there is no refund and still the remaining service is charged again which is $25.
     *   
     * @param ctx
     *
     * @author ishan.batra
     * @since 9.9
     */
    protected void  handleMRCGroup(Context ctx)
    {
    	HashSet MRCServices = new HashSet();  
    	HashSet oldMRCServices = new HashSet();
    	HashSet MRCBundles = new HashSet();  
    	HashSet oldMRCBundles = new HashSet();
    	SubscriberServices primaryService = null;


    	try {
    		Map services;

    		services = subscriber.getPricePlan(ctx).getServiceFees(ctx);

    		for ( Iterator i = subscriber.getProvisionedSubscriberServices(ctx).iterator(); i.hasNext();)
    		{
    			SubscriberServices subServices = (SubscriberServices) i.next();
    			ServiceFee2 serviceFee = (ServiceFee2) services.get(subServices.getServiceId());

    			if ( serviceFee != null && serviceFee.getApplyWithinMrcGroup())
    			{
    				MRCServices.add(subServices);

    				totalMrcCharge += serviceFee.getFee();

    				if (LogSupport.isDebugEnabled(ctx))
    				{
    					LogSupport.debug(ctx, this, "Service ID : " + subServices.getServiceId() + " for Subscriber ID : " + getOldSubscriber(ctx).getId() + 
    							"falls under MRC group so it charged fully");
    				}
    			}
    			
    			if ( serviceFee != null && serviceFee.isPrimary())
    			{
    				primaryService = subServices;

    				if (LogSupport.isDebugEnabled(ctx))
    				{
    					LogSupport.debug(ctx, this, "Service ID : " + subServices.getServiceId() + " for Subscriber ID : " + getOldSubscriber(ctx).getId() + 
    							"is Primary True");
    				}
    			}
    		}    		

    		if(getOldSubscriber(ctx) != null)
    		{
    			services = getOldSubscriber(ctx).getRawPricePlanVersion(ctx).getServiceFees(ctx);

    			for ( Iterator i = getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values().iterator(); i.hasNext();)
    			{
    				SubscriberServices subServices = (SubscriberServices) i.next();
    				ServiceFee2 serviceFee = (ServiceFee2) services.get(subServices.getServiceId());

    				if ( serviceFee != null && serviceFee.getApplyWithinMrcGroup())
    				{
    					oldMRCServices.add(subServices);

    					if (LogSupport.isDebugEnabled(ctx))
    					{
    						LogSupport.debug(ctx, this, "Service ID : " + subServices.getServiceId() + " for Subscriber ID : " + getOldSubscriber(ctx).getId() + 
    								"falls under MRC group so it not refunbable");
    					}
    				}
    			}
    		}

    		//Mrc group services are not refundable even if they are in suspended state
    		servicesToBeRefund.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeRefund, oldMRCServices));


    		for(Iterator i = SubscriberBundleSupport.getSubscribedBundles(ctx, subscriber).values().iterator(); i.hasNext();)
    		{
    			BundleFee bundleFee = (BundleFee) i.next();

    			if ( bundleFee != null && bundleFee.getApplyWithinMrcGroup())
    			{
    				MRCBundles.add(bundleFee.getId());

    				totalMrcCharge += bundleFee.getFee();

    				if (LogSupport.isDebugEnabled(ctx))
    				{
    					LogSupport.debug(ctx, this, "Bundle ID : " + bundleFee.getId() + " for Subscriber ID : " + getOldSubscriber(ctx).getId() + 
    							"falls under MRC group so it will charged fully");
    				}

    			}
    		}    		

    		if(getOldSubscriber(ctx) != null)
    		{
    			for(Iterator i = SubscriberBundleSupport.getSubscribedBundles(ctx, getOldSubscriber(ctx) ).values().iterator(); i.hasNext();)
    			{
    				BundleFee bundleFee = (BundleFee) i.next();

    				if ( bundleFee != null && bundleFee.getApplyWithinMrcGroup())
    				{
    					oldMRCBundles.add(bundleFee.getId());

    					if (LogSupport.isDebugEnabled(ctx))
    					{
    						LogSupport.debug(ctx, this, "Bundle ID : " + bundleFee.getId() + " for Subscriber ID : " + getOldSubscriber(ctx).getId() + 
    								"falls under MRC group so it not refunbable");
    					}

    				}
    			}
    		}

    		if(primaryService == null)
    		{
    			totalMrcCharge = 0;
    			if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this, "Subscriber ID : " + getOldSubscriber(ctx).getId() + 
							" does not have any primary service ");
				}
    			return;
    		}
    		
    		//Mrc group bundles are not refundable
    		bundlesToBeRefund.removeAll(oldMRCBundles); 

    		boolean serviceCharged = (Boolean) ctx.get(ExtendSubscriberExpiryHome.SKIP_PROVISIONING_AND_CHARGING_CALCULATIONS, Boolean.FALSE);
    		
    		// skip charging for MRC services if expiry is on future and its a state change(specific check for barred to active)
    		if(allowChargeOnStateChange && !isPrimaryServiceExpired(subscriber, primaryService) &&
    				subscriber.getState().equals(SubscriberStateEnum.ACTIVE) && getOldSubscriber(ctx).getState().equals(SubscriberStateEnum.LOCKED))
    		{
    			servicesToBeCharged.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeCharged, MRCServices));
    			bundlesToBeCharged.removeAll(MRCBundles);
    			
    			totalMrcCharge = 0;
    			
				if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this, "Moving Subscriber ID : " + getOldSubscriber(ctx).getId() + 
							" from locked to active state , removing all services and bundles of MRC group bcz of expiry date in future ");
				}
    		}    		
    		else if(  (!(MRCServices.equals(oldMRCServices) && MRCBundles.equals(oldMRCBundles))) || 
    				(!serviceCharged && allowChargeOnStateChange) )
    		{
    			//Mrc group services has to be charged if there is a change in a group
    			servicesToBeCharged.addAll(MRCServices);
    			//Mrc group Bundles has to be charged if there is a change in a group
    			bundlesToBeCharged.addAll(MRCBundles);
    			
    			if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this, "Subscriber ID : " + getOldSubscriber(ctx).getId() + 
							" . Adding MRC services and bunndles to get charged again ");
				}
    		} else{
    			totalMrcCharge = 0; // As there is no change in group the MRC charge is 0	
    		}


    	} catch (HomeException e) {
    		new MajorLogMsg(ctx, "fail to get PPV for sub" + subscriber.getId(), e).log(ctx);
    	}     

    }
    
    public boolean isPrimaryServiceExpired(Subscriber sub, final SubscriberServices primaryService)
    {
    	boolean isPrimaryServiceExpired = false;
        if (primaryService.getNextRecurringChargeDate() !=null && primaryService.getNextRecurringChargeDate().after(Subscriber.NEVER_EXPIRE_CUTOFF_DATE)
                && primaryService.getNextRecurringChargeDate().before(new Date()))
        {
        	isPrimaryServiceExpired = true;
        }

        return isPrimaryServiceExpired;
    }
    
    public void addAllCLTCServices(Context ctx)
    {
		this.servicesToBeRefund.addAll(this.getOldSubscriber(ctx).getCLTCSubscriberServices(ctx));  
    }
    
    
    //subscriber service refactoring is half cooked. it could be updated, deleted, or created in many places
    // in the pipe, as a result, there is not way know if the subscriber service is unprovisioned or not. 
    // it is very very bad. the logic is copied from SubscriberClearSuspendedEntities.
    private Collection getUnprovisionedCLTCService(Context ctx)
    {
    	Collection ret = new HashSet(); 
        try
        {
        	
        	PricePlanVersion version = this.getNewSubscriber().getRawPricePlanVersion(ctx);
            
            Set serviceFees = this.getNewSubscriber().getIntentToProvisionServiceIds();
            Map services = new HashMap(this.getNewSubscriber().getSuspendedSubscriberServices(ctx));
            Map PPserviceFees = version.getServicePackageVersion().getServiceFees();
            Iterator it = services.keySet().iterator();
            while (it.hasNext())
            {
                Object key = it.next();
                if ( !serviceFees.contains(key) || (!PPserviceFees.containsKey(key) && serviceFees.contains(key)))
                {
                    ret.add(services.get(key));
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, this, "Cannot erase service suspention record for sub = " + this.getNewSubscriber().getId(), e);
        }

        return ret; 
    }
    
    
    public Map getBundleOverUsageCharge() {
        return bundleOverUsageCharge;
    }


    public void setBundleOverUsageCharge(Map bundleOverUsageCharge) {
        this.bundleOverUsageCharge = bundleOverUsageCharge;
    }


    public Map getBundleUnderUsageRefund() {
        return bundleUnderUsageRefund;
    }


    public void setBundleUnderUsageRefund(Map bundleUnderUsageRefund) {
        this.bundleUnderUsageRefund = bundleUnderUsageRefund;
    } 

    public static Collection<? extends Enum> getNonChargeableStates()
    {
        return NON_CHARGEABLE_STATES;
    }
    
    public static Collection<? extends Enum> getNonChargeablePostpaidStates()
    {
        return NON_POSTPAID_CHARGEABLE_STATES;
    }
    
    
    
    
    
    
    protected Map bundleOverUsageCharge; 
    protected Map bundleUnderUsageRefund;
    
    private static final Collection<SubscriberStateEnum> NON_CHARGEABLE_STATES = 
        Arrays.asList(
            SubscriberStateEnum.AVAILABLE,
            SubscriberStateEnum.IN_ARREARS,
            SubscriberStateEnum.IN_COLLECTION
            );
    
    private static final Collection<SubscriberStateEnum> NON_POSTPAID_CHARGEABLE_STATES = 
        Arrays.asList(
            SubscriberStateEnum.IN_ARREARS,
            SubscriberStateEnum.IN_COLLECTION
            );
    
    
    
    
}
