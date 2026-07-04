<template>
  <div class="node-attachments">
    <el-upload :show-file-list="false" :before-upload="handleUpload" accept=".jpg,.jpeg,.png,.gif,.webp,.mp3,.wav,.ogg,.mp4,.pdf">
      <el-button :icon="Upload" :loading="uploading">上传附件</el-button>
    </el-upload>
    <div class="url-attachment">
      <el-input v-model.trim="attachmentUrl" placeholder="输入附件 URL" />
      <el-select v-model="attachmentMediaType" class="url-attachment__type">
        <el-option label="图片" value="IMAGE" />
        <el-option label="音频" value="AUDIO" />
        <el-option label="视频" value="VIDEO" />
        <el-option label="文件" value="FILE" />
      </el-select>
      <el-button @click="addUrlAttachment">添加</el-button>
    </div>
    <div v-for="(attachment, index) in attachments" :key="`${attachment.fileUrl}-${index}`" class="attachment-row">
      <span>{{ attachment.fileName }}</span>
      <el-tag size="small" effect="plain">{{ mediaTypeText(attachment.mediaType) }}</el-tag>
      <el-button :icon="Delete" circle @click="attachments.splice(index, 1)" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, type UploadRawFile } from 'element-plus'
import { Delete, Upload } from '@element-plus/icons-vue'

import type { QuestionAttachmentPayload } from '@/api/exam-business'
import { inferMediaType, mediaTypeText } from '@/utils/question-bank-editor'

const attachments = defineModel<QuestionAttachmentPayload[]>({ required: true })

const props = defineProps<{
  uploading: boolean
  uploadAttachment: (file: UploadRawFile) => Promise<QuestionAttachmentPayload>
}>()

const attachmentUrl = ref('')
const attachmentMediaType = ref<QuestionAttachmentPayload['mediaType']>('FILE')

async function handleUpload(file: UploadRawFile) {
  attachments.value.push(await props.uploadAttachment(file))
  ElMessage.success('附件已上传')
  return false
}

function addUrlAttachment() {
  if (!attachmentUrl.value) {
    ElMessage.error('请输入附件 URL')
    return
  }
  attachments.value.push({
    fileName: attachmentUrl.value.split('/').pop() || 'attachment',
    fileUrl: attachmentUrl.value,
    mediaType: inferMediaType(attachmentUrl.value, attachmentMediaType.value),
  })
  attachmentUrl.value = ''
  attachmentMediaType.value = 'FILE'
}
</script>

<style scoped>
.node-attachments {
  display: grid;
  gap: 10px;
  width: 100%;
}

.url-attachment,
.attachment-row {
  display: grid;
  gap: 8px;
  align-items: center;
  width: 100%;
}

.url-attachment {
  grid-template-columns: minmax(0, 1fr) 96px auto;
}

.attachment-row {
  grid-template-columns: minmax(0, 1fr) auto auto;
}

.attachment-row span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 900px) {
  .url-attachment {
    grid-template-columns: 1fr;
  }
}
</style>
