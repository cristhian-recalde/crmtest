package com.trilogy.app.crm.filter;

import com.trilogy.framework.xhome.beans.XDeepCloneable;


public class SimpleDeepClone implements XDeepCloneable
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    /**
     * {@inheritDoc}
     */
    public Object deepClone() throws CloneNotSupportedException
    {
        return clone();
    }
}
