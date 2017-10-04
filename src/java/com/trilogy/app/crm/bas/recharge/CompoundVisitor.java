/*
 *  CompoundVisitor.java
 *
 *  Author : victor.stratan@redknee.com
 *  Date   : Mar 17, 2006
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bas.recharge;

import java.util.Iterator;
import java.util.LinkedList;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * Hold references to other visitors and will call the visit on each of them in
 * the order that the visitors were added
 *
 * Note: not to be used with visitors that modify or store the passed object.
 *
 * @author victor.stratan@redknee.com
 */
public class CompoundVisitor implements Visitor {
    LinkedList visitors_ = new LinkedList();

    public void visit(Context ctx, Object object) throws AgentException, AbortVisitException
    {
        Iterator it = visitors_.iterator();
        while (it.hasNext())
        {
            Visitor visitor = (Visitor) it.next();
            visitor.visit(ctx, object);
        }
    }

    public void add(Visitor visitor)
    {
        visitors_.addLast(visitor);
    }

}
