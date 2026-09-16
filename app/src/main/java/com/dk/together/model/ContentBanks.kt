package com.dk.together.model

/**
 * Original in-app content. These prompts use familiar couples-game mechanics, but they are not copied
 * from Paired or another commercial prompt library.
 */
object ContentBanks {
    private val topics: Map<String, List<String>> = mapOf(
        "Everyday" to listOf("your workload", "your sleep", "our mornings", "our evenings", "our weekends", "your social battery", "our meals", "our phone habits", "our downtime", "our chores", "your energy lately", "our bedtime routine"),
        "Fun & Play" to listOf("our humour", "spontaneity", "date nights", "trying new things", "competition", "silly traditions", "games", "music", "food adventures", "weekend plans", "travel mishaps", "inside jokes"),
        "Memories" to listOf("our first date", "our first proper conversation", "our first trip", "our funniest day", "our first disagreement", "our first photo", "our first inside joke", "a hard period we got through", "a favourite meal together", "a quiet day that became memorable", "our early messages", "a celebration we shared"),
        "Values" to listOf("honesty", "loyalty", "kindness", "ambition", "balance", "independence", "generosity", "family", "freedom", "stability", "fairness", "responsibility"),
        "Future" to listOf("our next year", "travel", "where we live", "our home", "career changes", "shared hobbies", "financial goals", "family plans", "traditions", "health", "adventures", "how we spend weekends"),
        "Communication" to listOf("listening", "reassurance", "timing", "tone", "hard conversations", "asking for help", "saying no", "giving feedback", "texting", "apologies", "misunderstandings", "checking in"),
        "Conflict" to listOf("taking space", "repairing after arguments", "apologies", "defensiveness", "fairness", "forgiveness", "raised voices", "feeling unheard", "repeating arguments", "compromise", "stress spillover", "letting things go"),
        "Support" to listOf("work stress", "family stress", "money worries", "low confidence", "poor sleep", "a difficult decision", "feeling lonely", "burnout", "failure", "uncertainty", "motivation", "rest"),
        "Money" to listOf("saving", "spending", "debt", "financial security", "shared goals", "large purchases", "holidays", "helping family", "emergency funds", "housing", "gifts", "fun money"),
        "Home & Routines" to listOf("cooking", "cleaning", "shopping", "laundry", "life admin", "planning dates", "bills", "home maintenance", "meal planning", "tidiness", "sleep routines", "having guests"),
        "Family & Friends" to listOf("family boundaries", "friendships", "social energy", "holidays", "traditions", "meeting new people", "private relationship details", "family advice", "friend group time", "supporting relatives", "old friends", "double dates"),
        "Trust & Boundaries" to listOf("privacy", "jealousy", "independence", "phones", "ex-partners", "social media", "location sharing", "flirting", "friendships", "passwords", "personal space", "relationship details shared with others"),
        "Affection" to listOf("touch", "compliments", "cuddling", "kissing", "small gestures", "public affection", "surprises", "love notes", "holding hands", "goodnight routines", "greetings", "feeling desired"),
        "Intimacy" to listOf("vulnerability", "desire", "closeness", "initiation", "comfort", "being present", "trying something new", "saying no", "sexual communication", "non-sexual touch", "reconnecting after stress", "body confidence"),
        "Sex & Pleasure" to listOf("arousal", "foreplay", "orgasm", "climaxing", "feedback", "sexual confidence", "frequency", "fantasies", "boundaries", "initiation", "aftercare", "pressure around performance"),
        "Growth" to listOf("patience", "confidence", "discipline", "rest", "communication", "asking for help", "saying no", "managing stress", "taking risks", "celebrating progress", "learning", "change"),
        "Long Distance" to listOf("calls", "messages", "visits", "missing each other", "shared routines", "time zones", "virtual dates", "photos", "voice notes", "planning reunions", "feeling included", "independence while apart"),
        "Commitment" to listOf("teamwork", "reliability", "compromise", "shared plans", "choosing each other", "busy periods", "major life changes", "sacrifice", "consistency", "romance", "shared values", "long-term stability")
    )

