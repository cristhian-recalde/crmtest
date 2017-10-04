package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorSearchWebControl;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorSearchXInfo;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;

public class ApiMethodQueryExecutorSearchBorder extends SearchBorder
{
    public ApiMethodQueryExecutorSearchBorder(Context ctx)
    {
        super(ctx, ApiMethodQueryExecutor.class, new ApiMethodQueryExecutorSearchWebControl());
        
        addAgent(new SelectSearchAgent(ApiMethodQueryExecutorXInfo.API_INTERFACE,ApiMethodQueryExecutorSearchXInfo.API_INTERFACE).addIgnore("--"));

        addAgent(new SelectSearchAgent(ApiMethodQueryExecutorXInfo.API_METHOD, ApiMethodQueryExecutorSearchXInfo.API_METHOD).addIgnore("--"));

    }
}
