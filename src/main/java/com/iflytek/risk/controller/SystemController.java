package com.iflytek.risk.controller;

import com.iflytek.risk.common.RequestBean;
import com.iflytek.risk.common.ResponseBean;
import com.iflytek.risk.service.ICommonService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @program: law-risk->SystemController
 * @description: 系统公共方法
 * @author: 黄智强
 * @create: 2019-11-25 10:03
 **/
@RestController
@RequestMapping("/risk/system")
public class SystemController {
    @Resource
    ICommonService commonService;

    /**
     * 上传文件
     *
     * @param file 文件对象
     * @return 0:组名；1: 文件路径
     * @throws IOException
     */
    @PostMapping(value = "/upload")
    @ResponseBody
    public ResponseBean uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
        return commonService.uploadFile(file, request);
    }

    /**
     * fastDFS文件下载
     *
     * @param response  响应对象
     * @param groupName 组名称
     * @param path      文件路径
     */
    @RequestMapping(value = "/downFastDFSFile")
    @ResponseBody
    public void downFastDFSFile(HttpServletResponse response, String groupName, String path, String fileName) throws Exception {
        commonService.downFastDFSFile(response, groupName, path, fileName);
    }

    /**
     * 文件下载
     *
     * @param requestBean
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/downFile")
    @ResponseBody
    public void downFile(@RequestBody RequestBean requestBean, HttpServletRequest request, HttpServletResponse response) throws Exception {
        commonService.downFile(requestBean, request, response);
    }

    /**
     * 获取ps接口数据
     *
     * @param requestBean
     * @return
     */
    @RequestMapping(value = "/getPsData")
    @ResponseBody
    public ResponseBean getPsData(@RequestBody RequestBean requestBean) {
        return commonService.getPsData(requestBean);
    }

}
