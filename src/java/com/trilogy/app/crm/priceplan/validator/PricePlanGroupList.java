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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.DependencyGroupTypeEnum;
import com.trilogy.app.crm.bean.PricePlanGroup;
import com.trilogy.app.crm.bean.PricePlanGroupHome;
import com.trilogy.app.crm.bean.PricePlanGroupXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.support.BundleSupportHelper;

/**
 * Main class for Price Plan Validation
 * @author skularajasingham
 *
 */
public class PricePlanGroupList {

    public static final int PRICEPLAN_VALIDATION = 0;
    public static final int SUBSCRIBER_VALIDATION = PRICEPLAN_VALIDATION + 1;

    Long Id = null;
    Context ctx = null;
    PricePlanGroup ppg =null;
    PricePlanGroupList PPG_Parent=null;
    long parent_ppg = -1;

    ArrayList XOR_List =null;
    ArrayList AND_List =null;
    ArrayList PREREQ_List_if =null;
    ArrayList PREREQ_List_then =null;

    HashMap cached_depend_ids = null; //Hold all the Dependency cached (id, set) = {5, [1, 2, 3, ...] }
    HashMap cached_preq_ids = null;

    private int validation_type = 0;



    public PricePlanGroupList(Context ctx,Long Id, HashMap cached_depends_ids,HashMap cached_preq_ids)  throws HomeException
    {
        this.Id = Id;
        this.ctx = ctx;
        this.cached_depend_ids = cached_depends_ids;
        this.cached_preq_ids = cached_preq_ids;

        Home ppghome  =  (Home) ctx.get(PricePlanGroupHome.class);        

        try
        {
            ppg = (PricePlanGroup) ppghome.find(ctx,new EQ(PricePlanGroupXInfo.IDENTIFIER,Id));
        }
        catch (HomeException e)
        {
            throw new PricePlanValidationException(
                    "Error while searching for Price Plan Group id=" + Id + " due to:" + e.getMessage(),
                    e);
        }
        if (ppg!=null)
        {
            this.parent_ppg = ppg.getParentPPG();
        }
        this.loadLists();

    }

    private void loadLists() throws HomeException
    {
        if (ppg==null)
        {
            return;
        }
        DependencyGroupList dgl = new DependencyGroupList(ctx,ppg,this.cached_depend_ids);
        this.cached_depend_ids=dgl.getCachedDependIds();    //Get all the Dependency cache (id, set) = {5, [1, 2, 3, ...] }        
        this.XOR_List=dgl.getXORList();
        this.AND_List=dgl.getANDList();


        PreRequisiteGroupList preql = new PreRequisiteGroupList(ctx,ppg,this);

        //String prepreq_str = ppg.getPrereq_group_list();

    }




    public     PricePlanGroupList (PricePlanGroup priceplangroup)
    throws HomeException
    {
        this.Id=priceplangroup.getIdentifier();
        if (ppg!=null)
        {
            this.parent_ppg=(int) ppg.getParentPPG();
        }

        this.ppg = priceplangroup;
        this.loadLists();
    }

    /**
     * Validate an Exclusive Dependency rule.
     * Allows the intersection of the validate_set and the xor_predicate_list to be 0 (Zero).
     * @param validate_set
     * @param xor_predicate_list
     * @return
     */
    private boolean validateXOR(Set validate_set, ArrayList xor_predicate_list)
    {
        return validateXOR(validate_set, xor_predicate_list, false);
    }

