package com.iflytek.risk.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.risk.common.*;
import com.iflytek.risk.enums.CommonConstants;
import com.iflytek.risk.enums.UapUrlEnum;
import com.iflytek.risk.service.ICommonService;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.*;

/**
 * @program: law-risk->CommonServiceImpl
 * @description: 公共方法实体类
 * @author: 黄智强
 * @create: 2019-11-25 10:11
 **/
@Service
public class CommonServiceImpl implements ICommonService {
    private static TrackerClient trackerClient;
    private static TrackerServer trackerServer;
    private static StorageServer storageServer;
    private static StorageClient storageClient;
    protected Log log = LogFactory.getLog(this.getClass());

    @Value("${fastdfs.connect_timeout_in_seconds}")
    private String connectTimeout;
    @Value("${fastdfs.network_timeout_in_seconds}")
    private String networkTimeout;
    @Value("${fastdfs.charset}")
    private String charset;
    @Value("${fastdfs.http_tracker_http_port}")
    private String trackerHttpPort;
    @Value("${fastdfs.http_anti_steal_token}")
    private String antiStealToken;
    @Value("${fastdfs.http_secret_key}")
    private String secretKey;
    @Value("${fastdfs.tracker_servers}")
    private String trackerServerIp;
    @Value("${interfaceUrl.ps}")
    private String psRootUrl;

    /**
     * 上传文件
     *
     * @param multipartFile 文件对象
     * @return 返回数组，结构说明：0:组名；1: 文件路径；
     * @throws IOException
     */
    @Override
    public ResponseBean uploadFile(MultipartFile multipartFile, HttpServletRequest request) throws IOException {
        String[] fileAbsolutePath = {};
        Map resultMap = new HashMap();
        String fileName = multipartFile.getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        byte[] file_buff = null;
        InputStream inputStream = multipartFile.getInputStream();
        if (inputStream != null) {
            int len1 = inputStream.available();
            file_buff = new byte[len1];
            inputStream.read(file_buff);
        }
        inputStream.close();
        FastDFSFile fastfile = new FastDFSFile(fileName, file_buff, ext);
        try {
            fileAbsolutePath = this.upload(fastfile);
            if (fileAbsolutePath == null) {
                log.error("upload file failed,please upload again!");
            }
            FileInfo fileInfo = this.getFile(fileAbsolutePath[0], fileAbsolutePath[1]);
            resultMap.put("groupName", fileAbsolutePath[0]);
            resultMap.put("filePath", fileAbsolutePath[1]);
            resultMap.put("fileName", fileName);
            resultMap.put("fileSize", fileInfo.getFileSize() / 1024);
            resultMap.put("fileType", ext);
            resultMap.put("fileUrl", String.format("%s/risk/system/downFastDFSFile?groupName=%s&path=%s&fileName=%s", request.getContextPath(), fileAbsolutePath[0], fileAbsolutePath[1], fileName));
            return new ResponseBean(resultMap);
        } catch (Exception e) {
            log.error("upload file fail  Exception:" + e.getMessage());
            return new ResponseBean(
                    CommonConstants.FAIL.getCode(),
                    e.getMessage()
            );
        }
    }

