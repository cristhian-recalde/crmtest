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
package com.trilogy.app.crm.factory;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.config.AccountRequiredFieldConfig;


/**
 * Bean Factory that automatically sets the system type and registration only state of the new bean.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class AccountRequiredFieldConfigFactory extends ConstructorCallingBeanFactory<AccountRequiredFieldConfig>
{
    public AccountRequiredFieldConfigFactory()
    {
        this(AccountRequiredFieldConfig.DEFAULT_SYSTEMTYPE, AccountRequiredFieldConfig.DEFAULT_REGISTRATIONONLY);
    }

    public AccountRequiredFieldConfigFactory(SubscriberTypeEnum systemType, boolean registrationOnly)
    {
        super(AccountRequiredFieldConfig.class);
        systemType_ = systemType;
        registrationOnly_ = registrationOnly;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(Context ctx)
    {
        Object bean = super.create(ctx);
        if (bean instanceof AccountRequiredFieldConfig)
        {
            AccountRequiredFieldConfig config = (AccountRequiredFieldConfig) bean;
            config.setSystemType(systemType_);
            config.setRegistrationOnly(registrationOnly_);
        }
        return bean;
    }

    private SubscriberTypeEnum systemType_;
    private boolean registrationOnly_;
}
