package app.soumicslab.qrcodedemo

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build

import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan


import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer.Companion.render
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.color.Color
import com.github.sumimakito.awesomeqr.option.logo.Logo
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.concurrent.Executors

class StartActivity : AppCompatActivity() {
  companion object {
    val TAG: String = StartActivity::class.java.simpleName

    const val DEFAULT_QRCODE_VERSION: Int = 6
    val DEFAULT_QRCODE_ERROR_CORRECTION_LEVEL = ErrorCorrectionLevel.M
    const val DEFAULT_LOGO_SCALE_FACTOR = 0.25f  // 40% of qrCodes size
    const val DEFAULT_PATTERN_SCALE = 0.9f
  }

  private var downloadedFileUri: Uri = Uri.EMPTY

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_start)

    val imageView: ImageView = findViewById(R.id.imageView)

    val input = "https://fahimfarhan.github.io/"
    val qrCodeBitmap = buildNiceQrCode(input)

    imageView.setImageBitmap(qrCodeBitmap)

    val downloadButton: TextView = findViewById(R.id.download)

    downloadButton.setOnClickListener {
      Dexter.withContext(this)
        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .withListener(object : PermissionListener {
          override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
            onDownloadQrCode("myAwesomeQrCode", qrCodeBitmap = qrCodeBitmap!!) {
              val openGallery =
                View.OnClickListener {
                  val intent = Intent(Intent.ACTION_VIEW)
                  intent.setTypeAndNormalize("image/jpeg")
                  intent.data = downloadedFileUri
                  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    myGrantUriPermission(intent, downloadedFileUri)
                  }
                  startActivity(intent)
                }
              runOnUiThread {
                val rootView: View = findViewById(R.id.root)
                Snackbar.make(
                  rootView,
                  "open the qrCode",
                  Snackbar.LENGTH_INDEFINITE
                ).setAction("View", openGallery).show()
              }
            }
          }

          override fun onPermissionDenied(p0: PermissionDeniedResponse?) {}

          override fun onPermissionRationaleShouldBeShown(
            p0: PermissionRequest?,
            permissionToken: PermissionToken?
          ) {
            permissionToken?.continuePermissionRequest()  // adding this line shows permission popup from 2nd time onwards if the permission was denied
          }
        })
        .check()

    }


    //--------------
    val credit: TextView = findViewById(R.id.credit)
    // Image by <a href="https://pixabay.com/users/adjieargoputra-3378520/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=image&amp;utm_content=1693791">aji argo putro</a> from <a href="https://pixabay.com/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=image&amp;utm_content=1693791">Pixabay</a>
    credit.apply {

      val author = "aji argo putro"
      val source = "Pixabay"

      val message = SpannableString("Logo credit thanks to $author from $source").apply {
        setLinkSpan(author, "https://pixabay.com/users/adjieargoputra-3378520/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=image&amp;utm_content=1693791")
        setLinkSpan(source,"https://pixabay.com/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=image&amp;utm_content=1693791")
      }
      text = message
    }


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

  private fun onDownloadQrCode(qrCodeInput1: String, qrCodeBitmap: Bitmap, callback: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      downloadQrCodeForApiGte10(qrCodeInput1, qrCodeBitmap, callback)
    }else {
      downloadQrCodeForApiLt10(qrCodeInput1, qrCodeBitmap, callback)
    }
  }

  /**
   * @brief: Special thanks to [Rayenderlich scoped storage tutorial](https://www.raywenderlich.com/10217168-preparing-for-scoped-storage#toc-anchor-010)
   */
  @RequiresApi(api = Build.VERSION_CODES.Q)
  private fun downloadQrCodeForApiGte10(qrCodeInput1: String, qrBitmap: Bitmap, callback: () -> Unit) {
    Executors.newSingleThreadExecutor().execute {
      val name = "ridmik-qrcode-for-${qrCodeInput1}-${System.currentTimeMillis()}.jpeg"
      // todo: do it inside background thread
      val collection =
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) // this is how you make it available to external/all apps
      val dirDest = File(
        Environment.DIRECTORY_PICTURES, getString(R.string.app_name)
      )
      val date = System.currentTimeMillis()
      val newImageContentValues = ContentValues()
      newImageContentValues.put(
        MediaStore.Images.Media.DISPLAY_NAME,
        name
      ) // you need to fill these up so that they are available during query
      newImageContentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
      newImageContentValues.put(MediaStore.MediaColumns.DATE_ADDED, date)
      newImageContentValues.put(MediaStore.MediaColumns.DATE_MODIFIED, date)
      newImageContentValues.put(MediaStore.MediaColumns.SIZE, qrBitmap.byteCount)
      newImageContentValues.put(MediaStore.MediaColumns.WIDTH, qrBitmap.width)
      newImageContentValues.put(MediaStore.MediaColumns.HEIGHT, qrBitmap.height)
      //4
      val relativePath =
        dirDest.toString() + File.separator // save the picture in Pictures/Ridmik
      newImageContentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
      //5
      newImageContentValues.put(
        MediaStore.Images.Media.IS_PENDING,
        1
      ) // ispending = 1 hides the image from other apps
      val newImageUri = contentResolver.insert(
        collection,
        newImageContentValues
      ) // Media store creates a file based on these values, and returns an uri of the newempty file to us
      this.downloadedFileUri = newImageUri!!
      var outputStream: OutputStream? = null
      try {
        outputStream = contentResolver.openOutputStream(newImageUri, "w")
      } catch (e: FileNotFoundException) {
        e.printStackTrace()
      }
      qrBitmap.compress(
        Bitmap.CompressFormat.JPEG,
        100,
        outputStream
      ) // save image inside the file
      newImageContentValues.clear() // self explanatory
      //7
      newImageContentValues.put(
        MediaStore.Images.Media.IS_PENDING,
        0
      ) // unhides this image file
      //8
      contentResolver
        .update(newImageUri, newImageContentValues, null, null)

      callback.invoke()
    }
  }

  private fun downloadQrCodeForApiLt10(qrCodeInput1: String, qrCodeBitmap: Bitmap, callback: () -> Unit) {
    val name = "ridmik-qrcode-for-${qrCodeInput1}-${System.currentTimeMillis()}.jpeg"

    val photoFile = saveBitmapLegacy(qrCodeBitmap, name)

    val authority = packageName + ".fileprovider" // we get this from AndroidManifest.xml file

    this.downloadedFileUri = FileProvider.getUriForFile(this, authority, photoFile) // Uri.fromFile(photoFile);  // eta shudhu app er internal kajer jonno uri. baire share korte hole contentProvider / FileProvider use korte hobe

    Log.e(TAG, "uri.getPath() -> "+ downloadedFileUri.path)
    Log.e(TAG,  "uri -> $downloadedFileUri")

    callback.invoke()
  }

  private fun saveBitmapLegacy(qrCodeBitmap: Bitmap, nameJpeg: String): File {
    val picturesRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val destFolder = File(picturesRoot, getString(R.string.app_name))
    if(!destFolder.exists()) {
      destFolder.mkdirs()
    }
    val destFile = File(destFolder, nameJpeg)
    val fos = FileOutputStream(destFile)
    qrCodeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
    fos.close()
    return destFile
  }

  private fun myGrantUriPermission(inputIntent: Intent, inputUri: Uri) {
    val resolveInfoArrayList = ArrayList(
      packageManager.queryIntentActivities(
          inputIntent,
          PackageManager.MATCH_DEFAULT_ONLY
        )
    )
    for (resolveInfo in resolveInfoArrayList) {
      val packageName = resolveInfo.activityInfo.packageName
      grantUriPermission(
        packageName, inputUri,
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
      )
    }
  }

  private fun SpannableString.setLinkSpan(text: String, url: String) {
    val textIndex = this.indexOf(text)
    setSpan(
      object : ClickableSpan() {
        override fun onClick(widget: View) {
          Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }.also { startActivity(it) }
        }
      },
      textIndex,
      textIndex + text.length,
      Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
  }

}