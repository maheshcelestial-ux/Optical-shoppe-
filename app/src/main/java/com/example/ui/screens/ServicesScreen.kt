package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Send
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.APPOINTMENT_SERVICES
import com.example.data.model.APPOINTMENT_TIME_SLOTS
import com.example.data.model.Appointment
import com.example.data.model.LENS_TYPES
import com.example.data.model.Prescription
import com.example.data.model.StoreConfig
import com.example.ui.components.LuxuryEmptyState
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
import com.example.util.dialPhone
import com.example.util.formatCurrency
import com.example.util.openExternalUrl
import com.example.util.openGoogleMaps
import com.example.util.sendEmail
import java.util.Calendar

enum class ServicesTab(val label: String) {
  EYE_TEST("Eye Test"),
  PRESCRIPTION("Prescription"),
  BOUTIQUE("Boutique & SEO"),
}

@Composable
fun ServicesScreen(
  activeTab: ServicesTab,
  onSelectTab: (ServicesTab) -> Unit,
  storeConfig: StoreConfig,
  defaultName: String,
  defaultEmail: String,
  appointments: List<Appointment>,
  prescriptions: List<Prescription>,
  isBusy: Boolean,
  onBookAppointment: (String, String, String, String, String, String, String, () -> Unit) -> Unit,
  onCancelAppointment: (String) -> Unit,
  onSubmitPrescription: (Prescription, () -> Unit) -> Unit,
  onDeletePrescription: (String) -> Unit,
  onSubmitEnquiry: (String, String, String, String, String, () -> Unit) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AlabasterWhite),
  ) {
    TabRow(
      selectedTabIndex = activeTab.ordinal,
      containerColor = AlabasterWhite,
      contentColor = ObsidianBlack,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
          color = ChampagneGold,
        )
      },
    ) {
      ServicesTab.entries.forEach { tab ->
        Tab(
          selected = activeTab == tab,
          onClick = { onSelectTab(tab) },
          text = {
            Text(
              text = tab.label,
              style = MaterialTheme.typography.labelLarge,
              color = if (activeTab == tab) ObsidianBlack else MutedTaupe,
            )
          },
        )
      }
    }

    when (activeTab) {
      ServicesTab.EYE_TEST -> EyeTestBookingContent(
        storeConfig = storeConfig,
        defaultName = defaultName,
        defaultEmail = defaultEmail,
        appointments = appointments,
        isBusy = isBusy,
        onBookAppointment = onBookAppointment,
        onCancelAppointment = onCancelAppointment,
      )

      ServicesTab.PRESCRIPTION -> PrescriptionManagementContent(
        storeConfig = storeConfig,
        defaultName = defaultName,
        prescriptions = prescriptions,
        isBusy = isBusy,
        onSubmitPrescription = onSubmitPrescription,
        onDeletePrescription = onDeletePrescription,
      )

      ServicesTab.BOUTIQUE -> BoutiqueAndSeoContent(
        storeConfig = storeConfig,
        defaultName = defaultName,
        onSubmitEnquiry = onSubmitEnquiry,
      )
    }
  }
}

