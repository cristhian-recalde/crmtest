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

import com.trilogy.app.crm.bean.CallingGroupServiceTypeEnum;
import com.trilogy.app.crm.bean.DiscountTypeEnum;
import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;

/**
 * @author margarita.alp@redknee.com
 */
public class PersonalListPlanValidator extends ContextAwareSupport implements Validator
{
    public PersonalListPlanValidator(final Context ctx)
    {
        setContext(ctx);
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final PersonalListPlan plan = (PersonalListPlan) obj;

        final CompoundIllegalStateException exception = new CompoundIllegalStateException();

        if (plan.getName() == null || plan.getName().equals(""))
        {
            exception.thrown(new IllegalPropertyArgumentException(
                    "PLP Name",
                    "Empty names are not allowed"));
        }
        
        if (plan.getPlpServiceType().equals(CallingGroupServiceTypeEnum.ALL) || plan.getPlpServiceType().equals(CallingGroupServiceTypeEnum.VOICE))
        {
            validateVoiceFields(plan, exception);
        }
        
        if (plan.getPlpServiceType().equals(CallingGroupServiceTypeEnum.ALL) || plan.getPlpServiceType().equals(CallingGroupServiceTypeEnum.SMS))
        {
            validateSmsFields(plan, exception);
        }

        exception.throwAll();
    }
    
    private void validateSmsFields(PersonalListPlan plan, CompoundIllegalStateException exception)
    {
        if (plan.getVoiceDiscountType() == DiscountTypeEnum.DISCOUNT)
        {
            if (plan.getVoiceOutgoingValue() < 0 || plan.getVoiceOutgoingValue() > 100)
            {
                exception.thrown(new IllegalPropertyArgumentException(
                        "Voice MO Discount",
                        "Value must be 0...100"));
            }
            if (plan.getVoiceIncomingValue() < 0 || plan.getVoiceIncomingValue() > 100)
            {
                exception.thrown(new IllegalPropertyArgumentException(
                        "Voice MT Discount",
                        "Value must be 0...100"));
            }
        }
        else if (plan.getVoiceDiscountType() == DiscountTypeEnum.RATE_PLAN)
        {
            if (plan.getVoiceOutgoingValue() < 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(
                        "Voice MO Rate Plan",
                        "Value must be >= 0"));
            }
            if (plan.getVoiceIncomingValue() < 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(
                        "Voice MT Rate Plan",
                        "Value must be >= 0"));
            }
        }
    }
    
    private void validateVoiceFields(PersonalListPlan plan, CompoundIllegalStateException exception)
    {
        if (plan.getSmsDiscountType() == DiscountTypeEnum.DISCOUNT)
        {
            if (plan.getSmsOutgoingValue() < 0 || plan.getSmsOutgoingValue() > 100)
            {
                exception.thrown(new IllegalPropertyArgumentException(
                        "SMS MO Discount",
                        "Value must be 0...100"));
            }
            if (plan.getSmsIncomingValue() < 0 || plan.getSmsIncomingValue() > 100)
            {
                exception.thrown(new IllegalPropertyArgumentException(
                        "SMS MT Discount",
                        "Value must be 0...100"));
            }
        }
        else if (plan.getSmsDiscountType() == DiscountTypeEnum.RATE_PLAN)
        {
            if (plan.getSmsOutgoingValue() < 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(
                        "SMS MO Rate Plan",
                        "Value must be >= 0"));
            }
            if (plan.getSmsIncomingValue() < 0)
            {
                exception.thrown(new IllegalPropertyArgumentException(
                        "SMS MT Rate Plan",
                        "Value must be >= 0"));
            }
        }
    }
    
}
