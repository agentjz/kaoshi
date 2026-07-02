<template>
  <main class="error-page">
    <h1>404</h1>
    <p>页面不存在。</p>
    <el-button type="primary" @click="$router.replace('/dashboard')">返回工作台</el-button>
  </main>
</template>
