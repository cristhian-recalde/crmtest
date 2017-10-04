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
package com.trilogy.app.crm.bas.promotion.home;

import java.util.Date;

import com.trilogy.framework.xhome.support.Command;

/**
 * @author kwong
 *
 */
public class SyncHistoryCmd implements Command 
{
    private Date startDate_;
    private Date endDate_;
    protected String where_;
    
    public SyncHistoryCmd(Date startDate, Date endDate ,String where)
    {
      where_=where;
      startDate_ = startDate;
      endDate_ = endDate;
    }
    
    public Date getStartDate()
    {
        return startDate_;
    }
    
    public Date getEndDate()
    {
        return endDate_;
    }

    public String getWhere()
    {
    	return where_;
    }
    
    public void setWhere(String where)
    {
    	this.where_=where;
    }
}
