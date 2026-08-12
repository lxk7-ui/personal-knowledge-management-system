<template>
  <div class="note-edit-page">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入笔记标题" size="large" />
      </el-form-item>

      <div class="form-row">
        <el-form-item label="分类" style="flex:1">
          <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签" style="flex:2">
          <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width:100%">
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
      </div>

      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="简要描述笔记内容（选填）" />
      </el-form-item>

      <el-form-item label="内容" prop="content">
        <div class="editor-wrapper">
          <QuillEditor
            v-model:content="form.content"
            contentType="html"
            theme="snow"
            :toolbar="toolbarOptions"
            style="min-height: 200px"
          />
        </div>
      </el-form-item>

      <div class="form-row" style="gap:12px">
        <el-form-item>
          <el-checkbox v-model="isTop">置顶</el-checkbox>
        </el-form-item>
      </div>

      <div class="form-actions">
        <el-button @click="$router.back()" class="btn-click">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave" class="btn-click">
          {{ isEdit ? '保存修改' : '发布笔记' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { createNote, updateNote, getNoteDetail } from '../../api/note'
import { getCategoryList } from '../../api/category'
import { getTagList } from '../../api/tag'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const saving = ref(false)
const categories = ref([])
const tags = ref([])
const isTop = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  title: '',
  content: '',
  summary: '',
  categoryId: null,
  tagIds: [],
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const toolbarOptions = [
  ['bold', 'italic', 'underline', 'strike'],
  ['blockquote', 'code-block'],
  [{ header: [1, 2, 3, false] }],
  [{ list: 'ordered' }, { list: 'bullet' }],
  [{ color: [] }, { background: [] }],
  ['link'],
  ['clean']
]

onMounted(async () => {
  const [catRes, tagRes] = await Promise.all([getCategoryList(), getTagList()])
  categories.value = catRes.data || []
  tags.value = tagRes.data || []

  if (route.params.id) {
    const res = await getNoteDetail(route.params.id)
    const note = res.data.note
    form.id = note.id
    form.title = note.title
    form.content = note.content || ''
    form.summary = note.summary || ''
    form.categoryId = note.categoryId
    form.tagIds = res.data.tagIds || []
    form.status = note.status
    isTop.value = note.isTop === 1
  }
})

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const data = { ...form, isTop: isTop.value ? 1 : 0 }
    if (isEdit.value) {
      await updateNote(data)
      ElMessage.success('保存成功')
    } else {
      await createNote(data)
      ElMessage.success('发布成功')
    }
    router.push('/user/notes')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.note-edit-page {
  max-width: 100%;
}

.form-row {
  display: flex;
  gap: 16px;
}

.editor-wrapper {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.editor-wrapper :deep(.ql-toolbar) {
  border-radius: 12px 12px 0 0;
  background: rgba(90, 74, 58, 0.03);
  border-color: rgba(90, 74, 58, 0.12);
}

.editor-wrapper :deep(.ql-container) {
  border-radius: 0 0 12px 12px;
  border-color: rgba(90, 74, 58, 0.12);
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}
</style>
