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
package com.trilogy.app.crm.extension.subscriber;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.util.Link;

/**
 * Move request for PPSM Supporter subscriber extension.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class PPSMSupporterMoveRequest extends AbstractPPSMSupporterMoveRequest
{
    /**
     * @{inheritDoc}
     */
    public String getSuccessMessage(Context ctx)
    {
        String msg = null;
        
        MessageMgr mmgr = new MessageMgr(ctx, this);

        final Link link = new Link(ctx);
        link.remove("cmd");
        link.add("cmd","SubMenuSubProfileEdit");
        link.remove("key");
        link.add("key",this.getNewSubscriptionId());
        
        msg = mmgr.get(PPSMSupporterMoveRequest.class.getSimpleName() + ".success", 
                "PPSM supported subscriptions successfully moved from subscription {0} to subscription <a href=\"{1}\">{2}</a>.", 
                new String[] {
                        this.getOldSubscriptionId(),
                        link.write(),
                        this.getNewSubscriptionId()
                    });
        
        return msg;
    }  
    
    /**
    * @{inheritDoc}
    */
   public void reportError(Context ctx, Throwable error)
   {
       assertBeanNotFrozen();
       
       errors_.add(error);
   }

   /**
    * @{inheritDoc}
    */
   public void reportWarning(Context ctx, MoveWarningException warning)
   {
       assertBeanNotFrozen();
       
       warnings_.add(warning);
   }

   /**
    * @{inheritDoc}
    */
   public boolean hasErrors(Context ctx)
   {
       return errors_ != null && errors_.size() > 0;
   }

   /**
    * @{inheritDoc}
    */
   public boolean hasWarnings(Context ctx)
   {
       return warnings_ != null && warnings_.size() > 0;
   }

   /**
    * @{inheritDoc}
    */
   public Set<Throwable> getErrors(Context ctx)
   {
       return Collections.unmodifiableSet(errors_);
   }

   /**
    * @{inheritDoc}
    */
   public Set<MoveWarningException> getWarnings(Context ctx)
   {
       return Collections.unmodifiableSet(warnings_);
   }
   
   protected Set<Throwable> errors_ = new HashSet<Throwable>();
   protected Set<MoveWarningException> warnings_ = new HashSet<MoveWarningException>();
   }
