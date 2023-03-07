// 查询列表接口
const getDrugPage = (params) => {
  return $axios({
    url: '/drug/page',
    method: 'get',
    params
  })
}

// 删除接口
const deleteDrug = (ids) => {
  return $axios({
    url: '/drug',
    method: 'delete',
    params: { ids }
  })
}

// 修改接口
const editDrug = (params) => {
  return $axios({
    url: '/drug',
    method: 'put',
    data: { ...params }
  })
}

// 新增接口
const addDrug = (params) => {
  return $axios({
    url: '/drug',
    method: 'post',
    data: { ...params }
  })
}

// 查询详情
const queryDrugById = (id) => {
  return $axios({
    url: `/drug/${id}`,
    method: 'get'
  })
}

// 获取菜品分类列表
const getCategoryList = (params) => {
  return $axios({
    url: '/category/list',
    method: 'get',
    params
  })
}

// 查菜品列表的接口
const queryDrugList = (params) => {
  return $axios({
    url: '/drug/list',
    method: 'get',
    params
  })
}

// 文件down预览
const commonDownload = (params) => {
  return $axios({
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
    },
    url: '/common/download',
    method: 'get',
    params
  })
}

// 起售停售---批量起售停售接口
const drugStatusByStatus = (params) => {
  return $axios({
    url: `/drug/status/${params.status}`,
    method: 'post',
    params: { ids: params.id }
  })
}