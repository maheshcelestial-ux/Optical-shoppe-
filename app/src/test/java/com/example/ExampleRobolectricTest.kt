package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CartItem
import com.example.data.model.DefaultStoreCatalog
import com.example.data.model.OPTICAL_CATEGORIES
import com.example.data.model.Product
import com.example.util.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Atelier Optic", appName)
  }

  @Test
  fun `starter catalog covers all 11 required optical categories`() {
    val starterCats = DefaultStoreCatalog.starterProducts.map { it.category }.toSet()
    assertEquals(11, OPTICAL_CATEGORIES.size)
    assertTrue(starterCats.containsAll(OPTICAL_CATEGORIES))
  }

  @Test
  fun `product discount and cart line total calculate accurately`() {
    val product = Product(
      id = "p1",
      name = "Aurelia",
      price = 300.0,
      discountPercent = 15L,
      stockCount = 5L,
    )
    assertEquals(255.0, product.finalPrice, 0.01)
    val item = CartItem(product = product, quantity = 2)
    assertEquals(510.0, item.lineTotal, 0.01)
  }

  @Test
  fun `whatsapp prefilled messages contain product and shop details`() {
    val product = DefaultStoreCatalog.starterProducts.first()
    val msg = WhatsAppHelper.productEnquiryMessage("Atelier Optic", product)
    assertTrue(msg.contains("Atelier Optic"))
    assertTrue(msg.contains(product.sku))
  }
}
