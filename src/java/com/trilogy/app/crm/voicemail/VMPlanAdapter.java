package com.trilogy.app.crm.voicemail;

import com.trilogy.app.crm.bean.*;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

public class VMPlanAdapter implements Adapter
{
    /** Used by find and select methods. **/
    public Object adapt(Context _ctx,Object obj) throws HomeException
    {
       CrmVmPlan c_plan = (CrmVmPlan) obj;
       VMPlan plan = new VMPlan();

       plan.setVmPlanId(String.valueOf(c_plan.getId()));
       plan.setDescription(c_plan.getDescription());

       return plan;
    }

    /** Used by create and store methods. **/
    public Object unAdapt(Context _ctx,Object obj) throws HomeException
    {
       throw new HomeException("not supported");
    }
    
}