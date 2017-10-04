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

import com.trilogy.app.crm.bean.SubscriberCategory;
import com.trilogy.app.crm.bean.SubscriberCategoryXInfo;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;

public class DeleteCatagoryHome extends HomeProxy {

	private Home subscriberHome;
	public DeleteCatagoryHome(Context ctx, Home home)
    {
        super(ctx,home);
        subscriberHome = (Home) ctx.get(SubscriberHome.class);
    }
	

    
    /**
     * Save the association between subscriber to its bundles to the BundleAuxiliaryServiceHome
     */
    public Object create(Context ctx, Object obj) throws HomeException
    {           
    	SubscriberCategory subCat = (SubscriberCategory)obj;
    	Home thisSpidCategories = this.where(ctx,new EQ(SubscriberCategoryXInfo.SPID, Integer.valueOf(subCat.getSpid())));
    	SubscriberCategory existingSubCat = (SubscriberCategory) thisSpidCategories.find(new EQ(SubscriberCategoryXInfo.RANK, Integer.valueOf(subCat.getRank())));
    	//SubscriberCategory subCat =  (SubscriberCategory)obj;
    	if (existingSubCat==null)
    		return super.create(ctx, obj);
    	else
    		throw new HomeException("Rank should be unique across categories of a Service Provider, Category ID " + existingSubCat.getCategoryId() + " has same rank!!!" );
    }
    
    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    
    public Object store(Context ctx, Object obj) throws HomeException 
    {   
    	SubscriberCategory subCat =  (SubscriberCategory)obj;
    	if (subCat.getCategoryId()==0)
    		throw new HomeException("Default Category 0 can not be changed");
    	Home thisSpidCategories = this.where(ctx,new EQ(SubscriberCategoryXInfo.SPID, Integer.valueOf(subCat.getSpid())));
    	SubscriberCategory existingSubCat = (SubscriberCategory) thisSpidCategories.find(new EQ(SubscriberCategoryXInfo.RANK, Integer.valueOf(subCat.getRank())));
    	   	
    	if (existingSubCat==null)
    		return super.store(ctx, obj);
    	else if (subCat.getCategoryId()==existingSubCat.getCategoryId())
    		return super.store(ctx, obj);
    	else
    		throw new HomeException("Rank should be unique across categories of a Service Provider, Category ID " + existingSubCat.getCategoryId() + " has same rank!!!" );
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#remove(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    private boolean categoryIsFree(long categoryId, Context ctx_)
    {
    	//this function finds out of a subscriber category is being used.
    	if (subscriberHome==null)
    		subscriberHome = (Home) ctx_.get(SubscriberHome.class);
    	//Review comment
    	try {
			if 	(subscriberHome.find(new EQ(SubscriberXInfo.SUBSCRIBER_CATEGORY, Long.valueOf(categoryId)))==null)
				return true;
			else return false;
		} catch (HomeException e) {
			new DebugLogMsg(this,"Could not traverse subscriber home, returning category unused",e).log(ctx_);
			return false;
			
		}
    	
    	/*try {
    		Collection subColl = subscriberHome.selectAll();
    		Iterator subItr =  subColl.iterator();
    		while(subItr.hasNext())
    		{
    			if(categoryId==((Subscriber)subItr.next()).getSubscriberCategory())
    				return false;
    		}
    		
    		
		} catch (HomeInternalException e) {
			e.printStackTrace();
			return false;
			
		} catch (HomeException e) {
			e.printStackTrace();
			return false;
		}*/
		//return true;
    }
    public void remove(Context ctx, Object obj) throws HomeException 
    {    
    	long id = ((SubscriberCategory)obj).getCategoryId();
    	if (id==0)
    		throw new HomeException("Default Subscriber Category 0 can not be deleted");
    		
    	if(categoryIsFree(id,ctx)==true)
    			super.remove(ctx, obj);
    	else throw new HomeException("Subscriber Category not free");
    }

}
