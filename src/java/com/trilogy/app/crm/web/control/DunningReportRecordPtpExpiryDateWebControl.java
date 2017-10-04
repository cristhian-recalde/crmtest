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
package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;


/**
 * DateWebControl for the dunning report record PTP Expiry field. It only displays the
 * field if the dunning report record is being moved to PTP.
 * 
 * @author mcmarques
 * 
 */
public class DunningReportRecordPtpExpiryDateWebControl extends DateWebControl
{

    /**
     * Creates a new DunningReportRecordPtpExpiryDateWebControl object.
     */
    public DunningReportRecordPtpExpiryDateWebControl()
    {
        super();
    }


    /**
     * {@inheritDoc}
     */
    public void toWeb(Context ctx, java.io.PrintWriter out, String name, Object obj)
    {
        Object record = ctx.get(AbstractWebControl.BEAN);
        if (record == null || record instanceof DunningReportRecord && ((DunningReportRecord) record).isMoveToPTP())
        {
            super.toWeb(ctx, out, name, obj);
        }
    }
}
