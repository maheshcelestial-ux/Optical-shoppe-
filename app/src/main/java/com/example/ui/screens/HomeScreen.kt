package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BRAND_PARTNERS
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.data.model.Testimonial
import com.example.ui.components.LuxuryProductCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DeepAntiqueGold
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureIvory
import com.example.ui.theme.SandstoneBeige
import com.example.ui.theme.SoftGoldTint
import com.example.ui.theme.WarmBeige
import com.example.ui.theme.WarmCharcoal
import com.example.util.WhatsAppHelper
import com.example.util.dialPhone
import com.example.util.openExternalUrl
import com.example.util.openGoogleMaps
import com.example.util.sendEmail

@Composable
fun HomeScreen(
  storeConfig: StoreConfig,
  products: List<Product>,
  testimonials: List<Testimonial>,
  onNavigateToCatalogue: (category: String, brand: String) -> Unit,
  onNavigateToEyeTest: () -> Unit,
  onNavigateToPrescription: () -> Unit,
  onNavigateToAbout: () -> Unit,
  onProductClick: (Product) -> Unit,
  onAddToCart: (Product) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val featuredProducts = products.filter { it.featured }.ifEmpty { products.take(4) }
  val bestSellers = products.filter { it.bestSeller }.ifEmpty { products.take(4) }
  val newArrivals = products.filter { it.newArrival }.ifEmpty { products.take(4) }
  val specialOffers = products.filter { it.specialOffer || it.discountPercent > 0 }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(AlabasterWhite),
    contentPadding = PaddingValues(bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    // 1. Storefront Navigation Bar (Home, Eyeglasses, Sunglasses, Contact Lenses, Lenses, Eye Test, About Us)
    item {
      val navItems = listOf(
        "Home" to { },
        "Eyeglasses" to { onNavigateToCatalogue("Eyeglasses", "All") },
        "Sunglasses" to { onNavigateToCatalogue("SunglassesGroup", "All") },
        "Contact Lenses" to { onNavigateToCatalogue("Contact Lenses", "All") },
        "Lenses" to { onNavigateToCatalogue("LensesGroup", "All") },
        "Eye Test" to onNavigateToEyeTest,
        "About Us" to onNavigateToAbout,
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        navItems.forEachIndexed { index, (label, action) ->
          FilterChip(
            selected = index == 0,
            onClick = action,
            label = {
              Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = ObsidianBlack,
              selectedLabelColor = ChampagneGold,
              containerColor = PureIvory,
              labelColor = ObsidianBlack,
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = index == 0,
              borderColor = SandstoneBeige,
            ),
          )
        }
      }
    }

    // 2. Hero Banner
    item {
      Box(
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth()
          .height(340.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(ObsidianBlack),
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_hero_eyewear),
          contentDescription = "Luxury eyewear campaign hero",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  ObsidianBlack.copy(alpha = 0.25f),
                  ObsidianBlack.copy(alpha = 0.65f),
                  ObsidianBlack.copy(alpha = 0.94f),
                )
              )
            )
        )
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(22.dp),
        ) {
          Surface(
            color = ChampagneGold.copy(alpha = 0.2f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.7f)),
          ) {
            Text(
              text = "NEW SEASON COLLECTION",
              style = MaterialTheme.typography.labelSmall,
              color = ChampagneGold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = storeConfig.heroHeadline.ifBlank { "See Better. Look Better. Live Better." },
            style = MaterialTheme.typography.displayMedium,
            color = PureIvory,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = storeConfig.heroSubheadline,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmBeige,
            maxLines = 2,
          )
          Spacer(modifier = Modifier.height(16.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Button(
              onClick = { onNavigateToCatalogue("All", "All") },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("hero_shop_now_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = ChampagneGold,
                contentColor = ObsidianBlack,
              ),
            ) {
              Icon(Icons.Outlined.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Shop Now", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
              onClick = onNavigateToEyeTest,
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("hero_book_eye_test_button"),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, PureIvory.copy(alpha = 0.75f)),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = PureIvory),
            ) {
              Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Book Eye Test", style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
          }
        }
      }
    }

    // 3. Special Offers Promo Banner
    if (storeConfig.promoBannerText.isNotBlank()) {
      item {
        Surface(
          color = ObsidianBlack,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable { onNavigateToCatalogue("All", "All") },
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Surface(
              color = ChampagneGold.copy(alpha = 0.18f),
              shape = RoundedCornerShape(10.dp),
            ) {
              Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = ChampagneGold,
                modifier = Modifier.padding(10.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "EXCLUSIVE ATELIER OFFER",
                style = MaterialTheme.typography.labelSmall,
                color = ChampagneGold,
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = storeConfig.promoBannerText,
                style = MaterialTheme.typography.titleSmall,
                color = PureIvory,
              )
            }
          }
        }
      }
    }

    // 4. Featured Products
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
          eyebrow = "Curated Selection",
          title = "Featured Eyewear",
          actionLabel = "View All",
          onActionClick = { onNavigateToCatalogue("All", "All") },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          items(featuredProducts, key = { "feat_${it.id}_${it.sku}" }) { product ->
            LuxuryProductCard(
              product = product,
              onClick = { onProductClick(product) },
              onAddToCart = { onAddToCart(product) },
              onWhatsAppEnquiry = {
                WhatsAppHelper.openWhatsApp(
                  context,
                  storeConfig.whatsappNumber,
                  WhatsAppHelper.productEnquiryMessage(storeConfig.shopName, product),
                )
              },
              modifier = Modifier.width(225.dp),
            )
          }
        }
      }
    }

    // 5. Best-Selling Frames
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
          eyebrow = "Client Favorites",
          title = "Best-Selling Frames",
          actionLabel = "Explore",
          onActionClick = { onNavigateToCatalogue("All", "All") },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          items(bestSellers, key = { "best_${it.id}_${it.sku}" }) { product ->
            LuxuryProductCard(
              product = product,
              onClick = { onProductClick(product) },
              onAddToCart = { onAddToCart(product) },
              onWhatsAppEnquiry = {
                WhatsAppHelper.openWhatsApp(
                  context,
                  storeConfig.whatsappNumber,
                  WhatsAppHelper.productEnquiryMessage(storeConfig.shopName, product),
                )
              },
              modifier = Modifier.width(225.dp),
            )
          }
        }
      }
    }

    // 6. New Arrivals & Special Offers Strip
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
          eyebrow = "Just Arrived",
          title = if (specialOffers.isNotEmpty()) "New Arrivals & Offers" else "New Arrivals",
          actionLabel = "Shop All",
          onActionClick = { onNavigateToCatalogue("All", "All") },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          items((newArrivals + specialOffers).distinctBy { it.id }, key = { "new_${it.id}_${it.sku}" }) { product ->
            LuxuryProductCard(
              product = product,
              onClick = { onProductClick(product) },
              onAddToCart = { onAddToCart(product) },
              onWhatsAppEnquiry = {
                WhatsAppHelper.openWhatsApp(
                  context,
                  storeConfig.whatsappNumber,
                  WhatsAppHelper.productEnquiryMessage(storeConfig.shopName, product),
                )
              },
              modifier = Modifier.width(225.dp),
            )
          }
        }
      }
    }

    // 7. Quick Prescription & Clinical Optometry Callout
    item {
      Card(
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WarmBeige),
        border = BorderStroke(1.dp, SandstoneBeige),
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "CLINICAL OPTOMETRY & PRIVATE VAULT",
            style = MaterialTheme.typography.labelSmall,
            color = DeepAntiqueGold,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Already Have an Optical Prescription?",
            style = MaterialTheme.typography.headlineMedium,
            color = ObsidianBlack,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Upload your prescription privately or enter your SPH, CYL, AXIS, and PD for a tailored Zeiss or Varilux lens quotation.",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedTaupe,
          )
          Spacer(modifier = Modifier.height(14.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = onNavigateToPrescription,
              colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.height(48.dp),
            ) {
              Text("Upload Prescription")
            }
            OutlinedButton(
              onClick = onNavigateToEyeTest,
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, ObsidianBlack),
              modifier = Modifier.height(48.dp),
            ) {
              Text("Schedule Exam", color = ObsidianBlack)
            }
          }
        }
      }
    }

    // 8. Designer & Optical Lens Brands
    item {
      Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        SectionHeader(
          eyebrow = "Authorized Stockist",
          title = "Curated Maison & Lens Partners",
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          BRAND_PARTNERS.forEach { brand ->
            Surface(
              color = PureIvory,
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, SandstoneBeige),
              modifier = Modifier.clickable { onNavigateToCatalogue("All", brand) },
            ) {
              Text(
                text = brand,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = ObsidianBlack,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
              )
            }
          }
        }
      }
    }

    // 9. Customer Testimonials & Google Review Rating
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
          eyebrow = "Google Reviews · ${storeConfig.googleReviewRating} ★ (${storeConfig.googleReviewCount})",
          title = "Client Testimonials",
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(testimonials, key = { "${it.id}_${it.customerName}" }) { review ->
            Card(
              modifier = Modifier.width(280.dp),
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = PureIvory),
              border = BorderStroke(1.dp, SandstoneBeige),
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(review.rating.toInt().coerceIn(1, 5)) {
                      Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  }
                  if (review.verifiedGoogleReview) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = "Verified Google Review",
                        tint = DeepAntiqueGold,
                        modifier = Modifier.size(14.dp),
                      )
                      Text(
                        text = "Google Review",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepAntiqueGold,
                      )
                    }
                  }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "\"${review.quote}\"",
                  style = MaterialTheme.typography.bodyMedium,
                  color = ObsidianBlack,
                  maxLines = 4,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                  text = review.customerName,
                  style = MaterialTheme.typography.titleSmall,
                  color = ObsidianBlack,
                )
                if (review.framePurchased.isNotBlank()) {
                  Text(
                    text = review.framePurchased,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedTaupe,
                  )
                }
              }
            }
          }
        }
      }
    }

    // 10. Store Location, Opening Hours & Professional Footer
    item {
      Card(
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
      ) {
        Column(modifier = Modifier.padding(22.dp)) {
          Text(
            text = "VISIT THE FLAGSHIP BOUTIQUE",
            style = MaterialTheme.typography.labelSmall,
            color = ChampagneGold,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = storeConfig.shopName,
            style = MaterialTheme.typography.headlineLarge,
            color = PureIvory,
          )
          Spacer(modifier = Modifier.height(14.dp))

          Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(18.dp))
            Text(
              text = storeConfig.address,
              style = MaterialTheme.typography.bodyMedium,
              color = WarmBeige,
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(18.dp))
            Text(
              text = storeConfig.openingHours,
              style = MaterialTheme.typography.bodyMedium,
              color = WarmBeige,
            )
          }

          Spacer(modifier = Modifier.height(16.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Button(
              onClick = { openGoogleMaps(context, storeConfig.mapsQuery) },
              modifier = Modifier
                .weight(1f)
                .height(48.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Google Maps")
            }
            OutlinedButton(
              onClick = { dialPhone(context, storeConfig.mobileNumber) },
              modifier = Modifier
                .weight(1f)
                .height(48.dp),
              border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.6f)),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(Icons.Outlined.Call, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Call Store", color = PureIvory)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = WarmCharcoal)
          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                text = "Instagram",
                style = MaterialTheme.typography.labelMedium,
                color = ChampagneGold,
                modifier = Modifier.clickable { openExternalUrl(context, storeConfig.instagramUrl) },
              )
              Text(
                text = "Facebook",
                style = MaterialTheme.typography.labelMedium,
                color = ChampagneGold,
                modifier = Modifier.clickable { openExternalUrl(context, storeConfig.facebookUrl) },
              )
              Text(
                text = "Email",
                style = MaterialTheme.typography.labelMedium,
                color = ChampagneGold,
                modifier = Modifier.clickable { sendEmail(context, storeConfig.email, "Optical Enquiry") },
              )
            }
            Icon(
              imageVector = Icons.Outlined.Chat,
              contentDescription = "WhatsApp",
              tint = ChampagneGold,
              modifier = Modifier
                .size(22.dp)
                .clickable {
                  WhatsAppHelper.openWhatsApp(
                    context,
                    storeConfig.whatsappNumber,
                    "Hello ${storeConfig.shopName}! I'd like to speak with an optician.",
                  )
                },
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "${storeConfig.seoTitle} · ${storeConfig.seoDescription}",
            style = MaterialTheme.typography.bodySmall,
            color = MutedTaupe,
          )
        }
      }
    }
  }
}
