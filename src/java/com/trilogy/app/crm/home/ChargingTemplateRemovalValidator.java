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
package com.trilogy.app.crm.home;

import java.util.Collection;

import com.trilogy.app.crm.bean.ui.ChargingTemplate;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionXInfo;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterChargingTemplate;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterChargingTemplateHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterChargingTemplateXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Validates whether or not a charging template can be removed.
 * @author Marcio Marques
 * @since 8.5
 */
public class ChargingTemplateRemovalValidator implements Validator
{
    protected static Validator instance_ = null;

    private ChargingTemplateRemovalValidator()
    {
    }

    public static Validator instance()
    {
        if (instance_ == null)
        {
            instance_ = new ChargingTemplateRemovalValidator();
        }

        return instance_;
    }

    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        final long chargingTemplateId;
        final String name;
        final String description;
        if (obj instanceof com.redknee.app.crm.bean.ChargingTemplate)
        {
            chargingTemplateId = ((com.redknee.app.crm.bean.ChargingTemplate) obj).getIdentifier();
            name = ((com.redknee.app.crm.bean.ChargingTemplate) obj).getName();
        }
        else
        {
            chargingTemplateId = ((ChargingTemplate) obj).getIdentifier();
            name = ((ChargingTemplate) obj).getName();
        }
        description = chargingTemplateId + " - " + name;
        try
        {
            Home ppsmSupporterChargingTemplateHome = (Home) ctx.get(PPSMSupporterChargingTemplateHome.class);
            Home ppsmSupporteeSubExtensionHome = (Home) ctx.get(PPSMSupporteeSubExtensionHome.class);
            Collection<PPSMSupporteeSubExtension> supportees = (Collection<PPSMSupporteeSubExtension>) ppsmSupporteeSubExtensionHome
                    .where(ctx,
                            new EQ(PPSMSupporteeSubExtensionXInfo.CHARGING_TEMPLATE, Long.valueOf(chargingTemplateId)))
                    .selectAll(ctx);
            if (supportees.size() > 0)
            {
                cise.thrown(new IllegalStateException("Charging Template '" + description + "' is currently in use by "
                        + supportees.size() + " PPSM supportee subscribers and cannot be removed."));
            }
            else
            {
                Collection<PPSMSupporterChargingTemplate> supporters = (Collection<PPSMSupporterChargingTemplate>) ppsmSupporterChargingTemplateHome
                        .where(ctx,
                                new EQ(PPSMSupporterChargingTemplateXInfo.IDENTIFIER, Long.valueOf(chargingTemplateId)))
                        .selectAll(ctx);
                if (supporters.size() > 0)
                {
                    cise.thrown(new IllegalStateException("Charging Template '" + description
                            + "' is currently in use by " + supporters.size()
                            + " PPSM supporter subscribers and cannot be removed."));
                }
            }
        }
        catch (HomeException e)
        {
            String msg = "Unable to check if Charging Template '" + description + "' is currently in use due to exception: " + e.getMessage();
            LogSupport.minor(ctx, this, msg, e);
            cise.thrown(new IllegalStateException(msg));
        }
        cise.throwAll();
        
        
    }
    
}
