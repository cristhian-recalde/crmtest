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
package com.trilogy.app.crm.client.xmlhttp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.trilogy.app.crm.util.Objects;
import com.trilogy.framework.xhome.context.Context;


/**
 * 
 * @author simar.singh@redknee.com
 */
public interface Transalator
{

    /**
     * Create a String using the Context and Input object that serves as a Request for a
     * text based system (ex XML, Telnet)
     * 
     * @param context
     * @param input
     * @throws Exception
     *             when the Objects of interest can not be derived from String of Objects
     * @return String that serves as request for the text based system
     * 
     */
    Objects prepareRequest(Context context, Objects input, Class<?> returnType,
            Class<?>... returnTypes) throws Exception;


    /**
     * Consume the a String received as a as response from a text bases system and return
     * expected Objects driven from it
     * 
     * @param context
     * @param input
     *            - Objects provided as input
     * @param responseString
     *            - Response received from a text based system.
     * @param responseClass
     *            - Response type expected by the caller
     * @param resultClasses
     *            - Results of the other types expected by the caller
     * @throws Exception
     *             when the Objects of interest can not be derived from String of Objects
     * @return Objects of Interest derived from the response and input
     */
    Objects handleResponse(Context context, Objects input, Class<?> responseClass,
            Class<?>... resultClasses) throws Exception;
    
    /**
     * Collection of Instance type the implementation uses as input (prepareRequest).  
     * @return
     */
    Collection<Class<?>> getInputTypesPrepareRequest();
    
    /**
     * Collection of Instances the system produces are of the returned types (prepareRequest).
     * @return
     */
    Collection<Class<?>> getInputTypesHandleReponse();
    
    
    /**
     * Collection of Instance type the implementation uses as input (handleResponse).  
     * @return
     */
    Collection<Class<?>> getOutputTypesPrepareRequest();
    
    /**
     * Collection of Instances the system produces are of the returned types (handleResponse).
     * @return
     */
    Collection<Class<?>> getOutputTypesHandleReponse();
    
    /**
     * 
     * @return
     */
    String getDescription();
    
    
    /**
     * An Unmodifiable Empty collection of Class<?>.
     */
    Collection<Class<?>> NO_CLASSES = Collections.unmodifiableSet(new HashSet<Class<?>>());
    
}
