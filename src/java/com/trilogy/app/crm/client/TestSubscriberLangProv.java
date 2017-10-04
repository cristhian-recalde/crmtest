package com.trilogy.app.crm.client;

import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.StringHolder;

import com.trilogy.app.crm.provision.corba.api.ecareservices.error.ErrorCode;

import com.trilogy.app.pin.manager.SubscriberLangProvOperations;
import com.trilogy.app.pin.manager.param.OutParamID;
import com.trilogy.app.pin.manager.param.Parameter;
import com.trilogy.app.pin.manager.param.ParameterSetHolder;
import com.trilogy.app.pin.manager.param.ParameterValue;


public class TestSubscriberLangProv implements SubscriberLangProvOperations
{

    public TestSubscriberLangProv()
    {
        list = new HashMap();
    }

    public int getSubscriberLanaguage(int i, String s, StringHolder stringholder)
    {
        stringholder.value = list.get(s);
        return ErrorCode.SUCCESS;
    }

    /**
     * {@inheritDoc}
     */
    public int getSubscriberLanguageWithDefault(int i, String s, StringHolder stringholder, ParameterSetHolder paramSet)
    {
        if (list.containsKey(s))
        {
            stringholder.value = list.get(s);
            return ErrorCode.SUCCESS;
        }
        else
        {
            paramSet.value = new Parameter[1];
            paramSet.value[0] = new Parameter();
            paramSet.value[0].parameterID = OutParamID.DEFAULT_LANGUAGE;
            paramSet.value[0].value = new ParameterValue();
            paramSet.value[0].value.stringValue(DEFAULT_LANGUAGE);
            return ErrorCode.SUB_NOT_FOUND;
        }
        
    }

    public int isSubscriberLanguageUpdateSupported(int i, BooleanHolder booleanholder)
    {
        booleanholder.value = true;
        return ErrorCode.SUCCESS;
    }

    public int setSubscriberLanguage(int i, String s, String s1, StringHolder stringholder)
    {
        list.put(s, s1);
        
        stringholder.value = s1;
        
        return 0;
    }

    private Map<String, String> list;
    public static final String DEFAULT_LANGUAGE = "default";
}
