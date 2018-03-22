<img alt="Special, thus awesome." src="art/banner.png" style="max-width: 600px;">

[![](https://jitpack.io/v/SumiMakito/AwesomeQRCode.svg)](https://jitpack.io/#SumiMakito/AwesomeQRCode)
[![release](https://img.shields.io/github/release/SumiMakito/AwesomeQRCode.svg)](https://github.com/SumiMakito/AwesomeQRCode/releases/latest)
[![license](https://img.shields.io/github/license/SumiMakito/AwesomeQRCode.svg)](https://github.com/SumiMakito/AwesomeQRCode/blob/master/LICENSE)
![](https://img.shields.io/badge/made%20with-%3C3-orange.svg)

AwesomeQRCode - An awesome<del>(simple)</del> QR code generator for Android.

> [切换至中文（简体）版本？](README-zh_CN.md)

## Yay! Available on Google Play!

<a href="https://play.google.com/store/apps/details?id=com.github.sumimakito.awesomeqrsample" target="_blank"><img src="art/play_store_badge.png" alt="Google Play Store" width="200"></a>

## Examples

> Try to scan these QR codes below with your smart phone.

Example 1|Example 2|Example 3
------------ | ------------- | -------------
<img src="art/awesome-qr-1.png" width="400"> | <img src="art/awesome-qr-2.png" width="400"> | <img src="art/awesome-qr-3.png" width="400">

Solid dots instead of blocks|Binarized|With logo at the center
------------ | ------------- | -------------
<img src="art/awesome-qr-4.png" width="400"> | <img src="art/awesome-qr-5.png" width="400"> | <img src="art/awesome-qr-6.png" width="400">

## Installation

To add dependency into your project, Add below lines in build.gradle of your project:

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
        compile 'com.github.SumiMakito:AwesomeQRCode:1.0.6'
}
```

## Quick Start

### 1. "I just wanna get a Bitmap"

In this case, QR code will be generated synchronously. Thus it means you may take a risk blocking the UI thread, which would lead to Application Not Responding (ANR). I strongly recommend you to use it in a non-UI thread.

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

### 2. Generate a QR code asynchronously and show the QR code in an ImageView

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

## Render flow

```
Bitmap bitmap = new AwesomeQRCode.Renderer() ...
```

### Basic

#### .contents(String)

*Required*<br>
Contents to encode.<br>
Default: *null*

#### .size(int)

*Required*<br>
Width as well as the height of the output QR code, includes margin.<br>
Unit: *px*<br>
Default: *800*

#### .margin(int)

Space to add around the QR code.<br>
Unit: *px*<br>
Default: *20*

#### .dataDotScale(float)

Value used to scale down the data pattern's size.<br>
Default: *0.3f*

#### .roundedDataDots(boolean)

If set to true, data patterns will appear as solid dots instead of small rects.<br>
Default: *false*

#### .whiteMargin(boolean)

If set to true, the whole QR code will be drawn inside the white border.<br>
Default: *true*

### Post-processing

#### .binarize(boolean)

If set to true, the whole image will be binarized with the given or default threshold.<br>
Default: *false*

#### .binarizeThreshold(int)

Threshold used to binarize the whole image.<br>
Default: *128*

### Color

#### .autoColor(boolean)

If set to true, the dominant color of backgroundImage or every frame of backgroundGif will be used as colorDark.<br>
Default: *true*

#### .colorDark(int)

Color of "true" blocks in data patterns. Works only when both colorDark and colorLight are set.<br>
Default: *Color.BLACK*

#### .colorLight(int)

Color of empty space, or "false" blocks in data patterns. Works only when both colorDark and colorLight are set.<br>
Default: *Color.WHITE*

### Background

#### .background(Bitmap)

Background image to embed in the QR code. Leave null to disable.<br>
Default: *null*

#### .backgroundGif(File)

*Must be a GIF image. Otherwise an error will be thrown.*<br>
Background GIF to embed in the QR code. Leave null to disable.<br>
Default: *null*

#### .backgroundGifCropRect(RectF)

RectF to crop the background GIF before processing.<br>
Default: *null*

#### .saveTo(File)

*Required when backgroundGif is set.*<br>
Output file for the final GIF QR code.<br>
Default: *null*

### Logo

#### .logo(Bitmap)

Logo image to embed at the center of the QR code. Leave null to disable.<br>
Default: *null*

#### .logoMargin(int)

White margin that appears around the logo image. Leave 0 to disable.<br>
Unit: *px*<br>
Default: *10*

#### .logoCornerRadius(int)

Radius of the logo's corners. Leave 0 to disable.<br>
Unit: *px*<br>
Default: *8*

#### .logoScale(float)

Value used to scale the logo image. Larger value may lead to difficulties while decoding.<br>
Default: *0.2f*

```
... .render(); // Render and get the Bitmap!
```

## Changelog

#### Version 1.1.1

- Fixed a bug that would previously cause the gaps between blocks in position/alignment patterns.

#### Version 1.1.0

+ Added the support for GIF backgrounds.
+ Fixed some issues found in the previous version.

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

## Alternatives

### EFQRCode written in Swift

EFQRCode is a tool to generate QRCode image or recognize QRCode from image, in Swift.

AwesomeQRCode is inspired by [EFQRCode by EyreFree](https://github.com/EyreFree/EFQRCode).

If your application is in need of generating pretty QR codes in Swift, take a look at EFQRCode. It should help.

### Awesome-qr.js written in JavaScript

Redirect to [Awesome-qr.js](https://github.com/SumiMakito/Awesome-qr.js)

### <del>AwesomeQRCode-Kotlin written in Kotlin</del>

Deprecated. Not recommended.

Redirect to [AwesomeQRCode-Kotlin](https://github.com/SumiMakito/AwesomeQRCode-Kotlin)

## Donation

Would you like to buy me a cup of cappuccino?

PayPal | Alipay
----|----
[PayPal](https://www.paypal.me/makito) | [Alipay](https://qr.alipay.com/a6x02021re1jk4ftcymlw79)

## Copyright &amp; License

<img alt="Apache-2.0 license" src="https://lucene.apache.org/images/mantle-power.png" width="128">

AwesomeQRCode is available under the Apache-2.0 license. See the LICENSE file for more info.   
Copyright &copy; 2017-2018 Makito.

## Exclusive Distributor Agreement

By including, importing, modifying, redistributing, or using this library, you acknowledge and agree that you have read and accept the terms of this Exclusive Distributor Agreement.

**WHILE REDISTRIBUTING THIS LIBRARY, THIS AGREEMENT SHALL ALSO BE ATTACHED WITH THE APACHE-2.0 LICENSE.**

You're **free** to:

- Use AwesomeQRCode in your projects (commercial projects are okay as well).

+ Modify the code according to your needs.
+ Redistribute the modified code under the Exclusive Distributor Agreement and the Apache-2.0 license.

You're **forbidden** to:

+ Make AwesomeQRCode the **main** or the **only** feature of your applications.
+ Regard the **whole or part** of AwesomeQRCode as a paid function. 
+ Make a demo or sample application for AwesomeQRCode and submit the application to the store (IBNLT Google Play Store).