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
package com.trilogy.app.crm.provision.soap;

import java.io.Serializable;


/** 
 * @author imahalingam
 * Code ported by amit.baid@redknee.com
 */
public class SubscriberInfo implements Serializable{

	  
    public long spid;
    public String dateCreated;
    public String firstName;
    public String lastName;
    public int subscriberType;
    public String MSISDN;
    public int state;
    public String startDate;
   
    
    public SubscriberInfo() {}
   
 
    public SubscriberInfo(
        long spid,
        String dateCreated,
        String firstName,
        String lastName,
        int subscriberType,
        String MSISDN,
        int state,
        String startDate)
    {
        this.spid = spid;
        this.dateCreated = dateCreated;
        this.firstName = firstName;
        this.lastName = lastName;        
        this.subscriberType = subscriberType;
        this.MSISDN = MSISDN;
        this.state = state;
        this.startDate = startDate;
    }
    
    
    public String toString()
    {            
        return new StringBuilder().append("SubscriberInfo(")
                    .append("spid = ").append(spid)
                    .append(", dateCreated = ").append(dateCreated)
                    .append(", firstName = ").append(firstName)
                    .append(", lastName = ").append(lastName)
                    .append(", subscriberType = ").append(subscriberType)
                    .append(", MSISDN = ").append(MSISDN)
                    .append(", state = ").append(state)
                    .append(", startDate = ").append(startDate)
                    .append(")").toString();
    } 
    

}
