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
package com.trilogy.app.crm.client.alcatel;

import java.util.Collection;
import java.util.Map;

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.state.FinalStateAware;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.util.MessageManagerText;


/**
 * 
 * @author simar.singh@rdknee.com
 * 
 *         A generic class that handles propagation of updates on Alcatel related
 *         properties of a General-bean to Alcatel-System
 * 
 * @param <BEAN>
 */
public class AlcatelUpdateHome<BEAN extends AbstractBean & Identifiable> extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public AlcatelUpdateHome(Context ctx, AlcatelFunctions.AlcateBeanFunction<BEAN> beanFunction, Home delegate)
    {
        super(ctx, delegate);
        this.beanFunction_ = beanFunction;
    }


    /**
     * As required; propagate the updates on Alcatel related properties to Alcatel-System
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        // TODO Auto-generated method stub
        BEAN existingBean = getExistingBean(ctx, (BEAN) obj);
        BEAN newBean = (BEAN) getDelegate(ctx).store(ctx, obj);
        if (obj instanceof FinalStateAware && ((FinalStateAware) obj).isInFinalState())
        {
            onFinalization(ctx, newBean);
        }
        else
        {
            onUpdate(ctx, newBean, existingBean);
        }
        return newBean;
    }

    /**
     * Given the bean, find it's exising bean in the store
     * The method uses home.find(ctx , bean.ID()) to get the existing bean
     * If you have a cheaper way of fetching the it for your bean, override it. 
     * @param ctx
     * @param newBean
     * @return
     * @throws HomeException
     */
    protected BEAN getExistingBean(Context ctx, BEAN newBean) throws HomeException
    {
        return (BEAN)HomeSupportHelper.get(ctx).findBean(ctx, newBean.getClass(), newBean.ID());
    }

    /**
     * In general, if the bean on interest is in StateAwaer and is in final state, we got
     * nothing to update This behaviour may be suitable overidden. For example, in cose of
     * Subscriber, we will remove the Alcatel Services from Alcatel System on Subscriber's
     * finalization
     * 
     * @param ctx
     * @param obj
     */
    protected void onFinalization(Context ctx, BEAN obj)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Do nothing as the ENTITY of type [" + obj.getClass().getSimpleName() + "] "
                    + "with ID [" + obj.ID() + "] is in Final Sate. No updates to external system are necessary", null)
                    .log(ctx);
        }
    }


    /**
     * Propagate the updates on Alcatel related properties to Alcatel-System
     * 
     * @param ctx
     * @param newBean
     */
    protected void onUpdate(Context ctx, BEAN newBean, BEAN existingBean)
    {
        final Map<KeyConfiguration, PropertyInfo> keyValueParis = beanFunction_.getKeyValue(ctx, newBean);
        if (!keyValueParis.isEmpty())
        {
            // if there any alcatel related properties in the bean.
            try
            {
                // if there alcatel related values in the bean
                for (PropertyInfo propertyInfo : keyValueParis.values())
                {
                    if (!propertyInfo.f(ctx, newBean).equals(propertyInfo.f(ctx, existingBean)))
                    {
                        // if any value bean value has changed, send an update
                        alcatelUpdate(ctx, beanFunction_.getSubscriberServices(ctx, newBean));
                        return;
                    }
                }
            }
            catch (Throwable t)
            {
                final String errorMessage = "Could not propagate update to Alcatel-SSC-System update for Properties ["
                        + String.valueOf(keyValueParis) + "]. Error [" + t.getMessage() + "]";
                // TODO Auto-generated catch block
                logException(ctx, new IllegalStateException(errorMessage, t));
            }
        }
    }

    /**
     * Propagate update for changes to Alcatel System.
     * @param ctx
     * @param subscriberServices
     */
    protected void alcatelUpdate(Context ctx, Collection<SubscriberServices> subscriberServices)
    {
        AlcatelProvisioning alcatelService = (AlcatelProvisioning) ctx.get(AlcatelProvisioning.class);
        Home subscriberHome = (Home) ctx.get(SubscriberHome.class);
        Home servicesHome = (Home) ctx.get(ServiceHome.class);
        for (SubscriberServices subscriberService : subscriberServices)
        {
            try
            {
                final Subscriber sub = (Subscriber) subscriberHome.find(ctx, subscriberService.getSubscriberId());
                final Service service = (Service) servicesHome.find(ctx, subscriberService.getServiceId());
                if (null == sub)
                {
                    logException(ctx, new IllegalStateException("Could not find Subscriber with ID ["
                            + subscriberService.getSubscriberId() + "]"));
                }
                if (null == service)
                {
                    logException(ctx, new IllegalStateException("Could not find Service with ID ["
                            + subscriberService.getServiceId() + "]"));
                }
                alcatelService.updateAccount(ctx, service, sub);
            }
            catch (Throwable t)
            {
                final String[] messageParams = new String[]{subscriberService.getSubscriberId(),String.valueOf(subscriberService.getServiceId())};
                final String errorMessage = ALCATEL_UPDATE_ERROR_MEESAGE.get(new MessageMgr(ctx, this.getClass()), messageParams); 
                logException(ctx, new IllegalStateException(errorMessage, t));
            }
        }
    }


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
    final private AlcatelFunctions.AlcateBeanFunction<BEAN> beanFunction_;
    
    final String ALCATEL_UPDATE_ERROR_TEXT = "Error propagating update on CRM[Subscriber-Account] to External[Alcatel-SSC] system for Subscriber with ID [ {0} ] with corresponding Service ID [ {1} ]";
    final String ALCATEL_UPDATE_ERROR_MEESAGE_KEY = "alcatel.account.update.error";
    private final MessageManagerText ALCATEL_UPDATE_ERROR_MEESAGE = new MessageManagerText(ALCATEL_UPDATE_ERROR_MEESAGE_KEY, ALCATEL_UPDATE_ERROR_TEXT);
}