    private val categoryStarters: Map<String, List<String>> = mapOf(
        "Everyday" to listOf(
            "What small thing would make this week feel easier for you?",
            "What ordinary moment with me do you never get bored of?",
            "What would make tonight feel like quality time to you?",
            "What part of your current routine feels most draining?",
            "What tiny habit should we start together?"
        ),
        "Fun & Play" to listOf(
            "What ridiculous competition would you probably beat me at?",
            "What silly tradition should we invent?",
            "What would our relationship mascot be?",
            "What spontaneous adventure would you say yes to tonight?",
            "If we entered a game show together, which one would suit us?"
        ),
        "Memories" to listOf(
            "What early memory of us still feels unusually vivid?",
            "When did you first feel completely comfortable around me?",
            "What date would you happily replay exactly as it happened?",
            "What small moment made you realise we were becoming serious?",
            "What memory of us always improves your mood?"
        ),
        "Values" to listOf(
            "What quality matters most to you in a long-term partner?",
            "What does loyalty look like to you in everyday life?",
            "What does a good life mean to you right now?",
            "What principle would you struggle to compromise on?",
            "What makes someone trustworthy in your eyes?"
        ),
        "Future" to listOf(
            "What would you love our life to look like three years from now?",
            "What experience do you definitely want us to have together?",
            "What kind of home would feel most like us?",
            "What future milestone are you most excited to reach together?",
            "What do you hope never changes about us?"
        ),
        "Communication" to listOf(
            "When is it easiest for you to tell me something difficult?",
            "What makes you feel properly listened to?",
            "When you are upset, do you want questions, reassurance, or space first?",
            "What is one way I could communicate more clearly with you?",
            "What do you wish I asked you about more often?"
        ),
        "Conflict" to listOf(
            "What usually helps you calm down after an argument?",
            "What kind of apology feels sincere to you?",
            "What should we never use against each other in an argument?",
            "How much time do you usually need before revisiting a disagreement?",
            "What helps you feel an issue is genuinely resolved?"
        ),
        "Support" to listOf(
            "What kind of support do you need most when you are stressed?",
            "When do you want practical help rather than emotional reassurance?",
            "What goal could I support you with better?",
            "What makes you feel cared for when you are tired?",
            "What burden do you wish I could lighten for you this month?"
        ),
        "Money" to listOf(
            "What does financial security mean to you personally?",
            "What kind of spending makes you anxious?",
            "What financial goal would you like us to work toward together?",
            "What is worth paying extra for in your opinion?",
            "What does being financially fair in a relationship mean to you?"
        ),
        "Home & Routines" to listOf(
            "What makes a home feel cosy to you?",
            "What chore do you wish could disappear forever?",
            "What does a fair split of household work look like to you?",
            "What morning routine would you love us to have?",
            "What evening routine helps you switch off?"
        ),
        "Family & Friends" to listOf(
            "What family tradition would you like to keep in your adult life?",
            "What family tradition would you rather leave behind?",
            "Which friend brings out the best in you?",
            "What do you need from me around your family?",
            "How involved should friends be in relationship problems?"
        ),
        "Trust & Boundaries" to listOf(
            "What makes you feel secure in our relationship?",
            "What does privacy mean to you inside a relationship?",
            "What kind of independence should each partner protect?",
            "How should we handle situations where one of us feels jealous?",
            "What boundary are you glad we already respect?"
        ),
        "Affection" to listOf(
            "What kind of affection makes you feel closest to me?",
            "What compliment from me tends to stay with you?",
            "What non-sexual touch do you enjoy most?",
            "What affectionate habit would you like more of?",
            "What little sign tells you I am thinking about you?"
        ),
        "Intimacy" to listOf(
            "What helps you feel emotionally close before physical intimacy?",
            "What makes it easier for you to be vulnerable with me?",
            "What kind of atmosphere makes intimacy feel most natural?",
            "What does feeling desired mean to you?",
            "What makes you feel safest when trying something new together?"
        ),
        "Sex & Pleasure" to listOf(
            "What helps you get in the mood most reliably?",
            "What kind of foreplay do you enjoy most?",
            "Do you prefer intimacy to feel spontaneous or anticipated?",
            "What helps you communicate what feels good during sex?",
            "What helps you feel less pressure around climaxing?",
            "How important is orgasm to you in judging whether sex was satisfying?",
            "What would make conversations about orgasm easier between us?",
            "What kind of aftercare or closeness do you enjoy after sex?",
            "What makes you feel most confident sexually?",
            "What is something about our sex life you would be happy never to change?"
        ),
        "Growth" to listOf(
            "What have you learned about yourself since being with me?",
            "What part of yourself are you actively trying to improve?",
            "What habit are you most proud of changing?",
            "What is one way our relationship has helped you grow?",
            "What do you hope we become better at together?"
        ),
        "Long Distance" to listOf(
            "What makes you feel closest when we are apart?",
            "What kind of message from me can change your whole day?",
            "What do you miss most when we cannot be physically together?",
            "What virtual date would you genuinely look forward to?",
            "What should we prioritise during our next time together?"
        ),
        "Commitment" to listOf(
            "What makes a relationship feel stable to you?",
            "What promise between partners matters most to you?",
            "What does choosing each other look like on an ordinary day?",
            "What does being a team mean to you?",
            "What do you most want us to protect as life gets busier?"
        )
    )

