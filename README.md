# AwesomeQRCode

An awesome QR code generator.

一个优雅的 QR 二维码生成器

### Examples 样例

> Try to scan these QR codes below with your smart phone.

Example 1|Example 2|Example 3
------------ | ------------- | -------------
<img src="art/awesome-qr-1.png" width="400"> | <img src="art/awesome-qr-2.png" width="400"> | <img src="art/awesome-qr-3.png" width="400">

### Quick start 快速上手

```java
Bitmap qrCode = AwesomeQRCode.create("Makito loves Kafuu Chino.", 800, 20, 0.3f, Color.BLACK, Color.WHITE, backgroundBitmap, true, true);
```

### Parameters 参数

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
        boolean autoColor       // If true, colorDark will be set to the dominant color of backgroundImage. Default is true. 若为 true, 则将从背景图取主要颜色作为实点颜色
) throws IllegalArgumentException { ... }
```

### Would you like to buy me a cup of cappuccino? 要请我喝一杯卡布奇诺吗？
PayPal | Alipay
----|----
[PayPal](https://www.paypal.me/makito) | [Alipay](https://qr.alipay.com/a6x02021re1jk4ftcymlw79)


### Copyright &amp; License 版权信息与授权协议

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
