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
package com.trilogy.app.crm.transfer;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;


/**
 * Provides a search border for the ContractGroupMembership screen.
 *
 * @author gary.anderson@redknee.com
 */
public class ContractGroupMembershipSearchBorder
    extends SearchBorder
{
    /**
     * Creates a new border for the ContractGroupMembership search.
     *
     * @param context The operating context.
     */
    public ContractGroupMembershipSearchBorder(final Context context)
    {
        super(context, ContractGroupMembership.class, new ContractGroupMembershipSearchWebControl());
        addAgent(new ContractGroupSearchAgent());
        addAgent(new MobileNumberSearchAgent());
    }


    /**
     * Provides the search agent for ContractGroup.
     *
     * @author gary.anderson@redknee.com
     */
    public class ContractGroupSearchAgent
        extends SelectSearchAgent
    {
        /**
         * Creates a new ContractGroupSearchAgent.
         */
        public ContractGroupSearchAgent()
        {
            super(ContractGroupMembershipXInfo.GROUP_ID, ContractGroupMembershipSearchXInfo.GROUP_ID);
        }


        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;
    }


    /**
     * Provides the search agent for MobileNumber.
     *
     * @author gary.anderson@redknee.com
     */
    public class MobileNumberSearchAgent
        extends SelectSearchAgent
    {
        /**
         * Creates a new MobileNumberSearchAgent.
         */
        public MobileNumberSearchAgent()
        {
            super(ContractGroupMembershipXInfo.MOBILE_NUMBER, ContractGroupMembershipSearchXInfo.MOBILE_NUMBER);
        }



        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;
    }


}
