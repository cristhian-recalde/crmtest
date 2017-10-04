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
package com.trilogy.app.crm.poller.home;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeChangeEvent;
import com.trilogy.framework.xhome.home.HomeChangeListener;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.home.NotifyingHomeItem;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.bean.ErPollerConfigHome;
import com.trilogy.app.crm.config.NotesPollerConfig;
import com.trilogy.app.crm.poller.agent.PollerInstall;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 7.7
 */
public class NotesPollerConfigHomeChangeListener extends ContextAwareSupport implements HomeChangeListener
{    
    public NotesPollerConfigHomeChangeListener(Context ctx)
    {
        super();
        setContext(ctx);
    }

    /**
     * @{inheritDoc}
     */
    public void homeChange(HomeChangeEvent homechangeevent)
    {
        if( homechangeevent.getSource() instanceof NotesPollerConfig
                || (homechangeevent.getSource() instanceof NotifyingHomeItem
                        && ((NotifyingHomeItem)homechangeevent.getSource()).getNewObject() instanceof NotesPollerConfig) )
        {
            Context ctx = getContext().createSubContext();
            
            NotesPollerConfig config = null;
            if( homechangeevent.getSource() instanceof NotesPollerConfig )
            {
                config = (NotesPollerConfig) homechangeevent.getSource();
            }
            else
            {
                config = (NotesPollerConfig)((NotifyingHomeItem)homechangeevent.getSource()).getNewObject();
            }
            
            HomeOperationEnum operation = homechangeevent.getOperation();
            if( HomeOperationEnum.CREATE.equals(operation) )
            {                
                // Logic taken from FW's LifecycleWebAction
                new InfoLogMsg(this,"New notes poller configuration created.  Attempting to install Poller Id " + config.getIdentifier(),null).log(ctx);
                try
                {
                    ErPollerConfig erConfig = null;
                    Home erConfigHome = (Home)ctx.get(ErPollerConfigHome.class);
                    try
                    {
                        erConfig = (ErPollerConfig)erConfigHome.find(ctx, config.getIdentifier());
                    }
                    catch (HomeException e)
                    {
                        new MajorLogMsg(this, "Error occurred looking up ER Poller Configuration for poller ID: " + config.getIdentifier(), null).log(ctx);
                        throw new AgentException(e);
                    }
                    if( erConfig != null )
                    {
                        ctx.put(ErPollerConfig.class, erConfig);
                        new PollerInstall().execute(ctx);
                        new InfoLogMsg(this,"Poller Id " + config.getIdentifier() + " installed successfully.",null).log(ctx);
                    }
                    else
                    {
                        new MajorLogMsg(this, "ER Poller Configuration not found for poller ID: " + config.getIdentifier(), null).log(ctx);
                    }
                }
                catch (AgentException e)
                {
                    new MajorLogMsg(this, "Error occurred installing poller ID " + config.getIdentifier() + ": " + e.getMessage(), null).log(ctx);
                    new DebugLogMsg(this, "Exception occurred installing ER Poller: " + config, e).log(ctx);
                }
            }
        }
    }

}
