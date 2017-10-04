package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.DiscountTransactionHist;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class DiscountTransactionHistIdentifierSettingHome extends HomeProxy
{

    /**
     * Creates a new DiscountTransactionHistIdentifierSettingHome proxy.
     * 
     * @param delegate
     *            The Home to which we delegate.
     */
    public DiscountTransactionHistIdentifierSettingHome(final Home delegate)
    {
        super(delegate);
    }


    // INHERIT
    public Object create(final Context ctx, final Object bean) throws HomeException
    {
        final DiscountTransactionHist disTransHist = (DiscountTransactionHist) bean;
        // Throws HomeException.
        final long identifier = getNextIdentifier(ctx);
        disTransHist.setId(identifier);
        return super.create(ctx, disTransHist);
    }


    /**
     * Gets the next available identifier.
     * 
     * @return The next available identifier.
     */
    private long getNextIdentifier(final Context ctx)
        throws HomeException
    {
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(
            ctx,
            IdentifierEnum.DISCOUNT_TRANSACTION_HIST_REC_ID,
            1,
            Long.MAX_VALUE);

        
        return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
            ctx,
            IdentifierEnum.DISCOUNT_TRANSACTION_HIST_REC_ID,
            null);
    }

}
