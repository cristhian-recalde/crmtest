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
package com.trilogy.app.crm.validator;

import java.util.Date;

import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.DunningReportRecordXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * Validator responsible to check if the data in the dunning report records are valid.
 * @author Marcio Marques
 * @since 9.0
 *
 */
public class DunningReportRecordsValidator implements Validator
{

    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        if (obj instanceof DunningReport)
        {
            validateReport(ctx, (DunningReport) obj);
        }
        else if (obj instanceof DunningReportRecord)
        {
            validateRecord(ctx, ((DunningReportRecord) obj).getReportDate(), (DunningReportRecord) obj);
        }
    }
    
    /**
     * Validate a report.
     * @param ctx
     * @param report
     * @throws IllegalStateException
     */
    public void validateReport(Context ctx, DunningReport report) throws IllegalStateException
    {
        for (DunningReportRecord record : report.getRecords(null))
        {
            validateRecord(ctx, report.getReportDate(), record);
        }
    }

    /**
     * Validate a record.
     * @param ctx
     * @param reportDate
     * @param record
     * @throws IllegalStateException
     */
    public void validateRecord(Context ctx, Date reportDate, DunningReportRecord record) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        if (record.isMoveToPTP() && (record.getPtpExpiryDate() == null || 
                !record.getPtpExpiryDate().after(reportDate)))
        {
            cise.thrown(new IllegalPropertyArgumentException(DunningReportRecordXInfo.PTP_EXPIRY_DATE, "PTP expiry date should be a date after the report date"));
        }
        cise.throwAll();
    }
}
