package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.bas.tps.CreationPreferenceEnum;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;


public class CreationPreferencePredicate implements Predicate
{
    private PropertyInfo billCycleProperty_;

    public CreationPreferencePredicate(PropertyInfo billCycleProperty)
    {
        billCycleProperty_ = billCycleProperty;
    }
    
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {

        CreationPreferenceEnum preference = (CreationPreferenceEnum)obj;
        Object obj1 = (Object)ctx.get(AbstractWebControl.BEAN);
        
        if (preference == CreationPreferenceEnum.MANDATORY)
        {

            if (billCycleProperty_ != null)
            {
                Integer billCycleId = (Integer) billCycleProperty_.get(obj1);
                if(billCycleId == -1)
                {
                    return false;
                }
            }
            return true;
            
        }
        else
        {
            return true;
        }
    }
}
