package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.AuthScreen
import com.example.auth.signOut
import com.example.data.model.DefaultStoreCatalog
import com.example.data.model.Product
import com.example.data.repository.OpticalRepository
import com.example.ui.components.AtelierTopBar
import com.example.ui.components.WhatsAppFloatingButton
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CatalogueScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.ServicesTab
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.AtelierOpticTheme
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureIvory
import com.example.ui.theme.SoftGoldTint
import com.example.ui.viewmodel.OpticalViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.WhatsAppHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private enum class MainDestination(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
  CATALOGUE("catalogue", "Catalogue", Icons.Filled.Storefront, Icons.Outlined.Storefront),
  SERVICES("services", "Eye Care", Icons.Filled.MedicalServices, Icons.Outlined.MedicalServices),
  CART("cart", "Bag", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
  ADMIN("admin", "Admin", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings),
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AtelierOpticTheme {
        AppRoot()
      }
    }
  }
}

@Composable
fun AppRoot() {
  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose { Firebase.auth.removeAuthStateListener(listener) }
  }

  val user = currentUser
  if (user == null) {
    AuthScreen(onAuthSuccess = { currentUser = Firebase.auth.currentUser })
  } else {
    key(user.uid) {
      AtelierMainScaffold(
        currentUserId = user.uid,
        currentUserEmail = user.email ?: "",
        currentUserName = user.displayName ?: "",
      )
    }
  }
}

