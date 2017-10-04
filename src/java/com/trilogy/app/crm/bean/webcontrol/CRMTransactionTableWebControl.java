package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.TransactionTableWebControl;


public class CRMTransactionTableWebControl extends TransactionTableWebControl
{
    @Override
    public WebControl getAdjustmentTypeWebControl()
    {
        return CRMTransactionWebControl.CUSTOM_ADJUSTMENT_TYPE_WC;
    }

    @Override
    public WebControl getAmountWebControl()
    {
        return CRMTransactionWebControl.CUSTOM_AMOUNT_WC;
    }

    @Override
    public WebControl getBalanceWebControl()
    {
        return CRMTransactionWebControl.CUSTOM_AMOUNT_WC;
    }
}
