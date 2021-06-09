package app.soumicslab.qrcodedemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.widget.ImageView
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer.Companion.render
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.color.Color
import com.github.sumimakito.awesomeqr.option.logo.Logo
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

class StartActivity : AppCompatActivity() {
  companion object {
    const val DEFAULT_QRCODE_VERSION: Int = 6
    val DEFAULT_QRCODE_ERROR_CORRECTION_LEVEL = ErrorCorrectionLevel.M
    const val DEFAULT_LOGO_SCALE_FACTOR = 0.25f  // 40% of qrCodes size
    const val DEFAULT_PATTERN_SCALE = 0.9f
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_start)

    val imageView: ImageView = findViewById(R.id.imageView)

    val input = "https://fahimfarhan.github.io/"
    val qrCodeBitmap = buildNiceQrCode(input)

    imageView.setImageBitmap(qrCodeBitmap)

  }

  private fun buildNiceQrCode(link: String): Bitmap? {
    val color = Color()
    color.light = -0x1
    color.dark = -0x1000000
    color.background = -0x1
    color.auto = false

    color.topLeftColor = ResourcesCompat.getColor(this.resources, R.color.top_left, null)
    color.topRigntColor = ResourcesCompat.getColor(this.resources, R.color.top_right, null)
    color.bottomLeftColor = ResourcesCompat.getColor(this.resources, R.color.bottom_left, null)

    val renderOption = RenderOption()
    renderOption.content = link // = link // content to encode
    renderOption.size = 800 //size = 800 // size of the final QR code image
    renderOption.borderWidth = 30 //borderWidth = 0 // width of the empty space around the QR code
    renderOption.ecl = DEFAULT_QRCODE_ERROR_CORRECTION_LEVEL //ErrorCorrectionLevel.M // (optional) specify an error correction level
    renderOption.patternScale = DEFAULT_PATTERN_SCALE //patternScale = 0.6f // (optional) specify a scale for patterns
    renderOption.roundedPatterns = true //roundedPatterns = true // (optional) if true, blocks will be drawn as dots instead
    renderOption.clearBorder = false //clearBorder = true // if set to true, the background will NOT be drawn on the border area
    renderOption.color = color //color = color // set a color palette for the QR code

    renderOption.isCustomPositions = true
    renderOption.qrCodeVersion = DEFAULT_QRCODE_VERSION
    // logo Java
    val logoBitmap = BitmapFactory.decodeResource(resources, R.drawable.rocket) // Image by <a href="https://pixabay.com/users/adjieargoputra-3378520/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=image&amp;utm_content=1693791">aji argo putro</a> from <a href="https://pixabay.com/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=image&amp;utm_content=1693791">Pixabay</a>
    val logo = Logo()
    logo.bitmap = logoBitmap
    // logo.setBorderRadius(20); // radius for logo's corners
    // logo.setBorderWidth(10); // width of the border to be added around the logo
    logo.scale = DEFAULT_LOGO_SCALE_FACTOR // scale for the logo in the QR code
    // logo.setClippingRect(new RectF(0, 0, 200, 200)); // crop the logo image before applying it to the QR code
    renderOption.logo = logo
    try {
      val result = render(renderOption)
      if (result.bitmap != null) {
        return result.bitmap
      }
    } catch (x: Exception) {
      x.printStackTrace()
    }
    return null
  }
}