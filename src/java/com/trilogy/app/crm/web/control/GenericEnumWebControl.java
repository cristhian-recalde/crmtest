/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xhome.xenum.Enum;

import javax.servlet.ServletRequest;

/**
 * An EnumWebControl which takes a Number instead of an Enum.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class GenericEnumWebControl extends EnumWebControl
{

    public GenericEnumWebControl(EnumCollection enumc)
    {
        super(enumc);
    }

    public GenericEnumWebControl(EnumCollection enumc, int enumSize)
    {
        super(enumc, enumSize);
    }

    public GenericEnumWebControl(EnumCollection enumc, boolean autoPreview)
    {
        super(enumc, autoPreview);
    }

    public GenericEnumWebControl(EnumCollection enumc, Class baseClass,
            String sourceField, String targetField)
    {
        super(enumc, baseClass, sourceField, targetField);
    }

    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Number number = (Number) obj;
        Enum e = this.getEnumCollection(ctx).get((short) 0);
        for (Iterator it = this.getEnumCollection(ctx).iterator(); it.hasNext();)
        {
            e = (Enum) it.next();
            if (e.getIndex() == number.shortValue())
            {
                break;
            }
        }
        super.toWeb(ctx, out, name, e);
    }

    public Object fromWeb(Context ctx, ServletRequest req, String name)
            throws NullPointerException
    {
        Enum e = (Enum) super.fromWeb(ctx, req, name);
        return new Short(e.getIndex());
    }
}
