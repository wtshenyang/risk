package com.iflytek.risk.sec;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ResponseWrapper extends HttpServletResponseWrapper {
    private final LoggingServletOutputStream loggingServletOutpuStream = new LoggingServletOutputStream(); //1. 也是实现了自定义的ServletOutputStream内部类LoggingServletOutpuStream，并初始化

    private final HttpServletResponse delegate;

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        delegate = response;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return loggingServletOutpuStream; //2. 当外部获取ServletOutputStream时候返回内部实现类
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(loggingServletOutpuStream.baos);
    }

    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>(0);
        for (String headerName : getHeaderNames()) {
            headers.put(headerName, getHeader(headerName));
        }
        return headers;
    }

    public String getContent() {
        try {
            String responseEncoding = delegate.getCharacterEncoding();
            return loggingServletOutpuStream.baos.toString(responseEncoding != null ? responseEncoding : "utf-8");
        } catch (UnsupportedEncodingException e) {
            return "[UNSUPPORTED ENCODING]";
        }
    }

    public byte[] getContentAsBytes() {
        return loggingServletOutpuStream.baos.toByteArray(); //4. 获取请求内容时候从内部实现类的私有变量返回
    }

    private class LoggingServletOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream baos = new ByteArrayOutputStream();

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        @Override
        public void write(int b) throws IOException {
            baos.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            baos.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            baos.write(b, off, len);
            //3. 当向内部实现类LoggingServletOutpuStream 写入信息时，写到其内部字段baos上
        }
    }
}
