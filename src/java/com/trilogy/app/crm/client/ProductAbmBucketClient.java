/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client;

import org.omg.CORBA.IntHolder;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.FCTRollOverEnabledEnum;
import com.trilogy.app.crm.bean.FCTUsagePrecedenceEnum;
import com.trilogy.app.crm.bean.FreeCallTime;
import com.trilogy.app.crm.config.ProductAbmClientConfig;
import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.product.abm.BucketProvision;
import com.trilogy.product.abm.BucketProvisionHelper;
import com.trilogy.product.abm.ServiceUsageBucketTemplate;
import com.trilogy.product.abm.ServiceUsageBucketTemplateHolder;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.corba.CorbaClientPropertyHome;
import com.trilogy.util.corba.CorbaClientException;
import com.trilogy.util.corba.CorbaClientProxy;



/**
 * A client for accessing the ABM Bucket Provisioning facilities using CORBA.
 *
 * @author gary.anderson@redknee.com
 */
public class ProductAbmBucketClient extends ContextAwareSupport implements ContextAware, RemoteServiceStatus
{
    private static final String SERVICE_DESCRIPTION = "CORBA client for ABM Bucket Template Provisioning";

    /**
     * Creates a new client based on the CORBA configuration found in the
     * context with the key "ProductAbmBucketClient".
     *
     * @param context The operating context.
     */
    public ProductAbmBucketClient(final Context context)
    {
        super();
        setContext(context);
        initialize();
    }


    /**
     * This constructor is for testing purposes only.  This constructor allows
     * for creation of mock UPS/ABM clients.
     */
    protected ProductAbmBucketClient()
    {
        // Empty
    }


