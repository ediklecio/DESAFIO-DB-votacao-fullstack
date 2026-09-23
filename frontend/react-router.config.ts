import type { Config } from "@react-router/dev/config";

export default {
  // SPA mode: the app is a static bundle served by nginx; all data comes from the REST API.
  ssr: false,
} satisfies Config;
