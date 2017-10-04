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

import com.trilogy.app.crm.bean.GSMPackageXInfo;
import com.trilogy.app.crm.xhome.home.SimpleBeanERHome;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.home.Home;


/**
 * Logs ER 1136 RK_ICD_I_2011_4 This ER Tracks changes, including creation and deletion,
 * of GSM card packages. If the action is �creation�, then only the �new� field values are
 * relevant. If the action is �deletion�, then only the �old� field values are relevant.
 * If the action is �update�, then both the �new� and �old� field values are relevant and
 * will differ only for those values that have changed. [Feb 20, 2010]
 * 
 * @author simar.singh@redknee.com
 */
public class GSMPackageERLogHome extends SimpleBeanERHome
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public GSMPackageERLogHome(final Home delegate)
    {
        super(delegate, IDENTIFIER, CLASS, TITLE, FIELDS, GSMPackageXInfo.PACK_ID);
    }

    private static final int IDENTIFIER = 1136;
    private static final int CLASS = 1100;
    private static final String TITLE = "GSM Card Package Update:1:0";

    private static final PropertyInfo[] FIELDS =
    {
        GSMPackageXInfo.PACKAGE_GROUP,
        GSMPackageXInfo.TECHNOLOGY,
        GSMPackageXInfo.DEFAULT_RESOURCE_ID,
        GSMPackageXInfo.SERIAL_NO,
        GSMPackageXInfo.IMSI,
        GSMPackageXInfo.PIN1,
        GSMPackageXInfo.PUK1,
        GSMPackageXInfo.PIN2,
        GSMPackageXInfo.PUK2,
        GSMPackageXInfo.DEALER,
        GSMPackageXInfo.SERVICE_LOGIN1,
        GSMPackageXInfo.SERVICE_PASSWORD1,
        GSMPackageXInfo.SERVICE_LOGIN2,
        GSMPackageXInfo.SERVICE_PASSWORD2,
        GSMPackageXInfo.STATE,
        GSMPackageXInfo.BATCH_ID,
        
        
    };

}
