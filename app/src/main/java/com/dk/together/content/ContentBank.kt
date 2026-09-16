package com.dk.together.content

import java.time.LocalDate
import kotlin.math.abs

/** Platform-neutral content model. The same IDs/text can later be exposed to the HTML client. */
data class DailyQuestion(
    val id: String,
    val dayIndex: Int,
    val category: String,
    val prompt: String,
    val adult: Boolean = false,
)

data class CardPrompt(
    val id: String,
    val category: String,
    val mode: String,
    val prompt: String,
    val adult: Boolean = false,
)

data class DailyCardGame(
    val id: String,
    val dayIndex: Int,
    val category: String,
    val title: String,
    val items: List<CardPrompt>,
)

data class DailyChallenge(
    val id: String,
    val dayIndex: Int,
    val category: String,
    val timeframe: String,
    val prompt: String,
    val adult: Boolean = false,
)

data class RatingScale(val value: Int, val label: String)

object EveluneContentBank {
    const val DAYS = 730
    const val PARTNER_TOKEN = "{{partner_name}}"

    val ratingScale = listOf(
        RatingScale(1, "Not at all true / very unlikely"),
        RatingScale(2, "A little true / somewhat unlikely"),
        RatingScale(3, "Mostly true / somewhat likely"),
        RatingScale(4, "Very true / very likely"),
    )

    private data class QuestionSpec(val category: String, val cores: List<String>, val patterns: List<String>)
    private data class ChallengeSpec(val category: String, val tasks: List<String>)
    private data class CardSpec(val category: String, val actions: List<String>, val contexts: List<String>)

    private fun parts(value: String): List<String> = value.split('|').map(String::trim).filter(String::isNotBlank)

    private fun <T> stableMix(values: List<T>, key: (T) -> String): List<T> =
        values.sortedWith(compareBy<T> { stableHash(key(it)) }.thenBy { key(it) })

    private fun stableHash(value: String): Long {
        var h = 0xcbf29ce484222325UL
        value.forEach { c ->
            h = h xor c.code.toULong()
            h *= 0x100000001b3UL
        }
        return h.toLong()
    }

    val questions: List<DailyQuestion> by lazy {
        val generated = mutableListOf<DailyQuestion>()
        var id = 1
        questionSpecs.forEach { spec ->
            spec.cores.forEach { core ->
                spec.patterns.forEach { pattern ->
                    generated += DailyQuestion(
                        id = "q${id.toString().padStart(4, '0')}",
                        dayIndex = 0,
                        category = spec.category,
                        prompt = pattern.replace("{x}", core),
                        adult = spec.category in setOf("Sex & Pleasure", "Intimacy"),
                    )
                    id++
                }
            }
        }
        stableMix(generated) { it.prompt }.take(DAYS).mapIndexed { index, item -> item.copy(dayIndex = index + 1) }
    }

    val challenges: List<DailyChallenge> by lazy {
        val timeframes = listOf("Today", "Tonight", "This week", "Next time you are together")
        val generated = mutableListOf<DailyChallenge>()
        var id = 1
        challengeSpecs.forEach { spec ->
            spec.tasks.forEach { task ->
                timeframes.forEach { timeframe ->
                    generated += DailyChallenge(
                        id = "ch${id.toString().padStart(4, '0')}",
                        dayIndex = 0,
                        category = spec.category,
                        timeframe = timeframe,
                        prompt = "$timeframe: $task",
                        adult = spec.category in setOf("Sex & Pleasure", "Intimacy"),
                    )
                    id++
                }
            }
        }
        stableMix(generated) { it.prompt }.take(DAYS).mapIndexed { index, item -> item.copy(dayIndex = index + 1) }
    }

    val cardGames: List<DailyCardGame> by lazy {
        val pools = cardSpecs.associate { spec ->
            var localId = 1
            val items = mutableListOf<CardPrompt>()
            spec.actions.forEach { action ->
                spec.contexts.forEach { context ->
                    items += CardPrompt(
                        id = "${slug(spec.category)}-t${localId.toString().padStart(3, '0')}",
                        category = spec.category,
                        mode = "truth",
                        prompt = "How true is it that $PARTNER_TOKEN $action $context?",
                        adult = spec.category in setOf("Sex & Pleasure", "Intimacy"),
                    )
                    localId++
                    items += CardPrompt(
                        id = "${slug(spec.category)}-l${localId.toString().padStart(3, '0')}",
                        category = spec.category,
                        mode = "likelihood",
                        prompt = "How likely is it that $PARTNER_TOKEN would consistently show this quality $context: they $action?",
                        adult = spec.category in setOf("Sex & Pleasure", "Intimacy"),
                    )
                    localId++
                }
            }
            spec.category to stableMix(items) { it.prompt }
        }
        val positions = cardSpecs.associate { it.category to 0 }.toMutableMap()
        val out = mutableListOf<DailyCardGame>()
        repeat(DAYS) { zeroDay ->
            val spec = cardSpecs[zeroDay % cardSpecs.size]
            val start = positions.getValue(spec.category)
            val items = pools.getValue(spec.category).subList(start, start + 4)
            positions[spec.category] = start + 4
            out += DailyCardGame(
                id = "cg${(zeroDay + 1).toString().padStart(4, '0')}",
                dayIndex = zeroDay + 1,
                category = spec.category,
                title = "${spec.category} check-in",
                items = items,
            )
        }
        out
    }

