import { createJiti } from "../../../node_modules/.pnpm/jiti@2.4.2/node_modules/jiti/lib/jiti.mjs";

const jiti = createJiti(import.meta.url, {
  "interopDefault": true,
  "alias": {
    "@vben/vite-config": "/Users/超级无敌巨重要的资料/大三下/JAVA综合实训/222090140_俞云烽_工程源代码/audit-study-hub-admin/internal/vite-config"
  },
  "transformOptions": {
    "babel": {
      "plugins": []
    }
  }
})

/** @type {import("/Users/超级无敌巨重要的资料/大三下/JAVA综合实训/222090140_俞云烽_工程源代码/audit-study-hub-admin/internal/vite-config/src/index.js")} */
const _module = await jiti.import("/Users/超级无敌巨重要的资料/大三下/JAVA综合实训/222090140_俞云烽_工程源代码/audit-study-hub-admin/internal/vite-config/src/index.ts");

export const loadAndConvertEnv = _module.loadAndConvertEnv;
export const defineConfig = _module.defineConfig;
export const defineApplicationConfig = _module.defineApplicationConfig;
export const defineLibraryConfig = _module.defineLibraryConfig;
export const defaultImportmapOptions = _module.defaultImportmapOptions;
export const getDefaultPwaOptions = _module.getDefaultPwaOptions;
export const loadApplicationPlugins = _module.loadApplicationPlugins;
export const loadLibraryPlugins = _module.loadLibraryPlugins;
export const viteArchiverPlugin = _module.viteArchiverPlugin;
export const viteCompressPlugin = _module.viteCompressPlugin;
export const viteDtsPlugin = _module.viteDtsPlugin;
export const viteHtmlPlugin = _module.viteHtmlPlugin;
export const viteVisualizerPlugin = _module.viteVisualizerPlugin;
export const viteVxeTableImportsPlugin = _module.viteVxeTableImportsPlugin;