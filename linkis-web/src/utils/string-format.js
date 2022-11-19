String.prototype.format = function(args) {
  let result = this;
  if (arguments.length < 1) {
    return result;
  }
  let data = arguments;		//如果模板参数是数组
  if (arguments.length == 1 && typeof (args) == "object") {
    //如果模板参数是对象
    data = args;
  }
  for (let key in data) {
    let value = data[key];
    if (undefined != value) {
      result = result.replace("{" + key + "}", value);
    }
  }
  return result;
}
