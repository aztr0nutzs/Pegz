class AudioManager(context: Context) {

    private val pool = SoundPool.Builder().setMaxStreams(6).build()

    val jump = pool.load(context, R.raw.jump, 1)
    val squelch = pool.load(context, R.raw.squelch, 1)
    val slide = pool.load(context, R.raw.slide, 1)
    val combo = pool.load(context, R.raw.combo, 1)

    fun play(id: Int) {
        pool.play(id, 1f, 1f, 1, 0, 1f)
    }
}
