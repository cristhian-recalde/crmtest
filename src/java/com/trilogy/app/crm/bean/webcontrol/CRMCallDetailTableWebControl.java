package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.web.control.CurrencyContextSetupWebControl;


public class CRMCallDetailTableWebControl extends CustomCallDetailTableWebControl
{
    @Override
    public WebControl getBalanceWebControl()
    {
        return CUSTOM_CHARGE_WC;
    }

    @Override
    public WebControl getChargeWebControl()
    {
        return CUSTOM_CHARGE_WC;
    }

    public static final WebControl CUSTOM_CHARGE_WC = new CurrencyContextSetupWebControl(new XCurrencyWebControl(false));
}
