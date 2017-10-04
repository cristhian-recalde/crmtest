package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.config.NotesPollerConfig;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.poller.home.NotesPollerConfigHomeChangeListener;


public class NotesPollerConfigPipelineFactory implements PipelineFactory
{


    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException, AgentException
    {

        LogSupport.info(ctx, this, "Installing the Notes Config home ");
        
        Home noteshome = CoreSupport.bindHome(ctx, NotesPollerConfig.class);
        
        noteshome = new AuditJournalHome(ctx, noteshome);
        
        noteshome = new NotifyingHome(noteshome);
        ((NotifyingHome)noteshome).addHomeChangeListener(new NotesPollerConfigHomeChangeListener(ctx));
        
        noteshome = new RMIClusteredHome(ctx, NotesPollerConfig.class.getName(), noteshome);

        return noteshome;
    }

}
