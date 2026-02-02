package com.shjprofessionals.careride1.domain.model

/**
 * Represents available boost plans for doctors.
 * Doctors pay to appear higher in search results.
 */
data class BoostPlan(
    val id: String,
    val name: String,
    val description: String,
    val priceInCents: Int,
    val billingPeriod: BillingPeriod,
    val features: List<String>,
    val boostMultiplier: Float, // How much higher in results (e.g., 2.0 = 2x visibility)
    val isPopular: Boolean = false
) {
    val displayPrice: String
        get() = "$${priceInCents / 100}.${(priceInCents % 100).toString().padStart(2, '0')}"

    val billingDescription: String
        get() = when (billingPeriod) {
            BillingPeriod.MONTHLY -> "per month"
            BillingPeriod.YEARLY -> "per year"
        }

    val monthlyEquivalentCents: Int
        get() = when (billingPeriod) {
            BillingPeriod.MONTHLY -> priceInCents
            BillingPeriod.YEARLY -> priceInCents / 12
        }

    val monthlyEquivalentDisplay: String
        get() {
            val cents = monthlyEquivalentCents
            return "$${cents / 100}.${(cents % 100).toString().padStart(2, '0')}/mo"
        }
}

/**
 * Predefined boost plans for doctors
 */
object BoostPlans {

    val BASIC = BoostPlan(
        id = "boost_basic",
        name = "Basic Boost",
        description = "Get noticed by more patients",
        priceInCents = 4999, // $49.99/month
        billingPeriod = BillingPeriod.MONTHLY,
        features = listOf(
            "Featured badge on profile",
            "Priority in search results",
            "Basic analytics dashboard",
            "Cancel anytime"
        ),
        boostMultiplier = 1.5f,
        isPopular = false
    )

    val PRO = BoostPlan(
        id = "boost_pro",
        name = "Pro Boost",
        description = "Maximum visibility for your practice",
        priceInCents = 9999, // $99.99/month
        billingPeriod = BillingPeriod.MONTHLY,
        features = listOf(
            "Everything in Basic",
            "Top placement in search",
            "Advanced analytics",
            "Profile highlight color",
            "Priority support"
        ),
        boostMultiplier = 3.0f,
        isPopular = true
    )

    val ANNUAL = BoostPlan(
        id = "boost_annual",
        name = "Annual Pro",
        description = "Best value â€” 2 months free",
        priceInCents = 99999, // $999.99/year (~$83.33/mo)
        billingPeriod = BillingPeriod.YEARLY,
        features = listOf(
            "Everything in Pro Boost",
            "Save 17% vs monthly",
            "Dedicated account manager",
            "Custom profile badge"
        ),
        boostMultiplier = 3.0f,
        isPopular = false
    )

    val ALL = listOf(BASIC, PRO, ANNUAL)

    fun getById(id: String): BoostPlan? = ALL.find { it.id == id }
}
