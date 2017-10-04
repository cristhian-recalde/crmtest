package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.Address;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class AddressIdentifierSettingHome extends HomeProxy {

	private static final long serialVersionUID = 1L;
	
	public AddressIdentifierSettingHome(final Context ctx, final Home delegate) 
	{
		super(ctx, delegate);
	}

	public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException 
	{
		Address address = (Address) obj;
		
		IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx, IdentifierEnum.ADDRESS_ID, 1L, 9223372036854775807L);
		
		long addressIdentifier = IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(ctx, IdentifierEnum.ADDRESS_ID, null);
		
		address.setAddressId(String.valueOf(addressIdentifier));
		
		return super.create(ctx, address);
	}
}
