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
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.calldetail.CallDetailXDBHome;
import com.trilogy.app.crm.config.CallDetailConfig;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.util.CallDetailDateComparator;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Provides a ContextFactory for creating PrepaidCallingCardCallDetailHome pipeline on demand.
 *
 * @author kumaran.sivasubramaniam@redknee.com
 * @since June 3, 2009
 */
public class PrepaidCallingCardCallDetailHomePipelineFactory implements PipelineFactory
{

    /**
     * Create a new instance of <code>PrepaidCallingCardCallDetailHomePipelineFactory</code>.
     */
    protected PrepaidCallingCardCallDetailHomePipelineFactory()
    {
        // do nothing
    }


    /**
     * Returns an instance of <code>PrepaidCallingCardCallDetailHomePipelineFactory</code>.
     *
     * @return An instance of <code>PrepaidCallingCardCallDetailHomePipelineFactory</code>.
     */
    public static PrepaidCallingCardCallDetailHomePipelineFactory instance()
    {
        if (instance == null)
        {
            instance = new PrepaidCallingCardCallDetailHomePipelineFactory();
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
        Home home = null;

        if (LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.PREPAID_CALLING_CARD_LICENSE_KEY))
        {  
            new InfoLogMsg(this, " Install Prepaid calling card home",null).log(context);

            CallDetailConfig config = (CallDetailConfig) context.get(CallDetailConfig.class);
            home = new CallDetailXDBHome(context, PREPAID_CALLING_CARD_CALL_DETAIL_TABLE_NAME);

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
                    LogSupport.debug(context, this, "Creating CommitRatioHome since the ratio specified is "
                            + config.getCallDetailCommitRatio());
                }
                home = new CommitRatioHome(context, config.getCallDetailCommitRatio(), home);
            }
            home = new CallDetailIdGeneratorHome(context, home, PREPAID_CALLING_CARD_CALL_DETAIL_SEQUENCE_NAME);
            home = new CallDetailValidatingHome(home);
            home = new DroppedCallDetailHome(context, home);
            home = new CallDetailCategorizationHome(home);
            home = new CallDetailTaxAuthoritySettingHome(context, home);
            home = new CallDetailZoneHome(context, home);
            home = new SortingHome(context, home, new CallDetailDateComparator(true));
            home = new NoSelectAllHome(home);

        }

        context.put(Common.PREPAID_CALLING_CARD_CALL_DETAIL_HOME,home);
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
    private static PrepaidCallingCardCallDetailHomePipelineFactory instance;
    public static final String PREPAID_CALLING_CARD_CALL_DETAIL_TABLE_NAME ="PccCallDetail";
    public static final String PREPAID_CALLING_CARD_CALL_DETAIL_SEQUENCE_NAME = "Prepaid_CC_seq";

}
