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

package com.trilogy.app.crm.duplicatedetection;

import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionCriteria;
import com.trilogy.app.crm.bean.duplicatedetection.IdentificationDetectionCriteria;
import com.trilogy.app.crm.bean.duplicatedetection.NameDOBDetectionCriteria;

/**
 * Factory class for DuplicateAccountDetectionCommand.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class DuplicateAccountDetectionCommandFactory
{
    private DuplicateAccountDetectionCommandFactory()
    {
        // empty to prevent instantiation of factory class.
    }

    public static DuplicateAccountDetectionCommandFactory instance()
    {
        return instance;
    }

    /**
     * Creates the command object for the provided criteria.
     * 
     * @param criteria
     *            Criteria to search with.
     * @return The command object for the provided criteria.
     */
    public DuplicateAccountDetectionCommand createCommand(final DuplicateAccountDetectionCriteria criteria)
    {
        DuplicateAccountDetectionCommand command = NullDuplicateDetectionCommand.instance();

        if (criteria instanceof IdentificationDetectionCriteria)
        {
            command = new IdentificationDuplicateDetectionCommand((IdentificationDetectionCriteria) criteria);
        }
        else if (criteria instanceof NameDOBDetectionCriteria)
        {
            command = new NameDOBDuplicateDetectionCommand((NameDOBDetectionCriteria) criteria);
        }
        return command;
    }

    private static DuplicateAccountDetectionCommandFactory instance = new DuplicateAccountDetectionCommandFactory();
}
