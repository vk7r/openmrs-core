/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.Address;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.APIException;
import org.springframework.util.StringUtils;

/**
 * This class deals with {@link Person} objects when they are saved via a save* method in an Openmrs
 * Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP class. <br>
 *
 * @see RequiredDataHandler
 * @see SaveHandler
 * @see Person
 * @since 1.5
 */
@Handler(supports = Person.class)
public class PersonSaveHandler implements SaveHandler<Person> {

	/**
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 */
	@Override
	public void handle(Person person, User creator, Date dateCreated, String other) {
		handleAddresses(person);
		handleNames(person);
		handleAttributes(person);
		handleDeathStatus(person);
		handleVoidChecks(person, creator, dateCreated);
	}
	
	private void handleAddresses(Person person) {
		if (person.getAddresses() != null && !person.getAddresses().isEmpty()) {
			Set<Address> blankAddresses = new HashSet<>();
			for (PersonAddress pAddress : person.getAddresses()) {
				if (pAddress.isBlank()) {
					blankAddresses.add(pAddress);
					continue;
				}
				pAddress.setPerson(person);
			}
			person.getAddresses().removeAll(blankAddresses);
		}
	}

	private void handleNames(Person person) {
		if (person.getNames() != null && !person.getNames().isEmpty()) {
			for (PersonName pName : person.getNames()) {
				pName.setPerson(person);
			}
		}
	}

	private void handleAttributes(Person person) {
		if (person.getAttributes() != null && !person.getAttributes().isEmpty()) {
			for (PersonAttribute pAttr : person.getAttributes()) {
				pAttr.setPerson(person);
			}
		}
	}

	private void handleDeathStatus(Person person) {
		if (!person.getDead() && person.getCauseOfDeath() != null) {
			person.setCauseOfDeath(null);
		}
	}

	private void handleVoidChecks(Person person, User creator, Date dateCreated) {
		if (person.getPersonVoided()) {
			if (!StringUtils.hasLength(person.getPersonVoidReason())) {
				throw new APIException("Person.voided.bit", new Object[] { person });
			}
			if (person.getPersonVoidedBy() == null) {
				person.setPersonVoidedBy(creator);
			}
			if (person.getPersonDateVoided() == null) {
				person.setPersonDateVoided(dateCreated);
			}
		} else {
			person.setPersonVoidedBy(null);
			person.setPersonVoidReason(null);
			person.setPersonDateVoided(null);
		}
	}
}
