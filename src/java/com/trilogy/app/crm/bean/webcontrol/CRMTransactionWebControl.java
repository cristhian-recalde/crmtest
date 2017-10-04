package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.web.control.CurrencyContextSetupWebControl;
import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.webcontrol.FinalWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


public class CRMTransactionWebControl extends CustomTransactionWebControl
{

    @Override
    public WebControl getAdjustmentTypeWebControl()
    {
        return CUSTOM_ADJUSTMENT_TYPE_WC;
    }

    @Override
    public WebControl getAmountWebControl()
    {
        return CUSTOM_AMOUNT_WC;
    }

    @Override
    public WebControl getBalanceWebControl()
    {
        return CUSTOM_AMOUNT_WC;
    }

    public static final WebControl CUSTOM_ADJUSTMENT_TYPE_WC = new FinalWebControl(new com.redknee.app.crm.web.control.AdjustmentTypeCheckPermissionProxyWebControl(
            new com.redknee.app.crm.web.control.AdjustmentTypeComboKeyWebControl(
                    com.redknee.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_READ_ONLY_HOME,
                    true, TransactionXInfo.ADJUSTMENT_TYPE_CATEGORY)));

    public static final WebControl CUSTOM_AMOUNT_WC = new FinalWebControl(new CurrencyContextSetupWebControl(new XCurrencyWebControl(false)));

}
