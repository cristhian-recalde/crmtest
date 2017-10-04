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
package com.trilogy.app.crm.bean;


/**
 * Provides the primary class for the FreeCallTime.  It's primary purpose is to
 * ensure consistent values between the transient groupPooledSettings property,
 * and the underlying usagePrecedence and groupLimit properties.
 *
 * @author gary.anderson@redknee.com
 */
public
class FreeCallTime
    extends AbstractFreeCallTime
{
    /**
     * {@inheritDoc}
     */
    public void setUsagePrecedence(final FCTUsagePrecedenceEnum type)
    {
        getGroupPooledSettings().setUsagePrecedence(type);

        // While strictly not necessary, calling the overriden method here
        // ensures that this method is valid -- a change in the model's property
        // would likely cause a compilation error.
        super.setUsagePrecedence(type);
    }


    /**
     * {@inheritDoc}
     */
    public FCTUsagePrecedenceEnum getUsagePrecedence()
    {
        return getGroupPooledSettings().getUsagePrecedence();
    }


    /**
     * {@inheritDoc}
     */
    public void setGroupLimit(final int limit)
    {
        getGroupPooledSettings().setGroupLimit(limit);

        // While strictly not necessary, calling the overriden method here
        // ensures that this method is valid -- a change in the model's property
        // would likely cause a compilation error.
        super.setGroupLimit(limit);
    }


    /**
     * {@inheritDoc}
     */
    public int getGroupLimit()
    {
        return getGroupPooledSettings().getGroupLimit();
    }

} // class
