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
package com.trilogy.app.crm.home.account;

import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.DiscountClassTemplateInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.logger.LoggerSupport;

/**
 * 
 * Populate value in the adjustment type
 * 
 * @author ankit.nagpal@redknee.com
 * since 9_7_2
 */

public class PopulateAdjustmentTypeHome extends HomeProxy
{
    private static final long serialVersionUID = 1L;

    public PopulateAdjustmentTypeHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
    
    @Override
    public Object store(Context ctx,final Object obj)
    throws HomeException
    {
    	DiscountClassTemplateInfo newDiscountClassTemplateInfo = (DiscountClassTemplateInfo)obj; 

    	serviceAdjustmentType(ctx, newDiscountClassTemplateInfo);
    	auxServiceAdjustmentType(ctx, newDiscountClassTemplateInfo);
    	bundleAdjustmentType(ctx, newDiscountClassTemplateInfo);
    	auxBundleAdjustmentType(ctx, newDiscountClassTemplateInfo);
    	return super.store(ctx, newDiscountClassTemplateInfo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, Object obj) throws HomeException
    {
    	DiscountClassTemplateInfo newDiscountClassTemplateInfo = (DiscountClassTemplateInfo)obj; 
    	serviceAdjustmentType(ctx, newDiscountClassTemplateInfo);
    	auxServiceAdjustmentType(ctx, newDiscountClassTemplateInfo);
    	bundleAdjustmentType(ctx, newDiscountClassTemplateInfo);
    	auxBundleAdjustmentType(ctx, newDiscountClassTemplateInfo);
    	
        return super.create(ctx, newDiscountClassTemplateInfo);
    }
    
    private void serviceAdjustmentType(Context ctx, DiscountClassTemplateInfo newDiscountClassTemplateInfo)
    {
    	Set<Long> services = newDiscountClassTemplateInfo.getSetOfServices();
    	Home home = (Home) ctx.get(ServiceHome.class);
    	
    	Set<Integer> setAdjustmentType = new HashSet<Integer>();
    	
    	for ( Long service : services)
    	{
    		Service serv = null;
    		try {
				serv = (Service) home.find(ctx, service);
				if( serv != null)
				{
					setAdjustmentType.add(serv.getAdjustmentType());				
				}
			} catch (HomeException e) {
				LogSupport.major(ctx, this, e);
			}
    	}
    	newDiscountClassTemplateInfo.setServicesAdjustmentType(setAdjustmentType);    	    	
    }
    
    private void auxServiceAdjustmentType(Context ctx, DiscountClassTemplateInfo newDiscountClassTemplateInfo)
    {
    	Set<Long> auxServices = newDiscountClassTemplateInfo.getSetOfAux();
    	Home home = (Home) ctx.get(AuxiliaryServiceHome.class);
    	
    	Set<Integer> setAdjustmentType = new HashSet<Integer>();
    	
    	for ( Long auxService : auxServices)
    	{
    		AuxiliaryService auxServ = null;
    		try {
    			auxServ = (AuxiliaryService) home.find(ctx, auxService);
				if( auxServ != null)
				{
					setAdjustmentType.add(auxServ.getAdjustmentType());				
				}
			} catch (HomeException e) {
				LogSupport.major(ctx, this, e);
			}
    	}
    	newDiscountClassTemplateInfo.setAuxServicesAdjustmentType(setAdjustmentType);    	    	
    }
    
    private void bundleAdjustmentType(Context ctx, DiscountClassTemplateInfo newDiscountClassTemplateInfo)
    {
    	Set<Long> bundleServices = newDiscountClassTemplateInfo.getSetOfbundleservice();
    	Home home = (Home) ctx.get(BundleProfileHome.class);
    	
    	Set<Integer> setAdjustmentType = new HashSet<Integer>();
    	
    	for ( Long bundleProfile : bundleServices)
    	{
    		BundleProfile bundleProf = null;
    		try {
    			bundleProf = (BundleProfile) home.find(ctx, bundleProfile);
				if( bundleProf != null && !bundleProf.isAuxiliary())
				{
					setAdjustmentType.add(bundleProf.getAdjustmentType());				
				}
			} catch (HomeException e) {
				LogSupport.major(ctx, this, e);
			}
    	}
    	newDiscountClassTemplateInfo.setBundleServicesAdjustmentType(setAdjustmentType);    	    	
    }
    
    private void auxBundleAdjustmentType(Context ctx, DiscountClassTemplateInfo newDiscountClassTemplateInfo)
    {
    	Set<Long> auxBundleServices = newDiscountClassTemplateInfo.getSetOfauxbundle();
    	Home home = (Home) ctx.get(BundleProfileHome.class);
    	
    	Set<Integer> setAdjustmentType = new HashSet<Integer>();
    	
    	for ( Long bundleProfile : auxBundleServices)
    	{
    		BundleProfile bundleProf = null;
    		try {
    			bundleProf = (BundleProfile) home.find(ctx, bundleProfile);
				if( bundleProf != null && bundleProf.isAuxiliary())
				{
					setAdjustmentType.add(bundleProf.getAuxiliaryAdjustmentType());				
				}
			} catch (HomeException e) {
				LogSupport.major(ctx, this, e);
			}
    	}
    	newDiscountClassTemplateInfo.setAuxBundleServicesAdjustmentType(setAdjustmentType);    	    	
    }
}
