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
package com.trilogy.app.crm.support;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.json.JSONParser;
import com.trilogy.framework.xhome.parse.Parser;
import com.trilogy.framework.xhome.support.IdentitySupport;


/**
 * Provides a simple abstract implementation that assumes the bean is the
 * identifier.  Useful for creating anonymous instances of IdentitySupport for
 * specific purposes.
 *
 * @author gary.anderson@redknee.com
 */
public
class AbstractIdentitySupport
    implements IdentitySupport
{
    public AbstractIdentitySupport( )
    {
    }
    
    public AbstractIdentitySupport(PropertyInfo info)
    {
        xinfo_ =info;
    }
    // INHERIT
    public Object toBean(final Object id)
    {
        return id;
    }


    // INHERIT
    public Object setID(final Object bean, final Object id)
    {
        return id;
    }
   

    // INHERIT
    public Object fromStringID(final String id)
    {
        return id;
    }
   

    // INHERIT
    public String toStringID(final Object id)
    {
        return id.toString();
    }


    // INHERIT
    public Object ID(final Object bean)
    {
        return bean;
    }

    public boolean isKey(Object id)
    {
    	return ( id.getClass().isInstance(this.getClass()));
    }


    @Override
    public Object toKey(Object obj)
    {
        return ID(obj);
    }


    @Override
    public Parser createParser(Context ctx)
    {      
        
        return JSONParser.parser(ctx, xinfo_);    
    }
    PropertyInfo xinfo_;
} // class
