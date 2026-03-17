import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";

export default defineConfig({
  plugins: [react()],
  define: { global: "globalThis" },
  appType: "spa",
  server: {
    host: true,
    proxy: {
      // REST API
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      // WebSocket/SockJS endpoint
      "/ws": {
        target: "http://localhost:8080",
        changeOrigin: true,
        ws: true,
      },
    },
  },
});