    /**
     * Validate an Exclusive rule for a Prerequisite condition.
     * Enforces that the intersection of the validated_set and the and_predicate_list have to be greater than 0 (Zero).
     * @param validate_set
     * @param xor_predicate_list
     * @param isPrecondition
     * @return
     */
    private boolean validateXOR(Set validate_set, ArrayList xor_predicate_list, boolean isPrecondition)
    {
        if (debugEnabled())
        {
            LogSupport.debug(ctx,this,"Executing XOR");
        }
        boolean flag=true;
        if (xor_predicate_list!=null)
        {
            if (xor_predicate_list.size()>0)
            {
                for (int x=0; x<xor_predicate_list.size(); x++)
                {
                    Set master_set = (HashSet) xor_predicate_list.get(x);
                    Iterator it = validate_set.iterator();
                    int count = 0;
                    if (debugEnabled())
                    {
                        LogSupport.debug(ctx,this,"Checking XOR List:"+master_set+" to validate: "+validate_set);
                    }
                    while (it.hasNext())
                    {
                        String check_element = (String) it.next();
                        if (master_set.contains(check_element))
                        {
                            count++;
                        }
                        if (count>1)
                        {
                            break; // Perform validation below
                        }
                    }
                    if (debugEnabled())
                    {
                        LogSupport.debug(ctx,this,"validateXOR count=" + count);
                    }
                    if (count>1 
                            || (isPrecondition && count == 0))
                    {

                        flag=false; //It brakes exclusive rule, so no need to check more

                        StringBuilder buf = new StringBuilder();
                        buf.append("Failed to satisfy Exclusive rule condition. <br/>");
                        buf.append("The selection [");
                        buf.append(this.setToNameSet(validate_set));
                        buf.append(" CANNOT INCLUDE MORE THAN ONE FROM [");
                        buf.append(this.setToNameSet(master_set));
                        buf.append("]. Make corrections to service selection and Update again.");
                        
                        throwRuleSatisfactionException(buf);
                    }
                }
            }
        }         
        return flag;        
    }

    private int countAux(Set predicate_set)
    {
        int count=0;
        Iterator it = predicate_set.iterator();
        while (it.hasNext())
        {
            String str = (String) it.next();
            if (str!=null && str.startsWith("a"))
            {
                count++;
            }
        }
        return count;
    }
    /**
     * Validate the Inclusive Dependency rule.  
     * Allows the intersection of the validate_set and the and_predicate_list to be 0 (Zero).
     * @param validate_set
     * @param and_predicate_list
     * @return
     * @throws IllegalStateExceptions which will be caught by the CompoundValidator and printed to the Screen properly, in 
     * the event that the validation for INCLUSIVE RULE is not satisfied.
     */
    public boolean validateAND(Set validate_set, ArrayList and_predicate_list)
    {
        return validateAND(validate_set, and_predicate_list, false,false);
    }

