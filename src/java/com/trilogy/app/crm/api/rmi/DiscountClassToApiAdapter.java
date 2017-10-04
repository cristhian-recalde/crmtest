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
package com.trilogy.app.crm.api.rmi;

import java.math.BigDecimal;

import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.DiscountClass;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class DiscountClassToApiAdapter implements Adapter
{

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        DiscountClass apiDiscountClass = null;
        if (obj instanceof com.redknee.app.crm.bean.DiscountClass)
        {
            com.redknee.app.crm.bean.DiscountClass crmDiscountClass = (com.redknee.app.crm.bean.DiscountClass) obj;
            apiDiscountClass = new DiscountClass();
            DiscountClassToApiReferenceAdapter.adaptDiscountClassToReference(ctx, crmDiscountClass, apiDiscountClass);
            apiDiscountClass.setAdjustmentTypeID(Long.valueOf(crmDiscountClass.getAdjustmentType()));
            apiDiscountClass.setDiscountPercentage(BigDecimal.valueOf(crmDiscountClass.getDiscountPercentage()));
            apiDiscountClass.setUseThreshold(crmDiscountClass.isEnableThreshold());
            if (crmDiscountClass.isEnableThreshold())
            {
                apiDiscountClass.setThresholdAmount(crmDiscountClass.getMinimumTotalChargeThreshold());
            }
        }
        return apiDiscountClass;
    }


    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static com.redknee.app.crm.bean.DiscountClass adaptApiToDiscountClass(Context ctx,
            DiscountClass apiDiscountClass)
    {
        com.redknee.app.crm.bean.DiscountClass crmDiscountClass = null;
        try
        {
            crmDiscountClass = (com.redknee.app.crm.bean.DiscountClass) XBeans.instantiate(
                    com.redknee.app.crm.bean.DiscountClass.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(DiscountClassToApiAdapter.class,
                    "Error instantiating new DiscountClass.  Using default constructor.", e).log(ctx);
            crmDiscountClass = new com.redknee.app.crm.bean.DiscountClass();
        }
        adaptApiToDiscountClass(ctx, apiDiscountClass, crmDiscountClass);
        return crmDiscountClass;
    }


    public static com.redknee.app.crm.bean.DiscountClass adaptApiToDiscountClass(Context ctx,
            DiscountClass apiDiscountClass, com.redknee.app.crm.bean.DiscountClass crmDiscountClass)
    {
        if (apiDiscountClass.getUseThreshold() != null)
        {
            crmDiscountClass.setEnableThreshold(apiDiscountClass.getUseThreshold());
        }
        if (crmDiscountClass.isEnableThreshold() && apiDiscountClass.getThresholdAmount() != null)
        {
            crmDiscountClass.setMinimumTotalChargeThreshold(apiDiscountClass.getThresholdAmount());
        }
        if (apiDiscountClass.getName() != null)
        {
            crmDiscountClass.setName(apiDiscountClass.getName());
        }
        if (apiDiscountClass.getDiscountPercentage() != null)
        {
            crmDiscountClass.setDiscountPercentage(apiDiscountClass.getDiscountPercentage().doubleValue());
        }
        if (apiDiscountClass.getSpid() != null)
        {
            crmDiscountClass.setSpid(apiDiscountClass.getSpid());
        }        
        return crmDiscountClass;
    }
    
    
    public static com.redknee.app.crm.bean.DiscountClass adaptGenericParametersToCreateDiscountClass(
            GenericParameter[] apiGenericParameter, com.redknee.app.crm.bean.DiscountClass crmDiscountClass)
    {
        Object obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_ADJUSTMENTTYPEDESCRIPTION,
                apiGenericParameter);
        if (obj != null)
        {
            crmDiscountClass.setAdjustmentTypeDescription((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_GLCODE, apiGenericParameter);
        if (obj != null)
        {
            crmDiscountClass.setGLCode((String) obj);
        }
        obj = RmiApiSupport
                .getGenericParameterValue(Constants.GENERICPARAMETER_INVOICEDESCRIPTION, apiGenericParameter);
        if (obj != null)
        {
            crmDiscountClass.setInvoiceDescription((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_TAXAUTHORITY, apiGenericParameter);
        if (obj != null)
        {
            crmDiscountClass.setTaxAuthority(((Long) obj).intValue());
        }
        return crmDiscountClass;
    }
}
