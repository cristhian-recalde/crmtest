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
package com.trilogy.app.crm.support;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.calculator.AccountValueCalculator;
import com.trilogy.app.crm.calculator.CRMSpidValueCalculator;
import com.trilogy.app.crm.calculator.InvoiceValueCalculator;
import com.trilogy.app.crm.calculator.PropertyBasedValueCalculator;
import com.trilogy.app.crm.calculator.PropertyBasedValueCalculatorXInfo;
import com.trilogy.app.crm.calculator.ServiceValueCalculator;
import com.trilogy.app.crm.calculator.SubscriberValueCalculator;
import com.trilogy.app.crm.calculator.ValueCalculator;
import com.trilogy.app.crm.delivery.email.AccountConstantValueCalculator;
import com.trilogy.app.crm.delivery.email.InvoiceConstantValueCalculator;
import com.trilogy.app.crm.delivery.email.PropertyBasedConstantValueCalculator;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CRMEmailKeywordConfigurationSupport extends DefaultEmailKeywordConfigurationSupport
{
    protected static EmailKeywordConfigurationSupport CRM_instance_ = null;
    public static EmailKeywordConfigurationSupport instance()
    {
        if (CRM_instance_ == null)
        {
            CRM_instance_ = new CRMEmailKeywordConfigurationSupport();
        }
        return CRM_instance_;
    }

    protected CRMEmailKeywordConfigurationSupport()
    {
    }
    
    @Override
    public ValueCalculator getNonDeprecatedValueCalculator(ValueCalculator calc)
    {
        ValueCalculator valueCalculator = super.getNonDeprecatedValueCalculator(calc);

        if (valueCalculator instanceof AccountConstantValueCalculator)
        {
            AccountConstantValueCalculator propCalc = (AccountConstantValueCalculator) valueCalculator;
            PropertyBasedConstantValueCalculator newCalc = new PropertyBasedConstantValueCalculator();
            newCalc.setBeanClassName(propCalc.getBeanClassName());
            newCalc.setProperty(propCalc.getProperty()); 
            valueCalculator = newCalc;
        }

        if (valueCalculator instanceof InvoiceConstantValueCalculator)
        {
            InvoiceConstantValueCalculator propCalc = (InvoiceConstantValueCalculator) valueCalculator;
            PropertyBasedConstantValueCalculator newCalc = new PropertyBasedConstantValueCalculator();
            newCalc.setBeanClassName(propCalc.getBeanClassName());
            newCalc.setProperty(propCalc.getProperty()); 
            valueCalculator = newCalc;
        }

        PropertyInfo beanClassNameInfo = null;
        PropertyInfo propertyInfo = null;
        if (valueCalculator instanceof PropertyBasedValueCalculator)
        {
            String className = (String) PropertyBasedValueCalculatorXInfo.BEAN_CLASS_NAME.get(valueCalculator);
            PropertyInfo prop = (PropertyInfo) PropertyBasedValueCalculatorXInfo.PROPERTY.get(valueCalculator);

            Class cls = null;
            try
            {
                cls = Class.forName(className);
            }
            catch (ClassNotFoundException e)
            {
            }
            
            if (CRMSpid.class.isAssignableFrom(cls))
            {
                CRMSpidValueCalculator newCalc = new CRMSpidValueCalculator();
                newCalc.setProperty(prop);   
                valueCalculator = newCalc;
            }
            else if (Invoice.class.isAssignableFrom(cls))
            {
                InvoiceValueCalculator newCalc = new InvoiceValueCalculator();
                newCalc.setProperty(prop);
                valueCalculator = newCalc;
            }
            else if (Account.class.isAssignableFrom(cls))
            {
                AccountValueCalculator newCalc = new AccountValueCalculator();
                newCalc.setProperty(prop); 
                valueCalculator = newCalc;
            }
            else if (Subscriber.class.isAssignableFrom(cls))
            {
                SubscriberValueCalculator newCalc = new SubscriberValueCalculator();
                newCalc.setProperty(prop);
                valueCalculator = newCalc;
            }
            else if (Service.class.isAssignableFrom(cls))
            {
                ServiceValueCalculator newCalc = new ServiceValueCalculator();
                newCalc.setProperty(prop);
                valueCalculator = newCalc;
            }
            else
            {
                PropertyBasedValueCalculator newCalc = new PropertyBasedValueCalculator();
                newCalc.setBeanClassName(className);
                newCalc.setProperty(prop);
                valueCalculator = newCalc;
            }
        
        }

        return valueCalculator;
    }
}
