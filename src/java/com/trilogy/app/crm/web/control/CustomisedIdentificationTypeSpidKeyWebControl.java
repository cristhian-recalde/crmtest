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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.BlackList;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidKeyWebControl;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationHome;


/**
 * Custom web control that retrieves SPID of Identification Type of the BlackList entry
 *
 * @author isha.aderao
 * @since 9.7.1
 */
public class CustomisedIdentificationTypeSpidKeyWebControl extends CRMSpidKeyWebControl {


	/**
	 * {@inheritDoc}
	 */
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj) 
	{
		final BlackList blackList = (BlackList) ctx.get(AbstractWebControl.BEAN);
		final Context subCtx = ctx.createSubContext();
		
		int identificationType = blackList.getIdType();		
		Home identificationHome = (Home) ctx.get(IdentificationHome.class);
		//identificationHome = identificationHome.where(ctx, new EQ(IdentificationXInfo.CODE, Integer.valueOf(identificationType)));
		
		Identification identificationTypeEntry = null;
		try 
		{
			identificationTypeEntry = (Identification) identificationHome.find(subCtx, Integer.valueOf(identificationType));
		} 
		catch (HomeInternalException e) 
		{
			LogSupport.major(subCtx, CustomisedIdentificationTypeSpidKeyWebControl.class.getName(), "Not able to find Identification entry. Please select appropriate identification type.");
		} catch (HomeException e) 
		{
			LogSupport.major(subCtx, CustomisedIdentificationTypeSpidKeyWebControl.class.getName(), "Not able to find Identification entry. Please select appropriate identification type.");
		}
		
		Home crmSpidHome = (Home) ctx.get(CRMSpidHome.class);
		
		if(identificationTypeEntry != null)
		{
			crmSpidHome = crmSpidHome.where(subCtx, new EQ(CRMSpidXInfo.ID, Integer.valueOf(identificationTypeEntry.getSpid())));
		}
		
		subCtx.put(getHomeKey(), crmSpidHome);
		super.toWeb(subCtx, out, name, obj);
		
	}

}
