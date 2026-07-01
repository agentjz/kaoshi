<template>
  <main class="error-page">
    <h1>403</h1>
    <p>当前账号没有访问权限。</p>
    <el-button type="primary" @click="$router.replace('/admin')">返回工作台</el-button>
  </main>
</template>
