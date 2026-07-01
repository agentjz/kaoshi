<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>权限清单</h1>
      </div>
      <el-button :icon="Refresh" @click="loadPermissions">刷新</el-button>
    </header>

    <el-table v-loading="loading" :data="permissions" class="data-table" border>
      <el-table-column label="权限" min-width="220">
        <template #default="{ row }: { row: AdminPermission }">
          <div class="entity-stack">
            <span class="entity-name">{{ row.name }}</span>
            <span class="code-text">{{ row.code }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="240" show-overflow-tooltip />
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'

import { fetchAdminPermissions, type AdminPermission } from '@/api/admin'

const permissions = ref<AdminPermission[]>([])
const loading = ref(false)

onMounted(loadPermissions)

async function loadPermissions() {
  loading.value = true
  try {
    permissions.value = await fetchAdminPermissions()
  } finally {
    loading.value = false
  }
}
</script>
