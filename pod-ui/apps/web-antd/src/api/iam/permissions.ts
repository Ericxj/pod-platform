/**
 * IAM 权限/菜单 API（数据源 iam_permission，perm_type=MENU/BUTTON/API）
 */
export {
  getPermissionTree,
  getPermissionPage,
  pagePermissions,
  getPermission,
  createPermission,
  updatePermission,
  deletePermission,
  validatePermission,
} from '#/api/system/permission';

export type {
  PermissionTreeDto,
  PermissionCreateDto,
  PermissionUpdateDto,
  PermissionValidateResult,
  PermissionPageQuery,
  PermissionPageResult,
} from '#/api/system/permission';
