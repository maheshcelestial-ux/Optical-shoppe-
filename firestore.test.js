const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const ADMIN_UID = "admin_uid_789";
const BOOTSTRAP_ADMIN_EMAIL = "mahesh.celestial@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function nowPast(offsetMs = 5000) {
  return new Date(Date.now() - offsetMs);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function adminContext(email = BOOTSTRAP_ADMIN_EMAIL, emailVerified = true) {
  return testEnv.authenticatedContext(ADMIN_UID, {
    email,
    email_verified: emailVerified,
  });
}

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").get());
  await assertFails(unauthDb.collection("store_config").doc("main").get());
  await assertFails(unauthDb.collection("appointments").get());
  await assertFails(unauthDb.collection("prescriptions").get());
  await assertFails(unauthDb.collection("orders").get());
});

test("Email spoofing test: unverified admin email is rejected", async () => {
  const spoofedAdminDb = adminContext(BOOTSTRAP_ADMIN_EMAIL, false).firestore();
  await assertFails(
    spoofedAdminDb.collection("store_config").doc("main").set({
      shopName: "Atelier Optic",
      heroHeadline: "See Better. Look Better. Live Better.",
      address: "128 Savile Row",
      mobileNumber: "+14155550199",
      whatsappNumber: "+14155550199",
      email: "concierge@atelieroptic.com",
      mapsQuery: "Atelier Optic Flagship",
      openingHours: "Mon-Sat 10:00-20:00",
      updatedAt: nowPast(),
    })
  );
});

