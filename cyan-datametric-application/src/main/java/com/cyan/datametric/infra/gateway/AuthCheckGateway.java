package com.cyan.datametric.infra.gateway;

import com.cyan.arch.common.api.Response;
import com.cyan.dataauth.client.AuthCheckClient;
import com.cyan.dataauth.dto.UserSecurityLevelDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 权限校验网关封装层
 * <p>
 * 封装对 cyan-dataauth 的 Feign 调用，Application 层通过此类访问外部权限校验能力。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class AuthCheckGateway {

    private final AuthCheckClient authCheckClient;

    /**
     * 获取用户最高可访问密级
     *
     * @param passport 用户通行证
     * @return 用户密级信息
     */
    public Response<UserSecurityLevelDTO> getUserMaxSecurityLevel(String passport) {
        return authCheckClient.getUserMaxSecurityLevel(passport);
    }
}
