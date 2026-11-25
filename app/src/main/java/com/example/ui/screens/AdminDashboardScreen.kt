package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.APPOINTMENT_STATUSES
import com.example.data.model.Appointment
import com.example.data.model.CustomerEnquiry
import com.example.data.model.ENQUIRY_STATUSES
import com.example.data.model.ORDER_STATUSES
import com.example.data.model.OpticalOrder
import com.example.data.model.PRESCRIPTION_STATUSES
import com.example.data.model.Prescription
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.data.model.Testimonial
import com.example.ui.components.StatusPill
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DeepAntiqueGold
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureIvory
import com.example.ui.theme.SandstoneBeige
import com.example.ui.theme.SoftGoldTint
import com.example.ui.theme.WarmBeige
import com.example.util.formatCurrency

private enum class AdminSection(val label: String) {
  OVERVIEW("Overview & Reports"),
  PRODUCTS("Products"),
  ORDERS("Orders & Bookings"),
  RX_ENQUIRIES("Rx & Enquiries"),
  STORE_SETTINGS("Store & Reviews"),
}

@Composable
fun AdminDashboardScreen(
  isAdmin: Boolean,
  userEmail: String,
  storeConfig: StoreConfig,
  products: List<Product>,
  cloudProductsCount: Int,
  testimonials: List<Testimonial>,
  orders: List<OpticalOrder>,
  appointments: List<Appointment>,
  prescriptions: List<Prescription>,
  enquiries: List<CustomerEnquiry>,
  isBusy: Boolean,
  onSwitchAccount: () -> Unit,
  onSeedCatalogue: () -> Unit,
  onSaveProduct: (Product) -> Unit,
  onDeleteProduct: (String) -> Unit,
  onUpdateOrderStatus: (String, String) -> Unit,
  onUpdateAppointmentStatus: (String, String, String) -> Unit,
  onUpdatePrescriptionQuote: (String, String, Double?, String) -> Unit,
  onUpdateEnquiryStatus: (String, String) -> Unit,
  onSaveStoreConfig: (StoreConfig) -> Unit,
  onSaveTestimonial: (Testimonial) -> Unit,
  onDeleteTestimonial: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (!isAdmin) {
    Column(
      modifier = modifier
        .fillMaxSize()
        .background(AlabasterWhite)
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PureIvory),
        border = BorderStroke(1.dp, SandstoneBeige),
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Surface(color = ObsidianBlack, shape = RoundedCornerShape(14.dp)) {
            Icon(
              imageVector = Icons.Outlined.Lock,
              contentDescription = null,
              tint = ChampagneGold,
              modifier = Modifier.padding(14.dp).size(28.dp),
            )
          }
          Text("Admin Portal Protected", style = MaterialTheme.typography.headlineMedium, color = ObsidianBlack)
          Text(
            text = "Signed in as ${userEmail.ifBlank { "customer" }}. Store owner management requires an authorized administrator Google account.",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedTaupe,
          )
          Button(
            onClick = onSwitchAccount,
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
          ) {
            Text("Switch Google Account")
          }
        }
      }
    }
    return
  }

  val context = LocalContext.current
  var activeSection by remember { mutableStateOf(AdminSection.OVERVIEW) }
  var editingProduct by remember { mutableStateOf<Product?>(null) }
  var showProductDialog by remember { mutableStateOf(false) }
  var showTestimonialDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AlabasterWhite),
  ) {
    // Sub-navigation chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      AdminSection.entries.forEach { section ->
        FilterChip(
          selected = activeSection == section,
          onClick = { activeSection = section },
          label = { Text(section.label) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ObsidianBlack,
            selectedLabelColor = ChampagneGold,
            containerColor = PureIvory,
            labelColor = ObsidianBlack,
          ),
        )
      }
    }

    when (activeSection) {
      AdminSection.OVERVIEW -> {
        val totalRevenue = orders.filter { it.status != "Cancelled" }.sumOf { it.totalAmount }
        val pendingOrders = orders.count { it.status == "Pending" }
        val pendingAppointments = appointments.count { it.status == "Pending" }
        val lowStockProducts = products.filter { it.stockCount <= 5L }
        val openEnquiries = enquiries.count { it.status == "Open" }

        val reportText = remember(orders, appointments, prescriptions, enquiries, products, storeConfig) {
          buildString {
            appendLine("=== ${storeConfig.shopName} Sales & Enquiry Report ===")
            appendLine("Total Orders: ${orders.size} (Pending: $pendingOrders)")
            appendLine("Gross Order Value: ${formatCurrency(totalRevenue)}")
            appendLine("Eye Test Appointments: ${appointments.size} (Pending: $pendingAppointments)")
            appendLine("Private Prescriptions: ${prescriptions.size}")
            appendLine("Customer Enquiries: ${enquiries.size} (Open: $openEnquiries)")
            appendLine("Active Catalogue Items: ${products.size} (Low Stock <=5: ${lowStockProducts.size})")
          }
        }

        LazyColumn(
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          item {
            Surface(color = ObsidianBlack, shape = RoundedCornerShape(18.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(28.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text("Shop Owner Console", style = MaterialTheme.typography.titleLarge, color = PureIvory)
                  Text("Signed in as $userEmail", style = MaterialTheme.typography.bodySmall, color = WarmBeige)
                }
              }
            }
          }

          if (cloudProductsCount == 0) {
            item {
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGoldTint),
                border = BorderStroke(1.dp, ChampagneGold),
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Text("Initialize Cloud Storefront", style = MaterialTheme.typography.titleMedium, color = ObsidianBlack)
                  Text(
                    "Publish the starter 11-category eyewear catalogue, boutique config, and verified reviews to Cloud Firestore so you can edit prices, stock, and discounts live.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ObsidianBlack,
                  )
                  Button(
                    onClick = onSeedCatalogue,
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
                  ) {
                    Icon(Icons.Outlined.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Starter Catalogue to Cloud")
                  }
                }
              }
            }
          }

          item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
              KpiCard("Sales Value", formatCurrency(totalRevenue), "${orders.size} orders", Modifier.weight(1f))
              KpiCard("Appointments", "${appointments.size}", "$pendingAppointments pending", Modifier.weight(1f))
            }
          }
          item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
              KpiCard("Private Rx", "${prescriptions.size}", "Protected records", Modifier.weight(1f))
              KpiCard("Enquiries", "${enquiries.size}", "$openEnquiries open", Modifier.weight(1f))
            }
          }

          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = PureIvory),
              border = BorderStroke(1.dp, SandstoneBeige),
            ) {
              Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Outlined.Assessment, contentDescription = null, tint = DeepAntiqueGold)
                  Text("Sales & Enquiry Report", style = MaterialTheme.typography.titleMedium, color = ObsidianBlack)
                }
                Text(reportText, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                Button(
                  onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                      type = "text/plain"
                      putExtra(Intent.EXTRA_SUBJECT, "${storeConfig.shopName} Sales & Enquiry Report")
                      putExtra(Intent.EXTRA_TEXT, reportText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export Report"))
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Export / Share Report")
                }
              }
            }
          }
        }
      }

      AdminSection.PRODUCTS -> {
        LazyColumn(
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          item {
            Button(
              onClick = {
                editingProduct = null
                showProductDialog = true
              },
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_add_product_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
            ) {
              Icon(Icons.Outlined.Add, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Add New Eyewear Product")
            }
          }

          items(products, key = { "${it.id}_${it.sku}" }) { product ->
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = PureIvory),
              border = BorderStroke(1.dp, SandstoneBeige),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text("${product.name} (${product.sku})", style = MaterialTheme.typography.titleSmall, color = ObsidianBlack)
                  Text("${product.brand} · ${product.category}", style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                  Text(
                    text = "${formatCurrency(product.finalPrice)} · Stock: ${product.stockCount} · Disc: ${product.discountPercent}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = DeepAntiqueGold,
                  )
                }
                Row {
                  IconButton(onClick = {
                    editingProduct = product
                    showProductDialog = true
                  }) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit ${product.name}")
                  }
                  if (!product.id.startsWith("starter_")) {
                    IconButton(onClick = { onDeleteProduct(product.id) }) {
                      Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete ${product.name}", tint = CrimsonAlert)
                    }
                  }
                }
              }
            }
          }
        }
      }

      AdminSection.ORDERS -> {
        LazyColumn(
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          item { Text("Customer Orders (${orders.size})", style = MaterialTheme.typography.headlineMedium) }
          items(orders, key = { it.id }) { order ->
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = PureIvory),
              border = BorderStroke(1.dp, SandstoneBeige),
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("${order.customerName} · ${order.customerPhone}", style = MaterialTheme.typography.titleSmall)
                  StatusPill(order.status)
                }
                Text(order.itemsSummary, style = MaterialTheme.typography.bodySmall)
                Text("Total: ${formatCurrency(order.totalAmount)} · ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  ORDER_STATUSES.forEach { st ->
                    FilterChip(
                      selected = order.status == st,
                      onClick = { onUpdateOrderStatus(order.id, st) },
                      label = { Text(st) },
                    )
                  }
                }
              }
            }
          }

          item { Text("Eye Test Appointments (${appointments.size})", style = MaterialTheme.typography.headlineMedium) }
          items(appointments, key = { it.id }) { appt ->
            var adminNote by remember(appt.id, appt.adminNotes) { mutableStateOf(appt.adminNotes) }
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = PureIvory),
              border = BorderStroke(1.dp, SandstoneBeige),
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("${appt.fullName} · ${appt.serviceRequired}", style = MaterialTheme.typography.titleSmall)
                  StatusPill(appt.status)
                }
                Text("${appt.appointmentDate} at ${appt.preferredTime} · ${appt.mobileNumber}", style = MaterialTheme.typography.bodySmall, color = DeepAntiqueGold)
                if (appt.message.isNotBlank()) Text(appt.message, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                OutlinedTextField(
                  value = adminNote,
                  onValueChange = { adminNote = it },
                  label = { Text("Optician Note") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                )
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  APPOINTMENT_STATUSES.forEach { st ->
                    FilterChip(
                      selected = appt.status == st,
                      onClick = { onUpdateAppointmentStatus(appt.id, st, adminNote) },
                      label = { Text(st) },
                    )
                  }
                }
              }
            }
          }
        }
      }

      AdminSection.RX_ENQUIRIES -> {
        LazyColumn(
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          item { Text("Private Prescriptions (${prescriptions.size})", style = MaterialTheme.typography.headlineMedium) }
          items(prescriptions, key = { it.id }) { rx ->
            var quoteInput by remember(rx.id, rx.quotedPrice) { mutableStateOf(rx.quotedPrice?.toString() ?: "") }
            var rxNote by remember(rx.id, rx.notes) { mutableStateOf(rx.notes) }
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = PureIvory),
              border = BorderStroke(1.dp, SandstoneBeige),
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("${rx.customerName} (${rx.phoneNumber})", style = MaterialTheme.typography.titleSmall)
                  StatusPill(rx.status)
                }
                Text("${rx.lensType} · PD ${rx.pd.ifBlank { "-" }} · ADD ${rx.addPower.ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall)
                Text("OD: ${rx.rightSph}/${rx.rightCyl}/${rx.rightAxis} | OS: ${rx.leftSph}/${rx.leftCyl}/${rx.leftAxis}", style = MaterialTheme.typography.bodySmall, color = DeepAntiqueGold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  OutlinedTextField(value = quoteInput, onValueChange = { quoteInput = it }, label = { Text("Quote ($)") }, singleLine = true, modifier = Modifier.weight(1f))
                  OutlinedTextField(value = rxNote, onValueChange = { rxNote = it }, label = { Text("Notes") }, singleLine = true, modifier = Modifier.weight(2f))
                }
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  PRESCRIPTION_STATUSES.forEach { st ->
                    FilterChip(
                      selected = rx.status == st,
                      onClick = { onUpdatePrescriptionQuote(rx.id, st, quoteInput.toDoubleOrNull(), rxNote) },
                      label = { Text(st) },
                    )
                  }
                }
              }
            }
          }

          item { Text("Customer Enquiries (${enquiries.size})", style = MaterialTheme.typography.headlineMedium) }
          items(enquiries, key = { it.id }) { enq ->
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = PureIvory),
              border = BorderStroke(1.dp, SandstoneBeige),
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("${enq.customerName} · ${enq.subject}", style = MaterialTheme.typography.titleSmall)
                  StatusPill(enq.status)
                }
                Text(enq.message, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  ENQUIRY_STATUSES.forEach { st ->
                    FilterChip(
                      selected = enq.status == st,
                      onClick = { onUpdateEnquiryStatus(enq.id, st) },
                      label = { Text(st) },
                    )
                  }
                }
              }
            }
          }
        }
      }

      AdminSection.STORE_SETTINGS -> {
        var shopName by remember(storeConfig) { mutableStateOf(storeConfig.shopName) }
        var tagline by remember(storeConfig) { mutableStateOf(storeConfig.tagline) }
        var heroHeadline by remember(storeConfig) { mutableStateOf(storeConfig.heroHeadline) }
        var heroSub by remember(storeConfig) { mutableStateOf(storeConfig.heroSubheadline) }
        var promo by remember(storeConfig) { mutableStateOf(storeConfig.promoBannerText) }
        var address by remember(storeConfig) { mutableStateOf(storeConfig.address) }
        var mobile by remember(storeConfig) { mutableStateOf(storeConfig.mobileNumber) }
        var whatsapp by remember(storeConfig) { mutableStateOf(storeConfig.whatsappNumber) }
        var email by remember(storeConfig) { mutableStateOf(storeConfig.email) }
        var mapsQuery by remember(storeConfig) { mutableStateOf(storeConfig.mapsQuery) }
        var hours by remember(storeConfig) { mutableStateOf(storeConfig.openingHours) }
        var insta by remember(storeConfig) { mutableStateOf(storeConfig.instagramUrl) }
        var fb by remember(storeConfig) { mutableStateOf(storeConfig.facebookUrl) }
        var seoTitle by remember(storeConfig) { mutableStateOf(storeConfig.seoTitle) }
        var seoDesc by remember(storeConfig) { mutableStateOf(storeConfig.seoDescription) }

        LazyColumn(
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          item {
            Card(
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = PureIvory),
              border = BorderStroke(1.dp, SandstoneBeige),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Storefront, Banners, Contact & SEO", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(value = shopName, onValueChange = { shopName = it }, label = { Text("Optical Shop Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tagline, onValueChange = { tagline = it }, label = { Text("Tagline") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = heroHeadline, onValueChange = { heroHeadline = it }, label = { Text("Hero Banner Headline") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = heroSub, onValueChange = { heroSub = it }, label = { Text("Hero Subheadline") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = promo, onValueChange = { promo = it }, label = { Text("Special Offer Banner Text") }, modifier = Modifier.fillMaxWidth())
                HorizontalDivider(color = SandstoneBeige)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Store Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = hours, onValueChange = { hours = it }, label = { Text("Opening Hours") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("Configurable WhatsApp Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = mapsQuery, onValueChange = { mapsQuery = it }, label = { Text("Google Maps Location / Query") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = insta, onValueChange = { insta = it }, label = { Text("Instagram URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fb, onValueChange = { fb = it }, label = { Text("Facebook URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                HorizontalDivider(color = SandstoneBeige)
                OutlinedTextField(value = seoTitle, onValueChange = { seoTitle = it }, label = { Text("SEO Meta Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = seoDesc, onValueChange = { seoDesc = it }, label = { Text("SEO Meta Description") }, modifier = Modifier.fillMaxWidth())

                Button(
                  onClick = {
                    onSaveStoreConfig(
                      storeConfig.copy(
                        shopName = shopName,
                        tagline = tagline,
                        heroHeadline = heroHeadline,
                        heroSubheadline = heroSub,
                        promoBannerText = promo,
                        address = address,
                        mobileNumber = mobile,
                        whatsappNumber = whatsapp,
                        email = email,
                        mapsQuery = mapsQuery,
                        openingHours = hours,
                        instagramUrl = insta,
                        facebookUrl = fb,
                        seoTitle = seoTitle,
                        seoDescription = seoDesc,
                      )
                    )
                  },
                  enabled = !isBusy,
                  modifier = Modifier.fillMaxWidth().height(50.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
                ) {
                  Text("Save Storefront & SEO Settings")
                }
              }
            }
          }

          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Testimonials (${testimonials.size})", style = MaterialTheme.typography.headlineMedium)
              OutlinedButton(onClick = { showTestimonialDialog = true }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Review")
              }
            }
          }

          items(testimonials, key = { "${it.id}_${it.customerName}" }) { rev ->
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = PureIvory),
              border = BorderStroke(1.dp, SandstoneBeige),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text("${rev.customerName} (${rev.rating} ★)", style = MaterialTheme.typography.titleSmall)
                  Text(rev.quote, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                }
                if (!rev.id.startsWith("starter_")) {
                  IconButton(onClick = { onDeleteTestimonial(rev.id) }) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete testimonial", tint = CrimsonAlert)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (showProductDialog) {
    ProductEditorDialog(
      initialProduct = editingProduct,
      onDismiss = { showProductDialog = false },
      onSave = { prod ->
        onSaveProduct(prod)
        showProductDialog = false
      },
    )
  }

  if (showTestimonialDialog) {
    TestimonialEditorDialog(
      onDismiss = { showTestimonialDialog = false },
      onSave = { rev ->
        onSaveTestimonial(rev)
        showTestimonialDialog = false
      },
    )
  }
}

@Composable
private fun KpiCard(
  label: String,
  value: String,
  sub: String,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = PureIvory),
    border = BorderStroke(1.dp, SandstoneBeige),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = DeepAntiqueGold)
      Spacer(modifier = Modifier.height(4.dp))
      Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = ObsidianBlack)
      Text(sub, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
    }
  }
}
