package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.api.queryexecutor.ApiMethodKeyWebControl;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorSearch;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorXInfo;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class FilteredApiMethodKeyWebControl extends ApiMethodKeyWebControl
{
    public FilteredApiMethodKeyWebControl(boolean autoPreview)
    {
        super(autoPreview);
        init();
    }
    
    public FilteredApiMethodKeyWebControl(int listSize, boolean autoPreview, Object optionalValue)
    {
        super(listSize, autoPreview, optionalValue);
        init();
    }


    public Home getHome(Context ctx)
    {
        Object bean = ctx.get(AbstractWebControl.BEAN);
        Home home = super.getHome(ctx);
        if (bean instanceof ApiMethodQueryExecutor)
        {
            ApiMethodQueryExecutor apiMethodQueryExecutor = (ApiMethodQueryExecutor) bean; 
            home = home.where(ctx, new EQ(ApiMethodXInfo.API_INTERFACE, apiMethodQueryExecutor.getApiInterface()));
            
            try
            {
                for (ApiMethodQueryExecutor saved : HomeSupportHelper.get(ctx).getBeans(ctx, ApiMethodQueryExecutor.class, new EQ(ApiMethodQueryExecutorXInfo.API_INTERFACE, apiMethodQueryExecutor.getApiInterface())))
                {
                    if (!saved.getApiMethod().equals(((ApiMethodQueryExecutor) bean).getApiMethod()))
                    {
                        home = home.where(ctx, new NEQ(ApiMethodXInfo.FULL_NAME, saved.getApiMethod()));
                    }
                }
            }
            catch (HomeException e)
            {
                //Ignored
            }
        }
        else if (bean instanceof ApiMethodQueryExecutorSearch)
        {
            ApiMethodQueryExecutorSearch apiMethodQueryExecutor = (ApiMethodQueryExecutorSearch) bean; 
            home = home.where(ctx, new EQ(ApiMethodXInfo.API_INTERFACE, apiMethodQueryExecutor.getApiInterface()));
        }
        
        return home;
    }

}
