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

package com.trilogy.app.crm.lnp;
import junit.framework.TestCase;

import com.trilogy.app.crm.bean.LNPBulkloadConfig;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;

import com.trilogy.service.lnp.*;
import com.trilogy.service.lnp.bean.*;

/**
 * @author jke
 */
public class TestLnpBulkLoadFileGenerationCronAgent extends TestCase
{
    public static final String APPLICATION_CONTEXT =
        TestLnpBulkLoadFileGenerationCronAgent.class.getName() + ".APPLICATION_CONTEXT";

    public static Context createDefaultContext()
    {
        final Context context = new ContextSupport();
        context.setName("JUnit Context");
        context.put(APPLICATION_CONTEXT, false);
        return context;
    }    

    public void testConfig(){
        
        LNPBulkloadConfig lnpconfig = new LNPBulkloadConfig();
        lnpconfig.setFieldValue_1("f_1");
        lnpconfig.setFieldValue_2("f_2");
        lnpconfig.setFieldValue_3("f_3");
        lnpconfig.setFieldValue_4("f_4");
        lnpconfig.setFieldValue_5("f_5");
        lnpconfig.setFieldValue_6("f_6");
        lnpconfig.setFieldValue_7("f_7");
        lnpconfig.setFieldValue_8("f_8");
        lnpconfig.setFieldValue_9("f_9");
        lnpconfig.setFieldValue_10("f_10");
        lnpconfig.setFieldValue_11("f_11");
        lnpconfig.setFieldValue_12("f_12");

        LnpBulkLoadFileGenerationCronAgent lnpbl = new LnpBulkLoadFileGenerationCronAgent();
        
        LNPBulkLoadSchema scheBean = new LNPBulkLoadSchema();
        scheBean.setSchemaValues("Created_Name", "Created_Comp", lnpconfig.getFileType(), lnpconfig.getTableName(), 3, "yyyyMMdd_hhmm");
 
        LNPBulkLoadData dataBean = new LNPBulkLoadData();
        
        lnpbl.setLnpConfigToSchema(lnpconfig, scheBean);
        lnpbl.setLnpConfigToData(lnpconfig, dataBean);
        
        lnpbl.setMsisdn(lnpconfig, dataBean, "21234567890");
        
        LnpBulkLoadFileGeneration lnpBulkloadGeneration = new LnpBulkLoadFileGeneration(createDefaultContext(), "c:/tmp/lnp","Prefix");
        lnpBulkloadGeneration.createLNPBulkLoadSchema1_3(scheBean);

        lnpBulkloadGeneration.printLNPBulkLoadData(scheBean, dataBean);
        
        lnpBulkloadGeneration.close(createDefaultContext());
        
        System.out.println("Index1: " + lnpconfig.getField_1().getIndex());
        System.out.println("Field1: " + lnpconfig.getField_1().toString());
        System.out.println("Field2: " + lnpconfig.getField_2().getIndex());
        System.out.println("Field3: " + lnpconfig.getField_3().getIndex());
        System.out.println("Field4: " + lnpconfig.getField_4().getIndex());
    
    }
    /*
    public void testExecute()
    {
        System.out.println("TestLnpBulkLoadFileGenerationCronAgent: will generate .sche and .data file in c:\\tmp\\lnp.");
        LnpBulkLoadFileGenerationCronAgent cagent = new LnpBulkLoadFileGenerationCronAgent();
        try{
            cagent.execute(createDefaultContext());
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    */
    
}
