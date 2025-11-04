# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app

# Copy package.json & install dependencies
COPY package*.json ./
RUN npm ci

# Copy all source files
COPY . .

# Build CRA app
RUN npm run build

# Stage 2: Runtime (serve static files)
FROM nginx:alpine
# Copy built files
COPY --from=build /app/build /usr/share/nginx/html

# Copy custom Nginx config for SPA routing
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
