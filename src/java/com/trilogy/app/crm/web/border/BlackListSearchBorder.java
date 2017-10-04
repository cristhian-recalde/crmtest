package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.BlackList;
import com.trilogy.app.crm.bean.BlackListSearch;
import com.trilogy.app.crm.bean.BlackListSearchWebControl;
import com.trilogy.app.crm.bean.BlackListSearchXInfo;
import com.trilogy.app.crm.bean.BlackListXInfo;


public class BlackListSearchBorder extends SearchBorder
{

    public BlackListSearchBorder(Context ctx)
    {
        super(ctx, BlackList.class, new BlackListSearchWebControl());
        // ID Type
        addAgent(new ContextAgentProxy()
        {

            @Override
            public void execute(Context ctx) throws AgentException
            {
                BlackListSearch criteria = (BlackListSearch) getCriteria(ctx);
                int idType = criteria.getIdType();
                if (idType != -1)
                {
                    doSelect(ctx, new EQ(BlackListXInfo.ID_TYPE, Integer.valueOf(idType)));
                }
                delegate(ctx);
            }
        });
        addAgent(new WildcardSelectSearchAgent(BlackListXInfo.ID_NUMBER, BlackListSearchXInfo.ID_NUMBER, true));
/*        
        addAgent(new PrePostWildcardSelectSearchAgent(BlackListXInfo.ID_NUMBER, BlackListSearchXInfo.ID_NUMBER));
*/        
    }
}
