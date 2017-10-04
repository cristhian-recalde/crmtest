package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoChildrenRemoveHome;

import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;


public class NotInUseAccountHome extends NoChildrenRemoveHome
{

    public NotInUseAccountHome(Context ctx, String errMsg, Home delegate)
    {
        super(ctx, AccountHome.class, errMsg, delegate);
    }

    @Override
    public Predicate getRelationshipPredicate(Object id)
    {
        return new EQ(AccountXInfo.TYPE, id);
        //return new RelationshipPredicate(id);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
		AccountCategory accType = (AccountCategory) obj;
		AccountCategory oldAccType =
		    AccountTypeSupportHelper.get(ctx).getTypedAccountType(ctx,
		        accType.getIdentifier());
		if (oldAccType != null
		    && (oldAccType.getCustomerType() != accType.getCustomerType()))
        {
            processChildren(ctx, obj);
        }   
        return super.store(ctx, obj);
    }

}
