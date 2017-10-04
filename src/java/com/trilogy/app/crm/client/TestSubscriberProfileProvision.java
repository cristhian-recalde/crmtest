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

import com.trilogy.product.bundle.manager.provision.SubscriberProfile;
import com.trilogy.product.bundle.manager.provision.SubscriberProfileProvision;
import com.trilogy.product.bundle.manager.provision.SubscriberReturnParam;
import com.trilogy.product.bundle.manager.provision.SubscriberServiceReturnParam;
import com.trilogy.product.bundle.manager.provision.param.Parameter;


public class TestSubscriberProfileProvision implements SubscriberProfileProvision
{
    HashMap profiles_ = new HashMap();
    
    public SubscriberReturnParam changeMsisdn(String s, String s1, Parameter[] aparameter)
    {
        SubscriberReturnParam ret = new SubscriberReturnParam();
        if (profiles_.containsKey(s))
        {
            SubscriberProfile subProfile = (SubscriberProfile) profiles_.get(s);
            subProfile.msisdn = s1;
            profiles_.remove(s);
            profiles_.put(s1, subProfile);
            
            ret.resultCode = 0;
        }        
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public short[] createBulkSubs(SubscriberProfile[] asubscriberprofile, Parameter[][] aparameter)
    {        
        short [] ret = new short[asubscriberprofile.length];
        for (int i = 0; i < asubscriberprofile.length; i ++)
        {
            if (profiles_.containsKey(((SubscriberProfile)asubscriberprofile[i]).msisdn))
            {
                profiles_.put(((SubscriberProfile)asubscriberprofile[i]).msisdn, asubscriberprofile[i]);
                ret[i] = 0;               
            }
            else
            {
                ret[i] = -1;
            }
        }    

        return ret;
    }


    public SubscriberReturnParam createSubscriberProfile(SubscriberProfile subscriberprofile, Parameter[] aparameter)
    {
        SubscriberReturnParam ret = new SubscriberReturnParam();
        if (!profiles_.containsKey(subscriberprofile.msisdn))
        {
            profiles_.put(subscriberprofile.msisdn, subscriberprofile);
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public SubscriberReturnParam getSubscriberProfile(String s, Parameter[] aparameter)
    {
        SubscriberReturnParam ret = new SubscriberReturnParam();
        if (!profiles_.containsKey(s))
        {
            ret.outSubscriberProfile = (SubscriberProfile) profiles_.get(s);
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }


    public short[] removeBulkSubs(String[] as, Parameter[][] aparameter)
    {
        short [] ret = new short[as.length];
        for (int i = 0; i < as.length; i ++)
        {
            if (profiles_.containsKey(as[i]))
            {
                ret[i] = 0;
                profiles_.remove(as[i]);
            }
            else
            {
                ret[i] = -1;
            }
        }    

        return ret;
    }


    public SubscriberReturnParam removeSubscriberProfile(String s, Parameter[] aparameter)
    {
        SubscriberReturnParam ret = new SubscriberReturnParam();
        if (profiles_.containsKey(s))
        {
            profiles_.remove(s);
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }

    public SubscriberServiceReturnParam removeSubscriberProfile2(String s)
    {
        SubscriberServiceReturnParam ret = new SubscriberServiceReturnParam();
        if (profiles_.containsKey(s))
        {
            profiles_.remove(s);
            ret.resultCode = 0;
        }
        else
        {
            ret.resultCode = -1;
        }
        return ret;
    }
    
    public SubscriberReturnParam updateSubscriberProfile(String s, SubscriberProfile subscriberprofile,
            Parameter[] aparameter)
    {
        SubscriberReturnParam ret = new SubscriberReturnParam();
        if (!profiles_.containsKey(s))
        {
            profiles_.put(s, subscriberprofile);
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
