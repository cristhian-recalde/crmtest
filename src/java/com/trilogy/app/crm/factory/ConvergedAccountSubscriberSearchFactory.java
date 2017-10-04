package com.trilogy.app.crm.factory;

import java.security.Principal;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberSearch;
import com.trilogy.app.crm.bean.SearchTypeEnum;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public class ConvergedAccountSubscriberSearchFactory implements ContextFactory{

	
    public Object create(Context ctx) {
        ConvergedAccountSubscriberSearch conAcctSub    = new ConvergedAccountSubscriberSearch();
        //code to get Default search type from user->spid->type
        //set search type for this.
        Principal principal = (Principal) ctx.get(Principal.class);
        User user = (User) principal;
        Home spidHome = (Home) ctx.get(CRMSpidHome.class);

        CRMSpid spid = null;
       try
       {
           spid = (CRMSpid) spidHome.find(ctx, Integer.valueOf(user.getSpid()));

}
       catch (HomeException exception)
       {
           new MajorLogMsg(this, " HomeException thrown during CMRSpid look-up from User.", exception)
               .log(ctx);
           conAcctSub.setType(SearchTypeEnum.Account);
           return conAcctSub;
       }
        
        if (spid != null)
        {
          conAcctSub.setType(spid.getDefaultSearchType());
        }  
        else
        {
          conAcctSub.setType(SearchTypeEnum.Account);  //If there are no SPID's configured(case with fresh deployments) then Search Type 
          //is on "Account"
        }
        return conAcctSub;
     }

}