    fun expandedQuestions(existing: List<QuestionContent>): List<QuestionContent> {
        val out = existing.toMutableList()
        var next = existing.size + 1
        categoryStarters.forEach { (category, prompts) ->
            prompts.forEach { prompt -> out += QuestionContent("xq${next++}", category, prompt) }
        }
        topics.forEach { (category, items) ->
            items.forEach { topic ->
                out += QuestionContent("xq${next++}", category, "What is one thing you would like me to understand better about $topic?")
                out += QuestionContent("xq${next++}", category, "What is one thing you think we already do well around $topic?")
                out += QuestionContent("xq${next++}", category, "If we could improve one thing about $topic, what would you choose?")
            }
        }
        return out.distinctBy { it.prompt.lowercase() }
    }

    fun cards(questions: List<QuestionContent>): List<CardContent> {
        val deckMap = mapOf(
            "Everyday" to "Warm-up", "Fun & Play" to "Playful", "Memories" to "Memories",
            "Values" to "Deep", "Future" to "Future", "Communication" to "Deep",
            "Conflict" to "Repair", "Support" to "Teamwork", "Money" to "Teamwork",
            "Home & Routines" to "Teamwork", "Family & Friends" to "Deep",
            "Trust & Boundaries" to "Deep", "Affection" to "Appreciation",
            "Intimacy" to "Intimacy", "Sex & Pleasure" to "Spicy", "Growth" to "Deep",
            "Long Distance" to "Appreciation", "Commitment" to "Teamwork"
        )
        val perDeckCount = mutableMapOf<String, Int>()
        val out = mutableListOf<CardContent>()
        questions.forEach { q ->
            val deck = deckMap[q.category] ?: return@forEach
            val used = perDeckCount[deck] ?: 0
            if (used < 40) {
                val lead = when (deck) {
                    "Playful" -> "Take turns and keep it light: "
                    "Repair" -> "Talk this through gently: "
                    "Spicy" -> "Answer only as far as feels comfortable: "
                    "Teamwork" -> "Compare your answers: "
                    else -> "Take turns answering: "
                }
                out += CardContent("card${out.size + 1}", deck, lead + q.prompt)
                perDeckCount[deck] = used + 1
            }
        }
        return out
    }

