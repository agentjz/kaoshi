package com.kaoshi.auth.mail;

import com.kaoshi.auth.dto.MailStatusResponse;
import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final MailProperties properties;
    private final org.springframework.boot.autoconfigure.mail.MailProperties springMailProperties;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public MailService(
            MailProperties properties,
            org.springframework.boot.autoconfigure.mail.MailProperties springMailProperties,
            ObjectProvider<JavaMailSender> mailSenderProvider
    ) {
        this.properties = properties;
        this.springMailProperties = springMailProperties;
        this.mailSenderProvider = mailSenderProvider;
    }

    public MailStatusResponse status() {
        boolean configured = configured();
        String message = properties.enabled()
                ? configured ? "邮件服务可用" : "邮件服务已开启但 SMTP 配置不完整"
                : "当前部署未启用邮件服务";
        return new MailStatusResponse(
                properties.enabled(),
                configured,
                properties.deliveryMode().name(),
                safe(properties.from()),
                springMailProperties.getHost(),
                springMailProperties.getPort(),
                message
        );
    }

    public void sendVerificationCode(String email, String purpose, String code) {
        ensureConfigured();
        String title = switch (purpose) {
            case "REGISTER" -> "kaoshi 注册验证码";
            case "RESET_PASSWORD" -> "kaoshi 找回密码验证码";
            default -> "kaoshi 邮箱验证码";
        };
        String body = "你的 kaoshi 验证码是 " + code + "，10 分钟内有效。若非本人操作，请忽略。";
        send(email, title, body);
    }

    public void sendTestMail(String email) {
        ensureConfigured();
        send(email, "kaoshi 邮件配置自检", "这是一封 kaoshi 邮件服务自检邮件。收到此邮件说明 SMTP 配置可用。");
    }

    public String exposedCode(String code) {
        return properties.exposeDebugCode() || properties.deliveryMode() == MailProperties.DeliveryMode.LOG ? code : null;
    }

    private void send(String email, String title, String body) {
        if (properties.deliveryMode() == MailProperties.DeliveryMode.LOG) {
            log.info("Mail LOG mode: to={}, title={}, body={}", email, title, body);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(email);
        message.setSubject(title);
        message.setText(body);
        mailSenderProvider.getObject().send(message);
    }

    private void ensureConfigured() {
        if (!properties.enabled() || !configured()) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前部署未启用邮件服务，请联系管理员");
        }
    }

    private boolean configured() {
        if (!properties.enabled()) {
            return false;
        }
        if (properties.deliveryMode() == MailProperties.DeliveryMode.LOG) {
            return true;
        }
        return notBlank(properties.from()) && notBlank(springMailProperties.getHost());
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
