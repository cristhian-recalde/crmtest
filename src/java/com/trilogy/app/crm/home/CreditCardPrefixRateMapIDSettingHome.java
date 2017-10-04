package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.CreditCardPrefixRateMap;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class CreditCardPrefixRateMapIDSettingHome  extends HomeProxy
{
	 
	/**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    

    public CreditCardPrefixRateMapIDSettingHome(final Context ctx,  final String sequenceName, final Home delegate)
    {
        super(ctx, delegate);
        this.seqName = sequenceName;

    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
    	CreditCardPrefixRateMap cmd = (CreditCardPrefixRateMap) obj;
        
        cmd.setId((int)this.getNextIdentifier(ctx));
        
        return super.create(ctx, cmd); 
    }
    
    
    /**
     * Gets the next available identifier.
     *
     * @return The next available identifier.
     *
     * @exception HomeException Thrown if there is a problem accessing the
     * sequence identifier information in the operating context.
     */
    private long getNextIdentifier(final Context ctx)
        throws HomeException
    {
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(
            ctx,
            seqName,
            1,
            Long.MAX_VALUE);

        return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
            ctx,
            seqName,
            null);
    }
    
    
    String seqName;
}