    fun todayIndex(date: LocalDate = LocalDate.now()): Int = Math.floorMod(date.toEpochDay().toInt(), DAYS)
    fun todayQuestion(date: LocalDate = LocalDate.now()) = questions[todayIndex(date)]
    fun todayCard(date: LocalDate = LocalDate.now()) = cardGames[todayIndex(date)]
    fun todayChallenge(date: LocalDate = LocalDate.now()) = challenges[todayIndex(date)]
    fun render(text: String, partnerName: String): String = text.replace(PARTNER_TOKEN, partnerName)

    fun scoreMean(first: List<Int>, second: List<Int>): Double {
        if (first.isEmpty() || first.size != second.size) return 0.0
        return first.indices.map { (first[it] + second[it]) / 2.0 }.average()
    }

    fun averageDifference(first: List<Int>, second: List<Int>): Double {
        if (first.isEmpty() || first.size != second.size) return 0.0
        return first.indices.map { abs(first[it] - second[it]).toDouble() }.average()
    }

    private fun slug(value: String) = value.lowercase().replace("&", "and").replace(Regex("[^a-z0-9]+"), "-").trim('-')

    private val questionPatternsGeneral = parts("When do you {x}?|What usually makes you {x}?|What is one small thing I do that helps you {x}?|What could we do more often so you {x}?")

