// 查询列表数据
const getSuitPage = (params) => {
  return $axios({
    url: '/suit/page',
    method: 'get',
    params
  })
}

// 删除数据接口
const deleteSuit = (ids) => {
  return $axios({
    url: '/suit',
    method: 'delete',
    params: { ids }
  })
}

// 修改数据接口
const editSuit = (params) => {
  return $axios({
    url: '/suit',
    method: 'put',
    data: { ...params }
  })
}

// 新增数据接口
const addSuit = (params) => {
  return $axios({
    url: '/suit',
    method: 'post',
    data: { ...params }
  })
}

// 查询详情接口
const querySuitById = (id) => {
  return $axios({
    url: `/suit/${id}`,
    method: 'get'
  })
}

// 批量起售禁售
const suitStatusByStatus = (params) => {
  return $axios({
    url: `/suit/status/${params.status}`,
    method: 'post',
    params: { ids: params.ids }
  })
}
