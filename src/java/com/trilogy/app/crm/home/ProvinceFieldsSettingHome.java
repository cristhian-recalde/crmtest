package com.trilogy.app.crm.home;

import java.util.Collection;
import com.trilogy.app.crm.bean.Province;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.VisitorProxy;

public class ProvinceFieldsSettingHome
extends HomeProxy
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProvinceFieldsSettingHome(Home home)
	{
		super(home); 
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
	{
		Province province = (Province) obj;

		setProvincePrimaryKey(ctx, province);

		return super.create(ctx, province);
	}

	private void setProvincePrimaryKey(Context ctx, Province province)
	{
		province.setName(province.getSpid() + "-" + province.getDisplayName());
	}

	@Override
	public Object find(Context ctx, Object obj) throws HomeException,
	HomeInternalException {

		Province province = (Province) super.find(ctx, obj);
		if(province != null)
		{
			if(province.getDisplayName() == null || "".equals(province.getDisplayName()))
			{
				province.setDisplayName(province.getName());
			}
		}

		return province;
	}

	@Override
	public Collection select(Context ctx, Object obj) throws HomeException,
	HomeInternalException 
	{
		Collection coll =  super.select(ctx, obj);
		if(coll != null)
		{
			for(Object iterableObj :coll)
			{
				Province province = (Province) iterableObj;
				if(province != null)
				{
					if(province.getDisplayName() == null || "".equals(province.getDisplayName()))
					{
						province.setDisplayName(province.getName());
					}
				}
			}
		}
		return coll;
	}

	@Override
	public Visitor forEach(Context ctx, Visitor visitor, Object where)
	throws HomeException, HomeInternalException {
		return super.forEach(ctx, new ProvinceFieldSettingVisitor(visitor), where);
	}
}

class ProvinceFieldSettingVisitor extends VisitorProxy
{
	ProvinceFieldSettingVisitor(Visitor delegate)
	{
		super(delegate);
	}

	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
	AbortVisitException {
		Province province = (Province) obj;
		if(province != null)
		{
			if(province.getDisplayName() == null || "".equals(province.getDisplayName()))
			{
				province.setDisplayName(province.getName());
			}
		}
		super.visit(ctx, obj);
	}
}
