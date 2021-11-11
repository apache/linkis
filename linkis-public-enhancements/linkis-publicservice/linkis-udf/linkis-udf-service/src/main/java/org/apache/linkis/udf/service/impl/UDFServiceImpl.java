/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.udf.service.impl;

import com.google.common.collect.Lists;
import org.apache.linkis.common.io.Fs;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.storage.FSFactory;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.udf.dao.UDFDao;
import org.apache.linkis.udf.entity.UDFInfo;
import org.apache.linkis.udf.entity.UDFManager;
import org.apache.linkis.udf.excepiton.UDFException;
import org.apache.linkis.udf.service.UDFService;
import org.apache.linkis.udf.utils.ConstantVar;
import org.apache.linkis.udf.utils.UdfConfiguration;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.apache.linkis.udf.utils.ConstantVar.UDF_JAR;


@Service
public class UDFServiceImpl implements UDFService {

    private static final Logger logger = Logger.getLogger(UDFServiceImpl.class);

    Map<String, Collection<Integer>> categoryToCodes = new HashedMap();
    {
        categoryToCodes.put(ConstantVar.FUNCTION, Lists.newArrayList(ConstantVar.FUNCTION_PY, ConstantVar.FUNCTION_SCALA));
        categoryToCodes.put(ConstantVar.UDF, Lists.newArrayList(UDF_JAR, ConstantVar.UDF_PY, ConstantVar.UDF_SCALA));
        categoryToCodes.put(ConstantVar.ALL, Lists.newArrayList(ConstantVar.FUNCTION_PY, ConstantVar.FUNCTION_SCALA, UDF_JAR, ConstantVar.UDF_PY, ConstantVar.UDF_SCALA));
    }

    @Autowired
    private UDFDao udfDao;


    @Override
    public UDFInfo addUDF(UDFInfo udfInfo, String userName) throws UDFException {
        logger.info(userName + " add udfInfo: " + udfInfo.getUdfName());
        if (userName.equals(udfInfo.getCreateUser())) {
            long count = udfDao.getSameNameCountByUser(udfInfo.getUdfName(), userName);
            if(count > 0) {
                throw new UDFException("The name of udf is the same name. Please rename it and rebuild it.(udf的名字重名，请改名后重建)");
            }
            udfInfo.setPath(StringUtils.replace(udfInfo.getPath(), "file://", ""));


            //If it is a jar package, do some basic verification(如果是jar包，做一些基本校验)
            if(udfInfo.getUdfType() == UDF_JAR && StringUtils.isNotBlank(udfInfo.getPath())){
                //validateJarFile(udfInfo, userName);
            }
            udfDao.addUDF(udfInfo);
            if(udfInfo.getLoad()){
                addLoadInfo(udfInfo.getId(), userName);
            }
        } else {
            throw new UDFException("Current user must be consistent with the user created(当前用户必须和创建用户一致)");
        }
        return udfInfo;
    }

