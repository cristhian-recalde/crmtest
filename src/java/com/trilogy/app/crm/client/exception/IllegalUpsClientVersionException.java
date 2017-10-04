/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.client.exception;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.trilogy.app.crm.config.AbmClientVersionEnum;


/**
 * Thrown by UPS methods/implementations that do not support the given methods.
 * It is intended that new methods added to ABM's interface throw this exception
 * when connected to earlier versions of ABM.
 *
 * @author Aaron Gourley
 * @since 7.5
 *
 */
public class IllegalUpsClientVersionException extends Exception
{
    public IllegalUpsClientVersionException(AbmClientVersionEnum minVersion)
    {
        super();
        minVersion_ = minVersion;
    }

    public IllegalUpsClientVersionException(AbmClientVersionEnum... supportedVersions)
    {
        super();
        for( AbmClientVersionEnum version : supportedVersions )
        {
            supportedVersions_.add(version);
        }
    }
    
    @Override
    public String getMessage()
    {
        StringBuilder msgBuilder = new StringBuilder("The following ABM clients support this operation: ");
        if( minVersion_ != null )
        {
            return msgBuilder
            .append(minVersion_.getDescription())
            .append(" and up").toString();
        }
        else if( supportedVersions_ != null
                && supportedVersions_.size() > 0 )
        {
            for( AbmClientVersionEnum version : supportedVersions_ )
            {
                msgBuilder.append(version.getDescription() + ", ");
            }
            return msgBuilder.toString();
        }
        else
        {
            return "No version of the ABM client supports this operation.";
        }
    }
    private AbmClientVersionEnum minVersion_ = null;
    private Set<AbmClientVersionEnum> supportedVersions_ = new LinkedHashSet<AbmClientVersionEnum>();
    
}
