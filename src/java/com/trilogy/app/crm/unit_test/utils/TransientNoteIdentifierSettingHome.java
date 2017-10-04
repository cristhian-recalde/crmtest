package com.trilogy.app.crm.unit_test.utils;

import com.trilogy.app.crm.bean.Note;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * The Note bean is neither SequenceIdentifiable nor does exist in the IdentifierSequenceEnum.
 * 
 * No choice but to introduce an id counter here.
 * 
 * @author angie.li
 *
 */
public class TransientNoteIdentifierSettingHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    
    private long idCounter_ = 0;

    /**
     * Creates a new TransientNoteIdentifierSettingHome.
     *
     * @param ctx
     *            The operation context.
     * @param delegate
     *            The Home to which we delegate.
     * @exception HomeException
     *                Thrown if there are problems accessing Home data in the context used
     *                to set-up this home.
     */
    public TransientNoteIdentifierSettingHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        Note note = (Note) obj;
        note.setId(getNextIdentifier());

        return super.create(ctx,note);
    }
    
    private synchronized long getNextIdentifier()
    {
        return idCounter_++;
    }
} // class

