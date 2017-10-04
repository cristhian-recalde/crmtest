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

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.DiscountClassTemplateInfo;
import com.trilogy.app.crm.bean.DiscountClassTemplateInfoHome;
import com.trilogy.app.crm.home.account.PopulateAdjustmentTypeHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

/**
 * 
 * Pipelinefactory for discount class
 * 
 * @author ankit.nagpal@redknee.com
 * since 9_7_2
 */
public class DiscountClassTemplateInfoPipelineFactory extends CoreDiscountClassTemplateInfoPipelineFactory
{

    /* (non-Javadoc)
     * @see com.redknee.app.crm.home.PipelineFactory#createPipeline(com.redknee.framework.xhome.context.Context, com.redknee.framework.xhome.context.Context)
     */

    public Home createPipeline(Context ctx, Context serverCtx)
            throws RemoteException, HomeException, IOException, AgentException {
        
    	Home home = (Home) ctx.get(DiscountClassTemplateInfoHome.class);
    	
    	home = new SpidAwareHome(ctx, home);
    	
    	home = new PopulateAdjustmentTypeHome(ctx, home);
    	
    	home = new AdapterHome(home, new com.redknee.app.crm.home.DiscountClassTemplateInfoAdapter());
    	
        home = new SortingHome(home);
        
        return home;
    }

}
