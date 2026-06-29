package com.mimi.photowall.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.mimi.photowall.config.AliyunSmsConfig;
import com.mimi.photowall.util.DesensitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 阿里云号码认证服务实现类
 * 负责调用阿里云号码认证服务API发送短信验证码
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunSmsService {

    private final AliyunSmsConfig aliyunSmsConfig;

    /**
     * 发送短信验证码（使用号码认证服务API）
     *
     * @param phoneNumber 手机号
     * @param code        验证码
     * @return 是否发送成功
     */
    public boolean sendSmsCode(String phoneNumber, String code) {
        try {
            // 创建客户端
            Client client = createClient();

            // 构建请求
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phoneNumber)
                    .setSignName(aliyunSmsConfig.getSignName())
                    .setTemplateCode(aliyunSmsConfig.getTemplateCode())
                    .setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"5\"}");

            // 发送短信
            RuntimeOptions runtime = new RuntimeOptions();
            SendSmsVerifyCodeResponse resp = client.sendSmsVerifyCodeWithOptions(request, runtime);

            // 检查响应
            if (resp.getBody() != null && "OK".equals(resp.getBody().getCode())) {
                log.info("短信发送成功: phoneNumber={}", desensitizePhone(phoneNumber));
                return true;
            } else {
                log.error("短信发送失败: phoneNumber={}, code={}, message={}",
                        desensitizePhone(phoneNumber),
                        resp.getBody() != null ? resp.getBody().getCode() : "null",
                        resp.getBody() != null ? resp.getBody().getMessage() : "null");
                return false;
            }
        } catch (Exception e) {
            log.error("短信发送异常: phoneNumber={}, error={}", desensitizePhone(phoneNumber), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建阿里云客户端（号码认证服务）
     *
     * @return 客户端实例
     */
    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(aliyunSmsConfig.getAccessKeyId())
                .setAccessKeySecret(aliyunSmsConfig.getAccessKeySecret())
                .setEndpoint("dypnsapi.aliyuncs.com");
        return new Client(config);
    }

    /**
     * 手机号脱敏
     */
    private String desensitizePhone(String phone) {
        return DesensitizeUtil.desensitizePhone(phone);
    }
}
