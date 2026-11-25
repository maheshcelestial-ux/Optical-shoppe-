package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.BRAND_PARTNERS
import com.example.data.model.OPTICAL_CATEGORIES
import com.example.data.model.Product
import com.example.data.model.ProductSortOption
import com.example.data.model.StoreConfig
import com.example.ui.components.LuxuryEmptyState
import com.example.ui.components.LuxuryProductCard
import com.example.ui.components.ProductVisual
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DeepAntiqueGold
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureIvory
import com.example.ui.theme.SandstoneBeige
import com.example.ui.theme.SoftGoldTint
import com.example.ui.theme.WarmBeige
import com.example.ui.viewmodel.CatalogueFilterState
import com.example.util.WhatsAppHelper
import com.example.util.formatCurrency

@Composable
fun CatalogueScreen(
  storeConfig: StoreConfig,
  products: List<Product>,
  filterState: CatalogueFilterState,
  onSearchChange: (String) -> Unit,
  onCategorySelect: (String) -> Unit,
  onBrandSelect: (String) -> Unit,
  onMaxPriceChange: (Float) -> Unit,
  onSortSelect: (ProductSortOption) -> Unit,
  onResetFilters: () -> Unit,
  onAddToCart: (Product) -> Unit,
  selectedProduct: Product?,
  onSelectProduct: (Product?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var showFiltersPanel by remember { mutableStateOf(false) }

  val categoryOptions = remember { listOf("All") + OPTICAL_CATEGORIES }
  val brandOptions = remember(products) {
    (listOf("All") + BRAND_PARTNERS + products.map { it.brand }).distinct()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AlabasterWhite),
  ) {
    // Search & filter bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = filterState.searchQuery,
          onValueChange = onSearchChange,
          placeholder = { Text("Search frames, SKU, brand, material...") },
          leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
          trailingIcon = {
            if (filterState.searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchChange("") }) {
                Icon(Icons.Outlined.Close, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("catalogue_search_input"),
        )
        Surface(
          color = if (showFiltersPanel) ObsidianBlack else PureIvory,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, SandstoneBeige),
          modifier = Modifier
            .height(54.dp)
            .clickable { showFiltersPanel = !showFiltersPanel },
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 14.dp),
          ) {
            Text(
              text = if (showFiltersPanel) "Hide" else "Filters",
              style = MaterialTheme.typography.labelLarge,
              color = if (showFiltersPanel) ChampagneGold else ObsidianBlack,
            )
          }
        }
      }

      // 11-Category Horizontal Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        categoryOptions.forEach { cat ->
          val selected = filterState.selectedCategory == cat
          FilterChip(
            selected = selected,
            onClick = { onCategorySelect(cat) },
            label = { Text(cat, style = MaterialTheme.typography.labelMedium) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = ObsidianBlack,
              selectedLabelColor = ChampagneGold,
              containerColor = PureIvory,
              labelColor = ObsidianBlack,
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = selected,
              borderColor = SandstoneBeige,
            ),
          )
        }
      }

      // Expandable Brand, Price, and Sort panel
      if (showFiltersPanel) {
        Surface(
          color = WarmBeige,
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, SandstoneBeige),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Brand Filter", style = MaterialTheme.typography.titleSmall, color = ObsidianBlack)
              TextButton(onClick = onResetFilters) {
                Icon(Icons.Outlined.FilterAltOff, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset All")
              }
            }
            Row(
              modifier = Modifier.horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              brandOptions.forEach { brand ->
                FilterChip(
                  selected = filterState.selectedBrand == brand,
                  onClick = { onBrandSelect(brand) },
                  label = { Text(brand) },
                )
              }
            }

            Column {
              Text(
                text = "Max Price: ${formatCurrency(filterState.maxPrice.toDouble())}",
                style = MaterialTheme.typography.titleSmall,
                color = ObsidianBlack,
              )
              Slider(
                value = filterState.maxPrice,
                onValueChange = onMaxPriceChange,
                valueRange = 60f..800f,
                colors = SliderDefaults.colors(
                  thumbColor = ObsidianBlack,
                  activeTrackColor = ChampagneGold,
                ),
              )
            }

            Text("Sort By", style = MaterialTheme.typography.titleSmall, color = ObsidianBlack)
            Row(
              modifier = Modifier.horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              ProductSortOption.entries.forEach { opt ->
                FilterChip(
                  selected = filterState.sortOption == opt,
                  onClick = { onSortSelect(opt) },
                  label = { Text(opt.label) },
                )
              }
            }
          }
        }
      }
    }

    Text(
      text = "${products.size} ${if (products.size == 1) "model" else "models"} available",
      style = MaterialTheme.typography.labelMedium,
      color = MutedTaupe,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )

    if (products.isEmpty()) {
      Box(modifier = Modifier.padding(16.dp)) {
        LuxuryEmptyState(
          icon = Icons.Outlined.Visibility,
          title = "No matching eyewear found",
          subtitle = "Adjust your search query, category, brand, or price range.",
          actionLabel = "Reset Filters",
          onAction = onResetFilters,
        )
      }
    } else {
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columns = when {
          maxWidth >= 840.dp -> 3
          maxWidth >= 520.dp -> 2
          else -> 2
        }
        LazyVerticalGrid(
          columns = GridCells.Fixed(columns),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(products, key = { "${it.id}_${it.sku}" }) { product ->
            LuxuryProductCard(
              product = product,
              onClick = { onSelectProduct(product) },
              onAddToCart = { onAddToCart(product) },
              onWhatsAppEnquiry = {
                WhatsAppHelper.openWhatsApp(
                  context,
                  storeConfig.whatsappNumber,
                  WhatsAppHelper.productEnquiryMessage(storeConfig.shopName, product),
                )
              },
            )
          }
        }
      }
    }
  }

  if (selectedProduct != null) {
    BackHandler { onSelectProduct(null) }
    ProductDetailSheet(
      product = selectedProduct,
      storeConfig = storeConfig,
      onDismiss = { onSelectProduct(null) },
      onAddToCart = {
        onAddToCart(selectedProduct)
        onSelectProduct(null)
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailSheet(
  product: Product,
  storeConfig: StoreConfig,
  onDismiss: () -> Unit,
  onAddToCart: () -> Unit,
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val gallery = remember(product) {
    (listOf(product.imageKey) + product.galleryKeys).filter { it.isNotBlank() }.distinct()
  }
  var activeImageIndex by remember(product.id) { mutableIntStateOf(0) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = AlabasterWhite,
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp),
      contentPadding = PaddingValues(bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        val activeKey = gallery.getOrElse(activeImageIndex) { product.imageKey }
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(WarmBeige),
        ) {
          ProductVisual(
            imageKey = activeKey,
            imageUri = if (activeImageIndex == 0) product.imageUri else null,
            contentDescription = product.name,
            modifier = Modifier.fillMaxSize(),
          )
        }
        if (gallery.size > 1) {
          Spacer(modifier = Modifier.height(10.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            gallery.forEachIndexed { idx, key ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                  width = if (idx == activeImageIndex) 2.dp else 1.dp,
                  color = if (idx == activeImageIndex) ChampagneGold else SandstoneBeige,
                ),
                modifier = Modifier
                  .size(58.dp)
                  .clickable { activeImageIndex = idx },
              ) {
                ProductVisual(
                  imageKey = key,
                  imageUri = if (idx == 0) product.imageUri else null,
                  contentDescription = "Gallery thumbnail ${idx + 1}",
                  modifier = Modifier.fillMaxSize(),
                )
              }
            }
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "${product.brand.uppercase()} · SKU ${product.sku}",
              style = MaterialTheme.typography.labelSmall,
              color = DeepAntiqueGold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = product.name,
              style = MaterialTheme.typography.headlineLarge,
              color = ObsidianBlack,
            )
            Text(
              text = product.category,
              style = MaterialTheme.typography.bodySmall,
              color = MutedTaupe,
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = formatCurrency(product.finalPrice),
              style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
              color = ObsidianBlack,
            )
            if (product.discountPercent > 0) {
              Text(
                text = formatCurrency(product.price),
                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                color = MutedTaupe,
              )
              Surface(color = SoftGoldTint, shape = RoundedCornerShape(50)) {
                Text(
                  text = "Save ${product.discountPercent}%",
                  style = MaterialTheme.typography.labelSmall,
                  color = DeepAntiqueGold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }
          }
        }
      }

      item {
        Text(
          text = product.description,
          style = MaterialTheme.typography.bodyLarge,
          color = ObsidianBlack,
        )
      }

      item {
        Surface(
          color = PureIvory,
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, SandstoneBeige),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text("Optical Specifications", style = MaterialTheme.typography.titleMedium, color = ObsidianBlack)
            HorizontalDivider(color = SandstoneBeige)
            SpecRow("Material", product.frameMaterial)
            SpecRow("Frame Colour", product.frameColour)
            SpecRow("Shape", product.frameShape)
            SpecRow("Dimensions", product.frameSize)
            SpecRow("Lens Compatibility", product.lensType)
            SpecRow(
              "Availability",
              if (product.inStock) "In Stock (${product.stockCount} units)" else "Out of Stock",
              valueColor = if (product.inStock) EmeraldVerified else CrimsonAlert,
            )
            if (product.seoSlug.isNotBlank()) {
              SpecRow("Catalogue Ref", "/eyewear/${product.seoSlug}")
            }
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = onAddToCart,
            enabled = product.inStock,
            modifier = Modifier
              .weight(1f)
              .height(52.dp)
              .testTag("detail_add_to_cart_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = ObsidianBlack,
              contentColor = PureIvory,
            ),
          ) {
            Icon(Icons.Outlined.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (product.inStock) "Add to Bag" else "Out of Stock")
          }

          OutlinedButton(
            onClick = {
              WhatsAppHelper.openWhatsApp(
                context,
                storeConfig.whatsappNumber,
                WhatsAppHelper.productEnquiryMessage(storeConfig.shopName, product),
              )
            },
            modifier = Modifier
              .weight(1f)
              .height(52.dp)
              .testTag("detail_whatsapp_enquiry_button"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, EmeraldVerified),
          ) {
            Icon(Icons.Outlined.Chat, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("WhatsApp Enquiry", color = EmeraldVerified, maxLines = 1)
          }
        }
      }
    }
  }
}

@Composable
private fun SpecRow(
  label: String,
  value: String,
  valueColor: androidx.compose.ui.graphics.Color = ObsidianBlack,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(text = label, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
      color = valueColor,
    )
  }
}
