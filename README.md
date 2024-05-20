# Krigacy

> Krigacy = Kritor Legacy  
> Kritor 原本声明[并不是为了取代 Onebot](https://github.com/KarinJS/kritor/commit/1ee33acec68922e58c9a4329b64de845ca390d32#diff-b335630551682c19a781afebcf4d07bf978fb1f8ac04c6bf87428ed5106870f5L23)，却在做着类似的事。

> OpenShamrock 归档了，Lagrange 依然支持 Onebot，Kritor 作用不显著，本项目可能不会继续开发。

Krigacy 是 [Kritor](https://github.com/KarinJS/kritor) 的 Onebot 11 适配器，用于将 Kritor 标准转换为 [Onebot](https://11.onebot.dev)/[go-cqhttp](https://docs.go-cqhttp.org/api) 标准

本项目的定位为 Kritor 应用端，Onebot 协议端。

# TODO

+ [x] 支持主动 WebSocket 连接 *实验性*
+ [ ] 支持被动 WebSocket 连接

# 构建

拉取仓库或更新 Kritor 后，需要执行一次
```shell
./gradlew generateProto
```

正在编写中。

需要编译 standalone (独立版)，请执行
```shell
./gradlew shadowDistZip
```
编译产物在 `./standalone/build/distributions/`
