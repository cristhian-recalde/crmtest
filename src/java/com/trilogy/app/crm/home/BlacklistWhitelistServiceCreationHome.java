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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;

import com.trilogy.app.crm.bean.BlacklistWhitelistTemplate;
import com.trilogy.app.crm.bean.BlacklistWhitelistTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.ui.AbstractService;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;

/**
 * @author chandrachud.ingale
 * @since 9.6
 */
public class BlacklistWhitelistServiceCreationHome extends HomeProxy
{
    private static final long   serialVersionUID           = 8936127275798578600L;
    private static final String MODULE            = BlacklistWhitelistServiceCreationHome.class.getName();
    private static final String CREATE_METHOD_LOG_CONSTANT = MODULE + " (create) ";


    public BlacklistWhitelistServiceCreationHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    public Object create(final Context ctx, final Object arg) throws HomeException
    {
        final BlacklistWhitelistTemplate blwlTemplate = (BlacklistWhitelistTemplate) super.create(ctx, arg);

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx,this, CREATE_METHOD_LOG_CONSTANT + arg);
        }

        if (blwlTemplate != null)
        {
            Service service;
            try
            {
                service = (Service) XBeans.instantiate(Service.class, ctx);
            }
            catch (Throwable t)
            {
                service = new Service();
                new MinorLogMsg(this, CREATE_METHOD_LOG_CONSTANT + "XBeans can't instantiate Service", t).log(ctx);
            }

            service.setName(blwlTemplate.getName());
            service.setSpid(blwlTemplate.getSpid());
            service.setType(ServiceTypeEnum.GENERIC);

            Collection<Extension> extensions = new ArrayList<Extension>();

            BlacklistWhitelistTemplateServiceExtension extension = new BlacklistWhitelistTemplateServiceExtension();
            extension.setServiceId(service.getIdentifier());
            extension.setSpid(service.getSpid());
            extension.setCallingGroupId(blwlTemplate.getIdentifier());
            extension.setGlCode(blwlTemplate.getGLCode());
            if (blwlTemplate.getType() == BlacklistWhitelistTypeEnum.BLACKLIST)
            {
                extension.setCallingGroupType(CallingGroupTypeEnum.BL);
            }
            else
            {
                extension.setCallingGroupType(CallingGroupTypeEnum.WL);
            }
            extensions.add(extension);

            service.setServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));

            service.setSmartSuspension(true);
            service.setSubscriptionType(AbstractService.DEFAULT_SUBSCRIPTIONTYPE);
            service.setChargeScheme(blwlTemplate.getChargeScheme());
            service.setAdjustmentTypeDesc("Blacklist Whitelist PLP - " + blwlTemplate.getName());
            service.setAdjustmentGLCode(blwlTemplate.getGLCode());
            service.setTechnology(blwlTemplate.getTechnology());

            final Home home = (Home) ctx.get(ServiceHome.class);
            Exception exception = null;
            try
            {
                if (home != null)
                {
                    service = (Service) home.create(ctx, service);
                    if (service != null)
                    {
                        blwlTemplate.setServiceReferencedIn(service.getIdentifier());

                        CallingGroupERLogMsg.generateBlWlCreationER(blwlTemplate,
                                CallingGroupERLogMsg.SUCCESS_RESULT_CODE, ctx);
                    }
                }
            }
            catch (HomeException e)
            {
                exception = e;
            }
            finally
            {
                if (home == null || service == null || exception != null)
                {
                    String reason = exception != null ? (" Reason : " + exception.getLocalizedMessage()) : "";
                    String msg = "Can't create service associated with blacklist whitelist template with ID : "
                            + blwlTemplate.getIdentifier() + reason;

                    new SeverityLogMsg(SeverityEnum.MAJOR, this.getClass().getName(), msg, exception).log(ctx);

                    CallingGroupERLogMsg.generateBlWlCreationER(blwlTemplate,
                            CallingGroupERLogMsg.ERROR_PROVISIONING_SERVICE, ctx);
                    
                    throw new HomeException(msg);
                }
            }
        }

        return blwlTemplate;
    }


    public void remove(final Context ctx, final Object arg) throws HomeException
    {
         // edit & delete restricted
    }

    /**
     * {@inheritDoc}
     */
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        throw new HomeException("Cannot modify Blacklist/Whitelist template.", new IllegalStateException("Cannot modify Blacklist/Whitelist template."));
    }
}
