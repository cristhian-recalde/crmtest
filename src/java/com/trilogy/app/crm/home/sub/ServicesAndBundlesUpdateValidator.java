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

package com.trilogy.app.crm.home.sub;

import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * Validate to Prevent In-Arrears state subscribers from Updating Services,Auxiliary Services and Bundles
 * 
 * @author piyush.shirke@redknee.com
 * @since 9.5.4
 */
public class ServicesAndBundlesUpdateValidator implements Validator{

private static Validator _instance;
    
    
    private ServicesAndBundlesUpdateValidator()
    {
    }

    public static Validator instance()
    {
        if (_instance == null)
        {
            _instance = new ServicesAndBundlesUpdateValidator();
        }
        return _instance;
    }


	@Override
	public void validate(Context ctx, Object object)
			throws IllegalStateException {
	

        final Subscriber newSub = (Subscriber) object;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        boolean isError = false;
        
        if (SubscriberStateEnum.IN_ARREARS.equals(newSub.getState()))
        {
                       
            CompoundIllegalStateException compound = new CompoundIllegalStateException();
            
            Set<ServiceFee2ID> oldServices = oldSub.getServices(ctx);
            Set<ServiceFee2ID> newServices = newSub.getServices(ctx);

            Set<ServiceFee2ID> newSvcFee2IDSet = new HashSet<ServiceFee2ID>();
            newSvcFee2IDSet.addAll(newServices);

            boolean isSameSize = (oldServices.size() == newServices.size());
          
            if(!isSameSize)
            {
            	isError =  true;
            }else{
            	newSvcFee2IDSet.removeAll(oldServices);
            	  if(newSvcFee2IDSet.size() > 0)
            		  isError = true;  	 
            }
            if(isError){
            	compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.PRICE_PLAN,
            			"Updating PricePlan Services to an In-Arrears state subscriber is not allowed."));
            	isError = false;
            }
            
            Set<Long> newSet = new HashSet<Long>();
            newSet.clear();
            Set<Long> oldAuxServices = oldSub.getAuxiliaryServiceIds(ctx);
            Set<Long> newAuxServices = newSub.getAuxiliaryServiceIds(ctx);
            
            newSet.addAll(newAuxServices);
            
            isSameSize = (oldAuxServices.size() == newAuxServices.size());
           
            if(!isSameSize)
            {
                isError = true;
            }else{
            	 newSet.removeAll(oldAuxServices);
            	 if(newSet.size() > 0)
            		  isError = true;
            }
            if(isError){
            	compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.AUXILIARY_SERVICES,
                    "Updating Auxiliary Services to an In-Arrears state subscriber is not allowed."));
            	isError = false;
            }
            
            newSet.clear();
            Set<Long> oldBundles = oldSub.getBundles().keySet();
            Set<Long> newBundles = newSub.getBundles().keySet();
            
            newSet.addAll(newBundles);
          
            isSameSize = (oldBundles.size() == newBundles.size());
            
            if(!isSameSize)
            {
            	isError = true;
            }else{
            	newSet.removeAll(oldBundles);
            	if(newSet.size() > 0)
            		isError = true;
            }
            
            if(isError){
            	compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.BUNDLES,
                        "Updating Bundles to an In-Arrears state subscriber is not allowed."));
            }
            
            compound.throwAll();
        }
	}



}