    /**
     * Validate the Inclusive rule for a Prerequisite condition. 
     * Enforces that the intersection of the validated_set and the and_predicate_list have to be greater than 0 (Zero).
     * @param validate_set
     * @param and_predicate_list
     * @param isPrereqCondition
     * @return
     * @throws IllegalStateExceptions which will be caught by the CompoundValidator and printed to the Screen properly, in 
     * the event that the validation for INCLUSIVE RULE is not satisfied.
     */
    private boolean validateAND(Set validate_set, ArrayList and_predicate_list, boolean isPrereqCondition, boolean isPreCondition)
    {
        if (debugEnabled())
        {
            LogSupport.debug(ctx,this,"Excecuting AND");
        }
        boolean flag=true;
        if (and_predicate_list!=null)
        {
            if (and_predicate_list.size()>0)
            {
                for (int x=0; x<and_predicate_list.size(); x++)
                {
                    Set master_set = (HashSet) and_predicate_list.get(x);
                    if (debugEnabled())
                    {
                        LogSupport.debug(ctx,this,"Checking AND List:"+master_set+" to validate: "+validate_set);
                    }

                    Iterator it = validate_set.iterator();                     
                    int count = 0;
                    int aux_count=0;

                    /* Need to Exclude Auxiliary for Price Plan Validation for INCLUSIVE RULE, that is why using this variable (Senthooran)
                     * Must enforce full match (including aux service) when the validation is for a 
                     * Prerequisite condition, regardless of validation type */
                    if (this.getValidationType()==PRICEPLAN_VALIDATION)
                    {
                        aux_count = this.countAux(master_set);
                    }  

                    while (it.hasNext())
                    {
                        String check_element = (String) it.next();

                        //if (check_element!=null && this.getValidationType()==PRICEPLAN_VALIDATION && check_element.startsWith("a")) aux_count++;
                        if (master_set.contains(check_element))
                        {
                            count++;
                        }
                    }
                    if (debugEnabled())
                    {
                        LogSupport.debug(ctx,this,"AND: count="+count);
                        LogSupport.debug(ctx,this,"AND: validate_set.size()="+validate_set.size());
                    }
                    // If the validation is part of a precondition, then the empty intersection is not allowed.
                    //OR
                    //If it is price plan validation AND is PRE-Condition AND there are auxiliary services no point in proceeding
                    //with post condition, so make it fail
                    if (master_set.size()>(count+aux_count) 
                            && ((isPrereqCondition && count == 0)
                                    || count!=0)
                                    || (this.getValidationType()==PRICEPLAN_VALIDATION && isPreCondition && aux_count>0))
                    {
                        flag=false; //It brakes Inclusive Rule, so no need to check more
                        StringBuilder buf = new StringBuilder();
                        
                        buf.append("Failed to satisfy Inclusive rule condition. <br/>");
                        buf.append("The selection [");
                        buf.append(this.setToNameSet(validate_set));
                        buf.append("] must also INCLUDE ALL FROM [");
                        buf.append(this.setToNameSet(master_set));
                        buf.append("]. Make corrections to service selection and Update again.");
                        
                        throwRuleSatisfactionException(buf);
                    }
                }                 
            }
        }
        return flag;        
    }


    /**
     * Validation of Independent criteria (used during Prerequisite Validation).
     * @param validate_set
     * @param indep_predicate_list
     * @return
     * @throws IllegalStateExceptions which will be caught by the CompoundValidator and printed to the Screen properly, in 
     * the event that the validation for INDEPENDENT RULE is not satisfied.
     */
    public boolean validateINDEP(Set validate_set, ArrayList indep_predicate_list)
    {
        if (debugEnabled())
        {
            LogSupport.debug(ctx,this,"Excecuting INDEP");
        }
        boolean flag=true;
        if (indep_predicate_list!=null)
        {
            if (indep_predicate_list.size()>0)
            {
                for (int x=0; x<indep_predicate_list.size(); x++)
                {
                    Set master_set = (HashSet) indep_predicate_list.get(x);
                    if (debugEnabled())
                    {
                        LogSupport.debug(ctx,this,"Checking INDEPENDENT List:"+master_set+" to validate: "+validate_set);
                    }


                    Iterator it = validate_set.iterator();                     
                    int count = 0;
                    while (it.hasNext())
                    {
                        String check_element = (String) it.next();
                        if (master_set.contains(check_element))
                        {
                            count++;
                        }
                    }
                    if (debugEnabled())
                    {
                        LogSupport.debug(ctx,this,"INDEPENDENT: count="+count);
                        LogSupport.debug(ctx,this,"INDEPENDENT: validate_set.size()="+validate_set.size());
                    }
                    if (master_set.size()>0 && count<=0)
                    {
                        StringBuilder buf = new StringBuilder();
                        
                        buf.append("Failed to satisfy Independent rule condition. <br/>");
                        buf.append("The selection [");
                        buf.append(this.setToNameSet(validate_set));
                        buf.append("] must also INCLUDE AT LEAST ONE FROM [");
                        buf.append(this.setToNameSet(master_set));
                        buf.append("]. Make corrections to service selection and Update again.");
                        
                        flag=false; //It brakes Inclusive Rule, so no need to check more 
                        
                        throwRuleSatisfactionException(buf);
                    }
                }                 
            }
        }
        return flag;        
    }

