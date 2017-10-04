package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.*;

/**
 * This class is only used temporarily. Its functionality can be
 * replaced by an application of the OrHome
 */
public class TransientDataHome
	extends HomeProxy
{
	public TransientDataHome(Home delegate)
	{
		super(delegate);
	}

	public void add(Object obj)
	{
		data.add(obj);
	}

	public Collection select(Context ctx,Object where)
		throws HomeInternalException, HomeException
	{
		Collection records = super.select(ctx,where);
		data.addAll(records);
		return data;
	}

	private Collection data = new ArrayList();
}
