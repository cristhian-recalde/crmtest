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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.IPCGDataHome;
import com.trilogy.app.crm.bean.IPCGDataXDBHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;


/**
 * This class creates IPCGDataHome for managing IPCG data before creating
 * call detail entries.  This class is mainly created for the IPCG pollers.
 *
 * @author jimmy.ng@redknee.com
 */
public class IPCGDataHomeFactory
    implements ContextFactory
{
    /**
     * Creates a new IPCGDataHomeFactory.
     */
    public IPCGDataHomeFactory()
    {
    }


    /**
     * INHERIT
     **/
    public Object create(final Context ctx)
    {
        if(ctx.has(IPCGDataHome.class))
        {
            return ctx.get(IPCGDataHome.class);
        }
        else
        {
            Home home = new IPCGDataXDBHome(ctx);
            ctx.put(IPCGDataXDBHome.class, home);  // To be used in IPCGBufferFlushingHome
            
            home = new IPCGBufferFlushingHome(home, ctx);

            ctx.put(IPCGDataHome.class, home);
            return home;
        }
    }
}
