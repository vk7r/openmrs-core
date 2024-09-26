/*
 * This is the refactored version of the Patient class located in
 * "openmrs-core/openmrs-core/api/src/main/java/org/openmrs/Patient.java"
 * With improvements being made regarding the system log completeness.
 */



/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.ContainedIn;

/**
 * Defines a Patient in the system. A patient is simply an extension of a person and all that that
 * implies.
 * 
 * @version 2.0
 */
@Audited
public class Patient_refactored extends Person {

    private static final Logger log = LoggerFactory.getLogger(Patient_refactored.class);
    
    public static final long serialVersionUID = 93123L;
    
    private Integer patientId;
    
    private String allergyStatus = Allergies.UNKNOWN;
    
    @ContainedIn
    private Set<PatientIdentifier> identifiers;

    // Constructors

    /** default constructor */
    public Patient_refactored() {
        log.debug("Creating a new Patient_refactored object");
        setPatient(true);
    }

    /**
     * This constructor creates a new Patient object from the given {@link Person} object.
     *
     * @param person the person object to copy onto a new Patient
     */
    public Patient_refactored(Person person) {
        super(person);
        if (person != null) {
            this.patientId = person.getPersonId();
            if (person.getUuid() != null) {
                this.setUuid(person.getUuid());
            }
        }
        log.debug("Patient_refactored created from Person with id: {}", person != null ? person.getPersonId() : "null");
        setPatient(true);
    }

    /**
     * Constructor with default patient id
     *
     * @param patientId
     */
    public Patient_refactored(Integer patientId) {
        super(patientId);
        this.patientId = patientId;
        log.debug("Patient_refactored created with patientId: {}", patientId);
        setPatient(true);
    }

    /**
     * Clone constructor.
     *
     * @param patient the patient object to copy onto a new Patient
     */
    public Patient_refactored(Patient_refactored patient) {
        super(patient);
        this.patientId = patient.getPatientId();
        this.allergyStatus = patient.getAllergyStatus();
        log.debug("Cloning Patient_refactored object with patientId: {}", patient.getPatientId());

        Set<PatientIdentifier> newIdentifiers = new TreeSet<>();
        for (PatientIdentifier pid : patient.getIdentifiers()) {
            PatientIdentifier identifierClone = (PatientIdentifier) pid.clone();
            identifierClone.setPatient(this);
            newIdentifiers.add(identifierClone);
        }
        this.identifiers = newIdentifiers;
    }

    // Property accessors

    /**
     * @return internal identifier for patient
     */
    public Integer getPatientId() {
        if (this.patientId == null) {
            log.debug("Patient ID is null, fetching from personId");
            this.patientId = getPersonId();
        }
        log.debug("Returning patientId: {}", this.patientId);
        return this.patientId;
    }

    /**
     * Sets the internal identifier for a patient.
     *
     * @param patientId
     */
    public void setPatientId(Integer patientId) {
        log.debug("Setting patientId: {}", patientId);
        super.setPersonId(patientId);
        this.patientId = patientId;
    }

    /**
     * Returns allergy status for patient
     *
     * @return current allergy status
     */
    public String getAllergyStatus() {
        log.debug("Returning allergyStatus: {}", this.allergyStatus);
        return this.allergyStatus;
    }

    /**
     * Sets the allergy status for a patient.
     *
     * @param allergyStatus
     */
    public void setAllergyStatus(String allergyStatus) {
        log.debug("Setting allergyStatus: {}", allergyStatus);
        this.allergyStatus = allergyStatus;
    }

    /**
     * Overrides the parent setPersonId to set patientId correctly.
     */
    @Override
    public void setPersonId(Integer personId) {
        log.debug("Setting personId (and patientId): {}", personId);
        super.setPersonId(personId);
        this.patientId = personId;
    }

    /**
     * Get all of this patient's identifiers
     *
     * @return Set of all identifiers for this patient
     */
    public Set<PatientIdentifier> getIdentifiers() {
        if (identifiers == null) {
            log.debug("Identifiers set is null, initializing new TreeSet");
            identifiers = new TreeSet<>();
        }
        log.debug("Returning identifiers set, size: {}", identifiers.size());
        return this.identifiers;
    }

