package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Appointment
import com.example.data.model.CustomerEnquiry
import com.example.data.model.DefaultStoreCatalog
import com.example.data.model.OpticalOrder
import com.example.data.model.Prescription
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.data.model.Testimonial
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val BOOTSTRAP_ADMIN_EMAIL = "mahesh.celestial@gmail.com"

class OpticalRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  private fun isBootstrapAdmin(): Boolean {
    val user = auth.currentUser ?: return false
    return user.isEmailVerified && user.email?.equals(BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true) == true
  }

  private fun <T> Flow<T>.withFirestoreLogging(
    operationType: OperationType,
    path: String,
    expectedUid: String,
  ): Flow<T> = catch { error ->
    val firestoreError = generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    val exceptionToLog = firestoreError ?: (error as? Exception)
    if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
      handleFirestoreError(exceptionToLog, operationType, path)
    }
    throw error
  }

  fun observeIsAdmin(uid: String): Flow<Boolean> = flow {
    if (isBootstrapAdmin()) {
      emit(true)
      return@flow
    }
    val path = "admins/$uid"
    emitAll(
      db.collection("admins").document(uid)
        .snapshots()
        .map { snapshot -> snapshot.exists() || isBootstrapAdmin() }
        .withFirestoreLogging(OperationType.GET, path, uid)
    )
  }

  fun observeStoreConfig(uid: String): Flow<StoreConfig> {
    val path = "store_config/main"
    return db.collection("store_config").document("main")
      .snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          snapshot.toObject(StoreConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?: DefaultStoreCatalog.defaultConfig
        } else {
          DefaultStoreCatalog.defaultConfig
        }
      }
      .withFirestoreLogging(OperationType.GET, path, uid)
  }

  fun observeProducts(uid: String): Flow<List<Product>> {
    val path = "products"
    return db.collection("products")
      .snapshots()
      .map { snapshot ->
        val items = snapshot.toObjects(
          Product::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        )
        if (items.isEmpty()) DefaultStoreCatalog.starterProducts
        else items.sortedByDescending { it.popularityScore }
      }
      .withFirestoreLogging(OperationType.LIST, path, uid)
  }

  fun observeCloudProductsOnly(uid: String): Flow<List<Product>> {
    val path = "products"
    return db.collection("products")
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          Product::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      }
      .withFirestoreLogging(OperationType.LIST, path, uid)
  }

  fun observeTestimonials(uid: String): Flow<List<Testimonial>> {
    val path = "testimonials"
    return db.collection("testimonials")
      .snapshots()
      .map { snapshot ->
        val items = snapshot.toObjects(
          Testimonial::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        )
        if (items.isEmpty()) DefaultStoreCatalog.starterTestimonials
        else items.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      }
      .withFirestoreLogging(OperationType.LIST, path, uid)
  }

  fun observeUserAppointments(uid: String): Flow<List<Appointment>> {
    val path = "appointments"
    return db.collection("appointments")
      .whereEqualTo("userId", uid)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          Appointment::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      }
      .withFirestoreLogging(OperationType.LIST, path, uid)
  }

  fun observeUserPrescriptions(uid: String): Flow<List<Prescription>> {
    val path = "prescriptions"
    return db.collection("prescriptions")
      .whereEqualTo("userId", uid)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          Prescription::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      }
      .withFirestoreLogging(OperationType.LIST, path, uid)
  }

  fun observeUserOrders(uid: String): Flow<List<OpticalOrder>> {
    val path = "orders"
    return db.collection("orders")
      .whereEqualTo("userId", uid)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          OpticalOrder::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      }
      .withFirestoreLogging(OperationType.LIST, path, uid)
  }

  fun observeAllAppointments(uid: String): Flow<List<Appointment>> =
    observeAdminCollection("appointments", uid, Appointment::class.java) { it.createdAt ?: Timestamp(0, 0) }

  fun observeAllPrescriptions(uid: String): Flow<List<Prescription>> =
    observeAdminCollection("prescriptions", uid, Prescription::class.java) { it.createdAt ?: Timestamp(0, 0) }

  fun observeAllOrders(uid: String): Flow<List<OpticalOrder>> =
    observeAdminCollection("orders", uid, OpticalOrder::class.java) { it.createdAt ?: Timestamp(0, 0) }

  fun observeAllEnquiries(uid: String): Flow<List<CustomerEnquiry>> =
    observeAdminCollection("enquiries", uid, CustomerEnquiry::class.java) { it.createdAt ?: Timestamp(0, 0) }

  private fun <T> observeAdminCollection(
    collection: String,
    uid: String,
    clazz: Class<T>,
    sortKey: (T) -> Timestamp,
  ): Flow<List<T>> {
    return db.collection(collection)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(clazz, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending(sortKey)
      }
      .withFirestoreLogging(OperationType.LIST, collection, uid)
  }

  suspend fun seedStarterCatalogueIfEmpty(): Result<Int> {
    return try {
      requireUserId()
      val existingConfig = db.collection("store_config").document("main").get().await()
      if (!existingConfig.exists()) {
        saveStoreConfig(DefaultStoreCatalog.defaultConfig).getOrThrow()
      }
      val existingProducts = db.collection("products").limit(1).get().await()
      var addedCount = 0
      if (existingProducts.isEmpty) {
        DefaultStoreCatalog.starterProducts.forEach { product ->
          saveProduct(product.copy(id = "")).getOrThrow()
          addedCount++
        }
      }
      val existingReviews = db.collection("testimonials").limit(1).get().await()
      if (existingReviews.isEmpty) {
        DefaultStoreCatalog.starterTestimonials.forEach { review ->
          saveTestimonial(review.copy(id = "")).getOrThrow()
        }
      }
      Result.success(addedCount)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "products")
      Result.failure(e)
    }
  }

  suspend fun bookAppointment(appointment: Appointment): Result<Unit> {
    var docPath = "appointments"
    return try {
      val uid = requireUserId()
      val docRef = db.collection("appointments").document()
      docPath = docRef.path
      val payload = mapOf(
        "userId" to uid,
        "fullName" to appointment.fullName.trim().take(100),
        "mobileNumber" to appointment.mobileNumber.trim().take(30),
        "email" to appointment.email.trim().take(120),
        "appointmentDate" to appointment.appointmentDate.trim().take(40),
        "preferredTime" to appointment.preferredTime.trim().take(40),
        "serviceRequired" to appointment.serviceRequired,
        "message" to appointment.message.trim().take(500),
        "status" to "Pending",
        "adminNotes" to "",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun cancelAppointment(appointmentId: String): Result<Unit> {
    val docPath = "appointments/$appointmentId"
    return try {
      requireUserId()
      db.document(docPath).update(
        mapOf(
          "status" to "Cancelled",
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun submitPrescription(rx: Prescription): Result<Unit> {
    var docPath = "prescriptions"
    return try {
      val uid = requireUserId()
      val docRef = db.collection("prescriptions").document()
      docPath = docRef.path
      val payload = mapOf(
        "userId" to uid,
        "customerName" to rx.customerName.trim().take(100),
        "phoneNumber" to rx.phoneNumber.trim().take(30),
        "rightSph" to rx.rightSph.trim().take(20),
        "rightCyl" to rx.rightCyl.trim().take(20),
        "rightAxis" to rx.rightAxis.trim().take(20),
        "leftSph" to rx.leftSph.trim().take(20),
        "leftCyl" to rx.leftCyl.trim().take(20),
        "leftAxis" to rx.leftAxis.trim().take(20),
        "addPower" to rx.addPower.trim().take(20),
        "pd" to rx.pd.trim().take(20),
        "lensType" to rx.lensType.trim().take(80),
        "attachmentNote" to rx.attachmentNote.trim().take(240),
        "notes" to rx.notes.trim().take(500),
        "status" to "Submitted",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun deletePrescription(prescriptionId: String): Result<Unit> {
    val docPath = "prescriptions/$prescriptionId"
    return try {
      requireUserId()
      db.document(docPath).delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun submitOrder(order: OpticalOrder): Result<String> {
    var docPath = "orders"
    return try {
      val uid = requireUserId()
      val docRef = db.collection("orders").document()
      docPath = docRef.path
      val payload = mapOf(
        "userId" to uid,
        "customerName" to order.customerName.trim().take(100),
        "customerPhone" to order.customerPhone.trim().take(30),
        "customerEmail" to order.customerEmail.trim().take(120),
        "deliveryAddress" to order.deliveryAddress.trim().take(300),
        "itemsSummary" to order.itemsSummary.trim().take(1000),
        "itemLines" to order.itemLines.take(30),
        "totalAmount" to order.totalAmount.coerceAtLeast(0.0),
        "orderNotes" to order.orderNotes.trim().take(400),
        "paymentMethod" to order.paymentMethod.trim().take(60),
        "status" to "Pending",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun submitEnquiry(enquiry: CustomerEnquiry): Result<Unit> {
    var docPath = "enquiries"
    return try {
      val uid = requireUserId()
      val docRef = db.collection("enquiries").document()
      docPath = docRef.path
      val payload = mapOf(
        "userId" to uid,
        "customerName" to enquiry.customerName.trim().take(100),
        "phone" to enquiry.phone.trim().take(30),
        "enquiryType" to enquiry.enquiryType.trim().take(60),
        "subject" to enquiry.subject.trim().take(160),
        "message" to enquiry.message.trim().take(800),
        "status" to "Open",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  // --- ADMIN OPERATIONS ---

  suspend fun saveStoreConfig(config: StoreConfig): Result<Unit> {
    val docPath = "store_config/main"
    return try {
      requireUserId()
      val payload = mapOf(
        "shopName" to config.shopName.trim().take(80),
        "tagline" to config.tagline.trim().take(160),
        "heroHeadline" to config.heroHeadline.trim().take(120),
        "heroSubheadline" to config.heroSubheadline.trim().take(240),
        "promoBannerText" to config.promoBannerText.trim().take(160),
        "address" to config.address.trim().take(240),
        "mobileNumber" to config.mobileNumber.trim().take(40),
        "whatsappNumber" to config.whatsappNumber.trim().take(40),
        "email" to config.email.trim().take(120),
        "mapsQuery" to config.mapsQuery.trim().take(200),
        "openingHours" to config.openingHours.trim().take(200),
        "instagramUrl" to config.instagramUrl.trim().take(240),
        "facebookUrl" to config.facebookUrl.trim().take(240),
        "seoTitle" to config.seoTitle.trim().take(120),
        "seoDescription" to config.seoDescription.trim().take(300),
        "googleReviewRating" to config.googleReviewRating.coerceIn(0.0, 5.0),
        "googleReviewCount" to config.googleReviewCount.coerceAtLeast(0L),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      db.document(docPath).set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docPath)
      Result.failure(e)
    }
  }

  suspend fun saveProduct(product: Product): Result<Unit> {
    val isCreate = product.id.isBlank() || product.id.startsWith("starter_")
    val docRef = if (isCreate) {
      db.collection("products").document()
    } else {
      db.collection("products").document(product.id)
    }
    return try {
      requireUserId()
      val cleanImageUri = product.imageUri?.trim()?.takeIf { it.isNotEmpty() }?.take(2000)
      val gallery = product.galleryKeys.ifEmpty { listOf(product.imageKey.ifBlank { "img_frames_optical" }) }.take(6)
      val baseMap = mutableMapOf<String, Any?>(
        "name" to product.name.trim().take(120),
        "brand" to product.brand.trim().take(80),
        "sku" to product.sku.trim().take(40),
        "category" to product.category.trim().take(60),
        "price" to product.price.coerceIn(0.0, 1000000.0),
        "discountPercent" to product.discountPercent.coerceIn(0L, 90L),
        "description" to product.description.trim().take(1000),
        "frameMaterial" to product.frameMaterial.trim().take(80),
        "frameColour" to product.frameColour.trim().take(80),
        "frameShape" to product.frameShape.trim().take(80),
        "frameSize" to product.frameSize.trim().take(40),
        "lensType" to product.lensType.trim().take(80),
        "stockCount" to product.stockCount.coerceIn(0L, 100000L),
        "popularityScore" to product.popularityScore.coerceAtLeast(0L),
        "featured" to product.featured,
        "bestSeller" to product.bestSeller,
        "newArrival" to product.newArrival,
        "specialOffer" to product.specialOffer,
        "imageKey" to product.imageKey.ifBlank { "img_frames_optical" }.take(80),
        "imageUri" to cleanImageUri,
        "galleryKeys" to gallery,
        "seoSlug" to product.seoSlug.trim().take(120),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      if (isCreate) {
        baseMap["createdAt"] = FieldValue.serverTimestamp()
        @Suppress("UNCHECKED_CAST")
        docRef.set(baseMap.filterValues { it != null } as Map<String, Any>).await()
      } else {
        @Suppress("UNCHECKED_CAST")
        docRef.update(baseMap.filterValues { it != null } as Map<String, Any>).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteProduct(productId: String): Result<Unit> {
    val docPath = "products/$productId"
    return try {
      requireUserId()
      db.document(docPath).delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun saveTestimonial(testimonial: Testimonial): Result<Unit> {
    val isCreate = testimonial.id.isBlank() || testimonial.id.startsWith("starter_")
    val docRef = if (isCreate) {
      db.collection("testimonials").document()
    } else {
      db.collection("testimonials").document(testimonial.id)
    }
    return try {
      requireUserId()
      val baseMap = mutableMapOf<String, Any>(
        "customerName" to testimonial.customerName.trim().take(80),
        "rating" to testimonial.rating.coerceIn(1L, 5L),
        "quote" to testimonial.quote.trim().take(500),
        "framePurchased" to testimonial.framePurchased.trim().take(120),
        "verifiedGoogleReview" to testimonial.verifiedGoogleReview,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      if (isCreate) {
        baseMap["createdAt"] = FieldValue.serverTimestamp()
        docRef.set(baseMap).await()
      } else {
        docRef.update(baseMap).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteTestimonial(testimonialId: String): Result<Unit> {
    val docPath = "testimonials/$testimonialId"
    return try {
      requireUserId()
      db.document(docPath).delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateOrderStatus(orderId: String, status: String, notes: String? = null): Result<Unit> {
    val docPath = "orders/$orderId"
    return try {
      requireUserId()
      val updates = mutableMapOf<String, Any?>(
        "status" to status,
        "orderNotes" to notes?.trim()?.take(400),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      @Suppress("UNCHECKED_CAST")
      db.document(docPath).update(updates.filterValues { it != null } as Map<String, Any>).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateAppointmentStatus(appointmentId: String, status: String, adminNotes: String): Result<Unit> {
    val docPath = "appointments/$appointmentId"
    return try {
      requireUserId()
      db.document(docPath).update(
        mapOf(
          "status" to status,
          "adminNotes" to adminNotes.trim().take(400),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updatePrescriptionQuote(
    prescriptionId: String,
    status: String,
    quotedPrice: Double?,
    notes: String,
  ): Result<Unit> {
    val docPath = "prescriptions/$prescriptionId"
    return try {
      requireUserId()
      val updates = mutableMapOf<String, Any?>(
        "status" to status,
        "quotedPrice" to quotedPrice?.coerceIn(0.0, 1000000.0),
        "notes" to notes.trim().take(500),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      @Suppress("UNCHECKED_CAST")
      db.document(docPath).update(updates.filterValues { it != null } as Map<String, Any>).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateEnquiryStatus(enquiryId: String, status: String): Result<Unit> {
    val docPath = "enquiries/$enquiryId"
    return try {
      requireUserId()
      db.document(docPath).update(
        mapOf(
          "status" to status,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }
}
