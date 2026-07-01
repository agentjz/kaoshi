<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>菜单清单</h1>
      </div>
      <el-button :icon="Refresh" @click="loadMenus">刷新</el-button>
    </header>

    <el-table v-loading="loading" :data="menus" class="data-table" border>
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column label="菜单" min-width="220">
        <template #default="{ row }: { row: AdminMenu }">
          <div class="entity-stack">
            <span class="entity-name">{{ row.title }}</span>
            <span class="code-text">{{ row.code }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路径" min-width="200" />
      <el-table-column prop="icon" label="图标" width="120" />
      <el-table-column prop="parentId" label="父级 ID" width="110" />
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'

import { fetchAdminMenus, type AdminMenu } from '@/api/admin'

const menus = ref<AdminMenu[]>([])
const loading = ref(false)

onMounted(loadMenus)

async function loadMenus() {
  loading.value = true
  try {
    menus.value = await fetchAdminMenus()
  } finally {
    loading.value = false
  }
}
</script>
