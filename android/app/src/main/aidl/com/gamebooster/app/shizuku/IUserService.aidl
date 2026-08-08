package com.gamebooster.app.shizuku;

interface IUserService {
    void destroy() = 16777114; // Reserved Shizuku method ID for destroy
    String execCommand(String command) = 1;
    int getUid() = 2;
    String writeFileFromBase64(String b64content, String destPath) = 3;
}

