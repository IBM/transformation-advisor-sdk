package com.ibm.ta.sdk.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.spi.plugin.PluginProvider;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.util.Util;
import freemarker.ext.dom.NodeModel;
import org.tinylog.Logger;
import freemarker.template.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.ibm.ta.sdk.core.util.Constants.*;

public class FreeMarkerTemplateResolver {

    private PluginProvider pluginProvider;
    private File assessmentUnitDir;
    private String middleware;
    private Configuration cfg;
    private File migrationDir;
    private HashMap data;
    private File templateFileDir;

    public FreeMarkerTemplateResolver(PluginProvider pluginProvider, File assessmentUnitDir, JsonObject envJson) {
        this.pluginProvider = pluginProvider;
        this.assessmentUnitDir = assessmentUnitDir;
        this.middleware = pluginProvider.getMiddleware();
        initFMConfig();
        this.migrationDir = new File(assessmentUnitDir.getAbsolutePath() + File.separator + "migrationBundle");
        if (!migrationDir.exists()) {
            migrationDir.mkdir();
        }
        this.data = new HashMap();
        initData(envJson);
    }

    public FreeMarkerTemplateResolver(File templateFileDir, File assessmentUnitDir, JsonObject envJson) {
        this.assessmentUnitDir = assessmentUnitDir;
        this.templateFileDir = templateFileDir;
        this.middleware = envJson.get(ENV_MIDDLEWARE_NAME).getAsString();
        initFMConfig();
        this.migrationDir = new File(assessmentUnitDir.getAbsolutePath() + File.separator + "migrationBundle");
        if (!migrationDir.exists()) {
            migrationDir.mkdir();
        }
        this.data = new HashMap();
        initData(envJson);
    }

    private void initFMConfig(){
        this.cfg = new Configuration(Configuration.VERSION_2_3_30);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        //cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        cfg.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
    }

    private void initData(JsonObject envJson){
        String key = ENVIRONMENT_JSON.replace('.','_');
        this.data.put(key, new Gson().fromJson(envJson, HashMap.class));
        File recommandationJson = new File(this.assessmentUnitDir.getParentFile().getAbsolutePath()+
                File.separator+RECOMMENDATIONS_JSON);
        if (recommandationJson.exists()) {
            try {
                key = RECOMMENDATIONS_JSON.replace('.','_');
                JsonObject recJsonContent = GenericUtil.getJsonObj(new TypeToken<JsonObject>(){}, recommandationJson.toPath());
                this.data.put(key,new Gson().fromJson(recJsonContent, HashMap.class));
                Logger.debug("insert to recommendations.json file to data mode with key="+key);
            } catch (IOException e) {
                Logger.error("error to load recommendations.json file: "+recommandationJson, e);
            }
        }
        for (String sFileName: this.assessmentUnitDir.list()) {
            key = sFileName.replace('.','_');
            //key = key.replaceAll(this.assessmentUnitDir.getName(), "");
            if (sFileName.endsWith(".json")) {
                try {
                    JsonObject jsonContent = GenericUtil.getJsonObj(
                            new TypeToken<JsonObject>(){},
                            new File(assessmentUnitDir.getAbsolutePath()+File.separator+sFileName).toPath());
                    this.data.put(key,new Gson().fromJson(jsonContent, HashMap.class));
                    Logger.debug("insert json file to data mode with key="+key);
                } catch (IOException e) {
                    Logger.error("error to load json file: "+sFileName, e);
                }
            }
            if (sFileName.endsWith(".xml")) {
                try {
                    NodeModel fileContent = NodeModel.parse(new File(assessmentUnitDir.getAbsolutePath()+File.separator+sFileName));
                    this.data.put(key,fileContent);
                    Logger.debug("insert xml file to data mode with key="+key);
                    Logger.debug("fileContent="+fileContent);
                } catch (SAXException e) {
                    Logger.error("error to load xml file: "+sFileName, e);
                } catch (IOException e) {
                    Logger.error("error to load xml file: "+sFileName, e);
                } catch (ParserConfigurationException e) {
                    Logger.error("error to load xml file: "+sFileName, e);
                }
            }
        }
    }

