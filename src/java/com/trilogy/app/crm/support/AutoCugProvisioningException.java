package com.trilogy.app.crm.support;

import java.util.Collection;

import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.framework.xhome.home.HomeException;


public class AutoCugProvisioningException extends HomeException
{
    public AutoCugProvisioningException(String message,
            ClosedUserGroup returnCug, 
            Collection msisdnFailureList,
            Throwable t)
    {
        super(message, t);
        returnCug_ = returnCug;
        msisdnFailureList_ = msisdnFailureList;
    }
    
    public ClosedUserGroup getReturnCug()
    {
        return returnCug_;
    }
    
    public void setReturnCug(ClosedUserGroup returnCug)
    {
        this.returnCug_ = returnCug;
    }
    
    public Collection getMsisdnFailureList()
    {
        return msisdnFailureList_;
    }
    
    public void setMsisdnFailureList(Collection msisdnFailureList)
    {
        this.msisdnFailureList_ = msisdnFailureList;
    }
    
    private ClosedUserGroup returnCug_ = null;
    private Collection msisdnFailureList_ = null;
    
   
}
