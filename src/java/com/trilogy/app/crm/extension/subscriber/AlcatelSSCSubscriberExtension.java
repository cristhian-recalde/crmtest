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
package com.trilogy.app.crm.extension.subscriber;

import java.util.Collection;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriptionTypeAware;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.client.alcatel.AlcatelProvisioning;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.support.AlcatelSSCSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.util.MessageManagerText;


/**
 * Extension that contains key/value pairs that can be provisioned to an Alcatel SSC for
 * such broadband services
 * 
 * @author aaron.gourley@redknee.com
 * @author simar.singh@redknee.com (impmented update() and relted private functions)
 * @since 8.2
 */
public class AlcatelSSCSubscriberExtension extends AbstractAlcatelSSCSubscriberExtension implements ContextAware
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public AlcatelSSCSubscriberExtension()
    {
        super();
    }


    public AlcatelSSCSubscriberExtension(Context ctx)
    {
        super();
        setContext(ctx);
    }


    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        // Validate whether or not this extension is allowed to be contained within the
        // parent bean.
        ExtensionAware parentBean = this.getParentBean(ctx);
        if (parentBean instanceof SubscriptionTypeAware)
        {
            SubscriptionTypeAware subTypeAware = (SubscriptionTypeAware) parentBean;
            SubscriptionType subscriptionType = subTypeAware.getSubscriptionType(ctx);
            if (subscriptionType == null || !SubscriptionTypeEnum.BROADBAND.equals(subscriptionType.getTypeEnum()))
            {
                cise.thrown(new IllegalArgumentException(this.getName(ctx) + " extension only allowed for "
                        + SubscriptionTypeEnum.BROADBAND + " subscription types."));
            }
        }
        // TODO: Validate extension contents (i.e. key/value pairs)
        cise.throwAll();
    }


    /**
     * {@inheritDoc}
     */
    public void install(Context ctx) throws ExtensionInstallationException
    {
        // TODO: Implement Alcatel SSC extension creation related business logic here
    }


    /**
     * {@inheritDoc}
     */
    public void uninstall(Context ctx) throws ExtensionInstallationException
    {
        // TODO: Implement Alcatel SSC extension removal related business logic here
    }


    /**
     * this functions {@inheritDoc}
     */
    public void update(Context ctx) throws ExtensionInstallationException
    {
        try
        {
            if (isAlcatelUpdateRequired(ctx))
            {
                alcatelUpdate(ctx);
            }
        }
        catch (Throwable t)
        {
            // TODO Auto-generated catch block
            logException(ctx, new IllegalStateException(
                    "Failed to propagate Alcatel related updates from Subscriber-Extension of TYPE["
                            + AlcatelSSCSubscriberExtension.class.getSimpleName() + "]. Error[" + t.getMessage() + "]",
                    t));
        }
    }


    /**
     * {@inheritDoc}
     */
    public void move(Context ctx, Object newContainer) throws ExtensionInstallationException
    {
        // got nothing to move
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setKeyValuePairs(Map keyValuePairs) throws IllegalArgumentException
    {
        assertKeyValuePairs(keyValuePairs);
        assertBeanNotFrozen();
        PMLogMsg pm = new PMLogMsg("AlcatelSSC", "Subscriber.initializeMap()");
        try
        {
            AlcatelSSCSupportHelper.get(getContext()).initializeMap(getContext(), KeyValueFeatureEnum.ALCATEL_SSC_SUBSCRIPTION, keyValuePairs);
        }
        finally
        {
            if (getContext() != null)
            {
                pm.log(getContext());
            }
        }
        super.setKeyValuePairs(keyValuePairs);
    }


    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return ctx_;
    }


    /**
     * {@inheritDoc}
     */
    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }


    /**
     * Determines if Alcatel system needs an update with respect to the state of the
     * extension
     * 
     * @author simar.singh@redknee.com
     * @param ctx
     * @return - true ; if Alcatel System needs an update
     * @throws HomeException 
     */
    private boolean isAlcatelUpdateRequired(Context ctx) throws HomeException
    {
        // TODO: Implement Alcatel SSC key/value update related business logic here
        final AlcatelSSCSubscriberExtension oldExt;
        if (ctx.has(Lookup.OLDSUBSCRIBER))
        {
            Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
            oldExt = getAlcatelSSCSubscriberExtension(ctx, oldSub.getId());
        }
        else
        {
            oldExt = HomeSupportHelper.get(ctx).findBean(ctx, AlcatelSSCSubscriberExtension.class, new EQ(
                    AlcatelSSCSubscriberExtensionXInfo.SUB_ID, getSubId()));
        }
        if (null != oldExt)
        {
            final Map<?, ?> oldKeyValuePairs = oldExt.getKeyValuePairs();
            final Map<?, ?> newKeyValuePairs = this.getKeyValuePairs();
            if (oldKeyValuePairs == null || newKeyValuePairs == null)
            {
                throw new HomeException("Error finding key value maps OLD-Key-Value-MAP [" + oldKeyValuePairs
                        + "], NEW-key-value-MAP [" + newKeyValuePairs + "] for Alcatel-Extension of Subscriber ["
                        + getSubId() + "]");
            }
            if (!oldKeyValuePairs.equals(newKeyValuePairs))
            {
                // some keys might have been removed; and-or new ones might have been
                // added or the respective values of the key do not match.
                return true;
            }
        }
        else
        {
            throw new HomeException("Could not find Subscriber Extension Subscriber-Extension of TYPE["
                    + AlcatelSSCSubscriberExtension.class.getSimpleName() + "] for SUBSCRIBER-ID [" + getSubId() + "]");
        }
        return false;
    }


    /**
     * Safe Update services for the Subscriber holding this extension
     * 
     * @author simar.singh@redknee.com
     * 
     * @param ctx
     * @param subscriberServices
     * @throws ProvisionAgentException
     * @throws HomeException
     */
    private void alcatelUpdate(Context ctx)
    {
        Subscriber sub = getSubscriber(ctx);
        try
        {
            Collection<Service> services = HomeSupportHelper.get(ctx).getBeans(ctx, Service.class, new In(ServiceXInfo.ID, sub
                    .getServices(ctx, ServiceTypeEnum.ALCATEL_SSC)));
            AlcatelProvisioning alcatelService = (AlcatelProvisioning) ctx.get(AlcatelProvisioning.class);
            for (Service service : services)
            {
                try
                {
                    alcatelService.updateAccount(ctx, service, sub);
                }
                catch (Throwable t)
                {
                    final String errorMessage = ALCATEL_UPDATE_ERROR_MEESAGE.get(new MessageMgr(ctx, getClass()), new String[]{sub.getId(), String.valueOf(service.getID()), service.getName()});
                    logException(ctx, new IllegalStateException(errorMessage, t));
                }
            }
        }
        catch (Throwable t)
        {
            // TODO Auto-generated catch block
            logException(ctx, t);
        }
    }


    /**
     * Logs exception to exceptionListner, debug and minor
     * 
     * @param ctx
     * @param t
     */
    protected void logException(Context ctx, Throwable t)
    {
        new MinorLogMsg(this, t.getMessage(), null).log(ctx);
        new DebugLogMsg(this, "Error: ", t).log(ctx);
        final ExceptionListener exceptListner = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (null != exceptListner)
        {
            exceptListner.thrown(t);
        }
    }

    public static AlcatelSSCSubscriberExtension getAlcatelSSCSubscriberExtension(Context ctx, String subscriberId) throws HomeException
    {
        Home home = (Home) ctx.get(AlcatelSSCSubscriberExtensionHome.class);
        return (AlcatelSSCSubscriberExtension) home.find(ctx, new EQ(AlcatelSSCSubscriberExtensionXInfo.SUB_ID, subscriberId));
    }

    protected transient Context ctx_ = null;
    final String ALCATEL_UPDATE_ERROR_TEXT = "Error propagating update from CRM-[Subscriber-Alcatel-Extension] to Alcatel-SSC-[Account] for Subscriber with ID [ {0} ] with corresponding Service [ {1} / {2} ]";
    final String ALCATEL_UPDATE_ERROR_MEESAGE_KEY = "alcatel.subscriber.extension.update.error";
    private final MessageManagerText ALCATEL_UPDATE_ERROR_MEESAGE = new MessageManagerText(
            ALCATEL_UPDATE_ERROR_MEESAGE_KEY, ALCATEL_UPDATE_ERROR_TEXT);
}
