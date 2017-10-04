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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.calldetail.CallDetailXDBHome;
import com.trilogy.app.crm.config.CallDetailConfig;
import com.trilogy.app.crm.home.CDRSetSubTypeHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.SubscriberIdentifierSettingHome;
import com.trilogy.app.crm.util.CallDetailDateComparator;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;


/**
 * Provides a ContextFactory for creating CallDetailHome pipeline on demand.
 *
 * @author cindy.wong@redknee.com
 * @since November 13, 2007
 */
public class CallDetailHomePipelineFactory implements PipelineFactory
{

    /**
     * Create a new instance of <code>CallDetailHomePipelineFactory</code>.
     */
    protected CallDetailHomePipelineFactory()
    {
        // do nothing
    }


    /**
     * Returns an instance of <code>CallDetailHomePipelineFactory</code>.
     *
     * @return An instance of <code>CallDetailHomePipelineFactory</code>.
     */
    public static CallDetailHomePipelineFactory instance()
    {
        if (instance == null)
        {
            instance = new CallDetailHomePipelineFactory();
        }
        return instance;
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
    public Home decorateHome(final Context context, final Context serverContext)
    {
        CallDetailConfig config = (CallDetailConfig) context.get(CallDetailConfig.class);
        String tableName = config.getTableName();
        
        if ( tableName == null || tableName.isEmpty())
        {
            tableName = "CallDetail";
        }
        
        Home home = new CallDetailXDBHome(context,tableName);
        context.put(CallDetailXDBHome.class, home); 

        // Install a home to adapt between business logic bean and data bean
        home = new AdapterHome(
                context, 
                home, 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.calldetail.CallDetail, com.redknee.app.crm.bean.core.custom.CallDetail>(
                        com.redknee.app.crm.bean.calldetail.CallDetail.class, 
                        com.redknee.app.crm.bean.core.custom.CallDetail.class));
        
        if (config.getCallDetailCommitRatio() > 1)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, this, "Creating CommitRatioHome since the ratio specified is " + config.getCallDetailCommitRatio());
            }
            home=new CommitRatioHome(context, config.getCallDetailCommitRatio(), home);
        }
        home = new CallDetailPrimaryKeyFindHome(home);
        home = new CalldetailSAPFieldsSettingHome(home); 
        home = new CallDetailChargeOcgHome(context, home);     
        home = new CallDetailIdGeneratorHome(context, home, "CallDetailID_seq");
        home = new CallDetailValidatingHome(home);
        home = new SubscriberIdentifierSettingHome(home);
        home = new CallDetailSupportedSubscriberIdentifierSettingHome(context, home);
        home = new DroppedCallDetailHome(context, home);
        home = new CDRSetSubTypeHome(home);
        home = new CallDetailCategorizationHome(home);
        home = new CallDetailTaxAuthoritySettingHome(context, home);
        home = new CallDetailZoneHome(context, home);
        home = new CallDetailSubAndBANLookupHome(home);
        home = new SortingHome(context, home, new CallDetailDateComparator(true));
        home = new NoSelectAllHome(home);
        

        return home;
    }

    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        return decorateHome(ctx,serverCtx);
    }
    
    /**
     * Singleton instance.
     */
    private static CallDetailHomePipelineFactory instance;


}
