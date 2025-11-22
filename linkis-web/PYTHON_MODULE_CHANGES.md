# Python Module Feature Toggle - Implementation Summary

## Changes Made

### 1. Environment Variable Configuration
**File**: `.env`
- Added `VUE_APP_ENABLE_PYTHON_MODULE=false` (default disabled)
- This controls whether the Python Module feature is available

### 2. Router Configuration
**File**: `src/apps/linkis/router.js`

**Changes**:
- Added environment variable check: `export const isPythonModuleEnabled = process.env.VUE_APP_ENABLE_PYTHON_MODULE === 'true'`
- Modified pythonModule route to be conditionally loaded:
  ```javascript
  ...(isPythonModuleEnabled ? [{
    name: 'pythonModule',
    path: 'pythonModule',
    component: () => import('./module/pythonModule/index.vue'),
    meta: {
      title: 'pythonModule',
      publicPage: true,
    },
  }] : []),
  ```

### 3. View Component Updates
**File**: `src/apps/linkis/view/linkis/index.vue`

**Changes**:
- Imported `isPythonModuleEnabled` from router
- Added to component data: `isPythonModuleEnabled: isPythonModuleEnabled`
- Modified sidebar menu visibility condition to hide Python Module when disabled:
  ```javascript
  v-if="(!isLogAdmin? ...) && (item.key !== '1-13' || isPythonModuleEnabled)"
  ```
- Updated default redirect logic in `mounted()`:
  ```javascript
  if(!localStorage.getItem('hasRead')) {
    if (isPythonModuleEnabled) {
      this.clickToRoute('1-13-1')  // Python Module
    } else {
      this.clickToRoute('1-1')      // Global History
    }
  }
  ```

## Feature Behavior

### When VUE_APP_ENABLE_PYTHON_MODULE=false (Default)
✅ **Recommended for users without WeBank npm registry access**

- Python Module route is NOT registered
- Python Module menu item is HIDDEN from sidebar
- After login, users are redirected to **Global History** page
- Accessing `/console/pythonModule` directly will show 404 error
- No dependency on internal `@webank` packages required

### When VUE_APP_ENABLE_PYTHON_MODULE=true
⚠️ **Requires WeBank internal npm registry access**

- Python Module route is registered
- Python Module menu item is VISIBLE in sidebar
- After login, users are redirected to **Python Module** page (if first time)
- Accessing `/console/pythonModule` directly works normally
- Requires successful build of PythonModule sub-application

## Testing

To test the feature toggle:

### Test 1: Disabled State (Default)
```bash
# 1. Ensure .env has VUE_APP_ENABLE_PYTHON_MODULE=false
cat linkis-web/.env | grep VUE_APP_ENABLE_PYTHON_MODULE

# 2. Rebuild
cd linkis-web
npm run build

# 3. Verify behavior:
# - Python Module menu should NOT appear in sidebar
# - Login should redirect to Global History
# - /console/pythonModule should return 404
```

### Test 2: Enabled State
```bash
# 1. Update .env
echo "VUE_APP_ENABLE_PYTHON_MODULE=true" >> linkis-web/.env

# 2. Rebuild
cd linkis-web
npm run build

# 3. Verify behavior:
# - Python Module menu should appear in sidebar
# - Login should redirect to Python Module
# - /console/pythonModule should work (if built)
```

## Migration Notes

- **Backward Compatibility**: Default is `false`, so existing deployments won't be affected
- **No Breaking Changes**: All other features remain unchanged
- **Easy Rollback**: Simply set `VUE_APP_ENABLE_PYTHON_MODULE=false` and rebuild

## Documentation

- Detailed configuration guide: `PYTHON_MODULE_CONFIG.md`
- This summary document: `PYTHON_MODULE_CHANGES.md`

## Next Steps

For users who want to enable Python Module:
1. Obtain access to WeBank internal npm registry
2. Configure `.npmrc` with registry credentials
3. Set `VUE_APP_ENABLE_PYTHON_MODULE=true` in `.env`
4. Run `npm run installAll` to install all dependencies
5. Run `npm run buildSubModule` to build Python Module
6. Run `npm run build` to build main application
