package com.trilogy.app.crm.unit_test.utils;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.IdentifierAware;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.sequenceId.SequenceIdentified;

/**
 * Used for beans that implement com.redknee.app.crm.sequenceId.SequenceIdentified or
 * com.redknee.app.crm.bean.IdentifierAware.
 * Decorates Home Pipelines and sets the bean's identifier to using a value 
 * from a transient Identifier Sequence.
 * 
 * Only for Unit Testing.
 * @author angie li
 *
 */
public class TransientSequenceIdentifiedSettingHome extends TransientIdentifierSequenceHome
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new TransientSequenceIdentifiedSettingHome.
     *
     * @param ctx
     *            The operation context.
     * @param delegate
     *            The Home to which we delegate.
     * @param identifier
     *            the Identifier Sequence identifier
     * @exception HomeException
     *                Thrown if there are problems accessing Home data in the context used
     *                to set-up this home.
     */
    public TransientSequenceIdentifiedSettingHome(
            final Context ctx, 
            final Home delegate,
            final IdentifierEnum identifier) throws HomeException
    {
        super(ctx, delegate, identifier);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        if (obj instanceof SequenceIdentified)
        {
            if (((SequenceIdentified)obj).getIdentifier() <= 0L)
            {
                final long identifier = getNextIdentifier(ctx);
                ((SequenceIdentified)obj).setIdentifier(identifier);
            }
        }
        else if (obj instanceof IdentifierAware)
        {
            if (((IdentifierAware)obj).getIdentifier() <= 0L)
            {
                final long identifier = getNextIdentifier(ctx);
                ((IdentifierAware)obj).setIdentifier(identifier);
            }
        }
        else
        {
            XInfo xinfo = (XInfo) XBeans.getInstanceOf(ctx, obj.getClass(), XInfo.class);
            final long identifier = getNextIdentifier(ctx);
            xinfo.getID().set(obj, identifier);
        }
        if(LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder msg = new StringBuilder().append("Creating new ");
            msg.append(obj.getClass().getSimpleName());
            msg.append(" bean. Details[");
            msg.append(obj.toString());
            msg.append("]."); 
            LogSupport.info(ctx, this, msg.toString());
        }
        return super.create(ctx, obj);
    }
    
} // class
