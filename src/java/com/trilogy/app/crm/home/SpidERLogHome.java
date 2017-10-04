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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.xhome.home.SimpleBeanERHome;

public class SpidERLogHome extends SimpleBeanERHome
{

    public SpidERLogHome(final Home delegate)
    {
        super(delegate, IDENTIFIER, CLASS, TITLE, FIELDS);
    }

    private static final int IDENTIFIER = 1121;
    private static final int CLASS = 700;
    private static final String TITLE = "SPID Management";
    
    private static final PropertyInfo[] FIELDS =
    {
        CRMSpidXInfo.ID,
        CRMSpidXInfo.NAME
    };

    protected Object getOriginal(final Context context, final Object object) throws HomeException
    {
        final CRMSpid newBean = (CRMSpid)object;

        final Home home = (Home)context.get(CRMSpidHome.class);

        return home.find(context, Integer.valueOf(newBean.getId()));
    }

}
