package com.pod.iam.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.iam.sys.application.CredentialApplicationService;
import com.pod.iam.sys.application.FxRateApplicationService;
import com.pod.iam.sys.application.PlatformApplicationService;
import com.pod.iam.sys.application.ShopApplicationService;
import com.pod.iam.sys.application.SiteApplicationService;
import com.pod.iam.sys.domain.FxRate;
import com.pod.iam.sys.domain.PlatPlatform;
import com.pod.iam.sys.domain.PlatShop;
import com.pod.iam.sys.domain.PlatSite;
import com.pod.iam.sys.dto.CredentialCreateDto;
import com.pod.iam.sys.dto.CredentialVo;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sys")
public class SysController {

    private final PlatformApplicationService platformService;
    private final SiteApplicationService siteService;
    private final ShopApplicationService shopService;
    private final CredentialApplicationService credentialService;
    private final FxRateApplicationService fxRateService;

    public SysController(PlatformApplicationService platformService, SiteApplicationService siteService,
                         ShopApplicationService shopService, CredentialApplicationService credentialService,
                         FxRateApplicationService fxRateService) {
        this.platformService = platformService;
        this.siteService = siteService;
        this.shopService = shopService;
        this.credentialService = credentialService;
        this.fxRateService = fxRateService;
    }

    // ---------- platforms ----------
    @GetMapping("/platforms")
    @RequirePerm("sys:platform:page")
    public Result<IPage<PlatPlatform>> pagePlatforms(
            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.success(platformService.page(new Page<>(current, size), status));
    }

    @GetMapping("/platforms/list")
    @RequirePerm("sys:platform:page")
    public Result<List<PlatPlatform>> listPlatforms(@RequestParam(required = false) String status) {
        return Result.success(platformService.list(status));
    }

    @GetMapping("/platforms/{id}")
    @RequirePerm("sys:platform:get")
    public Result<PlatPlatform> getPlatform(@PathVariable Long id) {
        return Result.success(platformService.get(id));
    }

    @PostMapping("/platforms")
    @RequirePerm("sys:platform:create")
    public Result<Void> createPlatform(@RequestBody PlatPlatform body) {
        platformService.create(body);
        return Result.success();
    }

    @PutMapping("/platforms/{id}")
    @RequirePerm("sys:platform:update")
    public Result<Void> updatePlatform(@PathVariable Long id, @RequestBody PlatPlatform body) {
        platformService.update(id, body);
        return Result.success();
    }

    @PostMapping("/platforms/{id}/enable")
    @RequirePerm("sys:platform:enable")
    public Result<Void> enablePlatform(@PathVariable Long id) {
        platformService.enable(id);
        return Result.success();
    }

    @PostMapping("/platforms/{id}/disable")
    @RequirePerm("sys:platform:disable")
    public Result<Void> disablePlatform(@PathVariable Long id) {
        platformService.disable(id);
        return Result.success();
    }

    // ---------- sites ----------
    @GetMapping("/sites")
    @RequirePerm("sys:site:page")
    public Result<IPage<PlatSite>> pageSites(
            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String platformCode, @RequestParam(required = false) String status) {
        return Result.success(siteService.page(new Page<>(current, size), platformCode, status));
    }

    @GetMapping("/sites/list")
    @RequirePerm("sys:site:page")
    public Result<List<PlatSite>> listSites(@RequestParam(required = false) String platformCode, @RequestParam(required = false) String status) {
        return Result.success(siteService.list(platformCode, status));
    }

    @GetMapping("/sites/{id}")
    @RequirePerm("sys:site:get")
    public Result<PlatSite> getSite(@PathVariable Long id) {
        return Result.success(siteService.get(id));
    }

    @PostMapping("/sites")
    @RequirePerm("sys:site:create")
    public Result<Void> createSite(@RequestBody PlatSite body) {
        siteService.create(body);
        return Result.success();
    }

    @PutMapping("/sites/{id}")
    @RequirePerm("sys:site:update")
    public Result<Void> updateSite(@PathVariable Long id, @RequestBody PlatSite body) {
        siteService.update(id, body);
        return Result.success();
    }

    @PostMapping("/sites/{id}/enable")
    @RequirePerm("sys:site:enable")
    public Result<Void> enableSite(@PathVariable Long id) {
        siteService.enable(id);
        return Result.success();
    }

    @PostMapping("/sites/{id}/disable")
    @RequirePerm("sys:site:disable")
    public Result<Void> disableSite(@PathVariable Long id) {
        siteService.disable(id);
        return Result.success();
    }

    // ---------- shops ----------
    @GetMapping("/shops")
    @RequirePerm("sys:shop:page")
    public Result<IPage<PlatShop>> pageShops(
            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String platformCode, @RequestParam(required = false) String siteCode,
            @RequestParam(required = false) String status) {
        return Result.success(shopService.page(new Page<>(current, size), platformCode, siteCode, status));
    }

