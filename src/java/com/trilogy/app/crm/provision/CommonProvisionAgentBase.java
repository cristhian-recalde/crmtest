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
package com.trilogy.app.crm.provision;

import java.util.Collection;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.hlr.nortel.BasicServiceMsisdn;
import com.trilogy.app.crm.bean.hlr.nortel.NortelServiceMapConfig;
import com.trilogy.app.crm.bean.service.SubscriptionProvisioningHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriptionProvisioningHistoryTransientHome;
import com.trilogy.app.crm.calculator.ValueCalculator;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.app.crm.support.BeanLoaderSupport;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.app.crm.support.KeyValueSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;



/**
 * Common class for Generic, Sms and Voice provisioning and unprovisioning agents.
 *
 * @author joe.chen@redknee.com
 */
public abstract class CommonProvisionAgentBase implements ContextAgent
{
    // SPG parameter context keys
    public static String SPG_PROVISIONING_CUSTOM_AMSISDN= "SPG_CUSTOM_AMSISDN";
    public static String SPG_PROVISIONING_CUSTOM_BEAR_TYPE_ID= "SPG_CUSTOM_BEAR_TYPE_ID";
    public static String SPG_PROVISIONING_CUSTOM_OLD_IMSI= "SPG_CUSTOM_OLD_IMSI";
    public static String SPG_PROVISIONING_CUSTOM_PACK_KI= "SPG_CUSTOM_PACK_KI";
    public static String SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE = "SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE";
    public static String SPG_PROVISIONING_CUSTOM_OLD_VOICE_MSISDN = "SPG_PROVISIONING_CUSTOM_OLD_VOICE_MSISDN";
    public static String SPG_PROVISIONING_CUSTOM_OLD_LANGUAGE = "SPG_PROVISIONING_CUSTOM_OLD_LANGUAGE";
    
    public static final String SPG_PROVISIONING_CUSTOM_OLD_PRICEPLANID = "SPG_PROVISIONING_CUSTOM_OLD_PRICEPLANID";
    public static final String SPG_PROVISIONING_CUSTOM_OLD_BILLCYCLEID = "SPG_PROVISIONING_CUSTOM_OLD_BILLCYCLEID";
    public static final String SPG_PROVISIONING_CUSTOM_BILLCYCLEID = "SPG_PROVISIONING_CUSTOM_BILLCYCLEID";
    public static final String SPG_PROVISIONING_CUSTOM_BILLCYCLEDAY = "SPG_PROVISIONING_CUSTOM_BILLCYCLEDAY";
    public static final String SPG_PROVISIONING_CUSTOM_OLD_BILLCYCLEDAY = "SPG_PROVISIONING_CUSTOM_OLD_BILLCYCLEDAY";
    public static final String SPG_PROVISIONING_EXAPTION= "SPG_PROVISIONING_EXAPTION"; 
    public static final String SPG_PROVISIONING_EXAPTION_CODE= "SPG_PROVISIONING_EXAPTION_CODE";
    public static final String SPG_PROVISIONING_CLIENT_VERSION = "SPG_PROVISIONING_CLIENT_VERSION"; 
    
    
    
    
    
    /**
     * Replaces the MSISDN for fax, voice and data and the IMSI tokens with the actual
     * values from the Subscriber in a hlr command string.
     *
     * @param context
     *            context used for logging
     * @param data
     *            the hlr data string
     * @param subscriber
     *            the subscriber
     * @return modified hlr data string
     * @deprecated Use {@link #replaceHLRCommand(Context,String,Subscriber,Msisdn,String)}
     *             instead
     */
    @Deprecated
    public static String replaceHLRCommand(final Context context, final String data, final Subscriber subscriber)
    {
        return replaceHLRCommand(context, data, subscriber, null, null);
    }


