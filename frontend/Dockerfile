# Build the SPA bundle (React Router in SPA mode -> static files in build/client).
FROM node:24-alpine AS build
WORKDIR /app
# package-lock.json is not versioned, so resolve dependencies from package.json.
COPY package*.json ./
RUN npm install --no-audit --no-fund
COPY . .
RUN npm run build

# Serve the static bundle with nginx, which also proxies /api to the backend.
FROM nginx:1.29-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/build/client /usr/share/nginx/html
EXPOSE 80
