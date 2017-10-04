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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.OptionalLongWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * A customized {@link OptionalLongWebControl} which returns <code>null</code> if a
 * non-numeric string is entered.
 *
 * @author cindy.wong@redknee.com
 */
public class CustomOptionalLongWebControl extends OptionalLongWebControl
{

    /**
     * Create a new instance of <code>CustomOptionalLongWebControl</code>.
     */
    public CustomOptionalLongWebControl()
    {
        super();
        this.defaultValueObject_ = Long.valueOf(this.defaultValue_);
    }


    /**
     * Create a new instance of <code>CustomOptionalLongWebControl</code>.
     *
     * @param defaultValue
     *            Default value of the web control.
     */
    public CustomOptionalLongWebControl(final long defaultValue)
    {
        super(defaultValue);
        this.defaultValueObject_ = Long.valueOf(this.defaultValue_);
    }


    /**
     * Create a new instance of <code>CustomOptionalLongWebControl</code>.
     *
     * @param delegate
     *            Delegate of this web control.
     * @param defaultValue
     *            Default value of the web control.
     */
    public CustomOptionalLongWebControl(final WebControl delegate, final long defaultValue)
    {
        super(delegate, defaultValue);
        this.defaultValueObject_ = Long.valueOf(this.defaultValue_);
    }


    /**
     * Create a new instance of <code>CustomOptionalLongWebControl</code>.
     *
     * @param delegate
     *            Delegate of this web control.
     * @param emptyWebControl
     *            The empty web control.
     * @param defaultValue
     *            Default value of the web control.
     */
    public CustomOptionalLongWebControl(final WebControl delegate, final WebControl emptyWebControl,
            final long defaultValue)
    {
        super(delegate, emptyWebControl, defaultValue);
        this.defaultValueObject_ = Long.valueOf(this.defaultValue_);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void toWeb(final Context context, final PrintWriter out, final String name, final Object object)
    {
        Object displayObject = object;
        if (!(object instanceof Number))
        {
            displayObject = this.defaultValueObject_;
        }
        super.toWeb(context, out, name, displayObject);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromWeb(final Context context, final ServletRequest request, final String name)
    {
        try
        {
            return super.fromWeb(context, request, name);
        }
        catch (ClassCastException exception)
        {
            throw new IllegalPropertyArgumentException(name, "This value must be a number.");
        }
    }

    /**
     * The objectified default value.
     */
    private final Long defaultValueObject_;
}
