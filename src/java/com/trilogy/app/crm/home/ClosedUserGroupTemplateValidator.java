/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.CallingGroupServiceTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.DiscountTypeEnum;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;


/**
 * This class validates some user input for Closed User Groups template.
 *
 * @author jimmy.ng@redknee.com
 */
public class ClosedUserGroupTemplateValidator
    implements Validator
{
    /**
     * Creates a new ClosedUserGroupValidator.
     *
     * @param ctx The operating context.
     */
    public ClosedUserGroupTemplateValidator()
    {
    }

    /**
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void validate(Context ctx,Object obj)
        throws IllegalStateException
    {
        final ClosedUserGroupTemplate cugTemplate = (ClosedUserGroupTemplate) obj;
    	
		final CompoundIllegalStateException exception = 
			new CompoundIllegalStateException();
			
        // Make sure the CUG Name is not blank.
        final String cugName = cugTemplate.getName().trim();
        if (cugName.length() == 0)
        {
            exception.thrown(new IllegalPropertyArgumentException(
                "CUG Name",
                "Empty name is not allowed"));        
        }
        
        if (cugTemplate.getCugServiceType().equals(CallingGroupServiceTypeEnum.ALL) || cugTemplate.getCugServiceType().equals(CallingGroupServiceTypeEnum.VOICE))
        {
            validateVoiceFields(cugTemplate, exception);
        }

        if (cugTemplate.getCugServiceType().equals(CallingGroupServiceTypeEnum.ALL) || cugTemplate.getCugServiceType().equals(CallingGroupServiceTypeEnum.SMS))
        {
            validateSmsFields(cugTemplate, exception);
        }

		exception.throwAll();
    }
    
    
    private void validateSmsFields(ClosedUserGroupTemplate cug, CompoundIllegalStateException exception)
    {
        if (cug.getSmsDiscountType() == DiscountTypeEnum.DISCOUNT)
        {
            if (cug.getSmsOutgoingValue() < 0 || cug.getSmsOutgoingValue() > 100)
            {
                exception.thrown(
                    new IllegalPropertyArgumentException(
                        "SMS MO Discount",
                        "Value must be 0...100"));      
            }
            if (cug.getSmsIncomingValue() < 0 || cug.getSmsIncomingValue() > 100)
            {
                exception.thrown(
                    new IllegalPropertyArgumentException(
                        "SMN MT Discount",
                        "Value must be 0...100"));      
            }
        }
        else if (cug.getSmsDiscountType() == DiscountTypeEnum.RATE_PLAN)
        {
            if (cug.getSmsOutgoingValue() < 0)
            {
                exception.thrown(
                    new IllegalPropertyArgumentException(
                        "SMS MO Rate Plan",
                        "Value must be >= 0"));
            }
            if (cug.getSmsIncomingValue() < 0)
            {
                exception.thrown(
                    new IllegalPropertyArgumentException(
                        "SMS MT Rate Plan",
                        "Value must be >= 0"));
            }
        }
    }
    
    private void validateVoiceFields(ClosedUserGroupTemplate cug, CompoundIllegalStateException exception)
    {
        if (cug.getVoiceDiscountType() == DiscountTypeEnum.DISCOUNT)
        {
            if (cug.getVoiceOutgoingValue() < 0 || cug.getVoiceOutgoingValue() > 100)
            {
                exception.thrown(
                    new IllegalPropertyArgumentException(
                        "Voice MO Discount",
                        "Value must be 0...100"));      
            }
            if (cug.getVoiceIncomingValue() < 0 || cug.getVoiceIncomingValue() > 100)
            {
                exception.thrown(
                    new IllegalPropertyArgumentException(
                        "Voice MT Discount",
                        "Value must be 0...100"));      
            }

        }
        else if (cug.getVoiceDiscountType() == DiscountTypeEnum.RATE_PLAN)
        {
            if (cug.getVoiceOutgoingValue() < 0)
            {
                exception.thrown(
                    new IllegalPropertyArgumentException(
                        "Voice MO Rate Plan",
                        "Value must be >= 0"));
            }
            if (cug.getVoiceIncomingValue() < 0)
            {
                exception.thrown(
                    new IllegalPropertyArgumentException(
                        "Voice MT Rate Plan",
                        "Value must be >= 0"));
            }
        }
    }    
}
