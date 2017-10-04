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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.bean.PersonalListPlanHome;
import com.trilogy.app.crm.home.PersonalListPlanServiceHome;
import com.trilogy.app.crm.home.PersonalListPlanValidator;
import com.trilogy.app.crm.home.PersonalPlanListAuxiliaryServiceCreationHome;
import com.trilogy.app.crm.home.PipelineFactory;

/**
 * Creates Personal List Plan Home pipeline.
 * @author victor.stratan@redknee.com
 */
public class PersonalListPlanHomeFactory implements PipelineFactory
{
    public Home createPipeline(final Context ctx, final Context serverCtx) throws HomeException, IOException
    {
        Home home = new PersonalListPlanServiceHome(ctx);
        home = new PersonalPlanListAuxiliaryServiceCreationHome(ctx, home);
        home = new SpidAwareHome(ctx, home);
        home = new NoSelectAllHome(home);
        home = new ValidatingHome(home, new PersonalListPlanValidator(ctx));

        ctx.put(PersonalListPlanHome.class, home);

        return home;
    }

}
