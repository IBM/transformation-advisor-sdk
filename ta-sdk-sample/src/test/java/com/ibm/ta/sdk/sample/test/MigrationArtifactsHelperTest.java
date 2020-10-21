package com.ibm.ta.sdk.sample.test;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.core.util.MigrationArtifactsHelper;
import com.ibm.ta.sdk.spi.plugin.TADataCollector;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.util.Util;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MigrationArtifactsHelperTest {

    static final String MIDDLEWARE_NAME = "middleware";
    static final String COLLECTION_UNIT_NAME = "collection1";
    static final String ASSESS_UNIT_NAME = "assessmentUnit1";

    @BeforeEach
    void cleanOutpotDir() throws IOException {
        Path outputDir = Util.getOutputDir().toPath();
        if (outputDir.toFile().exists()) {
            Files.walk(outputDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    public void generateMigrationFilesForTargetTest() throws IOException, URISyntaxException, TemplateException, TAException {
        List<String> argus = new ArrayList<>();
        argus.add("run");
        argus.add("test");
        TADataCollector tadc = new TADataCollector();
        tadc.runCommand(MIDDLEWARE_NAME, argus);
        String templateFilePath = "src/main/resources/middleware/templates/targetA";
        String assessmentPath = "output/collection1/assessmentUnit1";
        File outputDir = Util.getOutputDir();
        File templateFileDir = new File(templateFilePath);
        File assessmentFileDir = new File(assessmentPath);
        JsonObject envJson = GenericUtil.getJsonObj(new TypeToken<JsonObject>(){}, new File (assessmentFileDir.getParent()+"/environment.json").toPath());
        assertTrue(templateFileDir.exists());
        assertTrue(templateFileDir.isDirectory());
        MigrationArtifactsHelper.generateMigrationFilesForTarget("targetA", templateFileDir, assessmentFileDir, envJson);
        String migrationBundleDir = outputDir+File.separator+COLLECTION_UNIT_NAME+File.separator+ASSESS_UNIT_NAME+"/migrationBundle/";
        File bundleZipFile = new File (migrationBundleDir+ASSESS_UNIT_NAME+"_targetA.zip");
        File bundleDir = new File (migrationBundleDir+"targetA/");
        assertTrue(bundleZipFile.exists());
        assertTrue(bundleZipFile.isFile());
        File serverXmlFile = new File(bundleDir.getAbsolutePath()+File.separator+"server.xml");
        assertTrue(serverXmlFile.exists());
        assertTrue(serverXmlFile.isFile());
        assertTrue(serverXmlFile.length() > 2);
    }
}