    /**
     * Creates the given template in ABM.  This method treats the given template
     * object as immutable.  The identifier assigned to the template is
     * returned.
     *
     * @param template The template to create.
     * @return The new identifier of the template as assigned by ABM.
     *
     * @exception AbmBucketException Thrown if there is a failure to get the
     * template.
     */
    public long createTemplate(final FreeCallTime template)
        throws AbmBucketException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "createTemplate()");

        final long identifier;

        try
        {
            final BucketProvision service = getService();

            if (service != null)
            {
                try
                {
                    final IntHolder identifierHolder = new IntHolder();

                    final short result =
                        service.createServiceUsageBucketTemplate(
                            unadapt(template),
                            identifierHolder);

                    if (result != AbmResultCode.SUCCESS)
                    {
                        throw new AbmBucketException(
                            "Failed to create template.", result);
                    }

                    identifier = identifierHolder.value;
                }
                catch (final org.omg.CORBA.COMM_FAILURE commFail)
                {
                    invalidate(commFail);

                    throw new AbmBucketException(
                        "Failed to create template.", commFail);
                }
            }
            else
            {
                throw new AbmBucketException(
                    "Failed to create template.  No service available.");
            }
        }
        finally
        {
            pmLogMsg.log(getContext());
        }

        return identifier;
    }


    /**
     * Gets the FreeCallTime template.
     *
     * @param identifier The identifier of the template to get.
     *
     * @return The template.
     *
     * @exception AbmBucketException Thrown if there is a failure to get the
     * template.
     */
    public FreeCallTime getTemplate(final long identifier)
        throws AbmBucketException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getTemplate()");

        final FreeCallTime template;

        try
        {
            final BucketProvision service = getService();

            if (service != null)
            {
                try
                {
                    final ServiceUsageBucketTemplateHolder templateHolder =
                        new ServiceUsageBucketTemplateHolder();

                    final short result =
                        service.getServiceUsageBucketTemplate(
                            (int)identifier,
                            templateHolder);

                    if (result != AbmResultCode.SUCCESS)
                    {
                        throw new AbmBucketException(
                            "Failed to get template " + identifier, result);
                    }

                    template = adapt(templateHolder.value);
                }
                catch (final org.omg.CORBA.COMM_FAILURE commFail)
                {
                    invalidate(commFail);

                    throw new AbmBucketException(
                        "Failed to get template " + identifier, commFail);
                }
            }
            else
            {
                throw new AbmBucketException(
                    "Failed to get template " + identifier
                    + ".  No service available.");
            }
        }
        finally
        {
            pmLogMsg.log(getContext());
        }

        return template;
    }


    /**
     * Deletes the given template from ABM.
     *
     * @param identifier The identifier of the template to delete.
     *
     * @exception AbmBucketException Thrown if there is a failure to delete the
     * template.
     */
    public void deleteTemplate(final long identifier)
        throws AbmBucketException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deleteTemplate()");

        try
        {
            final BucketProvision service = getService();

            if (service != null)
            {
                try
                {
                    final short result =
                        service.deleteServiceUsageBucketTemplate((int)identifier);

                    if (result != AbmResultCode.SUCCESS)
                    {
                        throw new AbmBucketException(
                            "Failed to delete template " + identifier, result);
                    }
                }
                catch (final org.omg.CORBA.COMM_FAILURE commFail)
                {
                    invalidate(commFail);

                    throw new AbmBucketException(
                        "Failed to delete template " + identifier, commFail);
                }
            }
            else
            {
                throw new AbmBucketException(
                    "Failed to delete template " + identifier
                    + ".  No service available.");
            }
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }


    /**
     * Adapts an ABM bucket template to look like a CRM template.
     *
     * @param abmTemplate The ABM bucket template to adapt.
     * @return The equivalent CRM bucket template.
     */
    private FreeCallTime adapt(final ServiceUsageBucketTemplate abmTemplate)
    {
        final FreeCallTime crmTemplate = new FreeCallTime();
        crmTemplate.setIdentifier(abmTemplate.bucketId);
        crmTemplate.setName(abmTemplate.name);
        crmTemplate.setSpid(abmTemplate.spid);
        crmTemplate.setFreeCallTime(roundSecondsToMinutes(abmTemplate.initialAward));

        // We have to assume that roll-over has been enabled.  If not, then the
        // roll-over specific values will reflect this.
        crmTemplate.setRollOverMinutes(FCTRollOverEnabledEnum.ENABLED);
        crmTemplate.setRollOverPercentage(abmTemplate.rolloverPercent);
        crmTemplate.setMaximumRollOver(roundSecondsToMinutes(abmTemplate.rolloverMax));
        crmTemplate.setExpiryPercentage(abmTemplate.expiryPercent);

        if (abmTemplate.groupPrecedence == BucketProvision.GROUPPRECEDENCE_PERSONAL)
        {
            crmTemplate.setUsagePrecedence(FCTUsagePrecedenceEnum.SUBSCRIBER);
        }
        else
        {
            crmTemplate.setUsagePrecedence(FCTUsagePrecedenceEnum.GROUP);
        }

        crmTemplate.setGroupLimit(roundSecondsToMinutes(abmTemplate.groupUsageLimit));

        return crmTemplate;
    }


    /**
     * Invalidates the underlying CORBA connection so that upon the next
     * communication the connection will be reinitialized.
     *
     * @param throwable The throwable (may be null) that prompted the
     * invalidation.
     */
    public synchronized void invalidate(final Throwable throwable)
    {
        // Only raise a SNMP trap if this is the first time we discover the
        // connection is gone.
        if (service_ != null)
        {
            CorbaClientProperty abmProperty = getAbmProperty();
            new EntryLogMsg(11379, this, "",
                abmProperty.toString(),
                new String[]
                {
                abmProperty.getNameServiceHost(),
                    String.valueOf(abmProperty.getNameServicePort())
                },
                throwable).log(getContext());
        }

        corbaProxy_.invalidate();
        service_ = null;
    }


    /**
     * Gets the CORBA client properties for the CORBA connection to ABM.
     *
     * @return The CORBA client properties for the CORBA connection to ABM.
     *
     * @exception HomeException Thrown if there is a problem accessing Home data
     * in the context.
     */
    private CorbaClientProperty getClientProperties()
        throws HomeException
    {
        final Home home = (Home)getContext().get(CorbaClientPropertyHome.class);

        if (home == null)
        {
            throw new HomeException("Corba client configuration does not exist.");
        }

        final CorbaClientProperty abmProperty =
            (CorbaClientProperty)home.find(getContext(),CORBA_SETTINGS_KEY);

        return abmProperty;
    }
    
    private CorbaClientProperty getAbmProperty()
    {
        return getAbmProperty(false);
    }    
    
    private CorbaClientProperty getAbmProperty(boolean reload)
    {
        if( !reload && abmProperty_ != null )
        {
            return abmProperty_;
        }
        try
        {
            abmProperty_ = getClientProperties();
        }
        catch (HomeException e)
        {
            new DebugLogMsg(this, HomeException.class.getSimpleName() + " occurred in " + ProductAbmBucketClient.class.getSimpleName() + ".getAbmProperty(): " + e.getMessage(), e).log(getContext());
        }
        return abmProperty_;
    }


    /**
     * Creates a CorbaClientProxy for communicating with ABM.
     *
     * @return A CorbaClientProxy for communicating with ABM.
     *
     * @exception HomeException Thrown if there is a problem accessing Home data
     * in the context.
     * @exception CorbaClientException Thrown if there is a CORBA problem creating the
     * proxy.
     */
    private CorbaClientProxy createCorbaClientProxy()
        throws HomeException, CorbaClientException
    {
        //final ORB orb = ORB.init(new String[]{}, null);

        final CorbaClientProxy corbaProxy = CorbaSupportHelper.get(getContext()).createProxy(getContext(),getAbmProperty(true));

        return corbaProxy;
    }


    /**
     * Gets the service provisioning interface used to manage ABM Buckets.
     *
     * @return The service provisioning interface.
     */
    private synchronized BucketProvision getService()
    {
        if (service_ != null)
        {
            return service_;
        }

        if (corbaProxy_ == null)
        {
            try
            {
                corbaProxy_ = createCorbaClientProxy();
            }
            catch (final CorbaClientException exception)
            {
                invalidate(exception);
                return null;
            }
            catch (final HomeException exception)
            {
                invalidate(exception);
                return null;
            }
        }

        final org.omg.CORBA.Object objServant = corbaProxy_.instance();
        if (objServant != null)
        {
            try
            {
                // attempt to derive SubscriberProvision
                service_ = BucketProvisionHelper.narrow(objServant);

                if (service_ != null)
                {
                    // snmp external link up trap
                    CorbaClientProperty abmProperty = getAbmProperty();
                    new EntryLogMsg(11380 , this, "",
                        abmProperty.toString(),
                        new String[]
                        {
                        abmProperty.getNameServiceHost(),
                            String.valueOf(abmProperty.getNameServicePort())
                        },
                        null).log(getContext());
                }

                return service_;
            }
            catch (final Exception exception)
            {
                invalidate(exception);
                return null;
            }
        }

        invalidate(null);
        return null;
    }


    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceDescription()
     */
    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }


    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceName()
     */
    public String getName()
    {
        CorbaClientProperty property = getAbmProperty();
        if( property != null )
        {
            return property.getKey();
        }
        else
        {
            return CORBA_SETTINGS_KEY;
        }
    }


    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#isServiceAlive()
     */
    public boolean isAlive()
    {
        return getService() != null;
    }


    /**
     * Gets the voice service identifier used for creating new templates in
     * ABM.
     *
     * @return The voice service identifier used for creating new templates.
     */
    private int getVoiceServiceIdentifier()
    {
        // NOTE - 2004-11-18 - This is currently a simply value on the
        // configuration screen.  In the future, there are likely to be multiple
        // service types and this information will come from elsewhere.
        final ProductAbmClientConfig config =
            (ProductAbmClientConfig)getContext().get(ProductAbmClientConfig.class);
        
        return config.getAbmVoiceServiceIdentifier();
    }


    /**
     * Initializes the underlying CORBA connection.
     */
    private void initialize()
    {
        try
        {
            service_ = null;
            corbaProxy_ = createCorbaClientProxy();
        }
        catch (final Exception exception)
        {
            final IllegalStateException newException =
                new IllegalStateException(
                    "Failed to initialize CORBA connection for " + CORBA_SETTINGS_KEY);
            newException.initCause(exception);
            throw newException;
        }
    }


    /**
     * Rounds the given number of seconds to the nearest minute.
     *
     * @param seconds The number of seconds.
     * @return The number of minutes.
     */
    private int roundSecondsToMinutes(final int seconds)
    {
        return (int)Math.round(seconds / 60.0);
    }
    
	
    /**
     * Unadapts a CRM bucket template to look like a ABM template.
     *
     * @param crmTemplate The CRM bucket template to adapt.
     * @return The equivalent ABM bucket template.
     */
    private ServiceUsageBucketTemplate unadapt(final FreeCallTime crmTemplate)
    {
        final ServiceUsageBucketTemplate abmTemplate = new ServiceUsageBucketTemplate();
        abmTemplate.bucketId = (int)crmTemplate.getIdentifier();
        abmTemplate.name = crmTemplate.getName();
        abmTemplate.spid = crmTemplate.getSpid();
        abmTemplate.svcid = getVoiceServiceIdentifier();

        abmTemplate.initialAward = crmTemplate.getFreeCallTime() * 60;
        abmTemplate.rolloverPercent = crmTemplate.getRollOverPercentage();
        abmTemplate.rolloverMax = crmTemplate.getMaximumRollOver() * 60;
        abmTemplate.expiryPercent = crmTemplate.getExpiryPercentage();

        if (crmTemplate.getUsagePrecedence() == FCTUsagePrecedenceEnum.SUBSCRIBER)
        {
            abmTemplate.groupPrecedence = BucketProvision.GROUPPRECEDENCE_PERSONAL;
        }
        else
        {
            abmTemplate.groupPrecedence = BucketProvision.GROUPPRECEDENCE_GROUP;
        }

        abmTemplate.groupUsageLimit = crmTemplate.getGroupLimit() * 60;

        return abmTemplate;
    }


    /**
     * The key used to look up the CORBA settings in the context.
     */
    private static final String CORBA_SETTINGS_KEY = "ProductAbmBucketClient";

    /**
     * Used to identify this class in the PM logs.
     */
    private static final String PM_MODULE = ProductAbmBucketClient.class.getName();

    /**
     * The most recently used set of properties for the CORBA connection.
     */
    private CorbaClientProperty abmProperty_;
    
    /**
     *  The service interface.
     */
    private BucketProvision service_;

    /**
     * The CORBA communication proxy.
     */
    private CorbaClientProxy corbaProxy_;

    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        CorbaClientProperty property = getAbmProperty();
        return SystemStatusSupportHelper.get().generateConnectionStatus(property, isAlive());
    }


    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }

} // class
