package com.iflytek.risk.service;

import com.iflytek.risk.common.RequestBean;
import com.iflytek.risk.common.ResponseBean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ICommonService {
    /**
     * 上传文件
     *
     * @param multipartFile 文件对象
     * @return 0:组名；1: 文件路径
     * @throws IOException 流异常
     */
    public ResponseBean uploadFile(MultipartFile multipartFile, HttpServletRequest request) throws IOException;

    /**
     * 根据组名和文件路径下载FastDFS文件
     *
     * @param groupName      组名称
     * @param remoteFileName 文件路径
     * @param fileName       文件名称
     * @return byte数组
     */
    public void downFastDFSFile(HttpServletResponse response, String groupName, String remoteFileName, String fileName) throws Exception;


    /**
     * 根据组名称和文件路径删除文件
     *
     * @param groupName      组名称
     * @param remoteFileName 文件路径
     */
    public void deleteFile(String groupName, String remoteFileName);

    /**
     * 下载指定文件
     *
     * @param response
     * @param requestBean
     * @throws Exception
     */
    public void downFile(RequestBean requestBean, HttpServletRequest request, HttpServletResponse response) throws Exception;


    /**
     * 获取ps接口数据
     *
     * @param requestBean
     * @return
     */
    public ResponseBean getPsData(RequestBean requestBean);
}
