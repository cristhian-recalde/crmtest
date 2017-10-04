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
package com.trilogy.app.crm.client.bm;

import junit.framework.TestCase;

import com.trilogy.product.bundle.manager.provision.common.param.Parameter;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.param.ParameterID;


/**
 * Tests some basic usage of the ParameterBuilder.
 *
 * @author gary.anderson@redknee.com
 */
public class TestParameterBuilder
    extends TestCase
{
    /**
     * Tests that in the event that the ParameterBuilder is given no values,
     * that an empty array (rather than a null array) is returned.
     */
    public void testEmptyParameterBuilder()
    {
        final Parameter[] parameters = new Parameters().end();
        assertNotNull("Parameter array not null.", parameters);
        assertEquals("Parameter array length zero.", 0, parameters.length);
    }


    /**
     * Tests that when only one value is given to the ParameterBuilder, that it
     * returns only that one value.
     */
    public void testSingleParameters()
    {
        // SPID == 5
        {
            final Parameter[] parameters = new Parameters().spid(5).end();
            assertNotNull("Parameter array not null.", parameters);
            assertEquals("Parameter array length one.", 1, parameters.length);
            assertEquals("Parameter ID is for SPID.", ParameterID.SPID, parameters[0].parameterID);
            assertEquals("Parameter value is SPID == 5.", 5, parameters[0].value.intValue());
        }

        // SPID == 13
        {
            final Parameter[] parameters = new Parameters().spid(13).end();
            assertNotNull("Parameter array not null.", parameters);
            assertEquals("Parameter array length one.", 1, parameters.length);
            assertEquals("Parameter ID is for SPID.", ParameterID.SPID, parameters[0].parameterID);
            assertEquals("Parameter value is SPID == 13.", 13, parameters[0].value.intValue());
        }

        // MSISDN == "5551113333"
        {
            final Parameter[] parameters = new Parameters().msisdn("5551113333").end();
            assertNotNull("Parameter array not null.", parameters);
            assertEquals("Parameter array length one.", 1, parameters.length);
            assertEquals("Parameter ID is for SPID.", ParameterID.FIELD_LEVEL_MSISDN, parameters[0].parameterID);
            assertEquals("Parameter value is MSISDN == \"5551113333\".", "5551113333",
                parameters[0].value.stringValue());
        }

        // MSISDN == "99112233"
        {
            final Parameter[] parameters = new Parameters().msisdn("99112233").end();
            assertNotNull("Parameter array not null.", parameters);
            assertEquals("Parameter array length one.", 1, parameters.length);
            assertEquals("Parameter ID is for SPID.", ParameterID.FIELD_LEVEL_MSISDN, parameters[0].parameterID);
            assertEquals("Parameter value is MSISDN == \"99112233\".", "99112233", parameters[0].value.stringValue());
        }
    }


    /**
     * Tests that when two values are given to the ParameterBuilder, that it
     * returns only those two values.
     */
    public void testMultipleParameters()
    {
        // SPID == 13, MSISDN == "5551113333"
        final Parameter spid;
        final Parameter msisdn;

        {
            final Parameter[] parameters = new Parameters().spid(13).msisdn("5551113333").end();

            assertNotNull("Parameter array not null.", parameters);
            assertEquals("Parameter array length 2.", 2, parameters.length);

            if (ParameterID.SPID == parameters[0].parameterID)
            {
                spid = parameters[0];
                msisdn = parameters[1];
            }
            else
            {
                spid = parameters[1];
                msisdn = parameters[0];
            }
        }

        assertEquals("Parameter ID is for SPID.", ParameterID.SPID, spid.parameterID);
        assertEquals("Parameter value is SPID == 13.", 13, spid.value.intValue());

        assertEquals("Parameter ID is for SPID.", ParameterID.FIELD_LEVEL_MSISDN, msisdn.parameterID);
        assertEquals("Parameter value is MSISDN == \"5551113333\".", "5551113333", msisdn.value.stringValue());
    }

    /**
     * Tests that when the same value is passed in twice, only the latter is
     * stored.
     */
    public void testParameterOverride()
    {
        final Parameter[] parameters = new Parameters().spid(3).spid(5).end();
        assertNotNull("Parameter array not null.", parameters);
        assertEquals("Parameter array length one.", 1, parameters.length);
        assertEquals("Parameter ID is for SPID.", ParameterID.SPID, parameters[0].parameterID);
        assertEquals("Parameter value is SPID == 5.", 5, parameters[0].value.intValue());
    }

}
