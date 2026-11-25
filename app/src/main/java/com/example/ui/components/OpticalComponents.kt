package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.Product
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.AmberPending
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonSurface
import com.example.ui.theme.DeepAntiqueGold
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureIvory
import com.example.ui.theme.SandstoneBeige
import com.example.ui.theme.SoftGoldTint
import com.example.ui.theme.SubtleOutline
import com.example.ui.theme.WarmBeige
import com.example.util.formatCurrency
import com.example.util.resolveProductDrawable

@Composable
fun AtelierTopBar(
  shopName: String,
  tagline: String,
  userEmail: String,
  onSignOutClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = AlabasterWhite,
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.weight(1f),
      ) {
        Surface(
          color = ObsidianBlack,
          shape = CircleShape,
          modifier = Modifier.size(38.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Outlined.Visibility,
              contentDescription = "$shopName emblem",
              tint = ChampagneGold,
              modifier = Modifier.size(20.dp),
            )
          }
        }
        Column {
          Text(
            text = shopName.ifBlank { "Atelier Optic" },
            style = MaterialTheme.typography.headlineMedium,
            color = ObsidianBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = tagline.ifBlank { "Bespoke Eyewear & Optometry" },
            style = MaterialTheme.typography.labelSmall,
            color = DeepAntiqueGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (userEmail.isNotBlank()) {
          Surface(
            color = WarmBeige,
            shape = RoundedCornerShape(50),
            modifier = Modifier.padding(end = 4.dp),
          ) {
            Text(
              text = userEmail.substringBefore("@").take(14),
              style = MaterialTheme.typography.labelSmall,
              color = ObsidianBlack,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
          }
        }
        IconButton(
          onClick = onSignOutClick,
          modifier = Modifier.testTag("sign_out_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = "Sign out",
            tint = ObsidianBlack,
          )
        }
      }
    }
  }
}

@Composable
fun SectionHeader(
  eyebrow: String,
  title: String,
  actionLabel: String? = null,
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = eyebrow.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = DeepAntiqueGold,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = ObsidianBlack,
      )
    }
    if (actionLabel != null && onActionClick != null) {
      TextButton(onClick = onActionClick) {
        Text(
          text = actionLabel,
          style = MaterialTheme.typography.labelLarge,
          color = DeepAntiqueGold,
        )
      }
    }
  }
}

@Composable
fun ProductVisual(
  imageKey: String,
  imageUri: String?,
  contentDescription: String,
  modifier: Modifier = Modifier,
) {
  val fallbackRes = resolveProductDrawable(imageKey)
  if (!imageUri.isNullOrBlank()) {
    AsyncImage(
      model = imageUri,
      contentDescription = contentDescription,
      placeholder = painterResource(id = fallbackRes),
      error = painterResource(id = fallbackRes),
      contentScale = ContentScale.Crop,
      modifier = modifier,
    )
  } else {
    Image(
      painter = painterResource(id = fallbackRes),
      contentDescription = contentDescription,
      contentScale = ContentScale.Crop,
      modifier = modifier,
    )
  }
}

