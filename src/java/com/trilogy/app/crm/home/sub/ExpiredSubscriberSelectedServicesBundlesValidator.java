package com.trilogy.app.crm.home.sub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.batik.svggen.font.table.GsubTable;

import antlr.collections.List;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * Validator to Prevent expired subscribers from adding PricePlan OPTIONAL and/or Auxiliary services or bundles
 * 
 * @author mangaraj.sahoo@redknee.com
 * @since 9.3
 */
public class ExpiredSubscriberSelectedServicesBundlesValidator implements Validator
{

    private static Validator instance;
    
    
    private ExpiredSubscriberSelectedServicesBundlesValidator()
    {
    }


    public static Validator instance()
    {
        if (instance == null)
        {
            instance = new ExpiredSubscriberSelectedServicesBundlesValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object object)
    {
        final Subscriber newSub = (Subscriber) object;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);       
        
        
        CompoundIllegalStateException compound = new CompoundIllegalStateException();
        
        
        
        if (SubscriberStateEnum.EXPIRED.equals(newSub.getStateWithExpired()))
        {
            if (newSub.getPricePlan() != oldSub.getPricePlan())
            {
              /*This validator is not responsible for this case - PricePlan change.*/
                return;
            }
            
            if (newSub.getPricePlanVersion() != oldSub.getPricePlanVersion())
            {
              /*This validator is not responsible for this case - PricePlanVersion change.*/
                return;
            }
            
            if (isPricePlanVersionUpdate(ctx, newSub))
            {
              /*This validator is not responsible for this case - PricePlanVersion update.*/
                return;
            }
            

			
            Set<Long> oldServices = (Set<Long>) ((HashSet<ServiceFee2ID>) oldSub.getServices(ctx)).clone();
            Set<Long> newServices = (Set<Long>) ((HashSet<ServiceFee2ID>) newSub.getServices(ctx)).clone();
            
            
            HashMap oldBundlesMap = (HashMap) ((HashMap) oldSub.getBundles()).clone();    		
    		Set<Long> oldBundles = (Set<Long>) oldBundlesMap.keySet();
    		
    		HashMap newBundlesMap = (HashMap) ((HashMap) newSub.getBundles()).clone();    		
    		Set<Long> newBundles = (Set<Long>) newBundlesMap.keySet();

	    	boolean isPickNPayPricePlan;
	    	
			try {
				PricePlan pricePlan = PricePlanSupport.getPlan(ctx, newSub.getPricePlan()); 
				isPickNPayPricePlan = pricePlan.getPricePlanSubType().equals(PricePlanSubTypeEnum.PICKNPAY) ? true:false;
		    	
	            if (isPickNPayPricePlan)
	            {
	              /*This validator is not responsible for this case - PricePlanOption change for Pick N PAy*/
	            	
	            	HashSet<Long> newMRCGroupservices = getMRCGroupServices(ctx, newSub);
	            	HashSet<Long> oldMRCGroupservices = getMRCGroupServices(ctx, oldSub);
	            	
	            	HashSet<Long> newMRCGroupBundles = getMRCGroupBundles(ctx, newSub);
	            	HashSet<Long> oldMRCGroupBundles = getMRCGroupBundles(ctx, oldSub);

	            	// removing MRC group services and bundles from the list bcz we want to skip the validation for them
	            	
	            	newServices.removeAll(newMRCGroupservices);
	            	oldServices.removeAll(oldMRCGroupservices);
	            	
	            	newBundles.removeAll(newMRCGroupBundles);
	            	oldBundles.removeAll(oldMRCGroupBundles);
	            	
	            	if (LogSupport.isDebugEnabled(ctx))
	            	{
	            		LogSupport.debug(ctx, this, "newServices =" +newServices);
	            		LogSupport.debug(ctx, this, "oldServices =" +oldServices);
	            		LogSupport.debug(ctx, this, "newBundles =" +newBundles);
	            		LogSupport.debug(ctx, this, "oldBundles =" +oldBundles);
	            		
	            		LogSupport.debug(ctx, this, "newServices2 =" +newSub.getServices(ctx));
	            		LogSupport.debug(ctx, this, "oldServices2 =" +oldSub.getServices(ctx));
	            		LogSupport.debug(ctx, this, "newBundles2 =" +newSub.getBundles());
	            		LogSupport.debug(ctx, this, "oldBundles2 =" +oldSub.getBundles());
	            	}
	            	
	            }
			} catch (HomeException e) {
				
				compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.PRICE_PLAN,
                        "Errror in retrieving the current price plan details"));
			}
            

