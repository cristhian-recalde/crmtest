package com.trilogy.app.crm.bulkloader.generic.request;

import java.util.Set;

import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.context.Context;

/**
 * A Generic Bean Bulk Load Request
 * @author angie.li
 *
 */
public interface BulkloadRequest extends Identifiable 
{
    public void reportError(Context ctx, Throwable error);
    public boolean hasErrors(Context ctx);
    public Set<Throwable> getErrors(Context ctx);
    
    public String getSuccessMessage(Context ctx);
    
    /**
     * Returns the bean that is to be loaded
     * @param ctx
     * @return
     */
    public Object getBean(Context ctx);
}
