package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.blacklist.BlackListSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import java.util.Iterator;
import java.util.List;

public class BanTextFieldWebControl extends ProxyWebControl
{
	public BanTextFieldWebControl( WebControl delegate)
	{
		super(delegate);
	}

	public void toWeb(Context ctx, java.io.PrintWriter out, String name, Object obj)
	{
		try
		{
			boolean isBlackListed = false;
			Account bean = (Account) ctx.get(AbstractWebControl.BEAN);
			List accountIdList = bean.getIdentificationList();
			if(null != accountIdList)
			{
			    Iterator i = accountIdList.iterator();
			    while(i.hasNext() && !isBlackListed)
			    {
			        AccountIdentification ai = (AccountIdentification)i.next();
			        isBlackListed = isBlackListed || validateGrayIdField(ctx, ai.getIdType(), ai.getIdNumber());
			    }
			}
			if(isBlackListed)
			{
				out.println("<font color=\"red\">");
			}	

			super.toWeb(ctx, out, name, obj);

			if(isBlackListed)
			{	
				out.println("</font>");
		    }
		}
		catch(Exception e)
		{
			//eat it
		}
	}

	//Highlight warning message if id is in black/grey list
	public boolean validateGrayIdField(Context ctx, int idType, String idNumber) 
	{
		try
		{				
			// nothing to validate
			if ((idNumber == null) || (idNumber.length() == 0))
				return false;
			if ( BlackListSupport.isIdInList(ctx, idType, idNumber) )
			{
				return true;
			}
			return false;	
		}
		catch(Exception exp)
		{
			return false;
		}
	}	

}
