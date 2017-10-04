package com.trilogy.app.crm.home;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.BeanNotFoundHomeException;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.xdb.AbstractXDBHome;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * This class is solely used by VRAFraud Poller to update the vraFraudProfile on
 * the Subscriber table. Reason behind this single field update is due to the
 * race condition between ER1051 and ER442 (which was overwriting the subscriber state)
 * We should always use the subscriber pipeline.  I made this decision based usage of the property vraFraudProfile
 * and amount effort required to fix this issue.  Best solution would be to have 1 ER for both.  However, 
 * currently that is not possible
 * 
 * @author ksivasubramaniam
 * 
 */
public class SubscriberVRAFraudSingleFieldUpdateHome extends HomeProxy {
	public SubscriberVRAFraudSingleFieldUpdateHome(Context ctx) {
		super(ctx);
	}

	/**
	 * perform an optional command
	 * 
	 * @param ctx
	 * @param arg
	 * @return Object
	 * @exception HomeException
	 * @exception HomeInternalException
	 */
	public Object cmd(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		XDB xdb = (XDB) ctx.get(XDB.class);

		final Subscriber bean = (Subscriber) obj;
		

		int count = xdb.execute(ctx, new XStatement() {

			public String createStatement(Context ctx) {
			    Home home=(Home) ctx.get(SubscriberHome .class);
			    String tableName = " SUBSCRIBER ";
			    try 
			    {
			        tableName = (String) home.cmd(ctx,AbstractXDBHome.TABLE_NAME);
			    }
			    catch (Exception ex)
			    {
			       new MinorLogMsg(this," Unable to find subscriberhome in context" , ex).log(ctx);
			    }
			    return "UPDATE " + tableName + " SET " + SubscriberXInfo.VRA_FRAUD_PROFILE.getSQLName() + " = ? WHERE " + 
			    SubscriberXInfo.ID.getSQLName() + " = ? ";
			}

			public void set(Context ctx, XPreparedStatement ps)
					throws SQLException {
				// if ( info_.equals(SubscriberXInfo.TECHNOLOGY))
				ps.setString(bean.getVraFraudProfile() ? "y" : "n");

				// WHERE
				ps.setString(bean.getId());
			}

		});

		if (count != 1) 
		{
			String msg = " Unable to update SUBSCRIBER SET vraFraudProfile = " + bean.getVraFraudProfile() + " where id = " + bean.getId();
			LogSupport.info(ctx, this , msg, null);
			throw new BeanNotFoundHomeException();
		}
		else
		{
			String msg = " UPDATE SUBSCRIBER SET vraFraudProfile = " + bean.getVraFraudProfile() + " where id = " + bean.getId();
			LogSupport.info(ctx, this , msg, null);
		}

		return bean;

	}

}
