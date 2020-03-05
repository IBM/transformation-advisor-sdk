/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.recommendation;

/**
 * 
 * This is the broad definition of Complexity ratings.
 * 
 * Complex: Requires significant effort to prepare for migration
 * Moderate: Requires moderate effort to prepare for migration
 * Simple: Requires low or no effort to prepare for migration
 * 
 * The actual meaning for a particular domain/middleware will be 
 * defined by the Complexity Rules
 * @see ComplexityContribution
 */
public enum ComplexityRating {
  simple,
  moderate,
  complex;
}
