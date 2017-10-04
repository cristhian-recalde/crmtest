package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.ExternalCreditCheckHome;
import com.trilogy.app.crm.bean.ExternalCreditCheckXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

public class ExternalCreditCheckSearchPredicate implements Border {

	@Override
	public void service(Context ctx, HttpServletRequest req,
			HttpServletResponse res, RequestServicer delegate)
			throws ServletException, IOException {
		ctx = ctx.createSubContext();		
		Context session = Session.getSession(ctx);
		Account account = (Account) session.get(Account.class);

		if ( account != null )
		{
			Home creditCheckHome = (Home)ctx.get(ExternalCreditCheckHome.class);
			EQ whereClause = new EQ(ExternalCreditCheckXInfo.BAN, account.getBAN());
			creditCheckHome =  creditCheckHome.where(ctx, whereClause);
			ctx.put(ExternalCreditCheckHome.class, creditCheckHome);	
			ctx.put("isExternalCreditCheckSearchPredicateRequest", Boolean.TRUE);
		}
		delegate.service(ctx, req, res);
	}

}
