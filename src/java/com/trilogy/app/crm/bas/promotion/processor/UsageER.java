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
package com.trilogy.app.crm.bas.promotion.processor;

import com.trilogy.app.crm.bean.UsageReport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.util.time.Time;
import com.trilogy.framework.xlog.log.ERLogMsg;

/**
 * @author kwong
 *
 */
public class UsageER 
{    
    final static int EVENTRECORD_ID = 711;
    final static int EVENTRECORD_CLASS = 700;
    final static String EVENTRECORD_SID = "Usage Summary Event";
    
    //TODO log for strange reports
    protected static synchronized void log(Context ctx, UsageReport report)
    {      
        String[] params = 
            new String[]
            {                   
                report.getSubscriberID(),
                report.getMsisdn(),
                report.getBan(),
                String.valueOf(report.getUsagePeriod().getIndex()),
                String.valueOf(report.getDomVoiceMO()),
                String.valueOf(report.getDomVoiceMT()),
                String.valueOf(report.getInterVoice()),
                String.valueOf(report.getRoamVoice()),
                String.valueOf(report.getSms()),
                String.valueOf(report.getRoamSms()),
                String.valueOf(report.getWeb()),
                String.valueOf(report.getWap()),
                String.valueOf(report.getMms()),
                String.valueOf(report.getDownloadCount()),
                String.valueOf(report.getDomVoiceCharge()),
                String.valueOf(report.getInterVoiceCharge()),
                String.valueOf(report.getRoamVoiceCharge()),
                String.valueOf(report.getSmsCharge()),
                String.valueOf(report.getGprsCharge()),
                String.valueOf(report.getTotalCharge()),
                report.getStartDate().toString(),
                report.getEndDate().toString(),
                String.valueOf(report.getResultCode())
            };
        new ERLogMsg(EVENTRECORD_ID, EVENTRECORD_CLASS, 
                        EVENTRECORD_SID, report.getSpid(), 
                                params).log(ctx);
    }


}
