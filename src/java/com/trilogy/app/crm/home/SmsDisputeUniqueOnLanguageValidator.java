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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;


/**
 * 
 */
public final class SmsDisputeUniqueOnLanguageValidator implements Validator
{

    /**
     * Singleton instance.
     */
    private static SmsDisputeUniqueOnLanguageValidator instance;


    /**
     * Prevents initialization
     */
    private SmsDisputeUniqueOnLanguageValidator()
    {
        // empty
    }


    /**
     * 
     * @return
     */
    public static SmsDisputeUniqueOnLanguageValidator instance()
    {
        if (instance == null)
        {
            instance = new SmsDisputeUniqueOnLanguageValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
    	/*if (obj instanceof SmsDisputeNotificationConfig)
        {
            SmsDisputeNotificationConfig config = (SmsDisputeNotificationConfig) obj;
            if (!isLanguageUniqueForSameSpid(ctx, config))
            {
                throw new IllegalStateException((config.getLanguage().equals("") ? "-default-" : config.getLanguage())
                        + " already exists for SPID=" + config.getSpid());
            }
        }*/
    }


    /*private boolean isLanguageUniqueForSameSpid(Context ctx, SmsDisputeNotificationConfig config)
    {
        try
        {
            Home home = (Home) ctx.get(SmsDisputeNotificationConfigHome.class);
            SmsDisputeNotificationConfig existingConfig = (SmsDisputeNotificationConfig) home.find(ctx, new And().add(
                    new EQ(SmsDisputeNotificationConfigXInfo.SPID, Integer.valueOf(config.getSpid()))).add(
                    new EQ(SmsDisputeNotificationConfigXInfo.LANGUAGE, config.getLanguage())));
            if (existingConfig == null)
            {
                return true;
            }
            else
            {
                if (config.getId() == existingConfig.getId())
                {
                    return true;
                }
                return false;
            }
        }
        catch (HomeException he)
        {
            new MajorLogMsg(this, "Failed to look-up SmsDisputeNotificationConfig for SPID " + config.getSpid()
                    + " and Language " + (config.getLanguage().equals("") ? "-default-" : config.getLanguage()), he)
                    .log(ctx);
            return false;
        }
    }*/
}