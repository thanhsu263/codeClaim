package com.cobbleverse.codeclaim.config;

import java.util.List;

/**
 * Đại diện cho một code thưởng trong file config.
 */
public class CodeEntry {
    /** Số lần tối đa có thể nhập code này (-1 = không giới hạn) */
    public int maxUses;

    /** Danh sách lệnh chạy khi nhập code thành công. %player% sẽ được thay bằng tên người chơi. */
    public List<String> commands;

    public CodeEntry() {}

    public CodeEntry(int maxUses, List<String> commands) {
        this.maxUses = maxUses;
        this.commands = commands;
    }
}
