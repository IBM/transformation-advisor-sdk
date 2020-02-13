/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.validation;

import java.io.File;

public class TaCollectionZipValidator {

    private static final String FILE_XML = ".xml";
    private static final String FILE_JSON = ".json";
    private static final String FILE_LOG = ".log";
    private static final String FILE_ZIP = ".zip";

    public static boolean validateCollection(String zipFilePath){
        boolean isValid = true;
        if (!zipFilePath.endsWith(FILE_ZIP)) {
            System.out.println("Input collection file path is not a zip file");
            isValid = false;
        }
        File collectionFile = new File(zipFilePath);
        if (!collectionFile.exists()) {
            System.out.println("Input collection file does not exist");
            isValid = false;
        }
        return isValid;
    }
}
