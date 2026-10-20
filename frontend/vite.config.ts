import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const env =loadEnv(mode, process.cwd(), "");
  const proxy = {
    "/api": {
      target: env.BACKEND_URL || "http://localhost:8080",
      changeOrigin: true,
      rewrite: (path: string) =>path.replace(/^\/api/, ""),
    },
  };
  //verification emails point to localhost:5173, not allwoed to silently use another port.
  return {
    plugins: [react()],
    server: { port: 5173, strictPort: true, proxy },
    preview: { proxy },
  };
});
