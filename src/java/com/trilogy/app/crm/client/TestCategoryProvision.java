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
package com.trilogy.app.crm.client;

import java.util.HashMap;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;

import com.trilogy.product.bundle.manager.provision.v5_0.category.Category;
import com.trilogy.product.bundle.manager.provision.v5_0.category.CategoryProvision;
import com.trilogy.product.bundle.manager.provision.v5_0.category.CategoryReturnParam;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;


public class TestCategoryProvision implements CategoryProvision
{
    private static final long serialVersionUID = 1L;
    
    private HashMap<Integer, Category> categories = new HashMap<Integer, Category>(); 
    
    public CategoryReturnParam createCategory(Category category, Parameter[] aparameter)
    {
        categories.put(Integer.valueOf(category.categoryId), category);
        
        CategoryReturnParam ret = new CategoryReturnParam();
        ret.resultCode = 0;
        
        return ret;
    }


    public CategoryReturnParam getBundleCategory(int spId, int i, Parameter[] aparameter)
    {
        CategoryReturnParam ret = new CategoryReturnParam();
        if (categories.containsKey(Integer.valueOf(i)))
        {
            ret.outBundleCategory = (Category) categories.get(Integer.valueOf(i));
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public CategoryReturnParam removeBundleCategory(int spId, int i, Parameter[] aparameter)
    {
        CategoryReturnParam ret = new CategoryReturnParam();
        if (categories.containsKey(Integer.valueOf(i)))
        {
            categories.remove(Integer.valueOf(i));
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public CategoryReturnParam updateCategory(int i, Category category, Parameter[] aparameter)
    {
        CategoryReturnParam ret = new CategoryReturnParam();
        if (categories.containsKey(Integer.valueOf(i)))
        {
            categories.put(Integer.valueOf(i), category);
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public Request _create_request(Context context, String s, NVList nvlist, NamedValue namedvalue)
    {
        return null;
    }


    public Request _create_request(Context context, String s, NVList nvlist, NamedValue namedvalue,
            ExceptionList exceptionlist, ContextList contextlist)
    {
        return null;
    }


    public Object _duplicate()
    {
        return null;
    }


    public DomainManager[] _get_domain_managers()
    {
        return null;
    }


    public Object _get_interface_def()
    {
        return null;
    }


    public Policy _get_policy(int i)
    {
        return null;
    }


    public int _hash(int i)
    {
        return 0;
    }


    public boolean _is_a(String s)
    {
        return false;
    }


    public boolean _is_equivalent(Object obj)
    {
        return false;
    }


    public boolean _non_existent()
    {
        return false;
    }


    public void _release()
    {
    }


    public Request _request(String s)
    {
        return null;
    }


    public Object _set_policy_override(Policy[] apolicy, SetOverrideType setoverridetype)
    {
        return null;
    }
}
