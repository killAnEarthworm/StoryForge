const path = require('path')
module.exports = {
    base:"./",//公共基础路径 打包路径
    resolve: {
        alias:{
            '@': path.resolve(__dirname, './src')//别名设置
        }
    },
    // host: 'localhost',
    port:8888,//启动端口
    // https: true, // 开启https
    open: true, // 自动开启窗口
    proxy: { // 代理配置
        '/api': {
            target: 'http://localhost:8080',//后端服务地址
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, '') // 重写路径
        }
    },
}