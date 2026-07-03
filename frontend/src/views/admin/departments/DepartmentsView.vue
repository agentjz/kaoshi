<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>部门管理</h1>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openCreateDialog(null)">新建部门</el-button>
      </div>
    </header>

    <div class="management-workbench">
      <aside class="tree-pane" v-loading="loading">
        <div class="pane-title">
          <strong>组织树</strong>
          <span>{{ flatDepartments.length }} 个部门</span>
        </div>
        <el-empty v-if="departments.length === 0" description="暂无部门" />
        <el-tree
          v-else
          ref="treeRef"
          :data="departments"
          node-key="id"
          default-expand-all
          highlight-current
          :expand-on-click-node="false"
          :props="{ label: 'name', children: 'children' }"
          @node-click="selectDepartment"
        >
          <template #default="{ data }: { data: Department }">
            <div class="department-node">
              <div class="department-node__main">
                <span class="entity-name">{{ data.name }}</span>
                <span class="muted-text">{{ data.code }} · 下级 {{ data.children.length }}</span>
              </div>
              <div class="department-node__actions">
                <el-button link type="primary" @click.stop="openCreateDialog(data)">新建下级</el-button>
                <el-button link type="primary" @click.stop="openEditDialog(data)">编辑</el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </aside>

      <main class="detail-pane">
        <el-empty v-if="!selectedDepartment" description="请选择部门" />
        <template v-else>
          <section class="detail-head">
            <div class="entity-stack">
              <span class="muted-text">当前部门</span>
              <h2>{{ selectedDepartment.name }}</h2>
              <span class="code-text">{{ selectedDepartment.code }}</span>
            </div>
            <div class="header-actions">
              <el-button :icon="Plus" @click="openCreateDialog(selectedDepartment)">新建下级</el-button>
              <el-button type="primary" @click="openEditDialog(selectedDepartment)">编辑</el-button>
              <el-button type="danger" plain @click="removeDepartment(selectedDepartment)">删除</el-button>
            </div>
          </section>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="上级部门">{{ parentName(selectedDepartment.parentId) }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="selectedDepartment.status === 'ACTIVE' ? 'success' : 'info'" effect="plain">
                {{ selectedDepartment.status === 'ACTIVE' ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="说明" :span="2">{{ selectedDepartment.description || '无说明' }}</el-descriptions-item>
          </el-descriptions>

          <section class="child-section">
            <div class="section-title">
              <h2>直属下级</h2>
              <span class="muted-text">{{ selectedDepartment.children.length }} 个</span>
            </div>
            <el-table v-if="selectedDepartment.children.length" :data="selectedDepartment.children" border class="data-table">
              <el-table-column prop="name" label="部门" min-width="180" />
              <el-table-column prop="code" label="编码" min-width="180" show-overflow-tooltip />
              <el-table-column label="状态" width="100">
                <template #default="{ row }: { row: Department }">
                  <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" effect="plain">
                    {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }: { row: Department }">
                  <el-button link type="primary" @click="selectDepartment(row)">查看</el-button>
                  <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无直属下级" />
          </section>
        </template>
      </main>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingDepartment ? '编辑部门' : '新建部门'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            clearable
            class="form-control"
            placeholder="不选择则为顶级部门"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model.trim="form.name" maxlength="128" />
        </el-form-item>
        <el-form-item label="部门编码">
          <el-input v-model.trim="form.code" maxlength="64" disabled />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio-button value="ACTIVE">启用</el-radio-button>
            <el-radio-button value="DISABLED">禁用</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model.trim="form.description" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitDepartment">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

import {
  createDepartment,
  deleteDepartment,
  fetchDepartments,
  updateDepartment,
  type Department,
  type DepartmentPayload,
} from '@/api/admin'

const departments = ref<Department[]>([])
const selectedDepartment = ref<Department | null>(null)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingDepartment = ref<Department | null>(null)
const formRef = ref<FormInstance>()
const treeRef = ref()

const form = reactive<DepartmentPayload>({
  parentId: null,
  name: '',
  code: '',
  description: '',
  status: 'ACTIVE',
})

const rules: FormRules<DepartmentPayload> = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const flatDepartments = computed(() => flattenDepartments(departments.value))
const parentOptions = computed(() => {
  if (!editingDepartment.value) {
    return departments.value
  }
  return withoutDepartment(departments.value, editingDepartment.value.id)
})

onMounted(loadDepartments)

watch(
  () => [form.name, form.parentId, editingDepartment.value?.id] as const,
  () => {
    if (!editingDepartment.value) {
      form.code = generateDepartmentCode(form.name, form.parentId)
    }
  },
)

async function loadDepartments(preferredId = selectedDepartment.value?.id) {
  loading.value = true
  try {
    departments.value = await fetchDepartments()
    selectedDepartment.value = (preferredId && findDepartment(departments.value, preferredId)) || departments.value[0] || null
    await nextTick()
    if (selectedDepartment.value) {
      treeRef.value?.setCurrentKey(selectedDepartment.value.id)
    }
  } finally {
    loading.value = false
  }
}

function selectDepartment(department: Department) {
  selectedDepartment.value = department
  treeRef.value?.setCurrentKey(department.id)
}

function openCreateDialog(parent: Department | null) {
  editingDepartment.value = null
  form.parentId = parent?.id ?? null
  form.name = ''
  form.code = generateDepartmentCode('', parent?.id ?? null)
  form.description = ''
  form.status = 'ACTIVE'
  dialogVisible.value = true
}

function openEditDialog(department: Department) {
  editingDepartment.value = department
  form.parentId = department.parentId
  form.name = department.name
  form.code = department.code
  form.description = department.description || ''
  form.status = department.status
  dialogVisible.value = true
}

async function submitDepartment() {
  await formRef.value?.validate()
  saving.value = true
  try {
    let saved: Department
    if (editingDepartment.value) {
      saved = await updateDepartment(editingDepartment.value.id, { ...form })
      ElMessage.success('部门已更新')
    } else {
      saved = await createDepartment({ ...form })
      ElMessage.success('部门已创建')
    }
    dialogVisible.value = false
    await loadDepartments(saved.id)
  } finally {
    saving.value = false
  }
}

async function removeDepartment(department: Department) {
  await ElMessageBox.confirm(`确认删除“${department.name}”？`, '删除部门', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  await deleteDepartment(department.id)
  ElMessage.success('部门已删除')
  await loadDepartments(department.parentId ?? undefined)
}

function parentName(parentId: number | null) {
  if (!parentId) {
    return '顶级部门'
  }
  return findDepartment(departments.value, parentId)?.name || '顶级部门'
}

function flattenDepartments(items: Department[]): Department[] {
  return items.flatMap((item) => [item, ...flattenDepartments(item.children)])
}

function findDepartment(items: Department[], id: number): Department | null {
  for (const item of items) {
    if (item.id === id) {
      return item
    }
    const matched = findDepartment(item.children, id)
    if (matched) {
      return matched
    }
  }
  return null
}

function withoutDepartment(items: Department[], departmentId: number): Department[] {
  return items
    .filter((item) => item.id !== departmentId)
    .map((item) => ({
      ...item,
      children: withoutDepartment(item.children, departmentId),
    }))
}

function generateDepartmentCode(name: string, parentId: number | null) {
  const prefix = parentId ? `D${parentId}` : 'D'
  const source = name.trim()
  const readable = source
    ? Array.from(source)
        .map((char) => char.charCodeAt(0).toString(36).toUpperCase())
        .join('')
        .slice(0, 18)
    : 'NEW'
  return `${prefix}_${readable}_${Date.now().toString(36).toUpperCase()}`
}
</script>

<style scoped>
.management-workbench {
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
  gap: 16px;
  min-width: 0;
}

.tree-pane,
.detail-pane {
  min-width: 0;
  padding: 18px;
  border: 1px solid var(--ks-border);
  border-radius: var(--ks-radius);
  background: var(--ks-panel);
}

.tree-pane :deep(.el-tree-node__content) {
  height: auto;
  min-height: 54px;
  align-items: flex-start;
  padding-top: 5px;
  padding-bottom: 5px;
  border-radius: 6px;
}

.tree-pane :deep(.el-tree-node__expand-icon) {
  margin-top: 12px;
  flex: none;
}

.pane-title,
.detail-head,
.department-node,
.child-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.pane-title {
  margin-bottom: 12px;
}

.pane-title span {
  color: var(--ks-text-muted);
  font-size: 13px;
}

.department-node {
  width: 100%;
  min-width: 0;
  min-height: 44px;
  padding: 2px 0;
}

.department-node__main {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.department-node__main .entity-name,
.department-node__main .muted-text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 20px;
}

.department-node__actions {
  display: none;
  flex: none;
  gap: 4px;
}

.department-node:hover .department-node__actions,
.el-tree-node.is-current .department-node__actions {
  display: flex;
}

.detail-pane,
.child-section {
  display: grid;
  align-items: stretch;
}

.detail-head h2 {
  margin: 0;
  font-size: 20px;
  letter-spacing: 0;
}

.child-section {
  gap: 12px;
  margin-top: 16px;
}

@media (max-width: 900px) {
  .management-workbench {
    grid-template-columns: 1fr;
  }

  .detail-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
