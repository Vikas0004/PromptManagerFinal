# ---- Build step ----
FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

# ARG VITE_API_URL
# ENV VITE_API_URL=$VITE_API_URL

# ARG VITE_IMPROVE_API_URL
# ENV VITE_IMPROVE_API_URL=$VITE_IMPROVE_API_URL

RUN npm run build

# ---- Production NGINX image ----
FROM nginx:alpine

RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

# FIX IS HERE
COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]