    /**
     * Replaces the MSISDN for fax, voice and data and the IMSI tokens with the actual
     * values from the Subscriber in a hlr command string.
     *
     * @param context
     *            context used for logging
     * @param data
     *            the hlr data string
     * @param subscriber
     *            the subscriber
     * @param aMsisdn
     *            The additional MSISDN to be provisioned. Use <code>null</code> if this
     *            is not an additional MSISDN HLR command.
     * @param bearerType
     *            The bearer type of the additional MSISDN to be provisioned. Use
     *            <code>null</code> if this is not an additional MSISDN HLR command.
     * @return modified hlr data string
     */
    public static String replaceHLRCommand(final Context context, final String data, final Subscriber subscriber,
        final Msisdn aMsisdn, final String bearerType)
    {
        // Install some base cases into context for key/value calculation
        Context sCtx = context.createSubContext();
        sCtx.put(Subscriber.class, subscriber);
        sCtx.put(Msisdn.class, aMsisdn);
        sCtx.put(HLRConstants.HLR_PARAMKEY_BEARER_TYPE_KEY, bearerType);
        
        GenericPackage pkg = null;
        try
        {
            pkg = PackageSupportHelper.get(sCtx).getPackage(sCtx, subscriber.getTechnology(), subscriber.getPackageId(), subscriber.getSpid());
        }
        catch (HomeException e)
        {
            new InfoLogMsg(CommonProvisionAgent.class, 
                    "Failed to retrieve package " + subscriber.getPackageId()
                    + " for sub [ID=" + subscriber.getId() + ", MSISDN=" + subscriber.getMsisdn()
                    + "].  Package related variable substitution may fail in command...", null).log(sCtx);
        }
        if (pkg != null)
        {
            sCtx.put(pkg.getClass(), pkg);
        }
        
        // Install appropriate bean loader for key/value calculation
        BeanLoaderSupport beanLoaderSupport = BeanLoaderSupportHelper.get(sCtx);
        beanLoaderSupport.setBeanLoaderMap(sCtx, beanLoaderSupport.getBeanLoaderMap(sCtx, Subscriber.class));
        
        String result = data;
        
        // Substitute configured HLR variables
        Collection<KeyConfiguration> keys = KeyValueSupportHelper.get(sCtx).getConfiguredKeys(sCtx, true, KeyValueFeatureEnum.HLR_PARAMETER);
        for (KeyConfiguration key : keys)
        {
            if (result.contains(key.getKey()))
            {
                ValueCalculator calc = key.getValueCalculator();
                if (calc != null)
                {
                    Object value = calc.getValue(sCtx);
                    if (value != null)
                    {
                        result = result.replace(key.getKey(), String.valueOf(value));

                        if (LogSupport.isDebugEnabled(sCtx))
                        {
                            new DebugLogMsg(CommonProvisionAgent.class.getName(), "After replacing " + key.getKey() + " Value, cmd=["
                                + result + "]", null).log(sCtx);
                        }
                    }
                    else
                    {
                        new MinorLogMsg(CommonProvisionAgent.class, 
                                "Failed to retrieve value for " + key.getKey()
                                + " for sub [ID=" + subscriber.getId() + ", MSISDN=" + subscriber.getMsisdn() + "]", null).log(sCtx);
                    }
                }
            }
        }

        result = replaceNortelHlrCommands(sCtx, result, subscriber);

        if (LogSupport.isDebugEnabled(sCtx))
        {
            new DebugLogMsg(CommonProvisionAgent.class, "Replaced HLR command: " + result, null).log(sCtx);
        }
        return result;
    }


    /**
     * Replace the msisdn with valid provision string.
     *
     * @param bsm
     *            Basic service MSISDN.
     * @param msisdn
     *            MSISDN.
     * @return Returns a Nortel provision basic service MSISDN.
     */
    public static String generateNortelProvisionBSMsisdn(final BasicServiceMsisdn bsm, final String msisdn)
    {
        // <cc>,<NDC>,<SN>,<BC>
        if (msisdn == null || msisdn.length() == 0)
        {
            return msisdn;
        }
        final StringBuilder sb = new StringBuilder();
        final String delimiter = ",";
        if (msisdn.length() >= bsm.getCcLength() + bsm.getNdcLength() + bsm.getSnLength())
        {
            sb.append(msisdn.substring(0, bsm.getCcLength()));
            sb.append(delimiter);
            sb.append(msisdn.substring(bsm.getCcLength(), bsm.getNdcLength() + 1));
            sb.append(delimiter);
            sb.append(msisdn.substring(bsm.getCcLength() + bsm.getNdcLength()));

        }
        else if (msisdn.length() == bsm.getNdcLength() + bsm.getSnLength())
        {
            sb.append(bsm.getDefaultCC());
            sb.append(delimiter);
            sb.append(msisdn.substring(0, bsm.getNdcLength()));
            sb.append(delimiter);
            sb.append(msisdn.substring(bsm.getNdcLength()));

        }
        else
        {
            sb.append(bsm.getDefaultCC());
            sb.append(delimiter);
            if (msisdn.length() <= bsm.getNdcLength())
            {

                sb.append(msisdn);
                sb.append(delimiter);
            }
            else
            {
                sb.append(msisdn.substring(0, bsm.getNdcLength()));
                sb.append(delimiter);
                sb.append(msisdn.substring(bsm.getNdcLength()));
            }

        }

        return sb.toString();
    }