@Composable
private fun AtelierMainScaffold(
  currentUserId: String,
  currentUserEmail: String,
  currentUserName: String,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  val snackbarHostState = remember { SnackbarHostState() }

  val viewModel: OpticalViewModel = viewModel(
    key = currentUserId,
    factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY])
        val databaseId = app.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(databaseId)
        OpticalViewModel(
          repository = OpticalRepository(db),
          currentUserId = currentUserId,
          currentUserEmail = currentUserEmail,
          currentUserName = currentUserName,
        )
      }
    },
  )

  LaunchedEffect(viewModel) {
    viewModel.messages.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
  val storeConfig by viewModel.storeConfig.collectAsStateWithLifecycle()
  val productsState by viewModel.productsState.collectAsStateWithLifecycle()
  val cloudProductsState by viewModel.cloudProductsState.collectAsStateWithLifecycle()
  val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
  val filterState by viewModel.filterState.collectAsStateWithLifecycle()
  val testimonialsState by viewModel.testimonialsState.collectAsStateWithLifecycle()
  val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
  val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()
  val cartTotal by viewModel.cartTotal.collectAsStateWithLifecycle()
  val myOrdersState by viewModel.myOrdersState.collectAsStateWithLifecycle()
  val myAppointmentsState by viewModel.myAppointmentsState.collectAsStateWithLifecycle()
  val myPrescriptionsState by viewModel.myPrescriptionsState.collectAsStateWithLifecycle()
  val adminOrdersState by viewModel.adminOrdersState.collectAsStateWithLifecycle()
  val adminAppointmentsState by viewModel.adminAppointmentsState.collectAsStateWithLifecycle()
  val adminPrescriptionsState by viewModel.adminPrescriptionsState.collectAsStateWithLifecycle()
  val adminEnquiriesState by viewModel.adminEnquiriesState.collectAsStateWithLifecycle()
  val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

  val allProducts = (productsState as? UiState.Success)?.data ?: DefaultStoreCatalog.starterProducts
  val cloudProductsCount = (cloudProductsState as? UiState.Success)?.data?.size ?: 0
  val testimonials = (testimonialsState as? UiState.Success)?.data ?: DefaultStoreCatalog.starterTestimonials
  val myOrders = (myOrdersState as? UiState.Success)?.data ?: emptyList()
  val myAppointments = (myAppointmentsState as? UiState.Success)?.data ?: emptyList()
  val myPrescriptions = (myPrescriptionsState as? UiState.Success)?.data ?: emptyList()
  val adminOrders = (adminOrdersState as? UiState.Success)?.data ?: emptyList()
  val adminAppointments = (adminAppointmentsState as? UiState.Success)?.data ?: emptyList()
  val adminPrescriptions = (adminPrescriptionsState as? UiState.Success)?.data ?: emptyList()
  val adminEnquiries = (adminEnquiriesState as? UiState.Success)?.data ?: emptyList()

  var currentDestination by remember { mutableStateOf(MainDestination.HOME) }
  var activeServicesTab by remember { mutableStateOf(ServicesTab.EYE_TEST) }
  var selectedProduct by remember { mutableStateOf<Product?>(null) }

  if (currentDestination != MainDestination.HOME && selectedProduct == null) {
    BackHandler {
      currentDestination = MainDestination.HOME
    }
  }

  val handleSignOut: () -> Unit = {
    signOut(credentialManager = credentialManager, scope = scope)
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavRail = maxWidth >= 600.dp

    Scaffold(
      containerColor = AlabasterWhite,
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        AtelierTopBar(
          shopName = storeConfig.shopName,
          tagline = storeConfig.tagline,
          userEmail = currentUserEmail,
          onSignOutClick = handleSignOut,
        )
      },
      floatingActionButton = {
        if (currentDestination != MainDestination.ADMIN) {
          WhatsAppFloatingButton(
            onClick = {
              WhatsAppHelper.openWhatsApp(
                context,
                storeConfig.whatsappNumber,
                "Hello ${storeConfig.shopName}! I would like assistance with eyewear or booking an eye test.",
              )
            },
          )
        }
      },
      bottomBar = {
        if (!useNavRail) {
          NavigationBar(
            containerColor = PureIvory,
            tonalElevation = 4.dp,
          ) {
            MainDestination.entries.forEach { dest ->
              val selected = currentDestination == dest
              NavigationBarItem(
                selected = selected,
                onClick = { currentDestination = dest },
                modifier = Modifier.testTag("nav_${dest.route}"),
                icon = {
                  val iconVec = if (selected) dest.selectedIcon else dest.unselectedIcon
                  if (dest == MainDestination.CART && cartCount > 0) {
                    BadgedBox(badge = { Badge(containerColor = ChampagneGold, contentColor = ObsidianBlack) { Text("$cartCount") } }) {
                      Icon(iconVec, contentDescription = dest.label)
                    }
                  } else {
                    Icon(iconVec, contentDescription = dest.label)
                  }
                },
                label = { Text(dest.label) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = ObsidianBlack,
                  selectedTextColor = ObsidianBlack,
                  indicatorColor = SoftGoldTint,
                  unselectedIconColor = MutedTaupe,
                  unselectedTextColor = MutedTaupe,
                ),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
      ) {
        if (useNavRail) {
          NavigationRail(containerColor = PureIvory) {
            MainDestination.entries.forEach { dest ->
              val selected = currentDestination == dest
              NavigationRailItem(
                selected = selected,
                onClick = { currentDestination = dest },
                icon = {
                  val iconVec = if (selected) dest.selectedIcon else dest.unselectedIcon
                  if (dest == MainDestination.CART && cartCount > 0) {
                    BadgedBox(badge = { Badge(containerColor = ChampagneGold, contentColor = ObsidianBlack) { Text("$cartCount") } }) {
                      Icon(iconVec, contentDescription = dest.label)
                    }
                  } else {
                    Icon(iconVec, contentDescription = dest.label)
                  }
                },
                label = { Text(dest.label) },
                colors = NavigationRailItemDefaults.colors(
                  selectedIconColor = ObsidianBlack,
                  selectedTextColor = ObsidianBlack,
                  indicatorColor = SoftGoldTint,
                  unselectedIconColor = MutedTaupe,
                  unselectedTextColor = MutedTaupe,
                ),
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
          when (currentDestination) {
            MainDestination.HOME -> HomeScreen(
              storeConfig = storeConfig,
              products = allProducts,
              testimonials = testimonials,
              onNavigateToCatalogue = { category, brand ->
                viewModel.selectCategory(category)
                viewModel.selectBrand(brand)
                currentDestination = MainDestination.CATALOGUE
              },
              onNavigateToEyeTest = {
                activeServicesTab = ServicesTab.EYE_TEST
                currentDestination = MainDestination.SERVICES
              },
              onNavigateToPrescription = {
                activeServicesTab = ServicesTab.PRESCRIPTION
                currentDestination = MainDestination.SERVICES
              },
              onNavigateToAbout = {
                activeServicesTab = ServicesTab.BOUTIQUE
                currentDestination = MainDestination.SERVICES
              },
              onProductClick = { product ->
                selectedProduct = product
                currentDestination = MainDestination.CATALOGUE
              },
              onAddToCart = viewModel::addToCart,
            )

            MainDestination.CATALOGUE -> CatalogueScreen(
              storeConfig = storeConfig,
              products = filteredProducts,
              filterState = filterState,
              onSearchChange = viewModel::updateSearchQuery,
              onCategorySelect = viewModel::selectCategory,
              onBrandSelect = viewModel::selectBrand,
              onMaxPriceChange = viewModel::updateMaxPrice,
              onSortSelect = viewModel::selectSortOption,
              onResetFilters = viewModel::resetFilters,
              onAddToCart = viewModel::addToCart,
              selectedProduct = selectedProduct,
              onSelectProduct = { selectedProduct = it },
            )

            MainDestination.SERVICES -> ServicesScreen(
              activeTab = activeServicesTab,
              onSelectTab = { activeServicesTab = it },
              storeConfig = storeConfig,
              defaultName = currentUserName,
              defaultEmail = currentUserEmail,
              appointments = myAppointments,
              prescriptions = myPrescriptions,
              isBusy = isBusy,
              onBookAppointment = viewModel::bookAppointment,
              onCancelAppointment = viewModel::cancelAppointment,
              onSubmitPrescription = viewModel::submitPrescription,
              onDeletePrescription = viewModel::deletePrescription,
              onSubmitEnquiry = viewModel::logEnquiry,
            )

            MainDestination.CART -> CartScreen(
              storeConfig = storeConfig,
              cartItems = cartItems,
              cartTotal = cartTotal,
              orders = myOrders,
              defaultName = currentUserName,
              defaultEmail = currentUserEmail,
              isBusy = isBusy,
              onUpdateQuantity = viewModel::updateCartQuantity,
              onRemoveItem = viewModel::removeFromCart,
              onSubmitOrder = viewModel::submitOrder,
              onBrowseCatalogue = { currentDestination = MainDestination.CATALOGUE },
            )

            MainDestination.ADMIN -> AdminDashboardScreen(
              isAdmin = isAdmin,
              userEmail = currentUserEmail,
              storeConfig = storeConfig,
              products = allProducts,
              cloudProductsCount = cloudProductsCount,
              testimonials = testimonials,
              orders = adminOrders,
              appointments = adminAppointments,
              prescriptions = adminPrescriptions,
              enquiries = adminEnquiries,
              isBusy = isBusy,
              onSwitchAccount = handleSignOut,
              onSeedCatalogue = viewModel::seedStarterCatalogue,
              onSaveProduct = { viewModel.saveProduct(it) },
              onDeleteProduct = viewModel::deleteProduct,
              onUpdateOrderStatus = viewModel::updateOrderStatus,
              onUpdateAppointmentStatus = viewModel::updateAppointmentStatus,
              onUpdatePrescriptionQuote = viewModel::updatePrescriptionQuote,
              onUpdateEnquiryStatus = viewModel::updateEnquiryStatus,
              onSaveStoreConfig = { viewModel.saveStoreConfig(it) },
              onSaveTestimonial = { viewModel.saveTestimonial(it) },
              onDeleteTestimonial = viewModel::deleteTestimonial,
            )
          }
        }
      }
    }
  }
}
