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
package com.trilogy.app.crm.dunning;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQDay;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Report with information on accounts to be dunned on a certain date.
 * 
 * @author Marcio Marques
 * @since 9.0
 * 
 */
public class DunningReport extends AbstractDunningReport implements Comparable
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public List<DunningReportRecord> getRecords()
    {
        return getRecords(getContext());
    }


    /**
     * {@inheritDoc}
     */
    public int getNumberOfRecords()
    {
//        if ((this.getRecords().size() == 0 && records_ == null))
//        {
            return super.getNumberOfRecords();
//        }
//        else
//        {
//            return this.getRecords().size();
//        }
    }


    /**
     * Reset the records of this report, forcing them to be reloaded from the database.
     */
    public void resetRecords()
    {
        records_ = null;
    }


    /**
     * Return the records in this report.
     * 
     * @param context
     * @return
     */
    public List<DunningReportRecord> getRecords(Context context)
    {
        if (records_ == null && context != null)
        {
            try
            {
                if(LogSupport.isDebugEnabled(context))
                {
                    LogSupport.debug(context, this, "FETCHING DunningReportRecords");
                }
                
                if(this.getStatus() == DunningReportStatusEnum.PROCESSED_INDEX || this.getStatus() == DunningReportStatusEnum.PROCESSING_INDEX  || this.getStatus() == DunningReportStatusEnum.REFRESHING_INDEX)
                {
                    records_ = new ArrayList<DunningReportRecord>(HomeSupportHelper.get(context).getBeans(
                            context,
                            DunningReportRecord.class,
                            new And().add(new EQDay(DunningReportRecordXInfo.REPORT_DATE, this.getReportDate()))
                                     .add(new EQ(DunningReportRecordXInfo.SPID, Integer.valueOf(this.getSpid())))
                                     .add(new Or().add(new EQ(DunningReportRecordXInfo.STATUS, DunningReportRecordStatusEnum.getByName(DunningReportStatusEnum.get((short)this.getStatus()).getName()).getIndex()))
                                     	.add(new EQ(DunningReportRecordXInfo.STATUS, DunningReportRecordStatusEnum.DISCARDED_INDEX)))
                                     .add(new EQ(DunningReportRecordXInfo.FORECASTED_LEVEL, this.getNextLevel()))
//                                     add OR for DunningReportRecordMatureState to be this.getStatus(get int value from enum) or empty if current status is pending
                                     , true,
                            DunningReportRecordXInfo.FORECASTED_LEVEL, DunningReportRecordXInfo.ACCOUNT_TYPE,
                            DunningReportRecordXInfo.BAN));
                    
                }
                else
                {
                    records_ = new ArrayList<DunningReportRecord>(HomeSupportHelper.get(context).getBeans(
                            context,
                            DunningReportRecord.class,
                            new And().add(new EQDay(DunningReportRecordXInfo.REPORT_DATE, this.getReportDate()))
                                     .add(new EQ(DunningReportRecordXInfo.SPID, Integer.valueOf(this.getSpid())))
                                     .add(new Or().add(new EQ(DunningReportRecordXInfo.RECORD_MATURITY, DunningReportRecordMatureStateEnum.getByName(DunningReportStatusEnum.get((short)this.getStatus()).getName()).getIndex()))
                                    		 .add(new EQ(DunningReportRecordXInfo.RECORD_MATURITY, DunningReportRecordMatureStateEnum.DISCARDED_INDEX)))
                                     .add(new EQ(DunningReportRecordXInfo.FORECASTED_LEVEL, this.getNextLevel()))
//                                     add OR for DunningReportRecordMatureState to be this.getStatus(get int value from enum) or empty if current status is pending
                                     , true,
                            DunningReportRecordXInfo.FORECASTED_LEVEL, DunningReportRecordXInfo.ACCOUNT_TYPE,
                            DunningReportRecordXInfo.BAN));
                        
                }
                
                if(LogSupport.isDebugEnabled(context))
                {
                    LogSupport.debug(context, this, "FETCHED DunningReportRecords size : " + records_);
                }
                
//                DunningReportRecordMatureStateEnum.getByName(DunningReportStatusEnum.get((short)this.getStatus()).getName()).getIndex();
            }
            catch (Exception e)
            {
                LogSupport.minor(context, this, "Unable to retrieve Dunning Report records for '"
                        + this.getReportDate() + " STATUS : " + this.getStatus()  + " Next Level : " + this.getNextLevel()  + " : " + e.getMessage(), e);
            }
        }
        if (records_ == null)
        {
            return new ArrayList<DunningReportRecord>();
        }
        
        setNumberOfRecords(records_.size());
        
        return records_;
    }


    /**
     * Increase the number of successfully processed records in this report.
     */
    public synchronized void increaseSuccessfullyProcessedRecords()
    {
        this.successfullyProcessedRecords_++;
    }


    /**
     * Increase the number of records in this report.
     */
    public synchronized void increaseNumberOfRecords()
    {
        this.numberOfRecords_++;
    }

    /**
     * Increase the number of records in this report.
     */
    public synchronized void decreaseNumberOfRecords()
    {
        this.numberOfRecords_--;
    }
    
    /**
     * Increase the number of failed to process records in this report.
     */
    public synchronized void increaseFailedToProcessRecords()
    {
        this.failedToProcessRecords_++;
    }
    
    
    /**
     * Checks if the Dunning Report license is enabled.
     * @param context
     * @return
     */
    public static boolean isDunningReportSupportEnabled(final Context context)
    {
        return (LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.DUNNING_REPORT_SUPPORT));
    }


    public Context getContext()
    {
        return context_;
    }


    public void setContext(Context context)
    {
        context_ = context;
    }

    private Context context_;

    @Override
    public int compareTo(Object o)
    {
        if (o == null)
        {
            return -1;
        }
        else if (o instanceof DunningReport)
        {
            DunningReport other = (DunningReport) o;
            if (this.getSpid()<other.getSpid())
            {
                return -1;
            }
            else if (this.getSpid()>other.getSpid())
            {
                return 1;
            }
            else if (this.getReportDate().before(other.getReportDate()))
            {
                return -1;
            }
            else if (this.getReportDate().after(other.getReportDate()))
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else
        {
            return 0;
        }
    }
}
