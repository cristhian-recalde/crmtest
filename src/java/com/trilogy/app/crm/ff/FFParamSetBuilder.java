/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.ff;

import java.security.Principal;
import java.util.ArrayList;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.model.app.ff.param.Parameter;
import com.trilogy.model.app.ff.param.ParameterID;
import com.trilogy.model.app.ff.param.ParameterValue;

/**
 * A Builder to build FnF Parameters[]
 * @author simar.singh@redknee.com
 *
 */
public class FFParamSetBuilder
{

    public FFParamSetBuilder()
    {
    }


    public FFParamSetBuilder(String userAgent)
    {
        add(ParameterID.PARAM_AGENT_ID, (null != userAgent) ? userAgent : "");
    }


    public FFParamSetBuilder(Context ctx)
    {
        Principal pricipal = (Principal) ctx.get(Principal.class);
        if (null != pricipal)
        {
            add(ParameterID.PARAM_AGENT_ID, (null != pricipal.getName()) ? pricipal.getName() : "");
        }
        else
        {
            new InfoLogMsg(this, "No pricipal foun in Context. User Agent parameter not populated", null).log(ctx);
        }
    }


    public void add(Parameter parameter)
    {
        parameterList_.add(parameter);
    }


    public final FFParamSetBuilder add(short parameterID, String value)
    {
        final Parameter parameter;
        {
            parameter = instantiateParmeter(parameterID);
            parameter.value.stringValue(value);
        }
        add(parameter);
        return this;
    }


    public final FFParamSetBuilder add(short parameterID, long value)
    {
        final Parameter parameter;
        {
            parameter = instantiateParmeter(parameterID);
            parameter.value.longValue(value);
        }
        add(parameter);
        return this;
    }


    public final FFParamSetBuilder add(short parameterID, int value)
    {
        final Parameter parameter;
        {
            parameter = instantiateParmeter(parameterID);
            parameter.value.intValue(value);
        }
        add(parameter);
        return this;
    }


    public final FFParamSetBuilder add(short parameterID, short value)
    {
        final Parameter parameter;
        {
            parameter = instantiateParmeter(parameterID);
            parameter.value.shortValue(value);
        }
        add(parameter);
        return this;
    }


	public final FFParamSetBuilder add(short parameterID, boolean bool)
	{
		final Parameter parameter;
		{
			parameter = instantiateParmeter(parameterID);
			parameter.value.booleanValue(bool);
		}
		add(parameter);
		return this;
	}

    public Parameter[] getParameters()
    {
        return parameterList_.toArray(PARAMETER_ARRAY_MARKER);
    }


    private Parameter instantiateParmeter(short parameterID)
    {
        final Parameter parameter;
        {
            parameter = new Parameter();
            parameter.parameterID = parameterID;
            parameter.value = new ParameterValue();
        }
        return parameter;
    }

    private final ArrayList<Parameter> parameterList_ = new ArrayList<Parameter>();
    private final static Parameter[] PARAMETER_ARRAY_MARKER = new Parameter[0];
}