    fun games(): List<GamePrompt> {
        val groups = listOf(
            "Everyday" to listOf(
                "Cook together" to "Order in", "Early night" to "Late-night chat", "Plan the weekend" to "Decide on the day",
                "Coffee date" to "Dinner date", "Quiet evening" to "Busy evening out", "Walk somewhere" to "Drive somewhere",
                "Do chores together" to "Split up and finish faster", "Share dessert" to "Get separate desserts", "One long holiday" to "Several short breaks", "Make breakfast" to "Go out for breakfast"
            ),
            "Fun" to listOf(
                "Theme park" to "Water park", "Karaoke" to "Dance class", "Board games" to "Video games", "Museum" to "Arcade",
                "Camping" to "Hotel", "Road trip" to "Train trip", "Comedy show" to "Concert", "Escape room" to "Bowling",
                "Try a weird food" to "Try a weird activity", "Fancy dress party" to "Cosy night in"
            ),
            "Affection" to listOf(
                "Long hug" to "Long kiss", "Hold hands" to "Arm around your waist", "Love note" to "Surprise snack",
                "Compliment in public" to "Compliment in private", "Good-morning kiss" to "Goodnight cuddle", "Head scratches" to "Back rub",
                "Sweet text" to "Voice note", "Flowers" to "Favourite food", "Planned surprise" to "Spontaneous affection", "Cuddle on the sofa" to "Cuddle in bed"
            ),
            "Future" to listOf(
                "City life" to "Countryside life", "Big wedding" to "Small wedding", "Save for a home" to "Spend more on travel",
                "Own a place" to "Keep flexibility to move", "Lots of shared hobbies" to "Mostly separate hobbies", "Plan years ahead" to "Take life one year at a time",
                "One dream trip" to "Lots of smaller trips", "Host people often" to "Keep home very private", "Live near family" to "Live wherever suits us best", "Build a business together" to "Keep work separate"
            ),
            "Values" to listOf(
                "Security" to "Freedom", "Honesty even when awkward" to "Kindness first", "Ambition" to "Balance", "Tradition" to "Flexibility",
                "Save for later" to "Enjoy more now", "Be early" to "Be relaxed about time", "Private life" to "Open life", "Lead" to "Collaborate",
                "Take a calculated risk" to "Choose the safe option", "Say exactly what you think" to "Choose your words carefully"
            ),
            "Intimacy" to listOf(
                "Slow build-up" to "Spontaneous passion", "More kissing" to "More cuddling", "Words of desire" to "Non-verbal signals",
                "Planned intimate time" to "Completely spontaneous", "Lights low" to "Lights on", "Music" to "Quiet",
                "Long foreplay" to "Shorter build-up", "Morning intimacy" to "Night-time intimacy", "Trying something new" to "Returning to a favourite", "Talk first" to "Let the mood build naturally"
            ),
            "Sex & Pleasure" to listOf(
                "More teasing" to "More direct touch", "Give directions verbally" to "Guide with body language", "Focus on one person's pleasure" to "Take turns throughout",
                "Aim for orgasm" to "Take orgasm off the table", "Talk about fantasies in bed" to "Talk about them outside the bedroom", "Long session" to "Quick intimate moment",
                "Same favourite position" to "Experiment with positions", "Receive oral" to "Give oral", "Use a toy together" to "Keep it just the two of you", "After-sex cuddle" to "After-sex shower together"
            ),
            "Travel" to listOf(
                "Beach" to "Mountains", "City break" to "Cabin", "Packed itinerary" to "Slow holiday", "Fly somewhere" to "Road trip",
                "Luxury hotel" to "Interesting budget stay", "Hot weather" to "Cold weather", "Return to a favourite place" to "Always somewhere new", "Food-focused trip" to "Activity-focused trip",
                "Travel light" to "Pack for every possibility", "Sunrise plans" to "Late-night plans"
            ),
            "Home" to listOf(
                "Minimal home" to "Cosy clutter", "Big kitchen" to "Big bedroom", "Eat at a table" to "Eat on the sofa", "Cook from scratch" to "Quick easy meals",
                "Clean as you go" to "One big clean", "Lots of plants" to "Almost no plants", "Neutral decor" to "Colourful decor", "Separate workspaces" to "Work near each other",
                "Music in the background" to "TV in the background", "Host friends" to "Keep evenings just us"
            ),
            "Social" to listOf(
                "Big party" to "Small gathering", "Meet new people" to "Stick with close friends", "Double date" to "Just us", "Family event" to "Friend event",
                "Go out every weekend" to "Mostly stay in", "Be first to leave" to "Stay until the end", "Talk to everyone" to "Have one deep conversation",
                "Group holiday" to "Couple holiday", "Celebrate birthdays big" to "Keep birthdays low-key", "Post couple photos" to "Keep the relationship offline"
            )
        )
        val out = mutableListOf<GamePrompt>()
        groups.forEach { (category, pairs) ->
            pairs.forEach { (a, b) -> out += GamePrompt("game${out.size + 1}", category, a, b) }
        }
        val contexts = listOf(
            Triple("Couple choices", "When stressed: be comforted", "When stressed: be distracted"),
            Triple("Couple choices", "When upset: talk immediately", "When upset: take some space"),
            Triple("Couple choices", "For a gift: something useful", "For a gift: something sentimental"),
            Triple("Couple choices", "For a surprise date: romantic", "For a surprise date: adventurous"),
            Triple("Couple choices", "For conflict: apologise first", "For conflict: explain first"),
            Triple("Couple choices", "For reassurance: hear specific words", "For reassurance: see a practical action"),
            Triple("Couple choices", "For climaxing: keep trying", "For climaxing: take all pressure off it"),
            Triple("Couple choices", "For sexual feedback: say it during", "For sexual feedback: talk afterwards"),
            Triple("Couple choices", "For intimacy: more eye contact", "For intimacy: more touch"),
            Triple("Couple choices", "After sex: stay in bed together", "After sex: get up and reset together"),
            Triple("Couple choices", "For birthdays: a surprise", "For birthdays: choose together"),
            Triple("Couple choices", "For affection: lots of small touches", "For affection: one long cuddle"),
            Triple("Couple choices", "For planning: book early", "For planning: leave room for spontaneity"),
            Triple("Couple choices", "For a romantic message: sweet", "For a romantic message: flirty"),
            Triple("Couple choices", "For money: save unexpected cash", "For money: spend some on a treat"),
            Triple("Couple choices", "On holiday: lots of photos", "On holiday: mostly stay off the phone"),
            Triple("Couple choices", "For communication: messages through the day", "For communication: one proper catch-up later"),
            Triple("Couple choices", "For weekends: one main plan", "For weekends: keep it completely open"),
            Triple("Couple choices", "For sleep: cuddle until asleep", "For sleep: have personal space"),
            Triple("Couple choices", "For trying something sexual: discuss in advance", "For trying something sexual: decide in the moment")
        )
        contexts.forEach { (category, a, b) -> out += GamePrompt("game${out.size + 1}", category, a, b) }
        return out
    }
}