    /**
     * Support method to replace basic service provision msisdn.
     *
     * @param ctx
     *            The operating context.
     * @param data
     *            Current data.
     * @param bsm
     *            Basic service MSISDN.
     * @param msisdn
     *            MSISDN.
     * @return New basic serivce provision command.
     */
    public static String replaceNortelBasicServiceHlrCommand(final Context ctx, final String data,
        final BasicServiceMsisdn bsm, final String msisdn)
    {

        String result = data;
        try
        {
            if (bsm != null && msisdn != null && msisdn.length() > 0 && data.indexOf(bsm.getReplaceKey()) >= 0)
            {
                result = data.replaceAll(bsm.getReplaceKey(), generateNortelProvisionBSMsisdn(bsm, msisdn));
            }
        }
        catch (final Throwable t)
        {
            new InfoLogMsg(CommonProvisionAgent.class.getName(), "Replaced Nortel HLR command: " + result
                + " got an exception " + t.getMessage(), t).log(ctx);
        }

        return result;
    }


    /**
     * To replace basic service provision msisdns with Voice, SMS, and Data.
     *
     * @param ctx
     *            The operating context.
     * @param data
     *            Current HLR command.
     * @param sub
     *            The subscriber in question.
     * @return HLR command with the basic service provision MSISDN variables replaced.
     */
    public static String replaceNortelHlrCommands(final Context ctx, final String data, final Subscriber sub)
    {
        final NortelServiceMapConfig config = (NortelServiceMapConfig) ctx.get(NortelServiceMapConfig.class);
        String result = data;

        if (config != null)
        {
            result = replaceNortelBasicServiceHlrCommand(ctx, result, config.getVoiceService(), sub.getMSISDN());
            result = replaceNortelBasicServiceHlrCommand(ctx, result, config.getFaxService(), sub.getFaxMSISDN());
            result = replaceNortelBasicServiceHlrCommand(ctx, result, config.getDataService(), sub.getDataMSISDN());
        }
        else
        {
            new MinorLogMsg(CommonProvisionAgent.class.getName(), "NortelServiceMapConfig Not Found ", null).log(ctx);
        }
        
        return result;
    }


    /**
     * For unprovision hlr, we need to detect this flag, so we don't need to unprovision
     * individual hlr.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber in question.
     * @return Returns <code>true</code> if the MSISDN has just been removed from HLR,
     *         <code>false</code> otherwise.
     */
    protected abstract boolean hasMsisdnJustRemovedFromHlr(Context ctx, Subscriber sub);



    /**
     * Executes the HLR commands.
     *
     * @param ctx
     *            The operating context.
     * @param hlrId
     *            ID of the HLR to use.
     * @param hlrCmds
     *            HLR commands to send.
     * @param subscriber
     *            The subsciber in question.
     * @throws AgentException
     *             Thrown if the execution fails.
     */
    public abstract void callHlr(final Context ctx, final boolean isProvision, final Subscriber subscriber,
            final com.redknee.app.crm.bean.Service service, String aMsisdn, String bearTypeId) throws ProvisionAgentException;
    
    
    protected int executeHlrCommand(final Context ctx, final boolean isProvision, final Subscriber subscriber,
            final com.redknee.app.crm.bean.Service service, String aMsisdn, String bearTypeId) throws ProvisionAgentException
    {
        int result = 0;
        if (SystemSupport.needsHlr(ctx))
        {
            if (!hasMsisdnJustRemovedFromHlr(ctx, subscriber))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this,
                            "Provision  service " + service.getName() + "(ID=" + service.getID()
                                    + ") to the HLR (HLR ID=" + subscriber.getHlrId() + "). " + " for subscriber "
                                    + subscriber.getId());
                }
                Subscriber oldSub = (Subscriber)ctx.get(Lookup.OLD_FROZEN_SUBSCRIBER, subscriber);
                
                Context subCtx = ctx.createSubContext();
                subCtx.put(SPG_PROVISIONING_CUSTOM_AMSISDN, aMsisdn);
                subCtx.put(SPG_PROVISIONING_CUSTOM_BEAR_TYPE_ID, bearTypeId);
                
