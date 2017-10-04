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

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.extension.service.core.URCSPromotionServiceExtension;

/**
 * This validator prevents from creating auxiliary services with a service option that is used by a non closed
 * auxiliary service.
 *
 * @author victor.stratan@redknee.com
 */
public class AuxiliaryServiceOneToOneServiceOptionValidator implements Validator
{
    /**
     * Validate that service option is not in use by other non-closed auxiliary service.
     *
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
    	URCSPromotionServiceExtension.validate(ctx, obj);
    }

}