    public void resolveTemplatesForTargets(List<String> selectedTargets) throws TAException {
        List<String> targets = new ArrayList<>();
        try {
            targets = Arrays.asList(GenericUtil.getResourceListing(getClass(),this.middleware+"/templates/"));
            // Remove any targets in not in the selectedTargets list. Include all targets if
            // selectedGTargets is null or empty
            if (selectedTargets != null && selectedTargets.size() > 0) {
                for (String selectedTarget : selectedTargets) {
                    if (!targets.contains(selectedTarget)) {
                        throw new TAException("No template found with target name:" + selectedTarget);
                    }
                }

                // All selected targets found. Only resolve template for these targets
                targets = selectedTargets;
            }
        } catch (URISyntaxException e) {
            throw new TAException("Failed to load target template files in plugin provider "+this.pluginProvider.getClass(),e);
        } catch (IOException e) {
            throw new TAException("Failed to load target template files in plugin provider "+this.pluginProvider.getClass(),e);
        }
        // no template for targets find in the plugin provide,  throw exception
        if (targets==null || targets.size()==0) {
            throw new TAException("Command migrate is not supported for plugin provider "+this.pluginProvider.getClass()+
                    "\n        No target template files found in plugin provider ");
        }

        for (String target : targets) {
            try {
                resolveTemplates(target);
            } catch (IOException e) {
                throw new TAException("Failed to resolve template files in plugin provider "+this.pluginProvider.getClass(),e);
            } catch (URISyntaxException e) {
                throw new TAException("Failed to resolve template files in plugin provider "+this.pluginProvider.getClass(),e);
            } catch (TemplateException e) {
                throw new TAException("Failed to resolve template files in plugin provider "+this.pluginProvider.getClass(),e);
            }
        }
    }

    public void resolveTemplates(String target) throws IOException, URISyntaxException, TemplateException {
        String templatesDir = null;
        if (this.templateFileDir != null) {
            templatesDir = this.templateFileDir.getCanonicalPath()+File.separator;
            try {
                cfg.setDirectoryForTemplateLoading(this.templateFileDir);
            } catch (IOException ioe) {
                Logger.error("Cannot set the template dir in config.", ioe);
            }
        } else {
            templatesDir = this.middleware+"/templates/"+target+"/";
            cfg.setClassForTemplateLoading(getClass(), "/"+templatesDir);
        }
        String[] templateFiles = GenericUtil.getResourceListing(getClass(),templatesDir);
        File targetDir = new File (this.migrationDir+File.separator+target);
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        for (String templateFileName : templateFiles) {
            Logger.debug("template file is " + templateFileName);
            String targetFileName = templateFileName;
            if ( templateFileName.contains(".")) {
                targetFileName = templateFileName.substring(0, templateFileName.lastIndexOf('.'));
            }
            String targetFileType = templateFileName.substring(templateFileName.lastIndexOf('.')+1);
            File targetFile = new File (targetDir.getAbsolutePath()+File.separator+targetFileName);
            Logger.debug("targetFileName type is " + targetFileType);
            if (targetFile.exists()){
                targetFile.delete();
            }
            if (targetFileType.contains("ftl")) {
                Template temp = cfg.getTemplate(templateFileName);
                Writer out = new FileWriter(targetFile);
                temp.process(this.data, out);
                out.flush();
                out.close();
            } else if (targetFileType.equals("placeholder")) {
                String sourceFileName = targetFileName;
                Path filePath = GenericUtil.getFilePath(templatesDir+templateFileName);
                String contents = GenericUtil.readFileToString(filePath);
                Logger.debug("place holder file content is " + contents);
                if (contents != null && contents.contains("=")) {
                    sourceFileName = contents.split("=")[1];
                }
                Logger.debug("place holder source file name is " + sourceFileName);
                File sourceFile = new File(this.assessmentUnitDir.getAbsolutePath()+File.separator+sourceFileName);
                if (sourceFile.exists()) {
                    Files.copy(sourceFile.toPath(), targetFile.toPath());
                } else {
                    Logger.warn("cannot find the source file " + sourceFile.getAbsolutePath() +
                            " of the placeholder file " + sourceFile + " in the collection.");
                }

            } else {
                //copy file to migration target dir
                Path sourceFile = GenericUtil.getFilePath(templatesDir+templateFileName);
                targetFile = new File (targetDir.getAbsolutePath()+File.separator+templateFileName);
                Files.copy(sourceFile, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        // zip output dir
        String zipFileName = this.assessmentUnitDir.getName()+"_"+target+ ".zip";
        File zipFile = new File(this.migrationDir, zipFileName);
        if (zipFile.exists()){
            zipFile.delete();
        }
        Util.zipDir(zipFile.toPath(), targetDir);
    }
}