                if (ctx.has(HLRConstants.HLR_PARAMKEY_OLD_IMSI_KEY))
                    subCtx.put(SPG_PROVISIONING_CUSTOM_OLD_IMSI,
                        ctx.get(HLRConstants.HLR_PARAMKEY_OLD_IMSI_KEY));
                else 
                    subCtx.put(SPG_PROVISIONING_CUSTOM_OLD_IMSI,oldSub.getIMSI()); 
                
                subCtx.put(SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE,
                        String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_SERVICE));
                subCtx.put(SPG_PROVISIONING_CUSTOM_OLD_VOICE_MSISDN, oldSub.getMsisdn()); 
                subCtx.put(SPG_PROVISIONING_CUSTOM_OLD_LANGUAGE, oldSub.getBillingLanguage());

                
                
                if (subscriber.getTechnology() == TechnologyEnum.GSM)
                {
                    try
                    {
                        final GSMPackage pkg = PackageSupportHelper.get(ctx).getGSMPackage(ctx,
                                subscriber.getPackageId());
                        subCtx.put(SPG_PROVISIONING_CUSTOM_PACK_KI, pkg.getKI());
                    }
                    catch (HomeException homeEx)
                    {
                    }
                }
                
				// Adding a transient home in place of the Subscription
				// Provisioning History Home. This is a workaround to avoid the
				// ServiceProvisioningGatewaySupport class to add a provisioning
				// history for the service in case of failure. This will be done
				// later by the
				// SubscriberServiceSupport.provisionSubscriberServices or
				// SubscriberServiceSupport.unprovisionSubscriberServices, as an
				// exception will be thrown.
                // TODO: Fix this properly by not having the support class adding any provisioning history.
                subCtx.put(SubscriptionProvisioningHistoryHome.class, new SubscriptionProvisioningHistoryTransientHome(ctx));
                
                 result = ServiceProvisioningGatewaySupport.prepareAndSendIndividualServiceToSPG(subCtx, subscriber,
                        service, ServiceProvisioningGatewaySupport.HLR_SERVICE_SPG_SERVICE_ID, isProvision, this);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this,
                            "Provision  service " + service.getName() + "(ID=" + service.getID()
                                    + ") to the HLR (HLR ID=" + subscriber.getHlrId() + "). " + " for subscriber "
                                    + subscriber.getId());
                }
            }
        }
        return result;
    }




    
    
    public Subscriber getSubscriber(Context ctx) throws AgentException
    {
        Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
        if (subscriber == null)
        {
            throw new AgentException("System error: No subscriber to provision");
     
        }
        return subscriber;
        
    }
    
    public PricePlanVersion getPriceplanVersion(Context ctx, Subscriber sub) throws AgentException
    {
        PricePlanVersion pricePlan = null;
        try
        {
            // the Raw Price Plan Version has all the services, not only the subscribed
            // ones
            pricePlan = sub.getRawPricePlanVersion(ctx);
        }
        catch (final HomeException e)
        {
            throw new AgentException(e.getMessage(), e);
            
        }
        
        if (pricePlan == null)
        {
            throw new AgentException("System error: No price plan associated with subscriber");
            
        }
        return pricePlan;
    }

    public Account getAccount(Context ctx) throws AgentException
    {
        Account account = (Account) ctx.get(Account.class);
        if (account == null)
        {
            throw new AgentException("System error: subscriber's account not found");
            
        }
        return account;
    }

    public String getServiceDescription(final Service service)
    {
        final String message =
            "'" + service.getID()
            + " - " + service.getName() + "'";

        return message;
    }

    public Service getService( final Context ctx) throws AgentException
    {
        Service service = (Service) ctx.get(Service.class);
        if (service == null)
        {
            throw new AgentException("System error: Service for voice provisioning not found in context");
         
        }
        return service;
    }
    
    public String getHlrCommand(final Context ctx, final Subscriber subscriber, final Service service)
    {
        String hlrCmds = new String("");
        if (SystemSupport.needsHlr(ctx))
        {
            if (subscriber.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
            {
                hlrCmds = service.getProvisionConfigs();
            }
            else if (subscriber.getSubscriberType() == SubscriberTypeEnum.PREPAID)
            {
                hlrCmds = service.getPrepaidProvisionConfigs();
            }

            if (hlrCmds == null || hlrCmds.length() == 0)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "HLR commands are missing for voice provisioning", null).log(ctx);
                }
            }

            // look for the SogClient for the subscriber's HLR ID
            // short hlrId = subscriber.getHlrId();
        }
        return hlrCmds; 
    }
}
