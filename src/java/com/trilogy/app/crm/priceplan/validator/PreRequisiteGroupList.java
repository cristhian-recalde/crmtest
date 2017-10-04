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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.trilogy.app.crm.bean.*;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * @author skularajasingham
 *
 */
public class PreRequisiteGroupList {


    Context ctx = null;
    PricePlanGroup ppg = null;
    PricePlanGroupList callback = null;



    public PreRequisiteGroupList(Context ctx, PricePlanGroup ppg, PricePlanGroupList callback) throws HomeException
    {
        this.ctx=ctx;
        this.ppg=ppg;
        this.callback=callback;
        if (callback.cached_preq_ids==null) callback.cached_preq_ids = new HashMap();
        if (callback.cached_depend_ids==null) callback.cached_depend_ids = new HashMap();
        if (this.callback.PREREQ_List_if==null) this.callback.PREREQ_List_if = new ArrayList();
        if (this.callback.PREREQ_List_then==null) this.callback.PREREQ_List_then = new ArrayList();

        this.loadDependentList();
    }

    private void loadDependentList() throws HomeException
    {
        String prereq_str = ppg.getPrereq_group_list();
        StringTokenizer st = new StringTokenizer(prereq_str,",");

        while (st.hasMoreTokens()) {
            String str=st.nextToken();
            if (str!=null)
            {
                PrerequisiteGroup preqgroup = null;
                PreRequisiteGroupRec rec = (PreRequisiteGroupRec) this.callback.cached_preq_ids.get(str);
                //Try to get it from cache, avoid loading from the database, Senthooran
                if (rec==null){     
                    Home preqhome  =  (Home) ctx.get(PrerequisiteGroupHome.class);        
                    preqgroup = (PrerequisiteGroup) preqhome.find(ctx,new EQ(PrerequisiteGroupXInfo.IDENTIFIER,Long.parseLong(str)));

                    rec =  new PreRequisiteGroupRec(preqgroup.getPrereq_service(),preqgroup.getDependency_list()); 
                    this.callback.cached_preq_ids.put(str, rec);
                }

                if(rec != null)
                {
                //Check IF_SET Dependency  exists in the cache
                if (!this.callback.cached_depend_ids.containsKey(rec.getPreqService()))
                {
                    DependencyRec dprec = new DependencyRec(this.ctx,rec.getPreqService());                
                    this.callback.cached_depend_ids.put(rec.getPreqService(), dprec);

                }

                //Check THEN_SET Dependency exist in the cache
                if (!this.callback.cached_depend_ids.containsKey(rec.getPreqDependency()))
                {
                    DependencyRec dprec = new DependencyRec(this.ctx,rec.getPreqDependency());                
                    this.callback.cached_depend_ids.put(rec.getPreqDependency(), dprec);

                } 

                //Add the pairs together
                this.callback.PREREQ_List_if.add(rec.getPreqService());            
                this.callback.PREREQ_List_then.add(rec.getPreqDependency());
                }
            }
        }              

    }


}