@Composable
private fun EyeTestBookingContent(
  storeConfig: StoreConfig,
  defaultName: String,
  defaultEmail: String,
  appointments: List<Appointment>,
  isBusy: Boolean,
  onBookAppointment: (String, String, String, String, String, String, String, () -> Unit) -> Unit,
  onCancelAppointment: (String) -> Unit,
) {
  val context = LocalContext.current
  var fullName by remember(defaultName) { mutableStateOf(defaultName) }
  var mobile by remember { mutableStateOf("") }
  var email by remember(defaultEmail) { mutableStateOf(defaultEmail) }
  var date by remember { mutableStateOf("2026-12-05") }
  var preferredTime by remember { mutableStateOf(APPOINTMENT_TIME_SLOTS.first()) }
  var selectedService by remember { mutableStateOf(APPOINTMENT_SERVICES.first()) }
  var message by remember { mutableStateOf("") }

  val calendar = Calendar.getInstance()
  val datePicker = remember {
    DatePickerDialog(
      context,
      { _, y, m, d ->
        date = String.format("%04d-%02d-%02d", y, m + 1, d)
      },
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(Calendar.DAY_OF_MONTH),
    )
  }

  LazyColumn(
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
          .clip(RoundedCornerShape(20.dp)),
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_eye_exam_studio),
          contentDescription = "Clinical eye exam suite",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack.copy(alpha = 0.55f))
            .padding(18.dp),
          contentAlignment = Alignment.BottomStart,
        ) {
          Column {
            Text(
              text = "CLINICAL OPTOMETRY SUITE",
              style = MaterialTheme.typography.labelSmall,
              color = ChampagneGold,
            )
            Text(
              text = "Book Your Eye Test",
              style = MaterialTheme.typography.headlineLarge,
              color = PureIvory,
            )
          }
        }
      }
    }

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
          Text("Select Service", style = MaterialTheme.typography.titleMedium, color = ObsidianBlack)
          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            APPOINTMENT_SERVICES.forEach { service ->
              FilterChip(
                selected = selectedService == service,
                onClick = { selectedService = service },
                label = { Text(service) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = ObsidianBlack,
                  selectedLabelColor = ChampagneGold,
                ),
              )
            }
          }

          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("appt_name_input"),
          )
          OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("appt_mobile_input"),
          )
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OutlinedTextField(
              value = date,
              onValueChange = { date = it },
              label = { Text("Appointment Date *") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            OutlinedButton(
              onClick = { datePicker.show() },
              modifier = Modifier.height(54.dp),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(Icons.Outlined.CalendarMonth, contentDescription = "Pick date")
              Spacer(modifier = Modifier.width(6.dp))
              Text("Pick")
            }
          }

          Text("Preferred Time", style = MaterialTheme.typography.titleSmall, color = ObsidianBlack)
          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            APPOINTMENT_TIME_SLOTS.forEach { slot ->
              FilterChip(
                selected = preferredTime == slot,
                onClick = { preferredTime = slot },
                label = { Text(slot) },
              )
            }
          }

          OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Notes / Vision Concerns (Optional)") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
          )

          Button(
            onClick = {
              onBookAppointment(fullName, mobile, email, date, preferredTime, selectedService, message) {
                message = ""
              }
            },
            enabled = !isBusy,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("submit_appointment_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
          ) {
            Text("Confirm Appointment Request")
          }

          OutlinedButton(
            onClick = {
              WhatsAppHelper.openWhatsApp(
                context,
                storeConfig.whatsappNumber,
                WhatsAppHelper.eyeTestBookingMessage(
                  storeConfig.shopName,
                  selectedService,
                  date,
                  preferredTime,
                  fullName.ifBlank { "Customer" },
                ),
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldVerified),
          ) {
            Icon(Icons.Outlined.Chat, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Quick Book via WhatsApp", color = EmeraldVerified)
          }
        }
      }
    }

    item {
      Text(
        text = "Your Scheduled Appointments",
        style = MaterialTheme.typography.headlineMedium,
        color = ObsidianBlack,
      )
    }

    if (appointments.isEmpty()) {
      item {
        LuxuryEmptyState(
          icon = Icons.Outlined.CalendarMonth,
          title = "No appointments booked yet",
          subtitle = "Submit the form above to reserve a private optometry consultation.",
        )
      }
    } else {
      items(appointments, key = { it.id }) { appt ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = PureIvory),
          border = BorderStroke(1.dp, SandstoneBeige),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(appt.serviceRequired, style = MaterialTheme.typography.titleMedium, color = ObsidianBlack)
              StatusPill(appt.status)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "${appt.appointmentDate} at ${appt.preferredTime}",
              style = MaterialTheme.typography.bodyMedium,
              color = DeepAntiqueGold,
            )
            if (appt.message.isNotBlank()) {
              Text(appt.message, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
            }
            if (appt.adminNotes.isNotBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              Surface(color = SoftGoldTint, shape = RoundedCornerShape(8.dp)) {
                Text(
                  text = "Optician Note: ${appt.adminNotes}",
                  style = MaterialTheme.typography.bodySmall,
                  color = ObsidianBlack,
                  modifier = Modifier.padding(8.dp),
                )
              }
            }
            if (appt.status == "Pending") {
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedButton(
                onClick = { onCancelAppointment(appt.id) },
                shape = RoundedCornerShape(10.dp),
              ) {
                Text("Cancel Request")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PrescriptionManagementContent(
  storeConfig: StoreConfig,
  defaultName: String,
  prescriptions: List<Prescription>,
  isBusy: Boolean,
  onSubmitPrescription: (Prescription, () -> Unit) -> Unit,
  onDeletePrescription: (String) -> Unit,
) {
  val context = LocalContext.current
  var customerName by remember(defaultName) { mutableStateOf(defaultName) }
  var phoneNumber by remember { mutableStateOf("") }
  var rightSph by remember { mutableStateOf("") }
  var rightCyl by remember { mutableStateOf("") }
  var rightAxis by remember { mutableStateOf("") }
  var leftSph by remember { mutableStateOf("") }
  var leftCyl by remember { mutableStateOf("") }
  var leftAxis by remember { mutableStateOf("") }
  var addPower by remember { mutableStateOf("") }
  var pd by remember { mutableStateOf("") }
  var selectedLens by remember { mutableStateOf(LENS_TYPES.first()) }
  var attachmentNote by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
    onResult = { uri ->
      if (uri != null) {
        attachmentNote = uri.lastPathSegment ?: "prescription_photo_attached.jpg"
      }
    },
  )

  LazyColumn(
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Surface(
        color = ObsidianBlack,
        shape = RoundedCornerShape(16.dp),
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.Outlined.Lock, contentDescription = null, tint = ChampagneGold)
          Column {
            Text(
              text = "Private Prescription Vault",
              style = MaterialTheme.typography.titleMedium,
              color = PureIvory,
            )
            Text(
              text = "Prescription records are strictly private and accessible only to your signed-in account and authorized optometrists.",
              style = MaterialTheme.typography.bodySmall,
              color = WarmBeige,
            )
          }
        }
      }
    }

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
          Text("Submit Prescription & Lens Enquiry", style = MaterialTheme.typography.titleLarge, color = ObsidianBlack)

          OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("rx_name_input"),
          )
          OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("rx_phone_input"),
          )

          Text("Right Eye (OD)", style = MaterialTheme.typography.titleSmall, color = DeepAntiqueGold)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = rightSph,
              onValueChange = { rightSph = it },
              label = { Text("SPH") },
              placeholder = { Text("-1.50") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
              value = rightCyl,
              onValueChange = { rightCyl = it },
              label = { Text("CYL") },
              placeholder = { Text("-0.50") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
              value = rightAxis,
              onValueChange = { rightAxis = it },
              label = { Text("AXIS") },
              placeholder = { Text("90") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
          }

          Text("Left Eye (OS)", style = MaterialTheme.typography.titleSmall, color = DeepAntiqueGold)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = leftSph,
              onValueChange = { leftSph = it },
              label = { Text("SPH") },
              placeholder = { Text("-1.75") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
              value = leftCyl,
              onValueChange = { leftCyl = it },
              label = { Text("CYL") },
              placeholder = { Text("-0.25") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
              value = leftAxis,
              onValueChange = { leftAxis = it },
              label = { Text("AXIS") },
              placeholder = { Text("85") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = addPower,
              onValueChange = { addPower = it },
              label = { Text("ADD (Bifocal/Prog)") },
              placeholder = { Text("+1.25") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
              value = pd,
              onValueChange = { pd = it },
              label = { Text("PD (mm)") },
              placeholder = { Text("62") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
          }

          Text("Lens Type Selection", style = MaterialTheme.typography.titleSmall, color = ObsidianBlack)
          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            LENS_TYPES.forEach { lens ->
              FilterChip(
                selected = selectedLens == lens,
                onClick = { selectedLens = lens },
                label = { Text(lens) },
              )
            }
          }

          OutlinedButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              if (attachmentNote.isBlank()) "Upload Prescription Photo (Photo Picker)"
              else "Attached: $attachmentNote"
            )
          }

          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Additional Notes (Optional)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
          )

          val draftRx = Prescription(
            customerName = customerName,
            phoneNumber = phoneNumber,
            rightSph = rightSph,
            rightCyl = rightCyl,
            rightAxis = rightAxis,
            leftSph = leftSph,
            leftCyl = leftCyl,
            leftAxis = leftAxis,
            addPower = addPower,
            pd = pd,
            lensType = selectedLens,
            attachmentNote = attachmentNote,
            notes = notes,
          )

          Button(
            onClick = {
              onSubmitPrescription(draftRx) {
                notes = ""
                attachmentNote = ""
              }
            },
            enabled = !isBusy,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("submit_prescription_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
          ) {
            Text("Save & Submit Private Prescription")
          }

          OutlinedButton(
            onClick = {
              WhatsAppHelper.openWhatsApp(
                context,
                storeConfig.whatsappNumber,
                WhatsAppHelper.prescriptionEnquiryMessage(storeConfig.shopName, draftRx),
              )
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldVerified),
          ) {
            Icon(Icons.Outlined.Chat, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Request WhatsApp Lens Quotation", color = EmeraldVerified)
          }
        }
      }
    }

    item {
      Text(
        text = "My Private Prescription Records",
        style = MaterialTheme.typography.headlineMedium,
        color = ObsidianBlack,
      )
    }

    if (prescriptions.isEmpty()) {
      item {
        LuxuryEmptyState(
          icon = Icons.Outlined.Description,
          title = "No saved prescriptions",
          subtitle = "Submit your optical prescription above for private storage and bespoke lens quotes.",
        )
      }
    } else {
      items(prescriptions, key = { it.id }) { rx ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = PureIvory),
          border = BorderStroke(1.dp, SandstoneBeige),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(rx.lensType, style = MaterialTheme.typography.titleMedium, color = ObsidianBlack, modifier = Modifier.weight(1f))
              StatusPill(rx.status)
              IconButton(onClick = { onDeletePrescription(rx.id) }) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete prescription", tint = MutedTaupe)
              }
            }
            Text(
              text = "OD: SPH ${rx.rightSph.ifBlank { "-" }} / CYL ${rx.rightCyl.ifBlank { "-" }} / AX ${rx.rightAxis.ifBlank { "-" }}",
              style = MaterialTheme.typography.bodySmall,
              color = ObsidianBlack,
            )
            Text(
              text = "OS: SPH ${rx.leftSph.ifBlank { "-" }} / CYL ${rx.leftCyl.ifBlank { "-" }} / AX ${rx.leftAxis.ifBlank { "-" }} · ADD ${rx.addPower.ifBlank { "-" }} · PD ${rx.pd.ifBlank { "-" }}",
              style = MaterialTheme.typography.bodySmall,
              color = ObsidianBlack,
            )
            if (rx.attachmentNote.isNotBlank()) {
              Text("Attachment: ${rx.attachmentNote}", style = MaterialTheme.typography.labelSmall, color = DeepAntiqueGold)
            }
            if (rx.quotedPrice != null) {
              Spacer(modifier = Modifier.height(6.dp))
              Surface(color = SoftGoldTint, shape = RoundedCornerShape(8.dp)) {
                Text(
                  text = "Optician Lens Quote: ${formatCurrency(rx.quotedPrice)}",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = ObsidianBlack,
                  modifier = Modifier.padding(8.dp),
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
              onClick = {
                WhatsAppHelper.openWhatsApp(
                  context,
                  storeConfig.whatsappNumber,
                  WhatsAppHelper.prescriptionEnquiryMessage(storeConfig.shopName, rx),
                )
              },
              shape = RoundedCornerShape(10.dp),
            ) {
              Icon(Icons.Outlined.Chat, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Discuss on WhatsApp", color = EmeraldVerified)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BoutiqueAndSeoContent(
  storeConfig: StoreConfig,
  defaultName: String,
  onSubmitEnquiry: (String, String, String, String, String, () -> Unit) -> Unit,
) {
  val context = LocalContext.current
  var enquiryName by remember(defaultName) { mutableStateOf(defaultName) }
  var enquiryPhone by remember { mutableStateOf("") }
  var enquirySubject by remember { mutableStateOf("") }
  var enquiryMessage by remember { mutableStateOf("") }

  LazyColumn(
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureIvory),
        border = BorderStroke(1.dp, SandstoneBeige),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("ABOUT ${storeConfig.shopName.uppercase()}", style = MaterialTheme.typography.labelSmall, color = DeepAntiqueGold)
          Text(storeConfig.tagline, style = MaterialTheme.typography.headlineMedium, color = ObsidianBlack)
          Text(
            text = "Address: ${storeConfig.address}",
            style = MaterialTheme.typography.bodyMedium,
            color = ObsidianBlack,
          )
          Text(
            text = "Hours: ${storeConfig.openingHours}",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedTaupe,
          )
          Text(
            text = "Phone: ${storeConfig.mobileNumber} · WhatsApp: ${storeConfig.whatsappNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = ObsidianBlack,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { openGoogleMaps(context, storeConfig.mapsQuery) },
              colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
              modifier = Modifier.weight(1f).height(48.dp),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Open Maps")
            }
            OutlinedButton(
              onClick = { dialPhone(context, storeConfig.mobileNumber) },
              modifier = Modifier.weight(1f).height(48.dp),
              shape = RoundedCornerShape(12.dp),
            ) {
              Text("Call Boutique")
            }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
              onClick = { openExternalUrl(context, storeConfig.instagramUrl) },
              modifier = Modifier.weight(1f),
            ) { Text("Instagram") }
            OutlinedButton(
              onClick = { openExternalUrl(context, storeConfig.facebookUrl) },
              modifier = Modifier.weight(1f),
            ) { Text("Facebook") }
            OutlinedButton(
              onClick = { sendEmail(context, storeConfig.email, "Optical Enquiry") },
              modifier = Modifier.weight(1f),
            ) { Text("Email") }
          }
        }
      }
    }

    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureIvory),
        border = BorderStroke(1.dp, SandstoneBeige),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Send Concierge Enquiry", style = MaterialTheme.typography.titleLarge, color = ObsidianBlack)
          OutlinedTextField(
            value = enquiryName,
            onValueChange = { enquiryName = it },
            label = { Text("Your Name *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = enquiryPhone,
            onValueChange = { enquiryPhone = it },
            label = { Text("Phone / WhatsApp") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = enquirySubject,
            onValueChange = { enquirySubject = it },
            label = { Text("Subject (e.g. Lens Quotation, Frame Fit) *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = enquiryMessage,
            onValueChange = { enquiryMessage = it },
            label = { Text("Message *") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
          )
          Button(
            onClick = {
              onSubmitEnquiry(enquiryName, enquiryPhone, "General", enquirySubject, enquiryMessage) {
                enquirySubject = ""
                enquiryMessage = ""
              }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = PureIvory),
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Submit Enquiry")
          }
        }
      }
    }

    item {
      Surface(
        color = WarmBeige,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SandstoneBeige),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("LOCAL SEO & STORE SCHEMA PROFILE", style = MaterialTheme.typography.labelSmall, color = DeepAntiqueGold)
          Text(storeConfig.seoTitle, style = MaterialTheme.typography.titleSmall, color = ObsidianBlack)
          Text(storeConfig.seoDescription, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
          HorizontalDivider(color = SandstoneBeige, modifier = Modifier.padding(vertical = 4.dp))
          Text(
            text = "Schema.org Type: OpticalShop · Google Rating: ${storeConfig.googleReviewRating}/5.0 (${storeConfig.googleReviewCount} reviews)",
            style = MaterialTheme.typography.labelSmall,
            color = ObsidianBlack,
          )
        }
      }
    }
  }
}
