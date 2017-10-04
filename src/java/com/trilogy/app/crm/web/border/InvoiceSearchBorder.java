/*
 *  InvoiceSearchBorder
 *
 *  Author : Gary Anderson
 *  Date   : 2003-11-14
 *  
 *  Copyright (c) 2003, Redknee
 *  All rights reserved.
 */
 
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.home.InvoicePredictionHome;
import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.framework.xhome.web.search.*;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xlog.log.*;

import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * A Custom SearchBorder for Invoices.
 *
 * This will be generated from an XGen template in the future but for now
 * I'm still experimenting with the design.  Also, some common helper classes
 * will be created for each Search type.
 *
 * Add this Border before the WebController, not as one of either its
 * Summary or Detail borders.
 *
 * @author Gary Anderson
 **/
public class InvoiceSearchBorder
    extends SearchBorder
{
    public InvoiceSearchBorder(Context context)
    {
        super(context, Invoice.class, new InvoiceSearchWebControl());
      
        // BAN
        addAgent(
            new ContextAgentProxy() {
                public void execute(Context ctx)
                    throws AgentException
                {
                    InvoiceSearch criteria = (InvoiceSearch)getCriteria(ctx);
                    
                    // If its blank input, skip the search so the message 
                    // telling the user to enter the search criteria is displayed
                    if (!criteria.getBAN().equals("") || !criteria.getMsisdn().equals(""))
                    {
                        String finalBan = findBan(ctx, criteria);
                        if (!finalBan.equals(""))
                        {
                           doSelect(
                                ctx,
                                new EQ(InvoiceXInfo.BAN, finalBan));
                        }
                    }
                    delegate(ctx);
                }
            });
    }

    /**
     * Fetches the BAN if only the BAN is provided or if only the MSISDN is provided. 
     * 
     * @param ctx Context
     * @param criteria User input
     * @return Sought after BAN
     */
    String findBan(Context ctx, InvoiceSearch criteria)
    {
       String ban = "";
       if (!criteria.getBAN().equals(""))
       {
          ban = criteria.getBAN();
       }
       // If user inputs only msisdn, fetch the ban from the msisdn
       else if (criteria.getBAN().equals("") && !criteria.getMsisdn().equals(""))
       {
          try
          {
             // Look up subscriber for msisdn
             Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, criteria.getMsisdn());
             if (sub != null)
             {
                ban = sub.getBAN();
             }
          }
          catch (HomeException e)
          {
             //nop - lookupSubscriberForMSISDN throws exception if MSISDN does not exist
             //throw new AgentException(e);
          }
       }
       return ban;
    }
}

