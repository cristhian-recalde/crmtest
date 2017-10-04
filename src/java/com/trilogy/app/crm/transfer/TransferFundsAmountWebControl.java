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
package com.trilogy.app.crm.transfer;

import java.io.PrintWriter;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.context.Context;


/**
 * Provides a version of the XCurrencyWebControl that outputs a hidden field for
 * the value when in DISPLAY mode.
 * <p>
 * Unfortunately needed for the request servicer. The FinalWebControl does not
 * properly handle the XCurrencyWebControl because it fails to take into account
 * the currency formatting needed within the hidden field.
 *
 * @author gary.anderson@redknee.com
 */
class TransferFundsAmountWebControl
    extends XCurrencyWebControl
{

    /**
     * Gets a reusable instance of the TransferFundsAmountWebControl.
     *
     * @return A reusable instance of the TransferFundsAmountWebControl.
     */
    public static TransferFundsAmountWebControl instance()
    {
        return INSTANCE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        // Strategy: We always delegate. For DISPLAY, we also include a hidden
        // field. This hidden field is generated almost exactly the same as the
        // CREATE/EDIT input in XCurrencyWebControl, but with the "type" set to
        // "hidden". Since the fromWeb() does not consider mode, this works
        // well.

        super.toWeb(ctx, out, name, obj);

        final int mode = ctx.getInt("MODE", DISPLAY_MODE);
        if (mode == DISPLAY_MODE)
        {
            out.print("<input type=\"hidden\" name=\"" + name + "\" ");
            out.print("value=\"");
            if (obj != null)
            {
                final Currency currency = (Currency)ctx.get(Currency.class, Currency.DEFAULT);
                final long money = ((Number)obj).longValue();
                out.print(currency.formatValue(money));
            }
            out.print("\"");
            out.print(">");
        }
    }


    /**
     * A reusable instance.
     */
    private static final TransferFundsAmountWebControl INSTANCE = new TransferFundsAmountWebControl();
}
