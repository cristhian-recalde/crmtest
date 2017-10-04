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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Output web control used to output the description for adjustment types.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class AdjustmentTypeDescriptionOutputWebControl implements OutputWebControl
{
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        try {
            AdjustmentType bean = (AdjustmentType) obj;
            out.print(bean.getCode() + " - " + bean.getName());
        }
        catch (Exception e)
        {
            if (LogSupport.isDebugEnabled(ctx)) {
                new DebugLogMsg(this, "Error during output of AdjustmentType multi-select webcontrol. ", e).log(ctx); 
            } 
        }
    }
}
