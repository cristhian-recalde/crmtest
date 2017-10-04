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

import com.trilogy.product.bundle.manager.provision.v5_0.bundle.BundleProfile;
import com.trilogy.product.bundle.manager.provision.v5_0.bundle.BundleProfileProvision;
import com.trilogy.product.bundle.manager.provision.v5_0.bundle.BundleReturnParam;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;


public class TestBundleProfileProvision implements BundleProfileProvision
{
    private static final long serialVersionUID = 1L;
    
    private HashMap<Long, BundleProfile> bundles = new HashMap<Long, BundleProfile>();
    
    public BundleReturnParam createBundleProfile(BundleProfile bundleprofile, Parameter[] aparameter)
    {
        bundles.put(Long.valueOf(bundleprofile.bundleId), bundleprofile);

        BundleReturnParam ret = new BundleReturnParam();
        ret.resultCode = 0;

        return ret;
    }


    public BundleReturnParam getBundleProfile(int spid, long i, Parameter[] aparameter)
    {
        BundleReturnParam ret = new BundleReturnParam();
        if (bundles.containsKey(Long.valueOf(i)))
        {
            ret.resultCode = 0;
            ret.outBundleProfile = (BundleProfile) bundles.get(Long.valueOf(i));
        }
        {
            ret.resultCode = -1;            
        }
        return ret;
    }


    public BundleReturnParam removeBundleProfile(int spid, long i, Parameter[] aparameter)
    {
        BundleReturnParam ret = new BundleReturnParam();
        if (bundles.containsKey(Long.valueOf(i)))
        {
            bundles.remove(Long.valueOf(i));
            ret.resultCode = 0;
        }
        {
            ret.resultCode = -1;            
        }
        return ret;
    }


    public BundleReturnParam updateBundleProfile(long i, BundleProfile bundleprofile, Parameter[] aparameter)
    {
        BundleReturnParam ret = new BundleReturnParam();
        if (bundles.containsKey(Long.valueOf(i)))
        {
            bundles.put(Long.valueOf(i), bundleprofile);
            ret.resultCode = 0;
        }
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