    /**
     * 下载文件
     *
     * @param response  响应对象
     * @param groupName 组名称
     * @param path      文件路径
     */
    @Override
    public void downFastDFSFile(HttpServletResponse response, String groupName, String path, String fileName) throws Exception {
        fileName = fileName != null ? fileName : path.substring(path.lastIndexOf("/") + 1);
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/x-download");
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            outputStream.write(this.downFile(groupName, path));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] downFile(String groupName, String remoteFileName) {
        this.init();
        byte[] result = null;
        InputStream in = null;
        try {
            storageClient = new StorageClient(trackerServer, storageServer);
            byte[] fileByte = storageClient.download_file(groupName, remoteFileName);
            in = new ByteArrayInputStream(fileByte);
            result = new byte[in.available()];
            in.read(result);
            in.close();
            return result;
        } catch (IOException e) {
            log.error("IO Exception: Get File from Fast DFS failed", e);
        } catch (Exception e) {
            log.error("Non IO Exception: Get File from Fast DFS failed", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 上传文件
     *
     * @param file fastdfs的文件对象
     * @return 上传结果
     */
    private String[] upload(FastDFSFile file) {
        this.init();
        log.debug("File Name: " + file.getName() + "File Length:" + file.getContent().length);

        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author", file.getAuthor());

        long startTime = System.currentTimeMillis();
        String[] uploadResults = null;
        try {
            storageClient = new StorageClient(trackerServer, storageServer);
            uploadResults = storageClient.upload_file(file.getContent(), file.getExt(), meta_list);
        } catch (IOException e) {
            log.error("IO Exception when uploadind the file:" + file.getName(), e);
        } catch (Exception e) {
            log.error("Non IO Exception when uploadind the file:" + file.getName(), e);
        }
        log.debug("upload_file time used:" + (System.currentTimeMillis() - startTime) + " ms");

        if (uploadResults == null) {
            log.error("upload file fail, error code:" + storageClient.getErrorCode());
        }
        String groupName = uploadResults[0];
        String remoteFileName = uploadResults[1];

        log.debug("upload file successfully!!!" + "group_name:" + groupName + ", remoteFileName:" + " " + remoteFileName);
        return uploadResults;
    }

    /**
     * 加载配置文件方法
     */
    private void init() {
        try {
            Properties properties = new Properties();
            properties.setProperty("fastdfs.connect_timeout_in_seconds", this.connectTimeout);
            properties.setProperty("fastdfs.network_timeout_in_seconds", this.networkTimeout);
            properties.setProperty("fastdfs.charset", this.charset);
            properties.setProperty("fastdfs.http_tracker_http_port", this.trackerHttpPort);
            properties.setProperty("fastdfs.http_anti_steal_token", this.antiStealToken);
            properties.setProperty("fastdfs.http_secret_key", this.secretKey);
            properties.setProperty("fastdfs.tracker_servers", this.trackerServerIp);

            ClientGlobal.initByProperties(properties);

            trackerClient = new TrackerClient();
            trackerServer = trackerClient.getConnection();
            storageServer = trackerClient.getStoreStorage(trackerServer);
        } catch (Exception e) {
            log.error("FastDFS Client Init Fail!", e);
        }
    }

    /**
     * 根据groupName和文件名获取文件信息
     *
     * @param groupName      组名称
     * @param remoteFileName 文件名称
     * @return 文件信息对象
     */
    private FileInfo getFile(String groupName, String remoteFileName) {
        this.init();
        try {
            storageClient = new StorageClient(trackerServer, storageServer);
            return storageClient.get_file_info(groupName, remoteFileName);
        } catch (IOException e) {
            log.error("IO Exception: Get File from Fast DFS failed", e);
        } catch (Exception e) {
            log.error("Non IO Exception: Get File from Fast DFS failed", e);
        }
        return null;
    }

    /**
     * 根据组名称和文件路径删除文件
     *
     * @param groupName      组名称
     * @param remoteFileName 文件路径
     */
    @Override
    public void deleteFile(String groupName, String remoteFileName) {
        try {
            this.init();
            storageClient = new StorageClient(trackerServer, storageServer);
            int i = storageClient.delete_file(groupName, remoteFileName);
        } catch (IOException ioException) {
            log.error(ioException.getMessage());
        } catch (MyException myExcpeiton) {
            log.error(myExcpeiton.getMessage());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    /**
     * 下载指定文件
     *
     * @param response
     * @param requestBean
     * @throws Exception
     */
    @Override
    public void downFile(RequestBean requestBean, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String targetClassAndMethod = requestBean.getType();
        if (StringUtils.isEmpty(targetClassAndMethod)) {
            throw new Exception("class and method is null, please check your params type");
        }
        String[] cm = targetClassAndMethod.split("\\.");
        if (!SpringUtils.containsBean(cm[0])) {
            throw new Exception("class is error, not found in context, please check your params type");
        }
        Class<?>[] paramCls = new Class[3];
        paramCls[0] = Map.class;
        paramCls[1] = HttpServletRequest.class;
        paramCls[2] = HttpServletResponse.class;
        Object targetClass = SpringUtils.getBean(cm[0]);
        Method method = targetClass.getClass().getDeclaredMethod(cm[1], paramCls);
        method.invoke(targetClass, (Map) requestBean.getInfo(), request, response);
    }

    /**
     * 获取ps接口数据
     *
     * @param requestBean
     * @return
     */
    @Override
    public ResponseBean getPsData(RequestBean requestBean) {
        List resultList = new ArrayList();
        if (!StringUtils.isEmpty(requestBean.getInfo())) {
            // 获取ps人员数据
            Map<String, String> paramForGetPsPerson = new HashMap<String, String>();
            Map<String, String> paramForGetPsPerson2 = new HashMap<String, String>();
            paramForGetPsPerson.put("parm", "{\"accountName\":\"" + requestBean.getInfo() + "\"}");
            paramForGetPsPerson2.put("parm", "{\"exactName\":\"" + requestBean.getInfo() + "\"}");
            log.debug("---------psRootUrl---" + psRootUrl + UapUrlEnum.GET_PS_PERSONS.getUrl());
            String accountResult = Hzq.doPost(psRootUrl + UapUrlEnum.GET_PS_PERSONS.getUrl(), paramForGetPsPerson);
            log.debug("---------accountResult:" + accountResult);
            Object personOriginData = JSONObject.parse(accountResult);
            String exactResult = Hzq.doPost(psRootUrl + UapUrlEnum.GET_PS_PERSONS.getUrl(), paramForGetPsPerson2);
            log.debug("---------exactResult:" + exactResult);
            Object personOriginData2 = JSONObject.parse(exactResult);
            boolean personResultFlag = (boolean) ((JSONObject) personOriginData).get("result");
            boolean personResultFlag2 = (boolean) ((JSONObject) personOriginData).get("result");
            if (personResultFlag) {
                List personList = (List) ((JSONObject) personOriginData).get("content");
                if (personList.size() > 0) {
                    resultList.addAll(personList);
                }
            }
            if (personResultFlag2) {
                List personList2 = (List) ((JSONObject) personOriginData2).get("content");
                if (personList2.size() > 0) {
                    resultList.addAll(personList2);
                }
            }
        }
        return new ResponseBean(resultList);
    }
}
