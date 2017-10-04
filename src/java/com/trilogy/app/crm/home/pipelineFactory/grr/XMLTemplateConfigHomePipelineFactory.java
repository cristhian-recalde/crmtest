/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright  Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home.pipelineFactory.grr;

import com.trilogy.app.crm.bean.AuxiliaryServiceAdapter;
import com.trilogy.app.crm.grr.XMLTemplateConfig;
import com.trilogy.app.crm.home.grr.VendorAwareXMLTemplateConfigHome;
import com.trilogy.app.crm.home.grr.XMLTemplateConfigSettingHome;
import com.trilogy.app.crm.home.grr.XMLTemplateConfigCheckHome;
import com.trilogy.app.crm.home.grr.XMLTemplateConfigAdapter;
import com.trilogy.app.crm.home.grr.XMLTemplateConfigRemovingHome;
import java.io.IOException;
import java.rmi.RemoteException;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;


/**
 * 
 * @author sajid.memon@redknee.com
 */
public class XMLTemplateConfigHomePipelineFactory implements PipelineFactory
{
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.home.PipelineFactory#createPipeline(com.redknee.framework.xhome.context.Context, com.redknee.framework.xhome.context.Context)
     */
    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException
            
    {
        Home home = CoreSupport.bindHome(ctx, XMLTemplateConfig.class);

        home = new XMLTemplateConfigRemovingHome(ctx,home);
        
        home = new AdapterHome(home, new XMLTemplateConfigAdapter()); 
        
        home = new XMLTemplateConfigSettingHome(ctx,home);
        
        home = new XMLTemplateConfigCheckHome(ctx,home);
        
        home = new SortingHome(ctx,home);
        
        home = new VendorAwareXMLTemplateConfigHome(ctx,home);
        
        return home;
    }
}
