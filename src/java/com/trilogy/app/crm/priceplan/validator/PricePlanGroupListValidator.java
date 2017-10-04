/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.priceplan.validator;

import java.util.HashSet;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.DependencyGroupXInfo;
import com.trilogy.app.crm.bean.PricePlanGroup;
import com.trilogy.app.crm.bean.PricePlanGroupHome;

public class PricePlanGroupListValidator implements Validator 
{
    public void validate(Context ctx, Object obj) throws IllegalStateException 
    {
        PricePlanGroup ppg = (PricePlanGroup) obj;

        if (ppg.getParentPPG()==ppg.getIdentifier())
        {
            if (ppg.getParentPPG()!=-1)
                throw new IllegalStateException("PricePlan Group List: Cannot contain Parent PPG as itself");

        }
        else
        {
            if (ppg.getParentPPG()!=-1)
            {
                try {
                    PricePlanGroup tmp_ppg  = ppg;
                    HashSet set = new HashSet();
                    if (ppg.getIdentifier()!=-1)    set.add(Long.valueOf(ppg.getIdentifier()));
                    while (tmp_ppg.getParentPPG()!=-1)
                    {
                        Home ppg_home  =  (Home) ctx.get(PricePlanGroupHome.class);            
                        tmp_ppg = (PricePlanGroup) ppg_home.find(ctx,
                                new EQ(DependencyGroupXInfo.IDENTIFIER, Long.valueOf(tmp_ppg.getParentPPG())));
                        if (set.contains(Long.valueOf(tmp_ppg==null?-2:tmp_ppg.getIdentifier())))
                        {
                            throw new IllegalStateException("PricePlan Group List: The parent you have attached will result Circular list, Please attach another Parent PPG");
                        }
                        else
                        {
                            set.add(Long.valueOf(tmp_ppg==null?-2:tmp_ppg.getIdentifier()));
                        }
                        if (tmp_ppg==null) return;
                    }
                }
                catch (HomeException he)
                {
                    he.printStackTrace();
                }
            }
        }
    }

}
