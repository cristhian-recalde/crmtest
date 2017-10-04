/*
 * Created on Nov 2, 2005 1:44:35 PM
 *
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
 * @author psperneac
 */
public class ServicePackageNVersion extends AbstractServicePackageNVersion
{
    public final static long serialVersionUID = 4402197368562479050L;

    public Object getParent()
    {
        return Integer.valueOf(getId());
    }

    public void setParent(Object parent)
    {
        setId(((Number) parent).intValue());
    }

    public int compareTo(Object obj)
    {
        ServicePackageVersion other = (ServicePackageVersion) obj;

        if (getId() != other.getId())
        {
            return (getId() - other.getId());
        }

        return getVersion() - other.getVersion();
    }
}
