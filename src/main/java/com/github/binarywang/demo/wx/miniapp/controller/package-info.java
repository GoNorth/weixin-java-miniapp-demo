package com.github.binarywang.demo.wx.miniapp.controller;

//总览的开发文档：
//https://github.com/binarywang/WxJava/wiki/0_%E5%85%AC%E4%BC%97%E5%8F%B7%E5%BC%80%E5%8F%91%E6%96%87%E6%A1%A3

//需要开的接口：
//AAA：1、用户管理，包括用户列表的查询，用户详情的查询。
//https://github.com/binarywang/WxJava/wiki/MP_%E7%94%A8%E6%88%B7%E7%AE%A1%E7%90%86
//BBB：MP_标签管理
//https://github.com/binarywang/WxJava/wiki/MP_%E6%A0%87%E7%AD%BE%E7%AE%A1%E7%90%86

//MP_发送模板消息
//https://github.com/binarywang/WxJava/wiki/MP_%E5%8F%91%E9%80%81%E6%A8%A1%E6%9D%BF%E6%B6%88%E6%81%AF

//2、消息管理：接收消息：
//AAA：同步回复消息（被动回复） <<< 如：用户发送指令，如：[IMG1223]，则将指定日期的图片结果回复
//https://github.com/binarywang/WxJava/wiki/MP_%E5%90%8C%E6%AD%A5%E5%9B%9E%E5%A4%8D%E6%B6%88%E6%81%AF
//BBB：主动发送消息（客服消息） <<< 如：每日定点发送客户，您的图片生成完毕，请及时发布朋友圈哦。
//https://github.com/binarywang/WxJava/wiki/MP_%E4%B8%BB%E5%8A%A8%E5%8F%91%E9%80%81%E6%B6%88%E6%81%AF%EF%BC%88%E5%AE%A2%E6%9C%8D%E6%B6%88%E6%81%AF%EF%BC%89

//3、MP_群发消息：
//https://github.com/binarywang/WxJava/wiki/MP_%E7%BE%A4%E5%8F%91%E6%B6%88%E6%81%AF

//3、临时素材（3天过期）
//CCC：MP_多媒体文件管理
//https://github.com/binarywang/WxJava/wiki/MP_%E5%A4%9A%E5%AA%92%E4%BD%93%E6%96%87%E4%BB%B6%E7%AE%A1%E7%90%86
