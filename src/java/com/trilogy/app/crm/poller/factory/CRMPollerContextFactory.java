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
package com.trilogy.app.crm.poller.factory;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.poller.FilePoller;


/**
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class CRMPollerContextFactory implements ContextFactory
{
    private static ContextFactory instance_ = null;
    public static ContextFactory instance()
    {
        if( instance_ == null )
        {
            instance_ = new CRMPollerContextFactory();
        }
        return instance_;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.context.ContextFactory#create(com.redknee.framework.xhome.context.Context)
     */
    public Object create(Context ctx)
    {
        ErPollerConfig config = (ErPollerConfig) ctx.get(ErPollerConfig.class);
        if(config==null)
        {
            new MajorLogMsg(this,"Initializing poller failed. " + getClass().getSimpleName() + " requires instance of ErPollerConfig in context.",null).log(ctx);
            return null;
        }

        try
        {
            return new FilePoller(ctx, config);
        }
        catch(Exception e)
        {
            new MajorLogMsg(this,"Initializing  poller (id=" + config.getId() + ") failed.", e).log(ctx);
        }

        return null;
//        return new FilePoller(ctx, config);
/* 
        try
        {
            // Create/Recreate a CallDetail home in the context of this poller.
            //ErPollerSupport.createCallDetailHomeWithCommitTimer(getContext());
            
            switch( config.getType().getIndex() )
            {
            case PollerTypeEnum.SMSB_INDEX:
                return new SMSBPoller(ctx, pollerConfig, queueSize, threads);
            
            case PollerTypeEnum.ABM_INDEX:
                return new ABMPoller(ctx, pollerConfig, queueSize, threads);
            
            case PollerTypeEnum.URS_INDEX:
                return new URSPoller(ctx, pollerConfig, queueSize, threads);
            
            case PollerTypeEnum.URS_ROAMING_INDEX:
                return new URSRoamingPoller(ctx, pollerConfig, queueSize, threads);
            
            case PollerTypeEnum.IPCGW_INDEX:
                return new IPCGWPoller(ctx, pollerConfig, queueSize, threads);
            
            case PollerTypeEnum.VRA_INDEX:
                return new VRAPoller(ctx, pollerConfig, queueSize, threads);
            
            case PollerTypeEnum.LOYALTY_INDEX:
                return new LoyaltyPoller(ctx, pollerConfig, queueSize, threads);
            
            case PollerTypeEnum.BM_INDEX:
                return new BMPoller(ctx, pollerConfig, queueSize, threads);

            case PollerTypeEnum.MCOMMERCE_INDEX:
                return new MCommercePoller(ctx, pollerConfig, queueSize, threads);
            
            case PollerTypeEnum.OCG_INDEX:
                return new OCGChargingPoller(ctx, pollerConfig, queueSize, threads);
            
            case PollerTypeEnum.SELFCARECUG_INDEX:
                return new FnFSelfCarePoller(ctx, pollerConfig, queueSize, threads);
                
            case PollerTypeEnum.TFA_INDEX:
                return new TFAPoller(ctx, pollerConfig, queueSize, threads);
            
            case PollerTypeEnum.NOTES_INDEX:
                Home notehome = (Home) ctx.get(NotesPollerConfigHome.class);
                NotesPollerConfig notesPollerConfig = (NotesPollerConfig)notehome.find(ctx, config.getId());
                if( notesPollerConfig != null )
                {
                    Context notesCtx = ctx.createSubContext();
                    notesCtx.setName("NotesPollerContext");
                    notesCtx.put(NotesPollerConfig.class, notesPollerConfig);
                    return new NotesGWPoller(notesCtx, pollerConfig, queueSize, threads);
                }
                new InfoLogMsg(this,"No notes poller configuration was found for " + config.getType().getDescription() + " poller (id=" + config.getId() + ").", null).log(ctx);
                return null;
                
            default:
                new MinorLogMsg(this, "Unknown poller type '" + config.getType() + "' encountered during poller install.  Can't create poller for id=" + config.getId(), null).log(ctx);
            }
        }
        catch (Exception e)
        {
            new MajorLogMsg(this,"Initializing " + config.getType().getDescription() + " poller (id=" + config.getId() + ") failed.", e).log(ctx);
        }
        
        return null;
        */
    }

}