    /**
     * Update all identifiers for patient
     *
     * @param identifiers Set of identifiers to update
     */
    public void setIdentifiers(Set<PatientIdentifier> identifiers) {
        log.debug("Setting identifiers, size: {}", identifiers.size());
        this.identifiers = identifiers;
    }

    /**
     * Adds this PatientIdentifier if the patient doesn't contain it already
     *
     * @param patientIdentifier
     */
    public void addIdentifier(PatientIdentifier patientIdentifier) {
        if (patientIdentifier != null) {
            log.debug("Adding PatientIdentifier: {}", patientIdentifier);
            patientIdentifier.setPatient(this);
            for (PatientIdentifier currentId : getActiveIdentifiers()) {
                if (currentId.equalsContent(patientIdentifier)) {
                    log.debug("Duplicate PatientIdentifier detected, skipping: {}", patientIdentifier);
                    return;
                }
            }
        }
        getIdentifiers().add(patientIdentifier);
    }

    /**
     * Adds a collection of PatientIdentifiers if the patient doesn't contain them already.
     *
     * @param patientIdentifiers
     */
    public void addIdentifiers(Collection<PatientIdentifier> patientIdentifiers) {
        log.debug("Adding collection of PatientIdentifiers, size: {}", patientIdentifiers.size());
        for (PatientIdentifier identifier : patientIdentifiers) {
            addIdentifier(identifier);
        }
    }

    /**
     * Removes the given identifier from this patient's list of identifiers.
     *
     * @param patientIdentifier the identifier to remove
     */
    public void removeIdentifier(PatientIdentifier patientIdentifier) {
        if (patientIdentifier != null) {
            log.debug("Removing PatientIdentifier: {}", patientIdentifier);
            getIdentifiers().remove(patientIdentifier);
        }
    }

    /**
     * Returns the first "preferred" identifier for a patient.
     *
     * @return the preferred patient identifier or null
     */
    public PatientIdentifier getPatientIdentifier() {
        log.debug("Fetching preferred patient identifier");
        if (!getIdentifiers().isEmpty()) {
            for (PatientIdentifier id : getIdentifiers()) {
                if (id.getPreferred() && !id.getVoided()) {
                    log.debug("Returning preferred identifier: {}", id);
                    return id;
                }
            }
            for (PatientIdentifier id : getIdentifiers()) {
                if (!id.getVoided()) {
                    log.debug("Returning non-voided identifier: {}", id);
                    return id;
                }
            }
        }
        log.debug("No identifier found");
        return null;
    }
	
	/**
	 * Returns only the non-voided identifiers for this patient. If you want <u>all</u> identifiers,
	 * use {@link #getIdentifiers()}
	 * 
	 * @return list of non-voided identifiers for this patient
	 * @see #getIdentifiers()
	 * <strong>Should</strong> return preferred identifiers first in the list
	 */
	public List<PatientIdentifier> getActiveIdentifiers() {
		List<PatientIdentifier> ids = new ArrayList<>();
		List<PatientIdentifier> nonPreferred = new LinkedList<>();
		for (PatientIdentifier pi : getIdentifiers()) {
			if (!pi.getVoided()) {
				if (pi.getPreferred()) {
					ids.add(pi);
				} else {
					nonPreferred.add(pi);
				}
			}
		}
		ids.addAll(nonPreferred);
		return ids;
	}
	
	/**
	 * Returns only the non-voided identifiers for this patient. If you want <u>all</u> identifiers,
	 * use {@link #getIdentifiers()}
	 * 
	 * @return list of non-voided identifiers for this patient
	 * @param pit PatientIdentifierType
	 * @see #getIdentifiers()
	 */
	public List<PatientIdentifier> getPatientIdentifiers(PatientIdentifierType pit) {
		List<PatientIdentifier> ids = new ArrayList<>();
		for (PatientIdentifier pi : getIdentifiers()) {
			if (!pi.getVoided() && pit.equals(pi.getIdentifierType())) {
				ids.add(pi);
			}
		}
		return ids;
	}
	
	@Override
	public String toString() {
		return "Patient#" + patientId;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getPatientId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setPatientId(id);
	}
	
	/**
	 * Returns the person represented
	 * 
	 * @return the person represented by this object
	 * @since 1.10.0
	 */
	public Person getPerson() {
		return this;
	}
	
}
