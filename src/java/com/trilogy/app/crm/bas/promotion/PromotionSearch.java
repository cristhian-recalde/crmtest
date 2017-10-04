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

import java.util.Collection;

import com.trilogy.app.crm.bas.promotion.home.ReportCmd;
import com.trilogy.app.crm.bas.promotion.summary.SummaryBundle;
import com.trilogy.app.crm.bean.calldetail.CallDetailSummaryHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author kwong
 *
 */
public class PromotionSearch 
    extends ContextAwareSupport
    implements PromotionBy
{
    private String whereClause_ = null;

	public PromotionSearch(Context context) {
		
        super();
        setContext(context);
	}

	public SummaryBundle generate() 
        throws HomeException
    {

         Home callHome = (Home) getContext().get(CallDetailSummaryHome.class);    
         
         Collection result;
         if (getWhereClause() == null)
         {
             result = (Collection)callHome.cmd(getContext(),new ReportCmd(""));
         }
         else 
         {
             result = (Collection) callHome.cmd(getContext(),new ReportCmd("where "+getWhereClause()));  
         }
         
         if (LogSupport.isDebugEnabled(getContext()))
         {
            new DebugLogMsg(this, "getting usage report for "+ getWhereClause(), null).log(getContext());                
         }
         
         return new SummaryBundle(result);

	}

    public String getWhereClause(){
        return whereClause_;
    }
	public void setWhereClause(Object o) {
        whereClause_ = (String)o;	
	}

}
