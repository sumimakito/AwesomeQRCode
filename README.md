# AwesomeQRCode [![](https://jitpack.io/v/SumiMakito/AwesomeQRCode.svg)](https://jitpack.io/#SumiMakito/AwesomeQRCode)

An awesome<del>(simple)</del> QR code generator for Android.

一个优雅的<del>(不起眼的)</del> QR 二维码生成器

### Get sample APK, 下载演示 APK

<a href="https://play.google.com/store/apps/details?id=com.github.sumimakito.awesomeqrsample" target="_blank"><img src="art/play_store_badge.png" alt="Google Play Store" width="200"></a>

### Examples, 样例

> Try to scan these QR codes below with your smart phone.

Example 1|Example 2|Example 3
------------ | ------------- | -------------
<img src="art/awesome-qr-1.png" width="400"> | <img src="art/awesome-qr-2.png" width="400"> | <img src="art/awesome-qr-3.png" width="400">

### Add dependency, 添加依赖项

Add below lines in build.gradle of your project:
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Then, add below lines in build.gradle of your app module:
```
dependencies {
        compile 'com.github.SumiMakito:AwesomeQRCode:1.0.0'
}
```

### Quick start, 快速上手

```java
Bitmap qrCode = AwesomeQRCode.create("Makito loves Kafuu Chino.", 800, 20, 0.3f, Color.BLACK, Color.WHITE, backgroundBitmap, true, true);
```

### Parameters, 参数

```java
public static Bitmap create(
        String contents,        // Contents to encode. 欲编码的内容
        int size,               // Width as well as the height of the output QR code, includes margin. 尺寸, 长宽一致
        int margin,             // Margin to add around the QR code. 二维码边缘的外边距
        float dataDotScale,     // Scale the data blocks and makes them appear smaller. 数据点缩小比例 (0 < scale < 1.0f)
        int colorDark,          // Color of blocks. Will be OVERRIDE by autoColor. (BYTE_DTA, BYTE_POS, BYTE_AGN, BYTE_TMG) 实点的颜色
        int colorLight,         // Color of empty space. Will be OVERRIDE by autoColor. (BYTE_EPT) 空白点的颜色
        Bitmap backgroundImage, // The background image to embed in the QR code. If null, no background image will be embedded. 欲嵌入的背景图
        boolean whiteMargin,    // If true, background image will not be drawn on the margin area. Default is true. 若为 true, 则背景图将不会绘制到外边距区域
        boolean autoColor,      // If true, colorDark will be set to the dominant color of backgroundImage. Default is true. 若为 true, 则将从背景图取主要颜色作为实点颜色
        boolean binarize,       // If true, background images will be binarized. Default is false. 若为 true, 背景图像将被二值化处理
        int binarizeThreshold   // Threshold value used while binarizing background images. Default is 128. 0 < threshold < 255. 控制背景图像二值化的阈值
) throws IllegalArgumentException { ... }
```

### Changelog, 更新日志

#### 1.0.1
Now background images can be binarized as you like.

#### 1.0.0
Initial release.

### Alternatives on other platforms/in other languages. 其他平台或语言下的对等项目

#### EFQRCode written in Swift

EFQRCode is a tool to generate QRCode image or recognize QRCode from image, in Swift.

AwesomeQRCode is inspired by [EFQRCode by EyreFree](https://github.com/EyreFree/EFQRCode).

If your application is in need of generating pretty QR codes in Swift, take a look at EFQRCode. It should help.

#### Awesome-qr.js written in JavaScript, 支持 JavaScript 的 Awesome-qr.js

Redirect to [Awesome-qr.js](https://github.com/SumiMakito/Awesome-qr.js)

### Would you like to buy me a cup of cappuccino? 要请我喝一杯卡布奇诺吗？
PayPal | Alipay
----|----
[PayPal](https://www.paypal.me/makito) | [Alipay](https://qr.alipay.com/a6x02021re1jk4ftcymlw79)

### Copyright &amp; License, 版权信息与授权协议

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
