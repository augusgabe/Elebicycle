# Elebicycle
这是参加物联网竞赛时针对模拟试题写的一个练手项目，运用到了socket技术，在同一局域网内与单片机设备进行数据帧的收发，当然需要通过一个中间服务器。写的比较简陋，也有好多不太规范的代码，鉴于没真正去外边学习过真正规范的写法，应该还是可以稍微原谅些吧。主体是一个MainActivity，我想的是一个Activity搭上几个Fragment，构成一个大体框架，各个Fragment就用来处理一些显示和交互的功能，MainActivity的话就用来在底层保持与socket的连接，因为我们一旦通过socket成功连接上了，我们需要能够一直保持住这个socket，然后还能通过这个socket进行数据帧的收发，而我们也知道安卓里边MainActivity是一直保持生存周期的，除非退出应用否则不会销毁，所以放在这里边去写socket的连接，我自己觉得是最合适不过了。当然了，因为后续的一些需求，我们可以把连接成功的socket保存到全局变量中，也就是放到Viewmodel中去。

三个主Fragmen界面展示：

![image](https://user-images.githubusercontent.com/75819020/228221208-3c43b276-b814-48e9-9611-0c3a22556f79.png)

![image](https://user-images.githubusercontent.com/75819020/228221305-166be21c-1978-4bac-bec0-b3ec5aea30b6.png)


![image](https://user-images.githubusercontent.com/75819020/228221391-892641f3-337e-4500-8c37-62efaf210fde.png)


弹窗界面：
![image](https://user-images.githubusercontent.com/75819020/228221638-f1d3e9b8-2443-4d9f-8e9f-ed69026b0b24.png)

登陆界面：
![image](https://user-images.githubusercontent.com/75819020/228221743-a1d000c8-1fe4-4c97-ac84-e3e32eac7c1f.png)

注册界面：
![image](https://user-images.githubusercontent.com/75819020/228221854-8b59c873-5eaa-452d-8ae9-eb7cd41645f3.png)
