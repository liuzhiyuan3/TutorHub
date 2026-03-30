package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.security.JwtUtils;
import com.teacher.common.security.LoginUser;
import com.teacher.common.security.LoginUserContext;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.dto.LoginRequest;
import com.teacher.pojo.dto.RegisterRequest;
import com.teacher.pojo.dto.WechatLoginRequest;
import com.teacher.pojo.dto.WechatPhoneLoginRequest;
import com.teacher.pojo.entity.AdminEntity;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.pojo.vo.LoginVO;
import com.teacher.pojo.vo.admin.AdminSessionVO;
import com.teacher.server.config.WechatProperties;
import com.teacher.server.mapper.AdminMapper;
import com.teacher.server.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final JwtUtils jwtUtils;
    private final WechatProperties wechatProperties;
    private final ObjectMapper objectMapper;
    private final MediaUrlService mediaUrlService;
    private volatile HttpClient httpClient;

    public AuthService(UserMapper userMapper, AdminMapper adminMapper, JwtUtils jwtUtils,
                       WechatProperties wechatProperties, ObjectMapper objectMapper,
                       MediaUrlService mediaUrlService) {
        this.userMapper = userMapper;
        this.adminMapper = adminMapper;
        this.jwtUtils = jwtUtils;
        this.wechatProperties = wechatProperties;
        this.objectMapper = objectMapper;
        this.mediaUrlService = mediaUrlService;
    }

    public LoginVO userLogin(LoginRequest request) {
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserAccount, request.getAccount())
                .eq(UserEntity::getUserDeleteStatus, 0)
                .last("limit 1"));
        if (user == null || !user.getUserPassword().equals(request.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        Integer activeUserType = resolveActiveUserType(request.getUserType(), user.getUserType());
        return buildLoginVO(user, activeUserType);
    }

    public LoginVO adminLogin(LoginRequest request) {
        AdminEntity admin = adminMapper.selectOne(new LambdaQueryWrapper<AdminEntity>()
                .eq(AdminEntity::getAdminAccount, request.getAccount())
                .eq(AdminEntity::getAdminDeleteStatus, 0)
                .last("limit 1"));
        if (admin == null || !admin.getAdminPassword().equals(request.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        String token = jwtUtils.generateToken(new LoginUser(admin.getId(), admin.getAdminAccount(), -1, true));
        return new LoginVO(token, admin.getId(), admin.getAdminAccount(), admin.getAdminName(), -1, true);
    }

    public LoginVO register(RegisterRequest request) {
        if (request.getUserType() == null || (request.getUserType() != 0 && request.getUserType() != 1)) {
            throw new BusinessException("用户类型不合法");
        }
        UserEntity exist = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserAccount, request.getAccount())
                .last("limit 1"));
        if (exist != null) {
            throw new BusinessException("账号已存在");
        }
        UserEntity phoneExist = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserPhone, request.getPhone())
                .last("limit 1"));
        if (phoneExist != null) {
            throw new BusinessException("手机号已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setId(IdUtil.uuid32());
        user.setUserAccount(request.getAccount());
        user.setUserPassword(request.getPassword());
        user.setUserName(request.getName());
        user.setUserPhone(request.getPhone());
        user.setUserType(request.getUserType());
        user.setUserStatus(1);
        user.setUserDeleteStatus(0);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);
        return buildLoginVO(user, request.getUserType());
    }

    public LoginVO userWechatLogin(WechatLoginRequest request) {
        Integer userType = request.getUserType();
        if (userType == null || (userType != 0 && userType != 1)) {
            throw new BusinessException("用户类型不合法");
        }
        String cleanNickName = normalizeNickName(request.getNickName());
        String cleanAvatarUrl = normalizeAvatarUrl(request.getAvatarUrl());
        String openid = fetchWechatOpenid(request.getCode());
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserWechatOpenid, openid)
                .eq(UserEntity::getUserDeleteStatus, 0)
                .last("limit 1"));
        if (user != null) {
            if (!cleanNickName.isBlank() && !cleanNickName.equals(trimToEmpty(user.getUserName()))) {
                user.setUserName(cleanNickName);
                user.setNicknameSource("wechat");
            }
            if (!cleanAvatarUrl.isBlank() && !cleanAvatarUrl.equals(trimToEmpty(user.getUserPortrait()))) {
                user.setUserPortrait(cleanAvatarUrl);
                user.setAvatarSource("wechat");
            }
            user.setLastLoginTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            Integer activeUserType = resolveActiveUserType(userType, user.getUserType());
            return buildLoginVO(user, activeUserType);
        }

        UserEntity newUser = new UserEntity();
        LocalDateTime now = LocalDateTime.now();
        newUser.setId(IdUtil.uuid32());
        newUser.setUserWechatOpenid(openid);
        newUser.setUserAccount(generateWechatAccount(openid));
        newUser.setUserPassword(UUID.randomUUID().toString().replace("-", ""));
        newUser.setUserName(resolveWechatUserName(cleanNickName, userType));
        newUser.setUserPortrait(cleanAvatarUrl);
        newUser.setNicknameSource("wechat");
        newUser.setAvatarSource(cleanAvatarUrl.isBlank() ? "manual" : "wechat");
        newUser.setProfileCompleted(0);
        newUser.setUserPhone(generatePlaceholderPhone());
        newUser.setUserType(userType);
        newUser.setUserGender(0);
        newUser.setUserStatus(1);
        newUser.setUserDeleteStatus(0);
        newUser.setLastLoginTime(now);
        newUser.setCreateTime(now);
        newUser.setUpdateTime(now);
        userMapper.insert(newUser);

        return buildLoginVO(newUser, userType);
    }

    public LoginVO userWechatPhoneLogin(WechatPhoneLoginRequest request) {
        Integer userType = request.getUserType();
        if (userType == null || (userType != 0 && userType != 1)) {
            throw new BusinessException("用户类型不合法");
        }
        String openid = fetchWechatOpenid(request.getCode());
        String phone = fetchWechatPhoneNumber(request.getPhoneCode());

        UserEntity openidUser = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserWechatOpenid, openid)
                .eq(UserEntity::getUserDeleteStatus, 0)
                .last("limit 1"));
        UserEntity phoneUser = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserPhone, phone)
                .eq(UserEntity::getUserDeleteStatus, 0)
                .last("limit 1"));

        if (openidUser != null && phoneUser != null && !openidUser.getId().equals(phoneUser.getId())) {
            throw new BusinessException("该微信与手机号分别绑定了不同账号，请联系管理员处理");
        }

        UserEntity target = openidUser != null ? openidUser : phoneUser;
        LocalDateTime now = LocalDateTime.now();
        if (target != null) {
            String oldOpenid = trimToEmpty(target.getUserWechatOpenid());
            if (!oldOpenid.isBlank() && !openid.equals(oldOpenid)) {
                throw new BusinessException("当前手机号已绑定其他微信账号");
            }
            target.setUserWechatOpenid(openid);
            target.setUserPhone(phone);
            target.setLastLoginTime(now);
            target.setUpdateTime(now);
            userMapper.updateById(target);
            Integer activeUserType = resolveActiveUserType(userType, target.getUserType());
            return buildLoginVO(target, activeUserType);
        }

        UserEntity newUser = new UserEntity();
        newUser.setId(IdUtil.uuid32());
        newUser.setUserWechatOpenid(openid);
        newUser.setUserAccount(generateWechatAccount(openid));
        newUser.setUserPassword(UUID.randomUUID().toString().replace("-", ""));
        newUser.setUserName(resolveWechatUserName("", userType));
        newUser.setUserPhone(phone);
        newUser.setProfileCompleted(0);
        newUser.setUserType(userType);
        newUser.setUserGender(0);
        newUser.setUserStatus(1);
        newUser.setUserDeleteStatus(0);
        newUser.setLastLoginTime(now);
        newUser.setCreateTime(now);
        newUser.setUpdateTime(now);
        userMapper.insert(newUser);
        return buildLoginVO(newUser, userType);
    }

    public AdminSessionVO adminMe() {
        LoginUser loginUser = requireAdmin();
        AdminEntity admin = adminMapper.selectById(loginUser.getId());
        if (admin == null || admin.getAdminDeleteStatus() != null && admin.getAdminDeleteStatus() == 1) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "管理员会话已失效，请重新登录");
        }
        return new AdminSessionVO(admin.getId(), admin.getAdminAccount(), admin.getAdminName(), true);
    }

    private String fetchWechatOpenid(String code) {
        String appId = trimToEmpty(wechatProperties.getAppId());
        String appSecret = trimToEmpty(wechatProperties.getAppSecret());
        if (appId.isBlank() || appSecret.isBlank()) {
            throw new BusinessException("微信登录配置缺失，请先设置 WECHAT_APP_ID 与 WECHAT_APP_SECRET");
        }
        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid="
                    + URLEncoder.encode(appId, StandardCharsets.UTF_8)
                    + "&secret=" + URLEncoder.encode(appSecret, StandardCharsets.UTF_8)
                    + "&js_code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                    + "&grant_type=authorization_code";
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());
            if (node.has("errcode") && node.path("errcode").asInt() != 0) {
                String errMsg = node.path("errmsg").asText("微信接口异常");
                throw new BusinessException("微信登录失败：" + errMsg);
            }
            String openid = node.path("openid").asText("");
            if (openid.isBlank()) {
                throw new BusinessException("微信登录失败：未获取到openid");
            }
            return openid;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new BusinessException("微信登录失败：调用微信接口异常");
        }
    }

    private String fetchWechatPhoneNumber(String phoneCode) {
        String accessToken = fetchWechatAccessToken();
        try {
            String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token="
                    + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
            String body = "{\"code\":\"" + phoneCode + "\"}";
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());
            if (node.has("errcode") && node.path("errcode").asInt() != 0) {
                String errMsg = node.path("errmsg").asText("微信手机号接口异常");
                throw new BusinessException("微信手机号授权失败：" + errMsg);
            }
            String phone = node.path("phone_info").path("purePhoneNumber").asText("");
            if (phone.isBlank()) {
                phone = node.path("phone_info").path("phoneNumber").asText("");
            }
            if (phone.isBlank()) {
                throw new BusinessException("微信手机号授权失败：未获取到手机号");
            }
            return phone;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new BusinessException("微信手机号授权失败：调用微信接口异常");
        }
    }

    private String fetchWechatAccessToken() {
        String appId = trimToEmpty(wechatProperties.getAppId());
        String appSecret = trimToEmpty(wechatProperties.getAppSecret());
        if (appId.isBlank() || appSecret.isBlank()) {
            throw new BusinessException("微信登录配置缺失，请先设置 WECHAT_APP_ID 与 WECHAT_APP_SECRET");
        }
        try {
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                    + URLEncoder.encode(appId, StandardCharsets.UTF_8)
                    + "&secret=" + URLEncoder.encode(appSecret, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());
            if (node.has("errcode") && node.path("errcode").asInt() != 0) {
                String errMsg = node.path("errmsg").asText("微信access_token接口异常");
                throw new BusinessException("微信登录失败：" + errMsg);
            }
            String token = node.path("access_token").asText("");
            if (token.isBlank()) {
                throw new BusinessException("微信登录失败：未获取到access_token");
            }
            return token;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new BusinessException("微信登录失败：调用微信接口异常");
        }
    }

    private String generateWechatAccount(String openid) {
        String prefix = openid.length() > 20 ? openid.substring(0, 20) : openid;
        String base = "wx_" + prefix;
        String account = base;
        int suffix = 1;
        while (userMapper.selectCount(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUserAccount, account)) > 0) {
            account = base + "_" + suffix;
            suffix++;
        }
        return account;
    }

    private String generatePlaceholderPhone() {
        for (int i = 0; i < 20; i++) {
            String tail = String.valueOf((long) (Math.random() * 1_000_000_000L));
            while (tail.length() < 9) {
                tail = "0" + tail;
            }
            String phone = "17" + tail;
            if (userMapper.selectCount(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUserPhone, phone)) == 0) {
                return phone;
            }
        }
        String tail = String.valueOf(System.currentTimeMillis() % 1_000_000_000L);
        while (tail.length() < 9) {
            tail = "0" + tail;
        }
        return "17" + tail;
    }

    private String resolveWechatUserName(String nickName, Integer userType) {
        if (nickName != null && !nickName.isBlank()) {
            return nickName.length() > 50 ? nickName.substring(0, 50) : nickName;
        }
        return userType != null && userType == 1 ? "微信教员用户" : "微信家长用户";
    }

    private String normalizeNickName(String nickName) {
        String value = trimToEmpty(nickName);
        if (value.isBlank()) {
            return "";
        }
        return value.length() > 50 ? value.substring(0, 50) : value;
    }

    private String normalizeAvatarUrl(String avatarUrl) {
        String value = mediaUrlService.normalize(avatarUrl);
        if (value.isBlank()) {
            return "";
        }
        return value.length() > 255 ? value.substring(0, 255) : value;
    }

    private Integer resolveActiveUserType(Integer requestedUserType, Integer fallbackUserType) {
        if (requestedUserType != null && (requestedUserType == 0 || requestedUserType == 1)) {
            return requestedUserType;
        }
        if (fallbackUserType != null && (fallbackUserType == 0 || fallbackUserType == 1)) {
            return fallbackUserType;
        }
        return 0;
    }

    private LoginVO buildLoginVO(UserEntity user, Integer activeUserType) {
        Integer resolvedType = resolveActiveUserType(activeUserType, user.getUserType());
        String token = jwtUtils.generateToken(new LoginUser(user.getId(), user.getUserAccount(), resolvedType, false));
        return new LoginVO(
                token,
                user.getId(),
                user.getUserAccount(),
                user.getUserName(),
                resolvedType,
                false,
                user.getUserWechatOpenid(),
                user.getProfileCompleted() != null && user.getProfileCompleted() == 1
        );
    }

    private String trimToEmpty(String text) {
        return text == null ? "" : text.trim();
    }

    private HttpClient getHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        synchronized (this) {
            if (httpClient == null) {
                try {
                    httpClient = HttpClient.newBuilder().build();
                } catch (RuntimeException ex) {
                    throw new BusinessException("微信登录失败：HTTP客户端初始化异常");
                }
            }
            return httpClient;
        }
    }

    public LoginUser currentLoginUser() {
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return loginUser;
    }

    public LoginUser requireAdmin() {
        LoginUser loginUser = currentLoginUser();
        if (!loginUser.isAdmin()) {
            throw new BusinessException("仅管理员可操作");
        }
        return loginUser;
    }
}
