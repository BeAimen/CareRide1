package com.shjprofessionals.careride1.domain.model

/**
 * Represents available subscription plans for patients.
 * Pricing is in cents to avoid floating point issues.
 */
data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String,
    val priceInCents: Int,
    val billingPeriod: BillingPeriod,
    val features: List<String>,
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

enum class BillingPeriod(val months: Int) {
    MONTHLY(1),
    YEARLY(12)
}

/**
 * Predefined subscription plans
 */
object SubscriptionPlans {

    val MONTHLY = SubscriptionPlan(
        id = "plan_monthly",
        name = "Monthly",
        description = "Flexible month-to-month access",
        priceInCents = 1999, // $19.99
        billingPeriod = BillingPeriod.MONTHLY,
        features = listOf(
            "Message any doctor",
            "Unlimited conversations",
            "24-hour response guarantee",
            "Cancel anytime"
        ),
        isPopular = false
    )

    val YEARLY = SubscriptionPlan(
        id = "plan_yearly",
        name = "Annual",
        description = "Best value â€” save 33%",
        priceInCents = 15999, // $159.99 ($13.33/mo)
        billingPeriod = BillingPeriod.YEARLY,
        features = listOf(
            "Everything in Monthly",
            "Save 33% vs monthly",
            "Priority support",
            "Cancel anytime"
        ),
        isPopular = true
    )

    val ALL = listOf(MONTHLY, YEARLY)

    fun getById(id: String): SubscriptionPlan? = ALL.find { it.id == id }
}
