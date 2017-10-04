/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.voicemail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.trilogy.app.crm.bean.VoicemailServiceConfig;
import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.client.RemoteServiceStatus;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.support.VoicemailSupport;
import com.trilogy.app.crm.voicemail.client.MpathixClient;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * @author Prasanna.Kulkarni
 * @time Oct 12, 2005
 */
public class MpathixConnectionInfoPropertyListener implements PropertyChangeListener, RemoteServiceStatus, ContextAware
{

    private static final String SERVICE_NAME = "ServiceVoicemailClient(Mpathix)";
    private static final String SERVICE_DESCRIPTION = "Client for Voicemail services";

    public MpathixConnectionInfoPropertyListener(Context ctx)
    {
        setContext(ctx);
    }


    public void propertyChange(PropertyChangeEvent evt)
    {
        /*
         * No need to check for any particular property, if any property is changed then
         * we need to regain the connection as all the properties are connection related
         */
        VoicemailServiceConfig configBean = VoicemailSupport.getVMConfig(getContext());
        if(configBean.getReconnectVM())
            acquireNewConnection();
    }

    private void acquireNewConnection()
    {
        VoiceMailManageInterface client = (VoiceMailManageInterface) getContext().get(VoiceMailManageInterface.class); 
        if ( client instanceof MpathixClient)
        {
           try {
            client.acquireNewConnection();
           } catch ( AgentException e )
           {
               new MajorLogMsg(this, " fail to restart Voice mail connection after property changed", e).log(getContext()); 
           }
        }
    }

    public String getName()
    {
        return SERVICE_NAME;
    }


    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }


    public String getRemoteInfo()
    {
        VoiceMailManageInterface client = (VoiceMailManageInterface) getContext().get(VoiceMailManageInterface.class); 
        if ( client instanceof MpathixClient)
        {
              return client.getRemoteInfo();
        }
        return ""; 
    }


    public boolean isAlive()
    {
        
         VoiceMailManageInterface client = (VoiceMailManageInterface) getContext().get(VoiceMailManageInterface.class); 
        if ( client instanceof MpathixClient)
        {
              return client.isAlive();
        }
        return false; 
    }


    public Context getContext()
    {
        return ctx_;
    }


    public void setContext(Context arg0)
    {
        ctx_ = arg0;
    }

    private Context ctx_;

    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(this.getRemoteInfo(), isAlive());
    }


    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }
}
