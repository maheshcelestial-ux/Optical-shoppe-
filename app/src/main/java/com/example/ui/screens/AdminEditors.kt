package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.OPTICAL_CATEGORIES
import com.example.data.model.Product
import com.example.data.model.Testimonial
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureIvory

private val BUNDLED_IMAGE_KEYS = listOf(
  "img_frames_optical" to "Optical Frames",
  "img_frames_sunglasses" to "Sunglasses",
  "img_hero_eyewear" to "Campaign Hero",
  "img_eye_exam_studio" to "Boutique Studio",
)

@Composable
fun ProductEditorDialog(
  initialProduct: Product?,
  onDismiss: () -> Unit,
  onSave: (Product) -> Unit,
) {
  var name by remember { mutableStateOf(initialProduct?.name ?: "") }
  var brand by remember { mutableStateOf(initialProduct?.brand ?: "Atelier Signature") }
  var sku by remember { mutableStateOf(initialProduct?.sku ?: "AO-${(100..999).random()}") }
  var category by remember { mutableStateOf(initialProduct?.category?.ifBlank { OPTICAL_CATEGORIES.first() } ?: OPTICAL_CATEGORIES.first()) }
  var priceText by remember { mutableStateOf(initialProduct?.price?.toString() ?: "240.0") }
  var discountText by remember { mutableStateOf(initialProduct?.discountPercent?.toString() ?: "0") }
  var stockText by remember { mutableStateOf(initialProduct?.stockCount?.toString() ?: "10") }
  var description by remember { mutableStateOf(initialProduct?.description ?: "") }
  var material by remember { mutableStateOf(initialProduct?.frameMaterial ?: "Italian Acetate") }
  var colour by remember { mutableStateOf(initialProduct?.frameColour ?: "Matte Obsidian") }
  var shape by remember { mutableStateOf(initialProduct?.frameShape ?: "Round") }
  var size by remember { mutableStateOf(initialProduct?.frameSize ?: "50-20-145") }
  var lensType by remember { mutableStateOf(initialProduct?.lensType ?: "Single Vision / Progressive Ready") }
  var imageKey by remember { mutableStateOf(initialProduct?.imageKey ?: "img_frames_optical") }
  var imageUri by remember { mutableStateOf(initialProduct?.imageUri ?: "") }
  var featured by remember { mutableStateOf(initialProduct?.featured ?: true) }
  var bestSeller by remember { mutableStateOf(initialProduct?.bestSeller ?: false) }
  var newArrival by remember { mutableStateOf(initialProduct?.newArrival ?: true) }
  var specialOffer by remember { mutableStateOf(initialProduct?.specialOffer ?: false) }

  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
    onResult = { uri ->
      if (uri != null) {
        imageUri = uri.toString()
      }
    },
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = AlabasterWhite,
    title = {
      Text(
        text = if (initialProduct == null || initialProduct.id.startsWith("starter_")) "Add Eyewear Product" else "Edit Product",
        style = MaterialTheme.typography.headlineMedium,
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand *") }, singleLine = true, modifier = Modifier.weight(1f))
          OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU *") }, singleLine = true, modifier = Modifier.weight(1f))
        }

        Text("Category", style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OPTICAL_CATEGORIES.forEach { cat ->
            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = priceText,
            onValueChange = { priceText = it },
            label = { Text("Price ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = discountText,
            onValueChange = { discountText = it },
            label = { Text("Discount %") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = stockText,
            onValueChange = { stockText = it },
            label = { Text("Stock") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }

        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description *") }, minLines = 2, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = material, onValueChange = { material = it }, label = { Text("Material") }, singleLine = true, modifier = Modifier.weight(1f))
          OutlinedTextField(value = colour, onValueChange = { colour = it }, label = { Text("Colour") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = shape, onValueChange = { shape = it }, label = { Text("Shape") }, singleLine = true, modifier = Modifier.weight(1f))
          OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Size") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = lensType, onValueChange = { lensType = it }, label = { Text("Lens Type") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        Text("Studio Image Preset", style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          BUNDLED_IMAGE_KEYS.forEach { (key, label) ->
            FilterChip(selected = imageKey == key, onClick = { imageKey = key }, label = { Text(label) })
          }
        }

        OutlinedButton(
          onClick = {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
          },
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (imageUri.isBlank()) "Upload Custom Photo (Photo Picker)" else "Custom Photo Selected")
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          Text("Featured")
          Switch(checked = featured, onCheckedChange = { featured = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          Text("Best Seller")
          Switch(checked = bestSeller, onCheckedChange = { bestSeller = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          Text("New Arrival")
          Switch(checked = newArrival, onCheckedChange = { newArrival = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          Text("Special Offer")
          Switch(checked = specialOffer, onCheckedChange = { specialOffer = it })
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val slug = name.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
          onSave(
            Product(
              id = initialProduct?.id ?: "",
              name = name,
              brand = brand,
              sku = sku,
              category = category,
              price = priceText.toDoubleOrNull() ?: 0.0,
              discountPercent = (discountText.toLongOrNull() ?: 0L).coerceIn(0L, 90L),
              description = description,
              frameMaterial = material,
              frameColour = colour,
              frameShape = shape,
              frameSize = size,
              lensType = lensType,
              stockCount = (stockText.toLongOrNull() ?: 0L).coerceAtLeast(0L),
              popularityScore = initialProduct?.popularityScore ?: 85L,
              featured = featured,
              bestSeller = bestSeller,
              newArrival = newArrival,
              specialOffer = specialOffer,
              imageKey = imageKey,
              imageUri = imageUri.takeIf { it.isNotBlank() },
              galleryKeys = listOf(imageKey),
              seoSlug = slug,
            )
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
      ) {
        Text("Save Product")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}

@Composable
fun TestimonialEditorDialog(
  onDismiss: () -> Unit,
  onSave: (Testimonial) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var ratingText by remember { mutableStateOf("5") }
  var quote by remember { mutableStateOf("") }
  var frame by remember { mutableStateOf("") }
  var verified by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = AlabasterWhite,
    title = { Text("Add Client Testimonial", style = MaterialTheme.typography.headlineMedium) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
          value = ratingText,
          onValueChange = { ratingText = it },
          label = { Text("Rating (1-5)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(value = frame, onValueChange = { frame = it }, label = { Text("Frame Purchased") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = quote, onValueChange = { quote = it }, label = { Text("Review Quote *") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(checked = verified, onCheckedChange = { verified = it })
          Text("Verified Google Review")
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            Testimonial(
              customerName = name,
              rating = (ratingText.toLongOrNull() ?: 5L).coerceIn(1L, 5L),
              quote = quote,
              framePurchased = frame,
              verifiedGoogleReview = verified,
            )
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
      ) { Text("Publish") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}
