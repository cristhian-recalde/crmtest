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
package com.trilogy.app.crm.priceplan.validator;

/**
 * Exception used when a Price Plan Validation rule condition has failed to be satisfied. 
 * Rule conditions can be Independent/Inclusive/Exclusive types.
 * 
 * Throwing this exception doesn't necessarily mean that the Price Plan Group rules have failed
 * to be met.
 * 
 * For instance, such a failure thrown while evaluating the prerequisite Condition to a 
 * PreRequisiteGroupRec criteria would only cause us to skip evaluating the post-condition to 
 * that rule (and thus avoid "failing" the criteria).
 * 
 * However, when such a failure is thrown while evaluating a DependencyGroupRec then
 * it means that the criteria failed to be satisfied, and consequently, it's a failure to 
 * meet the Price Plan Group rules.
 *    
 * 
 * @author ali
 *
 */
public class RuleSatisfactionException extends IllegalStateException 
{

    public RuleSatisfactionException(String s) 
    {
        super("Price Plan Validation Conflict: " + s);
    }

    private static final long serialVersionUID = 1L;

}
