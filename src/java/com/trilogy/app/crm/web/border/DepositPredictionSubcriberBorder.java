package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.DepositHome;
import com.trilogy.app.crm.bean.DepositXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

public class DepositPredictionSubcriberBorder implements Border {

	public DepositPredictionSubcriberBorder(Context ctx) {
	}

	public void service(Context ctx, HttpServletRequest req,
			HttpServletResponse res, RequestServicer delegate)
			throws ServletException, IOException {
		ctx = ctx.createSubContext();
		Context session = Session.getSession(ctx);
		Subscriber subscriber = (Subscriber) session.get(Subscriber.class);
		//LogSupport.info(ctx, this,"Subscriber :"+subscriber.getId());
		if (subscriber != null) {
			Home depositHome = (Home) ctx.get(DepositHome.class);
			EQ whereClause = new EQ(DepositXInfo.SUBSCRIPTION_ID,subscriber.getId());
			depositHome = depositHome.where(ctx, whereClause);
			ctx.put(DepositHome.class, depositHome);
		}
		delegate.service(ctx, req, res);
	}
}