    private void validateJarFile(UDFInfo udfInfo, String userName) throws UDFException {
        File targetFile = new File(UdfConfiguration.UDF_TMP_PATH().getValue() + udfInfo.getPath());
        FsPath fsPath = new FsPath("file://" + udfInfo.getPath());
        Fs remoteFs = FSFactory.getFsByProxyUser(fsPath,userName);
        try{
            remoteFs.init(null);
            if(remoteFs.exists(fsPath)){
                InputStream remoteStream = remoteFs.read(fsPath);
                if(!targetFile.exists()){
                    targetFile.getParentFile().mkdirs();
                    targetFile.createNewFile();
                }
                Files.copy(remoteStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                IOUtils.closeQuietly(remoteStream);
            }
        }catch (IOException e){
            logger.error(e);
            throw new UDFException("Verify that there is a problem with the UDF jar package(校验UDF jar包存在问题)：" + e.getMessage());
        }

        this.getClass().getResource("").getFile();
        File hiveDependency = new File(UdfConfiguration.UDF_HIVE_EXEC_PATH().getValue());
        String udfClassName = StringUtils.substringBetween(udfInfo.getRegisterFormat(), "\"", "\"");
        try{
            URL[] url = {new URL("file://" + targetFile.getAbsolutePath()), new URL("file://" + hiveDependency.getAbsolutePath())};
            URLClassLoader loader = URLClassLoader.newInstance(url);
            Class clazz = loader.loadClass(udfClassName);
            Constructor constructor = clazz.getConstructor(new Class[0]);
            if(!Modifier.isPublic(constructor.getModifiers())) throw new NoSuchMethodException();
        }catch (ClassNotFoundException cne){
            throw new UDFException("There is a problem verifying the UDF jar package: the class is not found(校验UDF jar包存在问题：找不到类) " + cne.getMessage());
        } catch (NoSuchMethodException e) {
            throw new UDFException("There is a problem verifying the UDF jar package: class(校验UDF jar包存在问题：类) " + udfClassName + " Missing public no-argument constructor(缺少public的无参数构造方法)");
        }catch (Exception e){
            throw new UDFException("Verify that there is a problem with the UDF jar package(校验UDF jar包存在问题)：" + e.getMessage());
        } finally {
            targetFile.delete();
        }
    }

    @Override
    public UDFInfo updateUDF(UDFInfo udfInfo, String userName) throws UDFException {
        logger.info(userName + " update udfInfo: " + udfInfo.getUdfName());
        if (udfInfo.getId() == null) {
            throw new UDFException("id Can not be empty(不能为空)");
        }
        UDFInfo oldUdfInfo = udfDao.getUDFById(udfInfo.getId());
        if(oldUdfInfo == null) {
            throw new UDFException("No old UDF found by this ID.");
        }
        if (userName.equals(oldUdfInfo.getCreateUser())) {
            if(oldUdfInfo.getUdfType() != udfInfo.getUdfType()){
                throw new UDFException("UDF type modification is not allowed.");
            }
            if(!udfInfo.getUdfName().equals(oldUdfInfo.getUdfName())){
                long count = udfDao.getSameNameCountByUser(udfInfo.getUdfName(), userName);
                if(count > 0) {
                    throw new UDFException("The name of udf is the same name. Please rename it and rebuild it.(udf的名字重名，请改名后重建)");
                }
            }

            udfInfo.setPath(StringUtils.replace(udfInfo.getPath(), "file://", ""));
            //If it is a jar package, do some basic verification(如果是jar包，做一些基本校验)
            if(udfInfo.getUdfType() == UDF_JAR && StringUtils.isNotBlank(udfInfo.getPath())){
                //validateJarFile(udfInfo, userName);
            }
            udfInfo.setShared(oldUdfInfo.getShared());

            // for shared UDF, Sync udf info
            Long sharedUdfId = udfDao.selectAllShareUDFInfoIdByUDFId(userName, oldUdfInfo.getUdfName());
            if(sharedUdfId == null){
                sharedUdfId = udfDao.selectAllShareUDFInfoIdByUDFId(userName, udfInfo.getUdfName());
            }
            if (sharedUdfId != null) {
                if(sharedUdfId == udfInfo.getId()){
                    throw new UDFException("请修改该共享函数对应的个人函数。");
                }
                FsPath sharedPath = null;
                try {
                    sharedPath = copySharedUdfFile(userName, udfInfo);
                } catch (IOException e) {
                    throw new UDFException("Failed to copy shared UDF file.");
                }
                UDFInfo originalSharedUdf = udfDao.getUDFById(sharedUdfId);
                try {
                    UDFInfo newSharedUdf = createSharedUdfInfo(udfInfo, originalSharedUdf.getTreeId(), sharedPath);
                    newSharedUdf.setId(sharedUdfId);
                    udfDao.updateUDF(newSharedUdf);
                } catch (Exception e) {
                    throw new UDFException("Failed to update shared UDF.");
                }
            }
            udfDao.updateUDF(udfInfo);

        } else {
            throw new UDFException("Current user must be consistent with the modified user(当前用户必须和修改用户一致)");
        }
        return udfInfo;
    }

    @Override
    public UDFInfo createSharedUdfInfo(UDFInfo udfInfo, Long shareParentId, FsPath sharedPath) throws Exception {
        UDFInfo sharedUDFInfo = new UDFInfo();
        DateConverter converter = new DateConverter(new Date());
        BeanUtilsBean.getInstance().getConvertUtils().register(converter, Date.class);
        BeanUtils.copyProperties(sharedUDFInfo,udfInfo);
        sharedUDFInfo.setId(null);
        sharedUDFInfo.setCreateTime(new Date());
        sharedUDFInfo.setUpdateTime(new Date());
        sharedUDFInfo.setTreeId(shareParentId);
        sharedUDFInfo.setPath(sharedPath.getPath());
        sharedUDFInfo.setShared(true);
        sharedUDFInfo.setExpire(false);
        return sharedUDFInfo;
    }

    @Override
    public FsPath copySharedUdfFile(String userName, UDFInfo udfInfo) throws IOException {
        String fileName = FilenameUtils.getBaseName(udfInfo.getPath()) + System.currentTimeMillis() + "." + FilenameUtils.getExtension(udfInfo.getPath());
        FsPath udfPath = FsPath.getFsPath(udfInfo.getPath());
        FsPath sharedPath = FsPath.getFsPath(UdfConfiguration.UDF_SHARE_PATH().getValue(), fileName);
        FileSystem fileSystem = (FileSystem) FSFactory.getFsByProxyUser(sharedPath, userName);
        try{
            fileSystem.init(null);
            File file = new File(sharedPath.getPath());
            if (!file.exists()) {
                file.createNewFile();
                file.setReadable(true, false);
                file.setWritable(true, false);
                //fileSystem.createNewFile(sharedPath);
            }
            InputStream udfStream = fileSystem.read(udfPath);
            FileUtils.copyInputStreamToFile(udfStream, file);
            //fileSystem.setPermission(sharedPath, "-rwxrwxrwx");
        } catch (Exception e){
            fileSystem.close();
            throw e;
        }
        return sharedPath;
    }

    @Override
    public Boolean deleteUDF(Long id, String userName) throws UDFException {
        logger.info(userName + " delete udfInfo: " + id);
        UDFInfo udfInfo = udfDao.getUDFById(id);
        if(!userName.equals(udfInfo.getCreateUser())){
            throw new UDFException("您不是该UDF的创建者，没有权限进行删除操作。");
        }
        if(udfInfo != null){
            Long sharedUdfId = udfDao.selectAllShareUDFInfoIdByUDFId(userName, udfInfo.getUdfName());
            if(sharedUdfId != null){
                if(sharedUdfId == udfInfo.getId()){
                    throw new UDFException("请删除该共享函数对应的个人函数。");
                }
                udfDao.deleteAllSharedUser(sharedUdfId);
                udfDao.deleteAllLoadInfo(sharedUdfId);
                udfDao.deleteUDF(sharedUdfId, userName);
            }
        }
        udfDao.deleteLoadInfo(id, userName);
        udfDao.deleteUDF(id, userName);
        return true;
    }

    @Override
    public UDFInfo getUDFById(Long id, String userName) {
        return udfDao.getUDFById(id);
    }

    @Override
    public Boolean deleteLoadInfo(Long id, String userName) {
        udfDao.deleteLoadInfo(id, userName);
        return true;
    }

    /**
     * * Load UDF For the UDF of the jar package type, you need to add a judgment:
     * * 1. Is there a judgment of the same name?
     * * 2. Is there a different path jar to judge?
     * 加载UDF 对于jar包类型的UDF 需要加判断：
     * 1.是否有同名判断
     * 2.是否有不同路径jar判断
     * @param id
     * @param userName
     * @return
     * @throws UDFException
     */
    @Override
    public Boolean addLoadInfo(Long id, String userName) throws UDFException {
        try {
            UDFInfo udfInfo = getUDFById(id, userName);
            if (udfInfo.getUdfType() == 0) {
                /*long sysCount = udfDao.getSameSysCount(udfInfo.getUdfName());*/
                long loadCount = udfDao.getSameLoadCount(userName, udfInfo.getUdfName());
                if (loadCount > 0) {
                    throw new UDFException("There is a Jar package function with the same name(存在同名的Jar包函数)： " + udfInfo.getUdfName());
                }
                String path = udfInfo.getPath();
                String jarPath = path.substring(path.lastIndexOf("/"));
                if (!StringUtils.isEmpty(jarPath)) {
                    List<UDFInfo> sameJarUDF = udfDao.getSameJarUDF(userName, jarPath);
                    for (UDFInfo udf : sameJarUDF) {
                        if (path.equalsIgnoreCase(udf.getPath())) {
                            continue;
                        }
                        if (jarPath.equalsIgnoreCase(udf.getPath().substring(udf.getPath().lastIndexOf("/")))) {
                            throw new UDFException("There is a Jar package with a different path of the same name. The UDF name is(存在同名不同路径的Jar包,UDF名字为)：" + udf.getUdfName() + ",The jar path is(jar路径为)" + udf.getPath());
                        }
                    }
                }
            }
            udfDao.addLoadInfo(id, userName);
        } catch (Throwable e) {
            if (e instanceof DuplicateKeyException) {
                return true;
            } else {
                throw new UDFException(e.getMessage());
            }
        }
        return true;
    }

    @Override
    public List<UDFInfo> getUDFSByUserName(String userName) {

        return udfDao.getUDFSByUserName(userName);
    }

    @Override
    public List<UDFInfo> getUDFSByTreeIdAndUser(Long treeId, String userName, String category) {
        return udfDao.getUDFSByTreeIdAndUser(treeId, userName, categoryToCodes.get(category));
    }

    @Override
    public List<UDFInfo> getUDFInfoByTreeId(Long treeId, String userName, String category) {
        return udfDao.getUDFInfoByTreeId(treeId, userName, categoryToCodes.get(category));
    }

    /**
     * Generate sql needs content:
     * divided into jar, python, scala
     * Save Path and registration syntax separately
     * 生成sql需要内容：
     * 分为jar，python，scala
     * 分别保存Path和注册语法
     * @param userName
     * @return
     */
    @Override
    public Map<String, List<String>> generateInitSql(String userName) {
        logger.info(userName + " generateInitSql");
        List<UDFInfo> loadedUDFs = udfDao.getLoadedUDFs(userName);
        Set<String> fileSet = new HashSet<>();
        List<String> udfJars = new ArrayList<>();
        List<String> registerJars = new ArrayList<>();
        List<String> udfPys = new ArrayList<>();
        List<String> registerPys = new ArrayList<>();
        List<String> udfScalas = new ArrayList<>();
        List<String> registerScalas = new ArrayList<>();

        List<String> functionsPython = new ArrayList<>();
        List<String> functionsScala = new ArrayList<>();

        for (UDFInfo udfInfo : loadedUDFs) {
            if (udfInfo.getUdfType() == UDF_JAR) {
                if (!fileSet.contains(udfInfo.getPath())) {
                    fileSet.add(udfInfo.getPath());
                    udfJars.add(udfInfo.getPath());
                }
                registerJars.add(udfInfo.getRegisterFormat());
            } else if (udfInfo.getUdfType() == ConstantVar.UDF_PY) {
                if (!fileSet.contains(udfInfo.getPath())) {
                    fileSet.add(udfInfo.getPath());
                    udfPys.add(udfInfo.getPath());
                }
                registerPys.add(udfInfo.getRegisterFormat());
            } else if (udfInfo.getUdfType() == ConstantVar.UDF_SCALA) {
                if (!fileSet.contains(udfInfo.getPath())) {
                    fileSet.add(udfInfo.getPath());
                    udfScalas.add(udfInfo.getPath());
                }
                registerScalas.add(udfInfo.getRegisterFormat());
            } else if (udfInfo.getUdfType() == ConstantVar.FUNCTION_PY) {
                if (!fileSet.contains(udfInfo.getPath())) {
                    fileSet.add(udfInfo.getPath());
                    functionsPython.add(udfInfo.getPath());
                }
            } else if (udfInfo.getUdfType() == ConstantVar.FUNCTION_SCALA) {
                if (!fileSet.contains(udfInfo.getPath())) {
                    fileSet.add(udfInfo.getPath());
                    functionsScala.add(udfInfo.getPath());
                }
            }
        }
        Map<String, List<String>> res = new HashedMap();
        res.put("udfJars", udfJars);
        res.put("registerJars", registerJars);
        res.put("udfPys", udfPys);
        res.put("registerPys", registerPys);
        res.put("udfScalas", udfScalas);
        res.put("registerScalas", registerScalas);
        res.put("functionsPython", functionsPython);
        res.put("functionsScala", functionsScala);
        return res;
    }

    @Override
    public Iterator<String> getAllLoadJars(String userName) throws UDFException {
        List<UDFInfo> loadedUDFs = udfDao.getLoadedUDFs(userName);
        Set<String> fileSet = new HashSet<>();
        for (UDFInfo udfInfo : loadedUDFs) {
            if (udfInfo.getUdfType() == 0) {
                if (!fileSet.contains(udfInfo.getPath())) {
                    fileSet.add(udfInfo.getPath());
                }
            }
        }
        return fileSet.iterator();
    }


    @Override
    public List<UDFInfo> getSharedUDFByUserName(String userName) {
        return null;
    }

    @Override
    public List<UDFInfo> getSharedUDFByTreeId(Integer treeId, String userName) {
        return null;
    }

    @Override
    public List<UDFInfo> getSharedUDFInfos(Long id, String userName, String category) {

        return udfDao.selectSharedUDFInfosByTreeIdAndUserName(id,userName, categoryToCodes.get(category));
    }

    @Override
    public Boolean isUDFManager(String userName) {
        UDFManager udfManager = udfDao.selectUDFManager(userName);
        if (udfManager == null){
            return false;
        }
        return true;
    }

    @Override
    public void checkSharedUsers(List<String> sharedUsers,String userName)  throws UDFException {
        if (sharedUsers.contains(userName)){
            throw new UDFException("Do not support sharing to yourself!(不支持分享给自己!)");
        }
//        List<String> notExistUsers = Lists.newArrayList();
//        for(String sharedUser: sharedUsers){
//            Long userId = udfDao.selectIdByUserName(sharedUser);
//            if(userId == null){
//                notExistUsers.add(sharedUser);
//            }
//        }
//        if(!notExistUsers.isEmpty()){
//            throw new UDFException("Sharing user does not exist：" + String.join(",", notExistUsers));
//        }
    }

    @Override
    public UDFInfo addSharedUDFInfo(UDFInfo sharedUDFInfo) throws UDFException {
        long count = udfDao.getShareSameNameCountByUser(sharedUDFInfo.getUdfName());
        if(count > 0) {
            throw new UDFException("Shared udf name(分享的udf的名字)("+sharedUDFInfo.getUdfName()+")Already exists, please edit the name and re-share(已存在，请修改名字后重新进行分享)");
        }
        udfDao.addUDF(sharedUDFInfo);
        return sharedUDFInfo;
    }

    @Override
    public void addUDFSharedUsers(List<String> sharedUsers, Long id) {
        for (String sharedUser : sharedUsers) {
            udfDao.insertUDFSharedUser(id,sharedUser);
        }
    }

    @Override
    public void setUDFSharedInfo(boolean iShared, Long id) {
        udfDao.updateUDFIsShared(iShared,id);
    }

    @Override
    public Long getAllShareUDFInfoIdByUDFId(String userName,String udfName) {

        return udfDao.selectAllShareUDFInfoIdByUDFId(userName,udfName);
    }

    @Override
    public void setSharedUDFInfoExpire(Long shareUDFId) {
        udfDao.updateSharedUDFInfoExpire(shareUDFId);

    }

    @Override
    public List<String> getAllgetSharedUsersByUDFIdAndUseName(String userName,String udfName) {

        List<String> ShareUsers = udfDao.selectAllShareUsersByUDFIdAndUseName(userName,udfName);
        Iterator<String> iterator = ShareUsers.iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            if (userName.equals(next)){
                iterator.remove();
            }
        }
        return ShareUsers;
    }

    @Override
    public void addSharedUser(List<String> sharedUsers,Long udfId) {
        for (String sharedUser : sharedUsers) {
            udfDao.insertSharedUser(sharedUser,udfId);
        }
    }

    @Override
    public void removeSharedUser(List<String> oldsharedUsers,Long udfId) {
        for (String oldsharedUser : oldsharedUsers) {
            udfDao.deleteSharedUser(oldsharedUser,udfId);
        }
    }

}
