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
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHelpWebControl;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.config.AccountRequiredField;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;


/**
 * This help web control only renders account fields that are configured for registration.
 * 
 * It also performs fromWeb on the given account applying the changes available.  If no
 * account is provided to the method, then it will check the database and the context
 * for the existing account to use as a starting point.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class AccountRegistrationHelpWebControl extends AccountHelpWebControl
{
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object bean)
    {
        Context sCtx = ctx.createSubContext();
        
        if (!(bean instanceof Account))
        {
            bean = BeanLoaderSupportHelper.get(sCtx).getBean(sCtx, Account.class);
        }
         
        if (bean instanceof Account)
        {
            Account account = (Account) bean;
            
            Map<String, AccountRequiredField> fields = account.getRegistrationFields(sCtx);
            for (PropertyInfo property : (List<PropertyInfo>) AccountXInfo.PROPERTIES)
            {
                if (property != null
                        && !AccountXInfo.BAN.equals(property)
                        && !fields.containsKey(property.getName()))
                {
                    setMode(sCtx, property, ViewModeEnum.NONE);
                }
            }
        }
        
        super.toWeb(sCtx, out, name, bean);
    }
}
