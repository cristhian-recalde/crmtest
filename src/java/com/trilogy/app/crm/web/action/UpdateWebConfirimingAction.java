/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.action;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.util.Link;



/**
 **/
public class UpdateWebConfirimingAction extends SimpleWebConfirmingAction
{

    private static final long serialVersionUID = 1L;


    public UpdateWebConfirimingAction()
    {
        this("update", "Update");
    }

    public UpdateWebConfirimingAction(String key, String vlaue)
    {
        super("update", "Update");
    }

    @Override
    public Link modifyLink(Context ctx, Object bean, Link link)
    {
        link.add("CMD", "Update");
        return super.modifyLink(ctx, bean, link);
    }
   
}