            Set<Long> servicesToAdd = new HashSet<Long>();
            for (Long svcId : newServices)
            {
            	if(!oldServices.contains(svcId))
            	{
            		servicesToAdd.add(svcId);
            	}
            }
            
            if(servicesToAdd.size() > 0)
            {
                compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.PRICE_PLAN,
                        "Adding optional services to an expired subscription is not allowed."));
            }
            
            Set<Long> oldAuxServices = oldSub.getAuxiliaryServiceIds(ctx);
            Set<Long> newAuxServices = newSub.getAuxiliaryServiceIds(ctx);
            Set<Long> auxServicesToAdd = new HashSet<Long>();
            
            for (Long auxSvcId : newAuxServices)
            {
            	if(!oldAuxServices.contains(auxSvcId))
            	{
            		auxServicesToAdd.add(auxSvcId);
            	}
            }
            
            if(auxServicesToAdd.size() > 0)
            {
                compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.AUXILIARY_SERVICES,
                        "Adding auxiliary services to an expired subscription is not allowed."));
            }

            Set<Long> bundlesToAdd = new HashSet<Long>();
            
            for (Long bundleId : newBundles)
            {
            	if(!oldBundles.contains(bundleId))
            	{
            		bundlesToAdd.add(bundleId);
            	}
            }
            
            if(bundlesToAdd.size() > 0)
            {
                compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.BUNDLES,
                        "Adding optional and auxiliary bundles to an expired subscription is not allowed."));
            }
            compound.throwAll();

        }
    }
    
    
    /**
     * Checks if it is a Subscriber's PricePlan version update request.
     * 
     * @param ctx
     * @param newSub
     * @return
     */
    private boolean isPricePlanVersionUpdate(final Context ctx, final Subscriber newSub)
    {
        PricePlanVersion currentPricePlan = null;
        final CompoundIllegalStateException compound = new CompoundIllegalStateException();
        try
        {
            currentPricePlan = PricePlanSupport.getCurrentVersion(ctx, newSub.getPricePlan());
        }
        catch (HomeException e)
        {
            String msg = "Failed to look up current price plan version for plan " + newSub.getPricePlan();
            LogSupport.minor(ctx, this, msg + " for subscriber " + newSub.getId(), e);
            compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.PRICE_PLAN, msg));
            compound.throwAll();
        }
        
        if (currentPricePlan == null)
        {
            String msg = "The current price plan version NOT FOUND for plan " + newSub.getPricePlan();
            LogSupport.minor(ctx, this, msg + " for subscriber " + newSub.getId());
            compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.PRICE_PLAN, msg));
            compound.throwAll();
        }
        
        if (newSub.getPricePlanVersion() != currentPricePlan.getVersion())
        {
            return true;
        }
        
        return false;
    }
    
    private HashSet<Long> getMRCGroupServices(Context ctx, final Subscriber subscriber) throws HomeException
    {
    	HashSet<Long> MRCServices = new HashSet<Long>();
    	Map services;
    	services = subscriber.getPricePlan(ctx).getServiceFees(ctx);

		for ( ServiceFee2ID subServiceId : subscriber.getServices(ctx))
		{
			ServiceFee2 serviceFee = (ServiceFee2) services.get(subServiceId);

			if ( serviceFee != null && serviceFee.getApplyWithinMrcGroup())
			{
				MRCServices.add(subServiceId.getServiceId());
				
				if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this, "Service ID : " + subServiceId + " for Subscriber ID : " + subscriber.getId() + 
							"falls under MRC group");
				}
			}
		}
		
		return MRCServices;
    }
    
    private HashSet<Long> getMRCGroupBundles(Context ctx, final Subscriber subscriber) throws HomeException
    {
    	HashSet<Long> MRCBundles = new HashSet<Long>();
    	
    	Collection<BundleFee> bundleFees = SubscriberBundleSupport.getSubscribedBundles(ctx, subscriber).values();

		for ( BundleFee fee : bundleFees)
		{
			if ( fee != null && fee.getApplyWithinMrcGroup())
			{
				MRCBundles.add(fee.getId());
				
				if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this, "Bundle ID : " + fee.getId() + " for Subscriber ID : " + subscriber.getId() + 
							"falls under MRC group");
				}
			}
		}
		
		return MRCBundles;
    }
    
}
