package com.ibm.ta.sdk.core.validation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.ArrayList;
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

    /**
     * Iterates through each issues.json and checks the the category matches with a category defined
     * in the issue-categories.json.
     */
    @Test
    public void validateIssueCategoryTest() {
        String middlewareName = getMiddlewareName();
        assertNotNull(middlewareName, "No system property for middleware('" + MIDDLEWARE_NAME_PROP + "' defined.");

        // Read categories
        JsonObject catsO = null;
        try {
            File categoriesJsonFile = new File(PLUGIN_RESOURCES_DIR + File.separator + middlewareName, Constants.FILE_ISSUECATS_JSON);
            catsO = GenericUtil.getJson(categoriesJsonFile.toPath()).getAsJsonObject();
        } catch (IOException e) {
            throw new AssertionFailedError("Failed to parse " + Constants.FILE_ISSUECATS_JSON, e);
        }

        // Read issues.json and check each category
        JsonElement issuesE = null;
        try {
            File issuesJsonFile = new File(PLUGIN_RESOURCES_DIR + File.separator + middlewareName, Constants.FILE_ISSUES_JSON);
            issuesE = GenericUtil.getJson(issuesJsonFile.toPath());
        } catch (IOException e) {
            throw new AssertionFailedError("Failed to parse " + Constants.FILE_ISSUES_JSON, e);
        }

        JsonArray issuesA = issuesE.getAsJsonObject().get("issues").getAsJsonArray();
        for (JsonElement issueE : issuesA) {
            String category = issueE.getAsJsonObject().get("category").getAsString();
            JsonElement categoriesCatE = catsO.get(category);
            assertTrue(categoriesCatE != null, "No category with name '" + category + "' found in " + Constants.FILE_ISSUECATS_JSON);
            assertTrue(categoriesCatE.isJsonObject() && categoriesCatE.getAsJsonObject().get("title") != null, "Category '" + category + "' is not a valid JsonObject in " + Constants.FILE_ISSUECATS_JSON);
        }
    }

    /**
     * Iterates through each issues.json and checks that each ID is unique
     */
    @Test
    public void uniqueIssueIdTest() {
        String middlewareName = getMiddlewareName();
        assertNotNull(middlewareName, "No system property for middleware('" + MIDDLEWARE_NAME_PROP + "' defined.");

        // Read issues.json and check that the ID is unique
        JsonElement issuesE = null;
        try {
            File issuesJsonFile = new File(PLUGIN_RESOURCES_DIR + File.separator + middlewareName, Constants.FILE_ISSUES_JSON);
            issuesE = GenericUtil.getJson(issuesJsonFile.toPath());
        } catch (IOException e) {
            throw new AssertionFailedError("Failed to parse " + Constants.FILE_ISSUES_JSON, e);
        }

        List<String> issueIdList = new ArrayList<>();
        JsonArray issuesA = issuesE.getAsJsonObject().get("issues").getAsJsonArray();
        for (JsonElement issueE : issuesA) {
            String id = issueE.getAsJsonObject().get("id").getAsString();
            if (issueIdList.contains(id)) {
                assertTrue(false, "Duplicate issue with ID '" + id + "' found in " + Constants.FILE_ISSUES_JSON);
            }
            issueIdList.add(id);
        }
    }

    /**
     * Iterates through each issues.json and checks that a complexities.json entry exists for either the
     * issue ID or category
     */
    @Test
    public void issueComplexityExistTest() {
        String middlewareName = getMiddlewareName();
        assertNotNull(middlewareName, "No system property for middleware('" + MIDDLEWARE_NAME_PROP + "' defined.");

        // Read complexities
        JsonArray complexitiesA = null;
        try {
            File complexitiesJsonFile = new File(PLUGIN_RESOURCES_DIR + File.separator + middlewareName, Constants.FILE_COMPLEXITIES_JSON);
            complexitiesA = GenericUtil.getJson(complexitiesJsonFile.toPath()).getAsJsonObject().get("complexities").getAsJsonArray();
        } catch (IOException e) {
            throw new AssertionFailedError("Failed to parse " + Constants.FILE_ISSUECATS_JSON, e);
        }

        List<String> complexityIssuesList = new ArrayList<>();
        List<String> complexityCatsList = new ArrayList<>();
        for (JsonElement complexity : complexitiesA) {
            JsonElement complexityIssues = complexity.getAsJsonObject().get("issues");
            if (complexityIssues != null) {
                complexityIssues.getAsJsonArray().forEach(i -> complexityIssuesList.add(i.getAsString()));
            }
            JsonElement complexityCats = complexity.getAsJsonObject().get("issuesCategory");
            if (complexityCats != null) {
                complexityCats.getAsJsonArray().forEach(c -> complexityCatsList.add(c.getAsString()));
            }
        }

        // Read issues.json and check that the ID is unique
        JsonElement issuesE = null;
        try {
            File issuesJsonFile = new File(PLUGIN_RESOURCES_DIR + File.separator + middlewareName, Constants.FILE_ISSUES_JSON);
            issuesE = GenericUtil.getJson(issuesJsonFile.toPath());
        } catch (IOException e) {
            throw new AssertionFailedError("Failed to parse " + Constants.FILE_ISSUES_JSON, e);
        }

        JsonArray issuesA = issuesE.getAsJsonObject().get("issues").getAsJsonArray();
        for (JsonElement issueE : issuesA) {
            String id = issueE.getAsJsonObject().get("id").getAsString();
            String category = issueE.getAsJsonObject().get("category").getAsString();
            assertTrue(complexityIssuesList.contains(id) || complexityCatsList.contains(category),
                    "No matching complexity in " + Constants.FILE_COMPLEXITIES_JSON + " found for issue ID '" +
                    id + "' or issue category '" + category + "'" );
        }
    }

    private static String getMiddlewareName() {
        return System.getProperty(MIDDLEWARE_NAME_PROP);
    }
}
