/*
      AcctNumMsisdnEnsureWebControl
    
      Author: Lily Zou
      Date:   Sept 29, 2003
*/
// INSPECTED: 10/06/2003  GEA
package com.trilogy.app.crm.web.control;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.filter.EitherPredicate;


public class AcctNumMsisdnEnsureWebControl
	extends ProxyWebControl
{
	public AcctNumMsisdnEnsureWebControl(WebControl delegate)
	{
        super(delegate);
	}


	@Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
        throws IllegalPropertyArgumentException
	{
        //   ======   check list  ======
        // 1. MSISDN actually belongs to the Account
        // 2. if Account# has been filled up but MSISDN is empty, prompt them for it
        // 3. if MSISDN has been filled up but Account# is empty, go to subscriber home
        //    to retrieve account# and set it.
            
        Transaction trans = (Transaction)getDelegate().fromWeb(ctx, req, name );

        if ( trans != null )
        {
           // check 1,  both of them need to be filled
           if ( trans.getAcctNum().length() == 0 && trans.getMSISDN().length() == 0 )
           {
                throw new IllegalPropertyArgumentException("MSISDN", "Please fill up both Account Number and Mobile Number.");
           }
           // check 2,  has account num but no MSISDN 
           else if (trans.getAcctNum().length() > 0 && trans.getMSISDN().length() == 0 )
           {
                throw new IllegalPropertyArgumentException("MSISDN", "Please fill in Mobile Number field. ");
           }
           // check 3,  no account but has MSISDN
           else if (trans.getAcctNum().length() == 0 && trans.getMSISDN().length() > 0 )
           {
                String     account_num = null;
                
                String     msisdn      = trans.getMSISDN();
                Subscriber sub         = getSubByMSISDN(ctx, msisdn);

                if ( sub == null)
                {
                    throw new IllegalPropertyArgumentException("MSISDN", "Mobile Number is invalid. No subscriber has been found");
                }
                
                account_num = sub.getBAN();

                if ( account_num == null )
                {
                    throw new IllegalPropertyArgumentException("MSISDN", "Mobile Number is invalid. Wrong subscriber account number");
                }

                trans.setAcctNum(account_num);
           }
           // check 4, both exist, need to check if MSISDN actually belongs to this account
           else
           {
                String   account_num = trans.getAcctNum();
                String        msisdn = trans.getMSISDN();

                Subscriber sub       = getSubByMSISDN(ctx, msisdn);

                if ( sub == null || !sub.getBAN().equals(account_num) )
                {
                    throw new IllegalPropertyArgumentException("MSISDN", "Mobile Number is invalid.");
                }
           }
         }

         return trans;
	}

    private Subscriber getSubByMSISDN(Context ctx, String msisdn)
    {
        Subscriber     sub  = null;
        Home      sub_home  = (Home) ctx.get(SubscriberHome.class);
                
        String   sql_clause = "msisdn = '" + msisdn + "'";
        
        try
        {
            sub = (Subscriber)sub_home.find(ctx,new EitherPredicate(
                    new EQ(SubscriberXInfo.MSISDN, msisdn),
                    sql_clause));
        }
        catch(Exception e)
        {
            // REVIEW(traceability): Should this be looged?  GEA
            e.printStackTrace();

            return null;
        }

        return sub;
    }
}
