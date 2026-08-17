package com.gamebooster.app.shizuku;

interface IUserService {
    void destroy() = 16777114; // Reserved Shizuku method ID for destroy
    String execCommand(String command) = 1;
    int getUid() = 2;
    boolean writeFile(String path, String content, String mode) = 3;
    String readFile(String path) = 4;
    boolean deletePath(String path) = 5;
    boolean ensureDir(String dirPath) = 6;
    boolean setProperty(String key, String value) = 7;
    String getProperty(String key) = 8;
}
