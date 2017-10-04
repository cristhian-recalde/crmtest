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

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.BlacklistWhitelistTemplate;
import com.trilogy.app.crm.bean.BlacklistWhitelistTemplateHome;
import com.trilogy.app.crm.bean.BlacklistWhitelistTemplateXInfo;
import com.trilogy.app.crm.bean.BlacklistWhitelistTypeEnum;

/**
 * @author chandrachud.ingale
 * @since 9.6
 */
public class BlacklistWhitelistTemplateCreateValidator implements Validator
{
    private static final BlacklistWhitelistTemplateCreateValidator instance = new BlacklistWhitelistTemplateCreateValidator();


    private BlacklistWhitelistTemplateCreateValidator()
    {}


    public static BlacklistWhitelistTemplateCreateValidator getInstance()
    {
        return instance;
    }


    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final BlacklistWhitelistTemplate blwlTemplate = (BlacklistWhitelistTemplate) obj;


        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx,this, BlacklistWhitelistTemplateCreateValidator.class + " validate() for blwltemplate : " + blwlTemplate.getName());
        }
        
        final CompoundIllegalStateException exception = new CompoundIllegalStateException();

        if (blwlTemplate.getName() == null || blwlTemplate.getName().equals(""))
        {
            exception.thrown(new IllegalPropertyArgumentException("Name", "Name cannot be empty."));
        }

        if (!(blwlTemplate.getType().getIndex() == BlacklistWhitelistTypeEnum.BLACKLIST_INDEX
                || blwlTemplate.getType().getIndex() == BlacklistWhitelistTypeEnum.WHITELIST_INDEX))
        {
            exception.thrown(new IllegalPropertyArgumentException("Type", "Type does not match Blacklist / Whitelist."));
        }
        
        if (blwlTemplate.getMaxSubscribersAllowed() <= 0)
        {
            exception.thrown(new IllegalPropertyArgumentException("Maximum Subscribers", "Invalid value for max subscribers."));
        }

        if (blwlTemplate.getGLCode() == null || blwlTemplate.getGLCode().equals(""))
        {
            exception.thrown(new IllegalPropertyArgumentException("GLCode", "GLCode cannot be empty."));
        }
        exception.throwAll();

        Home home = (Home) ctx.get(BlacklistWhitelistTemplateHome.class);
        try
        {
            Collection<BlacklistWhitelistTemplate> storedBlwlTemplates = home.select(ctx, new EQ(
                    BlacklistWhitelistTemplateXInfo.SPID, blwlTemplate.getSpid()));

            if (storedBlwlTemplates != null)
            {
                Iterator<BlacklistWhitelistTemplate> iterator = storedBlwlTemplates.iterator();
                while (iterator.hasNext())
                {
                    BlacklistWhitelistTemplate template = iterator.next();
                    if (template.getType() == blwlTemplate.getType())
                    {
                        if (blwlTemplate.getType().getIndex() == BlacklistWhitelistTypeEnum.BLACKLIST_INDEX)
                        {
                            throw new IllegalStateException("Blacklist template aready exists for SPID with ID : "
                                    + template.getIdentifier());
                        }
                        else if (blwlTemplate.getType().getIndex() == BlacklistWhitelistTypeEnum.WHITELIST_INDEX)
                        {
                            throw new IllegalStateException("Whitelist template aready exists for SPID with ID : "
                                    + template.getIdentifier());
                        }
                    }
                }
            }
        }
        catch (HomeException e)
        {
            throw new IllegalStateException(
                    "Cannot validate Blacklist/Whitelist template creation. Exception occured ", e);
        }

    }

}
