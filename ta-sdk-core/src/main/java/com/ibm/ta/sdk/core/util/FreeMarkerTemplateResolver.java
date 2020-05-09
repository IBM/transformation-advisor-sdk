package com.ibm.ta.sdk.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.spi.plugin.PluginProvider;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.util.Util;
import freemarker.ext.dom.NodeModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import freemarker.template.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.HashMap;

public class FreeMarkerTemplateResolver {
    private Logger logger = LogManager.getLogger(getClass().getName());

    private PluginProvider pluginProvider;
    private File assessmentUnitDir;
    private String middleware;
    private Configuration cfg;
    private File migrationDir;
    private HashMap data;

    public FreeMarkerTemplateResolver(PluginProvider pluginProvider, File assessmentUnitDir, JsonObject envJson) {
        this.pluginProvider = pluginProvider;
        this.assessmentUnitDir = assessmentUnitDir;
        this.middleware = pluginProvider.getMiddleware();
        initFMConfig();
        this.migrationDir = new File(assessmentUnitDir.getAbsolutePath() + File.separator + "migrationBundle");
        if (!migrationDir.exists()) {
            migrationDir.mkdir();
        }
        this.data = new Gson().fromJson(envJson, HashMap.class);
        initData();
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
        cfg.setClassForTemplateLoading(getClass(), "/");
    }

    private void initData(){
        for (String sFileName: this.assessmentUnitDir.list()) {
            String key = sFileName.replace('.','_');
            //key = key.replaceAll(this.assessmentUnitDir.getName(), "");
            if (sFileName.endsWith(".json")) {
                try {
                    JsonObject jsonContent = GenericUtil.getJsonObj(
                            new TypeToken<JsonObject>(){},
                            new File(assessmentUnitDir.getAbsolutePath()+File.separator+sFileName).toPath());
                    this.data.put(key,new Gson().fromJson(jsonContent, HashMap.class));
                    logger.debug("insert to json file to data mode with key="+key);
                    logger.debug("fileContent="+jsonContent);
                } catch (IOException e) {
                    logger.error("error to load json file: "+sFileName, e);
                }
            }
            if (sFileName.endsWith(".xml")) {
                try {
                    NodeModel fileContent = NodeModel.parse(new File(assessmentUnitDir.getAbsolutePath()+File.separator+sFileName));
                    this.data.put(key,fileContent);
                    logger.debug("insert xml file to data mode with key="+key);
                    logger.debug("fileContent="+fileContent);
                } catch (SAXException e) {
                    logger.error("error to load xml file: "+sFileName, e);
                } catch (IOException e) {
                    logger.error("error to load xml file: "+sFileName, e);
                } catch (ParserConfigurationException e) {
                    logger.error("error to load xml file: "+sFileName, e);
                }
            }
        }
    }

    public void resolveTemplatesForAllTargsts() throws TAException {
        String[] targets = new String[0];
        try {
            targets = GenericUtil.getResourceListing(getClass(),this.middleware+"/templates/");
        } catch (URISyntaxException e) {
            throw new TAException("Failed to load target template files in plugin provider "+this.pluginProvider.getClass(),e);
        } catch (IOException e) {
            throw new TAException("Failed to load target template files in plugin provider "+this.pluginProvider.getClass(),e);
        }
        // no template for targets find in the plugin provide,  throw exception
        if (targets==null || targets.length==0) {
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
        String templatesDir = this.middleware+"/templates/"+target+"/";
        String[] templateFiles = GenericUtil.getResourceListing(getClass(),templatesDir);
        File targetDir = new File (this.migrationDir+File.separator+target);
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        for (String templateFileName : templateFiles) {
            logger.debug("template file is " + templateFileName);
            String targetFileName = templateFileName.substring(0, templateFileName.lastIndexOf('.'));
            String targetFileType = templateFileName.substring(templateFileName.lastIndexOf('.')+1);
            File targetFile = new File (targetDir.getAbsolutePath()+File.separator+targetFileName);
            logger.debug("targetFileName type is " + targetFileType);
            if (targetFile.exists()){
                targetFile.delete();
            }
            if (targetFileType.contains("ftl")) {
                Template temp = cfg.getTemplate(templatesDir+templateFileName);
                Writer out = new FileWriter(targetFile);
                temp.process(this.data, out);
                out.flush();
                out.close();
            } else if (targetFileType.equals("placeholder")) {
                Path filePath = Paths.get(getClass().getClassLoader().getResource(templatesDir+templateFileName).toURI());
                String contents = GenericUtil.readFileToString(filePath);
                String sourceFileName = contents.split("=")[1];
                logger.debug("place holder file content is " + contents);
                Files.copy(new File(this.assessmentUnitDir.getAbsolutePath()+File.separator+sourceFileName).toPath(),
                        targetFile.toPath());
            } else {
                //copy file to migration target dir
                Path sourceFile = Paths.get(getClass().getClassLoader().getResource(templatesDir+templateFileName).toURI());
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
