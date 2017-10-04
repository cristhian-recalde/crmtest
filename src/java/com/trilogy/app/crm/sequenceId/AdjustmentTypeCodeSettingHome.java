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
package com.trilogy.app.crm.sequenceId;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;


/**
 * Sets the unique code for AdjustmentTypes.
 *
 * @author jimmy.ng@redknee.com
 */
public class AdjustmentTypeCodeSettingHome
    extends HomeProxy
{
    /**
     * Creates a new AdjustmentTypeCodeSettingHome proxy.
     *
     * @param context The operating context.
     * @param delegate The Home to which we delegate.
     */
    public AdjustmentTypeCodeSettingHome(
        Context context,
        Home delegate)
    {
        super(context, delegate);
    }

    /**
     * {@inheritDoc}
     *
     * This Home automatically assigns a new, unique, code to the given
     * AdjustmentType.
     */
    public Object create(Context ctx,final Object obj)
        throws HomeException
    {
        final AdjustmentType bean = (AdjustmentType) obj;

        // Throws HomeException.

        // this will allow us to enter adjustment types with codes != 0 to set the codes we miss through
        // beanshell. The ui brings in 0, so we get the next id.
        if(bean.getCode()==0)
        {
        	final int code = getNextIdentifier(ctx);
        	bean.setCode(code);
        }

        return super.create(ctx,bean);
    }


    /**
     * Gets the next available AdjustmentType code.
     *
     * @return The next available AdjustmentType code.
     *
     * @exception HomeException Thrown if there is a problem accessing the
     * sequence identifier information in the operating context.
     */
    protected int getNextIdentifier(Context ctx)
        throws HomeException
    {
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(
            ctx,
            IdentifierEnum.ADJUSTMENT_TYPE_CODE,
            50000,  // The last possible pre-defined Adjustment Type Code is
                    // 49999, so we'd start at 50000
            Integer.MAX_VALUE);

        boolean exist = true;
        int key = 0;
        for (int i = 50000; exist == true; i++)
        {
        	
        	key = (int) IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
                    ctx,
                    IdentifierEnum.ADJUSTMENT_TYPE_CODE,
                    null);
        	AdjustmentType type = (AdjustmentType) getDelegate().find(ctx, Integer.valueOf(key));
        	if (type == null)
        	{
        		exist = false;
        	}
        }
        return key;
    }
} // class
