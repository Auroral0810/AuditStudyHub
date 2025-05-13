import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
        secure: false,
        ws: true,
        configure: (proxy, options) => {
          // 配置代理
          proxy.on('error', (err, req, res) => {
            console.log('proxy error', err)
          })
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // 添加CORS头
            proxyReq.setHeader('Origin', 'http://localhost:3000')
            console.log('Sending Request to the Target:', req.method, req.url)
          })
          proxy.on('proxyRes', (proxyRes, req, res) => {
            // 添加CORS头
            proxyRes.headers['Access-Control-Allow-Origin'] = '*'
            proxyRes.headers['Access-Control-Allow-Credentials'] = 'true'
            proxyRes.headers['Access-Control-Allow-Methods'] = 'GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS'
            proxyRes.headers['Access-Control-Allow-Headers'] = '*'
            
            // 特殊处理PDF代理请求
            if (req.url.includes('/proxy-pdf')) {
              console.log('PDF Proxy Response:', proxyRes.statusCode)
            } else {
              console.log('Received Response from the Target:', proxyRes.statusCode, req.url)
            }
          })
        }
      }
    }
  }
})
