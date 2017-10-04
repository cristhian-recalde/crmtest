/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUILimitEnum;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIProperty;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * @author cindy.wong@redknee.com
 * 
 */
public class AdjustmentTypeEnhancedGUIPropertyLimitWebControl extends
        XCurrencyWebControl
{

    public AdjustmentTypeEnhancedGUIPropertyLimitWebControl()
    {
        super(false);
    }

    /**
     * Enable/disable changing limit option based on flag in bean.
     * 
     * @see com.redknee.framework.xhome.webcontrol.ProxyWebControl#getDelegate(com.redknee.framework.xhome.context.Context)
     */
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        AdjustmentTypeEnhancedGUIProperty bean = (AdjustmentTypeEnhancedGUIProperty) ctx
                .get(AbstractWebControl.BEAN);
        Currency currency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);

        int   mode   = ctx.getInt("MODE", DISPLAY_MODE);
        long  money  = ((Number) obj).longValue();

        switch (mode)
        {
           case CREATE_MODE:
           case EDIT_MODE:
              out.println("<input name=\"" + name + "\" size=\"14\" ");
              if (bean.getLimitSet() != AdjustmentTypeEnhancedGUILimitEnum.CUSTOM_INDEX)
              {
                  out.print("disabled=\"disabled\" ");
              }
              out.print("value=\"");
              if ( obj != null ) out.print(currency.formatValue(money));
              out.print("\"");
              out.print(">");

              if ( showCurrency_ )
              {
                 out.print("&nbsp;<b>");
                 out.print(currency.getCode());
                 out.print("</b>");
              }
              break;
           case DISPLAY_MODE:
           default:
              if ( obj != null ) out.print(currency.formatValue(money));
        }
    }

}
