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

import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.xhome.home.SimpleBeanERHome;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.home.Home;


/**
 * Logs ER 1136 RK_ICD_I_2011_4 This ER Tracks changes, including creation and deletion,
 * of TDMA card packages. If the action is �creation�, then only the �new� field values are
 * relevant. If the action is �deletion�, then only the �old� field values are relevant.
 * If the action is �update�, then both the �new� and �old� field values are relevant and
 * will differ only for those values that have changed. [Feb 20, 2010]
 * 
 * @author simar.singh@redknee.com
 */
public class TDMAPackageERLogHome extends SimpleBeanERHome
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public TDMAPackageERLogHome(final Home delegate)
    {
        super(delegate, IDENTIFIER, CLASS, TITLE, FIELDS, TDMAPackageXInfo.PACK_ID);
    }

    private static final int IDENTIFIER = 1136;
    private static final int CLASS = 1100;
    private static final String TITLE = "TDMA Card Package Update:1:0";

    private static final PropertyInfo[] FIELDS =
    {
        TDMAPackageXInfo.PACKAGE_GROUP,
        TDMAPackageXInfo.TECHNOLOGY,
        TDMAPackageXInfo.DEFAULT_RESOURCE_ID,
        TDMAPackageXInfo.MIN,
        TDMAPackageXInfo.ESN,
        TDMAPackageXInfo.SERIAL_NO,
        TDMAPackageXInfo.SUBSIDY_KEY,
        TDMAPackageXInfo.MASS_SUBSIDY_KEY,
        TDMAPackageXInfo.SERVICE_LOGIN1,
        TDMAPackageXInfo.SERVICE_PASSWORD1,
        TDMAPackageXInfo.SERVICE_LOGIN2,
        TDMAPackageXInfo.SERVICE_PASSWORD2,
        TDMAPackageXInfo.CALLBACK_ID,
        TDMAPackageXInfo.RADIUS_PROFILE_NAME,
        TDMAPackageXInfo.DEALER,
        TDMAPackageXInfo.STATE,
        TDMAPackageXInfo.BATCH_ID,
        TDMAPackageXInfo.PACKAGE_TYPE,
        TDMAPackageXInfo.EXTERNAL_MSID,
        TDMAPackageXInfo.CUSTOMER_OWNED,
        
    };


}
