package com.trilogy.app.crm.unit_test.utils;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
/**
 * Identifier Setting Home for that uses the Transient Identifier Sequence Home. 
 * Could not use AdjustmentTypeCodeSettingHome.
 * @author angie.li
 *
 */
public class TransientAdjustmentTypeIdentifierSettingHome extends TransientIdentifierSequenceHome
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new TransientTransactionIdentifierSettingHome.
     *
     * @param ctx
     *            The operation context.
     * @param delegate
     *            The Home to which we delegate.
     * @exception HomeException
     *                Thrown if there are problems accessing Home data in the context used
     *                to set-up this home.
     */
    public TransientAdjustmentTypeIdentifierSettingHome(final Context ctx, final Home delegate) throws HomeException
    {
        super(ctx, delegate, IdentifierEnum.ADJUSTMENT_TYPE_CODE, 50000, Integer.MAX_VALUE);
    }


    /**
     * Set a new Transaction Id every time a create is called. 
     */
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        AdjustmentType bean = (AdjustmentType) obj;
        
        if(bean.getCode()==0)
        {
            final int identifier = (int)getNextIdentifier(ctx);
            bean.setCode(identifier);
        }
        
        return super.create(ctx, bean);
    }
} // class
