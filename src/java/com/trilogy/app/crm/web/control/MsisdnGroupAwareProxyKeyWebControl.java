/*
 * Created on 2004-12-31
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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.MsisdnGroupAware;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

/**
 * @author jchen
 * 
 * A Proxy Key Webcontrol to filter msisdn belongs to certain group indicated in parent bean
 */
public class MsisdnGroupAwareProxyKeyWebControl extends AbstractKeyWebControl
{
	AbstractKeyWebControl delegate_ = null;

	public MsisdnGroupAwareProxyKeyWebControl(AbstractKeyWebControl delegate)
	{
		delegate_ = delegate;
	}

	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		Home home = (Home) ctx.get(delegate_.getHomeKey());
		MsisdnGroupAware bean = (MsisdnGroupAware) ctx.get(AbstractWebControl.BEAN);
		int msisdnGroupId = bean.getMsisdnGroup();

		Home filteredHome = home.where(ctx, new MsisdnByGroupIdPredicate(msisdnGroupId));

		Context subCtx = ctx.createSubContext();

		subCtx.put(delegate_.getHomeKey(), filteredHome);
		delegate_.toWeb(subCtx, out, name, obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redknee.framework.xhome.webcontrol.AbstractKeyWebControl#getHomeKey()
	 */
	public Object getHomeKey()
	{
		return delegate_.getHomeKey();
	}

	public String getDesc(Context ctx, Object bean)
	{
		return delegate_.getDesc(ctx, bean);
	}

	public IdentitySupport getIdentitySupport()
	{
		return delegate_.getIdentitySupport();
	}
}
