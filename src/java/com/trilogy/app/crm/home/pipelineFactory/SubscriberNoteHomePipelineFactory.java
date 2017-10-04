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
 * Copyright  Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.NoteOwnerTypeEnum;
import com.trilogy.app.crm.bean.SubscriberNote;
import com.trilogy.app.crm.bean.SubscriberNoteHome;
import com.trilogy.app.crm.home.NotesAuxiliaryFieldSetHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NullHome;


/**
 * 
 * @author Kumaran
 */
public class SubscriberNoteHomePipelineFactory implements PipelineFactory
{
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.home.PipelineFactory#createPipeline(com.redknee.framework.xhome.context.Context, com.redknee.framework.xhome.context.Context)
     */
    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        Home home =
            ConfigChangeRequestSupportHelper.get(ctx)
                .registerHomeForConfigSharing(ctx, new NullHome(), SubscriberNote.class);
        
        home = new NotesAuxiliaryFieldSetHome(ctx, home, NoteOwnerTypeEnum.SUBSCRIPTION);

        ctx.put(SubscriberNoteHome.class, home);
        return home;
    }
}
