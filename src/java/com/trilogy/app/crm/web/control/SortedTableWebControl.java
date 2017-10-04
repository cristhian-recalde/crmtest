// INSPECTED: 06/10/03 LZOU

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.TreeSet;
import java.util.Collection;

import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.TableWebControl;
import com.trilogy.framework.xhome.context.Context;

public class SortedTableWebControl
	extends ProxyWebControl
{
	public SortedTableWebControl(TableWebControl delegate)
	{
		super(delegate);
	}

	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		Collection beans = (Collection)obj;
		TreeSet sortedBeans =  new TreeSet(beans);
		super.toWeb(ctx, out, name, sortedBeans);
	}
}
