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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.MissingRequireValueException;
import com.trilogy.framework.xhome.beans.NullValueException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.support.LicensingSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class VoicemailServiceValidator implements Validator
{
    protected static Validator instance_ = null;
    public static Validator instance()
    {
        if (instance_ == null)
        {
            instance_ = new VoicemailServiceValidator();
        }
        return instance_;
    }
    
    protected VoicemailServiceValidator()
    {
    }
    
    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY)) 
        {   
            return;
        } 
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        if (obj instanceof Service)
        {
            Service svc = (Service) obj;
            if (svc.getType() == ServiceTypeEnum.VOICEMAIL)
            {
                String vmPlanId = svc.getVmPlanId();
                if (vmPlanId == null)
                {
                    cise.thrown(new NullValueException(ServiceXInfo.VM_PLAN_ID));
                }
                else if (vmPlanId.trim().length() == 0)
                {
                    cise.thrown(new MissingRequireValueException(ServiceXInfo.VM_PLAN_ID));
                }
            }
        }
        else if (obj instanceof SubscriberServices)
        {
            SubscriberServices svc = (SubscriberServices) obj;
            validate(ctx, svc.getService(ctx));
        }
        
        cise.throwAll();
    }
}
