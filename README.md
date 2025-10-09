# KeepInventory 保留库存

[![CodeFactor](https://www.codefactor.io/repository/github/earlydreamland/keepinventory/badge)](https://www.codefactor.io/repository/github/earlydreamland/keepinventory)
![CodeSize](https://img.shields.io/github/languages/code-size/EarlyDreamLand/KeepInventory)
[![Java CI with Build](https://github.com/EarlyDreamLand/KeepInventory/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/EarlyDreamLand/KeepInventory/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/EarlyDreamLand/KeepInventory?&logo=github)](https://github.com/EarlyDreamLand/KeepInventory/blob/main/LICENSE)
![Support](https://img.shields.io/badge/Minecraft-Java%201.13--Latest-green)

本插件基于Spigot实现，**理论上支持Java1.13版本至最新版本**。

本插件可以帮助你在每一次服务器重启/世界重置后都能够设置玩家死亡后是否保留库存。

## 使用统计

[![bStats](https://bstats.org/signatures/bukkit/EnableKeepInventory.svg)](https://bstats.org/plugin/bukkit/EnableKeepInventory/26836)

## 功能实现

- [ ] 支持更新配置文件
- [x] 支持为不同世界启用/禁用保留库存
- [x] 支持使用命令启用/禁用世界保留库存
- [ ] Multiverse-Core插件兼容性

## 使用命令

| 命令 | 功能描述 |
| ------ | ------ |
| /kip | 查看插件帮助信息 |
| /kip reload | 重新加载配置文件 |
| `/kip add <world>` | 启用世界保留库存 |
| `/kip del <world>` | 禁用世界保留库存 |

## 支持

Many thanks to Jetbrains for kindly providing a license, as well as to DeepSeek for their code assistance, both of which have been crucial for my work on this and other open-source projects.

[![](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://www.jetbrains.com/?from=https://github.com/EarlyDreamLand/KeepInventory)

## 开源协议

本项目源码采用 [MIT License](https://opensource.org/license/mit) 开源协议。

<details>
  <summary>关于 MIT 协议</summary>

>
> MIT 协议可能是几大开源协议中最宽松的一个，核心条款是：
>
> 该软件及其相关文档对所有人免费，可以任意处置，包括使用，复制，修改，合并，发表，分发，再授权，或者销售。唯一的限制是，软件中必须包含上述版权和许可提示。
>
> 这意味着：
>
> #### 你可以自由使用，复制，修改，可以用于自己的项目。
>
> #### 可以免费分发或用来盈利。
>
> #### 唯一的限制是必须包含许可声明。
>
> MIT 协议是所有开源许可中最宽松的一个，除了必须包含许可声明外，再无任何限制。
>
> *以上文字来自 [五种开源协议 (GPL,LGPL,BSD,MIT,Apache) 比较](https://segmentfault.com/a/1190000007798003) 。*
</details>