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
import java.util.Set;
import java.util.StringTokenizer;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.DependencyGroup;
import com.trilogy.app.crm.bean.DependencyGroupHome;
import com.trilogy.app.crm.bean.DependencyGroupTypeEnum;
import com.trilogy.app.crm.bean.DependencyGroupXInfo;

/**
 * @author skularajasingham
 *
 */
public class DependencyRec {

    public DependencyGroupTypeEnum rec_enum;
    public Set list = new HashSet();
    public Context ctx = null;
    public String depend_id=null;
    public DependencyGroup dg = null;
    private String[] predicator_names = {"SELECT ONE OF","SELECT ALL OF","MATCH ANY OF"};
    private String[] predicator_rules_statement = {"HAS ONLY ONE OF","INCLUDES ALL OF","MATCHES SOME OF"};
    private String[] predicator_rules_action = {"INCLUDE ONLY ONE FROM","INCLUDE ALL OF","INCLUDE AT LEAST ONE FROM"};



    public DependencyRec(Context ctx,String depend_id) throws HomeException
    {
        this.ctx=ctx;
        this.depend_id=depend_id;
        Home depend_home  =  (Home) ctx.get(DependencyGroupHome.class);

        try
        {
            dg = (DependencyGroup) depend_home.find(ctx,
                    new EQ(DependencyGroupXInfo.IDENTIFIER, Long.parseLong(depend_id)));
        }
        catch (HomeException e)
        {
            throw new PricePlanValidationException(
                    "Error while searching for Dependency Group id=" + depend_id + " due to: " + e.getMessage(),
                    e);
        }
        if (dg!=null)
        {
            this.rec_enum=dg.getType();
            this.loadList();
        }
    }

    public Set loadList()
    {
        String service_set =  dg.getServicesset();
        StringBuilder str_list = new StringBuilder(service_set);
        String bundle_set = dg.getBundleserviceset();
        String aux_set = dg.getAuxSet();

        //Building string buffer and converting to that to a Set for Performance
        //If we use "b"+str, that reduces performance, Senthooran

        StringTokenizer st = new StringTokenizer(bundle_set,",");        
        while (st.hasMoreTokens()) {
            if (str_list.length()>0) str_list.append(",");
            str_list.append("b");
            str_list.append(st.nextToken());
        }         

        st = new StringTokenizer(aux_set,",");        
        while (st.hasMoreTokens()) {
            if (str_list.length()>0) str_list.append(",");
            str_list.append("a");
            str_list.append(st.nextToken());
        }         

        //Now convert the Buffer to Set

        st = new StringTokenizer(str_list.toString(),",");        
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());

        }         
        LogSupport.debug(ctx,this,"Returning list: "+list);
        return list;
    }

    public Set getList()
    {
        return this.list;
    }

    /**
     * @return The type of criteria violated in the prerequisite definition, for the GUI display. 
     */
    public String getPredicateStatement()
    {
        return this.predicator_rules_statement[rec_enum.getIndex()];
    }
    
    /**
     * @return The action needed to satisfy the criteria the prerequisite definition, for the GUI display.
     */
    public String getPredicateAction()
    {
        return this.predicator_rules_action[rec_enum.getIndex()];
    }
}
