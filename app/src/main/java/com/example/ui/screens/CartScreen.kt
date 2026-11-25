package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.data.model.ORDER_STATUSES
import com.example.data.model.OpticalOrder
import com.example.data.model.StoreConfig
import com.example.ui.components.LuxuryEmptyState
import com.example.ui.components.ProductVisual
import com.example.ui.components.StatusPill
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DeepAntiqueGold
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureIvory
import com.example.ui.theme.SandstoneBeige
import com.example.ui.theme.SoftGoldTint
import com.example.ui.theme.WarmBeige
import com.example.util.WhatsAppHelper
import com.example.util.formatCurrency

@Composable
fun CartScreen(
  storeConfig: StoreConfig,
  cartItems: List<CartItem>,
  cartTotal: Double,
  orders: List<OpticalOrder>,
  defaultName: String,
  defaultEmail: String,
  isBusy: Boolean,
  onUpdateQuantity: (String, Int) -> Unit,
  onRemoveItem: (String) -> Unit,
  onSubmitOrder: (String, String, String, String, String, (OpticalOrder) -> Unit) -> Unit,
  onBrowseCatalogue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var customerName by remember(defaultName) { mutableStateOf(defaultName) }
  var customerPhone by remember { mutableStateOf("") }
  var customerEmail by remember(defaultEmail) { mutableStateOf(defaultEmail) }
  var deliveryAddress by remember { mutableStateOf("") }
  var orderNotes by remember { mutableStateOf("") }

  val totalSavings = cartItems.sumOf { item ->
    (item.product.price - item.product.finalPrice) * item.quantity
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(AlabasterWhite),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Text(
        text = "Shopping Bag & Orders",
        style = MaterialTheme.typography.headlineLarge,
        color = ObsidianBlack,
      )
    }

    if (cartItems.isEmpty()) {
      item {
        LuxuryEmptyState(
          icon = Icons.Outlined.ShoppingBag,
          title = "Your eyewear bag is empty",
          subtitle = "Explore our curated optical frames, sunglasses, and ophthalmic lenses.",
          actionLabel = "Browse Catalogue",
          onAction = onBrowseCatalogue,
        )
      }
    } else {
      items(cartItems, key = { it.product.id }) { item ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = PureIvory),
          border = BorderStroke(1.dp, SandstoneBeige),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            ProductVisual(
              imageKey = item.product.imageKey,
              imageUri = item.product.imageUri,
              contentDescription = item.product.name,
              modifier = Modifier
                .size(74.dp)
                .clip(RoundedCornerShape(12.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(item.product.brand.uppercase(), style = MaterialTheme.typography.labelSmall, color = DeepAntiqueGold)
              Text(item.product.name, style = MaterialTheme.typography.titleMedium, color = ObsidianBlack)
              Text(
                text = formatCurrency(item.lineTotal),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = ObsidianBlack,
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onUpdateQuantity(item.product.id, -1) }) {
                  Icon(Icons.Outlined.Remove, contentDescription = "Decrease quantity")
                }
                Text(
                  text = "${item.quantity}",
                  style = MaterialTheme.typography.titleMedium,
                  modifier = Modifier.padding(horizontal = 6.dp),
                )
                IconButton(onClick = { onUpdateQuantity(item.product.id, 1) }) {
                  Icon(Icons.Outlined.Add, contentDescription = "Increase quantity")
                }
              }
            }
            IconButton(onClick = { onRemoveItem(item.product.id) }) {
              Icon(Icons.Outlined.DeleteOutline, contentDescription = "Remove item", tint = MutedTaupe)
            }
          }
        }
      }

      // Cart Summary + Checkout Enquiry Form
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = PureIvory),
          border = BorderStroke(1.dp, SandstoneBeige),
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text("Cart Summary", style = MaterialTheme.typography.titleLarge, color = ObsidianBlack)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Items (${cartItems.sumOf { it.quantity }})", style = MaterialTheme.typography.bodyMedium, color = MutedTaupe)
              Text(formatCurrency(cartTotal + totalSavings), style = MaterialTheme.typography.bodyMedium, color = ObsidianBlack)
            }
            if (totalSavings > 0) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Atelier Discount", style = MaterialTheme.typography.bodyMedium, color = EmeraldVerified)
                Text("-${formatCurrency(totalSavings)}", style = MaterialTheme.typography.bodyMedium, color = EmeraldVerified)
              }
            }
            HorizontalDivider(color = SandstoneBeige)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Estimated Total", style = MaterialTheme.typography.titleMedium, color = ObsidianBlack)
              Text(
                formatCurrency(cartTotal),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = ObsidianBlack,
              )
            }

            Surface(
              color = WarmBeige,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = "Enquiry-Based Order: No online card charge required. Pay securely upon boutique fitting or delivery confirmation.",
                style = MaterialTheme.typography.bodySmall,
                color = ObsidianBlack,
                modifier = Modifier.padding(10.dp),
              )
            }

            OutlinedTextField(
              value = customerName,
              onValueChange = { customerName = it },
              label = { Text("Customer Name *") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("order_name_input"),
            )
            OutlinedTextField(
              value = customerPhone,
              onValueChange = { customerPhone = it },
              label = { Text("Mobile / WhatsApp Number *") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("order_phone_input"),
            )
            OutlinedTextField(
              value = customerEmail,
              onValueChange = { customerEmail = it },
              label = { Text("Email Address") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
              value = deliveryAddress,
              onValueChange = { deliveryAddress = it },
              label = { Text("Delivery Address *") },
              minLines = 2,
              modifier = Modifier.fillMaxWidth().testTag("order_address_input"),
            )
            OutlinedTextField(
              value = orderNotes,
              onValueChange = { orderNotes = it },
              label = { Text("Order Notes / Prescription Ref (Optional)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            Button(
              onClick = {
                onSubmitOrder(customerName, customerPhone, customerEmail, deliveryAddress, orderNotes) {
                  deliveryAddress = ""
                  orderNotes = ""
                }
              },
              enabled = !isBusy,
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_order_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
            ) {
              Text("Submit Order Enquiry")
            }

            OutlinedButton(
              onClick = {
                val draft = OpticalOrder(
                  id = "DRAFT",
                  customerName = customerName.ifBlank { "Customer" },
                  deliveryAddress = deliveryAddress.ifBlank { "To be confirmed" },
                  itemsSummary = cartItems.joinToString(", ") { "${it.product.name} x${it.quantity}" },
                  totalAmount = cartTotal,
                )
                WhatsAppHelper.openWhatsApp(
                  context,
                  storeConfig.whatsappNumber,
                  WhatsAppHelper.orderEnquiryMessage(storeConfig.shopName, draft),
                )
              },
              modifier = Modifier.fillMaxWidth().height(48.dp),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, EmeraldVerified),
            ) {
              Icon(Icons.Outlined.Chat, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("WhatsApp Order Enquiry", color = EmeraldVerified)
            }
          }
        }
      }
    }

    // Order History & Live Status Tracker
    item {
      Text(
        text = "Your Order History",
        style = MaterialTheme.typography.headlineMedium,
        color = ObsidianBlack,
      )
    }

    if (orders.isEmpty()) {
      item {
        LuxuryEmptyState(
          icon = Icons.Outlined.LocalShipping,
          title = "No orders placed yet",
          subtitle = "Submitted eyewear orders and status updates will appear here.",
        )
      }
    } else {
      items(orders, key = { it.id }) { order ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = PureIvory),
          border = BorderStroke(1.dp, SandstoneBeige),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Order #${order.id.take(6).uppercase()}",
                style = MaterialTheme.typography.titleMedium,
                color = ObsidianBlack,
              )
              StatusPill(order.status)
            }
            Text(order.itemsSummary, style = MaterialTheme.typography.bodyMedium, color = ObsidianBlack)
            Text(
              text = "Total: ${formatCurrency(order.totalAmount)} · ${order.deliveryAddress}",
              style = MaterialTheme.typography.bodySmall,
              color = MutedTaupe,
            )
            // Status progression bar
            val stepIndex = ORDER_STATUSES.indexOf(order.status).coerceAtLeast(0)
            if (order.status != "Cancelled") {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                listOf("Pending", "Confirmed", "Processing", "Ready", "Delivered").forEachIndexed { idx, label ->
                  val reached = idx <= stepIndex
                  Surface(
                    color = if (reached) SoftGoldTint else WarmBeige,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (reached) ChampagneGold else SandstoneBeige),
                    modifier = Modifier.weight(1f),
                  ) {
                    Text(
                      text = label,
                      style = MaterialTheme.typography.labelSmall,
                      color = if (reached) ObsidianBlack else MutedTaupe,
                      modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                      maxLines = 1,
                    )
                  }
                }
              }
            }

            OutlinedButton(
              onClick = {
                WhatsAppHelper.openWhatsApp(
                  context,
                  storeConfig.whatsappNumber,
                  WhatsAppHelper.orderEnquiryMessage(storeConfig.shopName, order),
                )
              },
              shape = RoundedCornerShape(10.dp),
            ) {
              Icon(Icons.Outlined.Chat, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Follow Up on WhatsApp", color = EmeraldVerified)
            }
          }
        }
      }
    }
  }
}