    private val questionSpecs = listOf(
        QuestionSpec("Connection", parts("feel closest to me|feel like we are really on the same team|feel most understood by me|feel most relaxed around me|feel most excited about us|feel most emotionally connected to me|feel most appreciated by me|feel most secure in our relationship|feel most playful with me|feel most proud of us|feel most like yourself around me"), questionPatternsGeneral),
        QuestionSpec("Communication", parts("say what you really need|feel heard without being interrupted|bring up a problem before it grows|ask for reassurance directly|say when you need space|admit when you have changed your mind|tell me when something has bothered you|explain what you mean when emotions are high|end a hard conversation feeling understood|talk openly about something difficult|ask a question instead of making an assumption"), parts("What helps you {x}?|What makes it easier for you to {x}?|How could I make it easier for you to {x}?|What tends to get in the way when you try to {x}?")),
        QuestionSpec("Affection", parts("a kiss before leaving the house|a long hug after a tiring day|being told you look good|holding hands in public|a thoughtful message during the day|cuddling before sleep|casual touch as we pass each other|hearing 'I love you' unexpectedly|small acts of care without being asked|being shown affection around other people|being greeted warmly when we reunite"), parts("How much does {x} matter to you?|What do you like most about {x}?|When does {x} feel most meaningful to you?|Would you like more, less, or about the same amount of {x}?")),
        QuestionSpec("Romance", parts("planning a date without revealing the details|dressing up for each other|leaving handwritten notes|recreating an early date|slow dancing at home|bringing home a small surprise|planning a weekend away|making an ordinary evening feel special|celebrating small milestones|doing something deliberately cheesy together|building anticipation before seeing each other"), parts("What would make {x} feel especially romantic to you?|How often would you enjoy {x}?|What is your favourite thing about {x}?|What would make you want us to do more of {x}?")),
        QuestionSpec("Sex & Pleasure", parts("foreplay that builds slowly|being told exactly what feels good|taking more time before orgasm|talking during sex|trying a new position|initiating sex spontaneously|planning sex in advance|giving each other direct feedback during sex|spending more time on non-penetrative pleasure|focusing on one person's pleasure for a while|making sex feel playful rather than goal-focused"), parts("What do you enjoy most about {x}?|What would make {x} even better for you?|How important is {x} to your enjoyment of sex?|What do you want me to understand better about {x}?")),
        QuestionSpec("Intimacy", parts("lying together with no phones around|talking in bed before sleeping|being physically close without it needing to lead to sex|sharing something that has been on your mind all day|being completely quiet together|taking a shower together|giving each other a massage|waking up slowly together|holding eye contact a little longer than usual|having private rituals only we understand|being vulnerable without trying to fix anything immediately"), parts("What do you like most about {x}?|When does {x} make you feel especially close to me?|How could we make more room for {x}?|What feeling do you associate most with {x}?")),
        QuestionSpec("Trust & Security", parts("relying on me when you are under pressure|believing I will keep my word|telling me something you worry I might misunderstand|feeling confident I will defend our relationship|knowing where we stand after a disagreement|trusting me with something private|believing I will be honest even when honesty is awkward|feeling secure when we spend time apart|trusting that I will respect a boundary|knowing I will show up when you need me|believing my actions match what I say"), parts("What helps you feel secure about {x}?|What makes {x} easier for you?|What could strengthen your confidence in {x}?|What does {x} look like at its best in our relationship?")),
        QuestionSpec("Conflict & Repair", parts("taking a pause before saying something hurtful|apologising without adding excuses|coming back to a disagreement after cooling down|admitting when one of us misunderstood the other|repairing the mood after an argument|disagreeing without raising our voices|saying what the real issue is|letting go once something is genuinely resolved|asking questions instead of assuming intentions|showing affection again after a difficult conversation|compromising without keeping score"), parts("What helps us most with {x}?|What makes {x} difficult for you?|How could we get better at {x}?|What do you need from me when we are trying to handle {x}?")),
        QuestionSpec("Space & Independence", parts("having an evening to yourself|seeing friends separately|keeping your own hobbies|making plans without constant updates|having quiet time after a busy day|travelling separately sometimes|having parts of life that are just yours|working toward a personal goal|spending a weekend doing different things|being able to ask for space without it becoming personal|having time to miss each other"), parts("What does a healthy version of {x} look like to you?|How much do you value {x}?|What helps {x} feel secure rather than distant?|How could I support you better with {x}?")),
        QuestionSpec("Support & Stress", parts("you are overwhelmed|you are exhausted|you are anxious about something important|work or study is taking over|you are disappointed in yourself|you need motivation|you are angry about something outside our relationship|you have too many decisions to make|you need comfort but not advice|you need practical help more than reassurance|you are having a day where everything feels harder than usual"), parts("What kind of support works best for you when {x}?|What should I avoid doing when {x}?|How can I tell what you need from me when {x}?|What is one thing I could do that would genuinely help when {x}?")),
        QuestionSpec("Daily Life", parts("our mornings|our evenings|weekends at home|sharing chores|deciding what to eat|getting ready to go somewhere|keeping the house organised|making time for each other during a busy week|going to bed at different times|planning the week ahead|handling last-minute changes"), parts("What would make {x} run more smoothly for us?|What do you already like about {x}?|What is one small change that would improve {x}?|How could {x} feel more like teamwork?")),
        QuestionSpec("Fun & Play", parts("being silly together|playing competitive games|making spontaneous plans|trying something we are both bad at|having inside jokes|teasing each other playfully|going somewhere just because it looks interesting|turning a boring task into something fun|doing something childish for the sake of it|laughing so hard we cannot finish the story|taking harmless little risks together"), parts("What do you enjoy most about {x}?|When did {x} last make you feel really happy with us?|How could we make more room for {x}?|What would make {x} even more fun for you?")),
        QuestionSpec("Future", parts("where we might live|what our home could feel like|how we want to spend holidays|how we would handle a major career opportunity|what kind of routines we want in a few years|how much adventure we want in our life|what financial security means for us|which traditions we want to create|what we want our relationship to feel like in five years|what we want to protect no matter how busy life gets|how we want to make big decisions together"), parts("What excites you most when you think about {x}?|What matters most to you about {x}?|Where do you think we are already aligned on {x}?|What would you love for us to decide together about {x}?")),
        QuestionSpec("Money", parts("saving for something big|spending on experiences|buying gifts for each other|splitting shared costs|handling an unexpected expense|talking about debt|deciding how much to save|spending on hobbies|supporting family financially|planning a large purchase|balancing enjoyment now with security later"), parts("What matters most to you when it comes to {x}?|What would make conversations about {x} feel straightforward?|Where do you think our instincts differ most around {x}?|What would a fair approach to {x} look like for us?")),
        QuestionSpec("Values", parts("loyalty|honesty|kindness|ambition|family|independence|generosity|stability|curiosity|respect|fairness"), parts("What does {x} mean to you inside a relationship?|How do you think we already show {x} well?|When can {x} become difficult to balance with other priorities?|What would make you feel that we are protecting {x} as a couple?")),
        QuestionSpec("Family & Friends", parts("spending time with each other's families|balancing couple time with friendships|deciding what stays private between us|handling a family member who oversteps|supporting each other at social events|hosting people at home|making time for close friends|dealing with different family traditions|deciding whose plans take priority on busy weekends|supporting each other when family life is stressful|leaving a social event when one of us has had enough"), parts("What feels most important to you about {x}?|What would make {x} easier for us?|Where do you think we work well as a team with {x}?|What boundary would help us handle {x} better?")),
        QuestionSpec("Appreciation", parts("the way I support you|the way I make you laugh|the way I show affection|the way I handle practical things|the way I include you in decisions|the way I speak about you to other people|the way I make time for us|the way I notice small details about you|the way I try after we have disagreed|the way I encourage your goals|the way I show up when you need me"), parts("What do you appreciate most about {x}?|When does {x} stand out to you most?|What is one example of {x} that stayed with you?|What would make you feel even more appreciated in return for {x}?")),
        QuestionSpec("Growth", parts("becoming more patient with each other|getting better at difficult conversations|protecting quality time|being more affectionate|being more adventurous|handling stress as a team|supporting each other's ambitions|making decisions together|repairing conflict faster|keeping curiosity alive|being more intentional instead of running on autopilot"), parts("What would progress look like for us with {x}?|What are we already doing well with {x}?|What is one habit that would help us with {x}?|What would make {x} feel natural rather than forced?")),
    )

