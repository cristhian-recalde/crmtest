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
package com.trilogy.app.crm.home;

import java.util.Set;

import com.trilogy.app.crm.bean.DiscountClassTemplateInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * Adapter to set the values of services, aux services, bundles and aux bundles
 * 
 * @author ankit.nagpal@redknee.com
 * since 9_7_2
 */
public class DiscountClassTemplateInfoAdapter implements Adapter {

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException {
        
        DiscountClassTemplateInfo discountClassTemplateInfo = (DiscountClassTemplateInfo) obj;
  
        //Adapt Services
        discountClassTemplateInfo.setServices(discountClassTemplateInfo.getSetOfServices());
        
        //Adapt Auxiliary Services
        discountClassTemplateInfo.setAuxiliaryService(discountClassTemplateInfo.getSetOfAux());
        
        
        //Adapt Bundle Services
        discountClassTemplateInfo.setServicebundle(discountClassTemplateInfo.getSetOfbundleservice());  
        
      //Adapt Aux Bundle Services
        discountClassTemplateInfo.setAuxbundle(discountClassTemplateInfo.getSetOfauxbundle());      
    
        return discountClassTemplateInfo;
       // return obj;
    }

    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object obj) throws HomeException {
        DiscountClassTemplateInfo discountClassTemplateInfo = (DiscountClassTemplateInfo) obj;
        //dependenygroup.setHashset("from unAdapt");
        
        //UnAdapt Services
        Set set = discountClassTemplateInfo.getServices();
        discountClassTemplateInfo.setSetOfServices(set);

        
        //UnAdapt Auxiliary Services
        Set auxset = discountClassTemplateInfo.getAuxiliaryService();
        discountClassTemplateInfo.setSetOfAux(auxset);
        
        
        //UnAdapt Bundle Services
        set = discountClassTemplateInfo.getServicebundle();
        discountClassTemplateInfo.setSetOfbundleservice(set);
        
          //UnAdapt Aux Bundle Services
        set = discountClassTemplateInfo.getAuxbundle();
        discountClassTemplateInfo.setSetOfauxbundle(set);
        
        return discountClassTemplateInfo;
    }

}

