RxLibrary工程：<br>1.rxjava2 + retrofit2的封装，常用的请求（Get,Post,文件上传，文件下载），简单便捷，支持自定义loading等属性。</br>2.RxBus的使用，用法完全与EvenBus一样。
=
效果图
------
>![image](https://github.com/buhuiming/RxLibrary/blob/master/screenShots/i.jpg)</br>

<br>

集成
-------
    compile 'com.bhm.sdk.rxlibrary:RxLibrary:2.3.0'
<br>或者

    <dependency>
      <groupId>com.bhm.sdk.rxlibrary</groupId>
      <artifactId>RxLibrary</artifactId>
      <version>2.3.0</version>
      <type>pom</type>
    </dependency>


一、rxjava2 + retrofit2的使用
-------  

<br>

二、RxBus的使用
-------  

### 1.订阅RxBus.get().register(this);
    一般在activity/fragment的oncreate方法中添加
### 2.取消订阅RxBus.get().unRegister(this);
     一般在activity/fragment的的ondestory方法添加
### 3.接收event，处理
    在activity/fragment中添加如下方法
      
    @Subscribe(code = 1111,threadMode = ThreadMode.MAIN)
    public void rxBusEvent(Entity entity){
        if(null != entity){
        Toast.makeText(this, "RxBus改变了MainActivity的标题", Toast.LENGTH_SHORT).show();      
        }
    }
       
    threadMode：MAIN，NEW_THREAD，CURRENT_THREAD（默认）
### 4.在任意一个地方发送
      RxBus.get().send(1111, entity);