    private val challengeSpecs = listOf(
        ChallengeSpec("Connection", parts("Spend ten minutes telling each other what has been taking up the most space in your head lately.|Ask $PARTNER_TOKEN one question you normally assume you already know the answer to.|Sit together for one full song without phones or another task competing for attention.|Tell $PARTNER_TOKEN one moment this week when you felt especially close to them.|Share one thing you want the two of you to make more time for.|Take a short walk together with no destination and no phones in your hands.|Each say one thing you want more of in the relationship this month.|Tell $PARTNER_TOKEN something you have been thinking about but have not mentioned yet.|Have a ten-minute check-in where neither person gives advice unless asked.|Choose one tiny ritual you want to repeat every week.|End the day by telling each other the best moment you shared today.")),
        ChallengeSpec("Communication", parts("Have five uninterrupted minutes each to talk while the other person only listens.|Tell $PARTNER_TOKEN one thing you need more clearly than you usually would.|Ask $PARTNER_TOKEN what kind of support would be most useful today.|Say one thing you have been avoiding because the wording felt awkward.|Ask one follow-up question before giving your opinion in your next serious conversation.|Tell $PARTNER_TOKEN how you prefer them to raise a difficult topic with you.|Finish the sentence together: 'Lately I feel most understood when…'|Ask $PARTNER_TOKEN what they wish you asked about more often.|Have one conversation with both phones completely out of reach.|Name one assumption you sometimes make about each other and replace it with a question.|Tell $PARTNER_TOKEN one phrase that helps you feel reassured.")),
        ChallengeSpec("Affection", parts("Give $PARTNER_TOKEN a long hug before saying anything else when you next see them.|Kiss $PARTNER_TOKEN slowly rather than as a quick habit.|Hold $PARTNER_TOKEN's hand for a few minutes while doing something ordinary.|Tell $PARTNER_TOKEN one physical feature you genuinely love about them.|Give $PARTNER_TOKEN a shoulder or scalp massage for five minutes.|Sit close enough that you are touching while you watch something together.|Send $PARTNER_TOKEN an affectionate message without asking for anything.|Give $PARTNER_TOKEN a compliment they have not heard from you recently.|Make a point of greeting $PARTNER_TOKEN warmly when you reunite.|End the day with one minute of uninterrupted cuddling.|Tell $PARTNER_TOKEN one kind of everyday touch you would like more often.")),
        ChallengeSpec("Romance", parts("Plan a small date for $PARTNER_TOKEN without telling them the details.|Make an ordinary drink or snack feel like a tiny date.|Leave $PARTNER_TOKEN a handwritten note somewhere they will find it.|Choose a song that reminds you of $PARTNER_TOKEN and explain why.|Recreate one small detail from an early date or early memory.|Dress a little nicer than usual for an evening together at home.|Slow dance with $PARTNER_TOKEN for one song.|Pick one nearby place that could become 'your place' and go there together.|Take one photo together that you actually want to keep.|Plan a date with a strict budget and make the constraint part of the fun.|Do one deliberately cheesy romantic thing and commit to it fully.")),
        ChallengeSpec("Sex & Pleasure", parts("Tell $PARTNER_TOKEN one thing that reliably helps you get more turned on.|Ask $PARTNER_TOKEN what they want more of during foreplay.|Tell $PARTNER_TOKEN one thing they do during sex that you want them to keep doing.|Talk about what helps each of you reach orgasm more easily.|Choose one thing you want to slow down during sex next time.|Tell $PARTNER_TOKEN where you want more touch and where you want less.|Ask $PARTNER_TOKEN what makes sex feel especially connected rather than routine.|Talk about whether you prefer spontaneous sex, planned sex, or a mix of both.|Tell $PARTNER_TOKEN one thing you would like to try during sex that you have not done together yet.|Talk for five minutes about pleasure without turning it into a performance review.|Tell $PARTNER_TOKEN whether you want more teasing, more directness, or more slow build-up next time.")),
        ChallengeSpec("Intimacy", parts("Spend ten minutes kissing without trying to rush toward anything else.|Tell $PARTNER_TOKEN one kind of touch you want more of.|Lie together for ten minutes with no phones and no conversation required.|Take turns giving each other a slow five-minute massage.|Tell $PARTNER_TOKEN one recent moment when you felt especially attracted to them.|Ask $PARTNER_TOKEN what kind of physical affection would feel best tonight.|Cuddle in silence for one full song.|Take a shower together and make it deliberately unhurried.|Tell $PARTNER_TOKEN one non-sexual thing they do that turns you on.|Spend a few minutes making eye contact while you are physically close.|Tell $PARTNER_TOKEN what makes you feel most emotionally exposed in a good way.")),
        ChallengeSpec("Trust & Security", parts("Tell $PARTNER_TOKEN one thing they do that makes you trust them.|Name one promise that matters to you more than it might seem.|Ask $PARTNER_TOKEN what helps them feel secure when you spend time apart.|Tell $PARTNER_TOKEN one boundary you appreciate them respecting.|Share one worry you usually keep to yourself.|Tell $PARTNER_TOKEN one way they make the relationship feel stable.|Ask what honesty looks like when the truth may be awkward.|Name one action that matters more to you than reassurance words.|Tell $PARTNER_TOKEN one situation where you rely on them most.|Ask each other what 'loyalty' looks like in everyday behaviour.|Agree on one thing you both want to protect from outside pressure.")),
        ChallengeSpec("Conflict & Repair", parts("Name one argument habit you want to avoid next time tension rises.|Tell $PARTNER_TOKEN one thing they do during conflict that helps you stay open.|Agree on a phrase either of you can use when you need a pause without ending the conversation.|Revisit one small unresolved issue calmly and keep the conversation under fifteen minutes.|Practise saying 'What I meant was…' instead of defending the first wording.|Tell $PARTNER_TOKEN what helps you feel repaired after a disagreement.|Agree on one thing neither of you wants to do during arguments.|Apologise for one small recent moment you could have handled better.|Ask $PARTNER_TOKEN whether anything small is still lingering from a recent disagreement.|End one difficult conversation with a clear statement of what you both agree on.|Tell each other what a good apology sounds like to you.")),
        ChallengeSpec("Space & Independence", parts("Give each other one uninterrupted hour for your own interests.|Encourage $PARTNER_TOKEN to make one plan that is just for them.|Spend part of the day doing separate things, then tell each other the best part.|Ask $PARTNER_TOKEN what kind of alone time helps them reset fastest.|Treat a request for quiet time as information rather than rejection.|Make one individual plan for the week and tell each other about it.|Support one hobby or interest $PARTNER_TOKEN enjoys independently.|Have a short conversation about what 'enough space' looks like this week.|Give each other permission to have a low-social-energy evening.|Choose one thing each of you wants to do alone this month and put it in the calendar.|Tell $PARTNER_TOKEN one part of your independence you want to protect as the relationship grows.")),
        ChallengeSpec("Support & Stress", parts("Ask $PARTNER_TOKEN what they need today: listening, advice, reassurance, or practical help.|Tell $PARTNER_TOKEN one reason you believe they can handle something they are facing.|Help $PARTNER_TOKEN break one stressful task into the next three steps.|Give $PARTNER_TOKEN ten uninterrupted minutes to vent without fixing anything.|Ask $PARTNER_TOKEN what they are carrying that you may not be noticing.|Offer to handle one small responsibility while $PARTNER_TOKEN takes a break.|Remind $PARTNER_TOKEN of one difficult thing they have already managed well.|Check in with a specific question instead of 'Are you okay?'|Ask $PARTNER_TOKEN what would make tonight feel easier.|Protect one block of quiet time for $PARTNER_TOKEN.|Tell $PARTNER_TOKEN exactly how they can signal when they need comfort rather than solutions.")),
        ChallengeSpec("Daily Life", parts("Do one task $PARTNER_TOKEN normally handles before they get to it.|Prepare something $PARTNER_TOKEN will need later.|Take one annoying job off $PARTNER_TOKEN's plate without making a big deal of it.|Ask which practical thing would make $PARTNER_TOKEN's day easier and do it.|Tidy one shared space that has been bothering both of you.|Make $PARTNER_TOKEN their preferred drink exactly how they like it.|Handle one piece of admin or planning you have both been putting off.|Set up tomorrow morning so it is easier for both of you.|Do a ten-minute reset of the room you spend most time in together.|Take over one routine responsibility for the day.|Choose one household routine to simplify together.")),
        ChallengeSpec("Fun & Play", parts("Play a quick game where the loser makes the next drink or snack.|Do something deliberately ridiculous together for ten minutes.|Send $PARTNER_TOKEN the funniest photo you have of the two of you.|Invent a completely unnecessary competition around an ordinary task.|Watch something you both loved as children.|Try a snack, drink, or food neither of you has had before.|Take turns making each other laugh without touching.|Make up a fake business idea and pitch it seriously for five minutes.|Go for a walk and each choose one strange thing the other has to photograph.|Create a two-person quiz about your relationship and see who remembers more.|Let a coin toss decide between two harmless plans.")),
        ChallengeSpec("Future", parts("Pick one thing you both want to do within the next three months and choose a date.|Describe your ideal ordinary Sunday together three years from now.|Choose one shared goal that would be satisfying to complete before the year ends.|Talk for ten minutes about what you want your home life to feel like in the future.|Add one place to a shared travel list and explain why it appeals to you.|Choose one tradition you would like to start as a couple.|Talk about one thing you never want busyness to push out of your relationship.|Pick one financial goal that would feel meaningful to achieve together.|Name one skill you would enjoy learning together.|Each describe one part of the future you are most excited to build together.|Choose one big decision you want to make as a team rather than by default.")),
        ChallengeSpec("Money", parts("Tell each other one thing you are happy to spend money on and one thing you hate spending on.|Choose one shared expense that could be simplified.|Set a small shared savings target for something fun.|Explain what 'financial security' means to you in one sentence.|Choose a low-cost date idea for this month.|Compare your instinctive response to an unexpected £500 expense.|Tell each other one money habit you want to improve.|Pick one thing you would both rather save for than buy impulsively.|Choose one spending category where you want to be more intentional.|Talk about one purchase you both think would genuinely improve daily life.|Decide how you would handle a shared expense that one person values much more than the other.")),
        ChallengeSpec("Values", parts("Each choose the three values you most want the relationship to represent.|Tell $PARTNER_TOKEN what loyalty means to you in ordinary behaviour.|Describe what respect looks like when you strongly disagree.|Name one value from your upbringing you want to keep.|Name one value from your upbringing you want to do differently.|Tell $PARTNER_TOKEN where independence fits inside commitment for you.|Talk about what generosity means when money is not involved.|Each explain what a meaningful life would look like without status.|Name one value you think the two of you already share strongly.|Tell $PARTNER_TOKEN one value you never want either of you to compromise.|Choose one value you want your future household to be known for.")),
        ChallengeSpec("Family & Friends", parts("Tell $PARTNER_TOKEN what makes you feel supported around your family.|Talk about how much couple time versus friend time feels healthy this month.|Agree on one thing that should stay private between the two of you.|Discuss how you want to handle a relative who oversteps.|Tell $PARTNER_TOKEN what helps you feel included around their friends.|Plan one social event where you both know the expected leaving time.|Ask each other what family tradition matters most to keep.|Discuss what to do when both families want the same date.|Name one friendship you want to protect even when life gets busier.|Tell $PARTNER_TOKEN what support you need when family life is stressful.|Agree on a signal either of you can use when you have had enough at a social event.")),
        ChallengeSpec("Appreciation", parts("Thank $PARTNER_TOKEN for one thing they do regularly that you usually take for granted.|Tell $PARTNER_TOKEN one thing they handled well this week.|Write down three things you appreciate about $PARTNER_TOKEN and read them out.|Notice one small effort $PARTNER_TOKEN makes and acknowledge it immediately.|Tell $PARTNER_TOKEN one way they make your life easier.|Name one quality in $PARTNER_TOKEN that you hope never changes.|Tell $PARTNER_TOKEN about a recent moment that made you feel proud of them.|Point out one way $PARTNER_TOKEN has grown since you have been together.|Tell $PARTNER_TOKEN one thing they do that makes you feel loved.|Thank $PARTNER_TOKEN for something emotional rather than practical.|Tell $PARTNER_TOKEN one thing about them you still notice even after seeing it many times.")),
        ChallengeSpec("Growth", parts("Tell $PARTNER_TOKEN one way you think the relationship has improved this year.|Choose one habit you want to build together for the next seven days.|Ask $PARTNER_TOKEN what they think you have become better at as a partner.|Name one area where you want the two of you to become more intentional.|Choose one thing you both want to stop postponing.|Tell $PARTNER_TOKEN one relationship skill you want to improve.|Pick one small weekly ritual to test for a month.|Each name one way you want to show up better during stressful weeks.|Choose one thing you want to protect as your lives get busier.|Talk about what 'better together' should mean in practice.|Name one pattern you hope feels easier a year from now.")),
    )

