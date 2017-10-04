package com.trilogy.app.crm.voicemail;

import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;

public interface VoiceMailManageInterface {

     
    public void connect() throws AgentException; 
    public void disconnect() throws AgentException; 
    public void reconnect() throws AgentException; 
    public void acquireNewConnection() throws AgentException;
    public String getRemoteInfo(); 
    public boolean isAlive(); 

}
