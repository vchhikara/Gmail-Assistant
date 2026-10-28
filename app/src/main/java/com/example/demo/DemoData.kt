package com.example.demo

import com.example.classify.Classifier
import com.example.data.AppDatabase
import com.example.data.EmailStore
import com.example.data.ServerEmail
import com.example.gmail.parseFrom
import com.example.gmail.toMeta

/** Sample inbox for the explicit "Try demo" mode. Rows carry the [PREFIX] id prefix and never reach Gmail. */
object DemoData {
    const val PREFIX = "demo_"

    suspend fun load(db: AppDatabase, classifier: Classifier) {
        val now = System.currentTimeMillis()
        val items = build(now)
        EmailStore(db).merge(items)
        items.forEach {
            val c = classifier.classify(it.toMeta())
            db.emailDao().updateClassification(it.id, c.category, c.isClutter, c.isUnsubscribeSuggested)
        }
    }

    suspend fun clear(db: AppDatabase) = db.emailDao().deleteDemo()

    private fun build(now: Long): List<ServerEmail> {
        val raw = listOf(
                    Triple(
                        "demo_msg_001",
                        "Stripe Billing <billing@stripe.com>",
                        "Your premium subscription renewed"
                    ) to Pair("Your statement of $29.00 USD is ready for standard download. Your next payment is scheduled for June 28, 2026.", now - 5 * 60 * 1000), // 5 min ago
                    
                    Triple(
                        "demo_msg_002",
                        "Sarah Connor <sarah.c@cyberdyne.org>",
                        "Urgent feedback: Smart Gmail App architecture"
                    ) to Pair("Hey developer! The architecture look very clean. Let's schedule a call to secure our Gemini api integration.", now - 18 * 60 * 1000), // 18 min ago
        
                    Triple(
                        "demo_msg_003",
                        "Substack Weekly <newsletter@substack.com>",
                        "Inside the Mind of the Antigravity Agent"
                    ) to Pair("Welcome to this week's newsletter! Today we explore autonomous programming in Kotlin and dynamic Gemini model alias resolution.", now - 45 * 60 * 1000), // 45 min ago
        
                    Triple(
                        "demo_msg_004",
                        "LinkedIn <notifications@linkedin.com>",
                        "John Doe viewed your profile and sent a message"
                    ) to Pair("Congratulations! John Doe and 5 other recruiters from Google DeepMind viewed your profile. Click here to read his message.", now - 120 * 60 * 1000),
        
                    Triple(
                        "demo_msg_005",
                        "Amazon Services <updates@amazon.com>",
                        "Your package is delivered! (Smart Coffee Warmer)"
                    ) to Pair("Delivered: Your package was handed to a resident or left in an eye-safe spot. Rate your delivery experience on Amazon.", now - 180 * 60 * 1000),
        
                    Triple(
                        "demo_msg_006",
                        "GitHub Notifications <git@github.com>",
                        "[Pull Request] #142 Merged: Room local DB integration"
                    ) to Pair("Success: Developer merged pull request #142 into main. Check Android Studio logs for detailed compilation feedback.", now - 240 * 60 * 1000),
        
                    Triple(
                        "demo_msg_007",
                        "Comcast Xfinity <billing@comcast.net>",
                        "Autopay confirmed: $84.95 debited successfully"
                    ) to Pair("Xfinity Autopay confirmation: Your payment has processed using payment master ending in 4242. No physical actions required.", now - 360 * 60 * 1000),
        
                    Triple(
                        "demo_msg_008",
                        "Figma Updates <newsletter@figma.com>",
                        "Introducing DevMode 2.0 with Jetpack Compose bindings"
                    ) to Pair("Design to build just got 10x faster. Copy Material 3 tokens directly, configure responsive dp dimensions, and eliminate AI slop.", now - 600 * 60 * 1000),
        
                    Triple(
                        "demo_msg_009",
                        "Airbnb Support <booking@airbnb.com>",
                        "Reservation confirmed: Minimalist cabin in Seattle"
                    ) to Pair("Get ready for your trip! You are checking into Seattle Forest Retreat on June 15. Standard house rules apply.", now - 1200 * 60 * 1000),
        
                    Triple(
                        "demo_msg_010",
                        "Slack Alerts <notifications@slack.com>",
                        "New mentions in #smart-gmail-manager workspace"
                    ) to Pair("@developer requested review on Android manifest updates. Let's merge it before making any subsequent API build actions.", now - 1600 * 60 * 1000),
        
                    Triple(
                        "demo_msg_011",
                        "Uber Receipts <receipts@uber.com>",
                        "Your ride receipt with Uber - $18.50"
                    ) to Pair("Thanks for riding, Customer! Here is your fare summary for your morning commute to Google AI Studio offices.", now - 2100 * 60 * 1000),
        
                    Triple(
                        "demo_msg_012",
                        "Product Hunt <hello@producthunt.com>",
                        "Daily Digest: Autonomous AI Coders inside Jetpack Compose"
                    ) to Pair("Top Hunted today: 1. Antigravity Agent (A coding wizard that builds Android apps), 2. PromptOptimizer, 3. SlackBot3000.", now - 2800 * 60 * 1000)
                )

        return raw.map { (meta, payload) ->
            val (id, sender, subject) = meta
            val (snippet, timestamp) = payload
            val (name, email) = parseFrom(sender)
            ServerEmail(id, id, name, email, subject, snippet, timestamp, listOf("INBOX", "UNREAD"))
        }
    }
}
