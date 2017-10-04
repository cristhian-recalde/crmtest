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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.service.lnp.bean.LNPBulkLoadData;
import com.trilogy.service.lnp.bean.LNPBulkLoadSchema;
import com.trilogy.service.lnp.bean.LnpSchemaFieldsEnum;
import com.trilogy.service.lnp.LnpBulkLoadFileGeneration;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.visitor.RowCountVisitor;
import com.trilogy.framework.xhome.auth.bean.User;


/**
 * @author jke
 */
public class LnpBulkLoadFileGenerationCronAgent implements ContextAgent
{
    public void execute(Context ctx) throws AgentException
    {
        LNPBulkloadConfig lnpconfig = (LNPBulkloadConfig) ctx.get(LNPBulkloadConfig.class);
        Home msisdnHome = (Home) ctx.get(MsisdnHome.class);
        if (msisdnHome == null)
        {
            throw new AgentException("System error: MsisdnHome not found in context");
        }
        // Provisioning
        if (LnpProvisioningEnum.REQUIRED.equals(lnpconfig.getLnpProvisioning()))
        {
            try
            {   
                XDB xdb = (XDB)ctx.get(XDB.class);
                RowCountVisitor visitor = new RowCountVisitor("SUB_COUNT");
                
                xdb.find(ctx, visitor,new SimpleXStatement("Select count(*) as SUB_COUNT from msisdn where state=" + MsisdnStateEnum.IN_USE_INDEX + " and lnpRequired=" + LnpReqirementEnum.REQUIRED_INDEX));
                
                if(visitor.getCount()>0)
                {
                
	                LNPBulkLoadSchema scheBean = new LNPBulkLoadSchema();
	                scheBean.setSchemaValues("", "Redknee Inc.", lnpconfig.getFileType(), lnpconfig
	                        .getTableName(), visitor.getCount(), lnpconfig.getDateFormat());
	                LNPBulkLoadData dataBean = new LNPBulkLoadData();
	                setLnpConfigToSchema(lnpconfig, scheBean);			                              
	                setLnpConfigToData(lnpconfig, dataBean);
	                
	                ctx =  ctx.createSubContext();
	                ctx.put(LNPBulkLoadSchema.class,scheBean);
	                ctx.put(LNPBulkLoadData.class,dataBean);
	                ctx.put(LNPBulkloadConfig.class,lnpconfig);
	
	                LnpBulkLoadFileGeneration lnpBulkloadGeneration = new LnpBulkLoadFileGeneration(ctx, lnpconfig
	                        .getLnpDirectory(), lnpconfig.getFileNamePrefix());
	                lnpBulkloadGeneration.createLNPBulkLoadSchema1_3(scheBean);
	                
	                ctx.put(LnpBulkLoadFileGeneration.class,lnpBulkloadGeneration);
                
                
	                msisdnHome.where(ctx,new EQ(MsisdnXInfo.STATE,MsisdnStateEnum.IN_USE))
	                          .where(ctx, new EQ(MsisdnXInfo.LNP_REQUIRED,LnpReqirementEnum.REQUIRED))
	                          .forEach(ctx, new CloneingVisitor(new HomeVisitor(msisdnHome)
	                                      {		                              
	                                          public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
	                                          {                                            
	                                              
	                                              Msisdn msisdn = (Msisdn)obj;
	                                              
	                                              LNPBulkLoadSchema scheBean = (LNPBulkLoadSchema)ctx.get(LNPBulkLoadSchema.class);
	                                              LNPBulkLoadData dataBean = (LNPBulkLoadData)ctx.get(LNPBulkLoadData.class);
	                                              LNPBulkloadConfig lnpconfig = (LNPBulkloadConfig)ctx.get(LNPBulkloadConfig.class);
	                                              LnpBulkLoadFileGeneration lnpBulkloadGeneration = (LnpBulkLoadFileGeneration)ctx.get(LnpBulkLoadFileGeneration.class);
	                                              
	                                              //Set msisdn into dataBean.
	                                              setMsisdn(lnpconfig, dataBean, msisdn.getMsisdn());
	                                              //Print data into data file.
	                                              lnpBulkloadGeneration.printLNPBulkLoadData(scheBean, dataBean);
	                                              //Change msisdn LNP Required state to NOT_REQUIRED
	                                              try
	                                              {
	                                                  msisdn.setLnpRequired(LnpReqirementEnum.NOT_REQUIRED);
	                                                  getHome().store(ctx, msisdn);
	                                              }
	                                              catch (Exception e)
	                                              {
	                                                  new MajorLogMsg(this, "Fail to set LnpRequired to No for MSISDN [ msisdn="
	                                                          + msisdn.getMsisdn() + " ]", e).log(ctx);
	                                              }
	                                              
	                                          }
	                                      }));   
		               
	                    lnpBulkloadGeneration.close(ctx);
                }
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "Encountered problem when seleting held entries from MSISDNHome.", e).log(ctx);
            }
            
        }
      /*  
        // DeProvisioning
        if (LnpProvisioningEnum.REQUIRED.equals(lnpconfig.getLnpDeprovisioning()))
        {
            try
            {
                //  find out msisdn by MsisdnStateEnum.HELD and LnpReqirementEnum.REQUIRED
                final String sqlQuery = "STATE=" + MsisdnStateEnum.HELD_INDEX + " AND LNPREQUIRED="
                        + LnpReqirementEnum.REQUIRED_INDEX;
                Collection allEntry = msisdnHome.select(ctx, Logic.Either(new Predicate()
                {

                    public boolean f(Context _ctx, Object obj)
                    {
                        Msisdn record = (Msisdn) obj;
                        System.out.println("record: " + record);
                        System.out.println("record.getState(): " + record.getState());
                        System.out.println("record.getLnpRequired(): " + record.getLnpRequired());
                        return MsisdnStateEnum.HELD.equals(record.getState())
                                && LnpReqirementEnum.REQUIRED.equals(record.getLnpRequired());
                    }
                }, sqlQuery));
                /*
                 * TODO: Get user and company info. final User principal =
                 * (User)ctx.get(User.class);
                 */
        /*        if (allEntry != null && allEntry.size() > 0)
                {
                    LNPBulkLoadSchema scheBean = new LNPBulkLoadSchema();
                    scheBean.setSchemaValues("Created_Name", "Created_Comp", lnpconfig.getFileType(), lnpconfig
                            .getTableName(), allEntry.size(), lnpconfig.getDateFormat());
                    LNPBulkLoadData dataBean = new LNPBulkLoadData();
                    setLnpConfigToSchema(lnpconfig, scheBean);
                    setLnpConfigToData(lnpconfig, dataBean);
                    LnpBulkLoadFileGeneration lnpBulkloadGeneration = new LnpBulkLoadFileGeneration(ctx, lnpconfig
                            .getLnpDirectory(), lnpconfig.getFileNamePrefix());
                    lnpBulkloadGeneration.createLNPBulkLoadSchema1_3(scheBean);
                    for (Iterator i = allEntry.iterator(); i.hasNext();)
                    {
                        Msisdn msisdn = (Msisdn) i.next();
                        //Set msisdn into dataBean.
                        setMsisdn(lnpconfig, dataBean, msisdn.getMsisdn());
                        //                      //Print data into data file.
                        lnpBulkloadGeneration.printLNPBulkLoadData(scheBean, dataBean);
                        //Change msisdn LNP Required state to NOT_REQUIRED
                        try
                        {
                            msisdn.setLnpRequired(LnpReqirementEnum.NOT_REQUIRED);
                            msisdnHome.store(ctx, msisdn);
                        }
                        catch (Exception e)
                        {
                            new MajorLogMsg(this, "Fail to set LnpRequired to No for MSISDN [ msisdn="
                                    + msisdn.getMsisdn() + " ]", e).log(ctx);
                        }
                    }
                    lnpBulkloadGeneration.close(ctx);
                }
            }
            catch (Exception e)
            {
                System.out.println("HomeException: " + e);
                new MinorLogMsg(this, "Encountered problem when seleting held entries from MSISDNHome.", e).log(ctx);
            }
        }*/
    }


    /**
     * Set LNP Config into scheBean
     * 
     * @param lnpconfig
     *            The LNP Config.
     * @param scheBean
     *            LNPBulkLoadSchema bean
     */
    public void setLnpConfigToSchema(LNPBulkloadConfig lnpconfig, LNPBulkLoadSchema scheBean)
    {
        //      scheBean.setFile_create_ts_value("");
        //      scheBean.setFile_type_value(lnpconfig.getFileType());
        scheBean.setFieldDelimiter_value(lnpconfig.getFieldDelimiter());
        scheBean.setLeadDelimiter_value(lnpconfig.getLeadDelimiter());
        scheBean.setRecordDelimiter_value(lnpconfig.getRecordDelimiter());
        //      scheBean.setTableName_value(lnpconfig.getTableName());
        int numberOfFields = 0;
        if (!lnpconfig.getField_1().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_1(lnpconfig.getField_1().toString());
        if (!lnpconfig.getField_2().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_2(lnpconfig.getField_2().toString());
        if (!lnpconfig.getField_3().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_3(lnpconfig.getField_3().toString());
        if (!lnpconfig.getField_4().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_4(lnpconfig.getField_4().toString());
        if (!lnpconfig.getField_5().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_5(lnpconfig.getField_5().toString());
        if (!lnpconfig.getField_6().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_6(lnpconfig.getField_6().toString());
        if (!lnpconfig.getField_7().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_7(lnpconfig.getField_7().toString());
        if (!lnpconfig.getField_8().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_8(lnpconfig.getField_8().toString());
        if (!lnpconfig.getField_9().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_9(lnpconfig.getField_9().toString());
        if (!lnpconfig.getField_10().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_10(lnpconfig.getField_10().toString());
        if (!lnpconfig.getField_11().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_11(lnpconfig.getField_11().toString());
        if (!lnpconfig.getField_12().equals(LnpSchemaFieldsEnum.None))
            numberOfFields++;
        scheBean.setFieldValue_12(lnpconfig.getField_12().toString());
        // Set number of fields to schema bean
        scheBean.setNumberOfFields_value(numberOfFields);
    }


    /**
     * Set LNP Config into dataBean
     * 
     * @param lnpconfig
     *            The LNP Config.
     * @param dataBean
     *            LNPBulkLoadData bean
     */
    public void setLnpConfigToData(LNPBulkloadConfig lnpconfig, LNPBulkLoadData dataBean)
    {
        if (!lnpconfig.getField_1().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_1().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_1(lnpconfig.getFieldValue_1());
        }
        else
        {
            dataBean.setField_1("");
        }
        if (!lnpconfig.getField_2().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_2().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_2(lnpconfig.getFieldValue_2());
        }
        else
        {
            dataBean.setField_2("");
        }
        if (!lnpconfig.getField_3().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_3().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_3(lnpconfig.getFieldValue_3());
        }
        else
        {
            dataBean.setField_3("");
        }
        if (!lnpconfig.getField_4().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_4().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_4(lnpconfig.getFieldValue_4());
        }
        else
        {
            dataBean.setField_4("");
        }
        if (!lnpconfig.getField_5().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_5().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_5(lnpconfig.getFieldValue_5());
        }
        else
        {
            dataBean.setField_5("");
        }
        if (!lnpconfig.getField_6().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_6().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_6(lnpconfig.getFieldValue_6());
        }
        else
        {
            dataBean.setField_6("");
        }
        if (!lnpconfig.getField_7().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_7().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_7(lnpconfig.getFieldValue_7());
        }
        else
        {
            dataBean.setField_7("");
        }
        if (!lnpconfig.getField_8().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_8().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_8(lnpconfig.getFieldValue_8());
        }
        else
        {
            dataBean.setField_8("");
        }
        if (!lnpconfig.getField_9().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_9().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_9(lnpconfig.getFieldValue_9());
        }
        else
        {
            dataBean.setField_9("");
        }
        if (!lnpconfig.getField_10().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_10().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_10(lnpconfig.getFieldValue_10());
        }
        else
        {
            dataBean.setField_10("");
        }
        if (!lnpconfig.getField_11().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_11().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_11(lnpconfig.getFieldValue_11());
        }
        else
        {
            dataBean.setField_11("");
        }
        if (!lnpconfig.getField_12().equals(LnpSchemaFieldsEnum.None)
                && !lnpconfig.getField_12().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_12(lnpconfig.getFieldValue_12());
        }
        else
        {
            dataBean.setField_12("");
        }
    }


    /**
     * Set msisdn to data bean. The lnpconfig is used to determined which field to save.
     */
    public void setMsisdn(LNPBulkloadConfig lnpconfig, LNPBulkLoadData dataBean, String msisdn)
    {
        if (lnpconfig.getField_1().getIndex() == LnpSchemaFieldsEnum.DirectoryNumber.getIndex())
        {
            dataBean.setField_1(msisdn);
        }
        else if (lnpconfig.getField_2().getIndex() == LnpSchemaFieldsEnum.DirectoryNumber.getIndex())
        {
            dataBean.setField_2(msisdn);
        }
        else if (lnpconfig.getField_3().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_3(msisdn);
        }
        else if (lnpconfig.getField_4().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_4(msisdn);
        }
        else if (lnpconfig.getField_5().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_5(msisdn);
        }
        else if (lnpconfig.getField_6().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_6(msisdn);
        }
        else if (lnpconfig.getField_7().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_7(msisdn);
        }
        else if (lnpconfig.getField_8().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_8(msisdn);
        }
        else if (lnpconfig.getField_9().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_9(msisdn);
        }
        else if (lnpconfig.getField_10().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_10(msisdn);
        }
        else if (lnpconfig.getField_11().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_11(msisdn);
        }
        else if (lnpconfig.getField_12().equals(LnpSchemaFieldsEnum.DirectoryNumber))
        {
            dataBean.setField_12(msisdn);
        }
    }
}
