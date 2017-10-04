package com.trilogy.app.crm.client.blackberry;

import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.client.RemoteServiceStatus;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.service.blackberry.IServiceBlackberry;
import com.trilogy.service.blackberry.ServiceBlackberryException;
import com.trilogy.service.blackberry.ServiceBlackberryFactory;
import com.trilogy.service.blackberry.model.BBConfig;

public class ServiceBlackberryClient  extends ContextAwareSupport implements RemoteServiceStatus {

	public ServiceBlackberryClient(Context context, BBConfig bbConfig)
	{
        if (context == null)
        {
            throw new IllegalArgumentException("The context parameter is null.");
        }
        setContext(context);
        BBConfig_ = bbConfig;
	}
	
	public String getRemoteInfo() {
		// TODO Auto-generated method stub
		return BBConfig_.getUrl();
	}

	public String getDescription() {
		return "BlackBerry Provisioning Services";
	}

	public String getName() {
		return "ServiceBlackberry";
	}

    public synchronized ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(getRemoteInfo(), isAlive());
    }

    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }

    public boolean isAlive() {
    	IServiceBlackberry serviceBlackberry = ServiceBlackberryFactory.getServiceBlackberry(getContext(), BBConfig_.getSpid());
		if (serviceBlackberry == null)
		{
			return false;
		}
        try
        {
        	// Method doesn't return anything. If nothing happens, service is running. Otherwise, service is dead.
        	serviceBlackberry.checkConnection(getContext(), BBConfig_.getSpid());
        	return true;
        	
        } catch (ServiceBlackberryException e)
        {
        	return false;
        }
	}

    private BBConfig BBConfig_;
}
