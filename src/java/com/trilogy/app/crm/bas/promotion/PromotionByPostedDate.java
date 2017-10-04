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
package com.trilogy.app.crm.bas.promotion;

import java.util.Date;

import com.trilogy.app.crm.bas.promotion.summary.*;
import com.trilogy.app.crm.support.MultiDbSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.support.DateUtil;

/**
 * @author kwong
 *
 */
public class PromotionByPostedDate 
    extends PromotionProxy
{
    private Date startDate_;    //exclusive
    private Date endDate_;      //inclusive
    
	public PromotionByPostedDate(Context ctx, PromotionBy delegate, Date start, Date end) {
        super(delegate, " posteddate >= "+ start.getTime() + " AND " +
                        "posteddate < " +  end.getTime() + " " );
        startDate_ = start;
        endDate_ = end;
    }

    public SummaryBundle generate()
        throws HomeException
    {
        SummaryBundle reports = super.generate();
        reports.setStartDate(startDate_);
        reports.setEndDate(endDate_);
        return reports;
    }

	protected Object joinClause(Object o1, Object o2) {
        return (String)o1 + " and " +(String)o2;
	}

}