test("Verified admin can write store_config, products, and testimonials; non-admin can only read", async () => {
  const adminDb = adminContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();

  await assertSucceeds(
    adminDb.collection("store_config").doc("main").set({
      shopName: "Atelier Optic",
      tagline: "Precision Craftsmanship",
      heroHeadline: "See Better. Look Better. Live Better.",
      heroSubheadline: "Handcrafted frames and bespoke optometry.",
      promoBannerText: "Complimentary Blue-Light Coating on First Pair",
      address: "420 Madison Ave, Suite 2B",
      mobileNumber: "+14155550199",
      whatsappNumber: "+14155550199",
      email: "concierge@atelieroptic.com",
      mapsQuery: "420 Madison Ave, New York",
      openingHours: "Mon-Sat 10:00 AM - 8:00 PM",
      instagramUrl: "https://instagram.com/atelieroptic",
      facebookUrl: "https://facebook.com/atelieroptic",
      seoTitle: "Atelier Optic | Luxury Eyewear & Optometry",
      seoDescription: "Handcrafted eyeglasses, sunglasses, and clinical eye exams.",
      googleReviewRating: 4.9,
      googleReviewCount: 312,
      updatedAt: nowPast(),
    })
  );

  await assertSucceeds(aliceDb.collection("store_config").doc("main").get());
  await assertFails(
    aliceDb.collection("store_config").doc("main").update({
      shopName: "Hacked Shop",
      updatedAt: nowPast(1000),
    })
  );

  const created = nowPast(10000);
  await assertSucceeds(
    adminDb.collection("products").doc("prod_1").set({
      name: "Aurelia Titanium",
      brand: "Atelier Signature",
      sku: "AO-101",
      category: "Men's Eyeglasses",
      price: 295.0,
      discountPercent: 10,
      description: "Japanese beta-titanium optical frame.",
      frameMaterial: "Japanese Titanium",
      frameColour: "Brushed Gold / Obsidian",
      frameShape: "Round",
      frameSize: "49-21-145",
      lensType: "Single Vision / Progressive Ready",
      stockCount: 14,
      popularityScore: 98,
      featured: true,
      bestSeller: true,
      newArrival: false,
      specialOffer: true,
      imageKey: "img_frames_optical",
      galleryKeys: ["img_frames_optical", "img_hero_eyewear"],
      seoSlug: "aurelia-titanium-round",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject product with @DocumentId "id" key in body
  await assertFails(
    adminDb.collection("products").doc("prod_2").set({
      id: "prod_2",
      name: "Bad Product",
      brand: "Atelier",
      sku: "AO-102",
      category: "Sunglasses",
      price: 200.0,
      discountPercent: 0,
      description: "Invalid ID field.",
      frameMaterial: "Acetate",
      frameColour: "Black",
      frameShape: "Square",
      frameSize: "52-18-145",
      lensType: "Polarized",
      stockCount: 5,
      popularityScore: 50,
      featured: false,
      bestSeller: false,
      newArrival: true,
      specialOffer: false,
      imageKey: "img_frames_sunglasses",
      galleryKeys: ["img_frames_sunglasses"],
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(aliceDb.collection("products").get());
  await assertFails(aliceDb.collection("products").doc("prod_1").delete());

  await assertSucceeds(
    adminDb.collection("testimonials").doc("rev_1").set({
      customerName: "Elena R.",
      rating: 5,
      quote: "The eye exam was thorough and the titanium frames feel weightless.",
      framePurchased: "Aurelia Titanium",
      verifiedGoogleReview: true,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(aliceDb.collection("testimonials").get());
});

test("Appointments: user isolation, status checks, and admin management", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = adminContext().firestore();
  const created = nowPast(10000);

  await assertSucceeds(
    aliceDb.collection("appointments").doc("appt_1").set({
      userId: ALICE_UID,
      fullName: "Alice Vance",
      mobileNumber: "+14155551001",
      email: "alice@example.com",
      appointmentDate: "2026-12-01",
      preferredTime: "11:00 AM",
      serviceRequired: "Eye examination",
      message: "Need annual checkup",
      status: "Pending",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Scoped query succeeds; unfiltered query fails for customer
  await assertSucceeds(aliceDb.collection("appointments").where("userId", "==", ALICE_UID).get());
  await assertFails(aliceDb.collection("appointments").get());

  // Cross-user read & write denied
  await assertFails(bobDb.collection("appointments").doc("appt_1").get());
  await assertFails(bobDb.collection("appointments").where("userId", "==", ALICE_UID).get());

  // Customer cannot confirm own appointment, only cancel Pending
  await assertFails(
    aliceDb.collection("appointments").doc("appt_1").update({
      status: "Confirmed",
      updatedAt: nowPast(1000),
    })
  );

  // Admin can list all and confirm
  await assertSucceeds(adminDb.collection("appointments").get());
  await assertSucceeds(
    adminDb.collection("appointments").doc("appt_1").update({
      status: "Confirmed",
      adminNotes: "Assigned to Dr. Meyer",
      updatedAt: nowPast(1000),
    })
  );
});

test("Prescriptions: strict medical privacy and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = adminContext().firestore();
  const created = nowPast(10000);

  await assertSucceeds(
    aliceDb.collection("prescriptions").doc("rx_1").set({
      userId: ALICE_UID,
      customerName: "Alice Vance",
      phoneNumber: "+14155551001",
      rightSph: "-1.75",
      rightCyl: "-0.50",
      rightAxis: "90",
      leftSph: "-2.00",
      leftCyl: "-0.25",
      leftAxis: "85",
      addPower: "+1.25",
      pd: "62",
      lensType: "Progressive Lenses",
      attachmentNote: "rx_scan_2026.jpg",
      notes: "Prefer ultra-thin 1.67 index",
      status: "Submitted",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Bob cannot read or list Alice's private prescriptions
  await assertFails(bobDb.collection("prescriptions").doc("rx_1").get());
  await assertFails(bobDb.collection("prescriptions").get());
  await assertSucceeds(aliceDb.collection("prescriptions").where("userId", "==", ALICE_UID).get());

  // Customer cannot self-quote
  await assertFails(
    aliceDb.collection("prescriptions").doc("rx_1").update({
      status: "Quoted",
      quotedPrice: 1.0,
      updatedAt: nowPast(1000),
    })
  );

  // Admin can review and quote
  await assertSucceeds(
    adminDb.collection("prescriptions").doc("rx_1").update({
      status: "Quoted",
      quotedPrice: 240.0,
      notes: "Zeiss 1.67 progressive anti-reflective quote",
      updatedAt: nowPast(1000),
    })
  );
});

test("Orders & Enquiries: owner create Pending/Open, admin updates status", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = adminContext().firestore();
  const created = nowPast(10000);

  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_1").set({
      userId: ALICE_UID,
      customerName: "Alice Vance",
      customerPhone: "+14155551001",
      customerEmail: "alice@example.com",
      deliveryAddress: "742 Evergreen Terrace, Apt 4",
      itemsSummary: "1x Aurelia Titanium ($265.50)",
      itemLines: ["Aurelia Titanium x1 - $265.50"],
      totalAmount: 265.5,
      orderNotes: "Gift box requested",
      paymentMethod: "Enquiry & Pay at Boutique / Cash on Delivery",
      status: "Pending",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject creating order with pre-confirmed status
  await assertFails(
    aliceDb.collection("orders").doc("ord_2").set({
      userId: ALICE_UID,
      customerName: "Alice Vance",
      customerPhone: "+14155551001",
      deliveryAddress: "742 Evergreen Terrace",
      itemsSummary: "1x Aurelia Titanium",
      itemLines: ["Aurelia Titanium x1"],
      totalAmount: 265.5,
      paymentMethod: "Enquiry & Pay at Boutique / Cash on Delivery",
      status: "Delivered",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(bobDb.collection("orders").doc("ord_1").get());
  await assertSucceeds(aliceDb.collection("orders").where("userId", "==", ALICE_UID).get());
  await assertSucceeds(
    adminDb.collection("orders").doc("ord_1").update({
      status: "Confirmed",
      updatedAt: nowPast(1000),
    })
  );

  await assertSucceeds(
    aliceDb.collection("enquiries").doc("enq_1").set({
      userId: ALICE_UID,
      customerName: "Alice Vance",
      phone: "+14155551001",
      enquiryType: "Product Enquiry",
      subject: "Aurelia Titanium Nose Pads",
      message: "Are silicone nose pads replaceable?",
      status: "Open",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertFails(bobDb.collection("enquiries").doc("enq_1").get());
  await assertSucceeds(
    adminDb.collection("enquiries").doc("enq_1").update({
      status: "Replied",
      updatedAt: nowPast(1000),
    })
  );
});
