package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.DebtCollectionAgencyHome;
import com.trilogy.app.crm.bean.DebtCollectionAgencyKeyWebControl;
import com.trilogy.app.crm.bean.DebtCollectionAgencyXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;


public class FilteredDebtCollectionAgencyKeyWebControl extends DebtCollectionAgencyKeyWebControl
{
    public FilteredDebtCollectionAgencyKeyWebControl(int listSize, boolean autoPreview, boolean isOptional)
    {
        super(listSize, autoPreview, isOptional);
    }
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Context subContext = filterHome(ctx);
        super.toWeb(subContext, out, name, obj);
    }

    public Context filterHome(Context context)
    {
        Context subCtx = context.createSubContext();
        Object obj = context.get(AbstractWebControl.BEAN);
        
        Or filter = new Or();
        filter.add(new EQ(DebtCollectionAgencyXInfo.ENABLED, Boolean.TRUE));

        if (obj instanceof Account)
        {
            Account account = (Account) obj;
            filter.add(new EQ(DebtCollectionAgencyXInfo.ID, Long.valueOf(account.getDebtCollectionAgencyId())));
        }            
        
        Home home = (Home) subCtx.get(DebtCollectionAgencyHome.class);
        home = home.where(subCtx, filter);
        subCtx.put(DebtCollectionAgencyHome.class, home);

        return subCtx;
    }    
}
