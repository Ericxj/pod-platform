import { requestClient } from '#/api/request';

export interface PageResult<T> {
  records: T[];
  total: number;
  current?: number;
  size?: number;
}

export interface PlatPlatformRecord {
  id?: number;
  platformCode?: string;
  platformName?: string;
  status?: string;
}

export interface PlatSiteRecord {
  id?: number;
  platformCode?: string;
  siteCode?: string;
  siteName?: string;
  countryCode?: string;
  currency?: string;
  timezone?: string;
  status?: string;
}

export interface PlatShopRecord {
  id?: number;
  platformCode?: string;
  shopCode?: string;
  shopName?: string;
  siteCode?: string;
  currency?: string;
  status?: string;
}

export interface CredentialRecord {
  id?: number;
  platformCode?: string;
  shopId?: number;
  authType?: string;
  credentialName?: string;
  payloadMasked?: string;
  expiresAt?: string;
  status?: string;
}

export interface CredentialCreateDto {
  platformCode: string;
  shopId: number;
  authType: string;
  credentialName?: string;
  payloadPlainJson: string;
  status?: string;
}

export interface FxRateRecord {
  id?: number;
  baseCurrency?: string;
  quoteCurrency?: string;
  rate?: number;
  effectiveDate?: string;
  source?: string;
  status?: string;
}

const SYS = '/sys';

export function getPlatformPage(params: { current?: number; size?: number; status?: string }) {
  return requestClient.get<PageResult<PlatPlatformRecord>>(SYS + '/platforms', { params });
}

export function getPlatformList(status?: string) {
  return requestClient.get<PlatPlatformRecord[]>(SYS + '/platforms/list', { params: status ? { status } : {} });
}

export function getPlatform(id: number) {
  return requestClient.get<PlatPlatformRecord>(`${SYS}/platforms/${id}`);
}

export function createPlatform(data: Partial<PlatPlatformRecord>) {
  return requestClient.post(SYS + '/platforms', data);
}

export function updatePlatform(id: number, data: Partial<PlatPlatformRecord>) {
  return requestClient.put(`${SYS}/platforms/${id}`, data);
}

export function enablePlatform(id: number) {
  return requestClient.post(`${SYS}/platforms/${id}/enable`);
}

export function disablePlatform(id: number) {
  return requestClient.post(`${SYS}/platforms/${id}/disable`);
}

export function getSitePage(params: { current?: number; size?: number; platformCode?: string; status?: string }) {
  return requestClient.get<PageResult<PlatSiteRecord>>(SYS + '/sites', { params });
}

export function getSiteList(platformCode?: string, status?: string) {
  return requestClient.get<PlatSiteRecord[]>(SYS + '/sites/list', { params: { platformCode, status } });
}

export function getSite(id: number) {
  return requestClient.get<PlatSiteRecord>(`${SYS}/sites/${id}`);
}

export function createSite(data: Partial<PlatSiteRecord>) {
  return requestClient.post(SYS + '/sites', data);
}

export function updateSite(id: number, data: Partial<PlatSiteRecord>) {
  return requestClient.put(`${SYS}/sites/${id}`, data);
}

export function enableSite(id: number) {
  return requestClient.post(`${SYS}/sites/${id}/enable`);
}

export function disableSite(id: number) {
  return requestClient.post(`${SYS}/sites/${id}/disable`);
}

export function getShopPage(params: { current?: number; size?: number; platformCode?: string; siteCode?: string; status?: string }) {
  return requestClient.get<PageResult<PlatShopRecord>>(SYS + '/shops', { params });
}

export function getShopList(platformCode?: string, siteCode?: string, status?: string) {
  return requestClient.get<PlatShopRecord[]>(SYS + '/shops/list', { params: { platformCode, siteCode, status } });
}

export function getShop(id: number) {
  return requestClient.get<PlatShopRecord>(`${SYS}/shops/${id}`);
}

export function createShop(data: Partial<PlatShopRecord>) {
  return requestClient.post(SYS + '/shops', data);
}

export function updateShop(id: number, data: Partial<PlatShopRecord>) {
  return requestClient.put(`${SYS}/shops/${id}`, data);
}

export function enableShop(id: number) {
  return requestClient.post(`${SYS}/shops/${id}/enable`);
}

export function disableShop(id: number) {
  return requestClient.post(`${SYS}/shops/${id}/disable`);
}

export function getCredentialPage(params: { current?: number; size?: number; platformCode?: string; shopId?: number }) {
  return requestClient.get<PageResult<CredentialRecord>>(SYS + '/credentials', { params });
}

export function getCredential(id: number) {
  return requestClient.get<CredentialRecord>(`${SYS}/credentials/${id}`);
}

export function createCredential(data: CredentialCreateDto) {
  return requestClient.post(SYS + '/credentials', data);
}

export function updateCredential(id: number, data: Partial<CredentialCreateDto>) {
  return requestClient.put(`${SYS}/credentials/${id}`, data);
}

export function enableCredential(id: number) {
  return requestClient.post(`${SYS}/credentials/${id}/enable`);
}

export function disableCredential(id: number) {
  return requestClient.post(`${SYS}/credentials/${id}/disable`);
}

export function testCredential(id: number) {
  return requestClient.post<boolean>(`${SYS}/credentials/${id}/test`);
}

export function getFxRatePage(params: { current?: number; size?: number; baseCurrency?: string; quoteCurrency?: string; status?: string }) {
  return requestClient.get<PageResult<FxRateRecord>>(SYS + '/fx-rates', { params });
}

export function getFxRate(id: number) {
  return requestClient.get<FxRateRecord>(`${SYS}/fx-rates/${id}`);
}

export function createFxRate(data: Partial<FxRateRecord>) {
  return requestClient.post(SYS + '/fx-rates', data);
}

export function updateFxRate(id: number, data: Partial<FxRateRecord>) {
  return requestClient.put(`${SYS}/fx-rates/${id}`, data);
}

export function enableFxRate(id: number) {
  return requestClient.post(`${SYS}/fx-rates/${id}/enable`);
}

export function disableFxRate(id: number) {
  return requestClient.post(`${SYS}/fx-rates/${id}/disable`);
}

export function getFxQuote(base: string, quote: string, date?: string) {
  return requestClient.get<number>(SYS + '/fx-rates/quote', { params: { base, quote, date } });
}
