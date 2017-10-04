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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.IdentifierSequenceHome;
import com.trilogy.app.crm.bean.IdentifierSequenceTransientHome;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationHome;
import com.trilogy.app.crm.bean.account.AccountIdentificationTransientHome;
import com.trilogy.app.crm.bean.account.ContactHome;
import com.trilogy.app.crm.bean.account.ContactTransientHome;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionIdentificationResult;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResult;
import com.trilogy.app.crm.bean.duplicatedetection.IdentificationDetectionCriteria;
import com.trilogy.app.crm.bean.duplicatedetection.IdentificationDetectionCriteriaProperty;
import com.trilogy.app.crm.home.account.AccountLazyLoadedPropertyUpdateHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.utils.TransientSequenceIdentifiedSettingHome;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;
import com.trilogy.framework.xhome.beans.DefaultFacetMgr;
import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.beans.ParentClassFacetMgr;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-07-05
 */
public class IdentificationDuplicateDetectionCommandTest extends
    ContextAwareTestCase
{

	/**
	 * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#setUp()
	 */
	@Override
	@BeforeClass
	protected void setUp()
	{
		super.setUp();
		final Context context = getContext().createSubContext();
		context.put(FacetMgr.class, new ParentClassFacetMgr(
		    new DefaultFacetMgr()));
		Home home = new IdentifierSequenceTransientHome(context);
		context.put(IdentifierSequenceHome.class, home);

		final IdentifierEnum identifier = new IdentifierEnum(99, "XCONTACT","");
		home = new ContactTransientHome(context);
		home = new TransientFieldResettingHome(context, home);
		try
		{
			home =
			    new TransientSequenceIdentifiedSettingHome(context, home,
			        identifier);
		}
		catch (final HomeException e)
		{
			fail("failed to setup the ContactHome");
		}
		context.put(ContactHome.class, home);

		home = new AccountIdentificationTransientHome(context);
		context.put(AccountIdentificationHome.class, home);

		home = new SubscriberTransientHome(context);
		context.put(SubscriberHome.class, home);

		home =
		    new AccountLazyLoadedPropertyUpdateHome(context, new AccountTransientHome(
		        context));
		setContext(context);
		context.put(AccountHome.class, home);

		generateNames();

		try
		{
			createAccount(matchBan_, matchFirstName_, matchLastName_,
			    matchDOB_, AccountStateEnum.ACTIVE, accountType_);

		}
		catch (final Exception exception)
		{
			fail("Exception caught: " + exception);
		}
	}

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.IdentificationDuplicateDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)}
	 * .
	 */
	@Test
	public void testFindDuplicatesSingleMatch()
	{
		Map result = null;
		Object obj;
		DuplicateAccountDetectionResult row;

		try
		{
			createAccount(matchBan_ + 100, noMatchFirstName_, noMatchLastName_,
			    noMatchDOB_, AccountStateEnum.ACTIVE, accountType_);
			createAccount(matchBan_ + 200, noMatchFirstName_, noMatchLastName_,
			    noMatchDOB_, AccountStateEnum.ACTIVE, accountType_);
			addAccountId(matchBan_, 0, idType1_, idNumber1_);
			addAccountId(matchBan_ + 100, 0, idType1_, idNumber2_);
			addAccountId(matchBan_ + 200, 0, idType2_, idNumber1_);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		// test match
		final IdentificationDetectionCriteria criteria =
		    new IdentificationDetectionCriteria();
		criteria.setSpid(spid_);
		final List properties = new ArrayList();
		final IdentificationDetectionCriteriaProperty property =
		    new IdentificationDetectionCriteriaProperty();
		property.setIdType(idType1_);
		property.setIdNumber(idNumber1_);
		properties.add(property);

		criteria.setIdentifications(properties);
		try
		{
			result = testCriteria(criteria);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(result.size(), 1);
		obj = result.get(0);
		assertTrue(obj instanceof DuplicateAccountDetectionResult);
		row = (DuplicateAccountDetectionResult) obj;
		assertEquals(row.getBan(), matchBan_);
		assertTrue(row.getFirstName().equals(matchFirstName_));
		assertTrue(row.getLastName().equals(matchLastName_));
		assertTrue(row.getDateOfBirth().equals(matchDOB_));
		assertTrue(row.getAddress().equals(concatAddress1_));
		assertTrue(row.getCity().equals(billingCity_));
		assertTrue(row.getAccountState() == AccountStateEnum.ACTIVE);
		assertEquals(row.getAccountType(), accountType_);
		assertNotNull(row.getIdentifications());
		assertEquals(row.getIdentifications().size(), 1);
		DuplicateAccountDetectionIdentificationResult id =
		    (DuplicateAccountDetectionIdentificationResult) row
		        .getIdentifications().get(0);
		assertEquals(id.getIdType(), idType1_);
		assertEquals(id.getIdNumber(), idNumber1_);
		assertTrue(id.getMatched());
	}

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.IdentificationDuplicateDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)}
	 * .
	 */
	@Test
	public void testFindDuplicatesMultiMatch()
	{
		Map result = null;
		DuplicateAccountDetectionResult row;

		try
		{
			createAccount(matchBan_ + 100, noMatchFirstName_, noMatchLastName_,
			    noMatchDOB_, AccountStateEnum.ACTIVE, accountType_);
			createAccount(matchBan_ + 200, noMatchFirstName_, noMatchLastName_,
			    noMatchDOB_, AccountStateEnum.ACTIVE, accountType_);
			addAccountId(matchBan_, 0, idType1_, idNumber1_);
			addAccountId(matchBan_ + 100, 0, idType1_, idNumber1_);
			addAccountId(matchBan_ + 200, 0, idType1_, idNumber2_);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		// test match
		final IdentificationDetectionCriteria criteria =
		    new IdentificationDetectionCriteria();
		criteria.setSpid(spid_);
		final List properties = new ArrayList();
		final IdentificationDetectionCriteriaProperty property =
		    new IdentificationDetectionCriteriaProperty();
		property.setIdType(idType1_);
		property.setIdNumber(idNumber1_);
		properties.add(property);

		criteria.setIdentifications(properties);
		try
		{
			result = testCriteria(criteria);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(result.size(), 2);
		for (final Object obj : result.values())
		{
			assertTrue(obj instanceof DuplicateAccountDetectionResult);
			row = (DuplicateAccountDetectionResult) obj;
			if (row.getBan().equals(matchBan_))
			{
				assertTrue(row.getFirstName().equals(matchFirstName_));
				assertTrue(row.getLastName().equals(matchLastName_));
				assertTrue(row.getDateOfBirth().equals(matchDOB_));
				assertTrue(row.getAddress().equals(concatAddress1_));
			}
			else if (row.getBan().equals(matchBan_ + 100))
			{
				assertTrue(row.getFirstName().equals(noMatchFirstName_));
				assertTrue(row.getLastName().equals(noMatchLastName_));
				assertTrue(row.getDateOfBirth().equals(noMatchDOB_));
				assertTrue(row.getAddress().equals(concatAddress2_));
			}
			else
			{
				fail("Wrong BAN");
			}
			assertTrue(row.getCity().equals(billingCity_));
			assertTrue(row.getAccountState() == AccountStateEnum.ACTIVE);
			assertEquals(row.getAccountType(), accountType_);
			assertNotNull(row.getIdentifications());
			assertEquals(row.getIdentifications().size(), 1);
			DuplicateAccountDetectionIdentificationResult id =
			    (DuplicateAccountDetectionIdentificationResult) row
			        .getIdentifications().get(0);
			assertEquals(id.getIdType(), idType1_);
			assertEquals(id.getIdNumber(), idNumber1_);
			assertTrue(id.isMatched());
		}
	}

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.IdentificationDuplicateDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)}
	 * .
	 */
	@Test
	public void testFindDuplicatesWrongSpid()
	{
		Map result = null;
		final Object obj;
		final DuplicateAccountDetectionResult row;

		try
		{
			createAccount(matchBan_ + 100, noMatchFirstName_, noMatchLastName_,
			    noMatchDOB_, AccountStateEnum.ACTIVE, accountType_);
			addAccountId(matchBan_, 0, idType1_, idNumber1_);
			addAccountId(matchBan_ + 100, 0, idType1_, idNumber2_);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		// test match
		final IdentificationDetectionCriteria criteria =
		    new IdentificationDetectionCriteria();
		criteria.setSpid(spid_ + 3);
		final List properties = new ArrayList();
		final IdentificationDetectionCriteriaProperty property =
		    new IdentificationDetectionCriteriaProperty();
		property.setIdType(idType1_);
		property.setIdNumber(idNumber1_);
		properties.add(property);

		criteria.setIdentifications(properties);
		try
		{
			result = testCriteria(criteria);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.IdentificationDuplicateDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)}
	 * .
	 */
	@Test
	public void testFindDuplicatesNoMatch()
	{
		Map result = null;

		try
		{
			createAccount(matchBan_ + 100, noMatchFirstName_, noMatchLastName_,
			    noMatchDOB_, AccountStateEnum.ACTIVE, accountType_);
			addAccountId(matchBan_, 0, idType2_, idNumber1_);
			addAccountId(matchBan_ + 100, 0, idType1_, idNumber2_);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		// test match
		final IdentificationDetectionCriteria criteria =
		    new IdentificationDetectionCriteria();
		criteria.setSpid(spid_);
		final List properties = new ArrayList();
		final IdentificationDetectionCriteriaProperty property =
		    new IdentificationDetectionCriteriaProperty();
		property.setIdType(idType1_);
		property.setIdNumber(idNumber1_);
		properties.add(property);

		criteria.setIdentifications(properties);
		try
		{
			result = testCriteria(criteria);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.IdentificationDuplicateDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)}
	 * .
	 */
	@Test
	public void testFindDuplicatesMultiIdSingleCriteriaMatch()
	{
		Map result = null;
		DuplicateAccountDetectionResult row;

		try
		{
			createAccount(matchBan_ + 100, noMatchFirstName_, noMatchLastName_,
			    noMatchDOB_, AccountStateEnum.ACTIVE, accountType_);
			addAccountId(matchBan_, 0, idType1_, idNumber1_);
			addAccountId(matchBan_, 1, idType2_, idNumber2_);
			addAccountId(matchBan_ + 100, 0, idType1_, idNumber2_);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		// test match
		final IdentificationDetectionCriteria criteria =
		    new IdentificationDetectionCriteria();
		criteria.setSpid(spid_);
		final List properties = new ArrayList();
		final IdentificationDetectionCriteriaProperty property =
		    new IdentificationDetectionCriteriaProperty();
		property.setIdType(idType1_);
		property.setIdNumber(idNumber1_);
		properties.add(property);

		criteria.setIdentifications(properties);
		try
		{
			result = testCriteria(criteria);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(result.size(), 1);
		for (final Object obj : result.values())
		{
			assertTrue(obj instanceof DuplicateAccountDetectionResult);
			row = (DuplicateAccountDetectionResult) obj;
			assertEquals(row.getBan(), matchBan_);
			assertTrue(row.getFirstName().equals(matchFirstName_));
			assertTrue(row.getLastName().equals(matchLastName_));
			assertTrue(row.getDateOfBirth().equals(matchDOB_));
			assertTrue(row.getAddress().equals(concatAddress1_));
			assertTrue(row.getCity().equals(billingCity_));
			assertTrue(row.getAccountState() == AccountStateEnum.ACTIVE);
			assertEquals(row.getAccountType(), accountType_);
			assertNotNull(row.getIdentifications());
			assertEquals(row.getIdentifications().size(), 2);

			for (Object elem : row.getIdentifications())
			{
				DuplicateAccountDetectionIdentificationResult id =
				    (DuplicateAccountDetectionIdentificationResult) elem;
				if (id.getIdType() == idType1_)
				{
					assertEquals(id.getIdNumber(), idNumber1_);
					assertTrue(id.isMatched());
				}
				else if (id.getIdType() == idType2_)
				{
					assertEquals(id.getIdNumber(), idNumber2_);
					assertFalse(id.isMatched());
				}
				else
				{
					fail("Wrong ID Type");
				}
			}
		}
	}

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.IdentificationDuplicateDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)}
	 * .
	 */
	@Test
	public void testFindDuplicatesMultiIdMultiMatch()
	{
		Map result = null;
		DuplicateAccountDetectionResult row;

		try
		{
			createAccount(matchBan_ + 100, noMatchFirstName_, noMatchLastName_,
			    noMatchDOB_, AccountStateEnum.ACTIVE, accountType_);
			addAccountId(matchBan_, 0, idType1_, idNumber1_);
			addAccountId(matchBan_, 1, idType2_, idNumber2_);
			addAccountId(matchBan_ + 100, 0, idType1_, idNumber2_);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		// test match
		final IdentificationDetectionCriteria criteria =
		    new IdentificationDetectionCriteria();
		criteria.setSpid(spid_);
		final List properties = new ArrayList();
		IdentificationDetectionCriteriaProperty property =
		    new IdentificationDetectionCriteriaProperty();
		property.setIdType(idType1_);
		property.setIdNumber(idNumber1_);
		properties.add(property);
		property = new IdentificationDetectionCriteriaProperty();
		property.setIdType(idType2_);
		property.setIdNumber(idNumber2_);
		properties.add(property);
		criteria.setIdentifications(properties);
		try
		{
			result = testCriteria(criteria);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(result.size(), 1);
		for (final Object obj : result.values())
		{
			assertTrue(obj instanceof DuplicateAccountDetectionResult);
			row = (DuplicateAccountDetectionResult) obj;
			assertEquals(row.getBan(), matchBan_);
			assertTrue(row.getFirstName().equals(matchFirstName_));
			assertTrue(row.getLastName().equals(matchLastName_));
			assertTrue(row.getDateOfBirth().equals(matchDOB_));
			assertTrue(row.getAddress().equals(concatAddress1_));
			assertTrue(row.getCity().equals(billingCity_));
			assertTrue(row.getAccountState() == AccountStateEnum.ACTIVE);
			assertEquals(row.getAccountType(), accountType_);
			assertNotNull(row.getIdentifications());
			assertEquals(row.getIdentifications().size(), 2);

			for (Object elem : row.getIdentifications())
			{
				DuplicateAccountDetectionIdentificationResult id =
				    (DuplicateAccountDetectionIdentificationResult) elem;
				if (id.getIdType() == idType1_)
				{
					assertEquals(id.getIdNumber(), idNumber1_);
					assertTrue(id.isMatched());
				}
				else if (id.getIdType() == idType2_)
				{
					assertEquals(id.getIdNumber(), idNumber2_);
					assertTrue(id.isMatched());
				}
				else
				{
					fail("Wrong ID Type");
				}
			}
		}
	}

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.IdentificationDuplicateDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)}
	 * .
	 */
	@Test
	public void testFindDuplicatesMultiIdOnlyOneMatch()
	{
		Map result = null;
		DuplicateAccountDetectionResult row;

		try
		{
			createAccount(matchBan_ + 100, noMatchFirstName_, noMatchLastName_,
			    noMatchDOB_, AccountStateEnum.ACTIVE, accountType_);
			addAccountId(matchBan_, 0, idType1_, idNumber1_);
			addAccountId(matchBan_, 1, idType2_, idNumber2_);
			addAccountId(matchBan_ + 100, 0, idType1_, idNumber2_);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		// test match
		final IdentificationDetectionCriteria criteria =
		    new IdentificationDetectionCriteria();
		criteria.setSpid(spid_);
		final List properties = new ArrayList();
		IdentificationDetectionCriteriaProperty property =
		    new IdentificationDetectionCriteriaProperty();
		property.setIdType(idType1_);
		property.setIdNumber(idNumber1_);
		properties.add(property);
		property = new IdentificationDetectionCriteriaProperty();
		property.setIdType(idType2_);
		property.setIdNumber(idNumber3_);
		properties.add(property);

		criteria.setIdentifications(properties);
		try
		{
			result = testCriteria(criteria);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(result.size(), 1);
		for (final Object obj : result.values())
		{
			assertTrue(obj instanceof DuplicateAccountDetectionResult);
			row = (DuplicateAccountDetectionResult) obj;
			assertEquals(row.getBan(), matchBan_);
			assertTrue(row.getFirstName().equals(matchFirstName_));
			assertTrue(row.getLastName().equals(matchLastName_));
			assertTrue(row.getDateOfBirth().equals(matchDOB_));
			assertTrue(row.getAddress().equals(concatAddress1_));
			assertTrue(row.getCity().equals(billingCity_));
			assertTrue(row.getAccountState() == AccountStateEnum.ACTIVE);
			assertEquals(row.getAccountType(), accountType_);
			assertNotNull(row.getIdentifications());
			assertEquals(row.getIdentifications().size(), 2);

			for (Object elem : row.getIdentifications())
			{
				DuplicateAccountDetectionIdentificationResult id =
				    (DuplicateAccountDetectionIdentificationResult) elem;
				if (id.getIdType() == idType1_)
				{
					assertEquals(id.getIdNumber(), idNumber1_);
					assertTrue(id.isMatched());
				}
				else if (id.getIdType() == idType2_)
				{
					assertEquals(id.getIdNumber(), idNumber2_);
					assertTrue(id.isMatched());
				}
				else
				{
					fail("Wrong ID Type");
				}
			}
		}
	}

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.IdentificationDuplicateDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)}
	 * .
	 */
	@Test
	public void testFindDuplicatesMultiIdMultiSingleMatch()
	{
		Map result = null;
		DuplicateAccountDetectionResult row;

		try
		{
			createAccount(matchBan_ + 100, noMatchFirstName_, noMatchLastName_,
			    noMatchDOB_, AccountStateEnum.ACTIVE, accountType_);
			addAccountId(matchBan_, 0, idType1_, idNumber1_);
			addAccountId(matchBan_, 1, idType2_, idNumber3_);
			addAccountId(matchBan_ + 100, 0, idType1_, idNumber1_);
			addAccountId(matchBan_ + 100, 1, idType2_, idNumber2_);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		// test match
		final IdentificationDetectionCriteria criteria =
		    new IdentificationDetectionCriteria();
		criteria.setSpid(spid_);
		final List properties = new ArrayList();
		IdentificationDetectionCriteriaProperty property =
		    new IdentificationDetectionCriteriaProperty();
		property.setIdType(idType1_);
		property.setIdNumber(idNumber1_);
		properties.add(property);
		property = new IdentificationDetectionCriteriaProperty();
		property.setIdType(idType2_);
		property.setIdNumber(idNumber1_);
		properties.add(property);

		criteria.setIdentifications(properties);
		try
		{
			result = testCriteria(criteria);
		}
		catch (final Exception e)
		{
			fail("Exception caught : " + e);
		}

		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(result.size(), 4);
		for (final Object obj : result.values())
		{
			assertTrue(obj instanceof DuplicateAccountDetectionResult);
			row = (DuplicateAccountDetectionResult) obj;
			assertTrue(row.getCity().equals(billingCity_));
			assertTrue(row.getAccountState() == AccountStateEnum.ACTIVE);
			assertEquals(row.getAccountType(), accountType_);
			if (row.getBan().equals(matchBan_))
			{
				assertTrue(row.getFirstName().equals(matchFirstName_));
				assertTrue(row.getLastName().equals(matchLastName_));
				assertTrue(row.getDateOfBirth().equals(matchDOB_));
				assertTrue(row.getAddress().equals(concatAddress1_));
			}
			else if (row.getBan().equals(matchBan_ + 100))
			{
				assertTrue(row.getFirstName().equals(noMatchFirstName_));
				assertTrue(row.getLastName().equals(noMatchLastName_));
				assertTrue(row.getDateOfBirth().equals(noMatchDOB_));
				assertTrue(row.getAddress().equals(concatAddress2_));
			}
			assertNotNull(row.getIdentifications());
			assertEquals(row.getIdentifications().size(), 2);
			for (Object elem : row.getIdentifications())
			{
				DuplicateAccountDetectionIdentificationResult id =
				    (DuplicateAccountDetectionIdentificationResult) elem;
				if (id.getIdType() == idType1_)
				{
					assertEquals(id.getIdNumber(), idNumber1_);
					assertTrue(id.isMatched());
				}
				else if (id.getIdType() == idType2_)
				{
					assertFalse(id.isMatched());
					if (row.getBan().equals(matchBan_))
					{
						assertEquals(id.getIdNumber(), idNumber3_);
					}
					else if (row.getBan().equals(matchBan_ + 100))
					{
						assertEquals(id.getIdNumber(), idNumber2_);
					}
				}

			}
		}
	}

	private Map testCriteria(final IdentificationDetectionCriteria criteria)
	    throws HomeInternalException, HomeException
	{
		final IdentificationDuplicateDetectionCommand command =
		    new IdentificationDuplicateDetectionCommand(criteria);
		return command.findDuplicates(getContext());
	}

	private void addAccountId(final String ban, final int idx,
	    final int idType, final String idNumber) throws HomeInternalException,
	    HomeException
	{
		final AccountIdentification id = new AccountIdentification();
		id.setId(idx);
		id.setBAN(ban);
		id.setIdType(idType);
		id.setIdNumber(idNumber);
		final Home home =
		    (Home) getContext().get(AccountIdentificationHome.class);
		home.create(getContext(), id);
	}

	private Account createAccount(final String ban, final String firstName,
	    final String lastName, final Date dateOfBirth,
	    final AccountStateEnum state, final long accountType)
	    throws IOException, InstantiationException, HomeInternalException,
	    HomeException
	{
		final Home home = (Home) getContext().get(AccountHome.class);
		Account account =
		    (Account) XBeans.instantiate(Account.class, getContext());
		account.setFirstName(firstName);
		account.setLastName(lastName);
		account.setDateOfBirth(dateOfBirth);
		account.setSpid(spid_);
		account.setBillingAddress1(billingAddress1_ + (addressCounter_++));
		account.setBillingAddress2(billingAddress2_);
		account.setBillingAddress3(billingAddress3_);
		account.setBillingCity(billingCity_);
		account.setBillingCountry(billingCountry_);
		account.setBAN(ban);
		account.setState(state);
		account.setType(accountType);
		account = (Account) home.create(getContext(), account);
		return account;
	}

	private void generateNames()
	{
		spid_ = (int) (Math.random() * 100) + 1;
		matchFirstName_ = "First " + new Date().getTime();
		matchLastName_ = "Last " + new Date().getTime();
		noMatchFirstName_ = "First " + (new Date().getTime() + 100);
		noMatchLastName_ = "Last " + (new Date().getTime() + 100);

		final int ban = (int) (Math.random() * 500) + 1000;
		matchBan_ = String.valueOf(ban);

		accountType_ = (long) (Math.random() * 500);

		billingAddress1_ = "Billing ";
		billingAddress2_ = "Billing 2";
		billingAddress3_ = "Billing 3";
		billingCity_ = "Billing City";
		billingCountry_ = "Billing Country";

		concatAddress1_ =
		    billingAddress1_ + addressCounter_ + ", " + billingAddress2_ + ", "
		        + billingAddress3_;
		concatAddress2_ =
		    billingAddress1_ + (addressCounter_ + 1) + ", " + billingAddress2_
		        + ", " + billingAddress3_;

		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, (int) (Math.random() * 100) + 1900);
		calendar.set(Calendar.MONTH, (int) (Math.random() * 12)
		    + Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, (int) (Math.random() * calendar
		    .getActualMaximum(Calendar.DAY_OF_MONTH)));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		matchDOB_ = calendar.getTime();

		do
		{
			calendar.set(Calendar.YEAR, (int) (Math.random() * 100) + 1900);
			calendar.set(Calendar.MONTH, (int) (Math.random() * 12)
			    + Calendar.JANUARY);
			calendar.set(Calendar.DAY_OF_MONTH, (int) (Math.random() * calendar
			    .getActualMaximum(Calendar.DAY_OF_MONTH)));
		}
		while (calendar.getTimeInMillis() == matchDOB_.getTime());

		noMatchDOB_ = calendar.getTime();

		idType1_ = (int) (Math.random() * 50);
		idType2_ = idType1_ + 21;
		idNumber1_ = "ID" + new Date().getTime();
		idNumber2_ = "ID2" + new Date().getTime();
		idNumber3_ = "ID3" + new Date().getTime();
	}

	private String matchBan_;
	private String matchFirstName_;
	private String matchLastName_;
	private Date matchDOB_;
	private String noMatchFirstName_;
	private String noMatchLastName_;
	private Date noMatchDOB_;

	private String billingAddress1_;
	private String billingAddress2_;
	private String billingAddress3_;
	private String billingCity_;
	private String billingCountry_;

	int addressCounter_ = (int) (Math.random() * 50);
	private long accountType_;
	private String concatAddress1_;
	private String concatAddress2_;
	private int spid_;

	private int idType1_;
	private int idType2_;
	private String idNumber1_;
	private String idNumber2_;
	private String idNumber3_;
}
