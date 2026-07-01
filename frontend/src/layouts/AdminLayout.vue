<template>
  <el-container class="shell">
    <el-aside class="sidebar" width="232px">
      <div class="brand">
        <span class="brand-mark">G</span>
        <div>
          <strong>考试</strong>
          <small>考试管理平台</small>
        </div>
      </div>
      <el-menu router :default-active="route.path" class="menu">
        <el-menu-item index="/admin">
          <el-icon><Grid /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/roles">
          <el-icon><Key /></el-icon>
          <span>角色管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/permissions">
          <el-icon><Lock /></el-icon>
          <span>权限清单</span>
        </el-menu-item>
        <el-menu-item index="/admin/menus">
          <el-icon><MenuIcon /></el-icon>
          <span>菜单清单</span>
        </el-menu-item>
        <el-menu-item index="/admin/question-banks">
          <el-icon><Collection /></el-icon>
          <span>题库管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/questions">
          <el-icon><Document /></el-icon>
          <span>试题管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/papers">
          <el-icon><Tickets /></el-icon>
          <span>试卷管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/exams">
          <el-icon><Timer /></el-icon>
          <span>考试管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/results">
          <el-icon><DataAnalysis /></el-icon>
          <span>成绩管理</span>
        </el-menu-item>
        <el-menu-item index="/exam">
          <el-icon><EditPen /></el-icon>
          <span>考试端</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div>
          <div class="page-title">{{ String(route.meta.title || '管理端') }}</div>
          <div class="page-subtitle">题库、试卷、考试、成绩和权限集中管理。</div>
        </div>
        <el-dropdown @command="handleCommand">
          <button class="user-button" type="button">
            <el-avatar :size="32">{{ auth.displayName.slice(0, 1) }}</el-avatar>
            <span>{{ auth.displayName }}</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="content">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import {
  Collection,
  DataAnalysis,
  Document,
  EditPen,
  Grid,
  Key,
  Lock,
  Menu as MenuIcon,
  Tickets,
  Timer,
  User,
} from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

async function handleCommand(command: string) {
  if (command === 'logout') {
    await auth.logout()
    await router.replace({ name: 'login' })
  }
}
</script>

