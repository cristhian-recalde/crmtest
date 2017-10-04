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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.VSATPackageXInfo;
import com.trilogy.app.crm.xhome.home.SimpleBeanERHome;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.home.Home;


/**
 * Logs ER 1136 RK_ICD_I_2011_4 This ER Tracks changes, including creation and deletion,
 * of VSAT card packages. If the action is �creation�, then only the �new� field values are
 * relevant. If the action is �deletion�, then only the �old� field values are relevant.
 * If the action is �update�, then both the �new� and �old� field values are relevant and
 * will differ only for those values that have changed. [Feb 20, 2010]
 * 
 * @author simar.singh@redknee.com
 */
public class VSATPackageERLogHome extends SimpleBeanERHome
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public VSATPackageERLogHome(final Home delegate)
    {
        super(delegate, IDENTIFIER, CLASS, TITLE, FIELDS, VSATPackageXInfo.PACK_ID);
    }

    private static final int IDENTIFIER = 1138;
    private static final int CLASS = 1100;
    private static final String TITLE = "VSAT Card Package Update:1:0";

    private static final PropertyInfo[] FIELDS =
    {
        VSATPackageXInfo.PACKAGE_GROUP,
        VSATPackageXInfo.TECHNOLOGY,
        VSATPackageXInfo.DEFAULT_RESOURCE_ID,
        VSATPackageXInfo.DEALER,
        VSATPackageXInfo.VSAT_ID,
        VSATPackageXInfo.PORT,
        VSATPackageXInfo.CHANNEL,
        VSATPackageXInfo.STATE,
        VSATPackageXInfo.BATCH_ID
    };

}
