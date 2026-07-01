FROM node:22-alpine AS build
WORKDIR /workspace/frontend
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install
COPY frontend .
RUN npm run build

FROM node:22-alpine
WORKDIR /app
RUN npm install -g serve@14
COPY --from=build /workspace/frontend/dist ./dist
EXPOSE 4173
ENTRYPOINT ["serve", "-s", "dist", "-l", "4173"]
