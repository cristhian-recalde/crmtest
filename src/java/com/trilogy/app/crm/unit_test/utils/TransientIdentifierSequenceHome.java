package com.trilogy.app.crm.unit_test.utils;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.IdentifierSequence;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Home decorator that has utilities to update the Transient Identifier Sequence Base Home 
 * @author angie.li
 *
 */
public class TransientIdentifierSequenceHome extends HomeProxy
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    private IdentifierEnum identifier_ = null;
    private long startNum_ = 1000000000000L;
    private long endNum_ = 2000000000000L;

    /**
     * Creates a new TransientIdentifierSequenceHome.
     *
     * @param ctx
     *            The operation context.
     * @param delegate
     *            The Home to which we delegate.
     * @exception HomeException
     *                Thrown if there are problems accessing Home data in the context used
     *                to set-up this home.
     */
    public TransientIdentifierSequenceHome(
            final Context ctx, 
            final Home delegate, 
            final IdentifierEnum identifier) throws HomeException
    {
        super(delegate);

        identifier_ = identifier;
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx, identifier_, startNum_,
                endNum_);
    }
    
    /**
     * Creates a new TransientIdentifierSequenceHome with configurable 
     * start and end sequence range.
     * @param ctx
     * @param delegate
     * @param identifier
     * @throws HomeException
     */
    public TransientIdentifierSequenceHome(
            final Context ctx, 
            final Home delegate, 
            final IdentifierEnum identifier,
            final long startSeq, 
            final long endSeq) throws HomeException
    {
        super(delegate);

        identifier_ = identifier;
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx, identifier_, startSeq, endSeq);
    }

    /**
     * Gets the next available identifier, and update the sequence counter.
     *
     * @param ctx
     *            The operating context.
     * @return The next available identifier.
     * @throws HomeException
     *             Thrown if there are problems retrieving the next available transaction
     *             identifier.
     */
    public synchronized long getNextIdentifier(final Context ctx) throws HomeException
    {
        IdentifierSequence seq = IdentifierSequenceSupportHelper.get(ctx).getIdentifierSequence(ctx, identifier_);
        IdentifierSequenceSupportHelper.get(ctx).updateIdentifierSequence(ctx, identifier_, seq.getNextNum()+1);
        return seq.getNextNum();
    }
} // class
