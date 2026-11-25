package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Appointment
import com.example.data.model.CartItem
import com.example.data.model.CustomerEnquiry
import com.example.data.model.DefaultStoreCatalog
import com.example.data.model.OpticalOrder
import com.example.data.model.Prescription
import com.example.data.model.Product
import com.example.data.model.ProductSortOption
import com.example.data.model.StoreConfig
import com.example.data.model.Testimonial
import com.example.data.repository.OpticalRepository
import com.example.util.formatCurrency
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

data class CatalogueFilterState(
  val searchQuery: String = "",
  val selectedCategory: String = "All",
  val selectedBrand: String = "All",
  val maxPrice: Float = 800f,
  val sortOption: ProductSortOption = ProductSortOption.POPULARITY,
)

class OpticalViewModel(
  private val repository: OpticalRepository,
  val currentUserId: String,
  val currentUserEmail: String,
  val currentUserName: String,
) : ViewModel() {

  private fun <T> Flow<T>.asUiState(label: String): StateFlow<UiState<T>> =
    map<T, UiState<T>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w("OpticalVM", "Error observing $label", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load $label"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading,
      )

  val isAdmin: StateFlow<Boolean> = repository.observeIsAdmin(currentUserId)
    .catch { error ->
      val firestoreError = generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
      if (firestoreError == null && error is CancellationException) throw error
      emit(false)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = false,
    )

  val storeConfig: StateFlow<StoreConfig> = repository.observeStoreConfig(currentUserId)
    .catch { error ->
      val firestoreError = generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
      if (firestoreError == null && error is CancellationException) throw error
      emit(DefaultStoreCatalog.defaultConfig)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = DefaultStoreCatalog.defaultConfig,
    )

  val productsState: StateFlow<UiState<List<Product>>> =
    repository.observeProducts(currentUserId).asUiState("products")

  val cloudProductsState: StateFlow<UiState<List<Product>>> =
    repository.observeCloudProductsOnly(currentUserId).asUiState("cloud products")

  val testimonialsState: StateFlow<UiState<List<Testimonial>>> =
    repository.observeTestimonials(currentUserId).asUiState("testimonials")

  val myAppointmentsState: StateFlow<UiState<List<Appointment>>> =
    repository.observeUserAppointments(currentUserId).asUiState("appointments")

  val myPrescriptionsState: StateFlow<UiState<List<Prescription>>> =
    repository.observeUserPrescriptions(currentUserId).asUiState("prescriptions")

  val myOrdersState: StateFlow<UiState<List<OpticalOrder>>> =
    repository.observeUserOrders(currentUserId).asUiState("orders")

  @OptIn(ExperimentalCoroutinesApi::class)
  val adminAppointmentsState: StateFlow<UiState<List<Appointment>>> =
    isAdmin.flatMapLatest { admin ->
      if (admin) repository.observeAllAppointments(currentUserId).map<List<Appointment>, UiState<List<Appointment>>> { UiState.Success(it) }
      else flowOf(UiState.Success(emptyList()))
    }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        emit(UiState.Error((firestoreError ?: error).message ?: "Failed to load admin appointments"))
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

  @OptIn(ExperimentalCoroutinesApi::class)
  val adminPrescriptionsState: StateFlow<UiState<List<Prescription>>> =
    isAdmin.flatMapLatest { admin ->
      if (admin) repository.observeAllPrescriptions(currentUserId).map<List<Prescription>, UiState<List<Prescription>>> { UiState.Success(it) }
      else flowOf(UiState.Success(emptyList()))
    }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        emit(UiState.Error((firestoreError ?: error).message ?: "Failed to load admin prescriptions"))
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

  @OptIn(ExperimentalCoroutinesApi::class)
  val adminOrdersState: StateFlow<UiState<List<OpticalOrder>>> =
    isAdmin.flatMapLatest { admin ->
      if (admin) repository.observeAllOrders(currentUserId).map<List<OpticalOrder>, UiState<List<OpticalOrder>>> { UiState.Success(it) }
      else flowOf(UiState.Success(emptyList()))
    }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        emit(UiState.Error((firestoreError ?: error).message ?: "Failed to load admin orders"))
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

  @OptIn(ExperimentalCoroutinesApi::class)
  val adminEnquiriesState: StateFlow<UiState<List<CustomerEnquiry>>> =
    isAdmin.flatMapLatest { admin ->
      if (admin) repository.observeAllEnquiries(currentUserId).map<List<CustomerEnquiry>, UiState<List<CustomerEnquiry>>> { UiState.Success(it) }
      else flowOf(UiState.Success(emptyList()))
    }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        emit(UiState.Error((firestoreError ?: error).message ?: "Failed to load enquiries"))
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

  // Catalogue Filters
  private val _filterState = MutableStateFlow(CatalogueFilterState())
  val filterState: StateFlow<CatalogueFilterState> = _filterState.asStateFlow()

  val filteredProducts: StateFlow<List<Product>> =
    combine(productsState, _filterState) { state, filter ->
      val products = (state as? UiState.Success)?.data ?: emptyList()
      val query = filter.searchQuery.trim().lowercase()
      val filtered = products.filter { p ->
        val matchesQuery = query.isEmpty() ||
          p.name.lowercase().contains(query) ||
          p.brand.lowercase().contains(query) ||
          p.sku.lowercase().contains(query) ||
          p.category.lowercase().contains(query) ||
          p.frameMaterial.lowercase().contains(query)
        val matchesCategory = filter.selectedCategory == "All" ||
          (filter.selectedCategory == "Eyeglasses" && p.category.contains("Eyeglasses")) ||
          (filter.selectedCategory == "SunglassesGroup" && p.category.contains("Sunglasses")) ||
          (filter.selectedCategory == "LensesGroup" && (p.category.contains("Lenses") || p.category.contains("Glasses"))) ||
          p.category == filter.selectedCategory
        val matchesBrand = filter.selectedBrand == "All" || p.brand == filter.selectedBrand
        val matchesPrice = p.finalPrice <= filter.maxPrice
        matchesQuery && matchesCategory && matchesBrand && matchesPrice
      }
      when (filter.sortOption) {
        ProductSortOption.POPULARITY -> filtered.sortedByDescending { it.popularityScore }
        ProductSortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.finalPrice }
        ProductSortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.finalPrice }
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  // Shopping Cart
  private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
  val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

  val cartCount: StateFlow<Int> = _cartItems
    .map { list -> list.sumOf { it.quantity } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

  val cartTotal: StateFlow<Double> = _cartItems
    .map { list -> list.sumOf { it.lineTotal } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

  private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val messages = _messages.asSharedFlow()

  private val _isBusy = MutableStateFlow(false)
  val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

  fun updateSearchQuery(q: String) = _filterState.update { it.copy(searchQuery = q) }
  fun selectCategory(category: String) = _filterState.update { it.copy(selectedCategory = category) }
  fun selectBrand(brand: String) = _filterState.update { it.copy(selectedBrand = brand) }
  fun updateMaxPrice(price: Float) = _filterState.update { it.copy(maxPrice = price) }
  fun selectSortOption(sort: ProductSortOption) = _filterState.update { it.copy(sortOption = sort) }
  fun resetFilters() {
    _filterState.value = CatalogueFilterState()
  }

  fun addToCart(product: Product) {
    if (!product.inStock) {
      _messages.tryEmit("${product.name} is currently out of stock.")
      return
    }
    _cartItems.update { current ->
      val existing = current.find { it.product.id == product.id }
      if (existing != null) {
        current.map {
          if (it.product.id == product.id) {
            val nextQty = (it.quantity + 1).coerceAtMost(product.stockCount.toInt().coerceAtLeast(1))
            it.copy(quantity = nextQty)
          } else it
        }
      } else {
        current + CartItem(product = product, quantity = 1)
      }
    }
    _messages.tryEmit("Added ${product.name} to bag")
  }

  fun updateCartQuantity(productId: String, delta: Int) {
    _cartItems.update { current ->
      current.mapNotNull { item ->
        if (item.product.id == productId) {
          val next = item.quantity + delta
          if (next <= 0) null
          else item.copy(quantity = next.coerceAtMost(item.product.stockCount.toInt().coerceAtLeast(1)))
        } else item
      }
    }
  }

  fun removeFromCart(productId: String) {
    _cartItems.update { current -> current.filterNot { it.product.id == productId } }
  }

  fun clearCart() {
    _cartItems.value = emptyList()
  }

  fun bookAppointment(
    fullName: String,
    mobileNumber: String,
    email: String,
    date: String,
    time: String,
    service: String,
    message: String,
    onSuccess: () -> Unit = {},
  ) {
    if (fullName.isBlank() || mobileNumber.trim().length < 5 || email.isBlank() || date.isBlank()) {
      _messages.tryEmit("Please fill in your name, mobile number, email, and preferred date.")
      return
    }
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.bookAppointment(
        Appointment(
          fullName = fullName,
          mobileNumber = mobileNumber,
          email = email,
          appointmentDate = date,
          preferredTime = time,
          serviceRequired = service,
          message = message,
        )
      )
      _isBusy.value = false
      res.onSuccess {
        _messages.tryEmit("Eye test appointment requested. We will confirm shortly.")
        onSuccess()
      }.onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to submit appointment")
      }
    }
  }

  fun cancelAppointment(appointmentId: String) {
    viewModelScope.launch {
      repository.cancelAppointment(appointmentId)
        .onSuccess { _messages.tryEmit("Appointment cancelled") }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Could not cancel appointment") }
    }
  }

  fun submitPrescription(rx: Prescription, onSuccess: () -> Unit = {}) {
    if (rx.customerName.isBlank() || rx.phoneNumber.trim().length < 5) {
      _messages.tryEmit("Please provide your name and valid phone number.")
      return
    }
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.submitPrescription(rx)
      _isBusy.value = false
      res.onSuccess {
        _messages.tryEmit("Private prescription submitted securely.")
        onSuccess()
      }.onFailure {
        _messages.tryEmit(it.localizedMessage ?: "Failed to submit prescription")
      }
    }
  }

  fun deletePrescription(id: String) {
    viewModelScope.launch {
      repository.deletePrescription(id)
        .onSuccess { _messages.tryEmit("Prescription record removed") }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Could not delete prescription") }
    }
  }

  fun submitOrder(
    customerName: String,
    customerPhone: String,
    customerEmail: String,
    deliveryAddress: String,
    orderNotes: String,
    onSuccess: (OpticalOrder) -> Unit = {},
  ) {
    val items = _cartItems.value
    if (items.isEmpty()) {
      _messages.tryEmit("Your bag is empty.")
      return
    }
    if (customerName.isBlank() || customerPhone.trim().length < 5 || deliveryAddress.trim().length < 5) {
      _messages.tryEmit("Please enter your name, phone number, and full delivery address.")
      return
    }
    val lines = items.map { "${it.product.name} x${it.quantity} (${formatCurrency(it.lineTotal)})" }
    val summary = lines.joinToString(", ")
    val total = items.sumOf { it.lineTotal }
    val draftOrder = OpticalOrder(
      customerName = customerName,
      customerPhone = customerPhone,
      customerEmail = customerEmail,
      deliveryAddress = deliveryAddress,
      itemsSummary = summary,
      itemLines = lines,
      totalAmount = total,
      orderNotes = orderNotes,
    )
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.submitOrder(draftOrder)
      _isBusy.value = false
      res.onSuccess { newId ->
        clearCart()
        _messages.tryEmit("Order enquiry submitted (#${newId.take(6).uppercase()}).")
        onSuccess(draftOrder.copy(id = newId))
      }.onFailure {
        _messages.tryEmit(it.localizedMessage ?: "Failed to submit order")
      }
    }
  }

  fun logEnquiry(
    customerName: String,
    phone: String,
    enquiryType: String,
    subject: String,
    message: String,
    onSuccess: () -> Unit = {},
  ) {
    if (customerName.isBlank() || subject.isBlank() || message.isBlank()) {
      _messages.tryEmit("Please enter your name, subject, and message.")
      return
    }
    viewModelScope.launch {
      repository.submitEnquiry(
        CustomerEnquiry(
          customerName = customerName,
          phone = phone,
          enquiryType = enquiryType,
          subject = subject,
          message = message,
        )
      ).onSuccess {
        _messages.tryEmit("Enquiry sent to boutique concierge.")
        onSuccess()
      }.onFailure {
        _messages.tryEmit(it.localizedMessage ?: "Failed to send enquiry")
      }
    }
  }

  // --- ADMIN ACTIONS ---

  fun seedStarterCatalogue() {
    viewModelScope.launch {
      _isBusy.value = true
      repository.seedStarterCatalogueIfEmpty()
        .onSuccess { count ->
          _messages.tryEmit(if (count > 0) "Published $count starter frames to Cloud Firestore" else "Cloud store already initialized")
        }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to initialize catalogue") }
      _isBusy.value = false
    }
  }

  fun saveProduct(product: Product, onSuccess: () -> Unit = {}) {
    if (product.name.isBlank() || product.brand.isBlank() || product.sku.isBlank() || product.description.isBlank()) {
      _messages.tryEmit("Name, brand, SKU, and description are required.")
      return
    }
    viewModelScope.launch {
      _isBusy.value = true
      repository.saveProduct(product)
        .onSuccess {
          _messages.tryEmit("Product saved")
          onSuccess()
        }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to save product") }
      _isBusy.value = false
    }
  }

  fun deleteProduct(productId: String) {
    viewModelScope.launch {
      repository.deleteProduct(productId)
        .onSuccess { _messages.tryEmit("Product deleted") }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to delete product") }
    }
  }

  fun saveStoreConfig(config: StoreConfig, onSuccess: () -> Unit = {}) {
    if (config.shopName.isBlank() || config.heroHeadline.isBlank() || config.address.isBlank() || config.whatsappNumber.isBlank()) {
      _messages.tryEmit("Shop name, hero headline, address, and WhatsApp number are required.")
      return
    }
    viewModelScope.launch {
      _isBusy.value = true
      repository.saveStoreConfig(config)
        .onSuccess {
          _messages.tryEmit("Store settings & SEO updated")
          onSuccess()
        }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to save store settings") }
      _isBusy.value = false
    }
  }

  fun saveTestimonial(testimonial: Testimonial, onSuccess: () -> Unit = {}) {
    if (testimonial.customerName.isBlank() || testimonial.quote.isBlank()) {
      _messages.tryEmit("Customer name and review quote are required.")
      return
    }
    viewModelScope.launch {
      repository.saveTestimonial(testimonial)
        .onSuccess {
          _messages.tryEmit("Testimonial saved")
          onSuccess()
        }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to save testimonial") }
    }
  }

  fun deleteTestimonial(id: String) {
    viewModelScope.launch {
      repository.deleteTestimonial(id)
        .onSuccess { _messages.tryEmit("Testimonial deleted") }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to delete testimonial") }
    }
  }

  fun updateOrderStatus(orderId: String, status: String) {
    viewModelScope.launch {
      repository.updateOrderStatus(orderId, status)
        .onSuccess { _messages.tryEmit("Order updated to $status") }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to update order") }
    }
  }

  fun updateAppointmentStatus(appointmentId: String, status: String, adminNotes: String) {
    viewModelScope.launch {
      repository.updateAppointmentStatus(appointmentId, status, adminNotes)
        .onSuccess { _messages.tryEmit("Appointment marked $status") }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to update appointment") }
    }
  }

  fun updatePrescriptionQuote(prescriptionId: String, status: String, quotedPrice: Double?, notes: String) {
    viewModelScope.launch {
      repository.updatePrescriptionQuote(prescriptionId, status, quotedPrice, notes)
        .onSuccess { _messages.tryEmit("Prescription updated ($status)") }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to update prescription") }
    }
  }

  fun updateEnquiryStatus(enquiryId: String, status: String) {
    viewModelScope.launch {
      repository.updateEnquiryStatus(enquiryId, status)
        .onSuccess { _messages.tryEmit("Enquiry marked $status") }
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to update enquiry") }
    }
  }
}
