package com.trilogy.app.crm.home.sub;


import com.trilogy.app.crm.bean.CampaignConfig;
import com.trilogy.app.crm.log.ERLogger;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class NoDeleteMarketingCampaign extends HomeProxy {

	
	public NoDeleteMarketingCampaign(Context ctx, Home home)
    {
        super(ctx,home);
    }
	

    
    /**
     * 
     */
    public Object create(Context ctx, Object obj) throws HomeException
    {           
    	CampaignConfig campaignConfig = (CampaignConfig) obj;
		Object r = super.create(ctx, obj);
		ERLogger.logMarketingCampaignER(ctx, campaignConfig.getSpid(), campaignConfig.getCampaignId(), (short)0);
		return r;
    	
    }
    
    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    
    public Object store(Context ctx, Object obj) throws HomeException 
    {        
    	CampaignConfig campaignConfig = (CampaignConfig) obj;
		Object r = super.store(ctx,obj);
		ERLogger.logMarketingCampaignER(ctx, campaignConfig.getSpid(), campaignConfig.getCampaignId(), (short)1);
		return r;
    }

    /**
     * not to delete campaign id with ID 0
     */
    
    public void remove(Context ctx, Object obj) throws HomeException 
    {    
    	CampaignConfig campaignConfig = (CampaignConfig) obj;
    	if(campaignConfig.getCampaignId()!=0)
    			{
    			super.remove(ctx,obj);
    			ERLogger.logMarketingCampaignER(ctx, campaignConfig.getSpid(), campaignConfig.getCampaignId(), (short)3);
    			}
    	else
    		throw new HomeException("Default Marketing Campaign 0 can not be deleted");
    }

}