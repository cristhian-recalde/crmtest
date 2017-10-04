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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.bean.BlacklistWhitelistTemplateHome;
import com.trilogy.app.crm.home.BlackListWhitelistServiceHome;
import com.trilogy.app.crm.home.BlacklistWhitelistServiceCreationHome;
import com.trilogy.app.crm.home.BlacklistWhitelistTemplateCreateValidator;
import com.trilogy.app.crm.home.BlacklistWhitelistTemplateStoreValidator;
import com.trilogy.app.crm.home.PipelineFactory;

/**
 * @author chandrachud.ingale
 * @since  9.6
 */

public class BlacklistWhitelistTemplateHomePipelineFactory implements PipelineFactory
{

    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {

        Home home = new BlackListWhitelistServiceHome(ctx);
        home = new BlacklistWhitelistServiceCreationHome(ctx, home);
        home = new SpidAwareHome(ctx, home);
        home = new NoSelectAllHome(home);
        home = new ValidatingHome(BlacklistWhitelistTemplateCreateValidator.getInstance(), BlacklistWhitelistTemplateStoreValidator.getInstance(), home);

        ctx.put(BlacklistWhitelistTemplateHome.class, home);

        return home;
    }
}
