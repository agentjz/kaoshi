package com.kaoshi.auth.registration;

import com.kaoshi.user.domain.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdminRegistrationMapper {
    @Select("""
            select *
            from users
            where registration_source = 'SELF_REGISTERED'
              and approval_status = #{approvalStatus}
              and deleted_at is null
            order by registered_at desc, id desc
            """)
    List<UserAccount> findByApprovalStatus(@Param("approvalStatus") String approvalStatus);

    @Update("""
            update users
            set approval_status = 'APPROVED', updated_at = current_timestamp
            where id = #{userId}
              and registration_source = 'SELF_REGISTERED'
              and approval_status = 'PENDING'
            """)
    int approve(@Param("userId") Long userId);

    @Update("""
            update users
            set approval_status = 'REJECTED', status = 'DISABLED', updated_at = current_timestamp
            where id = #{userId}
              and registration_source = 'SELF_REGISTERED'
              and approval_status = 'PENDING'
            """)
    int reject(@Param("userId") Long userId);
}
