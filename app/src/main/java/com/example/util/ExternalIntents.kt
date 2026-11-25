package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.R
import com.example.data.model.OpticalOrder
import com.example.data.model.Prescription
import com.example.data.model.Product
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double): String {
  val format = NumberFormat.getCurrencyInstance(Locale.US)
  return format.format(amount)
}

fun resolveProductDrawable(imageKey: String): Int {
  return when (imageKey) {
    "img_hero_eyewear" -> R.drawable.img_hero_eyewear
    "img_frames_sunglasses" -> R.drawable.img_frames_sunglasses
    "img_eye_exam_studio" -> R.drawable.img_eye_exam_studio
    else -> R.drawable.img_frames_optical
  }
}

object WhatsAppHelper {
  fun openWhatsApp(context: Context, phoneNumber: String, prefilledMessage: String) {
    val cleanDigits = phoneNumber.filter { it.isDigit() }
    val encoded = URLEncoder.encode(prefilledMessage, "UTF-8")
    val url = if (cleanDigits.isNotEmpty()) {
      "https://wa.me/$cleanDigits?text=$encoded"
    } else {
      "https://wa.me/?text=$encoded"
    }
    openExternalUrl(context, url)
  }

  fun productEnquiryMessage(shopName: String, product: Product): String =
    "Hello $shopName! I would like to enquire about ${product.name} (SKU: ${product.sku}, ${formatCurrency(product.finalPrice)}) in ${product.frameColour}. Is it available for fitting?"

  fun orderEnquiryMessage(shopName: String, order: OpticalOrder): String =
    "Hello $shopName! Order Enquiry #${order.id.take(8).uppercase()} from ${order.customerName}.\nItems: ${order.itemsSummary}\nTotal: ${formatCurrency(order.totalAmount)}\nDelivery: ${order.deliveryAddress}"

  fun eyeTestBookingMessage(
    shopName: String,
    service: String,
    date: String,
    time: String,
    name: String,
  ): String =
    "Hello $shopName! I would like to confirm an eye test booking:\nName: $name\nService: $service\nDate: $date at $time"

  fun lensQuotationMessage(shopName: String, lensType: String): String =
    "Hello $shopName! Could you please share a lens quotation for: $lensType?"

  fun prescriptionEnquiryMessage(shopName: String, rx: Prescription): String =
    "Hello $shopName! Prescription Quotation Request for ${rx.customerName}:\n" +
      "OD (Right): SPH ${rx.rightSph.ifBlank { "0.00" }}, CYL ${rx.rightCyl.ifBlank { "0.00" }}, AXIS ${rx.rightAxis.ifBlank { "-" }}\n" +
      "OS (Left): SPH ${rx.leftSph.ifBlank { "0.00" }}, CYL ${rx.leftCyl.ifBlank { "0.00" }}, AXIS ${rx.leftAxis.ifBlank { "-" }}\n" +
      "ADD: ${rx.addPower.ifBlank { "-" }} | PD: ${rx.pd.ifBlank { "-" }}\n" +
      "Requested Lens: ${rx.lensType}"
}

fun openExternalUrl(context: Context, url: String) {
  val safeUrl = if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("geo:")) {
    url
  } else {
    "https://$url"
  }
  try {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl))
    context.startActivity(intent)
  } catch (_: ActivityNotFoundException) {
    Toast.makeText(context, "No app found to open link", Toast.LENGTH_SHORT).show()
  }
}

fun openGoogleMaps(context: Context, query: String) {
  val encoded = URLEncoder.encode(query, "UTF-8")
  openExternalUrl(context, "https://www.google.com/maps/search/?api=1&query=$encoded")
}

fun dialPhone(context: Context, phone: String) {
  openExternalUrl(context, "tel:${phone.trim()}")
}

fun sendEmail(context: Context, email: String, subject: String) {
  val encodedSubject = URLEncoder.encode(subject, "UTF-8")
  openExternalUrl(context, "mailto:${email.trim()}?subject=$encodedSubject")
}