    private val cardSpecs = listOf(
        CardSpec("Communication", parts("listens fully before responding|asks what you mean instead of assuming|makes it easy for you to say what you really think|explains their own feelings clearly|can discuss a problem without turning it into blame|tells you when something is bothering them|checks that they understood you correctly|makes space for your point of view|can say 'I was wrong' without making it a bigger issue|helps difficult conversations end with clarity"), parts("when the topic is emotionally charged|when you disagree strongly|when one of you is tired|when plans have gone wrong|when you are both busy|when the issue is small but irritating|when you need reassurance|when you need a direct answer|when you are trying not to argue|when something has been misunderstood")),
        CardSpec("Space & Independence", parts("gives you enough space to recharge|respects your need for time alone|supports you having interests of your own|is secure when you make plans without them|does not make your need for quiet time feel personal|encourages you to keep your own friendships|trusts you without needing constant updates|lets you focus on personal goals without guilt|understands that closeness and independence can exist together|can spend time apart without making the relationship feel distant"), parts("after a demanding day|during a busy week|when you have a lot on your mind|when your social battery is low|when you are focused on work or study|when you want to see friends|when your routines are different|when you want a quiet evening|when you are travelling separately|when you need to reset on your own")),
        CardSpec("Affection", parts("shows you enough physical affection|makes you feel wanted|initiates affectionate touch often enough|notices when you need comfort|gives you compliments that feel genuine|makes everyday affection feel natural|shows affection in ways you actually value|makes you feel attractive|uses small gestures to stay connected|makes you feel loved without needing a big occasion"), parts("at home|in public|after you have had a hard day|when you are both busy|when you have not seen each other for a while|before sleep|in the morning|when one of you is distracted|after a disagreement has been resolved|during ordinary routines")),
        CardSpec("Romance", parts("puts enough effort into keeping romance alive|still makes you feel chosen|creates special moments without needing an occasion|knows what makes you feel romantic|makes date time feel different from normal routine|surprises you in ways you actually enjoy|makes affection feel intentional|keeps some anticipation in the relationship|makes you feel pursued as well as loved|balances comfort with excitement"), parts("during busy periods|on ordinary evenings|around anniversaries|when money is limited|when you have settled into routine|when one of you is stressed|after a period with few dates|when planning time together|when you have not had much time alone|when you want to reconnect")),
        CardSpec("Sex & Pleasure", parts("pays attention to what gives you pleasure|makes it easy to say what feels good|gives enough time to foreplay|makes sex feel connected rather than mechanical|responds well to feedback during sex|cares about your orgasm as much as their own|creates enough variety to keep sex interesting|makes it easy to talk about what you want sexually|can slow things down when that would feel better|makes you feel desired during sex"), parts("when you are both relaxed|when one of you initiates spontaneously|when sex has been planned|when one of you is taking longer to get turned on|when you want more foreplay|when you want to focus on one person's pleasure|when you want to try something new|when you need to give direct feedback|when orgasm is taking longer than usual|when you want sex to feel more playful")),
        CardSpec("Intimacy", parts("makes emotional closeness feel easy|creates enough time for private connection|makes non-sexual touch feel meaningful|is present when you are physically close|helps you feel relaxed enough to be vulnerable|makes quiet moments together feel comfortable|can be affectionate without expecting it to lead to sex|makes private time feel different from ordinary routine|notices when you want closeness|helps you feel emotionally safe when you open up"), parts("at the end of the day|when you are both tired|when you have had little time together|when life feels hectic|when one of you is stressed|when you are lying in bed|during a quiet evening together|after a vulnerable conversation|when you have been apart|when you want closeness without talking much")),
        CardSpec("Trust & Security", parts("keeps their word|is honest even when the truth is awkward|makes you feel secure about the relationship|respects information you tell them privately|acts in ways that match what they say|makes it easy to trust their intentions|handles your vulnerability carefully|respects boundaries consistently|makes you feel confident in their loyalty|shows up when they say they will"), parts("when you are apart|when plans change|when other people are involved|when something is difficult to admit|when one of you feels insecure|when you need a clear answer|when you have to rely on each other|when there is social pressure|when emotions are running high|when keeping a promise is inconvenient")),
        CardSpec("Conflict & Repair", parts("stays respectful during disagreements|can pause before saying something hurtful|comes back to the issue after cooling down|apologises clearly when they are wrong|can separate the problem from the person|does not drag unrelated issues into an argument|tries to understand before defending themselves|helps repair the mood after conflict|can compromise without keeping score|lets an issue stay resolved once it is genuinely resolved"), parts("when you are both frustrated|when one of you feels criticised|when the issue has come up before|when the disagreement starts over something small|when one of you needs a pause|when you have different priorities|when you are short on time|when the conversation becomes emotional|when one of you misunderstood the other|when you need to make a decision quickly")),
        CardSpec("Support & Stress", parts("knows when to listen instead of giving advice|notices when you are struggling|encourages you without putting pressure on you|helps practically when you are overwhelmed|takes your stress seriously|can reassure you without dismissing the problem|supports your ambitions|makes you feel less alone when things are difficult|asks what you need rather than guessing|helps you recover after a bad day"), parts("when work or study is intense|when you are doubting yourself|when you are exhausted|when you are angry about something outside the relationship|when you have too many decisions to make|when you need motivation|when you are disappointed|when you are anxious about something important|when you need practical help|when you need comfort more than solutions")),
        CardSpec("Daily Life", parts("shares everyday responsibilities fairly|helps keep routines manageable|makes decisions about daily life with you|notices jobs that need doing without always being asked|respects your preferred routines|helps home life feel like teamwork|can compromise on small daily preferences|makes enough room for quality time in normal weeks|contributes fairly when one person is busier|helps ordinary life feel calm rather than chaotic"), parts("during weekday mornings|around meals|when chores pile up|when one of you is very busy|when you have different schedules|when plans change at the last minute|when the house feels messy|when you are both tired|when deciding what to do with a free evening|when planning the week")),
        CardSpec("Fun & Play", parts("makes ordinary days more fun|is willing to be silly with you|can laugh at themselves|brings enough spontaneity into the relationship|is open to trying unfamiliar things|makes you feel playful|can turn a boring task into something enjoyable|shares enough humour with you|is up for low-effort fun as well as big plans|makes you feel like the two of you have your own world"), parts("on a quiet weekend|when plans fall through|during a long journey|when you are stuck at home|after a stressful week|when you have very little money to spend|when the weather ruins the plan|when you are both tired|when you have an unexpected free evening|when you need cheering up")),
        CardSpec("Future", parts("includes you naturally in their future plans|takes shared goals seriously|is willing to talk about long-term decisions|makes the future feel exciting rather than uncertain|can compromise about what your future should look like|understands the kind of life you want|wants to build traditions with you|can discuss big changes without avoiding them|thinks about the relationship when making major decisions|makes you feel like you are building toward something together"), parts("when talking about where to live|when discussing careers|when discussing money|when discussing family|when talking about travel|when life plans change|when one person's opportunity affects both of you|when talking about the next few years|when deciding what to prioritise|when imagining an ordinary future together")),
        CardSpec("Money", parts("can talk about money without becoming defensive|respects your spending priorities|takes saving seriously enough for shared goals|is transparent about shared expenses|can compromise on what is worth spending on|makes financial decisions with the relationship in mind|handles unexpected costs calmly|has money habits you can realistically build around|can discuss expensive purchases before making them|understands what financial security means to you"), parts("when money is tight|when you are saving for something|when one of you earns more|when an unexpected bill appears|when planning a trip|when buying gifts|when deciding on a large purchase|when talking about long-term goals|when one of you wants to spend and the other wants to save|when family finances affect your plans")),
        CardSpec("Values", parts("acts with loyalty toward the relationship|is honest when honesty is inconvenient|treats you with respect under pressure|supports your independence|acts generously without keeping score|takes fairness seriously|protects the relationship from unnecessary drama|can prioritise kindness without avoiding hard truths|respects values that matter deeply to you|makes choices that fit the principles they talk about"), parts("when nobody else would know|when emotions are high|when friends disagree with you|when family pressure is involved|when money is involved|when one person has more power in a decision|when doing the right thing is inconvenient|when you have different priorities|when a boundary is being tested|when there is no easy compromise")),
        CardSpec("Family & Friends", parts("supports you maintaining close friendships|makes social plans feel balanced|helps you feel included around their friends|handles different social energy levels well|respects when you do not want to attend something|is comfortable with you socialising separately|can leave an event when you have had enough|makes you feel like a team in group settings|respects what you want kept private around other people|balances couple time and social time well"), parts("at parties|around close friends|during family gatherings|when one of you wants to stay home|when you are meeting new people|when plans are made at short notice|when the social calendar gets busy|when one of you is more outgoing|when someone asks personal questions|when you have separate plans")),
        CardSpec("Appreciation", parts("notices the effort you put into the relationship|thanks you for everyday things|makes you feel that your contribution matters|recognises emotional effort as well as practical effort|compliments qualities beyond your appearance|acknowledges when you have had a difficult week|notices when you are trying to improve something|makes appreciation feel specific rather than generic|shows gratitude without needing to be reminded|makes you feel seen for the things you quietly do"), parts("during normal routines|when life gets busy|after you help them|when you are both tired|when your effort is not obvious|when one of you is stressed|after you compromise|when you support one of their goals|when you handle something difficult|when you do something small but thoughtful")),
        CardSpec("Growth", parts("is willing to work on relationship habits|can hear constructive feedback without shutting down|encourages you to grow as an individual|takes responsibility for their own patterns|is open to changing routines that are not working|supports new goals without making them about the relationship|can admit when something needs improvement|keeps learning how to love you better|does not expect the relationship to run on autopilot|makes growth feel collaborative rather than critical"), parts("after a difficult period|when the same issue repeats|when one of you changes|when priorities shift|when life becomes more demanding|when feedback is uncomfortable|when one of you wants something different|when a habit is deeply established|when progress is slow|when the relationship is already going well")),
    )
}