    /**
     * Satisfaction of the Precondition leads to validation of the post condition.  Error is thrown if the 
     * postcondition is not satisfied when validated. 
     * @param validate_set
     * @param validate_mandatory_set
     * @return
     * @throws IllegalStateExceptions which will be caught by the CompoundValidator and printed to the Screen properly, in 
     * the event that the validation for PREREQUISITE RULE is not satisfied.
     */
    public boolean validatePREREQ(Set validate_set,Set validate_mandatory_set)
    {
        if (debugEnabled())
        {
            LogSupport.debug(ctx,this,"Excecuting PRE-REQ");
        }
        boolean flag=true;
        if (this.PREREQ_List_if!=null)
        {
            if (this.PREREQ_List_if.size()>0)
            {
                for (int x=0; x<this.PREREQ_List_if.size(); x++)
                {
                    String if_str = (String) this.PREREQ_List_if.get(x);
                    String then_str = (String) this.PREREQ_List_then.get(x);

                    DependencyRec dprec_if = (DependencyRec) this.cached_depend_ids.get(if_str);
                    DependencyRec dprec_then = (DependencyRec) this.cached_depend_ids.get(then_str);
                    boolean is_passed = true;
                    try {

                        //Check if IF_SET throws an error
                        ArrayList predicators_list_if  = new ArrayList();
                        predicators_list_if.add(dprec_if.getList());

                        ArrayList predicators_list_then  = new ArrayList();
                        predicators_list_then.add(dprec_then.getList());


                        if (dprec_if.rec_enum.equals(DependencyGroupTypeEnum.XOR))
                        {
                            this.validateXOR(validate_mandatory_set, predicators_list_if, true);
                        }
                        else 
                            if (dprec_if.rec_enum.equals(DependencyGroupTypeEnum.AND))
                            {
                                this.validateAND(validate_set, predicators_list_if, true,true);
                            }
                            else
                                if (dprec_if.rec_enum.equals(DependencyGroupTypeEnum.IND))
                                {
                                    this.validateINDEP(validate_set, predicators_list_if);
                                }

                        is_passed=false;

                        if (dprec_then.rec_enum.equals(DependencyGroupTypeEnum.XOR))
                        {
                            this.validateXOR(validate_mandatory_set, predicators_list_then, true);
                        }
                        else 
                            if (dprec_then.rec_enum.equals(DependencyGroupTypeEnum.AND))
                            {
                                this.validateAND(validate_set, predicators_list_then, true,false);
                            }
                            else
                                if (dprec_then.rec_enum.equals(DependencyGroupTypeEnum.IND))
                                {
                                    this.validateINDEP(validate_set, predicators_list_then);
                                }

                        is_passed=true;

                    }
                    catch (Exception e)
                    {
                        if (!is_passed)
                        {
                            StringBuilder buf = new StringBuilder();
                            String validate_set_str = this.setToNameSet(validate_set);
                            
                            buf.append("Failed to satisfy Prerequisite rule condition. <br/>");
                            buf.append("The selection [" + validate_set_str +"] ");
                            buf.append(dprec_if.getPredicateStatement());
                            buf.append(" [");
                            buf.append(this.setToNameSet(dprec_if.getList()));
                            
                            buf.append("] <br/>You must also ");
                            buf.append(dprec_then.getPredicateAction());
                            buf.append(" [");
                            buf.append(this.setToNameSet(dprec_then.getList()));
                            buf.append("] in your selection.  Make corrections to service selection and Update again.");

                            flag=false; //It brakes Prerequisite Rule, so no need to check more                     

                            throwRuleSatisfactionException(buf);
                        }
                    }


                }                 
            }
        }
        return flag;        
    }

