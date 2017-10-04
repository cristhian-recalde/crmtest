package com.trilogy.app.crm.client;

import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.service.aptilo.IServiceAptilo;
import com.trilogy.service.aptilo.ServiceAptiloException;
import com.trilogy.service.aptilo.ServiceAptiloFactory;

public class ServiceAptiloClient  extends ContextAwareSupport implements RemoteServiceStatus {

	public ServiceAptiloClient(Context context)
	{
        if (context == null)
        {
            throw new IllegalArgumentException("The context parameter is null.");
        }
        setContext(context);
	}
	
	public String getRemoteInfo() {
		return ServiceAptiloFactory.getAptiloConfig(getContext()).getHost();
	  
	}

	public String getDescription() {
		return "Aptilo Provisioning Services";
	}

	public String getName() {
		return "ServiceAptilo";
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
		IServiceAptilo serviceAptilo = ServiceAptiloFactory.getServiceAptilo(getContext());
		if (serviceAptilo == null)
		{
			return false;
		}
        try
        {
        	// Method doesn't return anything. If nothing happens, service is running. Otherwise, service is dead.
        	serviceAptilo.getConnection(getContext()).checkConnection(getContext());
        	return true;
        	
        } catch (ServiceAptiloException e)
        {
        	return false;
        }
	}

}
