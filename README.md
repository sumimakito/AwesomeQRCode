# AwesomeQRCode
[![](https://jitpack.io/v/SumiMakito/AwesomeQRCode.svg)](https://jitpack.io/#SumiMakito/AwesomeQRCode)
[![license](https://img.shields.io/github/license/SumiMakito/AwesomeQRCode.svg)](https://github.com/SumiMakito/AwesomeQRCode/blob/master/LICENSE)
[![release](https://img.shields.io/github/release/SumiMakito/AwesomeQRCode.svg)](https://github.com/SumiMakito/AwesomeQRCode/releases/latest)

An awesome<del>(simple)</del> QR code generator for Android.

[AwesomeQRCode in Kotlin](https://github.com/SumiMakito/AwesomeQRCode-Kotlin)

[切换至中文（简体）版本？](README-zh_CN.md)

<img alt="Special, thus awesome." src="art/banner.png" style="max-width: 600px;">

### Yay! Demo Available! 

<a href="https://play.google.com/store/apps/details?id=com.github.sumimakito.awesomeqrsample" target="_blank"><img src="art/play_store_badge.png" alt="Google Play Store" width="200"></a>

### Examples

> Try to scan these QR codes below with your smart phone.

Example 1|Example 2|Example 3
------------ | ------------- | -------------
<img src="art/awesome-qr-1.png" width="400"> | <img src="art/awesome-qr-2.png" width="400"> | <img src="art/awesome-qr-3.png" width="400">


Solid dots instead of blocks|Binarized|With logo at the center
------------ | ------------- | -------------
<img src="art/awesome-qr-4.png" width="400"> | <img src="art/awesome-qr-5.png" width="400"> | <img src="art/awesome-qr-6.png" width="400">

### Add dependency into your project

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
        compile 'com.github.SumiMakito:AwesomeQRCode:latest'
}
```

### Quick start


#### "I just wanna get a Bitmap":

> In this case, QR code will be generated synchronously. Thus it means you may take a risk blocking the UI thread, which would lead to Application Not Responding (ANR). I strongly recommend you to use it in a non-UI thread.

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

#### Generate a QR code asynchronously and show the QR code in an ImageView:

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
     // Tip: here we use runOnUiThread(...) to avoid the problems caused by operating UI elements from a non-UI thread.
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

### Parameters

Parameter | Type | Explanation | Default Value | Misc.
:----:|:------:|----|:--:|:--:
contents | String | Contents to encode. | null | Required
size | int-px | Width as well as the height of the output QR code, includes margin. | 800 | Required
margin | int-px | Margin to add around the QR code. | 20 | Required 
dataDotScale | float | Value used to scale down the data dots' size. | 0.3f | (0, 1.0f)
colorDark | int-color | Color of "true" blocks. Works only when both colorDark and colorLight are set. | Color.BLACK | 
colorLight | int-color | Color of empty space, or "false" blocks. Works only when both colorDark and colorLight are set. | Color.WHITE |
background | Bitmap | Background image to embed in the QR code. Leave null to disable. | null | 
whiteMargin | int-px | If set to true, a white border will appear around the background image. | true | 
autoColor | boolean | If set to true, the dominant color of backgroundImage will be used as colorDark. | true | 
binarize | boolean | If set to true, the whole image will be binarized with the given threshold, or default threshold if not specified. | fasle | 
binarizeThreshold | int | Threshold used to binarize the whole image. | 128 | (0, 255)
roundedDataDots | boolean | If set to true, data dots will appear as solid dots instead of blocks. | false | 
logo | Bitmap | Logo image to embed at the center of generated QR code. Leave null to disable. | null | 
logoMargin | int-px | White margin that appears around the logo image. Leave 0 to disable. LOGO | 10 | 
logoCornerRadius | int-px | Radius of the logo's corners. Leave 0 to disable. | 8 | 
logoScale | float | Value used to scale the logo image. Larger value may result in decode failure. | 0.2f | (0, 1.0f)


### Changelog

#### Version 1.0.6
- Fixed a "divide by zero" error mentioned in [#20](https://github.com/SumiMakito/AwesomeQRCode/issues/20).

#### Version 1.0.5
- The way to use AwesomeQRCode is more elegant.

#### Version 1.0.4
- New feature: Embedding a logo image in the QR code.
- Sample/Demo application updated.

#### Version 1.0.3
- Added CHARACTER_SET => UTF-8 to QR code's hints before encoding.
- Fixed an encoding issue mentioned in [#7](https://github.com/SumiMakito/AwesomeQRCode/issues/7).

#### Version 1.0.2
- Added an optional parameter which enables the data dots to appear as filled circles.

#### Version 1.0.1
- Now background images can be binarized as you like.

#### Version 1.0.0
- Initial release.

### Alternatives

#### EFQRCode written in Swift

EFQRCode is a tool to generate QRCode image or recognize QRCode from image, in Swift.

AwesomeQRCode is inspired by [EFQRCode by EyreFree](https://github.com/EyreFree/EFQRCode).

If your application is in need of generating pretty QR codes in Swift, take a look at EFQRCode. It should help.

#### Awesome-qr.js written in JavaScript

Redirect to [Awesome-qr.js](https://github.com/SumiMakito/Awesome-qr.js)

### Would you like to buy me a cup of cappuccino?
PayPal | Alipay
----|----
[PayPal](https://www.paypal.me/makito) | [Alipay](https://qr.alipay.com/a6x02021re1jk4ftcymlw79)

### Copyright &amp; License

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