    @GetMapping("/shops/list")
    @RequirePerm("sys:shop:page")
    public Result<List<PlatShop>> listShops(@RequestParam(required = false) String platformCode, @RequestParam(required = false) String siteCode, @RequestParam(required = false) String status) {
        return Result.success(shopService.list(platformCode, siteCode, status));
    }

    @GetMapping("/shops/{id}")
    @RequirePerm("sys:shop:get")
    public Result<PlatShop> getShop(@PathVariable Long id) {
        return Result.success(shopService.get(id));
    }

    @PostMapping("/shops")
    @RequirePerm("sys:shop:create")
    public Result<Void> createShop(@RequestBody PlatShop body) {
        shopService.create(body);
        return Result.success();
    }

    @PutMapping("/shops/{id}")
    @RequirePerm("sys:shop:update")
    public Result<Void> updateShop(@PathVariable Long id, @RequestBody PlatShop body) {
        shopService.update(id, body);
        return Result.success();
    }

    @PostMapping("/shops/{id}/enable")
    @RequirePerm("sys:shop:enable")
    public Result<Void> enableShop(@PathVariable Long id) {
        shopService.enable(id);
        return Result.success();
    }

    @PostMapping("/shops/{id}/disable")
    @RequirePerm("sys:shop:disable")
    public Result<Void> disableShop(@PathVariable Long id) {
        shopService.disable(id);
        return Result.success();
    }

    // ---------- credentials ----------
    @GetMapping("/credentials")
//    @RequirePerm("sys:credential:page")
    public Result<IPage<CredentialVo>> pageCredentials(
            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String platformCode, @RequestParam(required = false) Long shopId) {
        return Result.success(credentialService.page(new Page<>(current, size), platformCode, shopId));
    }

    @GetMapping("/credentials/{id}")
    @RequirePerm("sys:credential:get")
    public Result<CredentialVo> getCredential(@PathVariable Long id) {
        return Result.success(credentialService.get(id));
    }

    @PostMapping("/credentials")
    @RequirePerm("sys:credential:create")
    public Result<Void> createCredential(@RequestBody CredentialCreateDto body) {
        credentialService.create(body);
        return Result.success();
    }

    @PutMapping("/credentials/{id}")
    @RequirePerm("sys:credential:update")
    public Result<Void> updateCredential(@PathVariable Long id, @RequestBody CredentialCreateDto body) {
        credentialService.update(id, body);
        return Result.success();
    }

    @PostMapping("/credentials/{id}/enable")
    @RequirePerm("sys:credential:enable")
    public Result<Void> enableCredential(@PathVariable Long id) {
        credentialService.enable(id);
        return Result.success();
    }

    @PostMapping("/credentials/{id}/disable")
    @RequirePerm("sys:credential:disable")
    public Result<Void> disableCredential(@PathVariable Long id) {
        credentialService.disable(id);
        return Result.success();
    }

    @PostMapping("/credentials/{id}/test")
    @RequirePerm("sys:credential:test")
    public Result<Boolean> testCredential(@PathVariable Long id) {
        return Result.success(credentialService.testConnection(id));
    }

    // ---------- fx-rates ----------
    @GetMapping("/fx-rates")
    @RequirePerm("sys:fx:page")
    public Result<IPage<FxRate>> pageFxRates(
            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String baseCurrency, @RequestParam(required = false) String quoteCurrency,
            @RequestParam(required = false) String status) {
        return Result.success(fxRateService.page(new Page<>(current, size), baseCurrency, quoteCurrency, status));
    }

    @GetMapping("/fx-rates/{id}")
    @RequirePerm("sys:fx:get")
    public Result<FxRate> getFxRate(@PathVariable Long id) {
        return Result.success(fxRateService.get(id));
    }

    @PostMapping("/fx-rates")
    @RequirePerm("sys:fx:create")
    public Result<Void> createFxRate(@RequestBody FxRate body) {
        fxRateService.create(body);
        return Result.success();
    }

    @PutMapping("/fx-rates/{id}")
    @RequirePerm("sys:fx:update")
    public Result<Void> updateFxRate(@PathVariable Long id, @RequestBody FxRate body) {
        fxRateService.update(id, body);
        return Result.success();
    }

    @PostMapping("/fx-rates/{id}/enable")
    @RequirePerm("sys:fx:enable")
    public Result<Void> enableFxRate(@PathVariable Long id) {
        fxRateService.enable(id);
        return Result.success();
    }

    @PostMapping("/fx-rates/{id}/disable")
    @RequirePerm("sys:fx:disable")
    public Result<Void> disableFxRate(@PathVariable Long id) {
        fxRateService.disable(id);
        return Result.success();
    }

    @GetMapping("/fx-rates/quote")
    @RequirePerm("sys:fx:get")
    public Result<BigDecimal> quoteFxRate(@RequestParam String base, @RequestParam String quote, @RequestParam(required = false) LocalDate date) {
        return Result.success(fxRateService.quote(base, quote, date));
    }
}
