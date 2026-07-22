package com.gamelauncher.core.shizuku.aidl;

/**
 * AIDL interface for privileged shell command execution via Shizuku UserService.
 */
interface IShellCommandService {
    void destroy() = 17;
    int exec(in String command, out String[] output, in long timeoutMs) = 1;
    int execArgs(in String[] args, out String[] output, in long timeoutMs) = 2;
}
