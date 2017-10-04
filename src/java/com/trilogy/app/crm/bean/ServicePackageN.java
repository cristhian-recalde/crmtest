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

import java.io.Serializable;


/**
 * Service package implementation.
 *
 * @author paul.sperneac@redknee.com
 */
public class ServicePackageN extends AbstractServicePackageN implements Serializable
{

    /**
     * Serial version UID.
     */
    public static final long serialVersionUID = 9007070820291422500L;


    /**
     * {@inheritDoc}
     */
    public long getIdentifier()
    {
        return getId();
    }


    /**
     * {@inheritDoc}
     */
    public void setIdentifier(final long value)
    {
        this.setId((int) value);

    }


    /**
     * {@inheritDoc}
     */
    public boolean isEnabled()
    {
        return getState() == ServicePackageStateEnum.ACTIVE_INDEX;
    }


    /**
     * {@inheritDoc}
     */
    public void setEnabled(final boolean flag)
    {
        short state;
        if (flag)
        {
            state = ServicePackageStateEnum.ACTIVE_INDEX;
        }
        else
        {
            state = ServicePackageStateEnum.DEPRECATED_INDEX;
        }
        setState(state);
    }
}
