/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.collect;

import java.util.List;

/**
 * {@link ContentMask} is used to mask content in the {@link AssessmentUnit#getConfigFiles()} config files collected
 * in the assessment unit. These files may contain sensitive information, such as passwords, that needs to be
 * masked before they could be copied off of the system.
 *
 */
public interface ContentMask {
  /**
   * Matching content will be replaced with this {@code String} {@link #MASK}
   */
  public static final String MASK = "******";


  /**
   * Get a list of file names that the content mask will be applied to. The file names could be
   * a regular expression. The file names must match the absolute path of the file.
   *
   * @return {@code List} of {@code String} of the files where the content mask will be applied. '*' to apply the
   * content mask to all the config files in the assessment unit.
   */
  List<String> getFiles();


  /**
   * Apply the {@link #MASK} to the matching text from the lines in the files. Return lines from the file that is updated
   * to with the masked text.
   *
   * @param content
   *        the lines from the matching file, as a {@code List}, where the mask will be applied to the matching
   *        content
   * @return updated lines from the file, as a {@code List}, containing the masked content
   */
  List<String> mask(List<String> content);
}
