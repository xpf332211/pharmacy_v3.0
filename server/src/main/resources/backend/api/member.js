function getMemberList (params) {
  return $axios({
    url: '/staff/page',
    method: 'get',
    params
  })
}

// 修改---启用禁用接口
function enableOrDisableStaff (params) {
  return $axios({
    url: '/staff',
    method: 'put',
    data: { ...params }
  })
}

// 新增---添加员工
function addStaff (params) {
  return $axios({
    url: '/staff',
    method: 'post',
    data: { ...params }
  })
}

// 修改---添加员工
function editStaff (params) {
  return $axios({
    url: '/staff',
    method: 'put',
    data: { ...params }
  })
}

// 修改页面反查详情接口
function queryStaffById (id) {
  return $axios({
    url: `/staff/${id}`,
    method: 'get'
  })
}