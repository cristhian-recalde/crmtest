/*
 * Created on Dec 19 2013
 * 
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
 *
 * 
 */
package com.trilogy.app.crm.web.service;

/**
 * @author msubramanyam 
 * @author bdhavalshankh (Referred TDMAPackage Loader)
 *
 * This is temporary Data Transfer Object created for the 
 * GSM Package Bulk Loader. 
 */
public class GSMPackageLoaderDTO {

    private int spid_;
    private String packageGrp_;
    
    public String getPackageGrp() 
    {
        return packageGrp_;
    }
    
    public void setPackageGrp(String packageGrp) 
    {
        this.packageGrp_ = packageGrp;
    }
    
    public int getSpid() 
    {
        return spid_;
    }
    
    public void setSpid(int spid) 
    {
        this.spid_ = spid;
    }
}
