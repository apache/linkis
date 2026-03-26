# Python Module Feature Configuration

## Overview

The Python Module feature can be enabled or disabled via environment variable configuration. This feature depends on internal WeBank npm packages (`@webank/fes-design-material`, `@webank/letgo-components`) which are not publicly available.

## Configuration

### Environment Variable

Edit the `.env` file in the `linkis-web` root directory:

```bash
# Enable or disable Python Module feature (true/false)
# Set to false if you cannot access @webank internal npm packages
VUE_APP_ENABLE_PYTHON_MODULE=false
```

### Options

- `VUE_APP_ENABLE_PYTHON_MODULE=true`: Enable Python Module feature
  - The Python Module menu item will be displayed in the sidebar
  - After login, users will be redirected to the Python Module page
  - Requires access to WeBank internal npm registry to build

- `VUE_APP_ENABLE_PYTHON_MODULE=false`: Disable Python Module feature (Default)
  - The Python Module menu item will be hidden from the sidebar
  - After login, users will be redirected to the Global History page
  - No special npm registry access required

## Implementation Details

### Modified Files

1. **`.env`**: Added `VUE_APP_ENABLE_PYTHON_MODULE` environment variable
2. **`src/apps/linkis/router.js`**:
   - Added `isPythonModuleEnabled` constant
   - Conditionally load pythonModule route
3. **`src/apps/linkis/view/linkis/index.vue`**:
   - Import `isPythonModuleEnabled` from router
   - Conditionally display pythonModule in sidebar menu
   - Modified default redirect logic based on feature flag

### Behavior

When `VUE_APP_ENABLE_PYTHON_MODULE=false`:
- ❌ Python Module route is not registered
- ❌ Python Module menu item is hidden
- ✅ Users are redirected to Global History page after login
- ✅ Direct access to `/console/pythonModule` will show 404

When `VUE_APP_ENABLE_PYTHON_MODULE=true`:
- ✅ Python Module route is registered
- ✅ Python Module menu item is visible
- ✅ Users are redirected to Python Module page after login (if not `hasRead`)
- ✅ Direct access to `/console/pythonModule` works

## Building

After changing the environment variable:

```bash
# Rebuild the project
cd linkis-web
npm run build
```

## For Internal Users

If you have access to WeBank's internal npm registry:

1. Set `VUE_APP_ENABLE_PYTHON_MODULE=true` in `.env`
2. Configure npm registry (contact your administrator for registry URL)
3. Install dependencies: `npm run installAll`
4. Build PythonModule: `npm run buildSubModule`
5. Build main application: `npm run build`

## Troubleshooting

### Issue: 404 Error when accessing Python Module

**Solution**: Check that `VUE_APP_ENABLE_PYTHON_MODULE=true` and rebuild the application

### Issue: Python Module menu not showing

**Solution**:
1. Verify `.env` has `VUE_APP_ENABLE_PYTHON_MODULE=true`
2. Clear browser cache
3. Rebuild: `npm run build`

### Issue: Cannot install dependencies for PythonModule

**Solution**:
1. Set `VUE_APP_ENABLE_PYTHON_MODULE=false` to disable the feature
2. Or configure access to WeBank internal npm registry
