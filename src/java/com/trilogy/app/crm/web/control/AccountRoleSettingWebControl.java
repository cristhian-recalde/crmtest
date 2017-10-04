package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

public class AccountRoleSettingWebControl extends ProxyWebControl
{

	public AccountRoleSettingWebControl(WebControl delegate)
	{
		super(delegate);
	}

	@Override
	public void fromWeb(Context ctx, Object p1,
	    javax.servlet.ServletRequest p2, String p3)
	{
		setAccountRole(ctx);
		super.fromWeb(ctx, p1, p2, p3);
	}

	@Override
	public Object fromWeb(Context ctx, javax.servlet.ServletRequest p1,
	    String p2)
	{
		setAccountRole(ctx);
		Object result = super.fromWeb(ctx, p1, p2);
		return result;
	}

	private void setAccountRole(final Context ctx)
	{
        
		Object bean = ctx.get(AbstractWebControl.BEAN);
		Account acct = (Account) bean;
		if (CREATE_MODE == ctx.getInt("MODE", OutputWebControl.DISPLAY_MODE)) {

			if (acct.getParentBAN() != null && acct.getParentBAN().length() > 0) {
				// this code will set the value to the role property so it is
				// properly displayed on GUI.
				// the values is the lost during fromWeb() because there is no
				// holder on the web page.
				// the property will be later set to the same value in
				// AccountHierachySyncHome
				Account parent = null;
				try {
					parent = HomeSupportHelper.get(ctx).findBean(ctx,
							Account.class, acct.getParentBAN());
				} catch (HomeException e) {
				}

				if (parent != null) {
					acct.setRole(parent.getRole());
				}
			} else {
				if (acct.getRole() == Account.DEFAULT_ROLE) {
					// Default subscriber;
					acct.setRole(1);
				}
			}
			
		}
	}
	
}
