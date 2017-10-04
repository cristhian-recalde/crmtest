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
package com.trilogy.app.crm.web.border;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.home.AdjustmentTypeIdentifierProtectionHome;


/**
 * Provides a way to prevent the creation of new AdjustmentTypes in a specific
 * set of ranges.  This border decorates the actual AdjustmentType home with a
 * protection home.
 *
 * @author gary.anderson@redknee.com
 */
public
class AdjustmentTypeIdentifierProtectionBorder
    implements Border
{
    /**
     * Creates a new instance of AdjustmentTypeIdentifierProtectionBorder.
     */
    protected AdjustmentTypeIdentifierProtectionBorder()
    {
        // Empty
    }

    
    // INHERIT
    public void service(
        Context context,
        final HttpServletRequest request,
        final HttpServletResponse response,
        final RequestServicer delegate)
        throws ServletException, IOException
    {
        context = context.createSubContext();
        context.setName(this.getClass().getName());

        Home home = (Home)context.get(AdjustmentTypeHome.class);
        home = new AdjustmentTypeIdentifierProtectionHome(context, home);

        context.put(AdjustmentTypeHome.class, home);

        delegate.service(context, request, response);
    }

    
    /**
     * Provides access to a single instance of this border.
     */
    public static AdjustmentTypeIdentifierProtectionBorder instance()
    {
        return instance_;
    }

    
    /**
     * A sharable instance of this class.
     */
    private static final AdjustmentTypeIdentifierProtectionBorder instance_ =
        new AdjustmentTypeIdentifierProtectionBorder();
    
    
} // class
