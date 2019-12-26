package com.iflytek.risk.common;

import lombok.Data;

/**
 * @program: lzda->FastDFSFile
 * @description:
 * @author: 黄智强
 * @create: 2019-09-03 22:24
 **/
@Data
public class FastDFSFile {
    private String name;
    private byte[] content;
    private String ext;
    private String md5;
    private String author;

    public FastDFSFile(String name, byte[] content, String ext) {
        this.name = name;
        this.content = content;
        this.ext = ext;
    }
}
