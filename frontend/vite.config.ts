import { reactRouter } from "@react-router/dev/vite";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "vite";

// In dev, /api is proxied to the Spring Boot backend so the browser talks to a
// single origin (same setup nginx provides in Docker) - no CORS needed.
const apiTarget = process.env.API_URL ?? "http://localhost:8080";

export default defineConfig({
  plugins: [tailwindcss(), reactRouter()],
  resolve: {
    tsconfigPaths: true,
  },
  server: {
    proxy: {
      "/api": apiTarget,
    },
  },
});
