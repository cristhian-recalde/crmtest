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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.BeanOperationEnum;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.xhome.home.SimpleBeanERHome;

public class AdjustmentTypeERLogHome extends SimpleBeanERHome 
{

    public AdjustmentTypeERLogHome(final Home delegate)
    {
        super(delegate, IDENTIFIER, CLASS, TITLE, FIELDS);
    }

    private static final int IDENTIFIER = 1117;
    private static final int CLASS = 700;
    private static final String TITLE = "Adjustment Type Management";
    
    private static final PropertyInfo[] FIELDS =
    {
        AdjustmentTypeXInfo.CODE,
        AdjustmentTypeXInfo.PARENT_CODE,
        AdjustmentTypeXInfo.NAME,
        AdjustmentTypeXInfo.DESC,
        AdjustmentTypeXInfo.ACTION,
        AdjustmentTypeXInfo.PERMISSION,
        AdjustmentTypeXInfo.STATE,
        AdjustmentTypeXInfo.ADJUSTMENT_SPID_INFO
    };

    @Override
    protected Object getOriginal(final Context context, final Object object) throws HomeException
    {
        final AdjustmentType newBean = (AdjustmentType)object;

        final Home home = (Home)context.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_READ_ONLY_HOME);

        return home.find(context, Integer.valueOf(newBean.getCode()));
    }

    /**
     * Overwrite the getFieldValues method, so we can properly handle the "ACTION" mapping.
     */
    @Override
    protected String[] getFieldValues(final Context context, final BeanOperationEnum action,
            final Object oldBean, final Object newBean)
    {
        final List<String> values = new ArrayList<String>(fields_.length * 2 + 2);

        //Agent
        values.add(FrameworkSupportHelper.get(context).getCurrentUserID(context));

        //Action
        values.add(String.valueOf(action.getIndex()));

        for (final PropertyInfo info: fields_)
        {
            if (info.equals(AdjustmentTypeXInfo.ADJUSTMENT_SPID_INFO))
            {
                addSpidInfoValues(values, oldBean, newBean, info);
            }
            else
            {
                addValue(values, oldBean, info);
                addValue(values, newBean, info);
            }
        }

        return values.toArray(new String[values.size()]);
    }
    
    protected void addSpidInfoValues(final List<String> values, final Object oldBean, Object newBean, final PropertyInfo info)
    {
        //initialize
        resetLists();
        if (newBean != null)
        {
            newMap_ = (HashMap) info.get(newBean);
        }
        if (oldBean != null)
        {
            oldMap_ = (HashMap) info.get(oldBean);
        }
        
        Iterator oldKeys = oldMap_.keySet().iterator();
        while (oldKeys.hasNext())
        {
            Integer key  = (Integer)oldKeys.next();
            addSpidInfo(key);
            newMap_.remove(key);
        }
        //Report Spid Info Values only in the new Set (if there are any)
        Iterator newKeys = newMap_.keySet().iterator();
        while (newKeys.hasNext())
        {
            addSpidInfo((Integer)newKeys.next());
        }
        
        //Add the SPID, GL Code, Invoice Description, and Tax Authority Values to the ER fields list
        addList(values, oldSpids_);
        addList(values, newSpids_);
        addList(values, oldGLCodes_);
        addList(values, newGLCodes_);
        addList(values, oldInvoiceDescs_);
        addList(values, newInvoiceDescs_);
        addList(values, oldTaxAuths_);
        addList(values, newTaxAuths_);
    }
    
    private void addSpidInfo(Integer spid)
    {
        AdjustmentInfo oldAdjustmentInfo = (AdjustmentInfo) oldMap_.get(spid);
        AdjustmentInfo newAdjustmentInfo = (AdjustmentInfo) newMap_.get(spid);
        
        oldSpids_.add(oldAdjustmentInfo == null ? "" : String.valueOf(oldAdjustmentInfo.getSpid()));
        newSpids_.add(newAdjustmentInfo == null ? "" : String.valueOf(newAdjustmentInfo.getSpid()));
        
        oldGLCodes_.add(oldAdjustmentInfo == null ? "" : oldAdjustmentInfo.getGLCode());
        newGLCodes_.add(newAdjustmentInfo == null ? "" : newAdjustmentInfo.getGLCode());
        
        oldInvoiceDescs_.add(oldAdjustmentInfo == null ? "" : oldAdjustmentInfo.getInvoiceDesc());
        newInvoiceDescs_.add(newAdjustmentInfo == null ? "" : newAdjustmentInfo.getInvoiceDesc());
        
        oldTaxAuths_.add(oldAdjustmentInfo == null ? "" : String.valueOf(oldAdjustmentInfo.getTaxAuthority()));
        newTaxAuths_.add(newAdjustmentInfo == null ? "" : String.valueOf(newAdjustmentInfo.getTaxAuthority()));
    }
    
    private void addList(final List<String> fieldValues, List<String> list)
    {
        StringBuilder buffer = new StringBuilder();
        if(list != null) 
        {
            Iterator iter = list.iterator();
            while (iter.hasNext())
            {
                final String property = (String) iter.next();
                if(property != null)
                {
                    if (property.indexOf(",") >= 0)
                    {
                        // Safely deal with extra commas
                        buffer.append("\"").append(property).append("\"");
                    }
                    else
                    {
                        buffer.append(property);
                    }
                    buffer.append("|");
                }
            }
            fieldValues.add(buffer.toString());
        }
        
    }
    
    private void resetLists()
    {
        oldSpids_ = new ArrayList<String>();
        newSpids_ = new ArrayList<String>();
        oldGLCodes_ = new ArrayList<String>();
        newGLCodes_ = new ArrayList<String>();
        oldInvoiceDescs_ = new ArrayList<String>();
        newInvoiceDescs_ = new ArrayList<String>();
        oldTaxAuths_ = new ArrayList<String>();
        newTaxAuths_ = new ArrayList<String>();
        newMap_ = new HashMap();
        oldMap_ = new HashMap();
    }
    
    private List<String> oldSpids_;
    private List<String> newSpids_;
    private List<String> oldGLCodes_;
    private List<String> newGLCodes_;
    private List<String> oldInvoiceDescs_;
    private List<String> newInvoiceDescs_;
    private List<String> oldTaxAuths_;
    private List<String> newTaxAuths_;
    HashMap oldMap_;
    HashMap newMap_; 

}
