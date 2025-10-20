import axiosInstance from './index'

// 请求接口封装
export const axiosGet = (params) => axiosInstance.post('/demo',params);

