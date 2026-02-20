package com.pod.iam.sys.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.sys.domain.PlatApiCredential;
import com.pod.iam.sys.dto.CredentialCreateDto;
import com.pod.iam.sys.dto.CredentialVo;
import com.pod.iam.sys.mapper.PlatApiCredentialMapper;
import com.pod.iam.sys.service.CredentialEncryptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CredentialApplicationService {

    private static final String MASK = "***";

    private final PlatApiCredentialMapper credentialMapper;
    private final CredentialEncryptionService encryptionService;

    public CredentialApplicationService(PlatApiCredentialMapper credentialMapper, CredentialEncryptionService encryptionService) {
        this.credentialMapper = credentialMapper;
        this.encryptionService = encryptionService;
    }

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    public IPage<CredentialVo> page(Page<PlatApiCredential> page, String platformCode, Long shopId) {
        LambdaQueryWrapper<PlatApiCredential> q = new LambdaQueryWrapper<>();
        q.eq(PlatApiCredential::getTenantId, tenantId()).eq(PlatApiCredential::getFactoryId, factoryId()).eq(PlatApiCredential::getDeleted, 0);
        if (platformCode != null && !platformCode.isBlank()) q.eq(PlatApiCredential::getPlatformCode, platformCode);
        if (shopId != null) q.eq(PlatApiCredential::getShopId, shopId);
        q.orderByDesc(PlatApiCredential::getId);
        return credentialMapper.selectPage(page, q).convert(this::toVo);
    }

    public List<CredentialVo> list(String platformCode, Long shopId) {
        LambdaQueryWrapper<PlatApiCredential> q = new LambdaQueryWrapper<>();
        q.eq(PlatApiCredential::getTenantId, tenantId()).eq(PlatApiCredential::getFactoryId, factoryId()).eq(PlatApiCredential::getDeleted, 0);
        if (platformCode != null && !platformCode.isBlank()) q.eq(PlatApiCredential::getPlatformCode, platformCode);
        if (shopId != null) q.eq(PlatApiCredential::getShopId, shopId);
        return credentialMapper.selectList(q).stream().map(this::toVo).collect(Collectors.toList());
    }

    public CredentialVo get(Long id) {
        PlatApiCredential c = credentialMapper.selectById(id);
        if (c == null || !Objects.equals(c.getTenantId(), tenantId()) || (c.getDeleted() != null && c.getDeleted() != 0)) {
            throw new BusinessException("Credential not found: " + id);
        }
        return toVo(c);
    }

    private CredentialVo toVo(PlatApiCredential c) {
        CredentialVo vo = new CredentialVo();
        vo.setId(c.getId());
        vo.setPlatformCode(c.getPlatformCode());
        vo.setShopId(c.getShopId());
        vo.setAuthType(c.getAuthType());
        vo.setCredentialName(c.getCredentialName());
        vo.setPayloadMasked(c.getEncryptedPayload() != null && c.getEncryptedPayload().length() > 4 ? MASK + c.getEncryptedPayload().substring(c.getEncryptedPayload().length() - 4) : MASK);
        vo.setExpiresAt(c.getExpiresAt());
        vo.setRefreshExpiresAt(c.getRefreshExpiresAt());
        vo.setLastRefreshAt(c.getLastRefreshAt());
        vo.setStatus(c.getStatus());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(CredentialCreateDto dto) {
        PlatApiCredential c = new PlatApiCredential();
        c.setTenantId(tenantId());
        c.setFactoryId(factoryId());
        c.setPlatformCode(dto.getPlatformCode());
        c.setShopId(dto.getShopId());
        c.setAuthType(dto.getAuthType());
        c.setCredentialName(dto.getCredentialName());
        c.setStatus(dto.getStatus() != null && PlatApiCredential.STATUS_DISABLED.equals(dto.getStatus()) ? PlatApiCredential.STATUS_DISABLED : PlatApiCredential.STATUS_ENABLED);
        if (dto.getPayloadPlainJson() == null || dto.getPayloadPlainJson().isBlank()) throw new BusinessException("payloadPlainJson required");
        c.setEncryptedPayload(encryptionService.encrypt(dto.getPayloadPlainJson()));
        c.validateForCreate();
        long ex = credentialMapper.selectCount(new LambdaQueryWrapper<PlatApiCredential>()
            .eq(PlatApiCredential::getTenantId, tenantId()).eq(PlatApiCredential::getPlatformCode, c.getPlatformCode())
            .eq(PlatApiCredential::getShopId, c.getShopId()).eq(PlatApiCredential::getAuthType, c.getAuthType()).eq(PlatApiCredential::getDeleted, 0));
        if (ex > 0) throw new BusinessException("Credential already exists");
        credentialMapper.insert(c);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CredentialCreateDto dto) {
        PlatApiCredential c = credentialMapper.selectById(id);
        if (c == null || !Objects.equals(c.getTenantId(), tenantId())) throw new BusinessException("Credential not found: " + id);
        if (dto.getCredentialName() != null) c.setCredentialName(dto.getCredentialName());
        if (dto.getStatus() != null) c.setStatus(dto.getStatus());
        if (dto.getPayloadPlainJson() != null && !dto.getPayloadPlainJson().isBlank()) c.setEncryptedPayload(encryptionService.encrypt(dto.getPayloadPlainJson()));
        credentialMapper.updateById(c);
    }

    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        PlatApiCredential c = credentialMapper.selectById(id);
        if (c == null || !Objects.equals(c.getTenantId(), tenantId())) throw new BusinessException("Credential not found: " + id);
        c.enable();
        credentialMapper.updateById(c);
    }

    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        PlatApiCredential c = credentialMapper.selectById(id);
        if (c == null || !Objects.equals(c.getTenantId(), tenantId())) throw new BusinessException("Credential not found: " + id);
        c.disable();
        credentialMapper.updateById(c);
    }

    public boolean testConnection(Long id) {
        PlatApiCredential c = credentialMapper.selectById(id);
        if (c == null || !Objects.equals(c.getTenantId(), tenantId())) throw new BusinessException("Credential not found: " + id);
        if (c.getEncryptedPayload() == null || c.getEncryptedPayload().isBlank()) throw new BusinessException("No payload");
        try {
            String plain = encryptionService.decrypt(c.getEncryptedPayload());
            return plain != null && !plain.isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 供渠道拉单（OMS job）使用：按租户/工厂/平台/店铺/认证类型取解密后的 payload JSON。
     * 不校验 status=ENABLED，由调用方决定；未找到或已删除返回 null。
     */
    public String getDecryptedPayloadForChannelPull(String platformCode, Long shopId, String authType) {
        if (platformCode == null || platformCode.isBlank() || shopId == null || authType == null || authType.isBlank()) return null;
        LambdaQueryWrapper<PlatApiCredential> q = new LambdaQueryWrapper<>();
        q.eq(PlatApiCredential::getTenantId, tenantId()).eq(PlatApiCredential::getFactoryId, factoryId())
            .eq(PlatApiCredential::getPlatformCode, platformCode).eq(PlatApiCredential::getShopId, shopId)
            .eq(PlatApiCredential::getAuthType, authType).eq(PlatApiCredential::getDeleted, 0);
        PlatApiCredential c = credentialMapper.selectOne(q);
        if (c == null || c.getEncryptedPayload() == null || c.getEncryptedPayload().isBlank()) return null;
        try {
            return encryptionService.decrypt(c.getEncryptedPayload());
        } catch (Exception e) {
            return null;
        }
    }

    /** 供渠道拉单使用：取凭证实体（用于 id 与后续更新）。未找到返回 null。 */
    public PlatApiCredential getCredentialEntityForChannelPull(String platformCode, Long shopId, String authType) {
        if (platformCode == null || platformCode.isBlank() || shopId == null || authType == null || authType.isBlank()) return null;
        LambdaQueryWrapper<PlatApiCredential> q = new LambdaQueryWrapper<>();
        q.eq(PlatApiCredential::getTenantId, tenantId()).eq(PlatApiCredential::getFactoryId, factoryId())
            .eq(PlatApiCredential::getPlatformCode, platformCode).eq(PlatApiCredential::getShopId, shopId)
            .eq(PlatApiCredential::getAuthType, authType).eq(PlatApiCredential::getDeleted, 0);
        return credentialMapper.selectOne(q);
    }

    /** 拉单侧刷新 token 后写回：更新 encrypted_payload、expires_at、last_refresh_at。 */
    @Transactional(rollbackFor = Exception.class)
    public void updateCredentialAfterRefresh(Long credentialId, String newEncryptedPayload, LocalDateTime expiresAt, LocalDateTime lastRefreshAt) {
        PlatApiCredential c = credentialMapper.selectById(credentialId);
        if (c == null || !Objects.equals(c.getTenantId(), tenantId())) throw new BusinessException("Credential not found: " + credentialId);
        if (newEncryptedPayload != null && !newEncryptedPayload.isBlank()) c.setEncryptedPayload(newEncryptedPayload);
        if (expiresAt != null) c.setExpiresAt(expiresAt);
        if (lastRefreshAt != null) c.setLastRefreshAt(lastRefreshAt);
        credentialMapper.updateById(c);
    }

    /** 拉单侧刷新后写回明文 payload（内部加密后落库）、expires_at、last_refresh_at。 */
    @Transactional(rollbackFor = Exception.class)
    public void updateCredentialPayloadAfterRefresh(Long credentialId, String newPayloadPlainJson, LocalDateTime expiresAt, LocalDateTime lastRefreshAt) {
        PlatApiCredential c = credentialMapper.selectById(credentialId);
        if (c == null || !Objects.equals(c.getTenantId(), tenantId())) throw new BusinessException("Credential not found: " + credentialId);
        if (newPayloadPlainJson != null && !newPayloadPlainJson.isBlank()) c.setEncryptedPayload(encryptionService.encrypt(newPayloadPlainJson));
        if (expiresAt != null) c.setExpiresAt(expiresAt);
        if (lastRefreshAt != null) c.setLastRefreshAt(lastRefreshAt);
        credentialMapper.updateById(c);
    }
}
