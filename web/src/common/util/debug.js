/**
 * 日志输出控制
 * @param {String|Object} info 日志输出
 * @param {String} type console可用方法
 */
function debug_log (type = 'log', ...info) {
  const isDev = process.env.NODE_ENV === 'dev'
  if (isDev || window.debug_log === true) {
    if( console[type]) {
      console[type](...info)
    }
  }
}
export default debug_log
