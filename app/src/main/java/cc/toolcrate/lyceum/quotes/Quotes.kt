package cc.toolcrate.lyceum.quotes

/** What a passage speaks to. The quote shown after saving is chosen by theme. */
enum class Theme { Habit, Endurance, Balance, Practice, Joy, Choice }

/**
 * A passage from the Nicomachean Ethics, quoted exactly from H. Rackham's translation
 * (Loeb Classical Library, 1926) as published by the Perseus Digital Library (CC BY-SA 4.0).
 * "…" marks where a sentence is entered or left part way. These are the same verified
 * passages Stagira uses; check new ones the same way before adding them.
 */
data class Quote(val text: String, val ref: String, val theme: Theme) {
    val citation: String get() = "Aristotle, Nicomachean Ethics $ref"
}

object Quotes {
    val all: List<Quote> = listOf(
        // Habit: who we become by what we repeatedly do.
        Quote("In a word, our moral dispositions are formed as a result of the corresponding activities.", "II.1, 1103b", Theme.Habit),
        Quote("They acquire a particular quality by constantly acting in a particular way.", "III.5, 1114a", Theme.Habit),
        Quote("…men become builders by building houses, harpers by playing on the harp.", "II.1, 1103a", Theme.Habit),
        Quote("Similarly we become just by doing just acts, temperate by doing temperate acts, brave by doing brave acts.", "II.1, 1103b", Theme.Habit),
        Quote("Mark me, my friend, ’tis long-continued training, And training in the end becomes men’s nature.", "VII.10, 1152a", Theme.Habit),
        Quote("…virtue results from the repeated performance of just and temperate actions.", "II.4, 1105b", Theme.Habit),

        // Endurance: heavy days, misfortune, patience.
        Quote(
            "Yet nevertheless even in adversity nobility shines through, when a man endures repeated and severe " +
                "misfortune with patience, not owing to insensibility but from generosity and greatness of soul.",
            "I.10, 1100b", Theme.Endurance,
        ),
        Quote(
            "…the truly good and wise man will bear all kinds of fortune in a seemly way, and will always act in " +
                "the noblest manner that the circumstances allow…",
            "I.10, 1101a", Theme.Endurance,
        ),
        Quote("…one swallow does not make spring, nor does one fine day…", "I.7, 1098a", Theme.Endurance),
        Quote(
            "It is not surprising that a man should be overcome by violent and excessive pleasures or pains: " +
                "indeed it is excusable if he succumbs after a struggle…",
            "VII.7, 1150b", Theme.Endurance,
        ),
        Quote("…in this working out of details Time seems to be a good inventor or at all events coadjutor.", "I.7, 1098a", Theme.Endurance),

        // Balance: too much, too little, the middle course.
        Quote("This is why it is a hard task to be good, for it is hard to find the middle point in anything…", "II.9, 1109a", Theme.Balance),
        Quote("…it is easy to fail and difficult to succeed—easy to miss the target and difficult to hit it…", "II.6, 1106b", Theme.Balance),
        Quote(
            "However, we do not blame one who diverges a little from the right course, whether on the side of " +
                "the too much or of the too little, but one who diverges more widely…",
            "II.9, 1109b", Theme.Balance,
        ),
        Quote(
            "Hence, inasmuch as to hit the mean extremely well is difficult, the second best way to sail, as " +
                "the saying goes, is to take the least of the evils…",
            "II.9, 1109a", Theme.Balance,
        ),
        Quote(
            "…we must drag ourselves away in the opposite direction, for by steering wide of our besetting " +
                "error we shall make a middle course.",
            "II.9, 1109b", Theme.Balance,
        ),

        // Practice: learning by doing, beginning roughly.
        Quote("…for no doubt the proper procedure is to begin by making a rough sketch, and to fill it in afterwards.", "I.7, 1098a", Theme.Practice),
        Quote(
            "The virtues on the other hand we acquire by first having actually practised them, just as we do the arts.",
            "II.1, 1103a", Theme.Practice,
        ),
        Quote(
            "…in the practical sciences the end is not to attain a theoretic knowledge of the various subjects, " +
                "but rather to carry out our theories in action.",
            "X.9, 1179b", Theme.Practice,
        ),
        Quote("…as you will become a good builder from building well, so you will become a bad one from building badly.", "II.1, 1103b", Theme.Practice),
        Quote(
            "This is shown by the way in which men train themselves for some contest or pursuit: they practice continually.",
            "III.5, 1114a", Theme.Practice,
        ),

        // Joy: the pleasure of doing good things well.
        Quote("And further, the life of active virtue is essentially pleasant.", "I.8, 1099a", Theme.Joy),
        Quote("…those who work with pleasure always work with more discernment and with greater accuracy…", "X.5, 1175a", Theme.Joy),
        Quote("…to do noble and virtuous deeds is a thing desirable for its own sake.", "X.6, 1176b", Theme.Joy),
        Quote("The doer’s achievement therefore remains, for nobility or beauty is long-lived…", "IX.7, 1168a", Theme.Joy),
        Quote(
            "Still, even if happiness is not sent us from heaven, but is won by virtue and by some kind of " +
                "study or practice, it seems to be one of the most divine things that exist.",
            "I.9, 1099b", Theme.Joy,
        ),

        // Choice: what is up to us.
        Quote(
            "For where we are free to act we are also free to refrain from acting, and where we are able to say No " +
                "we are also able to say Yes…",
            "III.5, 1113b", Theme.Choice,
        ),
        Quote("…it consequently depends on us whether we are virtuous or vicious.", "III.5, 1113b", Theme.Choice),
        Quote(
            "Hence it is incumbent on us to control the character of our activities, since on the quality of these " +
                "depends the quality of our dispositions.",
            "II.1, 1103b", Theme.Choice,
        ),
        Quote(
            "The second rule is to notice what are the errors to which we are ourselves most prone (as different " +
                "men are inclined by nature to different faults)…",
            "II.9, 1109b", Theme.Choice,
        ),
    )

    fun ofTheme(theme: Theme): List<Quote> = all.filter { it.theme == theme }
}