@Composable
fun LuxuryProductCard(
  product: Product,
  onClick: () -> Unit,
  onAddToCart: () -> Unit,
  onWhatsAppEnquiry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag("product_card_${product.sku}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = PureIvory),
    border = BorderStroke(1.dp, SandstoneBeige),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(168.dp)
          .background(WarmBeige),
      ) {
        ProductVisual(
          imageKey = product.imageKey,
          imageUri = product.imageUri,
          contentDescription = product.name,
          modifier = Modifier.fillMaxSize(),
        )
        // Badges
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          if (product.discountPercent > 0) {
            Surface(
              color = ObsidianBlack,
              shape = RoundedCornerShape(50),
            ) {
              Text(
                text = "-${product.discountPercent}%",
                style = MaterialTheme.typography.labelSmall,
                color = ChampagneGold,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
              )
            }
          } else if (product.newArrival) {
            Surface(
              color = SoftGoldTint,
              shape = RoundedCornerShape(50),
              border = BorderStroke(1.dp, ChampagneGold),
            ) {
              Text(
                text = "NEW",
                style = MaterialTheme.typography.labelSmall,
                color = ObsidianBlack,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
              )
            }
          } else {
            Spacer(modifier = Modifier.width(1.dp))
          }

          Surface(
            color = if (product.inStock) PureIvory.copy(alpha = 0.92f) else CrimsonSurface,
            shape = RoundedCornerShape(50),
          ) {
            Text(
              text = if (product.inStock) "${product.stockCount} left" else "Out of stock",
              style = MaterialTheme.typography.labelSmall,
              color = if (product.inStock) ObsidianBlack else CrimsonAlert,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
          }
        }
      }

      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = product.brand.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = DeepAntiqueGold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = product.name,
          style = MaterialTheme.typography.titleMedium,
          color = ObsidianBlack,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "${product.frameMaterial} · ${product.frameColour}",
          style = MaterialTheme.typography.bodySmall,
          color = MutedTaupe,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = formatCurrency(product.finalPrice),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = ObsidianBlack,
          )
          if (product.discountPercent > 0) {
            Text(
              text = formatCurrency(product.price),
              style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
              color = MutedTaupe,
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Button(
            onClick = onAddToCart,
            enabled = product.inStock,
            modifier = Modifier
              .weight(1f)
              .height(42.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = ObsidianBlack,
              contentColor = PureIvory,
            ),
          ) {
            Icon(
              imageVector = Icons.Outlined.AddShoppingCart,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (product.inStock) "Add" else "Sold Out",
              style = MaterialTheme.typography.labelLarge,
              maxLines = 1,
            )
          }

          OutlinedButton(
            onClick = onWhatsAppEnquiry,
            modifier = Modifier.size(42.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(0.dp),
            border = BorderStroke(1.dp, SubtleOutline),
          ) {
            Icon(
              imageVector = Icons.Outlined.Chat,
              contentDescription = "WhatsApp enquiry for ${product.name}",
              tint = EmeraldVerified,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
fun StatusPill(
  status: String,
  modifier: Modifier = Modifier,
) {
  val (bg, fg) = when (status.lowercase()) {
    "confirmed", "delivered", "completed", "fulfilled", "replied" -> EmeraldSurface to EmeraldVerified
    "cancelled" -> CrimsonSurface to CrimsonAlert
    "quoted", "ready", "processing" -> SoftGoldTint to DeepAntiqueGold
    else -> AmberSurface to AmberPending
  }
  Surface(
    color = bg,
    shape = RoundedCornerShape(50),
    modifier = modifier,
  ) {
    Text(
      text = status,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
      color = fg,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
  }
}

@Composable
fun WhatsAppFloatingButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ExtendedFloatingActionButton(
    onClick = onClick,
    containerColor = ObsidianBlack,
    contentColor = ChampagneGold,
    shape = RoundedCornerShape(50),
    modifier = modifier.testTag("whatsapp_floating_button"),
    icon = {
      Icon(
        imageVector = Icons.Outlined.Chat,
        contentDescription = "WhatsApp Concierge",
      )
    },
    text = {
      Text(
        text = "WhatsApp",
        style = MaterialTheme.typography.labelLarge,
        color = PureIvory,
      )
    },
  )
}

@Composable
fun LuxuryEmptyState(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = WarmBeige,
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(1.dp, SandstoneBeige),
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Surface(
        color = PureIvory,
        shape = CircleShape,
        modifier = Modifier.size(52.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DeepAntiqueGold,
            modifier = Modifier.size(26.dp),
          )
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = ObsidianBlack,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MutedTaupe,
      )
      if (actionLabel != null && onAction != null) {
        Spacer(modifier = Modifier.height(14.dp))
        Button(
          onClick = onAction,
          colors = ButtonDefaults.buttonColors(
            containerColor = ObsidianBlack,
            contentColor = PureIvory,
          ),
          shape = RoundedCornerShape(12.dp),
        ) {
          Text(actionLabel)
        }
      }
    }
  }
}
