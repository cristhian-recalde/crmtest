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
package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.framework.xhome.context.Context;


public class CugShortCodeTextRegexCheckingWebControl extends AbstractTextFieldRegexCheckingWebControl
{

        
    @Override
    public String getPatternRegExToMatch(Context ctx, String name, Object obj)
    {
        // expected from the enclosing Bean's WebControl (ClosedUserGroup) to have put it in context
        ClosedUserGroup cug = (ClosedUserGroup) ctx.get(ClosedUserGroup.class);
        return cug.getShortCodePattern(ctx);
    }


    @Override
    public boolean isUniqueInTable(Context ctx, String name, Object obj)
    {
        return true;
    }

    
}
