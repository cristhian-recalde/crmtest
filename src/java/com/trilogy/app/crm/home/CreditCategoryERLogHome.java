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

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.BeanOperationEnum;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.DunningConfigurationEnum;
import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.xhome.home.SimpleBeanERHome;

public class CreditCategoryERLogHome extends SimpleBeanERHome 
{

    public CreditCategoryERLogHome(final Home delegate)
    {
        super(delegate, IDENTIFIER, CLASS, TITLE, FIELDS);
    }

    private static final int IDENTIFIER = 1120;
    private static final int CLASS = 700;
    private static final String TITLE = "Credit Category Management";

    private static final PropertyInfo[] FIELDS =
    {
        CreditCategoryXInfo.SPID,
        CreditCategoryXInfo.CODE,
        CreditCategoryXInfo.DESC,
        CreditCategoryXInfo.FACTOR,
        CreditCategoryXInfo.DUNNING_EXEMPT,
        CreditCategoryXInfo.BILLING_MESSAGE,
        CreditCategoryXInfo.MAX_NUMBER_PTP,
        CreditCategoryXInfo.MAX_PTPINTERVAL,
        CreditCategoryXInfo.DUNNING_CONFIGURATION,
        CreditCategoryXInfo.WARNING_ACTION,
        CreditCategoryXInfo.DUNNING_ACTION,
        CreditCategoryXInfo.IN_ARREARS_ACTION,
        CreditCategoryXInfo.GRACE_DAYS_WARNING,
        CreditCategoryXInfo.GRACE_DAYS_DUNNING,
        CreditCategoryXInfo.GRACE_DAYS_IN_ARREARS,
        CreditCategoryXInfo.MINIMUM_PAYMENT_THRESHOLD,
        CreditCategoryXInfo.MINIMUM_OWING_THRESHOLD,
        CreditCategoryXInfo.AUTO_DEPOSIT_RELEASE_CONFIGURATION,
	    CreditCategoryXInfo.AUTO_DEPOSIT_RELEASE_CRITERIA,
	    CreditCategoryXInfo.MONTHLY_SPEND_LIMIT
    };

    @Override
    protected Object getOriginal(final Context context, final Object object) throws HomeException
    {
        final CreditCategory newBean = (CreditCategory)object;

        final Home home = (Home)context.get(CreditCategoryHome.class);
        
        return home.find(context, Integer.valueOf(newBean.getCode()));
    }

    /**
     * Overwrite the getFieldValues method, so we can properly handle the "ACTION" mapping.
     */
    @Override
    protected String[] getFieldValues(final Context context, final BeanOperationEnum action,
            final Object oldBean, final Object newBean)
    {
        final List<String> values = new ArrayList<String>(fields_.length * 2 + 2);

        //Agent
        values.add(FrameworkSupportHelper.get(context).getCurrentUserID(context));

        //Action
        values.add(String.valueOf(action.getIndex()));

        for (final PropertyInfo info: fields_)
        {
            if (info.equals(CreditCategoryXInfo.WARNING_ACTION) || 
                    info.equals(CreditCategoryXInfo.DUNNING_ACTION) || 
                    info.equals(CreditCategoryXInfo.IN_ARREARS_ACTION) )
            {
                addActionValue(values, oldBean, info);
                addActionValue(values, newBean, info);
            }
            else
            {
                addValue(values, oldBean, info);
                addValue(values, newBean, info);
            }
        }

        return values.toArray(new String[values.size()]);
    }
    
    private void addActionValue(final List<String> values, final Object bean, final PropertyInfo info)
    {
        if (bean != null)
        {
            boolean isCustom = ((CreditCategory) bean).getDunningConfiguration().equals(DunningConfigurationEnum.CUSTOM);
            if (isCustom)
            {
                ProvisionCommand command = (ProvisionCommand) info.get(bean);
                String property  = command.getName();
                if (property.indexOf(",") >= 0)
                {
                    // Safely deal with extra commas
                    // FIXME: this does not treat the \" properly. \" should be doubled when enclosing in "".
                    property = new StringBuilder().append("\"").append(property).append("\"").toString();
                }
                values.add(property);
            }
            else
            {
                values.add("");
            }
        }
        else
        {
            values.add("");
        }
    }

}
