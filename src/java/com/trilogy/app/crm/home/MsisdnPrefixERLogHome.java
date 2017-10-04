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
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.MsisdnPrefix;
import com.trilogy.app.crm.bean.MsisdnPrefixHome;
import com.trilogy.app.crm.bean.MsisdnPrefixXInfo;
import com.trilogy.app.crm.xhome.home.SimpleBeanERHome;

public class MsisdnPrefixERLogHome extends SimpleBeanERHome 
{

    public MsisdnPrefixERLogHome(final Home delegate)
    {
        super(delegate, IDENTIFIER, CLASS, TITLE, FIELDS);
    }

    private static final int IDENTIFIER = 1122;
    private static final int CLASS = 700;
    private static final String TITLE = "Destination Zone Prefix Management";
    
    private static final PropertyInfo[] FIELDS =
    {
        MsisdnPrefixXInfo.IDENTIFIER,
        MsisdnPrefixXInfo.ID,
        MsisdnPrefixXInfo.PREFIX,
        MsisdnPrefixXInfo.DESCRIPTION,
        MsisdnPrefixXInfo.IS_SHORT_CODE
    };

    protected Object getOriginal(final Context context, final Object object) throws HomeException
    {
        final MsisdnPrefix newBean = (MsisdnPrefix)object;

        final Home home = (Home)context.get(MsisdnPrefixHome.class);

        final And criteria = new And();
        criteria.add(new EQ(MsisdnPrefixXInfo.IDENTIFIER, newBean.getIdentifier()));
        criteria.add(new EQ(MsisdnPrefixXInfo.ID, newBean.getId()));

        return home.find(context, criteria);
    }

}
