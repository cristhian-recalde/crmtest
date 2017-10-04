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
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Handler for usage report
 * 
 * @author kwong
 *
 */
abstract public class ReportHandler 
    extends ContextAwareSupport
{

    protected UsageReport report_;
    protected ReportHandler delegate_ = null;
    
    protected UsageReport getReport()
    {
        return report_;
    }
    
    public void setReport(UsageReport report)
    {
        report_ = report;
        if (getDelegate() != null)
        {
            getDelegate().setReport(report);
        }
    }
    
    protected ReportHandler getDelegate()
    {
        return delegate_;
    }
    
    protected void setDelegate(ReportHandler delegate)
    {
        if (delegate != null)
        {    
            delegate_ = delegate;
        }
    }
    
    /** 
     *  Handling of usage report 
     *
     */
    abstract protected void handling() 
        throws HandlerException, HomeException;
    
    public void process()
        throws HandlerException, HomeException
    {
         handling();
         if (getDelegate() != null)
         {
             getDelegate().handling();
         }
    }

}
