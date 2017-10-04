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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;

import com.trilogy.app.crm.bean.ClosedUserGroupHome;
import com.trilogy.app.crm.home.ClosedUserGroupERLogHome;
import com.trilogy.app.crm.home.ClosedUserGroupServiceHome;
import com.trilogy.app.crm.home.ClosedUserGroupSpidSettingHome;
import com.trilogy.app.crm.home.ClosedUserGroupSubAuxiliaryServiceHome;
import com.trilogy.app.crm.home.ClosedUserGroupValidator;
import com.trilogy.app.crm.home.OldClosedUserGroupInitializationHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.cug.PrivateCUGHome;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

/**
 * Creates Closed User Group Home pipeline.
 * @author victor.stratan@redknee.com
 */
public class ClosedUserGroupHomeFactory implements PipelineFactory
{
    public Home createPipeline(final Context ctx, final Context serverCtx) throws HomeException, IOException
    {
        Home home = new ClosedUserGroupServiceHome(ctx);
        home = new ClosedUserGroupSubAuxiliaryServiceHome(ctx, home);        
        /* ClosedUserGroupERLogHome has to go after ClosedUserGroupAuxiliaryServiceCreationHome &
           ClosedUserGroupServiceHome to log success of create */
        home = new ClosedUserGroupERLogHome(ctx, home);        
        home = new SpidAwareHome(ctx, home);
        home = new NoSelectAllHome(home);
        home = new PrivateCUGHome(home);
        home = new ValidatingHome(home, new ClosedUserGroupValidator());
        home = new OldClosedUserGroupInitializationHome(ctx, home);
        ctx.put(ClosedUserGroupHome.class, home);
        return home;
    }
    
    
    public Home createMovePipeline(final Context ctx, final Context serverCtx) throws HomeException, IOException
    {
        Home home = new ClosedUserGroupServiceHome(ctx);        
        home = new ClosedUserGroupERLogHome(ctx, home);
        home = new OldClosedUserGroupInitializationHome(ctx, home);
        home = new SpidAwareHome(ctx, home);
        home = new NoSelectAllHome(home);
        home = new PrivateCUGHome(home);
        ctx.put(MoveConstants.CUSTOM_CUG_HOME_CTX_KEY, home);
        return home;
    }

}
