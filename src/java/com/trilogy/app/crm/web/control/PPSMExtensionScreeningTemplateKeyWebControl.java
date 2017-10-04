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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.ChargingTemplateHome;
import com.trilogy.app.crm.bean.ChargingTemplateXInfo;
import com.trilogy.app.crm.bean.ScreeningTemplateHome;
import com.trilogy.app.crm.bean.ScreeningTemplateKeyWebControl;
import com.trilogy.app.crm.bean.ScreeningTemplateXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.IsNull;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Key web control used to show PPSM Extension Screening templates on the supportee
 * screen based on the given supporter MSISDN.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class PPSMExtensionScreeningTemplateKeyWebControl  extends ScreeningTemplateKeyWebControl
{
    public PPSMExtensionScreeningTemplateKeyWebControl()
    {
        init();
    }

    public PPSMExtensionScreeningTemplateKeyWebControl(boolean autoPreview)
    {
        super(autoPreview);
        init();
    }

    public PPSMExtensionScreeningTemplateKeyWebControl(int listSize)
    {
        super(listSize);
        init();
    }

    public PPSMExtensionScreeningTemplateKeyWebControl(int listSize, boolean autoPreview)
    {
        super(listSize, autoPreview);
        init();
    }

    public PPSMExtensionScreeningTemplateKeyWebControl(int listSize, boolean autoPreview, Class baseClass,String sourceField,String targetField)
    {
        super(listSize, autoPreview, baseClass,sourceField,targetField);
        init();
    }

    public PPSMExtensionScreeningTemplateKeyWebControl(int listSize, boolean autoPreview, boolean isOptional)
    {
        super(listSize, autoPreview, isOptional);
        init();
    }

    public PPSMExtensionScreeningTemplateKeyWebControl(int listSize, boolean autoPreview, boolean isOptional, boolean allowCustom)
    {
        super(listSize, autoPreview, isOptional, allowCustom);
        init();
    }

    public PPSMExtensionScreeningTemplateKeyWebControl(int listSize, boolean autoPreview, Object optionalValue)
    {
        super(listSize, autoPreview, optionalValue);
        init();
    }

    public PPSMExtensionScreeningTemplateKeyWebControl(int listSize, boolean autoPreview, Object optionalValue, Class baseClass,String sourceField,String targetField)
    {
        super(listSize, autoPreview, optionalValue,baseClass,sourceField,targetField);
        init();
    }

    public PPSMExtensionScreeningTemplateKeyWebControl(int listSize, boolean autoPreview, Object optionalValue, boolean allowCustom)
    {
        super(listSize, autoPreview, optionalValue, allowCustom);
        init();
    }

    
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Context subContext = filterHome(ctx);
        super.toWeb(subContext, out, name, obj);
    }

    public Context filterHome(Context context)
    {
        Context subCtx = context.createSubContext();
        Object obj = subCtx.get(AbstractWebControl.BEAN);
        Subscriber supportedSubscriber = (Subscriber) ExtensionSupportHelper.get(subCtx).getParentBean(subCtx);
        
        if (obj instanceof PPSMSupporteeSubExtension)
        {
            PPSMSupporteeSubExtension extension = (PPSMSupporteeSubExtension) obj;

            if (supportedSubscriber==null || (extension.getSubId()!=null && !extension.getSubId().isEmpty() && !supportedSubscriber.getId().equals(extension.getSubId())))
            {
                try
                {
                    supportedSubscriber = SubscriberSupport.getSubscriber(context, extension.getSubId());
                }
                catch (HomeException e)
                {
                    LogSupport.minor(context, this, "Error retrieving supported subscriber for PPSM Supportee '" + extension.getSubId() + "': " + e.getMessage(), e);
                }
            }

            String msisdn = extension.getSupportMSISDN();
            Set<Long> identifiers = new HashSet<Long>();
            subCtx = subCtx.createSubContext();

            Subscriber subscriber = null;
            if (msisdn!=null && !msisdn.trim().isEmpty())
            {
                try
                {
                    subscriber = SubscriberSupport.lookupSubscriberForMSISDN(subCtx, msisdn);
                }
                catch (HomeException e)
                {
                    LogSupport.minor(subCtx, this, "Error retrieving subscriber for support MSISDN '" + msisdn + "': " + e.getMessage(), e);
                }
                
                if (subscriber!=null)
                {
                    PPSMSupporterSubExtension supporterExtension = null;
                    
                    try
                    {
                        supporterExtension = PPSMSupporterSubExtension.getPPSMSupporterSubscriberExtension(subCtx, subscriber.getId());
                    
                    }
                    catch (HomeException e)
                    {
                        LogSupport.minor(subCtx, this, "Error retrieving subscriber extension for support MSISDN '" + msisdn + "': " + e.getMessage(), e);
                    }
                    
                    if (supporterExtension != null)
                    {
                        // Filling in charging template IN home
                        for (String templateId : supporterExtension.getScreeningTemplates(subCtx))
                        {
                            identifiers.add(Long.valueOf(templateId));
                        }
                    }
                }
            }

            Home screeningTemplateHome = (Home) subCtx.get(ScreeningTemplateHome.class);
            And and = new And();
            and.add(new In(ScreeningTemplateXInfo.IDENTIFIER, identifiers));
            if (supportedSubscriber!=null)
            {
                and.add(new EQ(ScreeningTemplateXInfo.SPID, Integer.valueOf(supportedSubscriber.getSpid())));
            }
            
            screeningTemplateHome = screeningTemplateHome.where(subCtx, and);
            subCtx.put(ScreeningTemplateHome.class, screeningTemplateHome);
            

        }
        return subCtx;
        
    }
    
    
    
    
}
