package com.teezom.ftp_work;

public interface FtpInterface {
    void onFTPResponse(String json, String WSType, String fileName);
}
