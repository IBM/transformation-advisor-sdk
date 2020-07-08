package com.ibm.ta.sdk.core.validation;

import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.core.assessment.json.TargetsJson;
import com.ibm.ta.sdk.core.util.Constants;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.validation.TaJsonFileValidator;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains a set of validation tests for the plugins. These tests should be executed during the test
 * phase when compiling the plugin. It intention is to ensure that the plugin is valid, that it conforms to the
 * expectations of the SDK APIs, and avoid runtime errors. It is up to the plugin developer's discretion whether they
 * want to fail the plugin's build when there is an error in these tests. See the <i>pom.xml</i> of the <i>Sample</i>
 * plugin for sample usage.
 */
public class PluginProviderValidationTest {
    private static final String MIDDLEWARE_NAME_PROP = "ta.sdk.middleware";
    private static final String PLUGIN_RESOURCES_DIR = "src" + File.separator + "main" + File.separator + "resources";

    /**
     * Checks that the targets.json file exists in the plugin, under a directory that has the same name as the
     * middleware name of the plugin. Also checks that the JSON schema of target.json is valid.
     */
    @Test
    public void validateTargetsJsonTest() {
        String middlewareName = getMiddlewareName();
        assertNotNull(middlewareName, "No system property for middleware('" + MIDDLEWARE_NAME_PROP + "' defined.");

        File targetJson = new File(PLUGIN_RESOURCES_DIR + File.separator + middlewareName, Constants.FILE_TARGETS_JSON);
        assertTrue(targetJson.exists(), Constants.FILE_TARGETS_JSON + " file not found in:" + targetJson.getAbsolutePath());

        try {
            TaJsonFileValidator.validateTarget(targetJson.getAbsolutePath());
        } catch (TAException e) {
            throw new AssertionFailedError("Validation of " + Constants.FILE_TARGETS_JSON + " failed.", e);
        }
    }

    /**
     * Checks that the issues.json file exists in the plugin, under a directory that has the same name as the
     * middleware name of the plugin. Also checks that the JSON schema of issues.json is valid.
     */
    @Test
    public void validateIssuesJsonTest() {
        String middlewareName = getMiddlewareName();
        assertNotNull(middlewareName, "No system property for middleware('" + MIDDLEWARE_NAME_PROP + "' defined.");

        File issuesJson = new File(PLUGIN_RESOURCES_DIR + File.separator + middlewareName, Constants.FILE_ISSUES_JSON);
        assertTrue(issuesJson.exists(), Constants.FILE_ISSUES_JSON + " file not found in:" + issuesJson.getAbsolutePath());

        try {
            TaJsonFileValidator.validateIssue(issuesJson.getAbsolutePath());
        } catch (TAException e) {
            throw new AssertionFailedError("Validation of " + Constants.FILE_ISSUES_JSON + " failed.", e);
        }
    }

    /**
     * Checks that the complexities.json file exists in the plugin, under a directory that has the same name as the
     * middleware name of the plugin. Also checks that the JSON schema of complexities.json is valid.
     */
    @Test
    public void validateComplexitiesJsonTest() {
        String middlewareName = getMiddlewareName();
        assertNotNull(middlewareName, "No system property for middleware('" + MIDDLEWARE_NAME_PROP + "' defined.");

        File complexitiesJson = new File(PLUGIN_RESOURCES_DIR + File.separator + middlewareName, Constants.FILE_COMPLEXITIES_JSON);
        assertTrue(complexitiesJson.exists(), Constants.FILE_COMPLEXITIES_JSON + " file not found in:" + complexitiesJson.getAbsolutePath());

        try {
            TaJsonFileValidator.validateComplexity(complexitiesJson.getAbsolutePath());
        } catch (TAException e) {
            throw new AssertionFailedError("Validation of " + Constants.FILE_COMPLEXITIES_JSON + " failed.", e);
        }
    }


    /**
     * Retrieves the list of targets from targets.json and checks that this list matches
     * the list of targets for the templates.
     */
    @Test
    public void validateTemplateTargetsTest() {
        String middlewareName = getMiddlewareName();
        assertNotNull(middlewareName, "No system property for middleware('" + MIDDLEWARE_NAME_PROP + "' defined.");

        File targetJson = new File(PLUGIN_RESOURCES_DIR + File.separator + middlewareName, Constants.FILE_TARGETS_JSON);
        assertTrue(targetJson.exists(), Constants.FILE_TARGETS_JSON + " file not found in:" + targetJson.getAbsolutePath());

        try {
            String templatesDir = middlewareName + File.separator + "templates";
            File templatesPath = new File(PLUGIN_RESOURCES_DIR, templatesDir);
            if (templatesPath.exists()) {
                String[] templateTargets = GenericUtil.getResourceListing(getClass(), templatesDir);

                // Not every target may have a template target
                // For every template target the name must match with a target
                TargetsJson targetsJson = GenericUtil.getJsonObj(new TypeToken<TargetsJson>(){}, targetJson.toPath());
                List<String> targets = targetsJson.getTargets().stream()
                        .map(t -> t.getTargetId())
                        .collect(Collectors.toList());
                for (String templateTarget : templateTargets) {
                    assertTrue(targets.contains(templateTarget), "Found template target with name '" + templateTarget +
                            "' but no matching name found in " + Constants.FILE_TARGETS_JSON);
                }
            }
        } catch (IOException e) {
            throw new AssertionFailedError("Failed to get targets from targets.json", e);
        } catch (URISyntaxException e) {
            throw new AssertionFailedError("Failed to get targets for the FreeMarker templates", e);
        }
    }

    private static String getMiddlewareName() {
        return System.getProperty(MIDDLEWARE_NAME_PROP);
    }
}
