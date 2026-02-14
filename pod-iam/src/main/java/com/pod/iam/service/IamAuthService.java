package com.pod.iam.service;

import com.pod.iam.domain.IamPermission;
import com.pod.iam.domain.IamUser;
import com.pod.iam.dto.LoginDto;
import java.util.List;

public interface IamAuthService {
    String login(LoginDto loginDto);
    IamUser getCurrentUser();
    List<IamPermission> getCurrentUserPermissions();
    List<Long> getCurrentUserFactoryIds();
}
