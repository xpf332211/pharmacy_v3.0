const senMsg = (msg) => {
    return $axios({
        url: `/notice/sendMsg?msg=${msg}`,
        method: 'get'
    })
}
