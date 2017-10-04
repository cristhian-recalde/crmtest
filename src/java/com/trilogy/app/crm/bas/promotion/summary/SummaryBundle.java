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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.PeriodEnum;

/**
 *  Collection holds result for usage summary report 
 *  and search criteria
 *
 *  @author kwong
 *
 */
public class SummaryBundle {

    private Collection  reports_ ;
    private PeriodEnum  period_ = null;
    private Date        startDate_ = new Date();   
    private Date        endDate_ = new Date();     
    
	public SummaryBundle(Collection col) {
        reports_ = col;
	}
    
    public Collection getCollection()
    {
        return reports_;
    }

    public PeriodEnum getPeriod()
    {
        return period_;
    }

    public Date getStartDate()
    {
        return startDate_;
    }
    
    public Date getEndDate()
    {
        return endDate_;
    }
    
    public void setPeriod(PeriodEnum period)
    {
        period_ = period;
    }
    
    public void setStartDate(Date start)
    {
        startDate_ = start;
    }
    
    public void setEndDate(Date end)
    {
        endDate_ = end;
    }
    
}
