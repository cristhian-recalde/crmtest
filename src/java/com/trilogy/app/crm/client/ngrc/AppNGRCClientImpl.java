/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client.ngrc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.Principal;

import com.trilogy.app.crm.client.ClientException;
import com.trilogy.app.crm.config.NGRCClientConfig;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.product.s2200.idr.provisioning.soap.SubscriberProv63;
import com.trilogy.product.s2200.idr.provisioning.soap.SubscriberProv63Stub;
import com.trilogy.product.s2200.idr.provisioning.soap.xsd.ProvResponse;


/**
 * 
 * @author asim.mahmood@redknee.com
 * @since 8.5
 * 
 */
public class AppNGRCClientImpl extends ContextAwareSupport implements PropertyChangeListener, AppNGRCClient
{

    public AppNGRCClientImpl(Context ctx) throws AgentException
    {
        setContext(ctx);
        NGRCClientConfig config = (NGRCClientConfig) getContext().get(NGRCClientConfig.class);
        if (null == config)
        {
            throw new AgentException("System Error. NGRC Client Config not found in Context.");
        }
        config.addPropertyChangeListener(this);
        loadClient(config.getURL());
    }


    public void addOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, String deviceType, boolean confirmationRequired)
        throws ClientException
    {
        NGRCClientConfig config = (NGRCClientConfig) getContext().get(NGRCClientConfig.class);
        try
        {
            Integer confirmation = confirmationRequired ? 1 : 0;
            SubscriberProv63 client = getClient();
            ProvResponse response = client.addOptIn(config.getUsername(), config.getPassword(ctx), msisdn, null, spid,
                    baseRatePlan, baseOptIn, roamingRatePlan, roamingOptIn, deviceType, confirmation);
            
            handleReponse("addOptIn", msisdn, response, config);
        }
        catch (ClientException re)
        {
            throw re;
        }
        catch (Exception e)
        {
            final String msg = "Error occured during addOptIn request to NGRRC for msisdn=" + msisdn;
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new ClientException(msg, e, (short) ExternalAppSupport.REMOTE_EXCEPTION);
        }
    }

    public void deleteOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, Integer delayedOptOut)
        throws ClientException
    {
        NGRCClientConfig config = (NGRCClientConfig) getContext().get(NGRCClientConfig.class);
        try
        {
            SubscriberProv63 client = getClient();
            ProvResponse response = client.deleteOptIn(config.getUsername(), config.getPassword(ctx), msisdn, null, spid,
                    baseRatePlan, baseOptIn, roamingRatePlan, roamingOptIn, delayedOptOut);
            
            handleReponse("deleteOptIn", msisdn, response, config);

        }
        catch (ClientException re)
        {
            throw re;
        }
        catch (Exception e)
        {
            final String msg = "Error occured during deleteOptIn request to NGRRC for msisdn=" + msisdn;
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new ClientException(msg, e, (short) ExternalAppSupport.REMOTE_EXCEPTION);
        }
    }
    
    @Override
    public void updateImsi(Context ctx, String oldIMSI, String newIMSI)
    throws ClientException
    {
        NGRCClientConfig config = (NGRCClientConfig) getContext().get(NGRCClientConfig.class);
        try
        {
            SubscriberProv63 client = getClient();
            ProvResponse response = client.updateImsi(config.getUsername(), config.getPassword(ctx), oldIMSI, newIMSI);
            
            handleReponse("updateImsi", oldIMSI, response, config);

        }
        catch (ClientException re)
        {
            throw re;
        }
        catch (Exception e)
        {
            final String msg = "Error occured during updateImsi request to NGRC for old IMSI=" + oldIMSI;
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new ClientException(msg, e);
        }
    }
    
    protected void handleReponse(String method, String msisdn, ProvResponse response, NGRCClientConfig config) 
        throws ClientException
    {
        if (response.getResultCode() == null || !config.isInSuccessCodes(response.getResultCode()))
        {
            final String msg = String.format("Unsuccessful response [rc=%s, tag=%s] for %s request to NGRC for msisdn=%s.",
                    String.valueOf(response.getResultCode()), String.valueOf(response.getTag()), method,  msisdn);
            
            throw new ClientException(msg, response.getResultCode() != null ? response.getResultCode().shortValue() : (short) ExternalAppSupport.REMOTE_EXCEPTION);
        }
    }


    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(NGRCClientConfig.URL_PROPERTY))
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                LogSupport.debug(getContext(), this,
                        "Re-loading the TFA SOAP client with the URL [" + evt.getNewValue() + "]");
            }
            String url = (String) evt.getNewValue();
            loadClient(url);
        }
    }


    protected synchronized void loadClient(String url)
    {
        try
        {
            soapClient_ = new SubscriberProv63Stub(url);
        }
        catch (Exception e)
        {
            soapClient_ = null;
            new MajorLogMsg(this, "An error occured when trying to load the NGRC SOAP client with URL ["
                    + url + "]", e).log(getContext());
        }
    }


    protected synchronized SubscriberProv63 getClient() throws Exception
    {
        if (null != soapClient_)
        {
            return soapClient_;
        }
        else
        {
            throw new Exception("NGRC SOAP client has not been properly initialized. Check the configuration.");
        }
    }


    protected String getAgentId(final Context ctx)
    {
        User user = (User) ctx.get(Principal.class);
        String agentId = "";
        if (null != user)
        {
            agentId = user.getId();
        }
        else
        {
            LogSupport.minor(ctx, this, "Unable to retrieve the AgentId from the Context.");
        }
        return agentId;
    }

    private SubscriberProv63 soapClient_ = null;
}
