package com.trilogy.app.crm.provision.corba.api.ecareservices;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.client.RemoteServiceStatus;
import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.corba.CorbaClientPropertyHome;
import com.trilogy.util.corba.ConnectionListener;
import com.trilogy.util.corba.ConnectionUpException;
import com.trilogy.util.corba.CorbaClientException;
import com.trilogy.util.corba.CorbaClientProxy;

public abstract class AbstractCorbaClient extends ContextAwareSupport implements RemoteServiceStatus, ConnectionListener
{
    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.urcs.RemoteServiceStatus#getConnectionStatus()
     */
    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(property_, isAlive());
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.urcs.RemoteServiceStatus#getServiceStatus()
     */
    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }

    /**
     * Constructor. Configures and initializes a connection to the server. The
     * CorbaClientProperty is to be found in the context under
     * CorbaClientProperty.class
     * 
     * @param ctx
     */
    public AbstractCorbaClient(Context ctx)
    {
        setContext(ctx);
        init();
    }

    /**
     * Constructor. Configures and initialize a connection to the server. The
     * CorbaClientProperty will be searched in the property home under the key
     * that is passed here.
     * 
     * @param ctx
     * @param key
     */
    public AbstractCorbaClient(Context ctx, String key)
    {
        setContext(ctx);
        init(key);
    }

    protected void init()
    {
        property_ = (CorbaClientProperty) getContext().get(CorbaClientProperty.class);
        initWithProperty();
    }

    protected void init(String key) throws IllegalArgumentException
    {
        Home corbaClientPropertyHome = null;

        corbaClientPropertyHome = (Home) getContext().get(CorbaClientPropertyHome.class);

        if (corbaClientPropertyHome == null)
        {
            throw new IllegalArgumentException("Corba client configuration does not exist");
        }

        try
        {
            property_ = (CorbaClientProperty) corbaClientPropertyHome.find(getContext(), key);
        } catch (HomeException e)
        {
            new MinorLogMsg(this, e.getMessage(), e).log(getContext());
            throw new IllegalArgumentException("Unable to load corba proxy for " + key
                    + ". Corba property bean is null.");
        }

        initWithProperty();
    }

    protected void initWithProperty()
    {
        if (property_ == null)
        {
            throw new IllegalArgumentException("Can't find ClientProperty for AccountServicesClient");
        }

        try
        {
            new InfoLogMsg(this, property_.toString(), null).log(getContext());

            service_ = null;

            corbaProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(), property_);
        } catch (Exception e)
        {
            new MajorLogMsg(this, "Not able to create CorbaClient for Language Support", e).log(getContext());
        }
    }

    
    /**
     * Returns the connected LanguageSupportService
     * 
     * @return connected LanguageSupportService, null if error connecting
     */
    public org.omg.CORBA.Object getService()
    {
        org.omg.CORBA.Object objServant = null;

        if (service_ != null)
        {
            return service_;
        }

        if (corbaProxy_ == null)
        {
            try
            {
                corbaProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(), property_);
            } catch (CorbaClientException ccEx)
            {
                new InfoLogMsg(this, ccEx.getMessage(), ccEx).log(getContext());
                invalidate(ccEx);
                return null;
            }
        }

        objServant = corbaProxy_.instance();
        
        return objServant;
    }

    public synchronized void invalidate(Throwable t)
    {
        // only raise a SNMP trap if this is the first time we discover the
        // connection is gone
        if (service_ != null)
        {
            // TODO: snmp external link down trap throw new
        }
        corbaProxy_.invalidate();
        service_ = null;
    }

    /**
     * Checks if the client is connected to the server.
     * 
     * @return true if connection is ok (service is valid)
     */
    public boolean isConnected()
    {
        return getService() != null;
    }

    public String getName()
    {
        if( property_ != null )
        {
            property_.getKey();
        }
        return getClass().getSimpleName();
    }

    public boolean isAlive()
    {
        return bConnected_;
    }

    public void connectionUp() throws ConnectionUpException
    {
        bConnected_ = true;

    }

    public void connectionDown()
    {
        bConnected_ = false;

    }
    
    protected org.omg.CORBA.Object service_;

    private CorbaClientProxy corbaProxy_;

    private CorbaClientProperty property_;

    private boolean bConnected_ = false;
}
