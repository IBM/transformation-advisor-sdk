/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.recommendation;

/**
 * Issues are triggered when an application breaks 'migration to cloud' rules. 
 * Issues have three levels of severity, which signify how urgent it is to address an issue before migrating an application to the cloud.  
 * The issues are explained in detail in each application's analysis report.
 * 
 * Note: The actual values here will likely change from a color scheme to something else e.g. fire, smoke, smell
 * because RED issues are incorrectly assumed to be associated with Complexity
 * 
 * RED: Critical
 * Hover Text: Must be addressed before migration
 * Description: This issue must be addressed before migration or the application will break in the cloud environment.
 *
 * YELLOW: Potential
 * Hover Text: Must be investigated before migration
 * Description: This issue must be investigated before migration or the application may break in the cloud environment. 

 * GREEN: Suggested
 * Hover Text: Suggested to investigate after migration
 * Description: This issue should be investigated after migration or the application may function below an optimal level in the cloud environment. 
 */
public enum Severity {
  // TODO: Lowercase?
  Critical,
  Potential,
  Suggested;
}
