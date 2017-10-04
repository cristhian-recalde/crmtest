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
package com.trilogy.app.crm.home.sub.extension;

import com.trilogy.app.crm.extension.subscriber.PPSMSupporterChargingTemplate;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterChargingTemplateHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterChargingTemplateXInfo;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterScreenTemplate;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterScreenTemplateHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterScreenTemplateXInfo;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This home is responsible for saving the PPSMSupporterSubscriberExtension charging templates
 * and screening templates in the database tables used by them.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class PPSMSupporterSubscriberExtensionTemplatesMappingSavingHome  extends HomeProxy
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PPSMSupporterSubscriberExtensionTemplatesMappingSavingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException
    {
        Object ret = getDelegate().create(ctx, obj);
        Home chargingTemplateHome = (Home) ctx.get(PPSMSupporterChargingTemplateHome.class);
        Home screeningTemplateHome = (Home) ctx.get(PPSMSupporterScreenTemplateHome.class);
        PPSMSupporterSubExtension extension = (PPSMSupporterSubExtension) obj;
        extension.setSubId(((PPSMSupporterSubExtension) ret).getSubId());
        
        for (Object template : extension.getChargingTemplates(ctx))
        {
            Long chargingTemplate;
            if (template instanceof String)
            {
                chargingTemplate = Long.valueOf((String) template);
            }
            else
            {
                chargingTemplate = (Long) template;
            }
            PPSMSupporterChargingTemplate mapping = new PPSMSupporterChargingTemplate();
            mapping.setIdentifier(chargingTemplate);
            mapping.setSubId(extension.getSubId());
            chargingTemplateHome.create(ctx, mapping);
        }

        for (Object template : extension.getScreeningTemplates(ctx))
        {
            Long screeningTemplate;
            if (template instanceof String)
            {
                screeningTemplate = Long.valueOf((String) template);
            }
            else
            {
                screeningTemplate = (Long) template;
            }
            PPSMSupporterScreenTemplate mapping = new PPSMSupporterScreenTemplate();
            mapping.setIdentifier(screeningTemplate);
            mapping.setSubId(extension.getSubId());
            screeningTemplateHome.create(ctx, mapping);
        }
        
        extension.resetSavedTemplates(ctx);

        return extension;
    }
    
    public Object store(Context ctx, Object obj) throws HomeException
    {
        getDelegate().store(ctx, obj);
        Home chargingTemplateHome = (Home) ctx.get(PPSMSupporterChargingTemplateHome.class);
        Home screeningTemplateHome = (Home) ctx.get(PPSMSupporterScreenTemplateHome.class);
        PPSMSupporterSubExtension extension = (PPSMSupporterSubExtension) obj;
        

        for (Long chargingTemplate : extension.getAddedChargingTemplates(ctx))
        {
            PPSMSupporterChargingTemplate mapping = new PPSMSupporterChargingTemplate();
            mapping.setIdentifier(chargingTemplate);
            mapping.setSubId(extension.getSubId());
            chargingTemplateHome.create(ctx, mapping);
        }

        for (Long screeningTemplate : extension.getAddedScreeningTemplates(ctx))
        {
            PPSMSupporterScreenTemplate mapping = new PPSMSupporterScreenTemplate();
            mapping.setIdentifier(screeningTemplate);
            mapping.setSubId(extension.getSubId());
            screeningTemplateHome.create(ctx, mapping);
        }

        for (Long chargingTemplate : extension.getRemovedChargingTemplates(ctx))
        {
            PPSMSupporterChargingTemplate mapping = new PPSMSupporterChargingTemplate();
            mapping.setIdentifier(chargingTemplate);
            mapping.setSubId(extension.getSubId());
            chargingTemplateHome.remove(ctx, mapping);
        }

        for (Long screeningTemplate : extension.getRemovedScreeningTemplates(ctx))
        {
            PPSMSupporterScreenTemplate mapping = new PPSMSupporterScreenTemplate();
            mapping.setIdentifier(screeningTemplate);
            mapping.setSubId(extension.getSubId());
            screeningTemplateHome.remove(ctx, mapping);
        }
        
        extension.resetSavedTemplates(ctx);
        return extension;
    }    
    
    public void remove(Context ctx, Object obj) throws HomeException
    {
        final Home chargingTemplateHome = (Home) ctx.get(PPSMSupporterChargingTemplateHome.class);
        final Home screeningTemplateHome = (Home) ctx.get(PPSMSupporterScreenTemplateHome.class);
        PPSMSupporterSubExtension extension = (PPSMSupporterSubExtension) obj;
 
        chargingTemplateHome.where(ctx, new EQ(PPSMSupporterChargingTemplateXInfo.SUB_ID, extension.getSubId())).forEach(ctx,
                new Visitor()
                {

                    @Override
                    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                    {
                        PPSMSupporterChargingTemplate mapping = (PPSMSupporterChargingTemplate) obj;
                        try
                        {
                            chargingTemplateHome.remove(obj);
                        }
                        catch (HomeException e)
                        {
                            LogSupport.minor(ctx, this,
                                    "Unable to remove PPSMSupporterSubscriberExtension -> Charging Template mapping. SubscriberId="
                                            + mapping.getSubId() + ", ChargingTemplateId=" + mapping.getIdentifier()
                                            + ": " + e.getMessage(), e);
                        }
                    }
                });
 
        screeningTemplateHome.where(ctx, new EQ(PPSMSupporterScreenTemplateXInfo.SUB_ID, extension.getSubId())).forEach(ctx,
                new Visitor()
                {

                    @Override
                    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                    {
                        PPSMSupporterScreenTemplate mapping = (PPSMSupporterScreenTemplate) obj;
                        try
                        {
                            screeningTemplateHome.remove(obj);
                        }
                        catch (HomeException e)
                        {
                            LogSupport.minor(ctx, this,
                                    "Unable to remove PPSMSupporterSubscriberExtension -> Screening Template mapping. SubscriberId="
                                            + mapping.getSubId() + ", ScreeningTemplateId=" + mapping.getIdentifier()
                                            + ": " + e.getMessage(), e);
                        }
                    }
                });
        extension.resetSavedTemplates(ctx);
        super.remove(ctx, obj);
    }
}