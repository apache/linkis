package org.apache.linkis.engineconnplugin.datax.client;

import com.alibaba.datax.core.util.container.JarLoader;
import org.apache.linkis.common.utils.ZipUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Random;

public class LinkisDataxClient {
    private static final Logger LOG = LoggerFactory.getLogger(LinkisDataxClient.class);
    private File dataxHome;
    private Class dataxEngineClass;
    public LinkisDataxClient run(String code,String appHome) throws Throwable {
        init(appHome);
        String jobId = String.valueOf(new Random(101).nextInt(1000000));
        String[] args= {"-mode","standalone","-jobid",jobId,"-job",generateExecFile(code,jobId).getAbsolutePath()};
        JarLoader jarLoader = new JarLoader(new String[]{dataxHome+"/lib"});
        dataxEngineClass=jarLoader.loadClass("com.alibaba.datax.DataxEngine");
        dataxEngineClass.getDeclaredMethod("main",args.getClass()).invoke(null,(Object) args);
        return this;
    }


    private void init(String appHome) throws IOException {
        dataxHome = new File(appHome+"/datax");
        try {
            File reader = new File(dataxHome.getAbsolutePath()+"/plugin/reader");
            File writer = new File(dataxHome.getAbsolutePath()+"/plugin/writer");
            File log = new File(dataxHome.getAbsolutePath()+"/log");
            File job = new File(dataxHome.getAbsolutePath()+"/job");
            reader.mkdirs();
            writer.mkdirs();
            log.mkdirs();
            job.mkdirs();
            System.setProperty("datax.home",dataxHome.getAbsolutePath());
            LOG.info("Create Data Home:"+dataxHome.getAbsolutePath());
            File home = new File(dataxHome.getAbsolutePath().substring(0,dataxHome.getAbsolutePath().lastIndexOf("/")));
            File[] dirs = home.listFiles();
            for (int i = 0; i <  dirs.length; i++) {
                String fileName = dirs[i].getName();
                if(fileName.equals("core") ||
                        fileName.startsWith("reader") || fileName.startsWith("writer")) {
                    File[] subFiles = dirs[i].listFiles();
                    for (int j = 0; j < subFiles.length; j++) {
                        if(subFiles[j].getName().endsWith(".zip")) {
                            ZipUtils.fileToUnzip(dirs[i].getAbsolutePath() + "/" + subFiles[j].getName(), dirs[i].getAbsolutePath());
                            subFiles[j].delete();
                        }
                    }
                    if (fileName.startsWith("reader")) {
                        File readerFile = new File(dirs[i].getAbsolutePath() + "/plugin/reader").listFiles()[0];
                        File linkFile = new File(reader.getAbsolutePath() + "/" + readerFile.getName());
                        if(linkFile.exists()){
                            linkFile.delete();
                        }
                        Files.createSymbolicLink(FileSystems.getDefault().getPath(reader.getAbsolutePath() + "/" + readerFile.getName()), FileSystems.getDefault().getPath(readerFile.getAbsolutePath()));
                    } else if (fileName.startsWith("writer")) {
                        File writerFile = new File(dirs[i].getAbsolutePath() + "/plugin/writer").listFiles()[0];
                        File linkFile = new File(writer.getAbsolutePath() + "/" + writerFile.getName());
                        if(linkFile.exists()){
                            linkFile.delete();
                        }
                        Files.createSymbolicLink(FileSystems.getDefault().getPath(writer.getAbsolutePath() + "/" + writerFile.getName()), FileSystems.getDefault().getPath(writerFile.getAbsolutePath()));
                    }else if(fileName.equals("core")){
                        File confFile = new File(dirs[i].getAbsolutePath() + "/conf");
                        File libFile = new File(dirs[i].getAbsolutePath() + "/lib");
                        File linkConfFile = new File(dataxHome+"/conf");
                        File linkLibFile = new File(dataxHome+"/lib");
                        if(linkConfFile.exists()){
                            linkConfFile.delete();
                        }
                        if(linkLibFile.exists()){
                            linkLibFile.delete();
                        }
                        Files.createSymbolicLink(FileSystems.getDefault().getPath(linkConfFile.getAbsolutePath()), FileSystems.getDefault().getPath(confFile.getAbsolutePath()));
                        Files.createSymbolicLink(FileSystems.getDefault().getPath(linkLibFile.getAbsolutePath()), FileSystems.getDefault().getPath(libFile.getAbsolutePath()));
                    }
                }
            }
        }catch (Exception e){
            LOG.error("Init Datax Engine Failure"+e.getMessage());
            throw e;
        }
    }

    private File generateExecFile(String code,String jobId) throws IOException {
        String jobFileName = jobId+"_"+System.currentTimeMillis();
        File execFile = new File(dataxHome.getAbsolutePath()+"/job/"+jobFileName);
        execFile.createNewFile();
        FileUtils.writeStringToFile(execFile,code);
        return execFile;
    }

    public void destory(){
        Method method = null;
        try {
            method = dataxEngineClass.getDeclaredMethod("close");
            method.invoke(null);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOG.error("Close Engine Failure:"+e.getMessage());
        }
    }


}
