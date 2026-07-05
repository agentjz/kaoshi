package com.kaoshi.auth.registration;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EmailVerificationCodeMapper {
    @Select("""
            select *
            from email_verification_codes
            where email = #{email}
              and purpose = #{purpose}
              and consumed_at is null
            order by id desc
            limit 1
            """)
    EmailVerificationCode findLatest(@Param("email") String email, @Param("purpose") String purpose);

    @Insert("""
            insert into email_verification_codes (
              email, purpose, code_hash, expires_at, send_count, failed_attempt_count,
              last_sent_at, ip_address, user_agent
            ) values (
              #{email}, #{purpose}, #{codeHash}, #{expiresAt}, 1, 0,
              current_timestamp, #{ipAddress}, #{userAgent}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CodeInsertRow row);

    @Update("update email_verification_codes set consumed_at = current_timestamp where id = #{id} and consumed_at is null")
    int consume(@Param("id") Long id);

    @Update("""
            update email_verification_codes
            set failed_attempt_count = failed_attempt_count + 1,
                locked_until = case when failed_attempt_count + 1 >= 5 then #{lockedUntil} else locked_until end
            where id = #{id}
            """)
    void recordFailure(@Param("id") Long id, @Param("lockedUntil") java.time.LocalDateTime lockedUntil);

    class CodeInsertRow {
        private Long id;
        private final String email;
        private final String purpose;
        private final String codeHash;
        private final java.time.LocalDateTime expiresAt;
        private final String ipAddress;
        private final String userAgent;

        public CodeInsertRow(String email, String purpose, String codeHash, java.time.LocalDateTime expiresAt, String ipAddress, String userAgent) {
            this.email = email;
            this.purpose = purpose;
            this.codeHash = codeHash;
            this.expiresAt = expiresAt;
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public String getPurpose() {
            return purpose;
        }

        public String getCodeHash() {
            return codeHash;
        }

        public java.time.LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getUserAgent() {
            return userAgent;
        }
    }
}
