/*
 * Created on Apr 1, 2003
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.bas.promotion.summary;

import java.util.Date;

import com.trilogy.app.crm.bean.PeriodEnum;
import com.trilogy.app.crm.bean.UsageReport;

/**
 * Calculate and Fill up information in Usage Report
 * 
 * @author kwong
 *
 */
public class PromotionCalculation
    extends SummarySupport 
{
    private UsageReport         report_; 
    
	public PromotionCalculation(int spid, String subID, String msisdn, String acct,
                                Date start, Date end, PeriodEnum period) {
        report_ = new UsageReport();
        report_.setSpid(spid);
        report_.setSubscriberID(subID);
        report_.setMsisdn(msisdn);
        report_.setBan(acct);
        report_.setStartDate(start);
        report_.setEndDate(end);
        report_.setUsagePeriod(period);
        
	}
    
    public int getSpid()
    {
        return report_.getSpid();
    }
    
    public String getSubscriberID()
    {
        return report_.getSubscriberID();
    }
    
    public String getMsisdn()
    {
        return report_.getMsisdn();
    }
    
    public String getBAN()
    {
        return report_.getBan();
    }
    
    public Date getStartDate()
    {
        return report_.getStartDate();
    }
    
    public Date getEndDate()
    {
        return report_.getEndDate();
    }
    
    public PeriodEnum getPeriod()
    {
        return report_.getUsagePeriod();
    }
    
    /**
     * Produce usage report extracted from the summaries 
     * 
     * @return UsageReport
     */
    public UsageReport generateReport()
    {
        extractTo(report_);
        return report_;
    }
    
    
}