    /**
     * Parses the PricePlanGroupList set of Services/Bundles/Auxiliary Services to find Identifiers
     * and retrieves the bean name (the Descriptor to the user). 
     * @param set - Set of Services/Bundles/Auxiliary identifiers. Auxliary Services are prefixed 
     * by 'a'; Bundles are prefixed by 'b';  Otherwise, considered to be a Service.  
     * @return A String containing comma delimited list of the names of the 
     * Services/Bundles/Auxiliary Services in the given set. 
     */
    private String setToNameSet(Set set)
    {
        StringBuilder buf = new StringBuilder();
        Iterator it = set.iterator();

        while (it.hasNext())
        {
            if (buf.length()>0)
            {
                buf.append(", ");
            }
            try {    
                String str = (String) it.next();
                if (str!=null && str.length()>0)
                {
                    if (str.charAt(0)=='a')
                    {
                        Home auxhome  =  (Home) ctx.get(AuxiliaryServiceHome.class);        
                        if (auxhome!=null)
                        {
                            AuxiliaryService aux = (AuxiliaryService) auxhome.find(ctx,new EQ(AuxiliaryServiceXInfo.IDENTIFIER ,Long.valueOf(str.substring(1))));
                            if (aux!=null)
                            {
                                buf.append(aux.getIdentifier());
                                buf.append("-");
                                buf.append(aux.getName());
                            }
                        }
                    }
                    else
                        if  (str.charAt(0)=='b')
                        {
                            BundleProfile bundleapi = BundleSupportHelper.get(ctx).getBundleProfile(ctx, Long.parseLong(str.substring(1)));
                            if (bundleapi!=null)
                            {
                                buf.append(bundleapi.getBundleId());
                                buf.append("-");
                                buf.append(bundleapi.getName());
                            }
                        }
                        else
                        {
                            Home servicehome = (Home) ctx.get(ServiceHome.class);
                            if (servicehome!=null)
                            {
                                Service service = (Service) servicehome.find(ctx, Long.valueOf(str));
                                if (service!=null) 
                                {
                                    buf.append(service.getID());
                                    buf.append("-");
                                    buf.append(service.getName());
                                }
                            }
                        }
                }            
            }
            catch (Exception e)
            {
                e.printStackTrace();

            }
        }
        return buf.toString();
    }

    public boolean validate(Set validate_set,Set validate_mandatory_set, int validation_type) throws HomeException
    {
        boolean flag=true;
        this.validation_type=validation_type;

        if (this.ppg==null)
        {
            return true;
        }

        flag = this.validateXOR(validate_mandatory_set,this.XOR_List);
        if (flag)
        {
            flag=this.validateAND(validate_set,this.AND_List);
        }
        if (flag)
        {
            flag=this.validatePREREQ(validate_set,validate_mandatory_set);
        }

        if (flag && debugEnabled())
        {
            LogSupport.debug(ctx,this,"Depedency Rules Passed on the Current Node:"+this.Id);
        }
        if (flag && this.parent_ppg!=-1)
        {
            if (flag && debugEnabled())
            {
                LogSupport.debug(ctx,this,"Checking Parent Node:"+this.parent_ppg);
            }
            this.PPG_Parent=new PricePlanGroupList(this.ctx,this.parent_ppg,this.cached_depend_ids,this.cached_depend_ids);
            flag=PPG_Parent.validate(validate_set,validate_mandatory_set,validation_type);
        }

        return flag;
    }

    public int getValidationType()
    {
        return this.validation_type;
    }

    /**
     * @return Checks log support if there is an active Context.  During unit tests there won't be a running Context
     */
    private boolean debugEnabled()
    {
        if (this.ctx != null)
        {
            return LogSupport.isDebugEnabled(ctx);
        }
        return false;
    }
    
    /**
     * Logs given error reason and 
     * throws an Exception to indicate a failure to satisfy the Price Plan condition.
     */
    private void throwRuleSatisfactionException(StringBuilder buf) 
    {
        if (debugEnabled())
        {
            LogSupport.debug(this.ctx, this, 
                    "Fails to satisfy Price Plan Validation for Price Plan Group ID: " 
                    + this.Id + ". Error due to: " + buf.toString());
        }
        throw new RuleSatisfactionException(buf.toString());    
    }

}
