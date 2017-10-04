/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.web.search.LimitSearchAgent;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;

import com.trilogy.app.crm.bean.payment.PaymentException;
import com.trilogy.app.crm.bean.payment.PaymentExceptionSearch;
import com.trilogy.app.crm.bean.payment.PaymentExceptionSearchWebControl;
import com.trilogy.app.crm.bean.payment.PaymentExceptionSearchXInfo;
import com.trilogy.app.crm.bean.payment.PaymentExceptionXInfo;

public class PaymentExceptionSearchBorder extends SearchBorder 
{

    public PaymentExceptionSearchBorder(Context context)
    {
          super(context, PaymentException.class, new PaymentExceptionSearchWebControl());
        
          // BAN
          addAgent(new SelectSearchAgent(PaymentExceptionXInfo.BAN, PaymentExceptionSearchXInfo.BAN, false)

          );

          // MSISDN
          addAgent(new SelectSearchAgent(PaymentExceptionXInfo.MSISDN, PaymentExceptionSearchXInfo.MSISDN, false));

          // startDate
          addAgent(new ContextAgentProxy() {
             @Override
            public void execute(Context ctx)
                throws AgentException
             {
                PaymentExceptionSearch criteria = (PaymentExceptionSearch) getCriteria(ctx);

                if ( criteria.getExceptionStart() != null )
                {
                   doSelect(ctx, new GTE(PaymentExceptionXInfo.TRANS_DATE, criteria.getExceptionStart()));
                }

                delegate(ctx);
             }
          });
          
          // endDate
          addAgent(new ContextAgentProxy() {
             @Override
            public void execute(Context ctx)
                throws AgentException
             {
                PaymentExceptionSearch criteria = (PaymentExceptionSearch) getCriteria(ctx);

                if ( criteria.getExceptionEnd() != null )
                {
                   doSelect(ctx, new LTE(PaymentExceptionXInfo.TRANS_DATE, criteria.getExceptionEnd()));
                }

                delegate(ctx);
             }
          });
          
          // endDate
          addAgent(new ContextAgentProxy() {
             @Override
            public void execute(Context ctx)
                throws AgentException
             {
                PaymentExceptionSearch criteria = (PaymentExceptionSearch) getCriteria(ctx);

                if ( criteria.getExceptionEnd() != null )
                {
                   doSelect(ctx, new LTE(PaymentExceptionXInfo.TRANS_DATE, criteria.getExceptionEnd()));
                }

                delegate(ctx);
             }
          });
          
          // Limit
          addAgent(new LimitSearchAgent(PaymentExceptionSearchXInfo.LIMIT));
    }
}
