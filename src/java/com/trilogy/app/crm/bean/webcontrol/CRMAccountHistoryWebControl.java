package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.AccountHistoryWebControl;
import com.trilogy.app.crm.bean.payment.PaymentPlanHistoryWebControl;
import com.trilogy.app.crm.web.control.CurrencyContextSetupWebControl;

public class CRMAccountHistoryWebControl extends AccountHistoryWebControl
{

    @Override
    public WebControl getAmountWebControl()
    {
        return CUSTOM_AMOUNT_WC;
    }

    public static final WebControl CUSTOM_AMOUNT_WC = new CurrencyContextSetupWebControl(new XCurrencyWebControl(false));
}
