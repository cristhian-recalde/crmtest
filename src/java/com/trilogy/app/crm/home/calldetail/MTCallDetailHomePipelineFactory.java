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

package com.trilogy.app.crm.home.calldetail;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.calldetail.CallDetailXDBHome;
import com.trilogy.app.crm.config.CallDetailConfig;
import com.trilogy.app.crm.home.CDRSetSubTypeHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.SubscriberIdentifierSettingHome;
import com.trilogy.app.crm.util.CallDetailDateComparator;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Provides a ContextFactory for creating CallDetailHome pipeline on demand.
 *
 * @author cindy.wong@redknee.com
 * @since November 13, 2007
 */
public class MTCallDetailHomePipelineFactory implements PipelineFactory
{

    /**
     * Singleton instance.
     */
    private static MTCallDetailHomePipelineFactory instance;
    
    /**
     * Create a new instance of <code>CallDetailHomePipelineFactory</code>.
     */
    protected MTCallDetailHomePipelineFactory()
    {
        // do nothing
    }


    /**
     * Returns an instance of <code>CallDetailHomePipelineFactory</code>.
     *
     * @return An instance of <code>CallDetailHomePipelineFactory</code>.
     */
    public static MTCallDetailHomePipelineFactory instance()
    {
        if (instance == null)
        {
            instance = new MTCallDetailHomePipelineFactory();
        }
        return instance;
    }

    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        return decorateHome(ctx,serverCtx);
    }


    /**
     * Decorates the home.
     *
     * @param originalHome
     *            Home being decorated.
     * @param context
     *            The operating context.
     * @param serverContext
     *            The server context.
     * @return Decorated home.
     */
    public Home decorateHome(final Context ctx, final Context serverContext)
    {
        CallDetailConfig config = (CallDetailConfig) ctx.get(CallDetailConfig.class);
        String tableName = MT_CALLDETAIL_TABLE_NAME;
        
        Home home = new CallDetailXDBHome(ctx,tableName);
        home = new AdapterHome(
                ctx, 
                home, 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.calldetail.CallDetail, com.redknee.app.crm.bean.core.custom.CallDetail>(
                        com.redknee.app.crm.bean.calldetail.CallDetail.class, 
                        com.redknee.app.crm.bean.core.custom.CallDetail.class));
        
        if (config.getCallDetailCommitRatio() > 1)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Creating CommitRatioHome since the ratio specified is " + config.getCallDetailCommitRatio());
            }
            home=new CommitRatioHome(ctx, config.getCallDetailCommitRatio(), home);
        }
        
        home = new CalldetailSAPFieldsSettingHome(home); 
        home = new CallDetailIdGeneratorHome(ctx,home,"MTCallDetailID_seq");
        home = new SubscriberIdentifierSettingHome(home);
        home = new CallDetailSupportedSubscriberIdentifierSettingHome(ctx, home);
        home = new CDRSetSubTypeHome(home);
        home = new CallDetailCategorizationHome(home);
        home = new CallDetailZoneHome(ctx,home);
        home = new CallDetailSubAndBANLookupHome(home);
        home = new SortingHome(ctx,home,new CallDetailDateComparator(true));
        home = new SpidAwareHome(ctx,home);
                
        home = new NoSelectAllHome(home);
        
        // direct connection to DB
        ctx.put(Common.MT_CALL_DETAIL_HOME,home);
        
        

        return home;
    }

    /**
     * Default rerate table name
     */
    public static final String MT_CALLDETAIL_TABLE_NAME = "MtCallDetail";
}

