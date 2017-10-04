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

import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.poller.agent.PollerInstall;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeChangeEvent;
import com.trilogy.framework.xhome.home.HomeChangeListener;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.home.NotifyingHomeItem;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class ErPollerConfigHomeChangeListener extends ContextAwareSupport implements HomeChangeListener
{    
    public ErPollerConfigHomeChangeListener(Context ctx)
    {
        super();
        setContext(ctx);
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.home.HomeChangeListener#homeChange(com.redknee.framework.xhome.home.HomeChangeEvent)
     */
    public void homeChange(HomeChangeEvent homechangeevent)
    {
        if( homechangeevent.getSource() instanceof ErPollerConfig
                || (homechangeevent.getSource() instanceof NotifyingHomeItem
                        && ((NotifyingHomeItem)homechangeevent.getSource()).getNewObject() instanceof ErPollerConfig) )
        {
            Context ctx = getContext().createSubContext();
            
            ErPollerConfig config = null;
            if( homechangeevent.getSource() instanceof ErPollerConfig )
            {
                config = (ErPollerConfig) homechangeevent.getSource();
            }
            else
            {
                config = (ErPollerConfig)((NotifyingHomeItem)homechangeevent.getSource()).getNewObject();
            }
            
            HomeOperationEnum operation = homechangeevent.getOperation();
            if( HomeOperationEnum.CREATE.equals(operation) )
            {                
                // Logic taken from FW's LifecycleWebAction
                new InfoLogMsg(this,"New poller configuration created.  Attempting to install Poller Id " + config.getId(),null).log(ctx);
                try
                {
                    ctx.put(ErPollerConfig.class, config);
                    new PollerInstall().execute(ctx);
                    new InfoLogMsg(this,"Poller Id " + config.getId() + " installed successfully.",null).log(ctx);
                }
                catch (AgentException e)
                {
                    new MajorLogMsg(this, "Error occurred installing ER Poller: " + config, null).log(ctx);
                    new DebugLogMsg(this, "Exception occurred installing ER Poller: " + config, e).log(ctx);
                }
            }
        }
    }

}
