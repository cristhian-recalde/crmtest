/*
      AccountMsisdnComboWebControl

      Author: Lily Zou
      Date:   Sept 22, 2003
*/
// RESPONSE: Removed commented-out code. LZ
// INSPECTED: 24/09/2003 GEA
// REVIEW(codestyle): Convert tabs to spaces. GEA

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.filter.EitherPredicate;


/**  Custom WebControl for User's Account Number Input. Also has MSISDN field to retrieve Account Number from SubscriberHome in case Account Number has been forgotten. **/
public class AccountMsisdnComboWebControl
	extends PrimitiveWebControl
{
	public AccountMsisdnComboWebControl()
	{
	    msisdn_wc  = new TextFieldWebControl(20);
	    account_wc = new TextFieldWebControl(10);
	}

	public synchronized void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		int mode = ctx.getInt("MODE", DISPLAY_MODE);

		switch ( mode )
		{
			case CREATE_MODE:
			case EDIT_MODE:
				// obj should be BAN
                //  BAN first 
                account_wc.toWeb(ctx, out, name + "_AccountNum", obj);
                //  then MSISDN field used in case User don't remember Account Number
                out.print("<b>( or Mobile Number: ");
                out.print("&nbsp;");
                msisdn_wc.toWeb(ctx, out, name + "_Msisdn", "");
                out.print(")</b>");
				break;
			case DISPLAY_MODE:
			default:
				if ( obj != null )
				{
					out.print(obj.toString());
				}
				else
				{
					out.print("&nbsp;");
				}
		}
	}

	public synchronized Object fromWeb(Context ctx, ServletRequest req, String name)
	{
		try
		{
            // return account number(BAN) but if it is empty, check MSISDN field
            // instead and find corresponding Account number from SubscriberHome, 
            // fill in the account number field

            String account_num = null;
      
            if ( req.getParameter(name + "_AccountNum") != null  && req.getParameter(name + "_AccountNum" ).length() == 0)
            {
                String msisdn = (String) msisdn_wc.fromWeb(ctx, req, name + "_Msisdn");
            
                Home sub_home = (Home)ctx.get(SubscriberHome.class);

                String sql_clause = "msisdn = '" + msisdn + "'";
         
                Subscriber sub = (Subscriber)sub_home.find(ctx,new EitherPredicate(
                        new EQ(SubscriberXInfo.MSISDN, msisdn), 
                        sql_clause));

                if ( sub != null )
                {
                    account_num = sub.getBAN();
                }

                return account_num;
            }
            // REVIEW(cleanup): Remove commented-out code.  If it is needed later,
            // it is available in subversion. GEA
            return 
                ( req.getParameter(name +  "_AccountNum" ) != null )  ?  
                              account_wc.fromWeb(ctx,  req,  name + "_AccountNum") : null;
		}
		catch (Exception e)
		{
            e.printStackTrace();
			return null;
		}
	}

    protected WebControl msisdn_wc; 
	protected WebControl account_wc;
}
