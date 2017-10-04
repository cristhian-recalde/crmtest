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
package com.trilogy.app.crm.bean;


/**
 * An interface to be implemented by all enumeration classes required to be
 * made configurable.
 *
 * @author jimmy.ng@redknee.com
 */
public interface EnumerationConfig
{
    /**
     * Set the index of the enumeration.
     *
     * @param index The enumeration's index.
     */
    public void setIndex(short index);
    
    
    /**
     * Return the index of the enumeration.
     *
     * @return The enumeration's index.
     */
    public short getIndex();
    
    
    /**
     * Set the name of the enumeration.
     *
     * @param name The enumeration's name.
     */
    public void setName(String name);
    
    
    /**
     * Return the name of the enumeration.
     *
     * @return The enumeration's name.
     */
    public String getName();
    
    
    /**
     * Set the label of the enumeration.
     *
     * @param label The enumeration's label.
     */
    public void setLabel(String label);
    
    
    /**
     * Return the label of the enumeration.
     *
     * @return The enumeration's label.
     */
    public String getLabel();
    
} // interface
