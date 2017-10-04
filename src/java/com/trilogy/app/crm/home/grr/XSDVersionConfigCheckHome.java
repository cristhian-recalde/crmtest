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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.app.crm.grr.XSDVersionConfig;
import com.trilogy.app.crm.grr.XMLTemplateConfigHome;
import com.trilogy.app.crm.grr.XMLTemplateConfigXInfo;

/**
 * @author sajid.memon@redknee.com
 *  
 */
public class XSDVersionConfigCheckHome extends HomeProxy
{
    public XSDVersionConfigCheckHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    public void remove(Context ctx, Object obj) throws HomeException
    {      
        if (isUsedByXMLTemplateConfig(ctx,((XSDVersionConfig)obj).getVersion()))
        {
            throw new HomeException("Cannot Delete : XSD Version is being used by XML Template Configuration.");
        }
        super.remove(ctx, obj);
    }
    
    private boolean isUsedByXMLTemplateConfig(Context ctx, String version)
    {
        Home home = (Home)ctx.get(XMLTemplateConfigHome.class);
        Object xmlTemplateConfig = null;
        try
        {
        	xmlTemplateConfig = home.find(ctx, new EQ(XMLTemplateConfigXInfo.XSDVERSION, version));
        }
        catch (Exception e)
        {
            
        }
        return (xmlTemplateConfig != null);
    }
}
