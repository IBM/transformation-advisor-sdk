package com.ibm.ta.sdk.core.util;

import com.google.gson.JsonObject;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class MigrationArtifactsHelper {
    public static void generateMigrationFilesForTarget(String target, File templateFileDir, File assessmentUnitDir, JsonObject envJson) throws TemplateException, IOException, URISyntaxException {
        FreeMarkerTemplateResolver fmtr = new FreeMarkerTemplateResolver(templateFileDir, assessmentUnitDir, envJson);
        fmtr.resolveTemplates(target);
    }
}
