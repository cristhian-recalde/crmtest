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
import com.trilogy.framework.xhome.home.HomeException;


/**
 * @author skularajasingham
 *
 */
public class DependencyGroupList {

    Context ctx = null;
    PricePlanGroup ppg = null;
    HashMap depend_id_map = null;

    ArrayList xor_arr = new ArrayList();
    ArrayList and_arr = new ArrayList();


    public DependencyGroupList(Context ctx, PricePlanGroup ppg, HashMap depend_id_map) throws HomeException
    {
        this.ctx=ctx;
        this.ppg=ppg;

        if (depend_id_map==null) this.depend_id_map= new HashMap();
        else
            this.depend_id_map=depend_id_map;

        this.loadDependentList();
    }


    private void loadDependentList() throws HomeException
    {
        if (ppg==null) return;    
        String depend_str = ppg.getDepend_group_list();
        StringTokenizer st = new StringTokenizer(depend_str,",");

        while (st.hasMoreTokens()) {
            String str=st.nextToken();
            if (!this.depend_id_map.containsKey(str))
            {
                DependencyRec dprec = new DependencyRec(this.ctx,str);
                this.depend_id_map.put(str, dprec);

                if (dprec.rec_enum.equals(DependencyGroupTypeEnum.XOR))             
                    this.xor_arr.add(dprec.getList());
                else
                    if (dprec.rec_enum.equals(DependencyGroupTypeEnum.AND))
                        this.and_arr.add(dprec.getList());             
            }
        }              

    }

    public ArrayList getXORList()
    {    
        return this.xor_arr;    
    }

    public ArrayList getANDList()
    {    
        return this.and_arr;    
    }


    private void loadPreReqList() throws HomeException
    {

    }

    public HashMap getCachedDependIds()
    {
        return this.depend_id_map;    
    }

}
