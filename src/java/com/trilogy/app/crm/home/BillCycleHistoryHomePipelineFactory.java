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

import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.bean.BillCycleHistoryHome;
import com.trilogy.app.crm.home.core.CoreBillCycleHistoryHomePipelineFactory;
import com.trilogy.app.crm.home.validator.BillCycleHistoryAccountStateValidator;
import com.trilogy.app.crm.home.validator.BillCycleHistoryEventDateValidator;
import com.trilogy.app.crm.xhome.home.BillCycleHistoryDateSettingHome;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class BillCycleHistoryHomePipelineFactory extends CoreBillCycleHistoryHomePipelineFactory
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        // Should have already been installed by core installation
        Home home = (Home) ctx.get(BillCycleHistoryHome.class);
        if (home == null)
        {
            home = super.createPipeline(ctx, serverCtx);
        }

        if (home instanceof HomeProxy)
        {
            // Homes that require the bill cycle days be populated must go after the date setting home (installed by core)
            Home dateSettingHome = ((HomeProxy) home).findDecorator(BillCycleHistoryDateSettingHome.class);
            if (!(dateSettingHome instanceof HomeProxy))
            {
                dateSettingHome = new BillCycleHistoryDateSettingHome(ctx, home);
                home = dateSettingHome;
            }

            ((HomeProxy) dateSettingHome).addProxy(ctx, new AccountNoteBillCycleHistoryHome(ctx, NullHome.instance()));
            
            ((HomeProxy) dateSettingHome).addProxy(ctx, new UrcsUpdatingBillCycleHistoryHome(ctx, NullHome.instance()));
            
            ((HomeProxy) dateSettingHome).addProxy(ctx, new BillCycleHistoryCleanupHome(ctx, NullHome.instance()));
            
            ((HomeProxy) dateSettingHome).addProxy(ctx, new AutoAccountUpdatingBillCycleHistoryHome(ctx, NullHome.instance()));
            
            CompoundValidator validator = new CompoundValidator();
            validator.add(new BillCycleHistoryEventDateValidator());
            validator.add(new BillCycleHistoryAccountStateValidator());
            ((HomeProxy) dateSettingHome).addProxy(ctx, new ValidatingHome(NullHome.instance(), validator));
        }
        
        return home;
    }

}
