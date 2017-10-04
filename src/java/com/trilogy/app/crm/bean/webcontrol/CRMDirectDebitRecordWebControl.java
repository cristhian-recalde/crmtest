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
package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.DirectDebitRecordWebControl;
import com.trilogy.app.crm.web.control.CurrencyContextSetupWebControl;
import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

public class CRMDirectDebitRecordWebControl extends DirectDebitRecordWebControl {

    public WebControl getBillAmountWebControl()
    {
        return BILL_AMOUNT_WC;
    }


    public WebControl getReceivedAmountWebControl()
    {
        return RECEIVED_AMOUNT_WC;
    }


    public WebControl getMaxAmountWebControl()
    {
        return MAX_AMOUNT_WC;
    }

    public static final WebControl MAX_AMOUNT_WC      = new CurrencyContextSetupWebControl(new XCurrencyWebControl(
                                                              false));

    public static final WebControl BILL_AMOUNT_WC     = new CurrencyContextSetupWebControl(new XCurrencyWebControl(
                                                              false));

    public static final WebControl RECEIVED_AMOUNT_WC = new CurrencyContextSetupWebControl(new XCurrencyWebControl(
                                                              false));

}
