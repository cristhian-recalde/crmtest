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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.BlackList;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.web.control.CustomisedIdentificationTypeSpidKeyWebControl;

/**
 * Home that populates service provider value based on identification type of Blacklist entry
 *
 * @author isha.aderao
 * @since 9.7.1
 */
public class BlackListSpidSettingHome extends HomeProxy {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public BlackListSpidSettingHome() 
	{
	
	}
	
	public BlackListSpidSettingHome(final Context ctx, final Home delegate )
	{
		super(ctx, delegate);
	}

	 public Object create(Context ctx, Object obj) throws HomeException
	 {
	    FrameworkSupportHelper.get(ctx).initExceptionListener(ctx, this);
	    setBlackListSpid(ctx, (BlackList) obj);
	    Object blackList = (BlackList) super.create(ctx,obj);
	    FrameworkSupportHelper.get(ctx).printCapturedExceptionsAsWarnings(ctx, this);
	    return blackList;
	 }
	    

    public Object store(Context ctx, Object obj) throws HomeException
    {
        FrameworkSupportHelper.get(ctx).initExceptionListener(ctx, this);
        setBlackListSpid(ctx, (BlackList) obj);
        Object blackList = (BlackList) super.store(ctx,obj);
        FrameworkSupportHelper.get(ctx).printCapturedExceptionsAsWarnings(ctx, this);
        return blackList;
    }
    
    
    private void setBlackListSpid(Context ctx, BlackList blackList) throws HomeException
    {
    	int identificationType = blackList.getIdType();		
	
		Identification identificationTypeEntry = null;
		try 
		{
			identificationTypeEntry = HomeSupportHelper.get(ctx).findBean(ctx, Identification.class, Integer.valueOf(identificationType));
		} 
		catch (HomeException e) 
		{
			LogSupport.major(ctx, CustomisedIdentificationTypeSpidKeyWebControl.class.getName(), 
					"Not able to find Identification entry. Please select appropriate identification type.");
			throw new HomeException("Not able to find Identification entry. Please select appropriate identification type.", e);
		}
		
		if(identificationTypeEntry != null)
		{
			try 
			{
				CRMSpid spid = HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, Integer.valueOf(identificationTypeEntry.getSpid()));
				
				blackList.setSpid(spid.getSpid());
			} 
			catch (HomeException e) 
			{
				LogSupport.major(ctx, CustomisedIdentificationTypeSpidKeyWebControl.class.getName(), "Not able to find spid for identification type "+identificationType 
						+"Please check Identification configuration.");
				throw new HomeException("Not able to find spid for identification type "+identificationType 
						+"Please check Identification configuration.", e);
			}
		}
		else
		{
			LogSupport.major(ctx, CustomisedIdentificationTypeSpidKeyWebControl.class.getName(), "Not able to find spid for identification type "+identificationType 
					+"Please check Identification configuration.");
			throw new HomeException("Not able to find spid for identification type "+identificationType 
					+"Please check Identification configuration.");
		}
    }
}
