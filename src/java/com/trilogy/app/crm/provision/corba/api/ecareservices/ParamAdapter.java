package com.trilogy.app.crm.provision.corba.api.ecareservices;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.app.crm.provision.corba.api.ecareservices.param.ParameterValue;

public abstract class ParamAdapter
{
    public ParamAdapter()
    {
        super();        
    }

    public ParameterValue adaptParamValue(String str)
    {
        ParameterValue pv = new ParameterValue();
        pv.stringValue(str);
        return pv;
    }
    
    public ParameterValue adaptParamValue(int i)
    {
        ParameterValue pv = new ParameterValue();
        pv.intValue(i);
        return pv;
    }
    
    public ParameterValue adaptParamValue(long l)
    {
        ParameterValue pv = new ParameterValue();
        pv.longValue(l);
        return pv;
    }

    public ParameterValue adaptParamValue(Date date)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return adaptParamValue(sdf.format(date));
        
        
    }
    
    public final static String DATE_FORMAT = "yyyy-MM-dd";
}
