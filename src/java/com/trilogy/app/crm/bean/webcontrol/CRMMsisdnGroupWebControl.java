package com.trilogy.app.crm.bean.webcontrol;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.MsisdnGroupXInfo;
import com.trilogy.app.crm.web.control.CurrencyContextSetupWebControl;
import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


public class CRMMsisdnGroupWebControl extends CustomMsisdnGroupWebControl
{

    @Override
    public WebControl getFeeWebControl()
    {
        return CUSTOM_FEE_WC;
    }

    
    @Override
    public WebControl getSpidWebControl()
    {
        return GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ContextLocator.locate()) ? 
                new ReadOnlyWebControl(new TextFieldWebControl(200, -1){
                    @Override
                    public void toWeb(Context ctx, PrintWriter out,
                            String name, Object obj)
                    {
                        super.toWeb(ctx, out, name, "Not Applicable. System Config 'MSISDN for All SPID -All Paid Type' is ON.");
                    }
                }) : 
                    super.getSpidWebControl();
    }

    @Override
    public WebControl getAdjustmentIdWebControl()
    {
        return CUSTOM_ADJUSTMENT_TYPE_WC;
    }

    public static final WebControl CUSTOM_ADJUSTMENT_TYPE_WC = new com.redknee.app.crm.web.control.AdjustmentTypeCheckPermissionProxyWebControl(
            new com.redknee.app.crm.web.control.AdjustmentTypeComboKeyWebControl(
                    com.redknee.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_READ_ONLY_HOME,
                    true, MsisdnGroupXInfo.ADJUSTMENT_TYPE_CATEGORY));
    public static final WebControl CUSTOM_FEE_WC = new CurrencyContextSetupWebControl(new XCurrencyWebControl(false));
}
