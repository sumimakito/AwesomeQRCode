# AwesomeQRCode
[![](https://jitpack.io/v/SumiMakito/AwesomeQRCode.svg)](https://jitpack.io/#SumiMakito/AwesomeQRCode)
[![license](https://img.shields.io/github/license/SumiMakito/AwesomeQRCode.svg)](https://github.com/SumiMakito/AwesomeQRCode/blob/master/LICENSE)
[![release](https://img.shields.io/github/release/SumiMakito/AwesomeQRCode.svg)](https://github.com/SumiMakito/AwesomeQRCode/releases/latest)

一个优雅的<del>（不起眼的）</del> QR 二维码生成器

[Swithc to English Version?](README.md)

<img alt="Special, thus awesome." src="art/banner.png" style="max-width: 600px;">

### 好耶! 演示应用!

<a href="https://play.google.com/store/apps/details?id=com.github.sumimakito.awesomeqrsample" target="_blank"><img src="art/play_store_badge.png" alt="Google Play Store" width="200"></a>

### 样例

> 拿起你的手机扫描下面的二维码试试吧!

样例 1 | 样例 2 | 样例 3
------------ | ------------- | -------------
<img src="art/awesome-qr-1.png" width="400"> | <img src="art/awesome-qr-2.png" width="400"> | <img src="art/awesome-qr-3.png" width="400">


使用圆点做数据点 | 二值化处理 | 带有 Logo
------------ | ------------- | -------------
<img src="art/awesome-qr-4.png" width="400"> | <img src="art/awesome-qr-5.png" width="400"> | <img src="art/awesome-qr-6.png" width="400">

### 添加依赖项

> <del>万事开头难, 补全就好啦!</del>

在项目根目录下的 build.gradle 中补充以下内容:
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

在应用模块层级下的 build.gradle 中补充以下内容:
```
dependencies {
        compile 'com.github.SumiMakito:AwesomeQRCode:1.0.5'
}
```

### 快速上手

#### "人家只想要 Bitmap 嘛":

> <del>原来乃只想要 Bitmap 撒... 满足你!!</del>


> 这种情况下，二维码将同步（synchronously）生成，这有可能阻塞 UI 线程，引起应用无响应（ANR）问题。因此建议在非 UI 线程中使用。

```java
new Thread() {
  @Override
  public void run() {
   super.run();
   Bitmap qrCode = new AwesomeQRCode.Renderer()
    .contents("Makito loves Kafuu Chino.")
    .size(800).margin(20)
    .render();
  }.start();
```

#### 异步生成二维码并在 ImageView 中显示:

```java
new AwesomeQRCode.Renderer()
 .contents("Makito loves Kafuu Chino.")
 .size(800).margin(20)
 .renderAsync(new AwesomeQRCode.Callback() {
  @Override
  public void onRendered(AwesomeQRCode.Renderer renderer, final Bitmap bitmap) {
   runOnUiThread(new Runnable() {
    @Override
    public void run() {
     // 提示: 这里使用 runOnUiThread(...) 来规避从非 UI 线程操作 UI 控件时产生的问题。
     imageView.setImageBitmap(bitmap);
    }
   });
  }

  @Override
  public void onError(AwesomeQRCode.Renderer renderer, Exception e) {
   e.printStackTrace();
  }
 });
```

### 参数说明

参数名 | 类型 | 说明 | 默认值 | 备注
:----:|:------:|----|:--:|:-----:
contents | String | 欲编码的内容 | null | 必需
size | int-px | 尺寸, 长宽一致, 包含外边距 | 800 | 必需
margin | int-px | 二维码图像的外边距 | 20 | 必需 
dataDotScale | float | 数据点缩小比例 | 0.3f | (0, 1.0f) 
colorDark | int-color | 非空白区域的颜色 | Color.BLACK | 
colorLight | int-color | 空白区域的颜色 | Color.WHITE |
background | Bitmap | 欲嵌入的背景图, 设为 null 以禁用 | null | 
whiteMargin | int-px | 若设为 true, 背景图外将绘制白色边框 | true | 
autoColor | boolean | 若为 true, 背景图的主要颜色将作为实点的颜色, 即 colorDark | true | 
binarize | boolean | 若为 true, 图像将被二值化处理, 未指定阈值则使用默认值 | fasle | 
binarizeThreshold | int | 二值化处理的阈值 | 128 | (0, 255)
roundedDataDots | boolean | 若为 true, 数据点将以圆点绘制 | false | 
logo | Bitmap | 欲嵌入至二维码中心的 Logo, 设为 null 以禁用 | null | 
logoMargin | int-px | Logo 周围的空白边框, 设为 0 以禁用 | 10 | 
logoCornerRadius | int-px | Logo 及其边框的圆角半径, 设为 0 以禁用 | 8 | 
logoScale | float | 用于计算 Logo 大小, 过大将覆盖过多数据点而导致解码失败 | 0.2f | (0, 1.0f)



### 更新日志

#### 1.0.5 版本
- 使用 AwesomeQRCode 的方式变的更优雅

#### 1.0.4 版本
- 可以在二维码中选择嵌入 Logo
- 演示应用更新

#### 1.0.3 版本
- 在二维码中的 Hints 中加入 CHARACTER_SET => UTF-8 
- 修复 [#7](https://github.com/SumiMakito/AwesomeQRCode/issues/7) 中提到的编码问题

#### 1.0.2 版本
- 加入使用圆点绘制二维码数据点的选项

#### 1.0.1 版本
- 加入背景二值化的支持

#### 1.0.0 版本
- 初次发布

### 相关项目

#### Swift 下的 EFQRCode 

AwesomeQRCode 受 [由 EyreFree 创造的 EFQRCode](https://github.com/EyreFree/EFQRCode) 所启发而生，它是一个轻量级的、用来生成和识别二维码的纯 Swift 库，可根据输入的水印图和图标产生艺术二维码，基于 CoreImage 进行开发。受 qrcode 启发。EFQRCode 为你提供了一种更好的在你的 App 中操作二维码的方式。


#### 可在网页使用的 JavaScript 版: Awesome-qr.js

详情请至 [Awesome-qr.js](https://github.com/SumiMakito/Awesome-qr.js)

### 可以请我喝一杯卡布奇诺吗？
PayPal | 支付宝
----|----
[PayPal](https://www.paypal.me/makito) | [支付宝](https://qr.alipay.com/a6x02021re1jk4ftcymlw79)

### 版权信息与授权协议

Copyright &copy; 2017 Sumi Makito

Licensed under Apache License 2.0 License.

```
Copyright 2017 Sumi Makito

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
