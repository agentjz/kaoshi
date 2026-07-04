package com.kaoshi.question;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;

import java.util.Map;

record QuestionBankPackageEntries(Map<String, byte[]> entries) {
    byte[] required(String name) {
        byte[] bytes = entries.get(name);
        if (bytes == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "题库包缺少文件：" + name);
        }
        return bytes;
    }
}
