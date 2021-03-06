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

import java.util.regex.Pattern;

import com.trilogy.framework.xhome.context.Context;


public class TextFieldRegexCheckingWebControl extends AbstractTextFieldRegexCheckingWebControl
{

    public TextFieldRegexCheckingWebControl(Pattern pattern)
    {
        this(pattern, false);
    }


    public TextFieldRegexCheckingWebControl(Pattern pattern, boolean isUniqueInTable)
    {
        pattern_ = pattern.toString();
        isUniqueInTable_ = isUniqueInTable;
    }


    @Override
    public String getPatternRegExToMatch(Context ctx, String name, Object obj)
    {
        return pattern_;
    }


    @Override
    public boolean isUniqueInTable(Context ctx, String name, Object obj)
    {
        return isUniqueInTable_;
    }

    private final String pattern_;
    private final boolean isUniqueInTable_;
}
