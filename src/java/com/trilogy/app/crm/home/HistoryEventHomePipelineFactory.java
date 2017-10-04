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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.numbermgn.HistoryEvent;
import com.trilogy.app.crm.numbermgn.HistoryEventHome;
import com.trilogy.app.crm.numbermgn.ImsiMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.NumberMgmtHistoryXInfo;
import com.trilogy.app.crm.numbermgn.PackageMgmtHistoryHome;
import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoChildrenRemoveHome;
import com.trilogy.framework.xhome.home.SortingHome;


/**
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class HistoryEventHomePipelineFactory implements PipelineFactory
{
    private static PipelineFactory instance_ = null;
    
    public static PipelineFactory instance()
    {
        if( instance_ == null )
        {
            instance_ = new HistoryEventHomePipelineFactory();
        }
        return instance_;
    }
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.home.PipelineFactory#createPipeline(com.redknee.framework.xhome.context.Context, com.redknee.framework.xhome.context.Context)
     */
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        Home historyEventHome = 
            new PMHome(
                    new RMIClusteredHome(
                            ctx,
                            HistoryEventHome.class.getName(),
                            CoreSupport.bindHome(ctx, HistoryEvent.class)),
                            ctx,
                            HistoryEvent.class.getName());
        
        historyEventHome = 
            new NoChildrenRemoveHome(
                    ctx,
                    ImsiMgmtHistoryHome.class,
                    "Cannot delete History Event currently in use by IMSI History.",
                    historyEventHome)
        {
            @Override
            public Predicate getRelationshipPredicate(final Object id)
            {
                return new EQ(NumberMgmtHistoryXInfo.EVENT, id);
            }
        };

        historyEventHome = 
            new NoChildrenRemoveHome(
                    ctx,
                    MsisdnMgmtHistoryHome.class,
                    "Cannot delete History Event currently in use by Msisdn History.",
                    historyEventHome)
        {
            @Override
            public Predicate getRelationshipPredicate(final Object id)
            {
                return new EQ(NumberMgmtHistoryXInfo.EVENT, id);
            }
        };

        historyEventHome = 
            new NoChildrenRemoveHome(
                    ctx,
                    PackageMgmtHistoryHome.class,
                    "Cannot delete History Event currently in use by Package History.",
                    historyEventHome)
        {
            private static final long serialVersionUID = 491046623060916118L;

            @Override
            public Predicate getRelationshipPredicate(final Object id)
            {
                return new EQ(NumberMgmtHistoryXInfo.EVENT, id);
            }
        };
        
        return new SortingHome(historyEventHome);
    }

}
