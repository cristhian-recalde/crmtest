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
package com.trilogy.app.crm.home.grr;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.grr.XMLTemplateConfig;
import com.trilogy.app.crm.grr.ClientToXMLTemplateConfigHome;
import com.trilogy.app.crm.grr.ClientToXMLTemplateConfigXInfo;

/**
 * @author sajid.memon@redknee.com
 *  
 */
public class XMLTemplateConfigCheckHome extends HomeProxy
{
    public XMLTemplateConfigCheckHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    public void remove(Context ctx, Object obj) throws HomeException
    {      
    	XMLTemplateConfig bean = (XMLTemplateConfig) obj;
        if (isUsedByClientToXMLTemplateConfig(ctx,((XMLTemplateConfig)obj).getTemplateID()))
        {
            throw new HomeException("Cannot Delete : XML Template is being used by Client to XML Template Mappings.");
        }
        
        if (isUnAuthorizedForSystemTemplates(ctx, bean))
        {
            throw new HomeException("Cannot Delete : You are not authorized to delete System Defined Templates.");
        }
        super.remove(ctx, obj);
    }
    
    public Object store(Context ctx, Object obj) throws HomeException
    {      
    	XMLTemplateConfig bean = (XMLTemplateConfig) obj;
        
        if (isUnAuthorizedForSystemTemplates(ctx, bean))
        {
            throw new HomeException("Cannot Update : You are not authorized to update System Defined Templates. Please copy this template and perform necessary modifications to the new template.");
        }
        return super.store(ctx, obj);
    }
    
    private boolean isUsedByClientToXMLTemplateConfig(Context ctx, String templateID)
    {
        Home home = (Home)ctx.get(ClientToXMLTemplateConfigHome.class);
        Object clientToXMLTemplateConfig = null;
        try
        {
        	clientToXMLTemplateConfig = home.find(ctx, new EQ(ClientToXMLTemplateConfigXInfo.TEMPLATE_ID, templateID));
        }
        catch (Exception e)
        {
            
        }
        return (clientToXMLTemplateConfig != null);
    }
    
    private boolean isUnAuthorizedForSystemTemplates(Context ctx, XMLTemplateConfig bean) throws HomeException
    {
    	if(bean.getSystemProvided())
    	{
    		LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
    		if (lMgr == null)
    		{
    			throw new HomeException("LicenseManager not found in Context");
    		}
    		if(lMgr.isLicensed(ctx, CoreCrmLicenseConstants.GRR_DEVELOPER_LICENSE))
    		{
    			return false;
    		}
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
}
