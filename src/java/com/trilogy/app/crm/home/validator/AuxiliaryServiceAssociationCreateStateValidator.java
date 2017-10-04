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
package com.trilogy.app.crm.home.validator;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.SctAuxiliaryService;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * Validates that an association is allowed to be created based on the state
 * of the auxiliary service.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class AuxiliaryServiceAssociationCreateStateValidator implements Validator
{
    private static Validator instance_ = null;
    public static Validator instance()
    {
        if (instance_ == null)
        {
            instance_ = new AuxiliaryServiceAssociationCreateStateValidator();
        }
        return instance_;
    }
    
    protected AuxiliaryServiceAssociationCreateStateValidator()
    {
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        if (obj instanceof SubscriberAuxiliaryService)
        {
            SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;
            
            try
            {
                AuxiliaryService auxiliaryService = association.getAuxiliaryService(ctx);
                if (auxiliaryService == null)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, 
                            "Auxiliary Service " + association.getAuxiliaryServiceIdentifier() + " does not exist."));
                }
                else if (!EnumStateSupportHelper.get(ctx).stateEquals(auxiliaryService, AuxiliaryServiceStateEnum.ACTIVE))
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, 
                            "Auxiliary Service " + association.getAuxiliaryServiceIdentifier()
                            + " can't be provisioned for subscription " + association.getSubscriberIdentifier()
                            + " because it is " + auxiliaryService.getState()));
                }
            }
            catch (HomeException e)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, 
                        "Error retrieving Auxiliary Service " + association.getAuxiliaryServiceIdentifier()));
            }
        }
        else if (obj instanceof SctAuxiliaryService)
        {
            SctAuxiliaryService association = (SctAuxiliaryService) obj;

            try
            {
                AuxiliaryService auxiliaryService = HomeSupportHelper.get(ctx).findBean(ctx, AuxiliaryService.class, association.getAuxiliaryServiceIdentifier());
                if (auxiliaryService == null)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            SctAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, 
                            "Auxiliary Service " + association.getAuxiliaryServiceIdentifier() + " does not exist."));
                }
                else if (!EnumStateSupportHelper.get(ctx).stateEquals(auxiliaryService, AuxiliaryServiceStateEnum.ACTIVE))
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            SctAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, 
                            "Auxiliary Service " + association.getAuxiliaryServiceIdentifier()
                            + " can't be added to Subscription Creation Template " + association.ID()
                            + " because it is " + auxiliaryService.getState()));
                }
            }
            catch (HomeException e)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        SctAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, 
                        "Error retrieving Auxiliary Service " + association.getAuxiliaryServiceIdentifier()));
            }
        }
        
        cise.throwAll();
    }

}
